package com.lobai.controller;

import com.lobai.dto.request.CreateScheduleRequest;
import com.lobai.dto.request.UpdateScheduleRequest;
import com.lobai.dto.response.ApiResponse;
import com.lobai.dto.response.ScheduleResponse;
import com.lobai.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ScheduleController
 *
 * 일정 관리 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * 일정 생성
     *
     * POST /api/schedules
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(
            @Valid @RequestBody CreateScheduleRequest request) {
        log.info("Create schedule request: {}", request.getTitle());

        ScheduleResponse schedule = scheduleService.createSchedule(request);

        return ResponseEntity.ok(ApiResponse.success("일정이 생성되었습니다", schedule));
    }

    /**
     * 오늘 일정 조회
     *
     * GET /api/schedules/today
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getTodaysSchedules() {
        log.info("Get today's schedules request");

        List<ScheduleResponse> schedules = scheduleService.getTodaysSchedules();

        return ResponseEntity.ok(ApiResponse.success("오늘 일정 조회 성공", schedules));
    }

    /**
     * 날짜 범위로 일정 조회
     *
     * GET /api/schedules/range?start=2024-01-01T00:00:00&end=2024-01-31T23:59:59
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("Get schedules by range request: {} to {}", start, end);

        List<ScheduleResponse> schedules = scheduleService.getSchedulesByDateRange(start, end);

        return ResponseEntity.ok(ApiResponse.success("일정 조회 성공", schedules));
    }

    /**
     * 다가오는 일정 조회 (최대 N개)
     *
     * GET /api/schedules/upcoming?limit=5
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getUpcomingSchedules(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Get upcoming schedules request: limit={}", limit);

        List<ScheduleResponse> schedules = scheduleService.getUpcomingSchedules(limit);

        return ResponseEntity.ok(ApiResponse.success("다가오는 일정 조회 성공", schedules));
    }

    /**
     * 일정 수정
     *
     * PUT /api/schedules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateScheduleRequest request) {
        log.info("Update schedule request: id={}", id);

        ScheduleResponse schedule = scheduleService.updateSchedule(id, request);

        return ResponseEntity.ok(ApiResponse.success("일정이 수정되었습니다", schedule));
    }

    /**
     * 일정 삭제 (소프트 삭제)
     *
     * DELETE /api/schedules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable Long id) {
        log.info("Delete schedule request: id={}", id);

        scheduleService.deleteSchedule(id);

        return ResponseEntity.ok(ApiResponse.success("일정이 삭제되었습니다", null));
    }
}
