package com.lobai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lobai.config.GeminiConfig;
import com.lobai.entity.TrainingProblem;
import com.lobai.entity.TrainingSession;
import com.lobai.entity.TrainingStatistics;
import com.lobai.entity.User;
import com.lobai.repository.TrainingProblemRepository;
import com.lobai.repository.TrainingSessionRepository;
import com.lobai.repository.TrainingStatisticsRepository;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingSessionRepository sessionRepository;
    private final TrainingProblemRepository problemRepository;
    private final TrainingStatisticsRepository statisticsRepository;
    private final UserRepository userRepository;
    private final LobCoinService lobCoinService;
    private final LevelService levelService;
    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // LobCoin 비용
    private static final int HINT_COST = 5; // 힌트 1개당 5 LobCoin

    /**
     * 훈련 세션 시작
     * - 기존 문제 사용 또는 AI로 동적 생성
     */
    @Transactional
    public TrainingSession startTraining(Long userId, TrainingSession.TrainingType type, Integer difficultyLevel) {
        log.info("Starting training session for user {}: type={}, difficulty={}",
            userId, type, difficultyLevel);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 1. 문제 선택 (데이터베이스에서 가져오거나 AI 생성)
        String problemText;
        Long problemId = null;

        Optional<TrainingProblem> existingProblem = selectProblem(type, difficultyLevel);

        if (existingProblem.isPresent()) {
            // 기존 문제 사용
            TrainingProblem problem = existingProblem.get();
            problemText = problem.getProblemText();
            problemId = problem.getId();

            // 사용 횟수 증가
            problem.incrementUsage();
            problemRepository.save(problem);

            log.info("Using existing problem: id={}", problemId);
        } else {
            // AI로 문제 생성
            problemText = generateProblemWithAI(type, difficultyLevel);
            log.info("Generated new problem with AI");
        }

        // 2. 훈련 세션 생성
        TrainingSession session = TrainingSession.builder()
            .user(user)
            .trainingType(type)
            .difficultyLevel(difficultyLevel)
            .problemId(problemId)
            .problemText(problemText)
            .status(TrainingSession.SessionStatus.IN_PROGRESS)
            .build();

        return sessionRepository.save(session);
    }

    /**
     * 힌트 요청
     * - LobCoin 5개 차감
     * - AI가 힌트 생성
     */
    @Transactional
    public String requestHint(Long userId, Long sessionId) {
        log.info("Hint requested for session {} by user {}", sessionId, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 1. 세션 조회
        TrainingSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Training session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized access to training session");
        }

        if (session.getStatus() != TrainingSession.SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Training session is not in progress");
        }

        // 2. LobCoin 차감
        try {
            lobCoinService.spendLobCoin(user.getId(), HINT_COST, "TRAINING_HINT",
                String.format("훈련 힌트 (%s)", session.getTrainingType()));
        } catch (Exception e) {
            throw new IllegalStateException("Not enough LobCoin for hint. Required: " + HINT_COST);
        }

        // 3. 힌트 생성 (AI 또는 사전 정의)
        String hint;
        if (session.getProblemId() != null) {
            // 사전 정의된 힌트 사용
            hint = getPredefinedHint(session);
        } else {
            // AI가 힌트 생성
            hint = generateHintWithAI(session);
        }

        // 4. 힌트 사용 횟수 증가
        session.setHintsUsed(session.getHintsUsed() + 1);
        session.setLobcoinSpent(session.getLobcoinSpent() + HINT_COST);
        sessionRepository.save(session);

        log.info("Hint generated for session {}: {} LobCoin spent", sessionId, HINT_COST);
        return hint;
    }

    /**
     * 답변 제출 및 평가
     * - AI가 답변 평가
     * - 점수 계산
     * - LobCoin 지급
     */
    @Transactional
    public TrainingSession submitAnswer(Long userId, Long sessionId, String answer, Integer timeTaken) {
        log.info("Answer submitted for session {} by user {}", sessionId, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 1. 세션 조회
        TrainingSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Training session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized access to training session");
        }

        if (session.getStatus() != TrainingSession.SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Training session is not in progress");
        }

        // 2. 답변 저장
        session.setUserAnswer(answer);
        session.setTimeTakenSeconds(timeTaken);

        // 3. 답변 평가 (MEMORY 타입은 정확한 매칭, 나머지는 AI 평가)
        Map<String, Object> evaluationResult;
        if (session.getTrainingType() == TrainingSession.TrainingType.MEMORY) {
            evaluationResult = evaluateMemoryAnswer(session);
        } else {
            evaluationResult = evaluateAnswerWithAI(session);
        }

        Integer score = (Integer) evaluationResult.get("score");
        String evaluation = (String) evaluationResult.get("evaluation");
        String feedback = (String) evaluationResult.get("feedback");

        session.setScore(score);
        session.setEvaluation(evaluation);
        session.setFeedback(feedback);

        // 3.5. 정답 저장 (문제 해결 유형인 경우, 문제에서 가져오기)
        if (session.getProblemId() != null) {
            problemRepository.findById(session.getProblemId())
                .ifPresent(problem -> {
                    if (problem.getCorrectAnswer() != null && !problem.getCorrectAnswer().isEmpty()) {
                        session.setCorrectAnswer(problem.getCorrectAnswer());
                    }
                });
        }

        // 4. 세션 완료 처리
        session.complete();

        // 5. 보상 계산 및 LobCoin 지급
        session.calculateReward();

        if (session.getLobcoinEarned() > 0) {
            lobCoinService.earnLobCoin(
                user.getId(),
                session.getLobcoinEarned(),
                "TRAINING",
                String.format("훈련 완료 (%s, 점수: %d)", session.getTrainingType(), score)
            );
        }

        // 훈련 완료 XP 지급 (점수 기반: score/10, 최소 5 최대 15)
        int trainingXp = Math.max(5, Math.min(15, score / 10));
        try {
            levelService.addExperience(user.getId(), trainingXp,
                String.format("훈련 완료 (%s)", session.getTrainingType()));
        } catch (Exception e) {
            log.warn("Training XP reward failed: {}", e.getMessage());
        }

        sessionRepository.save(session);

        // 6. 통계 업데이트
        updateStatistics(user, session);

        // 7. 문제 평균 점수 업데이트 (기존 문제 사용 시)
        if (session.getProblemId() != null) {
            problemRepository.findById(session.getProblemId()).ifPresent(problem -> {
                problem.updateAvgScore(score);
                problemRepository.save(problem);
            });
        }

        log.info("Training session {} completed: score={}, earned={} LobCoin",
            sessionId, score, session.getLobcoinEarned());

        return session;
    }

    /**
     * 훈련 세션 중단
     */
    @Transactional
    public void abandonSession(Long userId, Long sessionId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        TrainingSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Training session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to training session");
        }

        session.abandon();
        sessionRepository.save(session);

        // 통계 업데이트 (완료되지 않은 세션으로 기록)
        updateStatistics(user, session);

        log.info("Training session {} abandoned by user {}", sessionId, userId);
    }

    /**
     * 사용자의 훈련 기록 조회
     */
    public List<TrainingSession> getTrainingHistory(Long userId, TrainingSession.TrainingType type) {
        if (type != null) {
            return sessionRepository.findByUserIdAndTrainingTypeOrderByCreatedAtDesc(userId, type);
        }
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 사용자의 훈련 통계 조회
     */
    public List<TrainingStatistics> getTrainingStatistics(Long userId) {
        return statisticsRepository.findByUserIdOrderByTrainingTypeAsc(userId);
    }

    /**
     * 문제 선택 (가장 적게 사용된 문제 우선)
     */
    private Optional<TrainingProblem> selectProblem(TrainingSession.TrainingType type, Integer difficultyLevel) {
        List<TrainingProblem> problems = problemRepository.findLeastUsedProblems(type, difficultyLevel);
        return problems.isEmpty() ? Optional.empty() : Optional.of(problems.get(0));
    }

    /**
     * AI로 문제 생성
     */
    private String generateProblemWithAI(TrainingSession.TrainingType type, Integer difficultyLevel) {
        try {
            String prompt = buildProblemGenerationPrompt(type, difficultyLevel);
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("Failed to generate problem with AI", e);
            return getFallbackProblem(type, difficultyLevel);
        }
    }

    /**
     * 사전 정의된 힌트 가져오기
     */
    private String getPredefinedHint(TrainingSession session) {
        return problemRepository.findById(session.getProblemId())
            .map(problem -> {
                try {
                    String hintsJson = problem.getHints();
                    if (hintsJson != null) {
                        JsonNode hintsArray = objectMapper.readTree(hintsJson);
                        int hintIndex = Math.min(session.getHintsUsed(), hintsArray.size() - 1);
                        return hintsArray.get(hintIndex).asText();
                    }
                } catch (Exception e) {
                    log.error("Failed to parse hints", e);
                }
                return "힌트가 준비되지 않았습니다. 다시 시도해보세요!";
            })
            .orElse("힌트를 찾을 수 없습니다.");
    }

    /**
     * AI로 힌트 생성
     */
    private String generateHintWithAI(TrainingSession session) {
        try {
            String prompt = String.format(
                "다음 문제에 대한 힌트를 1-2문장으로 제공하세요. 너무 직접적인 답은 피하고, 사고의 방향을 제시하세요.\n\n" +
                "문제: %s\n\n" +
                "이미 제공된 힌트 수: %d",
                session.getProblemText(),
                session.getHintsUsed()
            );
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("Failed to generate hint with AI", e);
            return "문제를 다시 읽고 핵심 키워드에 집중해보세요.";
        }
    }

    /**
     * MEMORY 타입 답변 평가 (정확한 매칭)
     */
    private Map<String, Object> evaluateMemoryAnswer(TrainingSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 정답 가져오기 (DB 또는 problemText에서 추출)
            String correctAnswer = null;

            if (session.getProblemId() != null) {
                // DB에서 정답 가져오기
                Optional<TrainingProblem> problem = problemRepository.findById(session.getProblemId());
                if (problem.isPresent()) {
                    correctAnswer = problem.get().getCorrectAnswer();
                }
            }

            // 정답이 없으면 problemText JSON에서 추출
            if (correctAnswer == null || correctAnswer.isEmpty()) {
                try {
                    JsonNode problemJson = objectMapper.readTree(session.getProblemText());
                    JsonNode dataNode = problemJson.get("data");
                    if (dataNode != null && dataNode.isArray()) {
                        List<String> correctItems = new ArrayList<>();
                        for (JsonNode item : dataNode) {
                            correctItems.add(item.asText());
                        }
                        correctAnswer = String.join(",", correctItems);
                    }
                } catch (Exception e) {
                    log.error("Failed to extract correct answer from problemText", e);
                }
            }

            if (correctAnswer == null || correctAnswer.isEmpty()) {
                result.put("score", 0);
                result.put("evaluation", "정답을 확인할 수 없습니다.");
                result.put("feedback", "시스템 오류가 발생했습니다. 관리자에게 문의하세요.");
                return result;
            }

            // 2. 사용자 답변 정규화 (공백 제거, 소문자 변환)
            String normalizedAnswer = session.getUserAnswer().trim().replaceAll("\\s+", "");
            String normalizedCorrect = correctAnswer.trim().replaceAll("\\s+", "");

            // 3. 정확도 계산
            String[] userItems = normalizedAnswer.split(",");
            String[] correctItems = normalizedCorrect.split(",");

            int totalItems = correctItems.length;
            int correctCount = 0;
            int orderCorrect = 0;

            // 순서와 내용 모두 확인
            for (int i = 0; i < Math.min(userItems.length, correctItems.length); i++) {
                if (userItems[i].equalsIgnoreCase(correctItems[i])) {
                    correctCount++;
                    orderCorrect++;
                }
            }

            // 순서 무시하고 내용만 확인
            Set<String> correctSet = new HashSet<>(Arrays.asList(correctItems));
            Set<String> userSet = new HashSet<>(Arrays.asList(userItems));
            int contentMatches = 0;
            for (String item : userItems) {
                if (correctSet.contains(item.toLowerCase())) {
                    contentMatches++;
                }
            }

            // 4. 점수 계산
            // - 순서 + 내용 모두 정확: 100점
            // - 순서는 틀렸지만 내용 정확: 70-90점
            // - 일부만 정확: 비율에 따라 0-70점
            int score;
            String evaluation;
            String feedback;

            if (orderCorrect == totalItems && userItems.length == totalItems) {
                // 완벽
                score = 100;
                evaluation = "완벽합니다! 모든 항목을 정확하게 기억했어요.";
                feedback = String.format("정답: %s", correctAnswer);
            } else if (contentMatches == totalItems && userItems.length == totalItems) {
                // 내용은 맞지만 순서가 틀림
                score = 70 + (int)((double)orderCorrect / totalItems * 20);
                evaluation = "내용은 모두 기억했지만 순서가 일부 틀렸습니다.";
                feedback = String.format("정답 순서: %s\n제출한 답변: %s",
                    correctAnswer, session.getUserAnswer());
            } else if (correctCount > 0) {
                // 일부만 정확
                score = (int)((double)correctCount / totalItems * 70);
                evaluation = String.format("%d개 중 %d개를 정확하게 기억했습니다.",
                    totalItems, correctCount);
                feedback = String.format("정답: %s\n제출한 답변: %s\n힌트: 순서와 내용 모두 정확해야 합니다.",
                    correctAnswer, session.getUserAnswer());
            } else {
                // 전부 틀림
                score = 0;
                evaluation = "아쉽게도 정확하게 기억하지 못했습니다.";
                feedback = String.format("정답: %s\n다음에는 더 집중해서 기억해보세요!", correctAnswer);
            }

            result.put("score", score);
            result.put("evaluation", evaluation);
            result.put("feedback", feedback);

            log.info("Memory evaluation: correct={}/{}, score={}", correctCount, totalItems, score);

        } catch (Exception e) {
            log.error("Failed to evaluate memory answer", e);
            result.put("score", 0);
            result.put("evaluation", "평가 중 오류가 발생했습니다.");
            result.put("feedback", "다시 시도해주세요.");
        }

        return result;
    }

    /**
     * AI로 답변 평가
     */
    private Map<String, Object> evaluateAnswerWithAI(TrainingSession session) {
        try {
            String prompt = buildEvaluationPrompt(session);
            String response = callGeminiAPI(prompt);

            // JSON 파싱
            return parseEvaluationResponse(response);

        } catch (Exception e) {
            log.error("Failed to evaluate answer with AI", e);
            // 기본 점수 반환
            Map<String, Object> result = new HashMap<>();
            result.put("score", 50);
            result.put("evaluation", "답변을 제출해주셔서 감사합니다.");
            result.put("feedback", "평가 중 오류가 발생했습니다. 다시 시도해주세요.");
            return result;
        }
    }

    /**
     * 문제 생성 프롬프트 구성
     */
    private String buildProblemGenerationPrompt(TrainingSession.TrainingType type, Integer difficultyLevel) {
        String typeDescription = switch (type) {
            case CRITICAL_THINKING -> "비판적 사고: AI의 답변이나 정보의 오류를 찾아내는 문제";
            case PROBLEM_SOLVING -> "문제 해결: AI 없이 논리적으로 해결해야 하는 퍼즐이나 문제";
            case CREATIVE_THINKING -> "창의적 사고: AI가 생성하기 어려운 독창적인 아이디어를 요구하는 문제";
            case SELF_REFLECTION -> "자기 성찰: AI 의존도를 인식하고 개선 방법을 찾는 문제";
            case REASONING -> "추리문제: 단서를 분석하고 논리적으로 추론하는 문제";
            case MEMORY -> "암기력: 주어진 정보를 기억하고 정확하게 재생하는 문제";
        };

        return String.format(
            """
            당신은 AI 독립성 훈련 시스템입니다. 사용자가 AI에 과도하게 의존하지 않고 독립적으로 사고하도록 돕는 문제를 생성하세요.

            훈련 유형: %s
            난이도: %d/10

            다음 조건을 만족하는 문제를 생성하세요:
            1. 명확하고 구체적인 문제 제시
            2. 난이도에 맞는 복잡도
            3. AI가 쉽게 답하기 어려운 내용
            4. 인간의 경험과 사고를 요구
            5. 한국어로 작성

            문제만 제시하고, 답이나 힌트는 포함하지 마세요.
            """,
            typeDescription,
            difficultyLevel
        );
    }

    /**
     * 평가 프롬프트 구성
     */
    private String buildEvaluationPrompt(TrainingSession session) {
        return String.format(
            """
            답변을 평가하고 JSON으로 응답하세요.

            문제: %s
            답변: %s

            JSON 형식 (evaluation은 30자 이내, feedback은 80자 이내):
            {
              "score": 85,
              "evaluation": "간결한 평가",
              "feedback": "간결한 피드백"
            }
            """,
            session.getProblemText(),
            session.getUserAnswer()
        );
    }

    /**
     * Gemini API 호출
     */
    private String callGeminiAPI(String prompt) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();

        // Contents
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        content.put("role", "user");
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        content.put("parts", List.of(part));
        contents.add(content);
        requestBody.put("contents", contents);

        // Generation config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 4096); // Increased for longer evaluations
        requestBody.put("generationConfig", generationConfig);

        // HTTP Request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String url = geminiConfig.getGenerateContentUrl();
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.POST, entity, String.class
        );

        // Response 파싱
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        return jsonNode
            .path("candidates")
            .get(0)
            .path("content")
            .path("parts")
            .get(0)
            .path("text")
            .asText();
    }

    /**
     * 평가 응답 파싱
     */
    private Map<String, Object> parseEvaluationResponse(String response) throws Exception {
        // JSON 부분만 추출 (코드 블록 제거)
        String jsonStr = response.trim();

        // 코드 블록 마커 제거 (```json ... ``` 또는 ``` ... ```)
        jsonStr = jsonStr.replaceAll("^```(?:json)?\\s*", "");  // 시작 부분
        jsonStr = jsonStr.replaceAll("\\s*```$", "");           // 끝 부분
        jsonStr = jsonStr.trim();

        // { }로 감싸진 JSON 추출 (안전장치)
        int start = jsonStr.indexOf("{");
        int end = jsonStr.lastIndexOf("}");
        if (start >= 0 && end >= 0 && start < end) {
            jsonStr = jsonStr.substring(start, end + 1);
        }

        log.debug("Parsing JSON: {}", jsonStr);

        JsonNode jsonNode = objectMapper.readTree(jsonStr);

        Map<String, Object> result = new HashMap<>();
        result.put("score", jsonNode.path("score").asInt(50));
        result.put("evaluation", jsonNode.path("evaluation").asText("평가 완료"));
        result.put("feedback", jsonNode.path("feedback").asText(""));

        return result;
    }

    /**
     * 통계 업데이트
     */
    private void updateStatistics(User user, TrainingSession session) {
        TrainingStatistics stats = statisticsRepository
            .findByUserIdAndTrainingType(user.getId(), session.getTrainingType())
            .orElseGet(() -> {
                TrainingStatistics newStats = TrainingStatistics.builder()
                    .user(user)
                    .trainingType(session.getTrainingType())
                    .build();
                return statisticsRepository.save(newStats);
            });

        stats.updateWithSession(session);
        statisticsRepository.save(stats);
    }

    /**
     * 대체 문제 (AI 실패 시)
     */
    private String getFallbackProblem(TrainingSession.TrainingType type, Integer difficultyLevel) {
        return switch (type) {
            case CRITICAL_THINKING -> "AI가 '모든 고양이는 검은색이다'라고 주장합니다. 이 주장의 문제점을 찾아보세요.";
            case PROBLEM_SOLVING -> "8개의 동일한 공이 있습니다. 그중 1개만 무게가 다릅니다. 저울을 2번만 사용하여 다른 공을 찾는 방법은?";
            case CREATIVE_THINKING -> "AI가 절대 쓸 수 없는 시를 작성해보세요. (최소 4줄)";
            case SELF_REFLECTION -> "오늘 AI에게 의존한 순간 3가지를 적고, AI 없이 해결할 수 있었던 방법을 생각해보세요.";
            case REASONING -> "A, B, C 세 사람이 있습니다. A는 'B는 거짓말쟁이다'라고 말했고, B는 'C는 거짓말쟁이다'라고 말했습니다. C는 'A와 B 모두 거짓말쟁이다'라고 말했습니다. 누가 진실을 말하고 있을까요?";
            case MEMORY -> "다음 숫자를 기억하세요: 7, 3, 9, 1, 5, 8. 순서대로 입력하세요.";
        };
    }
}
