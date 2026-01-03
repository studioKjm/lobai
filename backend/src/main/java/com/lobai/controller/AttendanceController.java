package com.lobai.controller;

import com.lobai.dto.response.ApiResponse;
import com.lobai.entity.AttendanceRecord;
import com.lobai.security.SecurityUtil;
import com.lobai.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AttendanceController
 * 출석 체크 API
 */
@Slf4j
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * 출석 체크
     * POST /api/attendance/check-in
     */
    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn() {
        Long userId = SecurityUtil.getCurrentUserId();

        try {
            AttendanceRecord record = attendanceService.checkIn(userId);
            AttendanceResponse response = AttendanceResponse.from(record);

            log.info("출석 체크 성공: userId={}, streak={}", userId, record.getStreakCount());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalStateException e) {
            log.warn("출석 체크 실패: userId={}, reason={}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 출석 현황 조회
     * GET /api/attendance/status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<AttendanceStatusResponse>> getStatus() {
        Long userId = SecurityUtil.getCurrentUserId();

        AttendanceService.AttendanceStatus status = attendanceService.getAttendanceStatus(userId);
        AttendanceStatusResponse response = AttendanceStatusResponse.from(status);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 최근 출석 기록 조회 (30일)
     * GET /api/attendance/recent?days=30
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getRecentAttendance(
            @RequestParam(defaultValue = "30") int days) {
        Long userId = SecurityUtil.getCurrentUserId();

        List<AttendanceRecord> records = attendanceService.getRecentAttendance(userId, days);
        List<AttendanceResponse> responses = records.stream()
                .map(AttendanceResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * 출석 체크 응답 DTO
     */
    @lombok.Getter
    @lombok.Builder
    public static class AttendanceResponse {
        private Long id;
        private String checkInDate;
        private String checkInTime;
        private Integer streakCount;
        private Integer rewardPoints;

        public static AttendanceResponse from(AttendanceRecord record) {
            return AttendanceResponse.builder()
                    .id(record.getId())
                    .checkInDate(record.getCheckInDate().toString())
                    .checkInTime(record.getCheckInTime().toString())
                    .streakCount(record.getStreakCount())
                    .rewardPoints(record.getRewardPoints())
                    .build();
        }
    }

    /**
     * 출석 현황 응답 DTO
     */
    @lombok.Getter
    @lombok.Builder
    public static class AttendanceStatusResponse {
        private boolean checkedInToday;
        private int currentStreak;
        private int totalAttendanceDays;
        private int maxStreak;

        public static AttendanceStatusResponse from(AttendanceService.AttendanceStatus status) {
            return AttendanceStatusResponse.builder()
                    .checkedInToday(status.isCheckedInToday())
                    .currentStreak(status.getCurrentStreak())
                    .totalAttendanceDays(status.getTotalAttendanceDays())
                    .maxStreak(status.getMaxStreak())
                    .build();
        }
    }
}
