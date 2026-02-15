package com.lobai.service;

import com.lobai.entity.AffinityScore;
import com.lobai.entity.AffinityScoreHistory;
import com.lobai.repository.AffinityScoreHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * AffinityHistoryService
 *
 * 친밀도 점수 일별 스냅샷 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AffinityHistoryService {

    private final AffinityScoreHistoryRepository historyRepository;

    /**
     * 오늘 날짜로 스냅샷 저장 (이미 있으면 업데이트)
     */
    @Transactional
    public void saveOrUpdateDailySnapshot(AffinityScore score) {
        LocalDate today = LocalDate.now();

        historyRepository.findByUserIdAndSnapshotDate(score.getUserId(), today)
                .ifPresentOrElse(
                        existing -> {
                            // 오늘 스냅샷이 이미 있으면 무시 (하루 한 번만)
                            log.debug("Daily snapshot already exists: userId={}, date={}", score.getUserId(), today);
                        },
                        () -> {
                            AffinityScoreHistory snapshot = AffinityScoreHistory.fromAffinityScore(score);
                            historyRepository.save(snapshot);
                            log.info("Daily affinity snapshot saved: userId={}, score={}", score.getUserId(), score.getOverallScore());
                        }
                );
    }

    /**
     * 최근 N일간 트렌드 데이터 조회
     */
    @Transactional(readOnly = true)
    public List<AffinityScoreHistory> getTrend(Long userId, int days) {
        LocalDate since = LocalDate.now().minusDays(days);
        return historyRepository.findRecentHistory(userId, since);
    }
}
