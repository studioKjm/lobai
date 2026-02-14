package com.lobai.dto.response;

import com.lobai.entity.TrainingSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSessionResponse {
    private Long id;
    private TrainingSession.TrainingType trainingType;
    private Integer difficultyLevel;
    private String problemText;
    private String userAnswer;
    private String correctAnswer;  // 정답 (문제 해결 유형에만 제공)
    private Integer score;
    private String evaluation;
    private String feedback;
    private Integer timeTakenSeconds;
    private Integer hintsUsed;
    private Integer lobcoinEarned;
    private Integer lobcoinSpent;
    private TrainingSession.SessionStatus status;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    public static TrainingSessionResponse from(TrainingSession session) {
        return TrainingSessionResponse.builder()
            .id(session.getId())
            .trainingType(session.getTrainingType())
            .difficultyLevel(session.getDifficultyLevel())
            .problemText(session.getProblemText())
            .userAnswer(session.getUserAnswer())
            .correctAnswer(session.getCorrectAnswer())  // 정답 포함
            .score(session.getScore())
            .evaluation(session.getEvaluation())
            .feedback(session.getFeedback())
            .timeTakenSeconds(session.getTimeTakenSeconds())
            .hintsUsed(session.getHintsUsed())
            .lobcoinEarned(session.getLobcoinEarned())
            .lobcoinSpent(session.getLobcoinSpent())
            .status(session.getStatus())
            .completedAt(session.getCompletedAt())
            .createdAt(session.getCreatedAt())
            .build();
    }
}
