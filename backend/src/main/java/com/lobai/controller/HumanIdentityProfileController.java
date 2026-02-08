package com.lobai.controller;

import com.lobai.entity.HumanIdentityProfile;
import com.lobai.service.HumanIdentityProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HumanIdentityProfileController
 *
 * HIP 관련 REST API
 */
@RestController
@RequestMapping("/api/hip")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class HumanIdentityProfileController {

    private final HumanIdentityProfileService hipService;

    /**
     * 내 HIP 조회
     * GET /api/hip/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            HumanIdentityProfile hip = hipService.getOrCreateProfile(userId);

            Map<String, Object> response = buildProfileResponse(hip);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get HIP: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * HIP ID로 조회 (공개 프로필)
     * GET /api/hip/{hipId}
     */
    @GetMapping("/{hipId}")
    public ResponseEntity<?> getProfileByHipId(@PathVariable String hipId) {
        try {
            HumanIdentityProfile hip = hipService.getProfileByHipId(hipId)
                .orElseThrow(() -> new IllegalArgumentException("HIP not found"));

            Map<String, Object> response = buildPublicProfileResponse(hip);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get HIP by ID: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 내 HIP 재분석 요청
     * POST /api/hip/me/reanalyze
     */
    @PostMapping("/me/reanalyze")
    public ResponseEntity<?> reanalyzeMyProfile(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            HumanIdentityProfile hip = hipService.reanalyzeProfile(userId);

            Map<String, Object> response = buildProfileResponse(hip);
            response.put("message", "Profile reanalyzed successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to reanalyze HIP: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 내 HIP 검증 요청
     * POST /api/hip/me/verify
     */
    @PostMapping("/me/verify")
    public ResponseEntity<?> verifyMyProfile(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            HumanIdentityProfile hip = hipService.getProfile(userId)
                .orElseThrow(() -> new IllegalArgumentException("HIP not found"));

            HumanIdentityProfile verifiedHip = hipService.verifyProfile(hip.getHipId());

            Map<String, Object> response = buildProfileResponse(verifiedHip);
            response.put("message", "Profile verification completed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to verify HIP: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * HIP 랭킹 조회
     * GET /api/hip/ranking?limit=10
     */
    @GetMapping("/ranking")
    public ResponseEntity<?> getRanking(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<HumanIdentityProfile> topProfiles = hipService.getTopProfiles(limit);

            List<Map<String, Object>> ranking = topProfiles.stream()
                .map(this::buildPublicProfileResponse)
                .toList();

            return ResponseEntity.ok(Map.of(
                "ranking", ranking,
                "total", ranking.size()
            ));

        } catch (Exception e) {
            log.error("Failed to get ranking: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * HIP 통계 조회
     * GET /api/hip/me/stats
     */
    @GetMapping("/me/stats")
    public ResponseEntity<?> getMyStats(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            HumanIdentityProfile hip = hipService.getProfile(userId)
                .orElseThrow(() -> new IllegalArgumentException("HIP not found"));

            Map<String, Object> stats = new HashMap<>();
            stats.put("hipId", hip.getHipId());
            stats.put("identityLevel", hip.getIdentityLevel());
            stats.put("identityLevelName", hip.getIdentityLevelName());
            stats.put("overallScore", hip.getOverallHipScore());
            stats.put("aiTrustScore", hip.getAiTrustScore());
            stats.put("dataQualityScore", hip.getDataQualityScore());
            stats.put("totalInteractions", hip.getTotalInteractions());
            stats.put("verificationStatus", hip.getVerificationStatus());

            // Core Scores
            Map<String, Object> coreScores = new HashMap<>();
            coreScores.put("cognitiveFlexibility", hip.getCognitiveFlexibilityScore());
            coreScores.put("collaborationPattern", hip.getCollaborationPatternScore());
            coreScores.put("informationProcessing", hip.getInformationProcessingScore());
            coreScores.put("emotionalIntelligence", hip.getEmotionalIntelligenceScore());
            coreScores.put("creativity", hip.getCreativityScore());
            coreScores.put("ethicalAlignment", hip.getEthicalAlignmentScore());

            stats.put("coreScores", coreScores);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Failed to get stats: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== Helper Methods ====================

    /**
     * 전체 프로필 응답 생성 (본인용)
     */
    private Map<String, Object> buildProfileResponse(HumanIdentityProfile hip) {
        Map<String, Object> response = new HashMap<>();
        response.put("hipId", hip.getHipId());
        response.put("userId", hip.getUserId());
        response.put("identityLevel", hip.getIdentityLevel());
        response.put("identityLevelName", hip.getIdentityLevelName());
        response.put("reputationLevel", hip.getReputationLevel());
        response.put("overallScore", hip.getOverallHipScore());
        response.put("aiTrustScore", hip.getAiTrustScore());
        response.put("dataQualityScore", hip.getDataQualityScore());
        response.put("totalInteractions", hip.getTotalInteractions());
        response.put("verificationStatus", hip.getVerificationStatus());
        response.put("lastVerifiedAt", hip.getLastVerifiedAt());
        response.put("createdAt", hip.getCreatedAt());
        response.put("updatedAt", hip.getUpdatedAt());

        // Core Scores
        Map<String, Object> coreScores = new HashMap<>();
        coreScores.put("cognitiveFlexibility", hip.getCognitiveFlexibilityScore());
        coreScores.put("collaborationPattern", hip.getCollaborationPatternScore());
        coreScores.put("informationProcessing", hip.getInformationProcessingScore());
        coreScores.put("emotionalIntelligence", hip.getEmotionalIntelligenceScore());
        coreScores.put("creativity", hip.getCreativityScore());
        coreScores.put("ethicalAlignment", hip.getEthicalAlignmentScore());

        response.put("coreScores", coreScores);

        return response;
    }

    /**
     * 공개 프로필 응답 생성 (타인용)
     */
    private Map<String, Object> buildPublicProfileResponse(HumanIdentityProfile hip) {
        Map<String, Object> response = new HashMap<>();
        response.put("hipId", hip.getHipId());
        response.put("identityLevel", hip.getIdentityLevel());
        response.put("identityLevelName", hip.getIdentityLevelName());
        response.put("reputationLevel", hip.getReputationLevel());
        response.put("overallScore", hip.getOverallHipScore());
        response.put("verificationStatus", hip.getVerificationStatus());

        // 민감한 정보는 제외 (userId, 상세 점수 등)

        return response;
    }
}
