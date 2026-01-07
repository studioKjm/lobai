package com.lobai.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SetStatsRequest
 *
 * Stats 직접 설정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SetStatsRequest {

    @Min(0)
    @Max(100)
    private Integer hunger;

    @Min(0)
    @Max(100)
    private Integer energy;

    @Min(0)
    @Max(100)
    private Integer happiness;
}
