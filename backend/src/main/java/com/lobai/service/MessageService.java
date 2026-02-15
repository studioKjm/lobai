package com.lobai.service;

import com.lobai.dto.request.SendMessageRequest;
import com.lobai.dto.response.ChatResponse;
import com.lobai.dto.response.MessageResponse;
import com.lobai.dto.response.StatsResponse;
import com.lobai.entity.Message;
import com.lobai.entity.Persona;
import com.lobai.entity.User;
import com.lobai.entity.UserStatsHistory;
import com.lobai.llm.*;
import com.lobai.llm.prompt.PersonaPromptTemplate;
import com.lobai.llm.prompt.PromptContext;
import com.lobai.repository.MessageRepository;
import com.lobai.repository.PersonaRepository;
import com.lobai.repository.UserRepository;
import com.lobai.repository.UserStatsHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MessageService
 *
 * 메시지 및 채팅 관련 비즈니스 로직
 * → ContextAssemblyService + LlmRouter + PromptTemplate 통합
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final PersonaRepository personaRepository;
    private final UserStatsHistoryRepository userStatsHistoryRepository;
    private final GeminiService geminiService;
    private final ScheduleService scheduleService;
    private final AffinityScoreService affinityScoreService;
    private final FileStorageService fileStorageService;
    private final LobCoinService lobCoinService;
    private final ContextAssemblyService contextAssemblyService;
    private final ConversationSummaryService conversationSummaryService;
    private final PersonaPromptTemplate personaPromptTemplate;
    private final LlmRouter llmRouter;
    private final LlmUsageService llmUsageService;
    private final LevelService levelService;

    /**
     * 메시지 전송 및 AI 응답 생성
     */
    @Transactional
    public ChatResponse sendMessage(Long userId, SendMessageRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 2. 페르소나 결정
        Persona persona = resolvePersona(user, request.getPersonaId());

        // 2-1. 일일 토큰 제한 확인
        if (llmUsageService.isOverDailyLimit(userId)) {
            throw new IllegalStateException("일일 AI 사용량 제한을 초과했습니다. 내일 다시 이용해 주세요.");
        }

        // 3. 3계층 컨텍스트 조립 (기존 6개 고정 → 동적 토큰 예산)
        ContextAssemblyService.AssembledContext context =
                contextAssemblyService.assembleContext(userId, persona, 6000);

        // 4. 사용자 메시지 저장
        Message userMessage = Message.builder()
                .user(user)
                .persona(persona)
                .role(Message.MessageRole.user)
                .content(request.getContent())
                .build();
        userMessage = messageRepository.save(userMessage);

        // 4-1. 친밀도 점수 분석
        try {
            affinityScoreService.analyzeAndUpdateScore(userMessage);
        } catch (Exception e) {
            log.warn("Affinity score analysis failed, continuing: userId={}, error={}",
                    userId, e.getMessage());
        }

        // 4-2. 일일 첫 체크인 보상
        try {
            checkAndRewardDailyCheckIn(userId);
        } catch (Exception e) {
            log.warn("Daily check-in reward failed, continuing: userId={}, error={}",
                    userId, e.getMessage());
        }

        // 4-3. 경험치 지급 (채팅 메시지당 5 XP)
        try {
            levelService.addExperience(userId, 5, "채팅 메시지");
        } catch (Exception e) {
            log.warn("XP reward failed, continuing: userId={}, error={}", userId, e.getMessage());
        }

        // 5. 프롬프트 생성 + LLM 호출
        LlmProvider provider = llmRouter.resolve(LlmTaskType.CHAT_CONVERSATION);

        // 오늘 일정 블록 생성
        String todayScheduleBlock = buildTodayScheduleBlock(userId);

        PromptContext promptContext = PromptContext.builder()
                .persona(persona)
                .user(user)
                .hunger(user.getCurrentHunger())
                .energy(user.getCurrentEnergy())
                .happiness(user.getCurrentHappiness())
                .trustLevel(user.getTrustLevel())
                .userProfileBlock(context.getUserProfileBlock())
                .conversationSummaryBlock(context.getConversationSummaryBlock())
                .todayScheduleBlock(todayScheduleBlock)
                .providerName(provider.getProviderName())
                .build();

        String systemInstruction = personaPromptTemplate.render(promptContext);

        LlmRequest llmRequest = LlmRequest.builder()
                .systemInstruction(systemInstruction)
                .conversationHistory(context.getRecentMessages())
                .userMessage(request.getContent())
                .tools(geminiService.buildFunctionDeclarations())
                .taskType(LlmTaskType.CHAT_CONVERSATION)
                .build();

        long startTime = System.currentTimeMillis();
        LlmResponse llmResponse = llmRouter.executeWithFallback(LlmTaskType.CHAT_CONVERSATION, llmRequest);
        int responseTimeMs = (int) (System.currentTimeMillis() - startTime);

        // 사용량 로깅 (비동기)
        llmUsageService.logUsage(userId, llmResponse, LlmTaskType.CHAT_CONVERSATION, responseTimeMs, false);

        String aiResponseText = llmResponse.getContent() != null ? llmResponse.getContent()
                : "죄송해요, 지금 제 머리가 좀 복잡해서 답변이 어려워요.";

        // Function Call 처리
        if (llmResponse.hasFunctionCall()) {
            String functionName = llmResponse.getFunctionCall().getName();
            String argsJson = llmResponse.getFunctionCall().getArgsJson();
            log.info("Function call detected: {} with args: {}", functionName, argsJson);
            aiResponseText = geminiService.handleFunctionCall(llmRequest, functionName, argsJson, userId);
        }

        // 6. AI 응답 저장 (LLM 메타데이터 포함)
        Message botMessage = Message.builder()
                .user(user)
                .persona(persona)
                .role(Message.MessageRole.bot)
                .content(aiResponseText)
                .llmProvider(llmResponse.getProviderName())
                .llmModel(llmResponse.getModelUsed())
                .tokenCount(llmResponse.getUsage() != null ? llmResponse.getUsage().getTotalTokens() : null)
                .build();
        botMessage = messageRepository.save(botMessage);

        // 7. Stats 업데이트
        Integer newHappiness = user.getCurrentHappiness() + 2;
        user.updateStats(null, null, newHappiness);
        userRepository.save(user);

        // 8. Stats 히스토리 기록
        UserStatsHistory history = UserStatsHistory.builder()
                .user(user)
                .hunger(user.getCurrentHunger())
                .energy(user.getCurrentEnergy())
                .happiness(user.getCurrentHappiness())
                .actionType(UserStatsHistory.ActionType.chat)
                .build();
        userStatsHistoryRepository.save(history);

        // 9. 비동기 요약 트리거
        conversationSummaryService.summarizeIfNeeded(user, persona);

        log.info("Chat completed for user {}: persona={}, context={} msgs, provider={}, happiness {} -> {}",
                userId, persona.getNameEn(), context.getRecentMessages().size(),
                llmResponse.getProviderName(), newHappiness - 2, newHappiness);

        // 10. 응답 생성
        return ChatResponse.builder()
                .userMessage(MessageResponse.from(userMessage))
                .botMessage(MessageResponse.from(botMessage))
                .statsUpdate(StatsResponse.from(user))
                .build();
    }

    /**
     * 오늘 일정 블록 생성 (AI가 scheduleId를 알 수 있도록)
     */
    private String buildTodayScheduleBlock(Long userId) {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime dayStart = today.atStartOfDay();
            LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

            List<com.lobai.dto.response.ScheduleResponse> schedules =
                    scheduleService.getSchedulesByDateRangeForUser(dayStart, dayEnd, userId);

            StringBuilder sb = new StringBuilder();
            sb.append("=== 오늘 일정 ===\n");

            if (schedules.isEmpty()) {
                sb.append("일정이 없습니다.\n");
            } else {
                for (com.lobai.dto.response.ScheduleResponse s : schedules) {
                    String startTime = s.getStartTime().length() >= 16 ? s.getStartTime().substring(11, 16) : s.getStartTime();
                    String endTime = s.getEndTime().length() >= 16 ? s.getEndTime().substring(11, 16) : s.getEndTime();
                    String completedMark = s.getIsCompleted() ? "완료됨" : "미완료";
                    sb.append(String.format("[ID:%d] %s~%s %s (%s) - %s\n",
                            s.getId(), startTime, endTime, s.getTitle(), s.getType(), completedMark));
                }
            }

            sb.append("\n일정 수정/삭제/완료 시 위 목록에서 ID를 찾아 사용하세요.\n");
            sb.append("사용자가 일정 이름으로 언급하면 해당 ID를 매칭하여 함수를 호출하세요.\n");

            return sb.toString();
        } catch (Exception e) {
            log.debug("Could not build today's schedule block: {}", e.getMessage());
            return "";
        }
    }

    private Persona resolvePersona(User user, Long personaId) {
        if (personaId != null) {
            return personaRepository.findById(personaId)
                    .orElseThrow(() -> new IllegalArgumentException("페르소나를 찾을 수 없습니다: " + personaId));
        }
        Persona persona = user.getCurrentPersona();
        if (persona == null) {
            persona = personaRepository.findByNameEn("friend")
                    .orElseThrow(() -> new IllegalStateException("기본 페르소나를 찾을 수 없습니다"));
            user.changePersona(persona);
            userRepository.save(user);
        }
        return persona;
    }

    /**
     * 사용자의 최근 대화 히스토리 조회
     *
     * @param userId 사용자 ID
     * @param limit 조회할 메시지 개수 (기본 50개)
     * @return 메시지 목록 (최신순)
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessageHistory(Long userId, Integer limit) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
        }

        // 메시지 조회
        int messageLimit = (limit != null && limit > 0) ? limit : 50;
        Pageable pageable = PageRequest.of(0, messageLimit);

        List<Message> messages = messageRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .getContent();

        // 역순 정렬 (최신순 → 오래된순)
        List<Message> reversed = new ArrayList<>(messages);
        java.util.Collections.reverse(reversed);

        return reversed.stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 페르소나와의 대화 히스토리 조회
     *
     * @param userId 사용자 ID
     * @param personaId 페르소나 ID
     * @return 메시지 목록
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesByPersona(Long userId, Long personaId) {
        List<Message> messages = messageRepository.findByUserAndPersona(userId, personaId);

        return messages.stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 파일 첨부와 함께 메시지 전송
     */
    @Transactional
    public ChatResponse sendMessageWithFile(Long userId, SendMessageRequest request, MultipartFile file) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 2. 페르소나 결정
        Persona persona = resolvePersona(user, request.getPersonaId());

        // 3. 파일 저장
        String attachmentUrl = null;
        String attachmentType = null;
        String attachmentName = null;

        if (file != null && !file.isEmpty()) {
            try {
                attachmentUrl = fileStorageService.saveFile(file, userId);
                attachmentType = file.getContentType();
                attachmentName = file.getOriginalFilename();
                log.info("File uploaded: {} (type: {}, size: {})", attachmentName, attachmentType, file.getSize());
            } catch (Exception e) {
                log.error("Failed to upload file", e);
                throw new RuntimeException("파일 업로드에 실패했습니다: " + e.getMessage());
            }
        }

        // 4. 3계층 컨텍스트 조립
        ContextAssemblyService.AssembledContext context =
                contextAssemblyService.assembleContext(userId, persona, 6000);

        // 5. 사용자 메시지 저장 (파일 정보 포함)
        Message userMessage = Message.builder()
                .user(user)
                .persona(persona)
                .role(Message.MessageRole.user)
                .content(request.getContent())
                .attachmentUrl(attachmentUrl)
                .attachmentType(attachmentType)
                .attachmentName(attachmentName)
                .build();
        userMessage = messageRepository.save(userMessage);

        // 5-1. 친밀도 점수 분석
        try {
            affinityScoreService.analyzeAndUpdateScore(userMessage);
        } catch (Exception e) {
            log.warn("Affinity score analysis failed: {}", e.getMessage());
        }

        // 6. AI 응답 생성 (파일 첨부는 기존 GeminiService 경유)
        String aiResponseText = geminiService.generateResponse(
                request.getContent(),
                new ArrayList<>(), // 컨텍스트는 이미 시스템 프롬프트에 포함
                persona,
                user.getCurrentHunger(),
                user.getCurrentEnergy(),
                user.getCurrentHappiness(),
                attachmentUrl,
                attachmentType,
                user.getTrustLevel()
        );

        // 7. AI 응답 저장
        Message botMessage = Message.builder()
                .user(user)
                .persona(persona)
                .role(Message.MessageRole.bot)
                .content(aiResponseText)
                .build();
        botMessage = messageRepository.save(botMessage);

        // 8. Stats 업데이트
        Integer newHappiness = user.getCurrentHappiness() + 2;
        user.updateStats(null, null, newHappiness);
        userRepository.save(user);

        // 9. Stats 히스토리 기록
        UserStatsHistory history = UserStatsHistory.builder()
                .user(user)
                .hunger(user.getCurrentHunger())
                .energy(user.getCurrentEnergy())
                .happiness(user.getCurrentHappiness())
                .actionType(UserStatsHistory.ActionType.chat)
                .build();
        userStatsHistoryRepository.save(history);

        // 10. 비동기 요약 트리거
        conversationSummaryService.summarizeIfNeeded(user, persona);

        log.info("Chat with file completed for user {}: file={}", userId, attachmentName);

        return ChatResponse.builder()
                .userMessage(MessageResponse.from(userMessage))
                .botMessage(MessageResponse.from(botMessage))
                .statsUpdate(StatsResponse.from(user))
                .build();
    }

    /**
     * 사용자의 대화 히스토리 전체 삭제
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void clearMessageHistory(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
        }

        messageRepository.deleteByUserId(userId);
        log.info("Message history cleared for user {}", userId);
    }

    /**
     * 일일 첫 체크인 확인 및 보상 지급
     *
     * @param userId 사용자 ID
     */
    private void checkAndRewardDailyCheckIn(Long userId) {
        // 오늘 날짜 범위 계산
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDateTime startOfDay = today.atStartOfDay();
        java.time.LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        // 오늘 작성한 사용자 메시지가 있는지 확인 (현재 메시지 제외, 이전 메시지만)
        List<Message> todayMessages = messageRepository.findByUserIdAndDateRange(
                userId, startOfDay, endOfDay);

        // 사용자가 작성한 메시지만 카운트 (role = user)
        long todayUserMessageCount = todayMessages.stream()
                .filter(m -> m.getRole() == Message.MessageRole.user)
                .count();

        // 첫 메시지인 경우 (현재 메시지 포함해서 1개)
        if (todayUserMessageCount == 1) {
            lobCoinService.earnLobCoin(
                    userId,
                    10,
                    "DAILY_CHECK_IN",
                    String.format("일일 첫 체크인 (%s)", today)
            );
            // 일일 첫 체크인 XP 보너스
            levelService.addExperience(userId, 10, "일일 첫 체크인");
            log.info("Daily check-in reward (10 LobCoin + 10 XP) given to user {}", userId);

            // 연속 채팅 스트릭 보너스
            int chatStreak = calculateChatStreak(userId, today);
            int streakBonus = getChatStreakBonus(chatStreak);
            if (streakBonus > 0) {
                lobCoinService.earnLobCoin(
                        userId,
                        streakBonus,
                        "CHAT_STREAK",
                        String.format("%d일 연속 채팅 보너스", chatStreak)
                );
                // 스트릭 XP 보너스
                levelService.addExperience(userId, streakBonus, chatStreak + "일 연속 채팅");
                log.info("Chat streak bonus ({} LobCoin + {} XP) for {}d streak, user {}",
                        streakBonus, streakBonus, chatStreak, userId);
            }
        }
    }

    /**
     * 연속 채팅일 계산 (오늘 포함)
     */
    private int calculateChatStreak(Long userId, java.time.LocalDate today) {
        // 최근 120일간 채팅한 날짜 조회
        java.time.LocalDateTime since = today.minusDays(120).atStartOfDay();
        List<java.sql.Date> chatDates = messageRepository.findDistinctChatDatesByUserId(userId, since);

        if (chatDates == null || chatDates.isEmpty()) return 1;

        int streak = 0;
        java.time.LocalDate expected = today;

        for (java.sql.Date sqlDate : chatDates) {
            java.time.LocalDate chatDate = sqlDate.toLocalDate();
            if (chatDate.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else if (chatDate.isBefore(expected)) {
                break; // 연속 끊김
            }
        }

        return Math.max(streak, 1);
    }

    /**
     * 연속 채팅 스트릭 보너스 (매일 지급되는 추가 보너스)
     */
    private int getChatStreakBonus(int streak) {
        if (streak >= 30) return 20;  // 30일+: 매일 +20
        if (streak >= 14) return 10;  // 14일+: 매일 +10
        if (streak >= 7) return 5;    // 7일+: 매일 +5
        return 0;
    }
}
