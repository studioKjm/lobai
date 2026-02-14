package com.lobai.service;

import com.lobai.entity.ConversationSummary;
import com.lobai.entity.Message;
import com.lobai.entity.Persona;
import com.lobai.entity.UserMemory;
import com.lobai.llm.LlmMessage;
import com.lobai.llm.TokenEstimator;
import com.lobai.repository.ConversationSummaryRepository;
import com.lobai.repository.MessageRepository;
import com.lobai.repository.UserMemoryRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 3계층 컨텍스트 조립 서비스
 *
 * Tier 3: 사용자 프로필 기억 (~200 토큰)
 * Tier 2: 세션 요약 (~900 토큰)
 * Tier 1: 최근 메시지 (나머지 토큰 예산)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContextAssemblyService {

    private static final int DEFAULT_TOKEN_BUDGET = 6000;
    private static final int TIER3_TOKEN_BUDGET = 200;
    private static final int TIER2_TOKEN_BUDGET = 900;
    private static final int MAX_SUMMARIES = 3;
    private static final int MAX_RECENT_MESSAGES = 20;

    private final UserMemoryRepository userMemoryRepository;
    private final ConversationSummaryRepository conversationSummaryRepository;
    private final MessageRepository messageRepository;
    private final TokenEstimator tokenEstimator;

    /**
     * 3계층 컨텍스트 조립
     */
    @Transactional(readOnly = true)
    public AssembledContext assembleContext(Long userId, Persona persona, int tokenBudget) {
        if (tokenBudget <= 0) tokenBudget = DEFAULT_TOKEN_BUDGET;

        int usedTokens = 0;

        // === Tier 3: 사용자 프로필 기억 (항상 포함) ===
        String userProfileBlock = buildUserProfileBlock(userId);
        int profileTokens = tokenEstimator.estimateTokens(userProfileBlock);
        if (profileTokens > TIER3_TOKEN_BUDGET) {
            // 토큰 초과 시 잘라내기
            userProfileBlock = truncateToTokenBudget(userProfileBlock, TIER3_TOKEN_BUDGET);
            profileTokens = TIER3_TOKEN_BUDGET;
        }
        usedTokens += profileTokens;

        // === Tier 2: 세션 요약 (있으면 포함) ===
        String conversationSummaryBlock = "";
        if (persona != null) {
            conversationSummaryBlock = buildConversationSummaryBlock(userId, persona.getId());
            int summaryTokens = tokenEstimator.estimateTokens(conversationSummaryBlock);
            if (summaryTokens > TIER2_TOKEN_BUDGET) {
                conversationSummaryBlock = truncateToTokenBudget(conversationSummaryBlock, TIER2_TOKEN_BUDGET);
                summaryTokens = TIER2_TOKEN_BUDGET;
            }
            usedTokens += summaryTokens;
        }

        // === Tier 1: 최근 메시지 (남은 토큰 예산으로) ===
        int remainingBudget = tokenBudget - usedTokens;
        List<LlmMessage> recentMessages = buildRecentMessages(userId, remainingBudget);

        log.info("Context assembled for user {}: profile={} tokens, summary={} tokens, messages={} (budget={})",
                userId, profileTokens,
                tokenEstimator.estimateTokens(conversationSummaryBlock),
                recentMessages.size(), tokenBudget);

        return AssembledContext.builder()
                .userProfileBlock(userProfileBlock)
                .conversationSummaryBlock(conversationSummaryBlock)
                .recentMessages(recentMessages)
                .totalEstimatedTokens(usedTokens + tokenEstimator.estimateTokens(recentMessages))
                .build();
    }

    /**
     * Tier 3: 사용자 프로필 기억 블록 생성
     */
    private String buildUserProfileBlock(Long userId) {
        List<UserMemory> memories = userMemoryRepository.findActiveByUserId(userId);
        if (memories.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("=== 이 사용자에 대해 알고 있는 정보 ===\n");

        for (UserMemory memory : memories) {
            sb.append(String.format("- %s: %s\n", memory.getMemoryKey(), memory.getMemoryValue()));
        }

        return sb.toString();
    }

    /**
     * Tier 2: 대화 요약 블록 생성
     */
    private String buildConversationSummaryBlock(Long userId, Long personaId) {
        List<ConversationSummary> summaries = conversationSummaryRepository
                .findRecentByUserIdAndPersonaId(userId, personaId, PageRequest.of(0, MAX_SUMMARIES));

        if (summaries.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("=== 이전 대화 요약 ===\n");

        for (ConversationSummary summary : summaries) {
            sb.append(String.format("- [%s] %s\n",
                    summary.getCreatedAt().toLocalDate(),
                    summary.getSummaryText()));
        }

        return sb.toString();
    }

    /**
     * Tier 1: 최근 메시지를 토큰 예산 내에서 최대한 채우기
     */
    private List<LlmMessage> buildRecentMessages(Long userId, int tokenBudget) {
        if (tokenBudget <= 0) tokenBudget = 1000; // 최소 예산

        // 넉넉하게 가져와서 토큰 예산에 맞춰 줄이기
        List<Message> recentDb = messageRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, MAX_RECENT_MESSAGES))
                .getContent();

        // 역순 정렬 (오래된 → 최신)
        List<Message> chronological = new ArrayList<>();
        for (int i = recentDb.size() - 1; i >= 0; i--) {
            chronological.add(recentDb.get(i));
        }

        // LlmMessage로 변환
        List<LlmMessage> allMessages = chronological.stream()
                .map(msg -> LlmMessage.builder()
                        .role(msg.getRole() == Message.MessageRole.user
                                ? LlmMessage.Role.USER : LlmMessage.Role.ASSISTANT)
                        .content(msg.getContent())
                        .build())
                .collect(Collectors.toList());

        // 토큰 예산에 맞게 자르기 (최신 메시지 우선)
        int maxCount = tokenEstimator.maxMessagesWithinBudget(allMessages, tokenBudget);
        if (maxCount >= allMessages.size()) {
            return allMessages;
        }

        // 최신 메시지부터 maxCount개
        return allMessages.subList(allMessages.size() - maxCount, allMessages.size());
    }

    private String truncateToTokenBudget(String text, int tokenBudget) {
        if (tokenEstimator.estimateTokens(text) <= tokenBudget) return text;

        // 대략적인 자르기 (토큰 = 문자수 / 1.5 for Korean)
        int maxChars = (int) (tokenBudget * 1.5);
        if (text.length() <= maxChars) return text;
        return text.substring(0, maxChars) + "...";
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AssembledContext {
        private String userProfileBlock;
        private String conversationSummaryBlock;
        private List<LlmMessage> recentMessages;
        private int totalEstimatedTokens;
    }
}
