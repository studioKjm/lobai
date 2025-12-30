package com.lobai.controller;

import com.lobai.dto.request.UpdateStatsRequest;
import com.lobai.dto.response.ApiResponse;
import com.lobai.dto.response.StatsResponse;
import com.lobai.security.SecurityUtil;
import com.lobai.service.StatsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * StatsController
 *
 * 사용자 Stats 관련 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 현재 Stats 조회
     *
     * GET /api/stats
     */
    @GetMapping
    public ResponseEntity<ApiResponse<StatsResponse>> getStats() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Get stats request from user {}", userId);

        StatsResponse stats = statsService.getUserStats(userId);

        return ResponseEntity
                .ok(ApiResponse.success(stats));
    }

    /**
     * Stats 업데이트 (사용자 액션)
     *
     * PUT /api/stats
     */
    @PutMapping
    public ResponseEntity<ApiResponse<StatsResponse>> updateStats(
            @Valid @RequestBody UpdateStatsRequest request) {

        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Update stats request from user {}: action={}", userId, request.getAction());

        StatsResponse stats = statsService.updateStats(userId, request);

        return ResponseEntity
                .ok(ApiResponse.success("Stats가 업데이트되었습니다", stats));
    }

    /**
     * Stats 자동 감소 적용
     *
     * POST /api/stats/decay
     */
    @PostMapping("/decay")
    public ResponseEntity<ApiResponse<StatsResponse>> applyDecay() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Apply decay request from user {}", userId);

        StatsResponse stats = statsService.applyDecay(userId);

        return ResponseEntity
                .ok(ApiResponse.success(stats));
    }
}
