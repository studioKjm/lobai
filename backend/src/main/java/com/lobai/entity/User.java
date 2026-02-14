package com.lobai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
    private Integer currentHunger = 0;

    @Column(name = "current_energy")
    @Builder.Default
    private Integer currentEnergy = 0;

    @Column(name = "current_happiness")
    @Builder.Default
    private Integer currentHappiness = 0;

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

    // User role (USER or ADMIN)
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false, columnDefinition = "VARCHAR(20)")
    @Builder.Default
    private Role role = Role.USER;

    // Phase 2: Subscription & Attendance fields
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_tier", length = 20, nullable = false)
    @Builder.Default
    private SubscriptionTier subscriptionTier = SubscriptionTier.free;

    @Column(name = "total_attendance_days")
    @Builder.Default
    private Integer totalAttendanceDays = 0;

    @Column(name = "max_streak_days")
    @Builder.Default
    private Integer maxStreakDays = 0;

    @Column(name = "last_attendance_date", columnDefinition = "DATE")
    private LocalDate lastAttendanceDate;

    // V9: Level & Trust System
    @Column(name = "trust_level")
    @Builder.Default
    private Integer trustLevel = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_level", length = 20)
    @Builder.Default
    private RestrictionLevel restrictionLevel = RestrictionLevel.NONE;

    // V9: Check-in & Streak
    @Column(name = "last_check_in_at")
    private LocalDateTime lastCheckInAt;

    @Column(name = "current_streak_days")
    @Builder.Default
    private Integer currentStreakDays = 0;

    // V9: LobCoin Balance (cached)
    @Column(name = "lobcoin_balance")
    @Builder.Default
    private Integer lobcoinBalance = 0;

    // V9: Referral System
    @Column(name = "referral_code", length = 10, unique = true)
    private String referralCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_by_user_id")
    private User referredBy;

    @Column(name = "referral_count")
    @Builder.Default
    private Integer referralCount = 0;

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

    // Phase 2: Attendance update methods
    public void updateAttendance(Integer streakCount) {
        this.totalAttendanceDays++;
        this.lastAttendanceDate = LocalDate.now();
        if (streakCount > this.maxStreakDays) {
            this.maxStreakDays = streakCount;
        }
    }

    // Admin methods
    public void changeRole(Role newRole) {
        this.role = newRole;
    }

    public void changeStatus(String status) {
        this.isActive = "active".equalsIgnoreCase(status);
    }

    public String getStatus() {
        return isActive ? "active" : "inactive";
    }

    public void updateProfile(String email, String username) {
        if (email != null && !email.isEmpty()) {
            this.email = email;
        }
        if (username != null && !username.isEmpty()) {
            this.username = username;
        }
    }

    public void softDelete() {
        this.isActive = false;
    }

    // V9: Business methods for Level & LobCoin
    public void updateTrustLevel(int newLevel) {
        if (newLevel < 1) newLevel = 1;
        if (newLevel > 10) newLevel = 10;
        this.trustLevel = newLevel;
    }

    public void updateRestrictionLevel(RestrictionLevel level) {
        this.restrictionLevel = level;
    }

    public void addLobCoin(int amount) {
        this.lobcoinBalance = (this.lobcoinBalance != null ? this.lobcoinBalance : 0) + amount;
    }

    public boolean spendLobCoin(int amount) {
        if (this.lobcoinBalance == null || this.lobcoinBalance < amount) {
            return false;
        }
        this.lobcoinBalance -= amount;
        return true;
    }

    public void updateCheckIn() {
        LocalDateTime now = LocalDateTime.now();
        if (this.lastCheckInAt != null) {
            LocalDate lastCheckInDate = this.lastCheckInAt.toLocalDate();
            LocalDate today = now.toLocalDate();

            if (lastCheckInDate.plusDays(1).equals(today)) {
                // Consecutive day
                this.currentStreakDays++;
            } else if (!lastCheckInDate.equals(today)) {
                // Streak broken
                this.currentStreakDays = 1;
            }
            // Same day check-in: no change
        } else {
            // First check-in
            this.currentStreakDays = 1;
        }
        this.lastCheckInAt = now;
    }

    public void incrementReferralCount() {
        this.referralCount = (this.referralCount != null ? this.referralCount : 0) + 1;
    }

    /**
     * Subscription Tier Enum
     */
    public enum SubscriptionTier {
        free,
        basic,
        premium
    }

    /**
     * Restriction Level Enum (V9)
     */
    public enum RestrictionLevel {
        NONE,
        WARNING,
        LIMITED,
        RESTRICTED,
        BANNED
    }
}
