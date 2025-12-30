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

    // Phase 2용 분석 필드
    @Column(name = "sentiment_score", precision = 5, scale = 2)
    private BigDecimal sentimentScore;  // -1.00 ~ 1.00

    @Column(name = "clarity_score", precision = 5, scale = 2)
    private BigDecimal clarityScore;  // 0.00 ~ 1.00

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
