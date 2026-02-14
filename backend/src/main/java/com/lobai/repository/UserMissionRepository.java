package com.lobai.repository;

import com.lobai.entity.UserMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserMissionRepository extends JpaRepository<UserMission, Long> {

    /**
     * Find all missions for a user by status
     */
    List<UserMission> findByUserIdAndStatusOrderByStartedAtDesc(
        Long userId,
        UserMission.MissionStatus status
    );

    /**
     * Find all in-progress missions for a user
     */
    @Query("SELECT um FROM UserMission um WHERE um.user.id = :userId " +
           "AND (um.status = 'ASSIGNED' OR um.status = 'IN_PROGRESS') " +
           "ORDER BY um.startedAt DESC")
    List<UserMission> findInProgressMissions(@Param("userId") Long userId);

    /**
     * Find completed missions for a user
     */
    List<UserMission> findByUserIdAndStatusOrderByCompletedAtDesc(
        Long userId,
        UserMission.MissionStatus status
    );

    /**
     * Find user mission by user and mission ID
     */
    Optional<UserMission> findByUserIdAndMissionId(Long userId, Long missionId);

    /**
     * Check if user has mission assigned
     */
    @Query("SELECT CASE WHEN COUNT(um) > 0 THEN true ELSE false END FROM UserMission um " +
           "WHERE um.user.id = :userId AND um.mission.id = :missionId")
    boolean existsByUserIdAndMissionId(
        @Param("userId") Long userId,
        @Param("missionId") Long missionId
    );

    /**
     * Find missions with unclaimed rewards
     */
    @Query("SELECT um FROM UserMission um WHERE um.user.id = :userId " +
           "AND um.status = 'COMPLETED' AND um.rewardClaimed = false " +
           "ORDER BY um.completedAt DESC")
    List<UserMission> findUnclaimedRewards(@Param("userId") Long userId);

    /**
     * Count completed missions by user
     */
    @Query("SELECT COUNT(um) FROM UserMission um WHERE um.user.id = :userId AND um.status = 'COMPLETED'")
    Long countCompletedMissions(@Param("userId") Long userId);

    /**
     * Count completed missions by user and date range
     */
    @Query("SELECT COUNT(um) FROM UserMission um WHERE um.user.id = :userId " +
           "AND um.status = 'COMPLETED' AND um.completedAt BETWEEN :start AND :end")
    Long countCompletedMissionsInRange(
        @Param("userId") Long userId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * Find missions by user and mission type
     */
    @Query("SELECT um FROM UserMission um WHERE um.user.id = :userId " +
           "AND um.mission.missionType = :missionType " +
           "ORDER BY um.startedAt DESC")
    List<UserMission> findByUserIdAndMissionType(
        @Param("userId") Long userId,
        @Param("missionType") com.lobai.entity.Mission.MissionType missionType
    );
}
