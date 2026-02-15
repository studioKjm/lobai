package com.lobai.repository;

import com.lobai.entity.ProactiveMessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ProactiveMessageLogRepository extends JpaRepository<ProactiveMessageLog, Long> {

    /**
     * 특정 유저의 특정 날짜, 특정 트리거 타입 로그 존재 여부 (중복 방지)
     */
    boolean existsByUserIdAndTriggerTypeAndTriggerDate(Long userId, String triggerType, LocalDate triggerDate);

    /**
     * 특정 유저의 특정 트리거 타입 + 상세 조건 로그 존재 여부 (마일스톤 등)
     */
    boolean existsByUserIdAndTriggerTypeAndTriggerDetail(Long userId, String triggerType, String triggerDetail);

    /**
     * 특정 유저의 마지막 선제 메시지 조회
     */
    Optional<ProactiveMessageLog> findTopByUserIdOrderByGeneratedAtDesc(Long userId);
}
