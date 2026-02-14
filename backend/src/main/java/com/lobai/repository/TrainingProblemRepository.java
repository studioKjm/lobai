package com.lobai.repository;

import com.lobai.entity.TrainingProblem;
import com.lobai.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingProblemRepository extends JpaRepository<TrainingProblem, Long> {

    /**
     * 훈련 유형으로 문제 조회 (활성화된 것만)
     */
    List<TrainingProblem> findByTrainingTypeAndIsActiveOrderByDifficultyLevel(
        TrainingSession.TrainingType trainingType,
        Boolean isActive
    );

    /**
     * 훈련 유형과 난이도로 문제 조회
     */
    List<TrainingProblem> findByTrainingTypeAndDifficultyLevelAndIsActive(
        TrainingSession.TrainingType trainingType,
        Integer difficultyLevel,
        Boolean isActive
    );

    /**
     * 랜덤 문제 조회 (난이도와 유형)
     */
    @Query(value = "SELECT * FROM training_problems " +
           "WHERE training_type = :trainingType " +
           "AND difficulty_level = :difficultyLevel " +
           "AND is_active = true " +
           "ORDER BY RAND() LIMIT 1",
           nativeQuery = true)
    Optional<TrainingProblem> findRandomProblem(
        @Param("trainingType") String trainingType,
        @Param("difficultyLevel") Integer difficultyLevel
    );

    /**
     * 가장 적게 사용된 문제 조회
     */
    @Query("SELECT p FROM TrainingProblem p " +
           "WHERE p.trainingType = :trainingType " +
           "AND p.difficultyLevel = :difficultyLevel " +
           "AND p.isActive = true " +
           "ORDER BY p.usageCount ASC")
    List<TrainingProblem> findLeastUsedProblems(
        @Param("trainingType") TrainingSession.TrainingType trainingType,
        @Param("difficultyLevel") Integer difficultyLevel
    );
}
