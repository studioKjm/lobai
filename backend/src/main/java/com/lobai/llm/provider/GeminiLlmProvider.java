package com.lobai.llm.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lobai.llm.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Google Gemini LLM Provider
 *
 * 기존 GeminiService의 HTTP 클라이언트 로직을 추출한 구현체.
 */
@Slf4j
@Component
public class GeminiLlmProvider implements LlmProvider {

    private final LlmConfig llmConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public GeminiLlmProvider(LlmConfig llmConfig, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.llmConfig = llmConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder().build();
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        LlmConfig.ProviderConfig config = getConfig();
        if (config == null) {
            throw new IllegalStateException("Gemini provider not configured");
        }

        try {
            // 1. Request Body 생성
            Map<String, Object> requestBody = buildRequestBody(request, config);

            // 2. HTTP Request (with retry)
            String url = buildUrl(config, request.getModelOverride(), false);
            String responseBody = executeWithRetry(url, requestBody, 3);

            // 3. Response 파싱
            return parseResponse(responseBody, config);

        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            throw new RuntimeException("Gemini API 호출 실패: " + e.getMessage(), e);
        }
    }

    /** Function Call 시그널 접두사 (스트리밍 청크에서 FC 감지용) */
    public static final String FC_SIGNAL_PREFIX = "\0__FC__:";

    @Override
    public Flux<String> generateStream(LlmRequest request) {
        LlmConfig.ProviderConfig config = getConfig();
        if (config == null) {
            return Flux.error(new IllegalStateException("Gemini provider not configured"));
        }

        try {
            Map<String, Object> requestBody = buildRequestBody(request, config);
            String url = buildUrl(config, request.getModelOverride(), true);

            boolean hasTools = request.getTools() != null && !request.getTools().isEmpty();
            log.info("Gemini streaming request: hasTools={}, url={}", hasTools, url.replaceAll("key=[^&]+", "key=***"));

            // DataBuffer 기반 raw SSE 파싱: 네트워크 청크 도착 즉시 emit
            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(DataBuffer.class)
                    .map(buffer -> {
                        byte[] bytes = new byte[buffer.readableByteCount()];
                        buffer.read(bytes);
                        DataBufferUtils.release(buffer);
                        return new String(bytes, StandardCharsets.UTF_8);
                    })
                    .concatMap(raw -> {
                        // SSE data: 라인 추출
                        List<String> payloads = new ArrayList<>();
                        for (String line : raw.split("\n")) {
                            String trimmed = line.trim();
                            if (trimmed.startsWith("data:")) {
                                String payload = trimmed.substring(5).trim();
                                if (!payload.isEmpty()) {
                                    payloads.add(payload);
                                }
                            }
                        }
                        return Flux.fromIterable(payloads);
                    })
                    .map(chunk -> {
                        try {
                            JsonNode node = objectMapper.readTree(chunk);
                            JsonNode partsNode = node.path("candidates").path(0)
                                    .path("content").path("parts").path(0);

                            // Function Call 감지: functionCall이 있으면 시그널로 전달
                            if (partsNode.has("functionCall")) {
                                JsonNode fcNode = partsNode.path("functionCall");
                                log.info("Function Call detected in stream chunk: {}", fcNode.toString());
                                return FC_SIGNAL_PREFIX + fcNode.toString();
                            }

                            JsonNode textNode = partsNode.path("text");
                            String text = textNode.isMissingNode() ? "" : textNode.asText();

                            return text;
                        } catch (Exception e) {
                            log.debug("Skipping unparseable stream chunk: {}", e.getMessage());
                            return "";
                        }
                    })
                    .filter(text -> !text.isEmpty());
        } catch (Exception e) {
            return Flux.error(new RuntimeException("Gemini streaming 실패: " + e.getMessage(), e));
        }
    }

    @Override
    public boolean supportsFeature(LlmFeature feature) {
        return switch (feature) {
            case FUNCTION_CALLING, IMAGE_INPUT, STREAMING, JSON_MODE -> true;
        };
    }

