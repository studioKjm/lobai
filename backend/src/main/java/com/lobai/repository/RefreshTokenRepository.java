package com.lobai.repository;

import com.lobai.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RefreshTokenRepository
 *
 * RefreshToken 엔티티에 대한 데이터 접근 계층
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 문자열로 RefreshToken 조회
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 사용자의 유효한 RefreshToken 목록 조회
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 사용자의 모든 RefreshToken 조회
     */
    List<RefreshToken> findByUserId(Long userId);

    /**
     * 사용자의 모든 RefreshToken 폐기
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user.id = :userId")
    void revokeAllByUserId(@Param("userId") Long userId);

    /**
     * 만료된 RefreshToken 삭제
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * 폐기된 RefreshToken 삭제
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.isRevoked = true")
    void deleteRevokedTokens();

    /**
     * 토큰 존재 여부 확인
     */
    boolean existsByToken(String token);
}
