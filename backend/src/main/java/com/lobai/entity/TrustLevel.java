package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trust_levels", indexes = {
    @Index(name = "idx_score_range", columnList = "min_score, max_score")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrustLevel {

    @Id
    @Column(name = "level")
    private Integer level;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "min_score", nullable = false)
    private Integer minScore;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore;

    @Column(name = "daily_chat_limit")
    private Integer dailyChatLimit;

    @Column(name = "features_unlocked", columnDefinition = "JSON")
    private String featuresUnlocked;

    @Column(columnDefinition = "JSON")
    private String restrictions;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Check if this level is a penalty level (level 6-10)
     */
    public boolean isPenaltyLevel() {
        return level != null && level >= 6;
    }

    /**
     * Check if this level allows unlimited chat
     */
    public boolean hasUnlimitedChat() {
        return dailyChatLimit == null;
    }

    /**
     * Get the level for a given score
     */
    public boolean containsScore(int score) {
        return score >= minScore && score <= maxScore;
    }
}
