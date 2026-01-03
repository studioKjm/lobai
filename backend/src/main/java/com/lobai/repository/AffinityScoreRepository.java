package com.lobai.repository;

import com.lobai.entity.AffinityScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AffinityScoreRepository
 *
 * AffinityScore 엔티티에 대한 데이터 접근 계층
 */
@Repository
public interface AffinityScoreRepository extends JpaRepository<AffinityScore, Long> {

    /**
     * 사용자 ID로 친밀도 점수 조회
     */
    Optional<AffinityScore> findByUserId(Long userId);

    /**
     * 레벨별 친밀도 점수 조회 (점수 내림차순)
     */
    List<AffinityScore> findByLevelOrderByOverallScoreDesc(Integer level);

    /**
     * 전체 친밀도 점수 순위 조회 (상위 N명)
     */
    @Query("SELECT a FROM AffinityScore a ORDER BY a.overallScore DESC")
    List<AffinityScore> findTopByOrderByOverallScoreDesc();

    /**
     * 사용자별 평균 점수 조회
     */
    @Query("SELECT AVG(a.overallScore) FROM AffinityScore a WHERE a.userId = :userId")
    Optional<Double> getAverageScoreByUser(@Param("userId") Long userId);

    /**
     * 특정 점수 범위의 사용자 수 조회
     */
    @Query("SELECT COUNT(a) FROM AffinityScore a WHERE a.overallScore BETWEEN :minScore AND :maxScore")
    long countByScoreRange(@Param("minScore") double minScore, @Param("maxScore") double maxScore);

    /**
     * 레벨별 사용자 수 조회
     */
    @Query("SELECT COUNT(a) FROM AffinityScore a WHERE a.level = :level")
    long countByLevel(@Param("level") Integer level);

    /**
     * 전체 평균 점수 조회
     */
    @Query("SELECT AVG(a.overallScore) FROM AffinityScore a")
    Optional<Double> getGlobalAverageScore();
}
