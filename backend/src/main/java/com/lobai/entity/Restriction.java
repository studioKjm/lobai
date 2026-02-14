package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "restrictions", indexes = {
    @Index(name = "idx_user_active", columnList = "user_id, is_active"),
    @Index(name = "idx_ends_at", columnList = "ends_at"),
    @Index(name = "idx_type", columnList = "restriction_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_type", nullable = false, length = 20)
    private RestrictionType restrictionType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "recovery_condition", columnDefinition = "TEXT")
    private String recoveryCondition;

    @Column(name = "lifted_at")
    private LocalDateTime liftedAt;

    @Column(name = "lifted_by", length = 50)
    private String liftedBy;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    /**
     * Check if restriction is currently active
     */
    public boolean isCurrentlyActive() {
        if (!isActive) return false;
        if (endsAt == null) return true;
        return LocalDateTime.now().isBefore(endsAt);
    }

    /**
     * Check if restriction has expired
     */
    public boolean hasExpired() {
        if (endsAt == null) return false;
        return LocalDateTime.now().isAfter(endsAt);
    }

    /**
     * Lift the restriction
     */
    public void lift(String liftedBy) {
        this.isActive = false;
        this.liftedAt = LocalDateTime.now();
        this.liftedBy = liftedBy;
    }

    public enum RestrictionType {
        WARNING,
        CHAT_LIMIT,
        FEATURE_BLOCK,
        FULL_BLOCK
    }
}
