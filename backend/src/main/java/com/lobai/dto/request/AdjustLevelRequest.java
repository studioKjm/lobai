package com.lobai.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdjustLevelRequest {

    @NotNull(message = "레벨은 필수입니다")
    @Min(value = 0, message = "레벨은 0 이상이어야 합니다")
    @Max(value = 100, message = "레벨은 100 이하여야 합니다")
    private Integer level;

    @Size(max = 500, message = "사유는 500자 이하여야 합니다")
    private String reason;
}
