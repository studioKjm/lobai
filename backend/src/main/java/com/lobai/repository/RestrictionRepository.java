package com.lobai.repository;

import com.lobai.entity.Restriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RestrictionRepository extends JpaRepository<Restriction, Long> {

    /**
     * Find all active restrictions for a user
     */
    List<Restriction> findByUserIdAndIsActiveTrueOrderByStartedAtDesc(Long userId);

    /**
     * Find active restrictions by type
     */
    List<Restriction> findByUserIdAndRestrictionTypeAndIsActiveTrueOrderByStartedAtDesc(
        Long userId,
        Restriction.RestrictionType type
    );

    /**
     * Find all restrictions for a user (including inactive)
     */
    List<Restriction> findByUserIdOrderByStartedAtDesc(Long userId);

    /**
     * Find expired restrictions that are still active
     */
    @Query("SELECT r FROM Restriction r WHERE r.isActive = true AND r.endsAt IS NOT NULL AND r.endsAt < :now")
    List<Restriction> findExpiredActiveRestrictions(@Param("now") LocalDateTime now);

    /**
     * Check if user has active restriction
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Restriction r " +
           "WHERE r.user.id = :userId AND r.isActive = true")
    boolean hasActiveRestriction(@Param("userId") Long userId);

    /**
     * Check if user has active restriction of specific type
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Restriction r " +
           "WHERE r.user.id = :userId AND r.restrictionType = :type AND r.isActive = true")
    boolean hasActiveRestrictionOfType(
        @Param("userId") Long userId,
        @Param("type") Restriction.RestrictionType type
    );

    /**
     * Count active restrictions for a user
     */
    @Query("SELECT COUNT(r) FROM Restriction r WHERE r.user.id = :userId AND r.isActive = true")
    Long countActiveRestrictions(@Param("userId") Long userId);
}
