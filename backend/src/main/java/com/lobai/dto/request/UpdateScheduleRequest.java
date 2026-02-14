package com.lobai.dto.request;

import com.lobai.entity.Schedule;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScheduleRequest {

    @Size(min = 1, max = 255, message = "제목은 1-255자 사이여야 합니다")
    private String title;

    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Schedule.ScheduleType type;

    private Boolean isCompleted;

    @Min(value = 0, message = "알림 시간은 0 이상이어야 합니다")
    private Integer notifyBeforeMinutes;
}
