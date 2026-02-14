package com.lobai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UserResponse
 *
 * 사용자 정보 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private Integer currentHunger;
    private Integer currentEnergy;
    private Integer currentHappiness;
    private Long currentPersonaId;
    private Integer trustLevel;
}
