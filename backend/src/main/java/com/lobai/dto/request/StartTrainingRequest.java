package com.lobai.dto.request;

import com.lobai.entity.TrainingSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StartTrainingRequest {
    private TrainingSession.TrainingType trainingType;
    private Integer difficultyLevel; // 1-10
}
