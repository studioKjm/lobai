package com.lobai.controller;

import com.lobai.dto.response.AffinityScoreResponse;
import com.lobai.dto.response.ApiResponse;
import com.lobai.entity.AffinityScore;
import com.lobai.entity.AffinityScoreHistory;
import com.lobai.security.SecurityUtil;
import com.lobai.service.AffinityHistoryService;
import com.lobai.service.AffinityScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * AffinityController
 *
 * 친밀도 점수 관련 REST API (Phase 2 - 7차원 고도화)
 */
@Slf4j
@RestController
@RequestMapping("/api/affinity")
@RequiredArgsConstructor
public class AffinityController {

    private final AffinityScoreService affinityScoreService;
    private final AffinityHistoryService affinityHistoryService;

    /**
     * 사용자 친밀도 점수 조회 (무료)
     *
     * GET /api/affinity/score
     */
    @GetMapping("/score")
    public ResponseEntity<ApiResponse<AffinityScoreResponse>> getAffinityScore() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Get affinity score request from user: userId={}", userId);

        AffinityScore affinityScore = affinityScoreService.getUserAffinityScore(userId);
        AffinityScoreResponse response = AffinityScoreResponse.from(affinityScore);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 친밀도 점수 강제 재계산
     *
     * POST /api/affinity/recalculate
     */
    @PostMapping("/recalculate")
    public ResponseEntity<ApiResponse<AffinityScoreResponse>> recalculateScore() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Recalculate affinity score request from user: userId={}", userId);

        AffinityScore affinityScore = affinityScoreService.recalculateOverallScore(userId);
        AffinityScoreResponse response = AffinityScoreResponse.from(affinityScore);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 친밀도 트렌드 데이터 조회 (일별 스냅샷)
     *
     * GET /api/affinity/trend?days=30
     */
    @GetMapping("/trend")
    public ResponseEntity<ApiResponse<List<AffinityTrendResponse>>> getAffinityTrend(
            @RequestParam(defaultValue = "30") int days) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Get affinity trend request: userId={}, days={}", userId, days);

        List<AffinityScoreHistory> history = affinityHistoryService.getTrend(userId, days);
        List<AffinityTrendResponse> trend = history.stream()
                .map(AffinityTrendResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(trend));
    }

    /**
     * 트렌드 응답 DTO
     */
    @lombok.Getter
    @lombok.Builder
    public static class AffinityTrendResponse {
        private String date;
        private BigDecimal overallScore;
        private Integer level;
        private BigDecimal avgSentimentScore;
        private BigDecimal avgClarityScore;
        private BigDecimal avgContextScore;
        private BigDecimal avgUsageScore;
        private BigDecimal avgEngagementDepth;
        private BigDecimal avgSelfDisclosure;
        private BigDecimal avgReciprocity;
        private String relationshipStage;
        private String dataThresholdStatus;
        private Integer totalMessages;

        public static AffinityTrendResponse from(AffinityScoreHistory h) {
            return AffinityTrendResponse.builder()
                    .date(h.getSnapshotDate().toString())
                    .overallScore(h.getOverallScore())
                    .level(h.getLevel())
                    .avgSentimentScore(h.getAvgSentimentScore())
                    .avgClarityScore(h.getAvgClarityScore())
                    .avgContextScore(h.getAvgContextScore())
                    .avgUsageScore(h.getAvgUsageScore())
                    .avgEngagementDepth(h.getAvgEngagementDepth())
                    .avgSelfDisclosure(h.getAvgSelfDisclosure())
                    .avgReciprocity(h.getAvgReciprocity())
                    .relationshipStage(h.getRelationshipStage())
                    .dataThresholdStatus(h.getDataThresholdStatus())
                    .totalMessages(h.getTotalMessages())
                    .build();
        }
    }
}
