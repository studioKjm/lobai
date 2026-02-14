package com.lobai.service;

import com.lobai.dto.request.SetStatsRequest;
import com.lobai.dto.request.UpdateStatsRequest;
import com.lobai.dto.response.StatsResponse;
import com.lobai.entity.AffinityScore;
import com.lobai.entity.User;
import com.lobai.entity.UserStatsHistory;
import com.lobai.repository.AffinityScoreRepository;
import com.lobai.repository.UserRepository;
import com.lobai.repository.UserStatsHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * StatsService
 *
 * 사용자 Stats 관리 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepository;
    private final UserStatsHistoryRepository userStatsHistoryRepository;
    private final AffinityScoreRepository affinityScoreRepository;

    /**
     * 사용자의 현재 Stats 조회
     *
     * @param userId 사용자 ID
     * @return Stats 응답
     */
    @Transactional(readOnly = true)
    public StatsResponse getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        Integer trustScore = affinityScoreRepository.findByUserId(userId)
                .map(as -> as.getOverallScore().intValue())
                .orElse(50);

        return StatsResponse.from(user, trustScore);
    }

    /**
     * 사용자 액션에 따른 Stats 업데이트
     *
     * @param userId 사용자 ID
     * @param request 액션 요청
     * @return 업데이트된 Stats
     */
    @Transactional
    public StatsResponse updateStats(Long userId, UpdateStatsRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 2. 액션에 따른 Stats 변화 적용
        Integer newHunger = user.getCurrentHunger();
        Integer newEnergy = user.getCurrentEnergy();
        Integer newHappiness = user.getCurrentHappiness();

        switch (request.getAction()) {
            case feed:
                // 먹이기 → 포만감(Hunger) +10, 에너지(Energy) +5
                newHunger += 10;
                newEnergy += 5;
                log.info("User {} fed the bot: hunger {} -> {}, energy {} -> {}",
                        userId, user.getCurrentHunger(), newHunger, user.getCurrentEnergy(), newEnergy);
                break;

            case play:
                // 놀기 → 행복도(Happiness)만 소폭 증가, 에너지 약간 감소
                newHappiness += 5;
                newEnergy -= 2;
                log.info("User {} played with the bot: happiness {} -> {}, energy {} -> {}",
                        userId, user.getCurrentHappiness(), newHappiness, user.getCurrentEnergy(), newEnergy);
                break;

            case sleep:
                // 재우기 → 에너지(Energy)만 소폭 증가, 배고픔 약간 감소
                newEnergy += 5;
                newHunger -= 1;
                log.info("User {} put the bot to sleep: energy {} -> {}, hunger {} -> {}",
                        userId, user.getCurrentEnergy(), newEnergy, user.getCurrentHunger(), newHunger);
                break;
        }

        // 3. Stats 업데이트 (0-100 범위 자동 보정)
        user.updateStats(newHunger, newEnergy, newHappiness);
        userRepository.save(user);

        // 4. Stats 히스토리 기록
        UserStatsHistory history = UserStatsHistory.builder()
                .user(user)
                .hunger(user.getCurrentHunger())
                .energy(user.getCurrentEnergy())
                .happiness(user.getCurrentHappiness())
                .actionType(UserStatsHistory.ActionType.valueOf(request.getAction().name()))
                .build();

        userStatsHistoryRepository.save(history);

        // 5. 응답 반환
        return StatsResponse.from(user);
    }

    /**
     * Stats 직접 설정 (슬라이더 컨트롤용)
     *
     * @param userId 사용자 ID
     * @param request 직접 설정할 Stats 값
     * @return 업데이트된 Stats
     */
    @Transactional
    public StatsResponse setStats(Long userId, SetStatsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // Stats 직접 설정
        Integer newHunger = request.getHunger() != null ? request.getHunger() : user.getCurrentHunger();
        Integer newEnergy = request.getEnergy() != null ? request.getEnergy() : user.getCurrentEnergy();
        Integer newHappiness = request.getHappiness() != null ? request.getHappiness() : user.getCurrentHappiness();

        user.updateStats(newHunger, newEnergy, newHappiness);
        userRepository.save(user);

        log.info("User {} set stats directly: hunger={}, energy={}, happiness={}",
                userId, newHunger, newEnergy, newHappiness);

        return StatsResponse.from(user);
    }

    /**
     * Stats 자동 감소 (decay)
     * 프론트엔드의 5초마다 감소 로직을 백엔드로 이관하는 경우 사용
     *
     * @param userId 사용자 ID
     * @return 업데이트된 Stats
     */
    @Transactional
    public StatsResponse applyDecay(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // Decay 적용 (프론트엔드 로직과 동일)
        Integer newHunger = user.getCurrentHunger() - 1;
        Integer newEnergy = user.getCurrentEnergy() - 1;
        Integer newHappiness = user.getCurrentHappiness() - 1;

        user.updateStats(newHunger, newEnergy, newHappiness);
        userRepository.save(user);

        // Stats 히스토리 기록
        UserStatsHistory history = UserStatsHistory.builder()
                .user(user)
                .hunger(user.getCurrentHunger())
                .energy(user.getCurrentEnergy())
                .happiness(user.getCurrentHappiness())
                .actionType(UserStatsHistory.ActionType.decay)
                .build();

        userStatsHistoryRepository.save(history);

        log.debug("Stats decay applied for user {}", userId);

        return StatsResponse.from(user);
    }
}
