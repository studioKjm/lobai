package com.lobai.service;

import com.lobai.entity.LevelReward;
import com.lobai.entity.User;
import com.lobai.repository.LevelRewardRepository;
import com.lobai.repository.PartnerCouponRepository;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LevelRewardService {

    private final LevelRewardRepository rewardRepository;
    private final UserRepository userRepository;
    private final LobCoinService lobCoinService;
    private final PartnerCouponService couponService;

    // Reward configurations
    private static final Map<Integer, Integer> LEVEL_LOBCOIN_REWARDS = Map.of(
        2, 100,    // Level 2: 100 LobCoin
        3, 200,    // Level 3: 200 LobCoin
        4, 300,    // Level 4: 300 LobCoin
        5, 500     // Level 5: 500 LobCoin
    );

    /**
     * Claim level-up rewards
     */
    @Transactional
    public void claimLevelReward(Long userId, int level) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // Check if already claimed
        if (hasClaimedLobCoinReward(userId, level)) {
            log.info("User {} already claimed level {} rewards", userId, level);
            return;
        }

        // Give LobCoin reward
        Integer lobCoinReward = LEVEL_LOBCOIN_REWARDS.get(level);
        if (lobCoinReward != null) {
            lobCoinService.earnLobCoin(
                userId,
                lobCoinReward,
                "LEVEL_UP",
                String.format("레벨 %d 달성 보상", level)
            );

            // Record reward
            recordReward(userId, level, "LOBCOIN", String.format("{\"amount\": %d}", lobCoinReward));
            log.info("Level {} LobCoin reward ({}) claimed by user {}", level, lobCoinReward, userId);
        }

        // Level-specific rewards
        switch (level) {
            case 3:
                // Notion 1 month free (coupon ID 1 from migration)
                if (!hasClaimedCouponReward(userId, level)) {
                    recordReward(userId, level, "COUPON", "{\"couponId\": 1, \"name\": \"Notion Plus 1개월 무료\"}");
                    log.info("Level 3 Notion coupon reward claimed by user {}", userId);
                }
                break;

            case 4:
                // Netflix 1 month free (coupon ID 4 from migration)
                if (!hasClaimedCouponReward(userId, level)) {
                    recordReward(userId, level, "COUPON", "{\"couponId\": 4, \"name\": \"Netflix 1개월 무료\"}");
                    log.info("Level 4 Netflix coupon reward claimed by user {}", userId);
                }
                break;

            case 5:
                // Pro subscription 6 months free
                if (!hasClaimedSubscriptionReward(userId, level)) {
                    recordReward(userId, level, "SUBSCRIPTION_DISCOUNT",
                        "{\"tier\": \"pro\", \"months\": 6}");
                    log.info("Level 5 Pro subscription reward claimed by user {}", userId);
                }
                break;
        }
    }

    /**
     * Check if user has claimed LobCoin reward for a level
     */
    @Transactional(readOnly = true)
    public boolean hasClaimedLobCoinReward(Long userId, int level) {
        return rewardRepository.existsByUserIdAndLevelAndRewardType(userId, level, "LOBCOIN");
    }

    /**
     * Check if user has claimed coupon reward for a level
     */
    @Transactional(readOnly = true)
    public boolean hasClaimedCouponReward(Long userId, int level) {
        return rewardRepository.existsByUserIdAndLevelAndRewardType(userId, level, "COUPON");
    }

    /**
     * Check if user has claimed subscription reward for a level
     */
    @Transactional(readOnly = true)
    public boolean hasClaimedSubscriptionReward(Long userId, int level) {
        return rewardRepository.existsByUserIdAndLevelAndRewardType(userId, level, "SUBSCRIPTION_DISCOUNT");
    }

    /**
     * Get user's claimed rewards
     */
    @Transactional(readOnly = true)
    public Map<Integer, Map<String, Boolean>> getClaimedRewards(Long userId) {
        Map<Integer, Map<String, Boolean>> claimedRewards = new HashMap<>();

        for (int level = 1; level <= 5; level++) {
            Map<String, Boolean> levelRewards = new HashMap<>();
            levelRewards.put("lobcoin", hasClaimedLobCoinReward(userId, level));
            levelRewards.put("coupon", hasClaimedCouponReward(userId, level));
            levelRewards.put("subscription", hasClaimedSubscriptionReward(userId, level));
            claimedRewards.put(level, levelRewards);
        }

        return claimedRewards;
    }

    /**
     * Record a claimed reward
     */
    private void recordReward(Long userId, int level, String rewardType, String rewardData) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        LevelReward reward = LevelReward.builder()
            .user(user)
            .level(level)
            .rewardType(rewardType)
            .rewardData(rewardData)
            .build();

        rewardRepository.save(reward);
    }
}
