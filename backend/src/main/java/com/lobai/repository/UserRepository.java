package com.lobai.repository;

import com.lobai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository
 *
 * User 엔티티에 대한 데이터 접근 계층
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회 (인증용)
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * OAuth 제공자와 ID로 사용자 조회 (Phase 2)
     */
    Optional<User> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);

    /**
     * 활성화된 사용자 목록 조회
     */
    List<User> findByIsActiveTrue();

    /**
     * 최근 가입한 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(@Param("since") LocalDateTime since);

    /**
     * 최근 로그인한 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :since ORDER BY u.lastLoginAt DESC")
    List<User> findRecentlyLoggedInUsers(@Param("since") LocalDateTime since);

    /**
     * 특정 페르소나를 사용하는 사용자 수 조회
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.currentPersona.id = :personaId")
    long countByCurrentPersonaId(@Param("personaId") Long personaId);
}
