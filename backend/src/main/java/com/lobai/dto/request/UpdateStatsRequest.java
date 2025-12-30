package com.lobai.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UpdateStatsRequest
 *
 * Stats 업데이트 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatsRequest {

    @NotNull(message = "액션 타입은 필수입니다")
    private ActionType action;

    /**
     * 사용자 액션 타입
     */
    public enum ActionType {
        feed,   // 먹이기 (+20 hunger)
        play,   // 놀기 (+15 happiness, -10 energy)
        sleep   // 재우기 (+30 energy, -5 hunger)
    }
}
