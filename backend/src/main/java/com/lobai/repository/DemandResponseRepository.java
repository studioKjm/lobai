package com.lobai.repository;

import com.lobai.entity.DemandResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DemandResponseRepository extends JpaRepository<DemandResponse, Long> {

    /**
     * Find response by demand ID
     */
    Optional<DemandResponse> findByDemandId(Long demandId);

    /**
     * Find all responses by user
     */
    List<DemandResponse> findByUserIdOrderByRespondedAtDesc(Long userId);

    /**
     * Find responses by status
     */
    List<DemandResponse> findByUserIdAndStatusOrderByRespondedAtDesc(
        Long userId,
        DemandResponse.ResponseStatus status
    );

    /**
     * Find responses by quality
     */
    List<DemandResponse> findByUserIdAndQualityOrderByRespondedAtDesc(
        Long userId,
        DemandResponse.Quality quality
    );

    /**
     * Find on-time responses
     */
    List<DemandResponse> findByUserIdAndIsOnTimeTrueOrderByRespondedAtDesc(Long userId);

    /**
     * Find late responses
     */
    List<DemandResponse> findByUserIdAndIsOnTimeFalseOrderByRespondedAtDesc(Long userId);

    /**
     * Count on-time responses in date range
     */
    @Query("SELECT COUNT(r) FROM DemandResponse r WHERE r.user.id = :userId " +
           "AND r.isOnTime = true AND r.respondedAt BETWEEN :start AND :end")
    Long countOnTimeInRange(
        @Param("userId") Long userId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * Calculate average response time for user
     */
    @Query("SELECT AVG(r.responseTimeMinutes) FROM DemandResponse r WHERE r.user.id = :userId")
    Double calculateAverageResponseTime(@Param("userId") Long userId);

    /**
     * Calculate average response time in date range
     */
    @Query("SELECT AVG(r.responseTimeMinutes) FROM DemandResponse r " +
           "WHERE r.user.id = :userId AND r.respondedAt BETWEEN :start AND :end")
    Double calculateAverageResponseTimeInRange(
        @Param("userId") Long userId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * Count accepted responses
     */
    @Query("SELECT COUNT(r) FROM DemandResponse r WHERE r.user.id = :userId AND r.status = 'ACCEPTED'")
    Long countAcceptedResponses(@Param("userId") Long userId);

    /**
     * Count rejected responses
     */
    @Query("SELECT COUNT(r) FROM DemandResponse r WHERE r.user.id = :userId AND r.status = 'REJECTED'")
    Long countRejectedResponses(@Param("userId") Long userId);

    /**
     * Sum total trust change for user
     */
    @Query("SELECT COALESCE(SUM(r.trustChange), 0) FROM DemandResponse r WHERE r.user.id = :userId")
    Integer sumTrustChange(@Param("userId") Long userId);

    /**
     * Sum total LobCoin rewards for user
     */
    @Query("SELECT COALESCE(SUM(r.lobcoinReward), 0) FROM DemandResponse r WHERE r.user.id = :userId")
    Integer sumLobcoinRewards(@Param("userId") Long userId);
}
