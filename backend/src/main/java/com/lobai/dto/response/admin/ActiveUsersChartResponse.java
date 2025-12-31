package com.lobai.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Active Users Chart Response
 *
 * 활성 사용자 차트 데이터 (시계열)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveUsersChartResponse {

    private List<DataPoint> dailyActiveUsers;    // 일별 활성 사용자 수 (최근 30일)
    private List<DataPoint> newUserSignups;      // 일별 신규 가입자 수 (최근 30일)

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataPoint {
        private String date;    // yyyy-MM-dd 형식
        private Long count;     // 해당 날짜의 카운트
    }
}
