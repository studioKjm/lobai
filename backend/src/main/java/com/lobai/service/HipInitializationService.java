package com.lobai.service;

import com.lobai.entity.HumanIdentityProfile;
import com.lobai.entity.User;
import com.lobai.repository.HumanIdentityProfileRepository;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * HipInitializationService
 *
 * 애플리케이션 시작 시 기존 사용자들에 대해 HIP 초기화
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HipInitializationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HumanIdentityProfileRepository hipRepository;
    private final HumanIdentityProfileService hipService;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== HIP Initialization Service Started ===");

        try {
            initializeHipForExistingUsers();
            reanalyzeAllProfiles();
            log.info("=== HIP Initialization Service Completed ===");

        } catch (Exception e) {
            log.error("Failed to initialize HIP: {}", e.getMessage(), e);
        }
    }

    /**
     * 기존 사용자들에 대해 HIP 생성
     */
    private void initializeHipForExistingUsers() {
        log.info("Initializing HIP for existing users...");

        List<User> allUsers = userRepository.findAll();
        int createdCount = 0;
        int existingCount = 0;

        for (User user : allUsers) {
            if (hipRepository.existsByUserId(user.getId())) {
                existingCount++;
                continue;
            }

            try {
                hipService.createInitialProfile(user.getId());
                createdCount++;
                log.info("Created HIP for user: {}", user.getId());

            } catch (Exception e) {
                log.error("Failed to create HIP for user {}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("HIP initialization completed: {} created, {} existing", createdCount, existingCount);
    }

    /**
     * 모든 HIP 재분석
     * (AffinityScore, Message 등 기존 데이터 기반)
     */
    private void reanalyzeAllProfiles() {
        log.info("Reanalyzing all HIP profiles...");

        List<HumanIdentityProfile> allProfiles = hipRepository.findAll();
        int analyzedCount = 0;

        for (HumanIdentityProfile hip : allProfiles) {
            try {
                hipService.reanalyzeProfile(hip.getUserId());
                analyzedCount++;

                if (analyzedCount % 10 == 0) {
                    log.info("Reanalyzed {} profiles...", analyzedCount);
                }

            } catch (Exception e) {
                log.error("Failed to reanalyze HIP {}: {}", hip.getHipId(), e.getMessage());
            }
        }

        log.info("Reanalysis completed: {} profiles analyzed", analyzedCount);
    }

    /**
     * 수동 실행: 특정 사용자 HIP 초기화
     */
    @Transactional
    public HumanIdentityProfile initializeForUser(Long userId) {
        log.info("Manually initializing HIP for user: {}", userId);
        return hipService.getOrCreateProfile(userId);
    }

    /**
     * 수동 실행: 모든 HIP 검증
     */
    @Transactional
    public void verifyAllProfiles() {
        log.info("Verifying all HIP profiles...");

        List<HumanIdentityProfile> allProfiles = hipRepository.findAll();
        int verifiedCount = 0;

        for (HumanIdentityProfile hip : allProfiles) {
            try {
                hipService.verifyProfile(hip.getHipId());
                verifiedCount++;

            } catch (Exception e) {
                log.error("Failed to verify HIP {}: {}", hip.getHipId(), e.getMessage());
            }
        }

        log.info("Verification completed: {} profiles verified", verifiedCount);
    }
}
