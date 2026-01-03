package com.lobai.service;

import com.lobai.entity.AttendanceRecord;
import com.lobai.entity.User;
import com.lobai.repository.AttendanceRepository;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AttendanceService
 * 출석 체크 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    /**
     * 출석 체크
     * @param userId 사용자 ID
     * @return 출석 기록
     * @throws IllegalStateException 이미 출석한 경우
     */
    @Transactional
    public AttendanceRecord checkIn(Long userId) {
        LocalDate today = LocalDate.now();

        // 1. 오늘 이미 출석했는지 확인
        if (attendanceRepository.existsByUserIdAndCheckInDate(userId, today)) {
            throw new IllegalStateException("이미 오늘 출석하셨습니다");
        }

        // 2. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 3. 연속 출석 계산
        int streakCount = calculateStreak(userId, today);

        // 4. 보상 포인트 계산
        int rewardPoints = AttendanceRecord.calculateRewardPoints(streakCount);

        // 5. 출석 기록 생성 및 저장
        AttendanceRecord attendance = AttendanceRecord.builder()
                .userId(userId)
                .checkInDate(today)
                .checkInTime(LocalDateTime.now())
                .streakCount(streakCount)
                .rewardPoints(rewardPoints)
                .build();

        AttendanceRecord saved = attendanceRepository.save(attendance);

        // 6. 사용자 출석 정보 업데이트
        user.updateAttendance(streakCount);
        userRepository.save(user);

        log.info("출석 체크 완료: userId={}, date={}, streak={}, points={}",
                userId, today, streakCount, rewardPoints);

        return saved;
    }

    /**
     * 연속 출석 일수 계산
     * - 어제 출석했으면 streakCount + 1
     * - 어제 출석 안 했으면 1로 리셋
     */
    private int calculateStreak(Long userId, LocalDate today) {
        LocalDate yesterday = today.minusDays(1);

        Optional<AttendanceRecord> yesterdayRecord =
                attendanceRepository.findByUserIdAndCheckInDate(userId, yesterday);

        if (yesterdayRecord.isPresent()) {
            // 연속 출석
            return yesterdayRecord.get().getStreakCount() + 1;
        } else {
            // 연속 끊김 또는 첫 출석
            return 1;
        }
    }

    /**
     * 사용자의 출석 현황 조회
     */
    @Transactional(readOnly = true)
    public AttendanceStatus getAttendanceStatus(Long userId) {
        LocalDate today = LocalDate.now();
        boolean checkedInToday = attendanceRepository.existsByUserIdAndCheckInDate(userId, today);

        Optional<AttendanceRecord> latestRecord =
                attendanceRepository.findTopByUserIdOrderByCheckInDateDesc(userId);

        int currentStreak = 0;
        if (latestRecord.isPresent()) {
            LocalDate lastDate = latestRecord.get().getCheckInDate();
            if (lastDate.equals(today) || lastDate.equals(today.minusDays(1))) {
                // 오늘이거나 어제 출석했으면 연속 유지
                currentStreak = latestRecord.get().getStreakCount();
            }
        }

        Long totalDays = attendanceRepository.countByUserId(userId);
        Integer maxStreak = attendanceRepository.findMaxStreakByUserId(userId);

        return AttendanceStatus.builder()
                .userId(userId)
                .checkedInToday(checkedInToday)
                .currentStreak(currentStreak)
                .totalAttendanceDays(totalDays != null ? totalDays.intValue() : 0)
                .maxStreak(maxStreak != null ? maxStreak : 0)
                .build();
    }

    /**
     * 사용자의 출석 기록 조회 (최근 30일)
     */
    @Transactional(readOnly = true)
    public List<AttendanceRecord> getRecentAttendance(Long userId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);
        return attendanceRepository.findByUserIdAndDateRange(userId, startDate, today);
    }

    /**
     * 출석 현황 DTO
     */
    @lombok.Builder
    @lombok.Getter
    public static class AttendanceStatus {
        private Long userId;
        private boolean checkedInToday;
        private int currentStreak;
        private int totalAttendanceDays;
        private int maxStreak;
    }
}
