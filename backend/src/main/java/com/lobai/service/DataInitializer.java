package com.lobai.service;

import com.lobai.entity.Persona;
import com.lobai.entity.Role;
import com.lobai.entity.User;
import com.lobai.repository.PersonaRepository;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DataInitializer
 *
 * 애플리케이션 시작 시 초기 데이터 생성
 * - 기존 사용자 전체 삭제
 * - Admin 계정 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Order(1) // HipInitializationService보다 먼저 실행
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Data Initialization Started ===");

        try {
            // Admin 계정만 생성 (기존 사용자는 삭제하지 않음)
            createAdminUser();

            log.info("=== Data Initialization Completed ===");

        } catch (Exception e) {
            log.error("Failed to initialize data: {}", e.getMessage(), e);
        }
    }

    /**
     * 기존 사용자 전체 삭제
     */
    private void deleteAllUsers() {
        log.info("Deleting all existing users...");

        long count = userRepository.count();
        userRepository.deleteAll();

        log.info("Deleted {} existing users", count);
    }

    /**
     * Admin 계정 생성
     */
    private void createAdminUser() {
        log.info("Creating admin user...");

        // Admin 계정이 이미 존재하는지 확인
        if (userRepository.findByEmail("admin@admin.com").isPresent()) {
            log.info("Admin user already exists, skipping creation");
            return;
        }

        // 기본 페르소나 가져오기 (친구모드)
        Persona defaultPersona = personaRepository.findByNameEn("friend")
                .orElse(null);

        if (defaultPersona == null) {
            log.warn("Default persona 'friend' not found, admin user will have no persona");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode("admin123");

        // Admin 사용자 생성
        User adminUser = User.builder()
                .email("admin@admin.com")
                .passwordHash(encodedPassword)
                .username("Admin")
                .role(Role.ADMIN)
                .currentPersona(defaultPersona)
                .currentHunger(50)
                .currentEnergy(50)
                .currentHappiness(50)
                .isActive(true)
                .subscriptionTier(User.SubscriptionTier.premium)
                .totalAttendanceDays(0)
                .maxStreakDays(0)
                .build();

        userRepository.save(adminUser);

        log.info("✅ Admin user created successfully:");
        log.info("   Email: admin@admin.com");
        log.info("   Password: admin123");
        log.info("   Role: ADMIN");
    }
}
