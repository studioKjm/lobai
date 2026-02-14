package com.lobai.controller;

import com.lobai.dto.request.StartTrainingRequest;
import com.lobai.dto.request.SubmitAnswerRequest;
import com.lobai.dto.response.TrainingSessionResponse;
import com.lobai.dto.response.TrainingStatisticsResponse;
import com.lobai.entity.TrainingSession;
import com.lobai.entity.TrainingStatistics;
import com.lobai.security.SecurityUtil;
import com.lobai.service.TrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/api/training", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    /**
     * 훈련 세션 시작
     * POST /api/training/start
     */
    @PostMapping(value = "/start", produces = "application/json;charset=UTF-8")
    public ResponseEntity<TrainingSessionResponse> startTraining(
        @RequestBody StartTrainingRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Starting training: user={}, type={}, difficulty={}",
            userId, request.getTrainingType(), request.getDifficultyLevel());

        // 난이도 검증
        if (request.getDifficultyLevel() < 1 || request.getDifficultyLevel() > 10) {
            throw new IllegalArgumentException("난이도는 1-10 사이의 값이어야 합니다.");
        }

        TrainingSession session = trainingService.startTraining(
            userId,
            request.getTrainingType(),
            request.getDifficultyLevel()
        );

        return ResponseEntity.ok(TrainingSessionResponse.from(session));
    }

    /**
     * 힌트 요청
     * POST /api/training/hint
     */
    @PostMapping(value = "/hint", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Map<String, Object>> requestHint(
        @RequestBody Map<String, Long> request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long sessionId = request.get("sessionId");
        log.info("Hint requested: user={}, session={}", userId, sessionId);

        String hint = trainingService.requestHint(userId, sessionId);

        Map<String, Object> response = new HashMap<>();
        response.put("hint", hint);
        response.put("cost", 5); // LobCoin 차감액

        return ResponseEntity.ok(response);
    }

    /**
     * 답변 제출
     * POST /api/training/submit
     */
    @PostMapping(value = "/submit", produces = "application/json;charset=UTF-8")
    public ResponseEntity<TrainingSessionResponse> submitAnswer(
        @RequestBody SubmitAnswerRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Answer submitted: user={}, session={}", userId, request.getSessionId());

        TrainingSession session = trainingService.submitAnswer(
            userId,
            request.getSessionId(),
            request.getAnswer(),
            request.getTimeTakenSeconds()
        );

        return ResponseEntity.ok(TrainingSessionResponse.from(session));
    }

    /**
     * 훈련 세션 중단
     * POST /api/training/{sessionId}/abandon
     */
    @PostMapping("/{sessionId}/abandon")
    public ResponseEntity<Void> abandonSession(
        @PathVariable Long sessionId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Session abandoned: user={}, session={}", userId, sessionId);

        trainingService.abandonSession(userId, sessionId);

        return ResponseEntity.ok().build();
    }

    /**
     * 훈련 기록 조회
     * GET /api/training/history?type=CRITICAL_THINKING
     */
    @GetMapping(value = "/history", produces = "application/json;charset=UTF-8")
    public ResponseEntity<List<TrainingSessionResponse>> getHistory(
        @RequestParam(required = false) TrainingSession.TrainingType type
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Training history requested: user={}, type={}", userId, type);

        List<TrainingSession> history = trainingService.getTrainingHistory(userId, type);

        List<TrainingSessionResponse> response = history.stream()
            .map(TrainingSessionResponse::from)
            .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * 훈련 통계 조회
     * GET /api/training/statistics
     */
    @GetMapping(value = "/statistics", produces = "application/json;charset=UTF-8")
    public ResponseEntity<List<TrainingStatisticsResponse>> getStatistics() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Training statistics requested: user={}", userId);

        List<TrainingStatistics> statistics = trainingService.getTrainingStatistics(userId);

        List<TrainingStatisticsResponse> response = statistics.stream()
            .map(TrainingStatisticsResponse::from)
            .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * 훈련 세션 상세 조회
     * GET /api/training/{sessionId}
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<TrainingSessionResponse> getSession(
        @PathVariable Long sessionId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Session details requested: user={}, session={}", userId, sessionId);

        TrainingSession session = trainingService.getTrainingHistory(userId, null).stream()
            .filter(s -> s.getId().equals(sessionId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Training session not found"));

        return ResponseEntity.ok(TrainingSessionResponse.from(session));
    }
}
