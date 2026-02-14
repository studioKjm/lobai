package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "level_history", indexes = {
    @Index(name = "idx_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_new_level", columnList = "new_level")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "previous_level", nullable = false)
    private Integer previousLevel;

    @Column(name = "new_level", nullable = false)
    private Integer newLevel;

    @Column(length = 255)
    private String reason;

    @Column(name = "changed_by", length = 50)
    private String changedBy;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Check if this is a level up
     */
    public boolean isLevelUp() {
        return newLevel > previousLevel;
    }

    /**
     * Check if this is a level down
     */
    public boolean isLevelDown() {
        return newLevel < previousLevel;
    }

    /**
     * Get the level change amount
     */
    public int getLevelChange() {
        return newLevel - previousLevel;
    }

    public enum ChangedBy {
        SYSTEM,
        ADMIN,
        AI
    }
}
