package com.lobai.llm.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lobai.llm.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI LLM Provider
 *
 * OpenAI Chat Completions API (/v1/chat/completions) 구현체.
 */
@Slf4j
@Component
public class OpenAiLlmProvider implements LlmProvider {

    private final LlmConfig llmConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public OpenAiLlmProvider(LlmConfig llmConfig, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.llmConfig = llmConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder().build();
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        LlmConfig.ProviderConfig config = getConfig();
        if (config == null) {
            throw new IllegalStateException("OpenAI provider not configured");
        }

        try {
            Map<String, Object> requestBody = buildRequestBody(request, config);
            String url = config.getApiUrl() + "/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            return parseResponse(response.getBody(), config);

        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
            throw new RuntimeException("OpenAI API 호출 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<String> generateStream(LlmRequest request) {
        LlmConfig.ProviderConfig config = getConfig();
        if (config == null) {
            return Flux.error(new IllegalStateException("OpenAI provider not configured"));
        }

        try {
            Map<String, Object> requestBody = buildRequestBody(request, config);
            requestBody.put("stream", true);

            String url = config.getApiUrl() + "/chat/completions";

            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .filter(line -> !line.equals("[DONE]"))
                    .map(chunk -> {
                        try {
                            // SSE format: data: {...}
                            String json = chunk.startsWith("data: ") ? chunk.substring(6) : chunk;
                            if (json.trim().equals("[DONE]")) return "";
                            JsonNode node = objectMapper.readTree(json);
                            JsonNode delta = node.path("choices").path(0).path("delta").path("content");
                            return delta.isMissingNode() ? "" : delta.asText("");
                        } catch (Exception e) {
                            return "";
                        }
                    })
                    .filter(text -> !text.isEmpty());
        } catch (Exception e) {
            return Flux.error(new RuntimeException("OpenAI streaming 실패: " + e.getMessage(), e));
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
        return llmConfig.getProviderConfig("openai");
    }

    private Map<String, Object> buildRequestBody(LlmRequest request, LlmConfig.ProviderConfig config) {
        Map<String, Object> requestBody = new HashMap<>();
        String model = request.getModelOverride() != null ? request.getModelOverride() : config.getModel();
        requestBody.put("model", model);
        requestBody.put("temperature", request.getTemperature());
        requestBody.put("max_tokens", request.getMaxOutputTokens());

        List<Map<String, Object>> messages = new ArrayList<>();

        // System message
        if (request.getSystemInstruction() != null) {
            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", request.getSystemInstruction());
            messages.add(systemMsg);
        }

        // Conversation history
        if (request.getConversationHistory() != null) {
            for (LlmMessage msg : request.getConversationHistory()) {
                Map<String, Object> m = new HashMap<>();
                m.put("role", mapRole(msg.getRole()));
                m.put("content", msg.getContent());
                messages.add(m);
            }
        }

        // Current user message
        if (request.getUserMessage() != null) {
            if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
                // Multimodal message with images
                Map<String, Object> userMsg = new HashMap<>();
                userMsg.put("role", "user");

                List<Map<String, Object>> contentParts = new ArrayList<>();
                Map<String, Object> textPart = new HashMap<>();
                textPart.put("type", "text");
                textPart.put("text", request.getUserMessage());
                contentParts.add(textPart);

                for (LlmMessage.Attachment att : request.getAttachments()) {
                    Map<String, Object> imagePart = new HashMap<>();
                    imagePart.put("type", "image_url");
                    Map<String, String> imageUrl = new HashMap<>();
                    imageUrl.put("url", "data:" + att.getMimeType() + ";base64," + att.getBase64Data());
                    imagePart.put("image_url", imageUrl);
                    contentParts.add(imagePart);
                }

                userMsg.put("content", contentParts);
                messages.add(userMsg);
            } else {
                Map<String, Object> userMsg = new HashMap<>();
                userMsg.put("role", "user");
                userMsg.put("content", request.getUserMessage());
                messages.add(userMsg);
            }
        }

        requestBody.put("messages", messages);

        // JSON mode
        if (request.isJsonMode()) {
            Map<String, String> responseFormat = new HashMap<>();
            responseFormat.put("type", "json_object");
            requestBody.put("response_format", responseFormat);
        }

        // Function Calling (convert from Gemini format to OpenAI format)
        if (request.getTools() != null && !request.getTools().isEmpty()) {
            List<Map<String, Object>> openAiTools = convertToolsToOpenAiFormat(request.getTools());
            if (!openAiTools.isEmpty()) {
                requestBody.put("tools", openAiTools);
            }
        }

        return requestBody;
    }

    private String mapRole(LlmMessage.Role role) {
        return switch (role) {
            case USER -> "user";
            case ASSISTANT -> "assistant";
            case SYSTEM -> "system";
        };
    }

    private List<Map<String, Object>> convertToolsToOpenAiFormat(List<Map<String, Object>> geminiTools) {
        List<Map<String, Object>> openAiTools = new ArrayList<>();

        for (Map<String, Object> tool : geminiTools) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> functionDeclarations =
                    (List<Map<String, Object>>) tool.get("function_declarations");
            if (functionDeclarations == null) continue;

            for (Map<String, Object> funcDecl : functionDeclarations) {
                Map<String, Object> openAiTool = new HashMap<>();
                openAiTool.put("type", "function");

                Map<String, Object> function = new HashMap<>();
                function.put("name", funcDecl.get("name"));
                function.put("description", funcDecl.get("description"));
                function.put("parameters", funcDecl.get("parameters"));
                openAiTool.put("function", function);

                openAiTools.add(openAiTool);
            }
        }

        return openAiTools;
    }

    private LlmResponse parseResponse(String responseBody, LlmConfig.ProviderConfig config) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode choiceNode = jsonNode.path("choices").get(0);
            JsonNode messageNode = choiceNode.path("message");
            String finishReason = choiceNode.path("finish_reason").asText("unknown");

            // Function Call 확인
            JsonNode toolCalls = messageNode.path("tool_calls");
            if (!toolCalls.isMissingNode() && toolCalls.size() > 0) {
                JsonNode firstCall = toolCalls.get(0).path("function");
                return LlmResponse.builder()
                        .providerName("openai")
                        .modelUsed(config.getModel())
                        .finishReason(finishReason)
                        .functionCall(LlmResponse.FunctionCall.builder()
                                .name(firstCall.path("name").asText())
                                .argsJson(firstCall.path("arguments").asText())
                                .build())
                        .usage(extractUsage(jsonNode))
                        .build();
            }

            // 일반 텍스트 응답
            String content = messageNode.path("content").asText("");

            return LlmResponse.builder()
                    .content(content)
                    .providerName("openai")
                    .modelUsed(config.getModel())
                    .finishReason(finishReason)
                    .usage(extractUsage(jsonNode))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("OpenAI 응답 파싱 실패: " + e.getMessage(), e);
        }
    }

    private LlmResponse.Usage extractUsage(JsonNode jsonNode) {
        JsonNode usageNode = jsonNode.path("usage");
        if (usageNode.isMissingNode()) {
            return LlmResponse.Usage.builder().build();
        }

        int promptTokens = usageNode.path("prompt_tokens").asInt(0);
        int completionTokens = usageNode.path("completion_tokens").asInt(0);
        int totalTokens = usageNode.path("total_tokens").asInt(0);

        // GPT-4o-mini 비용: $0.15/1M input, $0.60/1M output
        BigDecimal inputCost = BigDecimal.valueOf(promptTokens)
                .multiply(BigDecimal.valueOf(0.00000015));
        BigDecimal outputCost = BigDecimal.valueOf(completionTokens)
                .multiply(BigDecimal.valueOf(0.0000006));
        BigDecimal totalCost = inputCost.add(outputCost).setScale(6, RoundingMode.HALF_UP);

        return LlmResponse.Usage.builder()
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .estimatedCostUsd(totalCost)
                .build();
    }
}
