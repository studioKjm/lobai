package com.lobai.repository;

import com.lobai.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {

    /**
     * Find all active missions by type
     */
    List<Mission> findByMissionTypeAndIsActiveTrueOrderByDisplayOrderAsc(Mission.MissionType type);

    /**
     * Find all active missions
     */
    List<Mission> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find missions available for a user level
     */
    @Query("SELECT m FROM Mission m WHERE m.isActive = true AND m.requiredLevel <= :userLevel " +
           "ORDER BY m.displayOrder ASC")
    List<Mission> findAvailableForUserLevel(@Param("userLevel") int userLevel);

    /**
     * Find currently available missions (within time range)
     */
    @Query("SELECT m FROM Mission m WHERE m.isActive = true " +
           "AND (m.availableFrom IS NULL OR m.availableFrom <= :now) " +
           "AND (m.availableUntil IS NULL OR m.availableUntil >= :now) " +
           "ORDER BY m.displayOrder ASC")
    List<Mission> findCurrentlyAvailable(@Param("now") LocalDateTime now);

    /**
     * Find daily missions
     */
    List<Mission> findByMissionTypeAndIsActiveTrueOrderByDifficultyAsc(Mission.MissionType type);

    /**
     * Find missions by difficulty
     */
    List<Mission> findByDifficultyAndIsActiveTrueOrderByDisplayOrderAsc(Integer difficulty);

    /**
     * Count active missions by type
     */
    @Query("SELECT COUNT(m) FROM Mission m WHERE m.missionType = :type AND m.isActive = true")
    Long countByTypeAndActive(@Param("type") Mission.MissionType type);
}
