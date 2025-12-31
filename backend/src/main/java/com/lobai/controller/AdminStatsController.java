package com.lobai.controller;

import com.lobai.dto.response.ApiResponse;
import com.lobai.dto.response.admin.*;
import com.lobai.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Statistics Controller
 *
 * 관리자 통계 조회 API
 */
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@Slf4j
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    /**
     * GET /api/admin/stats/overview
     * 전체 통계 개요 조회
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StatsOverviewResponse>> getStatsOverview() {
        log.info("Admin: Get stats overview");
        StatsOverviewResponse response = adminStatsService.getStatsOverview();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/admin/stats/users/active
     * 활성 사용자 차트 데이터 조회 (최근 30일)
     */
    @GetMapping("/users/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ActiveUsersChartResponse>> getActiveUsersChart() {
        log.info("Admin: Get active users chart");
        ActiveUsersChartResponse response = adminStatsService.getActiveUsersChart();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/admin/stats/messages
     * 메시지 통계 조회
     */
    @GetMapping("/messages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MessageStatsResponse>> getMessageStats() {
        log.info("Admin: Get message statistics");
        MessageStatsResponse response = adminStatsService.getMessageStats();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/admin/stats/personas
     * 페르소나별 사용 통계 조회
     */
    @GetMapping("/personas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PersonaStatsResponse>> getPersonaStats() {
        log.info("Admin: Get persona statistics");
        PersonaStatsResponse response = adminStatsService.getPersonaStats();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
