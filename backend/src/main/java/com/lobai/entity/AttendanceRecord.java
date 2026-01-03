package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AttendanceRecord Entity
 * 사용자 출석 체크 기록
 */
@Entity
@Table(name = "attendance_records", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_date", columnNames = {"user_id", "check_in_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "check_in_date", nullable = false, columnDefinition = "DATE")
    private LocalDate checkInDate;

    @Column(name = "check_in_time", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime checkInTime;

    /**
     * 연속 출석 일수
     */
    @Column(name = "streak_count", nullable = false)
    @Builder.Default
    private Integer streakCount = 1;

    /**
     * 출석 보상 포인트
     */
    @Column(name = "reward_points")
    @Builder.Default
    private Integer rewardPoints = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 연속 출석에 따른 보상 포인트 계산
     * 1-6일: 10 포인트
     * 7-13일: 20 포인트
     * 14-29일: 30 포인트
     * 30일 이상: 50 포인트
     */
    public static int calculateRewardPoints(int streakCount) {
        if (streakCount >= 30) {
            return 50;
        } else if (streakCount >= 14) {
            return 30;
        } else if (streakCount >= 7) {
            return 20;
        } else {
            return 10;
        }
    }
}
