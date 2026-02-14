package com.lobai.repository;

import com.lobai.entity.NotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {

    /**
     * Find settings by user ID
     */
    Optional<NotificationSettings> findByUserId(Long userId);

    /**
     * Check if settings exist for user
     */
    boolean existsByUserId(Long userId);
}
