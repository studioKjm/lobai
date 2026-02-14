package com.lobai.service;

import com.lobai.entity.*;
import com.lobai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * ReportGenerationService
 * 주간/월간 보고서 자동 생성 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportGenerationService {

    private final WeeklyReportRepository weeklyReportRepository;
    private final MonthlyReportRepository monthlyReportRepository;
    private final UserRepository userRepository;
    private final DailyStatsService dailyStatsService;
    private final AffinityScoreRepository affinityScoreRepository;
    private final ResilienceReportRepository resilienceReportRepository;
    private final LevelHistoryRepository levelHistoryRepository;
    private final NotificationService notificationService;

    /**
     * Generate weekly report for user
     */
    @Transactional
    public WeeklyReport generateWeeklyReport(Long userId) {
        // Get last week's date range
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        // Check if report already exists
        if (weeklyReportRepository.existsByUserIdAndWeekStartDate(userId, weekStart)) {
            log.warn("Weekly report already exists for user {} (week starting {})", userId, weekStart);
            return weeklyReportRepository.findByUserIdAndWeekStartDate(userId, weekStart).orElse(null);
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // Gather statistics
        int totalMessages = dailyStatsService.calculateTotalMessages(userId, weekStart, weekEnd);
        int totalChatTime = dailyStatsService.calculateTotalChatTime(userId, weekStart, weekEnd);
        long attendanceDays = dailyStatsService.countAttendanceDays(userId, weekStart, weekEnd);
        int missionsCompleted = dailyStatsService.calculateTotalMissions(userId, weekStart, weekEnd);
        int lobcoinEarned = dailyStatsService.calculateTotalLobcoinEarned(userId, weekStart, weekEnd);
        int lobcoinSpent = dailyStatsService.calculateTotalLobcoinSpent(userId, weekStart, weekEnd);

        // Get trust level at start and end of week
        DailyStats startStats = dailyStatsService.getStatsByDate(userId, weekStart);
        DailyStats endStats = dailyStatsService.getStatsByDate(userId, weekEnd);

        Integer trustLevelStart = startStats != null ? startStats.getTrustLevel() : user.getTrustLevel();
        Integer trustLevelEnd = endStats != null ? endStats.getTrustLevel() : user.getTrustLevel();

        // Get affinity score change
        AffinityScore affinityScore = affinityScoreRepository.findByUserId(userId).orElse(null);
        Integer affinityScoreChange = 0; // Would need historical data

        // Generate AI feedback
        String aiFeedback = generateAiFeedback(
            totalMessages, attendanceDays, missionsCompleted, trustLevelStart, trustLevelEnd
        );

        WeeklyReport report = WeeklyReport.builder()
            .user(user)
            .weekStartDate(weekStart)
            .weekEndDate(weekEnd)
            .totalMessages(totalMessages)
            .totalChatTimeMinutes(totalChatTime)
            .attendanceDays((int) attendanceDays)
            .missionsCompleted(missionsCompleted)
            .lobcoinEarned(lobcoinEarned)
            .lobcoinSpent(lobcoinSpent)
            .trustLevelStart(trustLevelStart)
            .trustLevelEnd(trustLevelEnd)
            .affinityScoreChange(affinityScoreChange)
            .aiFeedback(aiFeedback)
            .build();

        WeeklyReport saved = weeklyReportRepository.save(report);

        // Send notification
        notificationService.sendReportReadyNotification(userId, "주간");

        log.info("Generated weekly report for user {} (week {})", userId, weekStart);
        return saved;
    }

    /**
     * Generate monthly report for user
     */
    @Transactional
    public MonthlyReport generateMonthlyReport(Long userId) {
        // Get last month
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        String reportMonth = lastMonth.toString(); // Format: "YYYY-MM"

        // Check if report already exists
        if (monthlyReportRepository.existsByUserIdAndReportMonth(userId, reportMonth)) {
            log.warn("Monthly report already exists for user {} (month {})", userId, reportMonth);
            return monthlyReportRepository.findByUserIdAndReportMonth(userId, reportMonth).orElse(null);
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        LocalDate monthStart = lastMonth.atDay(1);
        LocalDate monthEnd = lastMonth.atEndOfMonth();

        // Gather statistics
        int totalMessages = dailyStatsService.calculateTotalMessages(userId, monthStart, monthEnd);
        int totalChatTime = dailyStatsService.calculateTotalChatTime(userId, monthStart, monthEnd);
        long attendanceDays = dailyStatsService.countAttendanceDays(userId, monthStart, monthEnd);
        int maxStreak = dailyStatsService.getMaxStreak(userId, monthStart, monthEnd);
        int missionsCompleted = dailyStatsService.calculateTotalMissions(userId, monthStart, monthEnd);
        int lobcoinEarned = dailyStatsService.calculateTotalLobcoinEarned(userId, monthStart, monthEnd);
        int lobcoinSpent = dailyStatsService.calculateTotalLobcoinSpent(userId, monthStart, monthEnd);

        // Get trust level changes
        List<LevelHistory> levelChanges = levelHistoryRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            userId, monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59)
        );

        DailyStats startStats = dailyStatsService.getStatsByDate(userId, monthStart);
        DailyStats endStats = dailyStatsService.getStatsByDate(userId, monthEnd);

        Integer trustLevelStart = startStats != null ? startStats.getTrustLevel() : user.getTrustLevel();
        Integer trustLevelEnd = endStats != null ? endStats.getTrustLevel() : user.getTrustLevel();

        // Get affinity and resilience scores
        AffinityScore affinityScore = affinityScoreRepository.findByUserId(userId).orElse(null);
        Integer affinityScoreStart = 0; // Would need historical data
        Integer affinityScoreEnd = affinityScore != null ? affinityScore.getTotalScore() : 0;

        // Get latest resilience score
        ResilienceReport latestResilience = resilienceReportRepository.findLatestByUserId(userId).orElse(null);
        Integer resilienceScore = latestResilience != null ? latestResilience.getResilienceScore() : 0;

        // Generate AI summary
        String aiSummary = generateMonthlyAiSummary(
            totalMessages, attendanceDays, missionsCompleted, trustLevelStart, trustLevelEnd
        );

        MonthlyReport report = MonthlyReport.builder()
            .user(user)
            .reportMonth(reportMonth)
            .totalMessages(totalMessages)
            .totalChatTimeMinutes(totalChatTime)
            .attendanceDays((int) attendanceDays)
            .maxStreakDays(maxStreak)
            .missionsCompleted(missionsCompleted)
            .lobcoinEarned(lobcoinEarned)
            .lobcoinSpent(lobcoinSpent)
            .trustLevelStart(trustLevelStart)
            .trustLevelEnd(trustLevelEnd)
            .trustLevelChanges(levelChanges.size())
            .affinityScoreStart(affinityScoreStart)
            .affinityScoreEnd(affinityScoreEnd)
            .resilienceScore(resilienceScore)
            .aiSummary(aiSummary)
            .build();

        MonthlyReport saved = monthlyReportRepository.save(report);

        // Send notification
        notificationService.sendReportReadyNotification(userId, "월간");

        log.info("Generated monthly report for user {} (month {})", userId, reportMonth);
        return saved;
    }

    /**
     * Generate weekly reports for all active users
     */
    @Transactional
    public void generateAllWeeklyReports() {
        List<User> activeUsers = userRepository.findAll().stream()
            .filter(User::getIsActive)
            .toList();

        int successCount = 0;
        for (User user : activeUsers) {
            try {
                generateWeeklyReport(user.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate weekly report for user {}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("Generated weekly reports: {} success, {} total users", successCount, activeUsers.size());
    }

    /**
     * Generate monthly reports for all active users
     */
    @Transactional
    public void generateAllMonthlyReports() {
        List<User> activeUsers = userRepository.findAll().stream()
            .filter(User::getIsActive)
            .toList();

        int successCount = 0;
        for (User user : activeUsers) {
            try {
                generateMonthlyReport(user.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to generate monthly report for user {}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("Generated monthly reports: {} success, {} total users", successCount, activeUsers.size());
    }

    /**
     * Get weekly report for user
     */
    @Transactional(readOnly = true)
    public WeeklyReport getWeeklyReport(Long userId, LocalDate weekStartDate) {
        return weeklyReportRepository.findByUserIdAndWeekStartDate(userId, weekStartDate)
            .orElse(null);
    }

    /**
     * Get latest weekly report
     */
    @Transactional(readOnly = true)
    public WeeklyReport getLatestWeeklyReport(Long userId) {
        return weeklyReportRepository.findLatestByUserId(userId).orElse(null);
    }

    /**
     * Get monthly report for user
     */
    @Transactional(readOnly = true)
    public MonthlyReport getMonthlyReport(Long userId, String reportMonth) {
        return monthlyReportRepository.findByUserIdAndReportMonth(userId, reportMonth)
            .orElse(null);
    }

    /**
     * Get latest monthly report
     */
    @Transactional(readOnly = true)
    public MonthlyReport getLatestMonthlyReport(Long userId) {
        return monthlyReportRepository.findLatestByUserId(userId).orElse(null);
    }

    /**
     * Generate AI feedback for weekly report
     */
    private String generateAiFeedback(
        int messages,
        long attendance,
        int missions,
        Integer trustLevelStart,
        Integer trustLevelEnd
    ) {
        StringBuilder feedback = new StringBuilder();

        if (attendance >= 7) {
            feedback.append("완벽한 출석! 훌륭합니다. ");
        } else if (attendance >= 5) {
            feedback.append("좋은 출석률을 보여주셨네요. ");
        } else {
            feedback.append("출석률을 높여보세요. ");
        }

        if (missions >= 5) {
            feedback.append("미션 완료도 대단합니다! ");
        } else if (missions > 0) {
            feedback.append("미션을 더 많이 도전해보세요. ");
        }

        if (trustLevelEnd != null && trustLevelStart != null && trustLevelEnd > trustLevelStart) {
            feedback.append("레벨이 상승했습니다! 계속 이대로 유지하세요.");
        } else if (trustLevelEnd != null && trustLevelStart != null && trustLevelEnd < trustLevelStart) {
            feedback.append("레벨이 하락했습니다. 더 열심히 참여해주세요.");
        }

        return feedback.toString();
    }

    /**
     * Generate AI summary for monthly report
     */
    private String generateMonthlyAiSummary(
        int messages,
        long attendance,
        int missions,
        Integer trustLevelStart,
        Integer trustLevelEnd
    ) {
        return String.format(
            "이번 달 총 %d개의 메시지를 보냈고, %d일 출석하셨으며, %d개의 미션을 완료하셨습니다. " +
            "레벨은 %d에서 %d로 변경되었습니다.",
            messages, attendance, missions,
            trustLevelStart != null ? trustLevelStart : 1,
            trustLevelEnd != null ? trustLevelEnd : 1
        );
    }
}
