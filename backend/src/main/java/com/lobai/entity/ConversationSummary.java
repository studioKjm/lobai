package com.lobai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_summaries", indexes = {
    @Index(name = "idx_cs_user_persona", columnList = "user_id, persona_id"),
    @Index(name = "idx_cs_user_created", columnList = "user_id, created_at DESC")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private Persona persona;

    @Column(name = "summary_text", columnDefinition = "TEXT", nullable = false)
    private String summaryText;

    @Column(name = "key_facts", columnDefinition = "TEXT")
    private String keyFacts;

    @Column(name = "message_start_id")
    private Long messageStartId;

    @Column(name = "message_end_id")
    private Long messageEndId;

    @Column(name = "message_count")
    @Builder.Default
    private Integer messageCount = 0;

    @Column(name = "token_count")
    @Builder.Default
    private Integer tokenCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_type")
    @Builder.Default
    private SummaryType summaryType = SummaryType.PERIODIC;

    @Column(name = "llm_provider", length = 50)
    private String llmProvider;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum SummaryType {
        SESSION, PERIODIC, MANUAL
    }
}