    // ===== Internal Methods =====

    private LlmConfig.ProviderConfig getConfig() {
        return llmConfig.getProviderConfig("gemini");
    }

    private String buildUrl(LlmConfig.ProviderConfig config, String modelOverride, boolean stream) {
        String model = modelOverride != null ? modelOverride : config.getModel();
        String action = stream ? "streamGenerateContent" : "generateContent";
        String streamParam = stream ? "&alt=sse" : "";
        return String.format("%s/%s:%s?key=%s%s",
                config.getApiUrl(), model, action, config.getApiKey(), streamParam);
    }

    Map<String, Object> buildRequestBody(LlmRequest request, LlmConfig.ProviderConfig config) {
        Map<String, Object> requestBody = new HashMap<>();

        // System instruction
        if (request.getSystemInstruction() != null) {
            Map<String, Object> systemInstructionPart = new HashMap<>();
            Map<String, String> systemInstructionText = new HashMap<>();
            systemInstructionText.put("text", request.getSystemInstruction());
            systemInstructionPart.put("parts", List.of(systemInstructionText));
            requestBody.put("system_instruction", systemInstructionPart);
        }

        // Contents (conversation history + current message)
        List<Map<String, Object>> contents = new ArrayList<>();

        if (request.getConversationHistory() != null) {
            for (LlmMessage msg : request.getConversationHistory()) {
                Map<String, Object> content = new HashMap<>();
                String role = msg.getRole() == LlmMessage.Role.USER ? "user" : "model";
                content.put("role", role);

                Map<String, String> part = new HashMap<>();
                part.put("text", msg.getContent());
                content.put("parts", List.of(part));
                contents.add(content);
            }
        }

        // Current user message with optional attachments
        if (request.getUserMessage() != null) {
            Map<String, Object> currentContent = new HashMap<>();
            currentContent.put("role", "user");

            List<Map<String, Object>> currentParts = new ArrayList<>();
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", request.getUserMessage());
            currentParts.add(textPart);

            // Image attachments
            if (request.getAttachments() != null) {
                for (LlmMessage.Attachment att : request.getAttachments()) {
                    Map<String, Object> imagePart = new HashMap<>();
                    Map<String, Object> inlineData = new HashMap<>();
                    inlineData.put("mime_type", att.getMimeType());
                    inlineData.put("data", att.getBase64Data());
                    imagePart.put("inline_data", inlineData);
                    currentParts.add(imagePart);
                }
            }

            currentContent.put("parts", currentParts);
            contents.add(currentContent);
        }

        requestBody.put("contents", contents);

        // Generation config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", request.getTemperature());
        generationConfig.put("maxOutputTokens", request.getMaxOutputTokens());
        if (request.isJsonMode()) {
            generationConfig.put("responseMimeType", "application/json");
        }
        requestBody.put("generationConfig", generationConfig);

        // Tools (Function Calling)
        if (request.getTools() != null && !request.getTools().isEmpty()) {
            requestBody.put("tools", request.getTools());
        }

        return requestBody;
    }

