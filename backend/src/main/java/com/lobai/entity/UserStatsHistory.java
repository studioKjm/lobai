package com.lobai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UserStatsHistory Entity
 *
 * 사용자 스탯 히스토리 엔티티
 */
@Entity
@Table(name = "user_stats_history", indexes = {
    @Index(name = "idx_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_action_type", columnList = "action_type")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "hunger", nullable = false)
    private Integer hunger;

    @Column(name = "energy", nullable = false)
    private Integer energy;

    @Column(name = "happiness", nullable = false)
    private Integer happiness;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Action Type Enum
     */
    public enum ActionType {
        feed,   // 먹이기
        play,   // 놀기
        sleep,  // 재우기
        chat,   // 대화
        decay   // 자동 감소
    }
}
