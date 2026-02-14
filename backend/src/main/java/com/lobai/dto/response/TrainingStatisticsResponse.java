package com.lobai.dto.response;

import com.lobai.entity.TrainingSession;
import com.lobai.entity.TrainingStatistics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingStatisticsResponse {
    private TrainingSession.TrainingType trainingType;
    private Integer totalSessions;
    private Integer completedSessions;
    private BigDecimal avgScore;
    private Integer bestScore;
    private Integer totalTimeSeconds;
    private Integer totalLobcoinEarned;
    private Integer totalLobcoinSpent;
    private Integer currentStreak;
    private LocalDate lastTrainingDate;

    public static TrainingStatisticsResponse from(TrainingStatistics stats) {
        return TrainingStatisticsResponse.builder()
            .trainingType(stats.getTrainingType())
            .totalSessions(stats.getTotalSessions())
            .completedSessions(stats.getCompletedSessions())
            .avgScore(stats.getAvgScore())
            .bestScore(stats.getBestScore())
            .totalTimeSeconds(stats.getTotalTimeSeconds())
            .totalLobcoinEarned(stats.getTotalLobcoinEarned())
            .totalLobcoinSpent(stats.getTotalLobcoinSpent())
            .currentStreak(stats.getCurrentStreak())
            .lastTrainingDate(stats.getLastTrainingDate())
            .build();
    }
}
