package com.lobai.service;

import com.lobai.entity.User;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HIP 자동 재분석 스케줄러
 *
 * 주기적으로 활성 사용자들의 HIP를 Gemini AI로 재분석합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HipSchedulerService {

    private final HumanIdentityProfileService hipService;
    private final UserRepository userRepository;

    /**
     * 매 시간마다 최근 활성 사용자들의 HIP 재분석
     * 최근 24시간 내 메시지를 보낸 사용자 대상
     */
    @Scheduled(cron = "0 0 * * * *") // 매 시간 정각 (00분 00초)
    public void reanalyzeActiveUsers() {
        log.info("=== Starting scheduled HIP reanalysis for active users ===");

        try {
            LocalDateTime since = LocalDateTime.now().minusHours(24);
            List<User> activeUsers = userRepository.findActiveUsersSince(since);

            log.info("Found {} active users (last 24 hours) for HIP reanalysis", activeUsers.size());

            if (activeUsers.isEmpty()) {
                log.info("No active users found, skipping scheduled reanalysis");
                return;
            }

            int successCount = 0;
            int failCount = 0;

            for (User user : activeUsers) {
                try {
                    log.debug("Reanalyzing HIP for user: {} (email: {})", user.getId(), user.getEmail());
                    hipService.reanalyzeProfile(user.getId());
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to reanalyze HIP for user: {}", user.getId(), e);
                    failCount++;
                }
            }

            log.info("=== Scheduled HIP reanalysis completed: {} success, {} failed ===",
                successCount, failCount);

        } catch (Exception e) {
            log.error("Scheduled HIP reanalysis failed", e);
        }
    }

    /**
     * 매일 새벽 3시에 모든 사용자의 HIP 재분석 (주간 정기 분석)
     * 부하가 적은 시간대에 전체 사용자 분석
     */
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    public void weeklyFullReanalysis() {
        log.info("=== Starting weekly full HIP reanalysis ===");

        try {
            List<User> allActiveUsers = userRepository.findByIsActiveTrue();

            log.info("Found {} active users for weekly full reanalysis", allActiveUsers.size());

            if (allActiveUsers.isEmpty()) {
                log.info("No active users found, skipping weekly reanalysis");
                return;
            }

            int successCount = 0;
            int failCount = 0;

            for (User user : allActiveUsers) {
                try {
                    log.debug("Weekly reanalysis for user: {} (email: {})", user.getId(), user.getEmail());
                    hipService.reanalyzeProfile(user.getId());
                    successCount++;

                    // API Rate Limit 고려하여 100ms 대기
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Weekly reanalysis interrupted", e);
                    break;
                } catch (Exception e) {
                    log.error("Failed weekly reanalysis for user: {}", user.getId(), e);
                    failCount++;
                }
            }

            log.info("=== Weekly full HIP reanalysis completed: {} success, {} failed ===",
                successCount, failCount);

        } catch (Exception e) {
            log.error("Weekly full HIP reanalysis failed", e);
        }
    }
}
