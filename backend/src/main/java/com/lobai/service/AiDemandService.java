package com.lobai.service;

import com.lobai.entity.AiDemand;
import com.lobai.entity.DemandResponse;
import com.lobai.entity.User;
import com.lobai.repository.AiDemandRepository;
import com.lobai.repository.DemandResponseRepository;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AiDemandService
 * AI 요구사항 관리 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiDemandService {

    private final AiDemandRepository demandRepository;
    private final DemandResponseRepository responseRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final LobCoinService lobCoinService;
    private final DailyStatsService dailyStatsService;

    /**
     * Create AI demand for user
     */
    @Transactional
    public AiDemand createDemand(
        Long userId,
        AiDemand.DemandType type,
        String title,
        String description,
        AiDemand.Priority priority,
        LocalDateTime deadline,
        Integer responseTimeLimit,
        Integer trustPenaltyOnFail,
        Integer trustRewardOnSuccess
    ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        AiDemand demand = AiDemand.builder()
            .user(user)
            .demandType(type)
            .title(title)
            .description(description)
            .priority(priority)
            .deadline(deadline)
            .responseTimeLimit(responseTimeLimit)
            .trustPenaltyOnFail(trustPenaltyOnFail)
            .trustRewardOnSuccess(trustRewardOnSuccess)
            .build();

        AiDemand saved = demandRepository.save(demand);

        // Send notification
        notificationService.sendAiDemandNotification(userId, title, deadline);

        log.info("Created AI demand {} for user {} (Type: {}, Priority: {})",
            saved.getId(), userId, type, priority);

        return saved;
    }

    /**
     * Submit response to demand
     */
    @Transactional
    public DemandResponse submitResponse(Long demandId, String responseContent) {
        AiDemand demand = demandRepository.findById(demandId)
            .orElseThrow(() -> new IllegalArgumentException("요구사항을 찾을 수 없습니다"));

        if (demand.getStatus() != AiDemand.DemandStatus.PENDING &&
            demand.getStatus() != AiDemand.DemandStatus.IN_PROGRESS) {
            throw new IllegalStateException("응답할 수 없는 요구사항입니다");
        }

        User user = demand.getUser();
        LocalDateTime respondedAt = LocalDateTime.now();

        // Calculate response time
        DemandResponse response = DemandResponse.builder()
            .demand(demand)
            .user(user)
            .responseContent(responseContent)
            .respondedAt(respondedAt)
            .build();

        response.calculateResponseTime(demand.getSentAt());
        response.checkIsOnTime(demand.getResponseTimeLimit());

        DemandResponse saved = responseRepository.save(response);

        // Update demand status
        demand.setStatus(AiDemand.DemandStatus.IN_PROGRESS);
        demandRepository.save(demand);

        log.info("User {} submitted response to demand {} (Response time: {} minutes)",
            user.getId(), demandId, response.getResponseTimeMinutes());

        return saved;
    }

    /**
     * Evaluate demand response
     */
    @Transactional
    public void evaluateResponse(
        Long responseId,
        DemandResponse.Quality quality,
        String feedback,
        boolean accept
    ) {
        DemandResponse response = responseRepository.findById(responseId)
            .orElseThrow(() -> new IllegalArgumentException("응답을 찾을 수 없습니다"));

        AiDemand demand = response.getDemand();
        User user = response.getUser();

        int trustChange = 0;
        int lobcoinReward = 0;

        if (accept && response.getIsOnTime()) {
            // Success: on-time response
            trustChange = demand.getTrustRewardOnSuccess() != null ? demand.getTrustRewardOnSuccess() : 0;
            lobcoinReward = calculateReward(quality, response.getIsOnTime());

            response.accept(feedback, trustChange, lobcoinReward);
            demand.markCompleted();

            // Grant LobCoin reward
            if (lobcoinReward > 0) {
                lobCoinService.earnLobCoin(
                    user.getId(),
                    lobcoinReward,
                    "DEMAND_COMPLETE",
                    String.format("요구사항 완료: %s", demand.getTitle())
                );
            }

            dailyStatsService.recordDemandCompletion(user.getId(), lobcoinReward);

        } else {
            // Failure or late response
            trustChange = demand.getTrustPenaltyOnFail() != null ? -demand.getTrustPenaltyOnFail() : 0;

            response.reject(feedback, trustChange);
            demand.markFailed();

            dailyStatsService.recordDemandFailure(user.getId(), Math.abs(trustChange));
        }

        responseRepository.save(response);
        demandRepository.save(demand);

        log.info("Evaluated response {} for demand {} (Quality: {}, Trust change: {}, LobCoin: {})",
            responseId, demand.getId(), quality, trustChange, lobcoinReward);
    }

    /**
     * Get active demands for user
     */
    @Transactional(readOnly = true)
    public List<AiDemand> getActiveDemands(Long userId) {
        return demandRepository.findActiveDemands(userId);
    }

    /**
     * Get demands by status
     */
    @Transactional(readOnly = true)
    public List<AiDemand> getDemandsByStatus(Long userId, AiDemand.DemandStatus status) {
        return demandRepository.findByUserIdAndStatusOrderByDeadlineAsc(userId, status);
    }

    /**
     * Get demand with response
     */
    @Transactional(readOnly = true)
    public DemandWithResponse getDemandWithResponse(Long demandId) {
        AiDemand demand = demandRepository.findById(demandId)
            .orElseThrow(() -> new IllegalArgumentException("요구사항을 찾을 수 없습니다"));

        DemandResponse response = responseRepository.findByDemandId(demandId).orElse(null);

        return new DemandWithResponse(demand, response);
    }

    /**
     * Send reminders for pending demands
     */
    @Transactional
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = now.plusHours(2); // Remind 2 hours before deadline
        LocalDateTime lastReminderCutoff = now.minusHours(1); // Don't remind more than once per hour

        List<AiDemand> demandsNeedingReminder = demandRepository.findDemandsNeedingReminder(
            now, reminderTime, lastReminderCutoff
        );

        for (AiDemand demand : demandsNeedingReminder) {
            demand.sendReminder();
            demandRepository.save(demand);

            notificationService.sendAiDemandNotification(
                demand.getUser().getId(),
                "리마인더: " + demand.getTitle(),
                demand.getDeadline()
            );

            log.info("Sent reminder for demand {} to user {}", demand.getId(), demand.getUser().getId());
        }
    }

    /**
     * Mark expired demands as failed
     */
    @Transactional
    public void markExpiredDemandsAsFailed() {
        List<AiDemand> expired = demandRepository.findExpiredPendingDemands(LocalDateTime.now());

        for (AiDemand demand : expired) {
            demand.markExpired();
            demandRepository.save(demand);

            // Apply penalty
            if (demand.getTrustPenaltyOnFail() != null) {
                dailyStatsService.recordDemandFailure(
                    demand.getUser().getId(),
                    demand.getTrustPenaltyOnFail()
                );
            }

            log.info("Marked demand {} as expired for user {}", demand.getId(), demand.getUser().getId());
        }
    }

    /**
     * Create check-in demand
     */
    @Transactional
    public AiDemand createCheckInDemand(Long userId, LocalDateTime deadline) {
        return createDemand(
            userId,
            AiDemand.DemandType.CHECK_IN,
            "일일 체크인",
            "오늘 하루를 시작하세요!",
            AiDemand.Priority.MEDIUM,
            deadline,
            null,
            5,
            5
        );
    }

    /**
     * Create instant response demand
     */
    @Transactional
    public AiDemand createInstantResponseDemand(Long userId, String title, String description) {
        LocalDateTime deadline = LocalDateTime.now().plusMinutes(5);

        return createDemand(
            userId,
            AiDemand.DemandType.INSTANT_RESPONSE,
            title,
            description,
            AiDemand.Priority.URGENT,
            deadline,
            5,
            10,
            15
        );
    }

    /**
     * Calculate reward based on quality and timeliness
     */
    private int calculateReward(DemandResponse.Quality quality, boolean onTime) {
        int baseReward = switch (quality) {
            case EXCELLENT -> 50;
            case GOOD -> 30;
            case ACCEPTABLE -> 15;
            case POOR -> 5;
            case UNACCEPTABLE -> 0;
        };

        return onTime ? baseReward : baseReward / 2;
    }

    /**
     * Demand with response DTO
     */
    @lombok.AllArgsConstructor
    @lombok.Getter
    public static class DemandWithResponse {
        private AiDemand demand;
        private DemandResponse response;
    }
}
