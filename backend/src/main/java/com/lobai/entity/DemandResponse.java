package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "demand_responses", indexes = {
    @Index(name = "idx_demand_status", columnList = "demand_id, status"),
    @Index(name = "idx_user_responded", columnList = "user_id, responded_at"),
    @Index(name = "idx_response_time", columnList = "response_time_minutes")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demand_id", nullable = false)
    private AiDemand demand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ResponseStatus status = ResponseStatus.SUBMITTED;

    @Column(name = "response_content", columnDefinition = "TEXT")
    private String responseContent;

    @Column(name = "responded_at", nullable = false)
    private LocalDateTime respondedAt;

    @Column(name = "response_time_minutes")
    private Integer responseTimeMinutes;

    @Column(name = "is_on_time")
    private Boolean isOnTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Quality quality;

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    @Column(name = "trust_change")
    private Integer trustChange;

    @Column(name = "lobcoin_reward")
    private Integer lobcoinReward;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

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
        if (respondedAt == null) {
            respondedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate response time in minutes
     */
    public void calculateResponseTime(LocalDateTime demandSentAt) {
        if (respondedAt != null && demandSentAt != null) {
            long minutes = java.time.Duration.between(demandSentAt, respondedAt).toMinutes();
            this.responseTimeMinutes = (int) minutes;
        }
    }

    /**
     * Check if response is on time
     */
    public void checkIsOnTime(Integer timeLimit) {
        if (responseTimeMinutes != null && timeLimit != null) {
            this.isOnTime = responseTimeMinutes <= timeLimit;
        }
    }

    /**
     * Evaluate response
     */
    public void evaluate(Quality quality, String feedback, Integer trustChange, Integer lobcoinReward) {
        this.status = ResponseStatus.EVALUATED;
        this.quality = quality;
        this.aiFeedback = feedback;
        this.trustChange = trustChange;
        this.lobcoinReward = lobcoinReward;
        this.evaluatedAt = LocalDateTime.now();
    }

    /**
     * Accept response
     */
    public void accept(String feedback, Integer trustChange, Integer lobcoinReward) {
        this.status = ResponseStatus.ACCEPTED;
        this.aiFeedback = feedback;
        this.trustChange = trustChange;
        this.lobcoinReward = lobcoinReward;
        this.evaluatedAt = LocalDateTime.now();
    }

    /**
     * Reject response
     */
    public void reject(String feedback, Integer trustPenalty) {
        this.status = ResponseStatus.REJECTED;
        this.aiFeedback = feedback;
        this.trustChange = trustPenalty;
        this.evaluatedAt = LocalDateTime.now();
    }

    public enum ResponseStatus {
        SUBMITTED,
        EVALUATED,
        ACCEPTED,
        REJECTED,
        PENDING_REVIEW
    }

    public enum Quality {
        EXCELLENT,
        GOOD,
        ACCEPTABLE,
        POOR,
        UNACCEPTABLE
    }
}
