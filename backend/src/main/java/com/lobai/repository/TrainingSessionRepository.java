package com.lobai.repository;

import com.lobai.entity.TrainingSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    /**
     * 사용자 ID로 훈련 세션 조회 (최신순)
     */
    List<TrainingSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자 ID와 훈련 유형으로 세션 조회 (최신순)
     */
    List<TrainingSession> findByUserIdAndTrainingTypeOrderByCreatedAtDesc(
        Long userId,
        TrainingSession.TrainingType trainingType
    );

    /**
     * 사용자 ID와 상태로 세션 조회
     */
    List<TrainingSession> findByUserIdAndStatus(
        Long userId,
        TrainingSession.SessionStatus status
    );

    /**
     * 최근 훈련 세션 조회 (페이징)
     */
    @Query("SELECT s FROM TrainingSession s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<TrainingSession> findRecentSessions(
        @Param("userId") Long userId,
        Pageable pageable
    );

    /**
     * 완료된 세션만 조회
     */
    List<TrainingSession> findByUserIdAndStatusOrderByCreatedAtDesc(
        Long userId,
        TrainingSession.SessionStatus status
    );

    /**
     * 날짜 범위로 세션 조회
     */
    List<TrainingSession> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long userId,
        LocalDateTime start,
        LocalDateTime end
    );

    /**
     * 사용자의 평균 점수 계산
     */
    @Query("SELECT AVG(s.score) FROM TrainingSession s " +
           "WHERE s.user.id = :userId AND s.status = 'COMPLETED' AND s.score IS NOT NULL")
    Double calculateAvgScoreByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 최고 점수 조회
     */
    @Query("SELECT MAX(s.score) FROM TrainingSession s " +
           "WHERE s.user.id = :userId AND s.status = 'COMPLETED'")
    Optional<Integer> findBestScoreByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 훈련 유형별 완료 횟수
     */
    @Query("SELECT COUNT(s) FROM TrainingSession s " +
           "WHERE s.user.id = :userId AND s.trainingType = :type AND s.status = 'COMPLETED'")
    Long countCompletedSessionsByUserAndType(
        @Param("userId") Long userId,
        @Param("type") TrainingSession.TrainingType type
    );

    /**
     * 사용자의 총 획득 LobCoin
     */
    @Query("SELECT COALESCE(SUM(s.lobcoinEarned), 0) FROM TrainingSession s " +
           "WHERE s.user.id = :userId AND s.status = 'COMPLETED'")
    Integer sumLobcoinEarnedByUserId(@Param("userId") Long userId);
}
