package com.lobai.dto.request;

import com.lobai.entity.Schedule;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 1, max = 255, message = "제목은 1-255자 사이여야 합니다")
    private String title;

    private String description;

    @NotNull(message = "시작 시간은 필수입니다")
    private LocalDateTime startTime;

    @NotNull(message = "종료 시간은 필수입니다")
    private LocalDateTime endTime;

    @NotNull(message = "일정 유형은 필수입니다")
    private Schedule.ScheduleType type;

    private String timezone;

    @Min(value = 0, message = "알림 시간은 0 이상이어야 합니다")
    private Integer notifyBeforeMinutes;
}
