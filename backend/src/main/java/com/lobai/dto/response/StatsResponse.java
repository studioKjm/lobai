package com.lobai.dto.response;

import com.lobai.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * StatsResponse
 *
 * 사용자 Stats 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsResponse {

    private Integer hunger;    // 배고픔 (0-100)
    private Integer energy;    // 에너지 (0-100)
    private Integer happiness; // 행복도 (0-100)
    private Integer trust;     // 신뢰도 (0-100, from affinity score)

    /**
     * User Entity to StatsResponse
     */
    public static StatsResponse from(User user) {
        return StatsResponse.builder()
                .hunger(user.getCurrentHunger())
                .energy(user.getCurrentEnergy())
                .happiness(user.getCurrentHappiness())
                .build();
    }

    /**
     * User Entity to StatsResponse with trust score
     */
    public static StatsResponse from(User user, Integer trustScore) {
        return StatsResponse.builder()
                .hunger(user.getCurrentHunger())
                .energy(user.getCurrentEnergy())
                .happiness(user.getCurrentHappiness())
                .trust(trustScore)
                .build();
    }
}
