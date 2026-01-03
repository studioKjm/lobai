package com.lobai.service;

import com.lobai.entity.AffinityScore;
import com.lobai.entity.Message;
import com.lobai.entity.Message.MessageRole;
import com.lobai.repository.AffinityScoreRepository;
import com.lobai.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AffinityScoreService
 *
 * 친밀도 점수 계산 및 관리 서비스 (Phase 1)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AffinityScoreService {

    private final AffinityScoreRepository affinityScoreRepository;
    private final MessageRepository messageRepository;

    // 긍정 키워드
    private static final String[] POSITIVE_WORDS = {
        "감사", "고마워", "좋아", "멋져", "훌륭", "최고", "완벽",
        "도움", "친절", "사랑", "기쁘", "행복", "만족", "좋네",
        "대단", "놀라워", "멋지", "완전", "정말", "짱", "굿"
    };

    // 부정 키워드
    private static final String[] NEGATIVE_WORDS = {
        "싫어", "화나", "짜증", "멍청", "바보", "쓰레기", "최악",
        "못해", "안돼", "틀려", "나빠", "실망", "별로", "형편없"
    };

    // 불명확한 표현
    private static final String[] UNCLEAR_PHRASES = {
        "아무거나", "그거", "그냥", "뭐라고", "저기", "저기요",
        "음", "뭐", "글쎄"
    };

    // 맥락 참조 표현
    private static final String[] CONTEXTUAL_WORDS = {
        "그거", "그것", "그때", "아까", "방금", "위에서", "전에",
        "이전", "그래서", "그러면", "그런데"
    };

    /**
     * 사용자의 친밀도 점수 조회 (없으면 초기화)
     */
    @Transactional
    public AffinityScore getUserAffinityScore(Long userId) {
        return affinityScoreRepository.findByUserId(userId)
                .orElseGet(() -> initializeUserScore(userId));
    }

    /**
     * 신규 사용자 친밀도 점수 초기화
     */
    @Transactional
    public AffinityScore initializeUserScore(Long userId) {
        AffinityScore score = AffinityScore.builder()
                .userId(userId)
                .overallScore(BigDecimal.valueOf(50.00))
                .level(3)
                .avgSentimentScore(BigDecimal.valueOf(0.00))
                .avgClarityScore(BigDecimal.valueOf(0.50))
                .avgContextScore(BigDecimal.valueOf(0.50))
                .avgUsageScore(BigDecimal.valueOf(0.50))
                .totalMessages(0)
                .analyzedMessages(0)
                .build();

        log.info("Initialized affinity score for user: userId={}", userId);
        return affinityScoreRepository.save(score);
    }

    /**
     * 메시지 저장 시 호출 - 점수 분석 및 업데이트
     */
    @Transactional
    public void analyzeAndUpdateScore(Message message) {
        // 봇 메시지는 분석하지 않음
        if (message.getRole() != MessageRole.user) {
            return;
        }

        Long userId = message.getUser().getId();

        try {
            // 1. 최근 대화 히스토리 조회 (컨텍스트 계산용)
            Pageable historyPageable = PageRequest.of(0, 10);
            List<Message> recentHistory = messageRepository
                    .findRecentUserMessages(userId, historyPageable);

            // 2. 개별 점수 계산
            BigDecimal sentimentScore = calculateSentimentScore(message.getContent());
            BigDecimal clarityScore = calculateClarityScore(message.getContent());
            BigDecimal contextScore = calculateContextScore(message.getContent(), recentHistory);
            BigDecimal usageScore = calculateUsageScore(userId);

            // 3. 메시지에 점수 저장 (Message 엔티티 업데이트는 별도 처리 필요)
            // TODO: Message 엔티티에 setter 추가 필요

            log.debug("Message scores calculated: userId={}, messageId={}, sentiment={}, clarity={}, context={}, usage={}",
                    userId, message.getId(), sentimentScore, clarityScore, contextScore, usageScore);

            // 4. 종합 친밀도 점수 재계산
            recalculateOverallScore(userId);

        } catch (Exception e) {
            log.error("Failed to analyze affinity score: userId={}, messageId={}, error={}",
                    userId, message.getId(), e.getMessage(), e);
        }
    }

    /**
     * 사용자의 종합 친밀도 점수 재계산
     */
    @Transactional
    public AffinityScore recalculateOverallScore(Long userId) {
        AffinityScore affinityScore = getUserAffinityScore(userId);

        // 1. 개별 점수 평균 조회
        Double avgSentiment = messageRepository.getAverageSentimentByUser(userId);
        Double avgClarity = messageRepository.getAverageClarityByUser(userId);
        Double avgContext = messageRepository.getAverageContextByUser(userId);
        Double avgUsage = messageRepository.getAverageUsageByUser(userId);

        // 2. null 처리 및 기본값 설정
        BigDecimal sentimentScore = avgSentiment != null ?
                BigDecimal.valueOf(avgSentiment).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        BigDecimal clarityScore = avgClarity != null ?
                BigDecimal.valueOf(avgClarity).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.valueOf(0.50);
        BigDecimal contextScore = avgContext != null ?
                BigDecimal.valueOf(avgContext).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.valueOf(0.50);
        BigDecimal usageScore = avgUsage != null ?
                BigDecimal.valueOf(avgUsage).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.valueOf(0.50);

        // 3. 종합 점수 계산 (0-100)
        // Formula: (Sentiment+1)/2*25 + Clarity*25 + Context*25 + Usage*25
        double sentimentNormalized = (sentimentScore.doubleValue() + 1.0) / 2.0 * 25.0;
        double clarityNormalized = clarityScore.doubleValue() * 25.0;
        double contextNormalized = contextScore.doubleValue() * 25.0;
        double usageNormalized = usageScore.doubleValue() * 25.0;

        double overallScoreValue = sentimentNormalized + clarityNormalized +
                                    contextNormalized + usageNormalized;
        BigDecimal overallScore = BigDecimal.valueOf(overallScoreValue).setScale(2, RoundingMode.HALF_UP);

        // 4. 점수 업데이트
        affinityScore.updateScores(sentimentScore, clarityScore, contextScore, usageScore);
        affinityScore.updateOverallScore(overallScore);

        // 5. 메시지 통계 업데이트
        long totalMessages = messageRepository.countByUserIdAndRole(userId, MessageRole.user);
        affinityScore.incrementTotalMessages(); // TODO: 실제 카운트로 교체

        AffinityScore saved = affinityScoreRepository.save(affinityScore);

        log.info("Affinity score updated: userId={}, overall={}, level={}, sentiment={}, clarity={}, context={}, usage={}",
                userId, saved.getOverallScore(), saved.getLevel(),
                sentimentScore, clarityScore, contextScore, usageScore);

        return saved;
    }

    /**
     * Sentiment Score 계산 (감정 점수)
     * 범위: -1.00 ~ 1.00
     */
    private BigDecimal calculateSentimentScore(String content) {
        int positiveCount = 0;
        int negativeCount = 0;

        String lowerContent = content.toLowerCase();

        for (String word : POSITIVE_WORDS) {
            if (lowerContent.contains(word)) {
                positiveCount++;
            }
        }

        for (String word : NEGATIVE_WORDS) {
            if (lowerContent.contains(word)) {
                negativeCount++;
            }
        }

        // 점수 계산: -1.0 (매우 부정) ~ 1.0 (매우 긍정)
        int total = positiveCount + negativeCount;
        if (total == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP); // 중립
        }

        double score = (double) (positiveCount - negativeCount) / total;
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Clarity Score 계산 (명확성 점수)
     * 범위: 0.00 ~ 1.00
     */
    private BigDecimal calculateClarityScore(String content) {
        double score = 0.5; // 기본 점수

        int length = content.length();

        // 1. 길이 체크 (너무 짧거나 너무 길면 감점)
        if (length < 5) {
            score -= 0.3; // 너무 짧음
        } else if (length > 500) {
            score -= 0.2; // 너무 김
        } else if (length >= 10 && length <= 200) {
            score += 0.2; // 적절한 길이
        }

        // 2. 질문/명령의 명확성
        if (content.contains("?") || content.contains("어떻게") ||
            content.contains("무엇") || content.contains("왜")) {
            score += 0.2; // 명확한 질문
        }

        // 3. 구두점 사용 (문장 구조)
        long punctuationCount = content.chars()
                .filter(c -> c == '.' || c == ',' || c == '!' || c == '?')
                .count();
        if (punctuationCount > 0 && length > 50) {
            score += 0.1; // 구조화된 문장
        }

        // 4. 불명확한 표현 감점
        for (String phrase : UNCLEAR_PHRASES) {
            if (content.contains(phrase)) {
                score -= 0.1;
                break;
            }
        }

        // 0.0 ~ 1.0 범위로 제한
        score = Math.max(0.0, Math.min(1.0, score));
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Context Awareness Score 계산 (맥락 유지 점수)
     * 범위: 0.00 ~ 1.00
     */
    private BigDecimal calculateContextScore(String currentMessage, List<Message> recentHistory) {
        if (recentHistory.isEmpty()) {
            return BigDecimal.valueOf(0.5).setScale(2, RoundingMode.HALF_UP); // 첫 메시지는 중립
        }

        double score = 0.5;

        // 최근 3개 메시지 분석
        List<Message> last3Messages = recentHistory.stream()
                .filter(m -> m.getRole() == MessageRole.user)
                .limit(3)
                .collect(Collectors.toList());

        // 1. 키워드 중복 체크 (주제 일관성)
        Set<String> currentKeywords = extractKeywords(currentMessage);
        for (Message prevMsg : last3Messages) {
            Set<String> prevKeywords = extractKeywords(prevMsg.getContent());

            // 교집합 크기
            long commonKeywords = currentKeywords.stream()
                    .filter(prevKeywords::contains)
                    .count();

            if (commonKeywords > 0) {
                score += 0.15; // 주제 연결성
            }
        }

        // 2. 대명사/지시어 사용 (맥락 참조)
        for (String word : CONTEXTUAL_WORDS) {
            if (currentMessage.contains(word)) {
                score += 0.1;
                break;
            }
        }

        // 0.0 ~ 1.0 범위로 제한
        score = Math.max(0.0, Math.min(1.0, score));
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Usage Pattern Score 계산 (AI 활용 태도 점수)
     * 범위: 0.00 ~ 1.00
     */
    private BigDecimal calculateUsageScore(Long userId) {
        double score = 0.5;

        // 1. 대화 빈도 (최근 7일)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long messageCount = messageRepository.countByUserIdAndRoleAndCreatedAtAfter(
                userId, MessageRole.user, weekAgo);

        if (messageCount >= 20) {
            score += 0.2; // 활발한 사용
        } else if (messageCount >= 10) {
            score += 0.1; // 적당한 사용
        } else if (messageCount < 3) {
            score -= 0.1; // 저조한 사용
        }

        // 2. 질문 다양성 (다양한 페르소나 사용)
        long uniquePersonas = messageRepository.countUniquePersonasByUser(userId);
        if (uniquePersonas >= 3) {
            score += 0.2; // 다양한 활용
        } else if (uniquePersonas >= 2) {
            score += 0.1;
        }

        // 3. 평균 메시지 길이 (너무 짧지 않은 질문)
        Double avgLength = messageRepository.getAverageMessageLengthByUser(userId, MessageRole.user);
        if (avgLength != null && avgLength >= 20) {
            score += 0.1; // 충분한 맥락 제공
        }

        // 0.0 ~ 1.0 범위로 제한
        score = Math.max(0.0, Math.min(1.0, score));
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 간단한 키워드 추출 (명사 추정)
     */
    private Set<String> extractKeywords(String text) {
        // 공백, 구두점으로 분리하고 길이 2 이상인 단어만 추출
        return Arrays.stream(text.split("[\\s,.!?]+"))
                .filter(word -> word.length() >= 2)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
