package com.lobai.repository;

import com.lobai.entity.BehavioralFingerprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BehavioralFingerprintRepository
 */
@Repository
public interface BehavioralFingerprintRepository extends JpaRepository<BehavioralFingerprint, Long> {

    /**
     * HIP ID로 모든 지문 조회
     */
    List<BehavioralFingerprint> findByHipId(String hipId);

    /**
     * HIP ID와 행동 유형으로 조회
     */
    Optional<BehavioralFingerprint> findByHipIdAndBehaviorType(String hipId, String behaviorType);

    /**
     * HIP ID와 행동 유형으로 최신 지문 조회
     */
    Optional<BehavioralFingerprint> findFirstByHipIdAndBehaviorTypeOrderByCapturedAtDesc(
        String hipId,
        String behaviorType
    );

    /**
     * HIP ID로 지문 존재 여부 확인
     */
    boolean existsByHipId(String hipId);
}
