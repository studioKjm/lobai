package com.lobai.service;

import com.lobai.entity.DailyStats;
import com.lobai.entity.User;
import com.lobai.repository.DailyStatsRepository;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * DailyStatsService
 * 일일 통계 수집 및 관리 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyStatsService {

    private final DailyStatsRepository dailyStatsRepository;
    private final UserRepository userRepository;

    /**
     * Get or create today's stats for user
     */
    @Transactional
    public DailyStats getTodayStats(Long userId) {
        LocalDate today = LocalDate.now();

        return dailyStatsRepository.findByUserIdAndStatDate(userId, today)
            .orElseGet(() -> createDailyStats(userId, today));
    }

    /**
     * Get stats for specific date
     */
    @Transactional(readOnly = true)
    public DailyStats getStatsByDate(Long userId, LocalDate date) {
        return dailyStatsRepository.findByUserIdAndStatDate(userId, date)
            .orElse(null);
    }

    /**
     * Get stats in date range
     */
    @Transactional(readOnly = true)
    public List<DailyStats> getStatsInRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return dailyStatsRepository.findByUserIdAndStatDateBetweenOrderByStatDateDesc(
            userId, startDate, endDate
        );
    }

    /**
     * Record message sent
     */
    @Transactional
    public void recordMessage(Long userId) {
        DailyStats stats = getTodayStats(userId);
        stats.incrementMessages();
        dailyStatsRepository.save(stats);
    }

    /**
     * Add chat time
     */
    @Transactional
    public void addChatTime(Long userId, int minutes) {
        DailyStats stats = getTodayStats(userId);
        stats.addChatTime(minutes);
        dailyStatsRepository.save(stats);
    }

    /**
     * Record attendance
     */
    @Transactional
    public void recordAttendance(Long userId, int streakCount) {
        DailyStats stats = getTodayStats(userId);
        stats.markAttendance(streakCount);
        dailyStatsRepository.save(stats);

        log.info("Recorded attendance for user {} (Streak: {})", userId, streakCount);
    }

    /**
     * Record mission completion
     */
    @Transactional
    public void recordMissionCompletion(Long userId, int lobcoinReward) {
        DailyStats stats = getTodayStats(userId);
        stats.recordMissionCompletion(lobcoinReward);
        dailyStatsRepository.save(stats);

        log.info("Recorded mission completion for user {} (Reward: {})", userId, lobcoinReward);
    }

    /**
     * Record demand completion
     */
    @Transactional
    public void recordDemandCompletion(Long userId, int lobcoinReward) {
        DailyStats stats = getTodayStats(userId);
        stats.recordDemandCompletion(lobcoinReward);
        dailyStatsRepository.save(stats);
    }

    /**
     * Record demand failure
     */
    @Transactional
    public void recordDemandFailure(Long userId, int lobcoinPenalty) {
        DailyStats stats = getTodayStats(userId);
        stats.recordDemandFailure(lobcoinPenalty);
        dailyStatsRepository.save(stats);
    }

    /**
     * Add LobCoin earned
     */
    @Transactional
    public void addLobcoinEarned(Long userId, int amount) {
        DailyStats stats = getTodayStats(userId);
        stats.addLobcoinEarned(amount);
        dailyStatsRepository.save(stats);
    }

    /**
     * Add LobCoin spent
     */
    @Transactional
    public void addLobcoinSpent(Long userId, int amount) {
        DailyStats stats = getTodayStats(userId);
        stats.addLobcoinSpent(amount);
        dailyStatsRepository.save(stats);
    }

    /**
     * Update user scores in stats
     */
    @Transactional
    public void updateScores(Long userId, Integer trustLevel, Integer affinityScore, Integer resilienceScore) {
        DailyStats stats = getTodayStats(userId);
        stats.updateScores(trustLevel, affinityScore, resilienceScore);
        dailyStatsRepository.save(stats);
    }

    /**
     * Calculate total messages in date range
     */
    @Transactional(readOnly = true)
    public int calculateTotalMessages(Long userId, LocalDate start, LocalDate end) {
        return dailyStatsRepository.sumMessages(userId, start, end);
    }

    /**
     * Calculate total chat time in date range
     */
    @Transactional(readOnly = true)
    public int calculateTotalChatTime(Long userId, LocalDate start, LocalDate end) {
        return dailyStatsRepository.sumChatTime(userId, start, end);
    }

    /**
     * Count attendance days in date range
     */
    @Transactional(readOnly = true)
    public long countAttendanceDays(Long userId, LocalDate start, LocalDate end) {
        return dailyStatsRepository.countAttendanceDays(userId, start, end);
    }

    /**
     * Calculate total missions completed in date range
     */
    @Transactional(readOnly = true)
    public int calculateTotalMissions(Long userId, LocalDate start, LocalDate end) {
        return dailyStatsRepository.sumMissionsCompleted(userId, start, end);
    }

    /**
     * Calculate total LobCoin earned in date range
     */
    @Transactional(readOnly = true)
    public int calculateTotalLobcoinEarned(Long userId, LocalDate start, LocalDate end) {
        return dailyStatsRepository.sumLobcoinEarned(userId, start, end);
    }

    /**
     * Calculate total LobCoin spent in date range
     */
    @Transactional(readOnly = true)
    public int calculateTotalLobcoinSpent(Long userId, LocalDate start, LocalDate end) {
        return dailyStatsRepository.sumLobcoinSpent(userId, start, end);
    }

    /**
     * Get max streak in date range
     */
    @Transactional(readOnly = true)
    public int getMaxStreak(Long userId, LocalDate start, LocalDate end) {
        Integer maxStreak = dailyStatsRepository.findMaxStreak(userId, start, end);
        return maxStreak != null ? maxStreak : 0;
    }

    /**
     * Calculate average messages per day in date range
     */
    @Transactional(readOnly = true)
    public double calculateAverageMessages(Long userId, LocalDate start, LocalDate end) {
        Double avg = dailyStatsRepository.calculateAverageMessages(userId, start, end);
        return avg != null ? avg : 0.0;
    }

    /**
     * Get weekly summary (last 7 days)
     */
    @Transactional(readOnly = true)
    public WeeklySummary getWeeklySummary(Long userId) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        return WeeklySummary.builder()
            .totalMessages(calculateTotalMessages(userId, start, end))
            .totalChatTime(calculateTotalChatTime(userId, start, end))
            .attendanceDays((int) countAttendanceDays(userId, start, end))
            .missionsCompleted(calculateTotalMissions(userId, start, end))
            .lobcoinEarned(calculateTotalLobcoinEarned(userId, start, end))
            .lobcoinSpent(calculateTotalLobcoinSpent(userId, start, end))
            .maxStreak(getMaxStreak(userId, start, end))
            .build();
    }

    /**
     * Create new daily stats record
     */
    private DailyStats createDailyStats(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        DailyStats stats = DailyStats.builder()
            .user(user)
            .statDate(date)
            .build();

        // Initialize with current user scores
        stats.updateScores(
            user.getTrustLevel(),
            null, // Affinity score would need to be fetched
            null  // Resilience score would need to be fetched
        );

        return dailyStatsRepository.save(stats);
    }

    /**
     * Weekly summary DTO
     */
    @lombok.Builder
    @lombok.Getter
    public static class WeeklySummary {
        private int totalMessages;
        private int totalChatTime;
        private int attendanceDays;
        private int missionsCompleted;
        private int lobcoinEarned;
        private int lobcoinSpent;
        private int maxStreak;
    }
}
