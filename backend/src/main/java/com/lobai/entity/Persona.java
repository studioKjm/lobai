package com.lobai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Persona Entity
 *
 * AI 페르소나 엔티티 (5개: 친구, 상담사, 코치, 전문가, 유머)
 */
@Entity
@Table(name = "personas")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;  // 한글명 (예: "친구")

    @Column(name = "name_en", length = 50, nullable = false, unique = true)
    private String nameEn;  // 영문명 (예: "friend")

    @Column(name = "display_name", length = 100, nullable = false)
    private String displayName;  // UI 표시명 (예: "친구모드")

    @Column(name = "system_instruction", columnDefinition = "TEXT", nullable = false)
    private String systemInstruction;  // Gemini API 시스템 프롬프트

    @Column(name = "icon_emoji", length = 10)
    private String iconEmoji;  // 아이콘 이모지 (예: "👥")

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;  // 표시 순서

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;  // 활성화 여부

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
