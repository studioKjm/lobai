package com.lobai.service;

import com.lobai.entity.*;
import com.lobai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MissionService
 * 미션 관리 및 진행 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final UserMissionRepository userMissionRepository;
    private final UserRepository userRepository;
    private final LobCoinService lobCoinService;
    private final NotificationService notificationService;
    private final DailyStatsService dailyStatsService;

    /**
     * Get all available missions for user
     */
    @Transactional(readOnly = true)
    public List<Mission> getAvailableMissions(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        int userLevel = user.getTrustLevel() != null ? user.getTrustLevel() : 1;

        return missionRepository.findCurrentlyAvailable(LocalDateTime.now()).stream()
            .filter(mission -> mission.canUserAccess(userLevel))
            .toList();
    }

    /**
     * Get missions by type
     */
    @Transactional(readOnly = true)
    public List<Mission> getMissionsByType(Mission.MissionType type) {
        return missionRepository.findByMissionTypeAndIsActiveTrueOrderByDisplayOrderAsc(type);
    }

    /**
     * Get user's assigned missions
     */
    @Transactional(readOnly = true)
    public List<UserMission> getUserMissions(Long userId, UserMission.MissionStatus status) {
        if (status != null) {
            return userMissionRepository.findByUserIdAndStatusOrderByStartedAtDesc(userId, status);
        } else {
            return userMissionRepository.findInProgressMissions(userId);
        }
    }

    /**
     * Get user's in-progress missions
     */
    @Transactional(readOnly = true)
    public List<UserMission> getInProgressMissions(Long userId) {
        return userMissionRepository.findInProgressMissions(userId);
    }

    /**
     * Assign mission to user
     */
    @Transactional
    public UserMission assignMission(Long userId, Long missionId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Mission mission = missionRepository.findById(missionId)
            .orElseThrow(() -> new IllegalArgumentException("미션을 찾을 수 없습니다"));

        // Check if already assigned
        if (userMissionRepository.existsByUserIdAndMissionId(userId, missionId)) {
            throw new IllegalStateException("이미 할당된 미션입니다");
        }

        // Check if mission is available
        if (!mission.isCurrentlyAvailable()) {
            throw new IllegalStateException("현재 사용할 수 없는 미션입니다");
        }

        // Check user level requirement
        int userLevel = user.getTrustLevel() != null ? user.getTrustLevel() : 1;
        if (!mission.canUserAccess(userLevel)) {
            throw new IllegalStateException(
                String.format("이 미션은 레벨 %d 이상부터 가능합니다", mission.getRequiredLevel())
            );
        }

        UserMission userMission = UserMission.builder()
            .user(user)
            .mission(mission)
            .status(UserMission.MissionStatus.ASSIGNED)
            .build();

        UserMission saved = userMissionRepository.save(userMission);

        // Send notification
        notificationService.sendMissionAssignedNotification(userId, mission.getTitle());

        log.info("Mission {} assigned to user {}", missionId, userId);
        return saved;
    }

    /**
     * Start mission (change status to IN_PROGRESS)
     */
    @Transactional
    public UserMission startMission(Long userMissionId) {
        UserMission userMission = userMissionRepository.findById(userMissionId)
            .orElseThrow(() -> new IllegalArgumentException("사용자 미션을 찾을 수 없습니다"));

        if (userMission.getStatus() != UserMission.MissionStatus.ASSIGNED) {
            throw new IllegalStateException("ASSIGNED 상태의 미션만 시작할 수 있습니다");
        }

        userMission.setStatus(UserMission.MissionStatus.IN_PROGRESS);
        return userMissionRepository.save(userMission);
    }

    /**
     * Complete mission
     */
    @Transactional
    public UserMission completeMission(Long userMissionId) {
        UserMission userMission = userMissionRepository.findById(userMissionId)
            .orElseThrow(() -> new IllegalArgumentException("사용자 미션을 찾을 수 없습니다"));

        if (userMission.getStatus() == UserMission.MissionStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 미션입니다");
        }

        Mission mission = userMission.getMission();
        Long userId = userMission.getUser().getId();

        // Mark as completed
        userMission.complete();
        userMissionRepository.save(userMission);

        // Grant rewards (automatically claimed upon completion)
        if (mission.getLobcoinReward() > 0) {
            lobCoinService.earnLobCoin(
                userId,
                mission.getLobcoinReward(),
                "MISSION_COMPLETE",
                String.format("미션 완료: %s", mission.getTitle())
            );
        }

        userMission.claimReward();
        userMissionRepository.save(userMission);

        // Update daily stats
        dailyStatsService.recordMissionCompletion(userId, mission.getLobcoinReward());

        // Send notification
        notificationService.sendMissionCompletedNotification(
            userId,
            mission.getTitle(),
            mission.getLobcoinReward()
        );

        log.info("Mission {} completed by user {} (Reward: {} LobCoin)",
            mission.getId(), userId, mission.getLobcoinReward());

        return userMission;
    }

    /**
     * Fail mission
     */
    @Transactional
    public UserMission failMission(Long userMissionId, String reason) {
        UserMission userMission = userMissionRepository.findById(userMissionId)
            .orElseThrow(() -> new IllegalArgumentException("사용자 미션을 찾을 수 없습니다"));

        userMission.fail();
        userMission.setMetadata(String.format("{\"failReason\":\"%s\"}", reason));

        log.info("Mission {} failed for user {}: {}", userMission.getMission().getId(),
            userMission.getUser().getId(), reason);

        return userMissionRepository.save(userMission);
    }

    /**
     * Auto-assign daily missions to user
     */
    @Transactional
    public void autoAssignDailyMissions(Long userId) {
        List<Mission> dailyMissions = missionRepository.findByMissionTypeAndIsActiveTrueOrderByDisplayOrderAsc(
            Mission.MissionType.DAILY
        );

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        int userLevel = user.getTrustLevel() != null ? user.getTrustLevel() : 1;

        for (Mission mission : dailyMissions) {
            // Check if already assigned
            if (userMissionRepository.existsByUserIdAndMissionId(userId, mission.getId())) {
                continue;
            }

            // Check level requirement
            if (!mission.canUserAccess(userLevel)) {
                continue;
            }

            // Auto-assign
            assignMission(userId, mission.getId());
        }

        log.info("Auto-assigned daily missions to user {}", userId);
    }

    /**
     * Check and expire old missions
     */
    @Transactional
    public void expireOldMissions() {
        List<UserMission> inProgress = userMissionRepository.findInProgressMissions(null);

        LocalDateTime now = LocalDateTime.now();

        for (UserMission userMission : inProgress) {
            Mission mission = userMission.getMission();

            if (mission.getAvailableUntil() != null && now.isAfter(mission.getAvailableUntil())) {
                userMission.expire();
                userMissionRepository.save(userMission);

                log.info("Expired mission {} for user {}", mission.getId(), userMission.getUser().getId());
            }
        }
    }

    /**
     * Get mission completion rate for user
     */
    @Transactional(readOnly = true)
    public double getCompletionRate(Long userId) {
        Long completed = userMissionRepository.countCompletedMissions(userId);
        Long total = (long) userMissionRepository.findByUserIdAndStatusOrderByStartedAtDesc(
            userId, UserMission.MissionStatus.COMPLETED
        ).size() + userMissionRepository.findInProgressMissions(userId).size();

        if (total == 0) return 0.0;
        return (completed.doubleValue() / total) * 100;
    }
}
