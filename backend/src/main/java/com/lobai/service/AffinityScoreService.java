package com.lobai.service;

import com.lobai.dto.MessageAnalysisResult;
import com.lobai.entity.AffinityScore;
import com.lobai.entity.Message;
import com.lobai.entity.Message.MessageRole;
import com.lobai.repository.AffinityScoreRepository;
import com.lobai.repository.ChatSessionRepository;
import com.lobai.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
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
 * 친밀도 점수 계산 및 관리 서비스 (Phase 2 - 7차원 고도화)
 * - Gemini AI 감성 분석 연동
 * - 동적 가중치 (관계 단계별)
 * - 노벨티 할인
 * - 데이터 임계치
 * - 존댓말→반말 전환 감지
 */
@Slf4j
@Service
public class AffinityScoreService {

    private final AffinityScoreRepository affinityScoreRepository;
    private final MessageRepository messageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final LevelService levelService;
    private final GeminiAffinityAnalyzer geminiAffinityAnalyzer;
    private final AffinityHistoryService affinityHistoryService;

    public AffinityScoreService(
            AffinityScoreRepository affinityScoreRepository,
            MessageRepository messageRepository,
            ChatSessionRepository chatSessionRepository,
            @Lazy LevelService levelService,
            GeminiAffinityAnalyzer geminiAffinityAnalyzer,
            AffinityHistoryService affinityHistoryService) {
        this.affinityScoreRepository = affinityScoreRepository;
        this.messageRepository = messageRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.levelService = levelService;
        this.geminiAffinityAnalyzer = geminiAffinityAnalyzer;
        this.affinityHistoryService = affinityHistoryService;
    }

    // 기존 키워드 (clarity/context heuristic용)
    private static final String[] UNCLEAR_PHRASES = {
        "아무거나", "그거", "그냥", "뭐라고", "저기", "저기요", "음", "뭐", "글쎄"
    };
    private static final String[] CONTEXTUAL_WORDS = {
        "그거", "그것", "그때", "아까", "방금", "위에서", "전에", "이전", "그래서", "그러면", "그런데"
    };

    // ==================== 동적 가중치 매핑 ====================

    /** 관계 단계별 7차원 가중치: sentiment, clarity, context, usage, engagement, reciprocity, disclosure */
    private static final Map<String, double[]> STAGE_WEIGHTS = Map.of(
        "EARLY",      new double[]{0.20, 0.10, 0.10, 0.10, 0.30, 0.10, 0.10},
        "DEVELOPING", new double[]{0.14, 0.14, 0.14, 0.14, 0.15, 0.15, 0.14},
        "MATURE",     new double[]{0.10, 0.10, 0.15, 0.10, 0.15, 0.25, 0.15}
    );

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
                .avgEngagementDepth(BigDecimal.valueOf(0.50))
                .avgSelfDisclosure(BigDecimal.ZERO)
                .avgReciprocity(BigDecimal.valueOf(0.50))
                .totalMessages(0)
                .analyzedMessages(0)
                .build();

