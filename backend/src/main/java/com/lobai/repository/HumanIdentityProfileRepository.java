package com.lobai.repository;

import com.lobai.entity.HumanIdentityProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * HumanIdentityProfileRepository
 */
@Repository
public interface HumanIdentityProfileRepository extends JpaRepository<HumanIdentityProfile, String> {

    /**
     * userId로 HIP 조회
     */
    Optional<HumanIdentityProfile> findByUserId(Long userId);

    /**
     * userId로 HIP 존재 여부 확인
     */
    boolean existsByUserId(Long userId);

    /**
     * 검증 상태로 조회
     */
    List<HumanIdentityProfile> findByVerificationStatus(String status);

    /**
     * Identity Level로 조회
     */
    List<HumanIdentityProfile> findByIdentityLevel(Integer level);

    /**
     * Overall HIP Score 범위로 조회
     */
    @Query("SELECT h FROM HumanIdentityProfile h " +
           "WHERE h.overallHipScore >= :minScore AND h.overallHipScore <= :maxScore " +
           "ORDER BY h.overallHipScore DESC")
    List<HumanIdentityProfile> findByScoreRange(
        @Param("minScore") BigDecimal minScore,
        @Param("maxScore") BigDecimal maxScore
    );

    /**
     * 상위 N개 HIP 조회 (점수 기준)
     */
    @Query("SELECT h FROM HumanIdentityProfile h " +
           "ORDER BY h.overallHipScore DESC")
    List<HumanIdentityProfile> findTopByScore(int limit);

    /**
     * 데이터 품질이 낮은 HIP 조회 (재분석 필요)
     */
    @Query("SELECT h FROM HumanIdentityProfile h " +
           "WHERE h.dataQualityScore < :threshold " +
           "ORDER BY h.dataQualityScore ASC")
    List<HumanIdentityProfile> findLowQualityProfiles(@Param("threshold") BigDecimal threshold);

    /**
     * 검증이 필요한 HIP 조회
     * (마지막 검증 후 30일 경과 또는 미검증)
     */
    @Query(value = "SELECT * FROM human_identity_profiles h " +
           "WHERE h.verification_status = 'unverified' " +
           "OR h.last_verified_at < DATE_SUB(NOW(), INTERVAL 30 DAY)",
           nativeQuery = true)
    List<HumanIdentityProfile> findProfilesNeedingVerification();
}
