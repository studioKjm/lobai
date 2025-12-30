package com.lobai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Entity
 *
 * 사용자 엔티티 (인증 정보 + Stats)
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_oauth", columnList = "oauth_provider, oauth_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    // Stats (0-100 range)
    @Column(name = "current_hunger")
    @Builder.Default
    private Integer currentHunger = 80;

    @Column(name = "current_energy")
    @Builder.Default
    private Integer currentEnergy = 90;

    @Column(name = "current_happiness")
    @Builder.Default
    private Integer currentHappiness = 70;

    // Persona relationship (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_persona_id")
    private Persona currentPersona;

    // OAuth fields (Phase 2)
    @Column(name = "oauth_provider", length = 50)
    private String oauthProvider;  // "google", "kakao"

    @Column(name = "oauth_id", length = 255)
    private String oauthId;

    // Metadata
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business methods
    public void updateStats(Integer hunger, Integer energy, Integer happiness) {
        if (hunger != null) this.currentHunger = validateStat(hunger);
        if (energy != null) this.currentEnergy = validateStat(energy);
        if (happiness != null) this.currentHappiness = validateStat(happiness);
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void changePersona(Persona persona) {
        this.currentPersona = persona;
    }

    private Integer validateStat(Integer value) {
        if (value < 0) return 0;
        if (value > 100) return 100;
        return value;
    }
}
