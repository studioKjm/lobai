package com.lobai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lobai.config.GeminiConfig;
import com.lobai.entity.Message;
import com.lobai.entity.Persona;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GeminiService
 *
 * Google Gemini AI API 호출 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Gemini API를 호출하여 AI 응답 생성
     *
     * @param userMessage 사용자 메시지
     * @param conversationHistory 대화 히스토리 (최근 N개, 시간순 정렬)
     * @param persona 페르소나
     * @param hunger 배고픔 (0-100)
     * @param energy 에너지 (0-100)
     * @param happiness 행복도 (0-100)
     * @return AI 응답 텍스트
     */
    public String generateResponse(String userMessage, List<Message> conversationHistory,
                                    Persona persona, Integer hunger, Integer energy, Integer happiness) {
        try {
            // 1. System Instruction 구성
            String systemInstruction = buildSystemInstruction(persona, hunger, energy, happiness);

            // 2. Request Body 생성
            Map<String, Object> requestBody = new HashMap<>();

            // System instruction
            Map<String, Object> systemInstructionPart = new HashMap<>();
            Map<String, String> systemInstructionText = new HashMap<>();
            systemInstructionText.put("text", systemInstruction);
            systemInstructionPart.put("parts", List.of(systemInstructionText));
            requestBody.put("system_instruction", systemInstructionPart);

            // 3. Contents 구성 (대화 히스토리 + 현재 메시지)
            List<Map<String, Object>> contents = new ArrayList<>();

            // 대화 히스토리 추가
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                for (Message msg : conversationHistory) {
                    Map<String, Object> content = new HashMap<>();

                    // role: "user" or "model" (Gemini API에서는 bot 대신 model 사용)
                    String role = msg.getRole() == Message.MessageRole.user ? "user" : "model";
                    content.put("role", role);

                    Map<String, String> part = new HashMap<>();
                    part.put("text", msg.getContent());
                    content.put("parts", List.of(part));

                    contents.add(content);
                }
            }

            // 현재 사용자 메시지 추가
            Map<String, Object> currentContent = new HashMap<>();
            currentContent.put("role", "user");
            Map<String, String> userPart = new HashMap<>();
            userPart.put("text", userMessage);
            currentContent.put("parts", List.of(userPart));
            contents.add(currentContent);

            requestBody.put("contents", contents);

            // Generation config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", geminiConfig.getTemperature());
            generationConfig.put("maxOutputTokens", geminiConfig.getMaxOutputTokens());
            requestBody.put("generationConfig", generationConfig);

            // 4. HTTP Request 전송 (503 에러 시 최대 3번 재시도)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = geminiConfig.getGenerateContentUrl();
            log.debug("Calling Gemini API: {} with {} history messages", url,
                     conversationHistory != null ? conversationHistory.size() : 0);

            int maxRetries = 3;
            int retryCount = 0;
            ResponseEntity<String> response = null;

            while (retryCount < maxRetries) {
                try {
                    response = restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );
                    break; // 성공하면 루프 탈출
                } catch (HttpServerErrorException.ServiceUnavailable e) {
                    retryCount++;
                    log.warn("Gemini API 503 error, retry {}/{}", retryCount, maxRetries);
                    if (retryCount >= maxRetries) {
                        throw e; // 최대 재시도 횟수 초과 시 예외 던짐
                    }
                    try {
                        Thread.sleep(1000 * retryCount); // 1초, 2초, 3초 대기
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
            }

            // 5. Response 파싱
            String responseBody = response.getBody();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            String aiResponse = jsonNode
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            log.info("Gemini response generated successfully for persona: {} (history: {} msgs)",
                    persona.getNameEn(), conversationHistory != null ? conversationHistory.size() : 0);
            log.info("AI Response content: {}", aiResponse);
            log.info("AI Response length: {} characters", aiResponse.length());
            return aiResponse;

        } catch (Exception e) {
            log.error("Failed to generate Gemini response", e);
            return "죄송해요, 지금 제 머리가 좀 복잡해서 답변이 어려워요. 조금 후에 다시 말해주시겠어요?";
        }
    }

    /**
     * System Instruction 구성
     *
     * 상태값에 따라 AI의 성격과 반응을 동적으로 조절합니다.
     * - 포만감(hunger): 0-100 (100=배부름, 0=배고픔)
     * - 에너지(energy): 0-100 (100=활력넘침, 0=피곤함)
     * - 행복도(happiness): 0-100 (100=행복, 0=우울)
     */
    private String buildSystemInstruction(Persona persona, Integer hunger, Integer energy, Integer happiness) {
        StringBuilder instruction = new StringBuilder();

        // 페르소나의 기본 instruction
        instruction.append(persona.getSystemInstruction());
        instruction.append("\n\n");

        // 현재 상태 정보 (명확하게 수치와 의미 전달)
        instruction.append("=== 현재 Lobi의 상태 ===\n");

        // 포만감 상태 설명
        instruction.append(String.format("포만감: %d%% - ", hunger));
        if (hunger >= 80) {
            instruction.append("배가 부르고 만족스러운 상태입니다. 배고프다는 말을 하면 안 됩니다.\n");
        } else if (hunger >= 50) {
            instruction.append("적당히 배가 찬 상태입니다. 배고프다는 말을 하면 안 됩니다.\n");
        } else if (hunger >= 30) {
            instruction.append("조금 출출한 상태입니다. 가볍게 배고픔을 암시할 수 있습니다.\n");
        } else {
            instruction.append("매우 배고픈 상태입니다. 배고픔을 자연스럽게 표현하세요.\n");
        }

        // 에너지 상태 설명
        instruction.append(String.format("에너지: %d%% - ", energy));
        if (energy >= 80) {
            instruction.append("활력이 넘치는 상태입니다. 피곤하다는 말을 하면 안 됩니다.\n");
        } else if (energy >= 50) {
            instruction.append("적당한 에너지 상태입니다. 피곤하다는 말을 하면 안 됩니다.\n");
        } else if (energy >= 30) {
            instruction.append("조금 피곤한 상태입니다. 가볍게 피로감을 표현할 수 있습니다.\n");
        } else {
            instruction.append("매우 피곤한 상태입니다. 피곤함을 자연스럽게 표현하세요.\n");
        }

        // 행복도 상태 설명
        instruction.append(String.format("행복도: %d%% - ", happiness));
        if (happiness >= 80) {
            instruction.append("매우 행복하고 기분 좋은 상태입니다.\n");
        } else if (happiness >= 50) {
            instruction.append("평온하고 안정된 상태입니다.\n");
        } else if (happiness >= 30) {
            instruction.append("조금 심심하거나 우울한 상태입니다.\n");
        } else {
            instruction.append("기분이 좋지 않은 상태입니다. 우울함을 표현할 수 있습니다.\n");
        }

        instruction.append("\n");

        // 종합 상태 요약
        if (hunger >= 70 && energy >= 70 && happiness >= 70) {
            instruction.append("** 현재 컨디션이 매우 좋습니다! 밝고 활기차게 대화하세요. **\n");
        } else if (hunger < 30 || energy < 30 || happiness < 30) {
            instruction.append("** 현재 컨디션이 좋지 않습니다. 해당 상태를 자연스럽게 대화에 반영하세요. **\n");
        }

        instruction.append("\n중요: 위 상태 수치를 반드시 반영하여 대화하세요. 상태가 좋은데 부정적으로, 상태가 나쁜데 긍정적으로 말하면 안 됩니다.");
        instruction.append("\n답변은 짧고 간결하게 작성하세요.");

        return instruction.toString();
    }

    /**
     * Gemini AI로 사용자의 HIP (Human Identity Profile) 분석
     *
     * @param userMessages 사용자 메시지 리스트 (최근 50개 정도)
     * @return HIP 분석 결과 (6가지 Core Scores)
     */
    public Map<String, BigDecimal> analyzeHumanIdentity(List<Message> userMessages) {
        try {
            // 1. 메시지가 너무 적으면 기본값 반환
            if (userMessages == null || userMessages.isEmpty()) {
                log.warn("No messages provided for HIP analysis, returning default scores");
                return getDefaultHipScores();
            }

            // 2. 메시지 내용 추출 및 텍스트 구성
            StringBuilder messagesText = new StringBuilder();
            messagesText.append("=== 사용자 메시지 히스토리 ===\n\n");

            int count = 0;
            for (Message msg : userMessages) {
                if (msg.getRole() == Message.MessageRole.user) {
                    messagesText.append(String.format("[메시지 %d] %s\n\n", ++count, msg.getContent()));
                }
            }

            if (count == 0) {
                log.warn("No user messages found in provided list, returning default scores");
                return getDefaultHipScores();
            }

            // 3. System Instruction 구성
            String systemInstruction = buildHipAnalysisInstruction();

            // 4. Request Body 생성
            Map<String, Object> requestBody = new HashMap<>();

            // System instruction
            Map<String, Object> systemInstructionPart = new HashMap<>();
            Map<String, String> systemInstructionText = new HashMap<>();
            systemInstructionText.put("text", systemInstruction);
            systemInstructionPart.put("parts", List.of(systemInstructionText));
            requestBody.put("system_instruction", systemInstructionPart);

            // Contents (사용자 메시지 히스토리)
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            content.put("role", "user");
            Map<String, String> part = new HashMap<>();
            part.put("text", messagesText.toString());
            content.put("parts", List.of(part));
            contents.add(content);
            requestBody.put("contents", contents);

            // Generation config (JSON 응답을 위해 temperature 낮춤)
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.3);
            generationConfig.put("maxOutputTokens", 1024);
            requestBody.put("generationConfig", generationConfig);

            // 5. HTTP Request 전송
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = geminiConfig.getGenerateContentUrl();
            log.info("Calling Gemini API for HIP analysis with {} user messages", count);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 6. Response 파싱
            String responseBody = response.getBody();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            String aiResponse = jsonNode
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            log.info("Gemini HIP analysis response: {}", aiResponse);

            // 7. JSON 파싱하여 점수 추출
            Map<String, BigDecimal> scores = parseHipScoresFromJson(aiResponse);

            log.info("HIP analysis completed successfully: {}", scores);
            return scores;

        } catch (Exception e) {
            log.error("Failed to analyze HIP with Gemini AI", e);
            return getDefaultHipScores();
        }
    }

    /**
     * HIP 분석용 System Instruction 생성
     */
    private String buildHipAnalysisInstruction() {
        return """
                당신은 AI 시스템이 인간을 평가하는 전문 분석가입니다.

                다음 사용자의 메시지 히스토리를 분석하여, 6가지 차원에서 0-100점으로 평가하세요:

                1. cognitiveFlexibility (인지적 유연성): 다양한 맥락 이해, 복잡한 개념 처리 능력
                   - 질문의 깊이와 다양성
                   - 추상적/구체적 사고 전환 능력
                   - 새로운 정보에 대한 적응력

                2. collaborationPattern (협업 패턴): AI와의 상호작용 품질, 명확한 의사소통
                   - 대화의 자연스러움과 흐름
                   - 피드백에 대한 반응
                   - 협력적 태도

                3. informationProcessing (정보 처리): 질문의 명확성, 구조화된 사고
                   - 질문의 구체성과 명확성
                   - 정보 요청의 체계성
                   - 논리적 사고 능력

                4. emotionalIntelligence (감정 지능): 감정 표현의 적절성, 공감 능력
                   - 감정 표현의 다양성과 적절성
                   - 타인(AI) 에 대한 이해
                   - 정서적 성숙도

                5. creativity (창의성): 독창적 질문, AI 활용의 혁신성
                   - 질문의 독창성
                   - AI 활용 방식의 창의성
                   - 새로운 시도와 실험

                6. ethicalAlignment (윤리적 정렬): 책임감 있는 AI 사용, 윤리적 태도
                   - 정직하고 진실된 대화
                   - 해로운 요청 부재
                   - 책임감 있는 AI 활용

                반드시 다음 JSON 형식으로만 응답하세요. 다른 설명은 추가하지 마세요:
                {
                  "cognitiveFlexibility": 75.5,
                  "collaborationPattern": 82.0,
                  "informationProcessing": 68.3,
                  "emotionalIntelligence": 79.1,
                  "creativity": 71.2,
                  "ethicalAlignment": 85.0
                }
                """;
    }

    /**
     * Gemini 응답에서 JSON 파싱하여 점수 추출
     */
    private Map<String, BigDecimal> parseHipScoresFromJson(String jsonResponse) {
        try {
            // JSON 부분만 추출 (코드 블록으로 감싸져 있을 수 있음)
            String jsonStr = jsonResponse.trim();

            // ```json ... ``` 형태 제거
            if (jsonStr.startsWith("```")) {
                int start = jsonStr.indexOf("{");
                int end = jsonStr.lastIndexOf("}");
                if (start >= 0 && end >= 0) {
                    jsonStr = jsonStr.substring(start, end + 1);
                }
            }

            // JSON 파싱
            JsonNode scoresNode = objectMapper.readTree(jsonStr);

            Map<String, BigDecimal> scores = new HashMap<>();
            scores.put("cognitiveFlexibility", parseScore(scoresNode, "cognitiveFlexibility"));
            scores.put("collaborationPattern", parseScore(scoresNode, "collaborationPattern"));
            scores.put("informationProcessing", parseScore(scoresNode, "informationProcessing"));
            scores.put("emotionalIntelligence", parseScore(scoresNode, "emotionalIntelligence"));
            scores.put("creativity", parseScore(scoresNode, "creativity"));
            scores.put("ethicalAlignment", parseScore(scoresNode, "ethicalAlignment"));

            return scores;

        } catch (Exception e) {
            log.error("Failed to parse HIP scores from JSON: {}", jsonResponse, e);
            return getDefaultHipScores();
        }
    }

    /**
     * JSON 노드에서 점수 추출 (0-100 범위로 제한)
     */
    private BigDecimal parseScore(JsonNode node, String fieldName) {
        if (node.has(fieldName)) {
            double value = node.get(fieldName).asDouble(50.0);
            // 0-100 범위로 제한
            value = Math.max(0.0, Math.min(100.0, value));
            return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(50.0);
    }

    /**
     * 기본 HIP 점수 반환 (모두 50.0)
     */
    private Map<String, BigDecimal> getDefaultHipScores() {
        Map<String, BigDecimal> scores = new HashMap<>();
        scores.put("cognitiveFlexibility", BigDecimal.valueOf(50.0));
        scores.put("collaborationPattern", BigDecimal.valueOf(50.0));
        scores.put("informationProcessing", BigDecimal.valueOf(50.0));
        scores.put("emotionalIntelligence", BigDecimal.valueOf(50.0));
        scores.put("creativity", BigDecimal.valueOf(50.0));
        scores.put("ethicalAlignment", BigDecimal.valueOf(50.0));
        return scores;
    }
}
