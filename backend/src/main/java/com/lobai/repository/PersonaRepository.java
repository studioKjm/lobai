package com.lobai.repository;

import com.lobai.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PersonaRepository
 *
 * Persona 엔티티에 대한 데이터 접근 계층
 */
@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {

    /**
     * 활성화된 페르소나 목록 조회 (표시 순서대로)
     */
    List<Persona> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * 영문명으로 페르소나 조회
     */
    Optional<Persona> findByNameEn(String nameEn);

    /**
     * 한글명으로 페르소나 조회
     */
    Optional<Persona> findByName(String name);

    /**
     * 활성화 여부로 페르소나 개수 조회
     */
    long countByIsActive(Boolean isActive);
}