        log.info("Initialized affinity score for user: userId={}", userId);
        return affinityScoreRepository.save(score);
    }

    /**
     * 메시지 저장 시 호출 - Gemini 분석 + 점수 업데이트
     */
    @Transactional
    public void analyzeAndUpdateScore(Message message) {
        if (message.getRole() != MessageRole.user) {
            return;
        }

        Long userId = message.getUser().getId();

        try {
            // 1. 최근 대화 히스토리 조회
            Pageable historyPageable = PageRequest.of(0, 10);
            List<Message> recentHistory = messageRepository.findRecentUserMessages(userId, historyPageable);
            List<String> recentContext = recentHistory.stream()
                    .map(Message::getContent)
                    .collect(Collectors.toList());

            // 2. Gemini/heuristic 감성 분석
            MessageAnalysisResult analysis = geminiAffinityAnalyzer.analyzeMessage(
                    message.getContent(), recentContext);

            // 3. 분석 결과를 Message 엔티티에 저장
            message.setSentimentScore(analysis.getSentimentScore());
            message.setPrimaryEmotion(analysis.getPrimaryEmotion());
            message.setSelfDisclosureDepth(analysis.getSelfDisclosureDepth());
            message.setHonorificLevel(analysis.getHonorificLevel());
            message.setIsQuestion(analysis.isQuestion());
            message.setIsInitiative(analysis.isInitiative());
            message.setIsAnalyzed(true);

            // 4. 기존 clarity/context/usage 점수도 계산하여 저장
            BigDecimal clarityScore = calculateClarityScore(message.getContent());
            BigDecimal contextScore = calculateContextScore(message.getContent(), recentHistory);
            BigDecimal usageScore = calculateUsageScore(userId);
            message.setClarityScore(clarityScore);
            message.setContextScore(contextScore);
            message.setUsageScore(usageScore);

            log.debug("Message analyzed: userId={}, messageId={}, sentiment={}, emotion={}, disclosure={}",
                    userId, message.getId(), analysis.getSentimentScore(),
                    analysis.getPrimaryEmotion(), analysis.getSelfDisclosureDepth());

            // 5. 종합 친밀도 점수 재계산
            recalculateOverallScore(userId);

        } catch (Exception e) {
            log.error("Failed to analyze affinity score: userId={}, messageId={}, error={}",
                    userId, message.getId(), e.getMessage(), e);
        }
    }

    /**
     * 사용자의 종합 친밀도 점수 재계산 (7차원)
     */
    @Transactional
    public AffinityScore recalculateOverallScore(Long userId) {
        AffinityScore affinityScore = getUserAffinityScore(userId);

        // 1. 기존 4개 차원 평균 조회
        Double avgSentiment = messageRepository.getAverageSentimentByUser(userId);
        Double avgClarity = messageRepository.getAverageClarityByUser(userId);
        Double avgContext = messageRepository.getAverageContextByUser(userId);
        Double avgUsage = messageRepository.getAverageUsageByUser(userId);

        BigDecimal sentimentScore = toBigDecimal(avgSentiment, BigDecimal.ZERO);
        BigDecimal clarityScore = toBigDecimal(avgClarity, BigDecimal.valueOf(0.50));
        BigDecimal contextScore = toBigDecimal(avgContext, BigDecimal.valueOf(0.50));
        BigDecimal usageScore = toBigDecimal(avgUsage, BigDecimal.valueOf(0.50));

        // 2. 새로운 3개 차원 계산
        BigDecimal engagementDepth = calculateEngagementDepth(userId);
        BigDecimal selfDisclosure = calculateSelfDisclosureDepth(userId);
        BigDecimal reciprocity = calculateReciprocity(userId);

        // 3. 메타데이터 업데이트
        long totalMessages = messageRepository.countByUserIdAndRole(userId, MessageRole.user);
        Long totalSessions = chatSessionRepository.countByUserId(userId);
        affinityScore.setTotalMessages((int) totalMessages);
        affinityScore.setTotalSessions(totalSessions != null ? totalSessions.intValue() : 0);

        // 4. 관계 단계 결정
        String stage = determineRelationshipStage(affinityScore.getLevel());
        affinityScore.setRelationshipStage(stage);

        // 5. 데이터 임계치 결정
        String threshold = determineDataThreshold((int) totalMessages);
        affinityScore.setDataThresholdStatus(threshold);

        // 6. 노벨티 할인 계산
        BigDecimal noveltyDiscount = getNoveltyDiscount(totalSessions != null ? totalSessions.intValue() : 0);
        affinityScore.setNoveltyDiscountFactor(noveltyDiscount);

        // 7. 존댓말→반말 전환 감지
        boolean honorificTransition = detectHonorificTransition(userId);
        affinityScore.setHonorificTransitionDetected(honorificTransition);

        // 8. 동적 가중치로 7차원 종합 점수 계산
        double[] weights = STAGE_WEIGHTS.getOrDefault(stage, STAGE_WEIGHTS.get("EARLY"));

        // 감정 점수 정규화: -1~1 → 0~1
        double sentNorm = (sentimentScore.doubleValue() + 1.0) / 2.0;
        double claritN = clarityScore.doubleValue();
        double ctxNorm = contextScore.doubleValue();
        double usageN = usageScore.doubleValue();
        double engNorm = engagementDepth.doubleValue();
        double recipN = reciprocity.doubleValue();
        double discN = selfDisclosure.doubleValue();

        // 가중 평균 (0~1 범위) → 0~100으로 스케일링
        double rawScore = (sentNorm * weights[0] + claritN * weights[1] + ctxNorm * weights[2] +
                usageN * weights[3] + engNorm * weights[4] + recipN * weights[5] + discN * weights[6]) * 100.0;

        // 9. 노벨티 할인 적용
        double adjustedScore = 50 + (rawScore - 50) * noveltyDiscount.doubleValue();

        // 10. 존댓말 전환 보너스
        if (honorificTransition) {
            adjustedScore = Math.min(100.0, adjustedScore + 5.0);
        }

        adjustedScore = Math.max(0.0, Math.min(100.0, adjustedScore));
        BigDecimal overallScore = BigDecimal.valueOf(adjustedScore).setScale(2, RoundingMode.HALF_UP);

        // 11. 모든 점수 업데이트
        affinityScore.updateAllScores(sentimentScore, clarityScore, contextScore, usageScore,
                engagementDepth, selfDisclosure, reciprocity);
        affinityScore.updateOverallScore(overallScore);
        affinityScore.incrementAnalyzedMessages();

        AffinityScore saved = affinityScoreRepository.save(affinityScore);

        // 12. 일별 스냅샷 저장
        affinityHistoryService.saveOrUpdateDailySnapshot(saved);

        log.info("Affinity score updated (7-dim): userId={}, overall={}, level={}, stage={}, threshold={}, " +
                 "engagement={}, disclosure={}, reciprocity={}, novelty={}",
                userId, saved.getOverallScore(), saved.getLevel(), stage, threshold,
                engagementDepth, selfDisclosure, reciprocity, noveltyDiscount);

        // 13. 사용자 레벨 자동 업데이트 (별도 트랜잭션으로 실행하여 롤백 방지)
        // Note: LevelService는 trust_levels 테이블(HIP 점수 기반)을 사용하므로,
        // affinity score와는 별개의 시스템임. 에러 발생 시 친밀도 점수 저장은 유지되어야 함.
        // TODO: 향후 친밀도 기반 별도 레벨 시스템 구축 필요
        // (현재는 LevelService 호출을 주석 처리)

        return saved;
    }

    // ==================== 새로운 차원 계산 ====================

    /**
     * Engagement Depth 계산 (CPS 기반)
     * - 세션당 평균 턴 수
     * - 세션 빈도 (최근 4주)
     * - 자발적 복귀율 (48시간 내)
     */
    private BigDecimal calculateEngagementDepth(Long userId) {
        Long totalSessions = chatSessionRepository.countByUserId(userId);
        if (totalSessions == null || totalSessions == 0) {
            return BigDecimal.valueOf(0.50).setScale(2, RoundingMode.HALF_UP);
        }

        Integer totalMessages = chatSessionRepository.sumTotalMessages(userId);
        double avgTurnsPerSession = (totalMessages != null && totalMessages > 0)
                ? (double) totalMessages / totalSessions : 0;

        // 턴 점수: 5-20턴이 적절, 정규화
        double turnsScore = Math.min(1.0, avgTurnsPerSession / 20.0);

        // 세션 빈도: 최근 4주 세션 수 / 4 (주 1회 = 0.25, 주 4회 = 1.0)
        LocalDateTime fourWeeksAgo = LocalDateTime.now().minusWeeks(4);
        Long recentSessions = chatSessionRepository.countByUserIdAndStartedAtAfter(userId, fourWeeksAgo);
        double weeklyFrequency = (recentSessions != null ? recentSessions : 0) / 4.0;
        double frequencyScore = Math.min(1.0, weeklyFrequency / 4.0);

        // 자발적 복귀율: 48시간 내 재방문 세션 비율
        Long returnSessions = chatSessionRepository.countVoluntaryReturnSessions(userId);
        double returnRate = (returnSessions != null && totalSessions > 1)
                ? (double) returnSessions / (totalSessions - 1) : 0;
        double returnScore = Math.min(1.0, returnRate);

        double score = turnsScore * 0.4 + frequencyScore * 0.3 + returnScore * 0.3;
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Self-Disclosure Depth 계산 (Gemini 분석 기반)
     */
    private BigDecimal calculateSelfDisclosureDepth(Long userId) {
        Double avg = messageRepository.getAverageSelfDisclosureByUser(userId);
        return toBigDecimal(avg, BigDecimal.ZERO);
    }

    /**
     * Reciprocity 계산 (상호작용 품질)
     * - 주도율, 질문율, 응답 정교함
     */
    private BigDecimal calculateReciprocity(Long userId) {
        long totalMessages = messageRepository.countByUserIdAndRole(userId, MessageRole.user);
        if (totalMessages == 0) {
            return BigDecimal.valueOf(0.50).setScale(2, RoundingMode.HALF_UP);
        }

        // 주도율
        long initiativeCount = messageRepository.countInitiativeMessages(userId);
        double initiativeRate = (double) initiativeCount / totalMessages;
        initiativeRate = Math.min(1.0, initiativeRate);

        // 질문율
        long questionCount = messageRepository.countQuestionMessages(userId);
        double questionRate = (double) questionCount / totalMessages;
        questionRate = Math.min(1.0, questionRate);

        // 응답 정교함: 평균 메시지 길이 (20자+ 기준, 200자에서 만점)
        Double avgLength = messageRepository.getAverageMessageLengthByUser(userId, MessageRole.user);
        double elaboration = 0.0;
        if (avgLength != null && avgLength >= 20) {
            elaboration = Math.min(1.0, (avgLength - 20) / 180.0);
        }

        double score = initiativeRate * 0.4 + questionRate * 0.3 + elaboration * 0.3;
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    // ==================== 메타데이터 계산 ====================

    /**
     * 관계 단계 결정 (레벨 기반)
     */
    private String determineRelationshipStage(int level) {
        if (level <= 2) return "EARLY";
        if (level == 3) return "DEVELOPING";
        return "MATURE";
    }

    /**
     * 데이터 임계치 결정
     */
    private String determineDataThreshold(int totalMessages) {
        if (totalMessages < 10) return "COLLECTING";
        if (totalMessages < 50) return "INITIAL";
        return "FULL";
    }

    /**
     * 노벨티 할인 계산 (Leite 연구 기반)
     */
    private BigDecimal getNoveltyDiscount(int totalSessions) {
        if (totalSessions <= 5) return BigDecimal.valueOf(0.60);
        if (totalSessions <= 15) return BigDecimal.valueOf(0.85);
        return BigDecimal.ONE;
    }

    /**
     * 존댓말→반말 전환 감지
     */
    private boolean detectHonorificTransition(Long userId) {
        List<String> timeline = messageRepository.getHonorificTimeline(userId);
        if (timeline.size() < 10) return false;

        int total = timeline.size();
        int earlyCount = (int) (total * 0.3);
        int lateStart = total - earlyCount;

        // 초기 30%의 formal 비율
        long earlyFormal = timeline.subList(0, earlyCount).stream()
                .filter("formal"::equals).count();
        double earlyFormalRate = (double) earlyFormal / earlyCount;

        // 최근 30%의 informal/mixed 비율
        long lateInformal = timeline.subList(lateStart, total).stream()
                .filter(h -> "informal".equals(h) || "mixed".equals(h)).count();
        double lateInformalRate = (double) lateInformal / earlyCount;

        return earlyFormalRate >= 0.6 && lateInformalRate >= 0.5;
    }

    // ==================== 기존 heuristic 점수 계산 (유지) ====================

    private BigDecimal calculateClarityScore(String content) {
        double score = 0.5;
        int length = content.length();

        if (length < 5) score -= 0.3;
        else if (length > 500) score -= 0.2;
        else if (length >= 10 && length <= 200) score += 0.2;

        if (content.contains("?") || content.contains("어떻게") ||
            content.contains("무엇") || content.contains("왜")) {
            score += 0.2;
        }

        long punctuationCount = content.chars()
                .filter(c -> c == '.' || c == ',' || c == '!' || c == '?')
                .count();
        if (punctuationCount > 0 && length > 50) score += 0.1;

        for (String phrase : UNCLEAR_PHRASES) {
            if (content.contains(phrase)) { score -= 0.1; break; }
        }

        return BigDecimal.valueOf(Math.max(0.0, Math.min(1.0, score))).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateContextScore(String currentMessage, List<Message> recentHistory) {
        if (recentHistory.isEmpty()) {
            return BigDecimal.valueOf(0.5).setScale(2, RoundingMode.HALF_UP);
        }

        double score = 0.5;
        List<Message> last3 = recentHistory.stream()
                .filter(m -> m.getRole() == MessageRole.user)
                .limit(3)
                .collect(Collectors.toList());

        Set<String> currentKeywords = extractKeywords(currentMessage);
        for (Message prevMsg : last3) {
            Set<String> prevKeywords = extractKeywords(prevMsg.getContent());
            long common = currentKeywords.stream().filter(prevKeywords::contains).count();
            if (common > 0) score += 0.15;
        }

        for (String word : CONTEXTUAL_WORDS) {
            if (currentMessage.contains(word)) { score += 0.1; break; }
        }

        return BigDecimal.valueOf(Math.max(0.0, Math.min(1.0, score))).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateUsageScore(Long userId) {
        double score = 0.5;

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long messageCount = messageRepository.countByUserIdAndRoleAndCreatedAtAfter(userId, MessageRole.user, weekAgo);

        if (messageCount >= 20) score += 0.2;
        else if (messageCount >= 10) score += 0.1;
        else if (messageCount < 3) score -= 0.1;

        long uniquePersonas = messageRepository.countUniquePersonasByUser(userId);
        if (uniquePersonas >= 3) score += 0.2;
        else if (uniquePersonas >= 2) score += 0.1;

        Double avgLength = messageRepository.getAverageMessageLengthByUser(userId, MessageRole.user);
        if (avgLength != null && avgLength >= 20) score += 0.1;

        return BigDecimal.valueOf(Math.max(0.0, Math.min(1.0, score))).setScale(2, RoundingMode.HALF_UP);
    }

    private Set<String> extractKeywords(String text) {
        return Arrays.stream(text.split("[\\s,.!?]+"))
                .filter(word -> word.length() >= 2)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    // ==================== Utility ====================

    private BigDecimal toBigDecimal(Double value, BigDecimal defaultVal) {
        return value != null
                ? BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP)
                : defaultVal;
    }
}
