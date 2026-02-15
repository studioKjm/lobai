package com.lobai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lobai.dto.response.DailySummaryDetailResponse;
import com.lobai.dto.response.DailySummaryListResponse;
import com.lobai.dto.response.MessageResponse;
import com.lobai.entity.ConversationSummary;
import com.lobai.entity.Message;
import com.lobai.entity.Persona;
import com.lobai.entity.User;
import com.lobai.entity.UserMemory;
import com.lobai.llm.LlmRequest;
import com.lobai.llm.LlmResponse;
import com.lobai.llm.LlmTaskType;
import com.lobai.llm.TokenEstimator;
import com.lobai.repository.ConversationSummaryRepository;
import com.lobai.repository.MessageRepository;
import com.lobai.repository.UserMemoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 대화 요약 서비스
 *
 * 요약되지 않은 메시지가 20개 이상 쌓이면 비동기로 요약 생성.
 * 요약 결과에서 key facts를 추출하여 UserMemory에 저장.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationSummaryService {

    private static final int SUMMARY_THRESHOLD = 20;

    private final ConversationSummaryRepository conversationSummaryRepository;
    private final MessageRepository messageRepository;
    private final UserMemoryRepository userMemoryRepository;
    private final GeminiService geminiService;
    private final TokenEstimator tokenEstimator;
    private final ObjectMapper objectMapper;

    /**
     * 요약이 필요한지 확인하고 비동기 생성
     */
    @Async
    public void summarizeIfNeeded(User user, Persona persona) {
        try {
            Long userId = user.getId();
            Long personaId = persona != null ? persona.getId() : null;

            // 마지막 요약 이후 메시지 수 확인
            Optional<Long> lastEndId = personaId != null
                    ? conversationSummaryRepository.findLastMessageEndId(userId, personaId)
                    : Optional.empty();

            long unsummarizedCount;
            if (lastEndId.isPresent()) {
                unsummarizedCount = messageRepository.countByUserIdAndIdGreaterThan(userId, lastEndId.get());
            } else {
                unsummarizedCount = messageRepository.countByUserId(userId);
            }

            if (unsummarizedCount < SUMMARY_THRESHOLD) {
                return;
            }

            log.info("Triggering conversation summary for user {} (unsummarized: {})", userId, unsummarizedCount);
            generateSummary(user, persona, lastEndId.orElse(0L));

        } catch (Exception e) {
            log.error("Failed to check/generate conversation summary for user {}", user.getId(), e);
        }
    }

    /**
     * 요약 생성
     */
    @Transactional
    public void generateSummary(User user, Persona persona, Long afterMessageId) {
        // 요약할 메시지 조회
        List<Message> messages = messageRepository.findByUserIdOrderByCreatedAtDesc(
                user.getId(), PageRequest.of(0, SUMMARY_THRESHOLD)).getContent();

        if (messages.isEmpty()) return;

        // 메시지 텍스트 구성
        StringBuilder messagesText = new StringBuilder();
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            String roleLabel = msg.getRole() == Message.MessageRole.user ? "사용자" : "AI";
            messagesText.append(String.format("[%s] %s\n", roleLabel, msg.getContent()));
        }

        // LLM으로 요약 요청
        String summaryPrompt = """
                이전 대화를 분석하여 다음을 추출하세요:
                1. 대화 요약 (2-3문장, 한국어)
                2. 핵심 사실: 사용자의 약속, 선호도, 반복 주제 등

                반드시 다음 JSON 형식으로만 응답하세요:
                {"summary": "...", "keyFacts": [{"key": "..", "value": "..", "type": "FACT"}]}

                type은 FACT, PROMISE, PREFERENCE 중 하나입니다.
                """;

        LlmRequest request = LlmRequest.builder()
                .systemInstruction(summaryPrompt)
                .userMessage(messagesText.toString())
                .temperature(0.3)
                .maxOutputTokens(1024)
                .jsonMode(true)
                .taskType(LlmTaskType.CONVERSATION_SUMMARY)
                .build();

        try {
            LlmResponse response = geminiService.generateViaRouter(LlmTaskType.CONVERSATION_SUMMARY, request);
            String responseText = response.getContent();

            // JSON 파싱
            String jsonStr = extractJson(responseText);
            JsonNode node = objectMapper.readTree(jsonStr);

            String summary = node.path("summary").asText("");
            JsonNode keyFactsNode = node.path("keyFacts");

            // 요약 저장
            Long startId = messages.get(messages.size() - 1).getId();
            Long endId = messages.get(0).getId();

            ConversationSummary cs = ConversationSummary.builder()
                    .user(user)
                    .persona(persona)
                    .summaryText(summary)
                    .keyFacts(keyFactsNode.toString())
                    .messageStartId(startId)
                    .messageEndId(endId)
                    .messageCount(messages.size())
                    .tokenCount(response.getUsage() != null ? response.getUsage().getTotalTokens() : 0)
                    .summaryType(ConversationSummary.SummaryType.PERIODIC)
                    .llmProvider(response.getProviderName())
                    .build();
            conversationSummaryRepository.save(cs);

            // Key Facts를 UserMemory에 저장
            if (keyFactsNode.isArray()) {
                for (JsonNode fact : keyFactsNode) {
                    String key = fact.path("key").asText();
                    String value = fact.path("value").asText();
                    String type = fact.path("type").asText("FACT");

                    if (!key.isEmpty() && !value.isEmpty()) {
                        saveOrUpdateMemory(user, key, value, type);
                    }
                }
            }

            log.info("Conversation summary generated for user {}: {} chars, {} key facts",
                    user.getId(), summary.length(), keyFactsNode.size());

        } catch (Exception e) {
            log.error("Failed to generate summary for user {}", user.getId(), e);
        }
    }

    // ==================== Daily Summary ====================

    /**
     * 일일 요약 생성 (스케줄러에서 호출)
     *
     * @return true if summary was generated, false if skipped (no messages)
     */
    @Transactional
    public boolean generateDailySummary(User user, LocalDate date) {
        Long userId = user.getId();

        // 중복 체크
        if (conversationSummaryRepository.existsByUserIdAndSummaryTypeAndSummaryDate(
                userId, ConversationSummary.SummaryType.DAILY, date)) {
            log.info("Daily summary already exists for user {} on {}", userId, date);
            return false;
        }

        // 해당 날짜 전체 메시지 조회
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        List<Message> messages = messageRepository.findByUserIdAndDateRange(userId, dayStart, dayEnd);

        if (messages.isEmpty()) {
            log.debug("No messages found for user {} on {}, skipping daily summary", userId, date);
            return false;
        }

        // 메시지 텍스트 구성
        StringBuilder messagesText = new StringBuilder();
        for (Message msg : messages) {
            String roleLabel = msg.getRole() == Message.MessageRole.user ? "사용자" : "AI";
            messagesText.append(String.format("[%s] %s\n", roleLabel, msg.getContent()));
        }

        // LLM으로 일일 요약 요청
        String dailySummaryPrompt = """
                하루 동안의 대화를 분석하여 다음을 추출하세요:
                1. 대화 요약 (3-5문장, 한국어): 오늘 대화의 주요 내용과 분위기
                2. 핵심 사실: 사용자가 언급한 중요 사항, 약속, 선호도, 감정 상태 등

                반드시 다음 JSON 형식으로만 응답하세요:
                {"summary": "...", "keyFacts": [{"key": "..", "value": "..", "type": "FACT"}]}

                type은 FACT, PROMISE, PREFERENCE 중 하나입니다.
                """;

        LlmRequest request = LlmRequest.builder()
                .systemInstruction(dailySummaryPrompt)
                .userMessage(messagesText.toString())
                .temperature(0.3)
                .maxOutputTokens(1024)
                .jsonMode(true)
                .taskType(LlmTaskType.CONVERSATION_SUMMARY)
                .build();

        try {
            LlmResponse response = geminiService.generateViaRouter(LlmTaskType.CONVERSATION_SUMMARY, request);
            String responseText = response.getContent();

            // JSON 파싱
            String jsonStr = extractJson(responseText);
            JsonNode node = objectMapper.readTree(jsonStr);

            String summary = node.path("summary").asText("");
            JsonNode keyFactsNode = node.path("keyFacts");

            Long startId = messages.get(0).getId();
            Long endId = messages.get(messages.size() - 1).getId();

            ConversationSummary cs = ConversationSummary.builder()
                    .user(user)
                    .persona(null) // 크로스 페르소나
                    .summaryText(summary)
                    .keyFacts(keyFactsNode.toString())
                    .messageStartId(startId)
                    .messageEndId(endId)
                    .messageCount(messages.size())
                    .tokenCount(response.getUsage() != null ? response.getUsage().getTotalTokens() : 0)
                    .summaryType(ConversationSummary.SummaryType.DAILY)
                    .summaryDate(date)
                    .llmProvider(response.getProviderName())
                    .build();
            conversationSummaryRepository.save(cs);

            // Key Facts를 UserMemory에 저장
            if (keyFactsNode.isArray()) {
                for (JsonNode fact : keyFactsNode) {
                    String key = fact.path("key").asText();
                    String value = fact.path("value").asText();
                    String type = fact.path("type").asText("FACT");

                    if (!key.isEmpty() && !value.isEmpty()) {
                        saveOrUpdateMemory(user, key, value, type);
                    }
                }
            }

            log.info("Daily summary generated for user {} (date {}): {} chars, {} messages, {} key facts",
                    userId, date, summary.length(), messages.size(), keyFactsNode.size());
            return true;

        } catch (Exception e) {
            log.error("Failed to generate daily summary for user {} on {}", userId, date, e);
            return false;
        }
    }

    /**
     * 일일 요약 목록 조회 (API용)
     */
    @Transactional(readOnly = true)
    public List<DailySummaryListResponse> getDailySummaryList(Long userId, int limit) {
        List<ConversationSummary> summaries = conversationSummaryRepository
                .findDailySummariesByUserId(userId, PageRequest.of(0, limit));

        return summaries.stream().map(cs -> {
            String preview = cs.getSummaryText();
            if (preview != null && preview.length() > 100) {
                preview = preview.substring(0, 100) + "...";
            }

            String keyFactsPreview = "";
            if (cs.getKeyFacts() != null) {
                try {
                    JsonNode factsNode = objectMapper.readTree(cs.getKeyFacts());
                    if (factsNode.isArray() && factsNode.size() > 0) {
                        StringBuilder sb = new StringBuilder();
                        int count = 0;
                        for (JsonNode fact : factsNode) {
                            if (count >= 3) break;
                            if (count > 0) sb.append(", ");
                            sb.append(fact.path("key").asText());
                            count++;
                        }
                        keyFactsPreview = sb.toString();
                    }
                } catch (Exception ignored) {}
            }

            return DailySummaryListResponse.builder()
                    .date(cs.getSummaryDate())
                    .summaryPreview(preview)
                    .messageCount(cs.getMessageCount())
                    .keyFactsPreview(keyFactsPreview)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 일일 요약 상세 조회 (API용)
     */
    @Transactional(readOnly = true)
    public DailySummaryDetailResponse getDailySummaryDetail(Long userId, LocalDate date) {
        Optional<ConversationSummary> summaryOpt = conversationSummaryRepository
                .findDailySummaryByUserIdAndDate(userId, date);

        if (summaryOpt.isEmpty()) {
            return null;
        }

        ConversationSummary cs = summaryOpt.get();

        // 해당 날짜 원본 메시지 조회
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        List<Message> messages = messageRepository.findByUserIdAndDateRange(userId, dayStart, dayEnd);

        List<MessageResponse> messageResponses = messages.stream()
                .map(msg -> MessageResponse.builder()
                        .id(msg.getId())
                        .role(msg.getRole().name())
                        .content(msg.getContent())
                        .personaId(msg.getPersona() != null ? msg.getPersona().getId() : null)
                        .personaName(msg.getPersona() != null ? msg.getPersona().getDisplayName() : null)
                        .messageType(msg.getMessageType() != null ? msg.getMessageType() : "NORMAL")
                        .createdAt(msg.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return DailySummaryDetailResponse.builder()
                .date(cs.getSummaryDate())
                .summaryText(cs.getSummaryText())
                .keyFacts(cs.getKeyFacts())
                .messageCount(cs.getMessageCount())
                .messages(messageResponses)
                .build();
    }

    private void saveOrUpdateMemory(User user, String key, String value, String type) {
        UserMemory.MemoryType memoryType;
        try {
            memoryType = UserMemory.MemoryType.valueOf(type);
        } catch (IllegalArgumentException e) {
            memoryType = UserMemory.MemoryType.FACT;
        }

        Optional<UserMemory> existing = userMemoryRepository.findByUserIdAndMemoryKey(user.getId(), key);
        if (existing.isPresent()) {
            UserMemory memory = existing.get();
            memory.setMemoryValue(value);
            memory.setConfidenceScore(
                    memory.getConfidenceScore().add(BigDecimal.valueOf(0.1)).min(BigDecimal.ONE));
            userMemoryRepository.save(memory);
        } else {
            UserMemory memory = UserMemory.builder()
                    .user(user)
                    .memoryKey(key)
                    .memoryValue(value)
                    .memoryType(memoryType)
                    .confidenceScore(BigDecimal.valueOf(0.50))
                    .build();
            userMemoryRepository.save(memory);
        }
    }

    private String extractJson(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf("{");
            int end = trimmed.lastIndexOf("}");
            if (start >= 0 && end >= 0) {
                return trimmed.substring(start, end + 1);
            }
        }
        return trimmed;
    }
}
