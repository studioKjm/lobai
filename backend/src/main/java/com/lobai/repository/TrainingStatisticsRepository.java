package com.lobai.repository;

import com.lobai.entity.TrainingStatistics;
import com.lobai.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingStatisticsRepository extends JpaRepository<TrainingStatistics, Long> {

    /**
     * 사용자 ID로 통계 조회
     */
    List<TrainingStatistics> findByUserId(Long userId);

    /**
     * 사용자 ID와 훈련 유형으로 통계 조회
     */
    Optional<TrainingStatistics> findByUserIdAndTrainingType(
        Long userId,
        TrainingSession.TrainingType trainingType
    );

    /**
     * 사용자의 모든 통계 조회
     */
    List<TrainingStatistics> findByUserIdOrderByTrainingTypeAsc(Long userId);
}
