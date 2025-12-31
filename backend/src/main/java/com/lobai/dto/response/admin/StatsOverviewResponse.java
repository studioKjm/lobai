package com.lobai.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Admin Stats Overview Response
 *
 * 전체 통계 개요 (대시보드 메인 화면용)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsOverviewResponse {

    // 사용자 통계
    private Long totalUsers;              // 전체 사용자 수
    private Long activeUsersToday;        // 오늘 활성 사용자 (로그인한 사용자)
    private Long newUsersThisWeek;        // 이번 주 신규 가입자
    private Long newUsersThisMonth;       // 이번 달 신규 가입자

    // 메시지 통계
    private Long totalMessages;           // 전체 메시지 수
    private Long messagesToday;           // 오늘 메시지 수
    private Long messagesThisWeek;        // 이번 주 메시지 수
    private Long messagesThisMonth;       // 이번 달 메시지 수
    private Double avgMessagesPerUser;    // 사용자당 평균 메시지 수

    // Stats 평균값
    private Double avgHunger;             // 전체 사용자 평균 배고픔
    private Double avgEnergy;             // 전체 사용자 평균 에너지
    private Double avgHappiness;          // 전체 사용자 평균 행복도

    // 페르소나 통계
    private String mostPopularPersona;    // 가장 인기있는 페르소나 이름
    private Long mostPopularPersonaCount; // 가장 인기있는 페르소나 사용 횟수
}
