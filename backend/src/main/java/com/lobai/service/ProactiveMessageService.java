package com.lobai.service;

import com.lobai.dto.response.MessageResponse;
import com.lobai.entity.*;
import com.lobai.llm.*;
import com.lobai.llm.prompt.PersonaPromptTemplate;
import com.lobai.llm.prompt.PromptContext;
import com.lobai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 선제 대화 (Proactive Messaging) 서비스
 *
 * 채팅 페이지 진입 시 조건을 평가하고,
 * 매칭되는 트리거가 있으면 LLM으로 선제 메시지를 생성한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProactiveMessageService {

    private static final Long LOBBY_MASTER_PERSONA_NAME = null; // resolved by nameEn
    private static final Set<Integer> STREAK_MILESTONES = Set.of(7, 14, 30, 50, 100);
    private static final int STAT_DECLINE_THRESHOLD = 20;
    private static final int LONG_ABSENCE_DAYS = 3;
    private static final int PROMISE_FOLLOWUP_DAYS = 2;

    private final UserRepository userRepository;
    private final PersonaRepository personaRepository;
    private final MessageRepository messageRepository;
    private final ProactiveMessageLogRepository proactiveLogRepository;
    private final UserMemoryRepository userMemoryRepository;
    private final ContextAssemblyService contextAssemblyService;
    private final PersonaPromptTemplate personaPromptTemplate;
    private final LlmRouter llmRouter;
    private final LlmUsageService llmUsageService;

    /**
     * 선제 메시지 생성 (조건 평가 → LLM 생성 → 저장)
     *
     * @return 생성된 메시지 (조건 미충족 시 empty)
     */
    @Transactional
    public Optional<MessageResponse> generateIfNeeded(Long userId) {
        // 1. 사용자 조회 + trustLevel 체크
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getTrustLevel() == null || user.getTrustLevel() < 2) {
            log.debug("Proactive check skipped: user {} (trustLevel < 2)", userId);
            return Optional.empty();
        }

        // 2. 트리거 평가 (우선순위순)
        Optional<TriggerResult> triggerOpt = evaluateTrigger(user);
        if (triggerOpt.isEmpty()) {
            log.debug("No proactive trigger matched for user {}", userId);
            return Optional.empty();
        }

        TriggerResult trigger = triggerOpt.get();
        log.info("Proactive trigger matched for user {}: type={}, detail={}", userId, trigger.getType(), trigger.getDetail());

        // 3. lobby_master 페르소나 조회
        Persona persona = personaRepository.findByNameEn("lobby_master")
                .orElseThrow(() -> new IllegalStateException("lobby_master 페르소나를 찾을 수 없습니다"));

        // 4. 컨텍스트 조립 (경량 - 3000 토큰)
        ContextAssemblyService.AssembledContext context =
                contextAssemblyService.assembleContext(userId, persona, 3000);

        // 5. LLM 호출
        LlmProvider provider = llmRouter.resolve(LlmTaskType.PROACTIVE_MESSAGE);

        PromptContext promptContext = PromptContext.builder()
                .persona(persona)
                .user(user)
                .hunger(user.getCurrentHunger())
                .energy(user.getCurrentEnergy())
                .happiness(user.getCurrentHappiness())
                .trustLevel(user.getTrustLevel())
                .userProfileBlock(context.getUserProfileBlock())
                .conversationSummaryBlock(context.getConversationSummaryBlock())
                .providerName(provider.getProviderName())
                .proactiveTriggerType(trigger.getType())
                .proactiveTriggerDetail(trigger.getDetail())
                .build();

        String systemInstruction = personaPromptTemplate.render(promptContext);

        LlmRequest llmRequest = LlmRequest.builder()
                .systemInstruction(systemInstruction)
                .conversationHistory(context.getRecentMessages())
                .userMessage("(선제 대화 생성 요청 - 사용자에게 먼저 말을 걸어주세요)")
                .taskType(LlmTaskType.PROACTIVE_MESSAGE)
                .build();

        long startTime = System.currentTimeMillis();
        LlmResponse llmResponse;
        try {
            llmResponse = llmRouter.executeWithFallback(LlmTaskType.PROACTIVE_MESSAGE, llmRequest);
        } catch (Exception e) {
            log.error("Proactive message LLM call failed for user {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
        int responseTimeMs = (int) (System.currentTimeMillis() - startTime);

        // 사용량 로깅
        llmUsageService.logUsage(userId, llmResponse, LlmTaskType.PROACTIVE_MESSAGE, responseTimeMs, false);

        String aiText = llmResponse.getContent();
        if (aiText == null || aiText.isBlank()) {
            log.warn("Proactive message LLM returned empty content for user {}", userId);
            return Optional.empty();
        }

        // 6. Message 저장 (messageType=PROACTIVE)
        Message botMessage = Message.builder()
                .user(user)
                .persona(persona)
                .role(Message.MessageRole.bot)
                .content(aiText)
                .messageType("PROACTIVE")
                .llmProvider(llmResponse.getProviderName())
                .llmModel(llmResponse.getModelUsed())
                .tokenCount(llmResponse.getUsage() != null ? llmResponse.getUsage().getTotalTokens() : null)
                .build();
        botMessage = messageRepository.save(botMessage);

        // 7. ProactiveMessageLog 저장 (중복 방지)
        ProactiveMessageLog logEntry = ProactiveMessageLog.builder()
                .user(user)
                .persona(persona)
                .message(botMessage)
                .triggerType(trigger.getType())
                .triggerDetail(trigger.getDetail())
                .triggerDate(LocalDate.now())
                .wasDisplayed(true)
                .build();
        proactiveLogRepository.save(logEntry);

        log.info("Proactive message generated for user {}: trigger={}, messageId={}", userId, trigger.getType(), botMessage.getId());

        return Optional.of(MessageResponse.from(botMessage));
    }

    /**
     * 트리거 평가 (우선순위순, 첫 매칭만 반환)
     */
    private Optional<TriggerResult> evaluateTrigger(User user) {
        Long userId = user.getId();
        LocalDate today = LocalDate.now();

        // 1. FIRST_VISIT_TODAY: 오늘 첫 방문
        if (isFirstVisitToday(userId, today)) {
            return Optional.of(TriggerResult.of("FIRST_VISIT_TODAY", "오늘 첫 접속"));
        }

        // 2. LONG_ABSENCE: 3일 이상 미접속
        Optional<TriggerResult> absenceTrigger = checkLongAbsence(user, today);
        if (absenceTrigger.isPresent()) {
            return absenceTrigger;
        }

        // 3. STREAK_MILESTONE: 연속 출석 마일스톤
        Optional<TriggerResult> streakTrigger = checkStreakMilestone(user, today);
        if (streakTrigger.isPresent()) {
            return streakTrigger;
        }

        // 4. STAT_DECLINE: 상태 저하
        Optional<TriggerResult> statTrigger = checkStatDecline(user, today);
        if (statTrigger.isPresent()) {
            return statTrigger;
        }

        // 5. PROMISE_FOLLOWUP: 약속 확인
        Optional<TriggerResult> promiseTrigger = checkPromiseFollowup(user);
        if (promiseTrigger.isPresent()) {
            return promiseTrigger;
        }

        return Optional.empty();
    }

    /**
     * 오늘 첫 방문 체크
     */
    private boolean isFirstVisitToday(Long userId, LocalDate today) {
        // 오늘 이미 proactive 로그가 있으면 스킵
        if (proactiveLogRepository.existsByUserIdAndTriggerTypeAndTriggerDate(userId, "FIRST_VISIT_TODAY", today)) {
            return false;
        }

        // 오늘 날짜 범위의 메시지가 0건인지 확인
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        List<Message> todayMessages = messageRepository.findByUserIdAndDateRange(userId, startOfDay, endOfDay);

        return todayMessages.isEmpty();
    }

    /**
     * 장기 미접속 체크
     */
    private Optional<TriggerResult> checkLongAbsence(User user, LocalDate today) {
        if (user.getLastCheckInAt() == null) return Optional.empty();

        long daysSinceLastCheckIn = ChronoUnit.DAYS.between(user.getLastCheckInAt().toLocalDate(), today);
        if (daysSinceLastCheckIn < LONG_ABSENCE_DAYS) return Optional.empty();

        // 오늘 이미 LONG_ABSENCE 로그가 있는지 확인
        if (proactiveLogRepository.existsByUserIdAndTriggerTypeAndTriggerDate(user.getId(), "LONG_ABSENCE", today)) {
            return Optional.empty();
        }

        String detail = daysSinceLastCheckIn + "일 미접속";
        return Optional.of(TriggerResult.of("LONG_ABSENCE", detail));
    }

    /**
     * 연속 출석 마일스톤 체크
     */
    private Optional<TriggerResult> checkStreakMilestone(User user, LocalDate today) {
        Integer streak = user.getCurrentStreakDays();
        if (streak == null || !STREAK_MILESTONES.contains(streak)) return Optional.empty();

        String detail = streak + "일 연속 출석";
        if (proactiveLogRepository.existsByUserIdAndTriggerTypeAndTriggerDetail(userId(user), "STREAK_MILESTONE", detail)) {
            return Optional.empty();
        }

        return Optional.of(TriggerResult.of("STREAK_MILESTONE", detail));
    }

    /**
     * 상태 저하 체크
     */
    private Optional<TriggerResult> checkStatDecline(User user, LocalDate today) {
        if (proactiveLogRepository.existsByUserIdAndTriggerTypeAndTriggerDate(userId(user), "STAT_DECLINE", today)) {
            return Optional.empty();
        }

        StringBuilder detail = new StringBuilder();
        int hunger = user.getCurrentHunger() != null ? user.getCurrentHunger() : 50;
        int energy = user.getCurrentEnergy() != null ? user.getCurrentEnergy() : 50;
        int happiness = user.getCurrentHappiness() != null ? user.getCurrentHappiness() : 50;

        if (hunger < STAT_DECLINE_THRESHOLD) detail.append("포만감 ").append(hunger).append("% ");
        if (energy < STAT_DECLINE_THRESHOLD) detail.append("에너지 ").append(energy).append("% ");
        if (happiness < STAT_DECLINE_THRESHOLD) detail.append("행복도 ").append(happiness).append("% ");

        if (detail.length() == 0) return Optional.empty();

        return Optional.of(TriggerResult.of("STAT_DECLINE", detail.toString().trim()));
    }

    /**
     * 약속 후속 확인 체크
     */
    private Optional<TriggerResult> checkPromiseFollowup(User user) {
        List<UserMemory> promises = userMemoryRepository.findByUserIdAndMemoryType(
                userId(user), UserMemory.MemoryType.PROMISE);

        if (promises.isEmpty()) return Optional.empty();

        LocalDateTime cutoff = LocalDateTime.now().minusDays(PROMISE_FOLLOWUP_DAYS);

        for (UserMemory promise : promises) {
            // lastReferencedAt이 null이거나 2일 이상 지난 경우
            if (promise.getLastReferencedAt() == null || promise.getLastReferencedAt().isBefore(cutoff)) {
                String detail = promise.getMemoryKey() + ": " + promise.getMemoryValue();
                // 해당 약속에 대한 로그가 이미 있는지 확인
                if (!proactiveLogRepository.existsByUserIdAndTriggerTypeAndTriggerDetail(
                        userId(user), "PROMISE_FOLLOWUP", detail)) {
                    return Optional.of(TriggerResult.of("PROMISE_FOLLOWUP", detail));
                }
            }
        }

        return Optional.empty();
    }

    private Long userId(User user) {
        return user.getId();
    }
}
