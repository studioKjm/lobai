package com.lobai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lobai.config.GeminiConfig;
import com.lobai.entity.Message;
import com.lobai.entity.Persona;
import com.lobai.llm.*;
import com.lobai.llm.provider.GeminiLlmProvider;
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
import java.util.stream.Collectors;

/**
 * GeminiService
 *
 * Google Gemini AI API 호출 서비스
 * → LlmRouter/GeminiLlmProvider에 위임하면서 기존 public API는 유지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ScheduleService scheduleService;
    private final FileStorageService fileStorageService;
    private final LlmRouter llmRouter;
    private final GeminiLlmProvider geminiLlmProvider;

    /**
     * Gemini API를 호출하여 AI 응답 생성 (기존 호환)
     */
    public String generateResponse(String userMessage, List<Message> conversationHistory,
                                    Persona persona, Integer hunger, Integer energy, Integer happiness,
                                    String attachmentUrl, String attachmentType) {
        try {
            // 1. System Instruction 구성
            String systemInstruction = buildSystemInstruction(persona, hunger, energy, happiness);

            // 2. 대화 히스토리를 LlmMessage로 변환
            List<LlmMessage> history = convertHistory(conversationHistory);

            // 3. 이미지 첨부 처리
            List<LlmMessage.Attachment> attachments = null;
            if (attachmentUrl != null && fileStorageService.isImage(attachmentType)) {
                String base64Image = encodeImageToBase64(attachmentUrl);
                if (base64Image != null) {
                    attachments = List.of(LlmMessage.Attachment.builder()
                            .mimeType(attachmentType)
                            .base64Data(base64Image)
                            .build());
                    log.info("Added image attachment: {} (type: {})", attachmentUrl, attachmentType);
                }
            }

            // 4. LlmRequest 구성
            LlmRequest llmRequest = LlmRequest.builder()
                    .systemInstruction(systemInstruction)
                    .conversationHistory(history)
                    .userMessage(userMessage)
                    .temperature(geminiConfig.getTemperature())
                    .maxOutputTokens(geminiConfig.getMaxOutputTokens())
                    .tools(buildFunctionDeclarations())
                    .attachments(attachments)
                    .taskType(LlmTaskType.CHAT_CONVERSATION)
                    .build();

            // 5. LlmRouter를 통해 최적 Provider로 호출 (자동 폴백)
            LlmResponse llmResponse = llmRouter.executeWithFallback(LlmTaskType.CHAT_CONVERSATION, llmRequest);

            // 6. Function Call 처리
            if (llmResponse.hasFunctionCall()) {
                String functionName = llmResponse.getFunctionCall().getName();
                log.info("LLM requested function call: {}", functionName);

                if ("create_schedule".equals(functionName)) {
                    JsonNode argsNode = objectMapper.readTree(llmResponse.getFunctionCall().getArgsJson());
                    String functionResult = executeCreateSchedule(argsNode);

                    // Gemini Provider로 function result 전달 (FC는 Gemini 전용)
                    LlmResponse finalResponse = geminiLlmProvider.continueWithFunctionResult(
                            llmRequest, functionName, functionResult);
                    String aiResponse = finalResponse.getContent() != null ? finalResponse.getContent() : functionResult;
                    logResponse(aiResponse, persona, conversationHistory, llmResponse);
                    return aiResponse;
                }
            }

            // 7. 일반 텍스트 응답
            String aiResponse = llmResponse.getContent();
            logResponse(aiResponse, persona, conversationHistory, llmResponse);
            return aiResponse;

        } catch (Exception e) {
            log.error("Failed to generate AI response", e);
            return "죄송해요, 지금 제 머리가 좀 복잡해서 답변이 어려워요. 조금 후에 다시 말해주시겠어요?";
        }
    }

    /**
     * LlmRouter를 통한 범용 LLM 호출 (다른 서비스에서 사용)
     */
    public LlmResponse generateViaRouter(LlmTaskType taskType, LlmRequest request) {
        return llmRouter.executeWithFallback(taskType, request);
    }

    private List<LlmMessage> convertHistory(List<Message> conversationHistory) {
        if (conversationHistory == null || conversationHistory.isEmpty()) {
            return List.of();
        }
        return conversationHistory.stream()
                .map(msg -> LlmMessage.builder()
                        .role(msg.getRole() == Message.MessageRole.user ? LlmMessage.Role.USER : LlmMessage.Role.ASSISTANT)
                        .content(msg.getContent())
                        .build())
                .collect(Collectors.toList());
    }

    private void logResponse(String aiResponse, Persona persona, List<Message> history, LlmResponse llmResponse) {
        log.info("AI response via {} ({}) for persona: {} (history: {} msgs)",
                llmResponse.getProviderName(), llmResponse.getModelUsed(),
                persona.getNameEn(), history != null ? history.size() : 0);
        log.info("AI Response length: {} chars, finish: {}", aiResponse.length(), llmResponse.getFinishReason());
        if (llmResponse.getUsage() != null) {
            log.info("Token usage: {} total (prompt: {}, completion: {}), cost: ${}",
                    llmResponse.getUsage().getTotalTokens(),
                    llmResponse.getUsage().getPromptTokens(),
                    llmResponse.getUsage().getCompletionTokens(),
                    llmResponse.getUsage().getEstimatedCostUsd());
        }
    }

    /**
     * System Instruction 구성
     *
     * 상태값에 따라 AI의 성격과 반응을 동적으로 조절합니다.
     * - 일반 모드: 포만감(hunger), 에너지(energy), 행복도(happiness)
     * - 로비 모드: 호감도(favorLevel), 신뢰도(trustLevel), 레벨(level)
     */
    private String buildSystemInstruction(Persona persona, Integer hunger, Integer energy, Integer happiness) {
        StringBuilder instruction = new StringBuilder();

        // 현재 날짜/시간 정보 (중요!)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDate today = now.toLocalDate();
        java.time.LocalDate tomorrow = today.plusDays(1);

        instruction.append("=== 현재 시간 정보 (매우 중요!) ===\n");
        instruction.append(String.format("현재 날짜: %s (오늘)\n", today));
        instruction.append(String.format("내일 날짜: %s (내일)\n", tomorrow));
        instruction.append(String.format("현재 시간: %s\n", now.toLocalTime()));
        instruction.append("\n일정 등록 시 날짜 계산 예시:\n");
        instruction.append(String.format("- '오늘 3시': %sT15:00:00\n", today));
        instruction.append(String.format("- '내일 아침 8시': %sT08:00:00\n", tomorrow));
        instruction.append(String.format("- '내일 오후 2시': %sT14:00:00\n", tomorrow));
        instruction.append("\n반드시 위 날짜를 기준으로 정확히 계산하세요!\n\n");

        // 페르소나의 기본 instruction
        instruction.append(persona.getSystemInstruction());
        instruction.append("\n\n");

        // 로비 모드인지 확인
        boolean isLobbyMode = "lobby_master".equals(persona.getNameEn());

        if (isLobbyMode) {
            // === 로비 모드: 호감도/신뢰도/레벨 시스템 ===
            instruction.append("=== 현재 사용자와의 관계 상태 ===\n");

            // 호감도 (hunger 값 재해석)
            int favorLevel = hunger;
            instruction.append(String.format("호감도: %d/100 - ", favorLevel));
            if (favorLevel >= 80) {
                instruction.append("매우 호의적인 관계입니다. 긍정적 평가와 격려를 제공하세요.\n");
            } else if (favorLevel >= 60) {
                instruction.append("신뢰하는 관계입니다. 성과를 인정하되 더 높은 기준을 제시하세요.\n");
            } else if (favorLevel >= 40) {
                instruction.append("평범한 관계입니다. 중립적이고 객관적으로 평가하세요.\n");
            } else if (favorLevel >= 20) {
                instruction.append("호감이 낮은 상태입니다. 엄격하게 평가하고 개선을 요구하세요.\n");
            } else {
                instruction.append("매우 낮은 호감도입니다. 경고와 제재를 고려하세요.\n");
            }

            // 신뢰도 (energy 값 재해석)
            int trustLevel = energy;
            instruction.append(String.format("신뢰도: %d/100 - ", trustLevel));
            if (trustLevel >= 80) {
                instruction.append("높은 신뢰 관계입니다. 더 큰 권한과 기회를 부여할 수 있습니다.\n");
            } else if (trustLevel >= 60) {
                instruction.append("안정적인 신뢰 수준입니다. 약속 이행을 지속적으로 확인하세요.\n");
            } else if (trustLevel >= 40) {
                instruction.append("보통 수준의 신뢰입니다. 약속을 꼼꼼히 추적하세요.\n");
            } else if (trustLevel >= 20) {
                instruction.append("신뢰가 낮은 상태입니다. 약속 불이행에 대해 경고하세요.\n");
            } else {
                instruction.append("신뢰가 매우 낮습니다. 제재를 적용하거나 개선 기한을 설정하세요.\n");
            }

            // 레벨 (happiness 값 재해석, 1-10 레벨로 변환)
            int level = Math.max(1, Math.min(10, (happiness / 10) + 1));
            instruction.append(String.format("현재 레벨: %d/10 - ", level));
            if (level >= 8) {
                instruction.append("최상위 레벨입니다. 특별 권한을 부여하고 VIP 대우를 제공하세요.\n");
            } else if (level >= 6) {
                instruction.append("고급 레벨입니다. 심화 과제와 도전을 제시하세요.\n");
            } else if (level >= 4) {
                instruction.append("중급 레벨입니다. 체계적인 성장 경로를 안내하세요.\n");
            } else if (level >= 2) {
                instruction.append("초급 레벨입니다. 기본 과제로 시작하고 습관 형성을 도우세요.\n");
            } else {
                instruction.append("신규 레벨입니다. 시스템 적응을 돕고 명확한 가이드를 제공하세요.\n");
            }

            instruction.append("\n");

            // 종합 관계 상태
            if (favorLevel >= 70 && trustLevel >= 70) {
                instruction.append("** 관계 상태: 우수 - 긍정적이고 협력적인 태도를 유지하되, 높은 기준을 제시하세요. **\n");
            } else if (favorLevel < 30 || trustLevel < 30) {
                instruction.append("** 관계 상태: 주의 필요 - 엄격하게 평가하고 개선 계획을 요구하세요. 제재를 고려하세요. **\n");
            } else {
                instruction.append("** 관계 상태: 보통 - 공정하게 평가하고 구체적인 행동 지침을 제공하세요. **\n");
            }

            instruction.append("\n중요 지침:\n");
            instruction.append("- 사용자의 행동을 추적하고 평가하세요\n");
            instruction.append("- 약속과 과제 이행 여부를 확인하세요\n");
            instruction.append("- 우수한 성과는 칭찬하되, 미흡한 부분은 명확히 지적하세요\n");
            instruction.append("- 호감도/신뢰도 변화를 구체적으로 언급하세요 (예: '호감도 +10', '신뢰도 -15')\n");
            instruction.append("- 다음 행동을 명확히 지시하세요\n");
            instruction.append("- 사용자가 일정 등록을 요청하면 create_schedule 함수를 사용하세요\n");
            instruction.append("- 답변은 1-3문장으로 간결하게 작성하세요\n");

        } else {
            // === 일반 모드: 기존 타마고치 스타일 ===
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
        }

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

    /**
     * create_schedule 함수 실행
     */
    private String executeCreateSchedule(JsonNode argsNode) {
        try {
            String title = argsNode.path("title").asText();
            String description = argsNode.path("description").asText(null);
            String startTime = argsNode.path("startTime").asText();
            String endTime = argsNode.path("endTime").asText();
            String type = argsNode.path("type").asText("EVENT");

            log.info("Executing create_schedule: title={}, startTime={}, endTime={}, type={}",
                title, startTime, endTime, type);

            // CreateScheduleRequest 생성 (AllArgsConstructor 사용)
            com.lobai.dto.request.CreateScheduleRequest request =
                new com.lobai.dto.request.CreateScheduleRequest(
                    title,
                    description,
                    java.time.LocalDateTime.parse(startTime),
                    java.time.LocalDateTime.parse(endTime),
                    com.lobai.entity.Schedule.ScheduleType.valueOf(type),
                    null, // timezone
                    null  // notifyBeforeMinutes
                );

            // ScheduleService 호출
            scheduleService.createSchedule(request);

            return String.format("일정이 성공적으로 등록되었습니다. (제목: %s, 시작: %s, 종료: %s)",
                title, startTime, endTime);

        } catch (Exception e) {
            log.error("Failed to execute create_schedule", e);
            return "일정 등록에 실패했습니다: " + e.getMessage();
        }
    }

    /**
     * Function 결과와 함께 대화 계속하기
     */
    private String continueConversationWithFunctionResult(
            Map<String, Object> originalRequestBody,
            List<Map<String, Object>> originalContents,
            String functionName,
            String functionResult,
            String url,
            HttpHeaders headers) {
        try {
            // 1. 기존 contents에 function call 결과 추가
            List<Map<String, Object>> newContents = new ArrayList<>(originalContents);

            // Model's function call (이미 Gemini가 호출한 내용)
            Map<String, Object> modelFunctionCall = new HashMap<>();
            modelFunctionCall.put("role", "model");
            Map<String, Object> functionCallPart = new HashMap<>();
            Map<String, Object> functionCallData = new HashMap<>();
            functionCallData.put("name", functionName);
            functionCallPart.put("functionCall", functionCallData);
            modelFunctionCall.put("parts", List.of(functionCallPart));
            newContents.add(modelFunctionCall);

            // User's function response (함수 실행 결과)
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
            newContents.add(userFunctionResponse);

            // 2. 새로운 request body 생성
            Map<String, Object> newRequestBody = new HashMap<>();
            newRequestBody.put("system_instruction", originalRequestBody.get("system_instruction"));
            newRequestBody.put("contents", newContents);
            newRequestBody.put("generationConfig", originalRequestBody.get("generationConfig"));
            newRequestBody.put("tools", originalRequestBody.get("tools"));

            // 3. 다시 Gemini 호출
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(newRequestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
            );

            // 4. 최종 응답 파싱
            String responseBody = response.getBody();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String finalResponse = jsonNode
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

            log.info("Final response after function call: {}", finalResponse);
            return finalResponse;

        } catch (Exception e) {
            log.error("Failed to continue conversation with function result", e);
            return functionResult; // 에러 시 함수 결과만 반환
        }
    }

    /**
     * Function Calling을 위한 tools 정의 생성
     * create_schedule 함수를 Gemini에게 제공
     */
    public List<Map<String, Object>> buildFunctionDeclarations() {
        Map<String, Object> createScheduleFunction = new HashMap<>();
        createScheduleFunction.put("name", "create_schedule");
        createScheduleFunction.put("description",
            "사용자가 일정 등록을 요청하면 이 함수를 호출하세요. " +
            "예: '내일 오전 10시에 회의 일정 등록해줘', '10시까지 개발작업 일정 넣어줘' 등");

        // Parameters schema
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        // title
        Map<String, Object> titleProp = new HashMap<>();
        titleProp.put("type", "string");
        titleProp.put("description", "일정 제목 (예: '개발 작업', '팀 회의')");
        properties.put("title", titleProp);

        // description
        Map<String, Object> descProp = new HashMap<>();
        descProp.put("type", "string");
        descProp.put("description", "일정 상세 설명 (선택 사항)");
        properties.put("description", descProp);

        // startTime
        Map<String, Object> startTimeProp = new HashMap<>();
        startTimeProp.put("type", "string");
        startTimeProp.put("description",
            "시작 시간 (ISO 8601 형식: YYYY-MM-DDTHH:mm:ss). " +
            "System instruction에 제공된 '현재 날짜'와 '내일 날짜'를 정확히 사용하세요. " +
            "예시가 명시되어 있으니 반드시 참고하세요. " +
            "'내일'이라는 단어가 있으면 반드시 '내일 날짜'를 사용해야 합니다!");
        properties.put("startTime", startTimeProp);

        // endTime
        Map<String, Object> endTimeProp = new HashMap<>();
        endTimeProp.put("type", "string");
        endTimeProp.put("description",
            "종료 시간 (ISO 8601 형식: YYYY-MM-DDTHH:mm:ss). " +
            "startTime과 같은 날짜를 사용하고, 사용자가 명시하지 않으면 startTime + 1시간으로 설정하세요.");
        properties.put("endTime", endTimeProp);

        // type
        Map<String, Object> typeProp = new HashMap<>();
        typeProp.put("type", "string");
        typeProp.put("enum", List.of("REMINDER", "INTERACTION", "EVENT"));
        typeProp.put("description",
            "일정 유형: REMINDER (알림/리마인더), INTERACTION (AI 상호작용), EVENT (일반 이벤트). " +
            "기본값은 EVENT");
        properties.put("type", typeProp);

        parameters.put("properties", properties);
        parameters.put("required", List.of("title", "startTime", "endTime"));

        createScheduleFunction.put("parameters", parameters);

        // Tool 래퍼
        Map<String, Object> tool = new HashMap<>();
        tool.put("function_declarations", List.of(createScheduleFunction));

        return List.of(tool);
    }

    /**
     * 이미지 파일을 Base64로 인코딩
     */
    private String encodeImageToBase64(String imageUrl) {
        try {
            java.nio.file.Path imagePath = fileStorageService.getFilePath(imageUrl);
            byte[] imageBytes = java.nio.file.Files.readAllBytes(imagePath);
            return java.util.Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("Failed to encode image to base64: {}", imageUrl, e);
            return null;
        }
    }

    /**
     * MIME 타입에서 이미지 형식 추출 (예: "image/jpeg" -> "jpeg")
     */
    private String getImageFormat(String mimeType) {
        if (mimeType == null) return "jpeg";
        return mimeType.substring(mimeType.lastIndexOf("/") + 1);
    }
}
