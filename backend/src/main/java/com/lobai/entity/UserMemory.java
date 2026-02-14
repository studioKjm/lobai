package com.lobai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_memories", indexes = {
    @Index(name = "idx_um_user_active", columnList = "user_id, is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "memory_key", length = 100, nullable = false)
    private String memoryKey;

    @Column(name = "memory_value", columnDefinition = "TEXT", nullable = false)
    private String memoryValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "memory_type", nullable = false)
    private MemoryType memoryType;

    @Column(name = "confidence_score", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal confidenceScore = BigDecimal.valueOf(0.50);

    @Column(name = "source_message_id")
    private Long sourceMessageId;

    @Column(name = "last_referenced_at")
    private LocalDateTime lastReferencedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum MemoryType {
        FACT, PROMISE, PREFERENCE, BEHAVIORAL_PATTERN
    }
}
