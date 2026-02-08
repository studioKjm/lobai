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
}
