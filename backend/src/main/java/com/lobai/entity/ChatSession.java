package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_sessions", indexes = {
    @Index(name = "idx_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_is_active", columnList = "is_active"),
    @Index(name = "idx_ended_at", columnList = "ended_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private Persona persona;

    @Column(name = "session_title", length = 200)
    private String sessionTitle;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "message_count")
    @Builder.Default
    private Integer messageCount = 0;

    @Column(name = "total_tokens")
    @Builder.Default
    private Integer totalTokens = 0;

    @Column(name = "duration_minutes")
    @Builder.Default
    private Integer durationMinutes = 0;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        lastActivityAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Increment message count
     */
    public void incrementMessageCount() {
        this.messageCount++;
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * Add tokens
     */
    public void addTokens(int tokens) {
        this.totalTokens += tokens;
    }

    /**
     * End session
     */
    public void endSession() {
        this.isActive = false;
        this.endedAt = LocalDateTime.now();

        if (startedAt != null && endedAt != null) {
            long minutes = java.time.Duration.between(startedAt, endedAt).toMinutes();
            this.durationMinutes = (int) minutes;
        }
    }

    /**
     * Check if session has expired (no activity for 30 minutes)
     */
    public boolean hasExpired() {
        if (!isActive || lastActivityAt == null) return false;
        return LocalDateTime.now().minusMinutes(30).isAfter(lastActivityAt);
    }

    /**
     * Update last activity
     */
    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * Generate session title from first message
     */
    public void generateTitle(String firstMessage) {
        if (firstMessage != null && firstMessage.length() > 0) {
            this.sessionTitle = firstMessage.length() > 50
                ? firstMessage.substring(0, 50) + "..."
                : firstMessage;
        }
    }
}
