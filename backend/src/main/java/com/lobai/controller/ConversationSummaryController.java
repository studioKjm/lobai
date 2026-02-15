package com.lobai.controller;

import com.lobai.dto.response.ApiResponse;
import com.lobai.dto.response.DailySummaryDetailResponse;
import com.lobai.dto.response.DailySummaryListResponse;
import com.lobai.entity.User;
import com.lobai.repository.UserRepository;
import com.lobai.security.SecurityUtil;
import com.lobai.service.ConversationSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 대화 요약 API
 */
@Slf4j
@RestController
@RequestMapping("/api/conversation-summaries")
@RequiredArgsConstructor
public class ConversationSummaryController {

    private final ConversationSummaryService conversationSummaryService;
    private final UserRepository userRepository;

    /**
     * 일일 요약 목록 조회
     *
     * GET /api/conversation-summaries/daily?limit=30
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<DailySummaryListResponse>>> getDailySummaries(
            @RequestParam(required = false, defaultValue = "30") int limit) {

        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Get daily summaries for user {}: limit={}", userId, limit);

        List<DailySummaryListResponse> summaries = conversationSummaryService.getDailySummaryList(userId, limit);

        return ResponseEntity.ok(ApiResponse.success(summaries));
    }

    /**
     * 특정 날짜 일일 요약 + 원본 메시지 조회
     *
     * GET /api/conversation-summaries/daily/{date}
     */
    @GetMapping("/daily/{date}")
    public ResponseEntity<ApiResponse<DailySummaryDetailResponse>> getDailySummaryDetail(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Get daily summary detail for user {} on {}", userId, date);

        DailySummaryDetailResponse detail = conversationSummaryService.getDailySummaryDetail(userId, date);

        if (detail == null) {
            return ResponseEntity.ok(ApiResponse.success("해당 날짜의 요약이 없습니다.", null));
        }

        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    /**
     * 수동 일일 요약 생성 트리거
     *
     * POST /api/conversation-summaries/daily/generate?date=2026-02-15
     */
    @PostMapping("/daily/generate")
    public ResponseEntity<ApiResponse<String>> generateDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Manual daily summary generation for user {} on {}", userId, date);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        boolean generated = conversationSummaryService.generateDailySummary(user, date);

        if (generated) {
            return ResponseEntity.ok(ApiResponse.success("일일 요약이 생성되었습니다.", "generated"));
        } else {
            return ResponseEntity.ok(ApiResponse.success("해당 날짜에 메시지가 없거나 이미 요약이 존재합니다.", "skipped"));
        }
    }
}
