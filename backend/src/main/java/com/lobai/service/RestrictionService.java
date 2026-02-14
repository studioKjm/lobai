package com.lobai.service;

import com.lobai.entity.Restriction;
import com.lobai.entity.User;
import com.lobai.repository.RestrictionRepository;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RestrictionService
 * 제재 관리 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RestrictionService {

    private final RestrictionRepository restrictionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Apply restriction to user
     */
    @Transactional
    public Restriction applyRestriction(
        Long userId,
        Restriction.RestrictionType type,
        String reason,
        LocalDateTime endsAt,
        String recoveryCondition
    ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // Check if similar restriction already exists
        if (restrictionRepository.hasActiveRestrictionOfType(userId, type)) {
            throw new IllegalStateException("이미 동일한 유형의 제재가 적용되어 있습니다");
        }

        Restriction restriction = Restriction.builder()
            .user(user)
            .restrictionType(type)
            .reason(reason)
            .endsAt(endsAt)
            .recoveryCondition(recoveryCondition)
            .build();

        Restriction saved = restrictionRepository.save(restriction);

        // Update user restriction level
        updateUserRestrictionLevel(user);

        // Send notification
        notificationService.sendRestrictionNotification(userId, reason);

        log.info("Restriction applied to user {}: {} until {}", userId, type, endsAt);
        return saved;
    }

    /**
     * Lift restriction
     */
    @Transactional
    public void liftRestriction(Long restrictionId, String liftedBy, String reason) {
        Restriction restriction = restrictionRepository.findById(restrictionId)
            .orElseThrow(() -> new IllegalArgumentException("제재를 찾을 수 없습니다"));

        if (!restriction.getIsActive()) {
            throw new IllegalStateException("이미 해제된 제재입니다");
        }

        restriction.lift(liftedBy);
        restrictionRepository.save(restriction);

        // Update user restriction level
        User user = restriction.getUser();
        updateUserRestrictionLevel(user);

        log.info("Restriction {} lifted for user {} by {} (Reason: {})",
            restrictionId, user.getId(), liftedBy, reason);
    }

    /**
     * Get active restrictions for user
     */
    @Transactional(readOnly = true)
    public List<Restriction> getActiveRestrictions(Long userId) {
        return restrictionRepository.findByUserIdAndIsActiveTrueOrderByStartedAtDesc(userId);
    }

    /**
     * Get all restrictions for user (including historical)
     */
    @Transactional(readOnly = true)
    public List<Restriction> getAllRestrictions(Long userId) {
        return restrictionRepository.findByUserIdOrderByStartedAtDesc(userId);
    }

    /**
     * Check if user has active restriction
     */
    @Transactional(readOnly = true)
    public boolean hasActiveRestriction(Long userId) {
        return restrictionRepository.hasActiveRestriction(userId);
    }

    /**
     * Check if user has specific restriction type
     */
    @Transactional(readOnly = true)
    public boolean hasRestrictionType(Long userId, Restriction.RestrictionType type) {
        return restrictionRepository.hasActiveRestrictionOfType(userId, type);
    }

    /**
     * Check and expire restrictions
     */
    @Transactional
    public void checkAndExpireRestrictions() {
        List<Restriction> expired = restrictionRepository.findExpiredActiveRestrictions(LocalDateTime.now());

        for (Restriction restriction : expired) {
            restriction.lift("SYSTEM");
            restrictionRepository.save(restriction);

            User user = restriction.getUser();
            updateUserRestrictionLevel(user);

            log.info("Auto-expired restriction {} for user {}", restriction.getId(), user.getId());
        }
    }

    /**
     * Apply warning to user
     */
    @Transactional
    public Restriction applyWarning(Long userId, String reason) {
        return applyRestriction(
            userId,
            Restriction.RestrictionType.WARNING,
            reason,
            LocalDateTime.now().plusDays(7),
            "경고 기간 동안 규칙 준수"
        );
    }

    /**
     * Apply chat limit restriction
     */
    @Transactional
    public Restriction applyChatLimit(Long userId, String reason, int days) {
        return applyRestriction(
            userId,
            Restriction.RestrictionType.CHAT_LIMIT,
            reason,
            LocalDateTime.now().plusDays(days),
            String.format("%d일 연속 출석 및 규칙 준수", days)
        );
    }

    /**
     * Apply full block
     */
    @Transactional
    public Restriction applyFullBlock(Long userId, String reason, int days) {
        return applyRestriction(
            userId,
            Restriction.RestrictionType.FULL_BLOCK,
            reason,
            LocalDateTime.now().plusDays(days),
            String.format("%d일 후 재심사", days)
        );
    }

    /**
     * Update user's restriction level based on active restrictions
     */
    private void updateUserRestrictionLevel(User user) {
        List<Restriction> activeRestrictions = restrictionRepository
            .findByUserIdAndIsActiveTrueOrderByStartedAtDesc(user.getId());

        if (activeRestrictions.isEmpty()) {
            user.updateRestrictionLevel(User.RestrictionLevel.NONE);
        } else {
            // Get the most severe restriction
            Restriction.RestrictionType mostSevere = activeRestrictions.stream()
                .map(Restriction::getRestrictionType)
                .max((t1, t2) -> Integer.compare(getSeverityLevel(t1), getSeverityLevel(t2)))
                .orElse(Restriction.RestrictionType.WARNING);

            user.updateRestrictionLevel(mapToUserRestrictionLevel(mostSevere));
        }

        userRepository.save(user);
    }

    /**
     * Get severity level for restriction type
     */
    private int getSeverityLevel(Restriction.RestrictionType type) {
        return switch (type) {
            case WARNING -> 1;
            case CHAT_LIMIT -> 2;
            case FEATURE_BLOCK -> 3;
            case FULL_BLOCK -> 4;
        };
    }

    /**
     * Map restriction type to user restriction level
     */
    private User.RestrictionLevel mapToUserRestrictionLevel(Restriction.RestrictionType type) {
        return switch (type) {
            case WARNING -> User.RestrictionLevel.WARNING;
            case CHAT_LIMIT -> User.RestrictionLevel.LIMITED;
            case FEATURE_BLOCK -> User.RestrictionLevel.RESTRICTED;
            case FULL_BLOCK -> User.RestrictionLevel.BANNED;
        };
    }

    /**
     * Check if user can chat based on restrictions
     */
    @Transactional(readOnly = true)
    public boolean canUserChat(Long userId) {
        if (hasRestrictionType(userId, Restriction.RestrictionType.FULL_BLOCK)) {
            return false;
        }
        return true;
    }

    /**
     * Get remaining chat count for user (if chat limit applied)
     */
    @Transactional(readOnly = true)
    public Integer getRemainingChatCount(Long userId) {
        if (!hasRestrictionType(userId, Restriction.RestrictionType.CHAT_LIMIT)) {
            return null; // No limit
        }

        // This would need to be integrated with message counting logic
        // For now, return a default limit
        return 5;
    }
}
