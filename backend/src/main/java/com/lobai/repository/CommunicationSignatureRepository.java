package com.lobai.repository;

import com.lobai.entity.CommunicationSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CommunicationSignatureRepository
 */
@Repository
public interface CommunicationSignatureRepository extends JpaRepository<CommunicationSignature, Long> {

    /**
     * HIP ID로 모든 서명 조회
     */
    List<CommunicationSignature> findByHipId(String hipId);

    /**
     * HIP ID와 서명 유형으로 조회
     */
    Optional<CommunicationSignature> findByHipIdAndSignatureType(String hipId, String signatureType);

    /**
     * HIP ID와 서명 유형으로 최신 서명 조회
     */
    Optional<CommunicationSignature> findFirstByHipIdAndSignatureTypeOrderByExtractedAtDesc(
        String hipId,
        String signatureType
    );

    /**
     * HIP ID로 서명 존재 여부 확인
     */
    boolean existsByHipId(String hipId);
}
