package com.lobai.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Message Statistics Response
 *
 * 메시지 통계
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageStatsResponse {

    // 전체 통계
    private Long totalMessages;                  // 전체 메시지 수
    private Long userMessages;                   // 사용자 메시지 수
    private Long botMessages;                    // 봇 메시지 수
    private Double avgMessagesPerUser;           // 사용자당 평균 메시지 수
    private Double avgMessagesPerDay;            // 일평균 메시지 수

    // 시계열 데이터
    private List<DailyMessageCount> dailyMessages;  // 일별 메시지 수 (최근 30일)

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyMessageCount {
        private String date;          // yyyy-MM-dd
        private Long userMessages;    // 사용자 메시지 수
        private Long botMessages;     // 봇 메시지 수
        private Long total;           // 전체 메시지 수
    }
}
