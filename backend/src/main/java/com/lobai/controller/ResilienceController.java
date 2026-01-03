package com.lobai.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lobai.dto.response.ApiResponse;
import com.lobai.entity.ResilienceReport;
import com.lobai.security.SecurityUtil;
import com.lobai.service.ResilienceAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ResilienceController
 * AI 적응력 분석 API
 */
@Slf4j
@RestController
@RequestMapping("/api/resilience")
@RequiredArgsConstructor
public class ResilienceController {

    private final ResilienceAnalysisService resilienceAnalysisService;
    private final ObjectMapper objectMapper;

    /**
     * AI 적응력 리포트 생성
     * POST /api/resilience/generate?type=basic
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ResilienceReportResponse>> generateReport(
            @RequestParam(defaultValue = "basic") String type) {
        Long userId = SecurityUtil.getCurrentUserId();

        try {
            ResilienceReport.ReportType reportType = ResilienceReport.ReportType.valueOf(type);
            ResilienceReport report = resilienceAnalysisService.generateReport(userId, reportType);
            ResilienceReportResponse response = ResilienceReportResponse.from(report, objectMapper);

            log.info("AI 적응력 리포트 생성 성공: userId={}, type={}, readiness={}",
                    userId, type, report.getReadinessScore());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.error("리포트 생성 실패 (잘못된 파라미터): userId={}, type={}", userId, type, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("잘못된 리포트 유형입니다"));

        } catch (IllegalStateException e) {
            log.warn("리포트 생성 실패 (데이터 부족): userId={}, reason={}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("리포트 생성 실패 (시스템 오류): userId={}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("리포트 생성 중 오류가 발생했습니다"));
        }
    }

    /**
     * 최근 리포트 조회
     * GET /api/resilience/latest
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<ResilienceReportResponse>> getLatestReport() {
        Long userId = SecurityUtil.getCurrentUserId();

        return resilienceAnalysisService.getLatestReport(userId)
                .map(report -> {
                    ResilienceReportResponse response = ResilienceReportResponse.from(report, objectMapper);
                    return ResponseEntity.ok(ApiResponse.success(response));
                })
                .orElse(ResponseEntity.ok(
                        ApiResponse.error("아직 생성된 리포트가 없습니다")));
    }

    /**
     * 모든 리포트 목록 조회
     * GET /api/resilience/reports
     */
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<ResilienceReportSummary>>> getAllReports() {
        Long userId = SecurityUtil.getCurrentUserId();

        List<ResilienceReport> reports = resilienceAnalysisService.getAllReports(userId);
        List<ResilienceReportSummary> summaries = reports.stream()
                .map(ResilienceReportSummary::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(summaries));
    }

    /**
     * 리포트 상세 조회
     * GET /api/resilience/reports/{reportId}
     */
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<ResilienceReportResponse>> getReport(
            @PathVariable Long reportId) {
        Long userId = SecurityUtil.getCurrentUserId();

        // TODO: reportId로 조회 후 userId 검증
        // 임시로 latest 리포트 반환
        return getLatestReport();
    }

    /**
     * 리포트 잠금 해제 (결제 연동 시)
     * POST /api/resilience/reports/{reportId}/unlock
     */
    @PostMapping("/reports/{reportId}/unlock")
    public ResponseEntity<ApiResponse<ResilienceReportResponse>> unlockReport(
            @PathVariable Long reportId) {
        Long userId = SecurityUtil.getCurrentUserId();

        try {
            ResilienceReport report = resilienceAnalysisService.unlockReport(reportId, userId);
            ResilienceReportResponse response = ResilienceReportResponse.from(report, objectMapper);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 리포트 상세 응답 DTO
     */
    @lombok.Getter
    @lombok.Builder
    public static class ResilienceReportResponse {
        private Long id;
        private String reportType;
        private String reportVersion;

        private BigDecimal readinessScore;
        private Integer readinessLevel;
        private String readinessLevelName;

        private BigDecimal communicationScore;
        private BigDecimal automationRiskScore;
        private BigDecimal collaborationScore;
        private BigDecimal misuseRiskScore;

        private List<String> strengths;
        private List<String> weaknesses;
        private String simulationResult;
        private String detailedFeedback;

        private Integer analyzedMessageCount;
        private Integer analyzedPeriodDays;

        private Boolean isUnlocked;
        private String createdAt;

        public static ResilienceReportResponse from(ResilienceReport report, ObjectMapper mapper) {
            List<String> strengths = null;
            List<String> weaknesses = null;

            try {
                if (report.getStrengths() != null) {
                    strengths = mapper.readValue(report.getStrengths(), new TypeReference<>() {});
                }
                if (report.getWeaknesses() != null) {
                    weaknesses = mapper.readValue(report.getWeaknesses(), new TypeReference<>() {});
                }
            } catch (Exception e) {
                log.error("JSON 파싱 실패", e);
            }

            return ResilienceReportResponse.builder()
                    .id(report.getId())
                    .reportType(report.getReportType().name())
                    .reportVersion(report.getReportVersion())
                    .readinessScore(report.getReadinessScore())
                    .readinessLevel(report.getReadinessLevel())
                    .readinessLevelName(report.getReadinessLevelName())
                    .communicationScore(report.getCommunicationScore())
                    .automationRiskScore(report.getAutomationRiskScore())
                    .collaborationScore(report.getCollaborationScore())
                    .misuseRiskScore(report.getMisuseRiskScore())
                    .strengths(strengths)
                    .weaknesses(weaknesses)
                    .simulationResult(report.getSimulationResult())
                    .detailedFeedback(report.getDetailedFeedback())
                    .analyzedMessageCount(report.getAnalyzedMessageCount())
                    .analyzedPeriodDays(report.getAnalyzedPeriodDays())
                    .isUnlocked(report.getIsUnlocked())
                    .createdAt(report.getCreatedAt().toString())
                    .build();
        }
    }

    /**
     * 리포트 요약 DTO (목록용)
     */
    @lombok.Getter
    @lombok.Builder
    public static class ResilienceReportSummary {
        private Long id;
        private String reportType;
        private BigDecimal readinessScore;
        private Integer readinessLevel;
        private String readinessLevelName;
        private Boolean isUnlocked;
        private String createdAt;

        public static ResilienceReportSummary from(ResilienceReport report) {
            return ResilienceReportSummary.builder()
                    .id(report.getId())
                    .reportType(report.getReportType().name())
                    .readinessScore(report.getReadinessScore())
                    .readinessLevel(report.getReadinessLevel())
                    .readinessLevelName(report.getReadinessLevelName())
                    .isUnlocked(report.getIsUnlocked())
                    .createdAt(report.getCreatedAt().toString())
                    .build();
        }
    }
}
