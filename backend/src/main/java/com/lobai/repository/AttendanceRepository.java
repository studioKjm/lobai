package com.lobai.repository;

import com.lobai.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    /**
     * 특정 날짜의 출석 기록 조회
     */
    Optional<AttendanceRecord> findByUserIdAndCheckInDate(Long userId, LocalDate checkInDate);

    /**
     * 사용자의 가장 최근 출석 기록 조회
     */
    Optional<AttendanceRecord> findTopByUserIdOrderByCheckInDateDesc(Long userId);

    /**
     * 사용자의 모든 출석 기록 조회 (최신순)
     */
    List<AttendanceRecord> findByUserIdOrderByCheckInDateDesc(Long userId);

    /**
     * 사용자의 특정 기간 출석 기록 조회
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.userId = :userId " +
            "AND a.checkInDate BETWEEN :startDate AND :endDate " +
            "ORDER BY a.checkInDate DESC")
    List<AttendanceRecord> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 사용자의 총 출석 일수 조회
     */
    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 최대 연속 출석 일수 조회
     */
    @Query("SELECT MAX(a.streakCount) FROM AttendanceRecord a WHERE a.userId = :userId")
    Integer findMaxStreakByUserId(@Param("userId") Long userId);

    /**
     * 특정 날짜에 출석했는지 확인
     */
    boolean existsByUserIdAndCheckInDate(Long userId, LocalDate checkInDate);
}
