package com.lobai.repository;

import com.lobai.entity.LevelHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LevelHistoryRepository extends JpaRepository<LevelHistory, Long> {

    /**
     * Find all level changes for a user (ordered by created_at DESC)
     */
    List<LevelHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find recent level changes for a user
     */
    @Query("SELECT h FROM LevelHistory h WHERE h.user.id = :userId ORDER BY h.createdAt DESC")
    List<LevelHistory> findRecentHistory(
        @Param("userId") Long userId,
        Pageable pageable
    );

    /**
     * Find latest level change for a user
     */
    @Query("SELECT h FROM LevelHistory h WHERE h.user.id = :userId ORDER BY h.createdAt DESC LIMIT 1")
    LevelHistory findLatestByUserId(@Param("userId") Long userId);

    /**
     * Find level changes by new level
     */
    List<LevelHistory> findByNewLevelOrderByCreatedAtDesc(Integer newLevel);

    /**
     * Find level changes in date range
     */
    List<LevelHistory> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long userId,
        LocalDateTime start,
        LocalDateTime end
    );

    /**
     * Count level ups for a user
     */
    @Query("SELECT COUNT(h) FROM LevelHistory h WHERE h.user.id = :userId AND h.newLevel > h.previousLevel")
    Long countLevelUpsByUserId(@Param("userId") Long userId);

    /**
     * Count level downs for a user
     */
    @Query("SELECT COUNT(h) FROM LevelHistory h WHERE h.user.id = :userId AND h.newLevel < h.previousLevel")
    Long countLevelDownsByUserId(@Param("userId") Long userId);
}
