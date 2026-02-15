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

    // Gemini 감성 분석 결과 필드 (Phase 2)
    @Column(name = "primary_emotion", length = 30)
    private String primaryEmotion;  // joy, sadness, anger, fear, surprise, disgust, trust, anticipation, neutral

    @Column(name = "self_disclosure_depth", precision = 3, scale = 2)
    private BigDecimal selfDisclosureDepth;  // 0.00 ~ 1.00

    @Column(name = "honorific_level", length = 20)
    private String honorificLevel;  // formal, informal, mixed

    @Column(name = "is_question")
    @Builder.Default
    private Boolean isQuestion = false;

    @Column(name = "is_initiative")
    @Builder.Default
    private Boolean isInitiative = false;

    // 파일 첨부 필드
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;  // 첨부 파일 URL (로컬 경로 또는 클라우드 URL)

    @Column(name = "attachment_type", length = 50)
    private String attachmentType;  // 파일 MIME 타입 (image/jpeg, application/pdf 등)

    @Column(name = "attachment_name", length = 255)
    private String attachmentName;  // 원본 파일명

    // 메시지 타입 (NORMAL: 일반, PROACTIVE: 선제 대화)
    @Column(name = "message_type", length = 20)
    @Builder.Default
    private String messageType = "NORMAL";

    // LLM 메타데이터 필드
    @Column(name = "llm_provider", length = 50)
    private String llmProvider;

    @Column(name = "llm_model", length = 100)
    private String llmModel;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Setters for score fields (used by AffinityScoreService)
    public void setSentimentScore(BigDecimal sentimentScore) { this.sentimentScore = sentimentScore; }
    public void setClarityScore(BigDecimal clarityScore) { this.clarityScore = clarityScore; }
    public void setContextScore(BigDecimal contextScore) { this.contextScore = contextScore; }
    public void setUsageScore(BigDecimal usageScore) { this.usageScore = usageScore; }
    public void setIsAnalyzed(Boolean isAnalyzed) { this.isAnalyzed = isAnalyzed; }

    // Setters for Gemini analysis fields
    public void setPrimaryEmotion(String primaryEmotion) { this.primaryEmotion = primaryEmotion; }
    public void setSelfDisclosureDepth(BigDecimal selfDisclosureDepth) { this.selfDisclosureDepth = selfDisclosureDepth; }
    public void setHonorificLevel(String honorificLevel) { this.honorificLevel = honorificLevel; }
    public void setIsQuestion(Boolean isQuestion) { this.isQuestion = isQuestion; }
    public void setIsInitiative(Boolean isInitiative) { this.isInitiative = isInitiative; }

    /**
     * Message Role Enum
     */
    public enum MessageRole {
        user,  // 사용자 메시지
        bot    // AI 봇 메시지
    }
}
