package com.lobai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Message Entity
 *
 * 채팅 메시지 엔티티
 */
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_persona", columnList = "persona_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MessageRole role;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    // 친밀도 점수 분석 필드 (Phase 1)
    @Column(name = "sentiment_score", precision = 5, scale = 2)
    private BigDecimal sentimentScore;  // -1.00 ~ 1.00 (감정 점수)

    @Column(name = "clarity_score", precision = 5, scale = 2)
    private BigDecimal clarityScore;  // 0.00 ~ 1.00 (명확성 점수)

    @Column(name = "context_score", precision = 5, scale = 2)
    private BigDecimal contextScore;  // 0.00 ~ 1.00 (맥락 유지 점수)

    @Column(name = "usage_score", precision = 5, scale = 2)
    private BigDecimal usageScore;  // 0.00 ~ 1.00 (AI 활용 태도 점수)

    @Column(name = "is_analyzed")
    @Builder.Default
    private Boolean isAnalyzed = false;  // 점수 분석 완료 여부

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Message Role Enum
     */
    public enum MessageRole {
        user,  // 사용자 메시지
        bot    // AI 봇 메시지
    }
}
