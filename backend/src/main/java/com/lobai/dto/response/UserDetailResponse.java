package com.lobai.dto.response;

import com.lobai.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UserDetailResponse
 *
 * 사용자 상세 정보 조회 응답 DTO (관리자용)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailResponse {

    private Long id;
    private String email;
    private String username;
    private Role role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    // Stats
    private Integer totalMessages;
    private Integer totalAttendanceDays;
    private Double affinityScore;
}
