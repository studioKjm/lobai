package com.lobai.service;

import com.lobai.dto.request.CreateScheduleRequest;
import com.lobai.dto.request.UpdateScheduleRequest;
import com.lobai.dto.response.ScheduleResponse;
import com.lobai.entity.Schedule;
import com.lobai.entity.User;
import com.lobai.repository.ScheduleRepository;
import com.lobai.repository.UserRepository;
import com.lobai.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final LobCoinService lobCoinService;

    /**
     * Create a new schedule
     */
    @Transactional
    public ScheduleResponse createSchedule(CreateScheduleRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Schedule schedule = Schedule.builder()
            .user(user)
            .title(request.getTitle())
            .description(request.getDescription())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .type(request.getType())
            .timezone(request.getTimezone() != null ? request.getTimezone() : "Asia/Seoul")
            .notifyBeforeMinutes(request.getNotifyBeforeMinutes() != null ? request.getNotifyBeforeMinutes() : 0)
            .build();

        schedule.validateTimeRange();
        Schedule saved = scheduleRepository.save(schedule);

        log.info("Schedule created: {} for user {}", saved.getId(), userId);
        return toScheduleResponse(saved);
    }

    /**
     * Get schedules by date range
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesByDateRange(LocalDateTime start, LocalDateTime end) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<Schedule> schedules = scheduleRepository
            .findByUserIdAndStartTimeBetweenAndIsDeletedFalse(userId, start, end);

        log.info("Found {} schedules for user {} between {} and {}", schedules.size(), userId, start, end);
        return schedules.stream()
            .map(this::toScheduleResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get today's schedules
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getTodaysSchedules() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return getSchedulesByDateRange(start, end);
    }

    /**
     * Get upcoming schedules (next N schedules)
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getUpcomingSchedules(int limit) {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();
        List<Schedule> schedules = scheduleRepository.findUpcomingSchedules(
            userId,
            now,
            PageRequest.of(0, limit)
        );

        return schedules.stream()
            .map(this::toScheduleResponse)
            .collect(Collectors.toList());
    }

    /**
     * Update schedule
     */
    @Transactional
    public ScheduleResponse updateSchedule(Long id, UpdateScheduleRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다"));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new IllegalStateException("권한이 없습니다");
        }

        // Track completion status change for reward
        boolean wasCompleted = schedule.getIsCompleted();
        boolean isNowCompleted = false;

        if (request.getTitle() != null) {
            schedule.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            schedule.setDescription(request.getDescription());
        }
        if (request.getStartTime() != null) {
            schedule.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            schedule.setEndTime(request.getEndTime());
        }
        if (request.getType() != null) {
            schedule.setType(request.getType());
        }
        if (request.getIsCompleted() != null) {
            schedule.setIsCompleted(request.getIsCompleted());
            isNowCompleted = request.getIsCompleted();
        }
        if (request.getNotifyBeforeMinutes() != null) {
            schedule.setNotifyBeforeMinutes(request.getNotifyBeforeMinutes());
        }

        schedule.validateTimeRange();
        Schedule saved = scheduleRepository.save(schedule);

        // Reward for completing a schedule (20 LobCoin)
        if (!wasCompleted && isNowCompleted) {
            try {
                lobCoinService.earnLobCoin(
                    userId,
                    20,
                    "MISSION_COMPLETE",
                    String.format("일정 완료: %s", schedule.getTitle())
                );
                log.info("Mission completion reward (20 LobCoin) given to user {} for schedule {}",
                         userId, id);
            } catch (Exception e) {
                log.warn("Failed to give mission completion reward: {}", e.getMessage());
            }
        }

        log.info("Schedule updated: {}", id);
        return toScheduleResponse(saved);
    }

    /**
     * Delete schedule (soft delete)
     */
    @Transactional
    public void deleteSchedule(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다"));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new IllegalStateException("권한이 없습니다");
        }

        schedule.setIsDeleted(true);
        scheduleRepository.save(schedule);
        log.info("Schedule soft deleted: {}", id);
    }

    /**
     * Convert Schedule entity to ScheduleResponse DTO
     */
    private ScheduleResponse toScheduleResponse(Schedule schedule) {
        return ScheduleResponse.builder()
            .id(schedule.getId())
            .title(schedule.getTitle())
            .description(schedule.getDescription())
            .startTime(schedule.getStartTime().toString())
            .endTime(schedule.getEndTime().toString())
            .type(schedule.getType().name())
            .isCompleted(schedule.getIsCompleted())
            .timezone(schedule.getTimezone())
            .notifyBeforeMinutes(schedule.getNotifyBeforeMinutes())
            .createdAt(schedule.getCreatedAt().toString())
            .build();
    }
}
