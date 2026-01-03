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
import org.springframework.web.client.RestTemplate;

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

            // 4. HTTP Request 전송
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = geminiConfig.getGenerateContentUrl();
            log.debug("Calling Gemini API: {} with {} history messages", url,
                     conversationHistory != null ? conversationHistory.size() : 0);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

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
     */
    private String buildSystemInstruction(Persona persona, Integer hunger, Integer energy, Integer happiness) {
        StringBuilder instruction = new StringBuilder();

        // 페르소나의 기본 instruction
        instruction.append(persona.getSystemInstruction());
        instruction.append("\n\n");

        // 현재 상태 정보
        instruction.append(String.format(
                "현재 Lobi(AI 로봇)의 상태:\n" +
                "- 배고픔: %d%%\n" +
                "- 에너지: %d%%\n" +
                "- 행복도: %d%%\n\n",
                hunger, energy, happiness
        ));

        // 상태에 따른 추가 지침
        if (hunger < 30) {
            instruction.append("현재 매우 배고픈 상태입니다. 배고픔을 자연스럽게 표현하세요.\n");
        }
        if (energy < 30) {
            instruction.append("현재 매우 피곤한 상태입니다. 에너지 부족을 자연스럽게 표현하세요.\n");
        }
        if (happiness < 30) {
            instruction.append("현재 기분이 좋지 않은 상태입니다. 우울함을 자연스럽게 표현하세요.\n");
        }

        instruction.append("\n답변은 1-2문장으로 짧고 간결하게 작성하세요.");

        return instruction.toString();
    }
}
