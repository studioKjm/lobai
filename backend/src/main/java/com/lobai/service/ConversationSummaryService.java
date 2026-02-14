package com.lobai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.Optional;

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
