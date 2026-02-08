package com.lobai.repository;

import com.lobai.entity.IdentityVerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * IdentityVerificationLogRepository
 */
@Repository
public interface IdentityVerificationLogRepository extends JpaRepository<IdentityVerificationLog, Long> {

    /**
     * HIP ID로 모든 검증 로그 조회 (최신순)
     */
    List<IdentityVerificationLog> findByHipIdOrderByVerifiedAtDesc(String hipId);

    /**
     * HIP ID로 최신 검증 로그 조회
     */
    Optional<IdentityVerificationLog> findFirstByHipIdOrderByVerifiedAtDesc(String hipId);

    /**
     * HIP ID와 검증 유형으로 조회
     */
    List<IdentityVerificationLog> findByHipIdAndVerificationType(String hipId, String verificationType);

    /**
     * HIP ID와 상태로 조회
     */
    List<IdentityVerificationLog> findByHipIdAndStatus(String hipId, String status);

    /**
     * 특정 기간의 검증 로그 조회
     */
    List<IdentityVerificationLog> findByVerifiedAtBetween(
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Flagged 상태의 최근 로그 조회
     */
    List<IdentityVerificationLog> findByStatusOrderByVerifiedAtDesc(String status);
}
