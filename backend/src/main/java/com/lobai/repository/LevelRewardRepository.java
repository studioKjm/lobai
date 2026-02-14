package com.lobai.repository;

import com.lobai.entity.LevelReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelRewardRepository extends JpaRepository<LevelReward, Long> {

    /**
     * Find rewards by user ID
     */
    List<LevelReward> findByUserIdOrderByClaimedAtDesc(Long userId);

    /**
     * Find rewards by user ID and level
     */
    List<LevelReward> findByUserIdAndLevel(Long userId, Integer level);

    /**
     * Check if user has claimed a specific reward
     */
    boolean existsByUserIdAndLevelAndRewardType(Long userId, Integer level, String rewardType);

    /**
     * Find specific reward by user, level, and type
     */
    Optional<LevelReward> findByUserIdAndLevelAndRewardType(
        Long userId,
        Integer level,
        String rewardType
    );

    /**
     * Count rewards claimed by user
     */
    long countByUserId(Long userId);
}
