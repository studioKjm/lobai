package com.lobai.controller;

import com.lobai.dto.response.AffinityScoreResponse;
import com.lobai.dto.response.ApiResponse;
import com.lobai.entity.AffinityScore;
import com.lobai.security.SecurityUtil;
import com.lobai.service.AffinityScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AffinityController
 *
 * 친밀도 점수 관련 REST API (Phase 1)
 */
@Slf4j
@RestController
@RequestMapping("/api/affinity")
@RequiredArgsConstructor
public class AffinityController {

    private final AffinityScoreService affinityScoreService;

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
     * 친밀도 점수 강제 재계산 (디버깅용, 추후 제거 또는 ADMIN 전용으로 변경)
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
}
