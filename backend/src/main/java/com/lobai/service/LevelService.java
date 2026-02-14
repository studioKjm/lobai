package com.lobai.service;

import com.lobai.entity.*;
import com.lobai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * LevelService
 * 레벨 관리 및 자동 계산 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LevelService {

    private final TrustLevelRepository trustLevelRepository;
    private final LevelHistoryRepository levelHistoryRepository;
    private final UserRepository userRepository;
    private final AffinityScoreRepository affinityScoreRepository;
    private final NotificationService notificationService;

    /**
     * Calculate and update user's trust level based on affinity score
     */
    @Transactional
    public void updateUserLevel(Long userId, String reason, String changedBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // Get current affinity score
        AffinityScore affinityScore = affinityScoreRepository.findByUserId(userId)
            .orElse(null);

        if (affinityScore == null) {
            log.warn("No affinity score found for user {}, skipping level update", userId);
            return;
        }

        int score = affinityScore.getTotalScore();

        // Find appropriate level for the score
        TrustLevel newLevel = trustLevelRepository.findByScore(score)
            .orElseThrow(() -> new IllegalStateException("레벨 정의를 찾을 수 없습니다: " + score));

        int currentLevel = user.getTrustLevel() != null ? user.getTrustLevel() : 1;

        if (currentLevel != newLevel.getLevel()) {
            // Level changed
            recordLevelChange(user, currentLevel, newLevel.getLevel(), reason, changedBy);
            user.updateTrustLevel(newLevel.getLevel());
            userRepository.save(user);

            // Send notification
            notificationService.sendLevelChangeNotification(userId, currentLevel, newLevel.getLevel());

            log.info("User {} level changed: {} -> {} (Score: {}, Reason: {})",
                userId, currentLevel, newLevel.getLevel(), score, reason);
        }
    }

    /**
     * Manually adjust user level (Admin function)
     */
    @Transactional
    public void adjustLevel(Long userId, int newLevel, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // Validate level range
        if (newLevel < 1 || newLevel > 10) {
            throw new IllegalArgumentException("레벨은 1-10 사이여야 합니다");
        }

        TrustLevel trustLevel = trustLevelRepository.findById(newLevel)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 레벨입니다"));

        int currentLevel = user.getTrustLevel() != null ? user.getTrustLevel() : 1;

        if (currentLevel != newLevel) {
            recordLevelChange(user, currentLevel, newLevel, reason, "ADMIN");
            user.updateTrustLevel(newLevel);
            userRepository.save(user);

            notificationService.sendLevelChangeNotification(userId, currentLevel, newLevel);

            log.info("Admin adjusted user {} level: {} -> {} (Reason: {})",
                userId, currentLevel, newLevel, reason);
        }
    }

    /**
     * Get user's current level info
     */
    @Transactional(readOnly = true)
    public TrustLevel getUserLevel(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        int levelNumber = user.getTrustLevel() != null ? user.getTrustLevel() : 1;

        return trustLevelRepository.findById(levelNumber)
            .orElseThrow(() -> new IllegalStateException("레벨 정의를 찾을 수 없습니다"));
    }

    /**
     * Get level history for a user
     */
    @Transactional(readOnly = true)
    public List<LevelHistory> getLevelHistory(Long userId) {
        return levelHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get all level definitions
     */
    @Transactional(readOnly = true)
    public List<TrustLevel> getAllLevels() {
        return trustLevelRepository.findAll();
    }

    /**
     * Get active levels (1-5)
     */
    @Transactional(readOnly = true)
    public List<TrustLevel> getActiveLevels() {
        return trustLevelRepository.findActiveLevels();
    }

    /**
     * Get penalty levels (6-10)
     */
    @Transactional(readOnly = true)
    public List<TrustLevel> getPenaltyLevels() {
        return trustLevelRepository.findPenaltyLevels();
    }

    /**
     * Check if user can perform action based on level
     */
    @Transactional(readOnly = true)
    public boolean canUserPerformAction(Long userId, String action) {
        TrustLevel level = getUserLevel(userId);

        // Parse features_unlocked JSON and check if action is included
        // This is a simplified version - in production, parse JSON properly
        String features = level.getFeaturesUnlocked();
        return features != null && features.contains(action);
    }

    /**
     * Get daily chat limit for user
     */
    @Transactional(readOnly = true)
    public Integer getDailyChatLimit(Long userId) {
        TrustLevel level = getUserLevel(userId);
        return level.getDailyChatLimit(); // null means unlimited
    }

    /**
     * Record level change in history
     */
    private void recordLevelChange(User user, int previousLevel, int newLevel, String reason, String changedBy) {
        LevelHistory history = LevelHistory.builder()
            .user(user)
            .previousLevel(previousLevel)
            .newLevel(newLevel)
            .reason(reason)
            .changedBy(changedBy)
            .build();

        levelHistoryRepository.save(history);
    }

    /**
     * Initialize user level (for new users)
     */
    @Transactional
    public void initializeUserLevel(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        if (user.getTrustLevel() == null) {
            user.updateTrustLevel(1);
            userRepository.save(user);

            recordLevelChange(user, 0, 1, "Initial level assignment", "SYSTEM");

            log.info("Initialized level for user {}: Level 1", userId);
        }
    }
}
