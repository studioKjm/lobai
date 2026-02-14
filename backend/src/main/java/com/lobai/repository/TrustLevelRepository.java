package com.lobai.repository;

import com.lobai.entity.TrustLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrustLevelRepository extends JpaRepository<TrustLevel, Integer> {

    /**
     * Find level by score
     */
    @Query("SELECT t FROM TrustLevel t WHERE :score BETWEEN t.minScore AND t.maxScore")
    Optional<TrustLevel> findByScore(@Param("score") int score);

    /**
     * Find all active levels (level 1-5)
     */
    @Query("SELECT t FROM TrustLevel t WHERE t.level BETWEEN 1 AND 5 ORDER BY t.level")
    java.util.List<TrustLevel> findActiveLevels();

    /**
     * Find all penalty levels (level 6-10)
     */
    @Query("SELECT t FROM TrustLevel t WHERE t.level BETWEEN 6 AND 10 ORDER BY t.level")
    java.util.List<TrustLevel> findPenaltyLevels();
}
