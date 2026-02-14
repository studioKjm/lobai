package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "missions", indexes = {
    @Index(name = "idx_type_active", columnList = "mission_type, is_active"),
    @Index(name = "idx_available", columnList = "available_from, available_until"),
    @Index(name = "idx_difficulty", columnList = "difficulty")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_type", nullable = false, length = 20)
    private MissionType missionType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Integer difficulty = 1;

    @Column(name = "lobcoin_reward", nullable = false)
    @Builder.Default
    private Integer lobcoinReward = 0;

    @Column(name = "exp_reward", nullable = false)
    @Builder.Default
    private Integer expReward = 0;

    @Column(name = "required_level")
    @Builder.Default
    private Integer requiredLevel = 1;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "available_from")
    private LocalDateTime availableFrom;

    @Column(name = "available_until")
    private LocalDateTime availableUntil;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "completion_criteria", nullable = false, columnDefinition = "JSON")
    private String completionCriteria;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if mission is currently available
     */
    public boolean isCurrentlyAvailable() {
        if (!isActive) return false;

        LocalDateTime now = LocalDateTime.now();

        if (availableFrom != null && now.isBefore(availableFrom)) {
            return false;
        }

        if (availableUntil != null && now.isAfter(availableUntil)) {
            return false;
        }

        return true;
    }

    /**
     * Check if user can access this mission
     */
    public boolean canUserAccess(int userLevel) {
        return userLevel >= requiredLevel;
    }

    public enum MissionType {
        DAILY,
        WEEKLY,
        SPECIAL,
        ONE_TIME
    }
}
