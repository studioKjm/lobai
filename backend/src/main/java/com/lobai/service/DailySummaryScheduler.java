package com.lobai.service;

import com.lobai.entity.User;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 일일 대화 요약 스케줄러
 *
 * 매일 00:05에 전일 대화를 LLM으로 요약 저장
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DailySummaryScheduler {

    private final ConversationSummaryService conversationSummaryService;
    private final UserRepository userRepository;

    /**
     * 매일 00:05 실행 - 전일 대화 요약 생성
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void generateDailySummaries() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("=== Starting daily conversation summary generation for {} ===", yesterday);

        try {
            List<User> activeUsers = userRepository.findByIsActiveTrue();
            log.info("Found {} active users for daily summary", activeUsers.size());

            if (activeUsers.isEmpty()) {
                log.info("No active users found, skipping daily summary generation");
                return;
            }

            int successCount = 0;
            int skipCount = 0;
            int failCount = 0;

            for (User user : activeUsers) {
                try {
                    boolean generated = conversationSummaryService.generateDailySummary(user, yesterday);
                    if (generated) {
                        successCount++;
                    } else {
                        skipCount++;
                    }

                    // API Rate Limit 고려하여 200ms 대기
                    Thread.sleep(200);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Daily summary generation interrupted", e);
                    break;
                } catch (Exception e) {
                    log.error("Failed to generate daily summary for user {}", user.getId(), e);
                    failCount++;
                }
            }

            log.info("=== Daily summary generation completed for {}: {} success, {} skipped, {} failed ===",
                    yesterday, successCount, skipCount, failCount);

        } catch (Exception e) {
            log.error("Daily summary generation failed for {}", yesterday, e);
        }
    }
}
