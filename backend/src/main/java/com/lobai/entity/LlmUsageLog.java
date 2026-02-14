package com.lobai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "llm_usage_logs", indexes = {
    @Index(name = "idx_lul_user", columnList = "user_id"),
    @Index(name = "idx_lul_provider", columnList = "provider_name, created_at")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider_name", length = 50, nullable = false)
    private String providerName;

    @Column(name = "model_name", length = 100, nullable = false)
    private String modelName;

    @Column(name = "task_type", length = 50, nullable = false)
    private String taskType;

    @Column(name = "prompt_tokens")
    @Builder.Default
    private Integer promptTokens = 0;

    @Column(name = "completion_tokens")
    @Builder.Default
    private Integer completionTokens = 0;

    @Column(name = "total_tokens")
    @Builder.Default
    private Integer totalTokens = 0;

    @Column(name = "estimated_cost_usd", precision = 10, scale = 6)
    @Builder.Default
    private BigDecimal estimatedCostUsd = BigDecimal.ZERO;

    @Column(name = "response_time_ms")
    @Builder.Default
    private Integer responseTimeMs = 0;

    @Column(name = "is_fallback")
    @Builder.Default
    private Boolean isFallback = false;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
