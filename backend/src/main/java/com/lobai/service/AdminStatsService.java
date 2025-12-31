package com.lobai.service;

import com.lobai.dto.response.admin.*;
import com.lobai.repository.MessageRepository;
import com.lobai.repository.PersonaRepository;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin Statistics Service
 *
 * 관리자 통계 조회 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminStatsService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final PersonaRepository personaRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 전체 통계 개요 조회
     */
    public StatsOverviewResponse getStatsOverview() {
        log.info("Fetching stats overview");

        // 현재 시각 기준 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.minusDays(7);
        LocalDateTime monthStart = now.minusDays(30);

        // 사용자 통계
        Long totalUsers = userRepository.count();
        Long activeUsersToday = userRepository.countByLastLoginAtAfter(todayStart);
        Long newUsersThisWeek = userRepository.countByCreatedAtAfter(weekStart);
        Long newUsersThisMonth = userRepository.countByCreatedAtAfter(monthStart);

        // 메시지 통계
        Long totalMessages = messageRepository.count();
        Long messagesToday = messageRepository.countByCreatedAtAfter(todayStart);
        Long messagesThisWeek = messageRepository.countByCreatedAtAfter(weekStart);
        Long messagesThisMonth = messageRepository.countByCreatedAtAfter(monthStart);
        Double avgMessagesPerUser = totalUsers > 0 ? (double) totalMessages / totalUsers : 0.0;

        // Stats 평균값 (활성 사용자만)
        Double avgHunger = userRepository.getAverageHungerOfActiveUsers().orElse(0.0);
        Double avgEnergy = userRepository.getAverageEnergyOfActiveUsers().orElse(0.0);
        Double avgHappiness = userRepository.getAverageHappinessOfActiveUsers().orElse(0.0);

        // 가장 인기있는 페르소나
        var mostPopularPersona = messageRepository.findMostPopularPersona();
        String mostPopularPersonaName = mostPopularPersona.map(p -> p[1].toString()).orElse("N/A");
        Long mostPopularPersonaCount = mostPopularPersona.map(p -> ((Number) p[2]).longValue()).orElse(0L);

        return StatsOverviewResponse.builder()
                .totalUsers(totalUsers)
                .activeUsersToday(activeUsersToday)
                .newUsersThisWeek(newUsersThisWeek)
                .newUsersThisMonth(newUsersThisMonth)
                .totalMessages(totalMessages)
                .messagesToday(messagesToday)
                .messagesThisWeek(messagesThisWeek)
                .messagesThisMonth(messagesThisMonth)
                .avgMessagesPerUser(Math.round(avgMessagesPerUser * 100.0) / 100.0)
                .avgHunger(Math.round(avgHunger * 100.0) / 100.0)
                .avgEnergy(Math.round(avgEnergy * 100.0) / 100.0)
                .avgHappiness(Math.round(avgHappiness * 100.0) / 100.0)
                .mostPopularPersona(mostPopularPersonaName)
                .mostPopularPersonaCount(mostPopularPersonaCount)
                .build();
    }

    /**
     * 활성 사용자 차트 데이터 조회 (최근 30일)
     */
    public ActiveUsersChartResponse getActiveUsersChart() {
        log.info("Fetching active users chart data");

        LocalDate today = LocalDate.now();
        List<ActiveUsersChartResponse.DataPoint> dailyActiveUsers = new ArrayList<>();
        List<ActiveUsersChartResponse.DataPoint> newUserSignups = new ArrayList<>();

        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            Long activeCount = userRepository.countByLastLoginAtBetween(dayStart, dayEnd);
            Long newCount = userRepository.countByCreatedAtBetween(dayStart, dayEnd);

            dailyActiveUsers.add(ActiveUsersChartResponse.DataPoint.builder()
                    .date(date.format(DATE_FORMATTER))
                    .count(activeCount)
                    .build());

            newUserSignups.add(ActiveUsersChartResponse.DataPoint.builder()
                    .date(date.format(DATE_FORMATTER))
                    .count(newCount)
                    .build());
        }

        return ActiveUsersChartResponse.builder()
                .dailyActiveUsers(dailyActiveUsers)
                .newUserSignups(newUserSignups)
                .build();
    }

    /**
     * 메시지 통계 조회
     */
    public MessageStatsResponse getMessageStats() {
        log.info("Fetching message statistics");

        Long totalMessages = messageRepository.count();
        Long userMessages = messageRepository.countByRole("user");
        Long botMessages = messageRepository.countByRole("bot");
        Long totalUsers = userRepository.count();
        Double avgMessagesPerUser = totalUsers > 0 ? (double) totalMessages / totalUsers : 0.0;

        // 일평균 메시지 수 (최근 30일 기준)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Long messagesLast30Days = messageRepository.countByCreatedAtAfter(thirtyDaysAgo);
        Double avgMessagesPerDay = messagesLast30Days / 30.0;

        // 일별 메시지 수 (최근 30일)
        LocalDate today = LocalDate.now();
        List<MessageStatsResponse.DailyMessageCount> dailyMessages = new ArrayList<>();

        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            Long userMsgCount = messageRepository.countByRoleAndCreatedAtBetween("user", dayStart, dayEnd);
            Long botMsgCount = messageRepository.countByRoleAndCreatedAtBetween("bot", dayStart, dayEnd);

            dailyMessages.add(MessageStatsResponse.DailyMessageCount.builder()
                    .date(date.format(DATE_FORMATTER))
                    .userMessages(userMsgCount)
                    .botMessages(botMsgCount)
                    .total(userMsgCount + botMsgCount)
                    .build());
        }

        return MessageStatsResponse.builder()
                .totalMessages(totalMessages)
                .userMessages(userMessages)
                .botMessages(botMessages)
                .avgMessagesPerUser(Math.round(avgMessagesPerUser * 100.0) / 100.0)
                .avgMessagesPerDay(Math.round(avgMessagesPerDay * 100.0) / 100.0)
                .dailyMessages(dailyMessages)
                .build();
    }

    /**
     * 페르소나별 사용 통계 조회
     */
    public PersonaStatsResponse getPersonaStats() {
        log.info("Fetching persona statistics");

        Long totalMessages = messageRepository.count();

        // 모든 페르소나 조회 후 각각의 통계 계산
        List<PersonaStatsResponse.PersonaUsageStat> personaUsage = personaRepository.findAll().stream()
                .map(persona -> {
                    Long messageCount = messageRepository.countByPersonaId(persona.getId());
                    Long currentUserCount = userRepository.countByCurrentPersonaId(persona.getId());
                    Double usagePercentage = totalMessages > 0
                            ? (messageCount * 100.0 / totalMessages)
                            : 0.0;

                    return PersonaStatsResponse.PersonaUsageStat.builder()
                            .personaId(persona.getId())
                            .personaName(persona.getName())
                            .displayName(persona.getDisplayName())
                            .iconEmoji(persona.getIconEmoji())
                            .messageCount(messageCount)
                            .currentUserCount(currentUserCount)
                            .usagePercentage(Math.round(usagePercentage * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());

        return PersonaStatsResponse.builder()
                .personaUsage(personaUsage)
                .build();
    }
}
