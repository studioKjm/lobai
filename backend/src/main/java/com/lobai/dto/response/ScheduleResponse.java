package com.lobai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponse {

    private Long id;
    private String title;
    private String description;
    private String startTime;  // ISO-8601 format
    private String endTime;    // ISO-8601 format
    private String type;
    private Boolean isCompleted;
    private String timezone;
    private Integer notifyBeforeMinutes;
    private String createdAt;  // ISO-8601 format
}