    private String executeWithRetry(String url, Map<String, Object> requestBody, int maxRetries) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.POST, entity, String.class);
                return response.getBody();
            } catch (HttpServerErrorException.ServiceUnavailable e) {
                retryCount++;
                log.warn("Gemini API 503 error, retry {}/{}", retryCount, maxRetries);
                if (retryCount >= maxRetries) throw e;
                try {
                    Thread.sleep(1000L * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
        throw new RuntimeException("Gemini API max retries exceeded");
    }

    private LlmResponse parseResponse(String responseBody, LlmConfig.ProviderConfig config) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode candidateNode = jsonNode.path("candidates").get(0);
            JsonNode contentNode = candidateNode.path("content");
            JsonNode partsNode = contentNode.path("parts");

            String finishReason = candidateNode.path("finishReason").asText("UNKNOWN");

            // Function Call 확인
            if (partsNode.size() > 0 && partsNode.get(0).has("functionCall")) {
                JsonNode functionCallNode = partsNode.get(0).path("functionCall");
                String functionName = functionCallNode.path("name").asText();
                String argsJson = functionCallNode.path("args").toString();

                return LlmResponse.builder()
                        .providerName("gemini")
                        .modelUsed(config.getModel())
                        .finishReason(finishReason)
                        .functionCall(LlmResponse.FunctionCall.builder()
                                .name(functionName)
                                .argsJson(argsJson)
                                .build())
                        .usage(extractUsage(jsonNode))
                        .build();
            }

            // 일반 텍스트 응답
            String textContent = partsNode.get(0).path("text").asText();

            return LlmResponse.builder()
                    .content(textContent)
                    .providerName("gemini")
                    .modelUsed(config.getModel())
                    .finishReason(finishReason)
                    .usage(extractUsage(jsonNode))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Gemini 응답 파싱 실패: " + e.getMessage(), e);
        }
    }

    private LlmResponse.Usage extractUsage(JsonNode jsonNode) {
        JsonNode usageNode = jsonNode.path("usageMetadata");
        if (usageNode.isMissingNode()) {
            return LlmResponse.Usage.builder().build();
        }

        int promptTokens = usageNode.path("promptTokenCount").asInt(0);
        int completionTokens = usageNode.path("candidatesTokenCount").asInt(0);
        int totalTokens = usageNode.path("totalTokenCount").asInt(0);

        // Gemini Flash 비용 추정: $0.075/1M input, $0.30/1M output
        BigDecimal inputCost = BigDecimal.valueOf(promptTokens)
                .multiply(BigDecimal.valueOf(0.000000075));
        BigDecimal outputCost = BigDecimal.valueOf(completionTokens)
                .multiply(BigDecimal.valueOf(0.0000003));
        BigDecimal totalCost = inputCost.add(outputCost).setScale(6, RoundingMode.HALF_UP);

        return LlmResponse.Usage.builder()
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .estimatedCostUsd(totalCost)
                .build();
    }

    /**
     * Function Call 결과와 함께 대화 계속하기
     * (GeminiService에서 이관)
     */
    public LlmResponse continueWithFunctionResult(LlmRequest originalRequest,
                                                    String functionName,
                                                    String functionResult) {
        LlmConfig.ProviderConfig config = getConfig();
        if (config == null) {
            throw new IllegalStateException("Gemini provider not configured");
        }

        try {
            Map<String, Object> requestBody = buildRequestBody(originalRequest, config);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> contents = (List<Map<String, Object>>) requestBody.get("contents");

            // Model's function call
            Map<String, Object> modelFunctionCall = new HashMap<>();
            modelFunctionCall.put("role", "model");
            Map<String, Object> functionCallPart = new HashMap<>();
            Map<String, Object> functionCallData = new HashMap<>();
            functionCallData.put("name", functionName);
            functionCallPart.put("functionCall", functionCallData);
            modelFunctionCall.put("parts", List.of(functionCallPart));
            contents.add(modelFunctionCall);

            // User's function response
            Map<String, Object> userFunctionResponse = new HashMap<>();
            userFunctionResponse.put("role", "user");
            Map<String, Object> functionResponsePart = new HashMap<>();
            Map<String, Object> functionResponseData = new HashMap<>();
            functionResponseData.put("name", functionName);
            Map<String, String> responseContent = new HashMap<>();
            responseContent.put("content", functionResult);
            functionResponseData.put("response", responseContent);
            functionResponsePart.put("functionResponse", functionResponseData);
            userFunctionResponse.put("parts", List.of(functionResponsePart));
            contents.add(userFunctionResponse);

            String url = buildUrl(config, originalRequest.getModelOverride(), false);
            String responseBody = executeWithRetry(url, requestBody, 3);
            return parseResponse(responseBody, config);

        } catch (Exception e) {
            log.error("Failed to continue with function result", e);
            return LlmResponse.builder()
                    .content(functionResult)
                    .providerName("gemini")
                    .modelUsed(config.getModel())
                    .build();
        }
    }
}
