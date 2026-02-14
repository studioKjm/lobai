package com.lobai.repository;

import com.lobai.entity.UserMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserMemoryRepository extends JpaRepository<UserMemory, Long> {

    /**
     * 활성 기억 조회
     */
    @Query("SELECT um FROM UserMemory um WHERE um.user.id = :userId AND um.isActive = true " +
           "ORDER BY um.confidenceScore DESC")
    List<UserMemory> findActiveByUserId(@Param("userId") Long userId);

    /**
     * 타입별 기억 조회
     */
    @Query("SELECT um FROM UserMemory um WHERE um.user.id = :userId AND um.memoryType = :type AND um.isActive = true")
    List<UserMemory> findByUserIdAndMemoryType(
            @Param("userId") Long userId,
            @Param("type") UserMemory.MemoryType type);

    /**
     * 특정 키의 기억 조회 (upsert용)
     */
    Optional<UserMemory> findByUserIdAndMemoryKey(Long userId, String memoryKey);
}
