package com.lobai.service;

import com.lobai.entity.*;
import com.lobai.repository.*;
import com.lobai.util.HipIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HumanIdentityProfileService
 *
 * HIP의 핵심 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HumanIdentityProfileService {

    private final HumanIdentityProfileRepository hipRepository;
    private final IdentityMetricsRepository metricsRepository;
    private final CommunicationSignatureRepository signatureRepository;
    private final BehavioralFingerprintRepository fingerprintRepository;
    private final IdentityVerificationLogRepository verificationLogRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final AffinityScoreRepository affinityScoreRepository;
    private final GeminiService geminiService;

    /**
     * 사용자의 HIP 초기 생성
     * User 생성 시 자동으로 호출되어야 함
     */
    @Transactional
    public HumanIdentityProfile createInitialProfile(Long userId) {
        log.info("Creating initial HIP for user: {}", userId);

        // 이미 존재하는지 확인
        if (hipRepository.existsByUserId(userId)) {
            log.warn("HIP already exists for user: {}", userId);
            return hipRepository.findByUserId(userId).orElseThrow();
        }

        // User 정보 가져오기
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // HIP ID 생성
        String hipId = HipIdGenerator.generate(userId, user.getEmail(), user.getCreatedAt());

        // HIP 생성
        HumanIdentityProfile hip = HumanIdentityProfile.builder()
            .hipId(hipId)
            .userId(userId)
            .build();

        HumanIdentityProfile savedHip = hipRepository.save(hip);

        // 초기 검증 로그 생성
        IdentityVerificationLog initialLog = IdentityVerificationLog.builder()
            .hipId(hipId)
            .verificationType("initial")
            .verificationMethod("creation")
            .newScore(BigDecimal.valueOf(50.00))
            .status("verified")
            .notes("Initial HIP creation")
            .build();
        verificationLogRepository.save(initialLog);

        log.info("Created HIP: {} for user: {}", hipId, userId);
        return savedHip;
    }

    /**
     * 사용자의 HIP 조회 (없으면 생성)
     */
    @Transactional
    public HumanIdentityProfile getOrCreateProfile(Long userId) {
        return hipRepository.findByUserId(userId)
            .orElseGet(() -> createInitialProfile(userId));
    }

    /**
     * HIP 재분석 및 업데이트
     * AffinityScore + Gemini AI 분석을 결합하여 HIP 재계산
     */
    @Transactional
    public HumanIdentityProfile reanalyzeProfile(Long userId) {
        log.info("Reanalyzing HIP for user: {}", userId);

        HumanIdentityProfile hip = getOrCreateProfile(userId);
        BigDecimal previousScore = hip.getOverallHipScore();

        // 1. AffinityScore 기반 점수 계산
        Map<String, BigDecimal> affinityScores = new HashMap<>();
        Optional<AffinityScore> affinityOpt = affinityScoreRepository.findByUserId(userId);

        if (affinityOpt.isPresent()) {
            AffinityScore affinity = affinityOpt.get();

            // Core Scores 계산 (AffinityScore의 개별 점수들을 HIP 점수로 매핑)
            affinityScores.put("cognitiveFlexibility", calculateCognitiveFlexibility(affinity));
            affinityScores.put("collaborationPattern", calculateCollaborationPattern(affinity));
            affinityScores.put("informationProcessing", calculateInformationProcessing(affinity));
            affinityScores.put("emotionalIntelligence", calculateEmotionalIntelligence(affinity));
            affinityScores.put("creativity", calculateCreativity(affinity));
            affinityScores.put("ethicalAlignment", calculateEthicalAlignment(affinity));

            log.info("AffinityScore-based HIP scores calculated for user: {}", userId);
        } else {
            log.warn("No AffinityScore found for user: {}, using defaults", userId);
        }

        // 2. Gemini AI 분석 (NEW!)
        List<Message> recentMessages = messageRepository.findRecentUserMessages(
            userId, PageRequest.of(0, 50)
        );

        Map<String, BigDecimal> geminiScores = new HashMap<>();
        if (!recentMessages.isEmpty()) {
            try {
                geminiScores = geminiService.analyzeHumanIdentity(recentMessages);
                log.info("Gemini HIP analysis completed for user: {} with {} messages",
                    userId, recentMessages.size());
            } catch (Exception e) {
                log.error("Gemini HIP analysis failed for user: {}, using AffinityScore only", userId, e);
            }
        } else {
            log.warn("No messages found for user: {}, skipping Gemini analysis", userId);
        }

        // 3. AffinityScore + Gemini 가중 평균 (40% + 60%)
        BigDecimal cognitiveFlexibility = combineScores(
            affinityScores.get("cognitiveFlexibility"),
            geminiScores.get("cognitiveFlexibility")
        );
        BigDecimal collaborationPattern = combineScores(
            affinityScores.get("collaborationPattern"),
            geminiScores.get("collaborationPattern")
        );
        BigDecimal informationProcessing = combineScores(
            affinityScores.get("informationProcessing"),
            geminiScores.get("informationProcessing")
        );
        BigDecimal emotionalIntelligence = combineScores(
            affinityScores.get("emotionalIntelligence"),
            geminiScores.get("emotionalIntelligence")
        );
        BigDecimal creativity = combineScores(
            affinityScores.get("creativity"),
            geminiScores.get("creativity")
        );
        BigDecimal ethicalAlignment = combineScores(
            affinityScores.get("ethicalAlignment"),
            geminiScores.get("ethicalAlignment")
        );

        log.info("Combined HIP scores: cognitive={}, collaboration={}, information={}, emotional={}, creativity={}, ethical={}",
            cognitiveFlexibility, collaborationPattern, informationProcessing,
            emotionalIntelligence, creativity, ethicalAlignment);

        // 4. HIP Core Scores 업데이트
        hip.updateCoreScores(
            cognitiveFlexibility,
            collaborationPattern,
            informationProcessing,
            emotionalIntelligence,
            creativity,
            ethicalAlignment
        );

        // 5. Message 기반 데이터 품질 점수 업데이트
        long messageCount = messageRepository.countByUserId(userId);
        hip.updateDataQualityScore((int) messageCount);

        // 6. 상호작용 수 업데이트
        hip.setTotalInteractions((int) messageCount);

        // 7. AI Trust Score 계산 (Overall HIP Score 기반)
        BigDecimal trustScore = hip.getOverallHipScore().multiply(BigDecimal.valueOf(0.9));
        hip.setAiTrustScore(trustScore);

        // 8. 저장
        HumanIdentityProfile updatedHip = hipRepository.save(hip);

        // 9. 검증 로그 생성
        String analysisMethod = !geminiScores.isEmpty()
            ? "gemini_ai_combined"
            : "affinity_score_only";

        IdentityVerificationLog verificationLog = IdentityVerificationLog.builder()
            .hipId(hip.getHipId())
            .verificationType("periodic")
            .verificationMethod(analysisMethod)
            .previousScore(previousScore)
            .newScore(hip.getOverallHipScore())
            .status("verified")
            .notes("Automatic reanalysis with Gemini AI + AffinityScore (60%/40%)")
            .build();
        verificationLogRepository.save(verificationLog);

        log.info("Reanalyzed HIP: {} - Score: {} -> {} (method: {})",
            hip.getHipId(), previousScore, hip.getOverallHipScore(), analysisMethod);

        return updatedHip;
    }

    /**
     * HIP 검증 (주기적 또는 수동)
     */
    @Transactional
    public HumanIdentityProfile verifyProfile(String hipId) {
        HumanIdentityProfile hip = hipRepository.findById(hipId)
            .orElseThrow(() -> new IllegalArgumentException("HIP not found: " + hipId));

        BigDecimal previousScore = hip.getOverallHipScore();

        // 검증 로직 (데이터 일관성, 이상 패턴 탐지 등)
        boolean isValid = performVerificationChecks(hip);

        if (isValid) {
            hip.markAsVerified();
        } else {
            hip.markAsFlagged();
        }

        HumanIdentityProfile updatedHip = hipRepository.save(hip);

        // 검증 로그 생성
        IdentityVerificationLog verificationLog = IdentityVerificationLog.builder()
            .hipId(hipId)
            .verificationType("challenge")
            .verificationMethod("consistency_check")
            .previousScore(previousScore)
            .newScore(hip.getOverallHipScore())
            .status(isValid ? "verified" : "flagged")
            .notes(isValid ? "Verification passed" : "Anomaly detected")
            .build();
        verificationLogRepository.save(verificationLog);

        return updatedHip;
    }

    /**
     * HIP 조회 (by userId)
     */
    @Transactional(readOnly = true)
    public Optional<HumanIdentityProfile> getProfile(Long userId) {
        return hipRepository.findByUserId(userId);
    }

    /**
     * HIP 조회 (by hipId)
     */
    @Transactional(readOnly = true)
    public Optional<HumanIdentityProfile> getProfileByHipId(String hipId) {
        return hipRepository.findById(hipId);
    }

    /**
     * 상위 랭킹 HIP 조회
     */
    @Transactional(readOnly = true)
    public List<HumanIdentityProfile> getTopProfiles(int limit) {
        return hipRepository.findTopByScore(limit);
    }

    /**
     * 검증이 필요한 HIP 목록 조회
     */
    @Transactional(readOnly = true)
    public List<HumanIdentityProfile> getProfilesNeedingVerification() {
        return hipRepository.findProfilesNeedingVerification();
    }

    // ==================== Private Helper Methods ====================

    /**
     * AffinityScore와 Gemini 점수를 가중 평균으로 결합
     * AffinityScore: 40%, Gemini: 60%
     */
    private BigDecimal combineScores(BigDecimal affinityScore, BigDecimal geminiScore) {
        if (affinityScore == null && geminiScore == null) {
            return BigDecimal.valueOf(50.0); // 기본값
        }
        if (affinityScore == null) {
            return geminiScore;
        }
        if (geminiScore == null) {
            return affinityScore;
        }

        // 가중 평균: AffinityScore 40% + Gemini 60%
        BigDecimal weighted = affinityScore.multiply(BigDecimal.valueOf(0.4))
            .add(geminiScore.multiply(BigDecimal.valueOf(0.6)));

        return weighted.setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 인지적 유연성 계산
     * AffinityScore의 context_score와 usage_score 기반
     */
    private BigDecimal calculateCognitiveFlexibility(AffinityScore affinity) {
        BigDecimal contextScore = affinity.getAvgContextScore().multiply(BigDecimal.valueOf(100));
        BigDecimal usageScore = affinity.getAvgUsageScore().multiply(BigDecimal.valueOf(100));
        return contextScore.add(usageScore).divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 협업 패턴 계산
     * AffinityScore의 sentiment_score와 clarity_score 기반
     */
    private BigDecimal calculateCollaborationPattern(AffinityScore affinity) {
        // sentiment: -1 ~ 1 -> 0 ~ 100 변환
        BigDecimal sentimentScore = affinity.getAvgSentimentScore()
            .add(BigDecimal.ONE)
            .multiply(BigDecimal.valueOf(50));
        BigDecimal clarityScore = affinity.getAvgClarityScore().multiply(BigDecimal.valueOf(100));
        return sentimentScore.add(clarityScore).divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 정보 처리 방식 계산
     * AffinityScore의 clarity_score 기반
     */
    private BigDecimal calculateInformationProcessing(AffinityScore affinity) {
        return affinity.getAvgClarityScore().multiply(BigDecimal.valueOf(100));
    }

    /**
     * 감정 지능 계산
     * AffinityScore의 sentiment_score 기반
     */
    private BigDecimal calculateEmotionalIntelligence(AffinityScore affinity) {
        // sentiment: -1 ~ 1 -> 0 ~ 100 변환
        return affinity.getAvgSentimentScore()
            .add(BigDecimal.ONE)
            .multiply(BigDecimal.valueOf(50));
    }

    /**
     * 창의성 계산
     * AffinityScore의 usage_score 기반 (AI 활용의 독창성)
     */
    private BigDecimal calculateCreativity(AffinityScore affinity) {
        return affinity.getAvgUsageScore().multiply(BigDecimal.valueOf(100));
    }

    /**
     * 윤리적 정렬 계산
     * AffinityScore의 overall_score 기반
     */
    private BigDecimal calculateEthicalAlignment(AffinityScore affinity) {
        return affinity.getOverallScore();
    }

    /**
     * HIP 검증 체크
     * 실제로는 더 복잡한 로직 필요 (이상 패턴 탐지, ML 모델 등)
     */
    private boolean performVerificationChecks(HumanIdentityProfile hip) {
        // 간단한 검증 예시
        // 1. 데이터 품질이 최소 기준 이상인지
        if (hip.getDataQualityScore().compareTo(BigDecimal.valueOf(20)) < 0) {
            return false;
        }

        // 2. 최소 상호작용 수가 있는지
        if (hip.getTotalInteractions() < 5) {
            return false;
        }

        // 3. 점수가 유효 범위 내인지
        if (hip.getOverallHipScore().compareTo(BigDecimal.ZERO) < 0 ||
            hip.getOverallHipScore().compareTo(BigDecimal.valueOf(100)) > 0) {
            return false;
        }

        return true;
    }
}
