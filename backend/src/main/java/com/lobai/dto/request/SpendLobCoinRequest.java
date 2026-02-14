package com.lobai.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SpendLobCoinRequest {

    @NotNull(message = "사용량은 필수입니다")
    @Min(value = 1, message = "사용량은 1 이상이어야 합니다")
    private Integer amount;

    @NotBlank(message = "사용 경로는 필수입니다")
    @Size(max = 50, message = "사용 경로는 50자 이하여야 합니다")
    private String source;

    @Size(max = 255, message = "설명은 255자 이하여야 합니다")
    private String description;
}
