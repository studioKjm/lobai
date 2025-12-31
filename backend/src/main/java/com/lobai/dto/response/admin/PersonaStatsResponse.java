package com.lobai.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Persona Statistics Response
 *
 * 페르소나별 사용 통계
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaStatsResponse {

    private List<PersonaUsageStat> personaUsage;  // 페르소나별 사용 통계

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PersonaUsageStat {
        private Long personaId;            // 페르소나 ID
        private String personaName;        // 페르소나 이름
        private String displayName;        // 표시명
        private String iconEmoji;          // 아이콘 이모지
        private Long messageCount;         // 메시지 수
        private Long currentUserCount;     // 현재 이 페르소나를 사용 중인 사용자 수
        private Double usagePercentage;    // 전체 메시지 중 비율 (0-100)
    }
}
