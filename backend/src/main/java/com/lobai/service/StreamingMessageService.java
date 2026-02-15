package com.lobai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lobai.dto.response.StreamChunk;
import com.lobai.entity.Message;
import com.lobai.entity.Persona;
import com.lobai.entity.User;
import com.lobai.entity.UserStatsHistory;
import com.lobai.llm.*;
import com.lobai.llm.prompt.PersonaPromptTemplate;
import com.lobai.llm.prompt.PromptContext;
import com.lobai.llm.provider.GeminiLlmProvider;
import com.lobai.repository.MessageRepository;
import com.lobai.repository.PersonaRepository;
import com.lobai.repository.UserRepository;
import com.lobai.repository.UserStatsHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SSE 스트리밍 메시지 서비스
 *
 * MessageService와 동일한 전처리 후 LlmProvider.generateStream()으로 실시간 전달.
 * 스트림 완료 후 전체 응답을 DB에 저장.
 * Function Call(일정 등록 등)은 스트림 중 감지하여 실행 후 자연어 응답을 전달.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingMessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final PersonaRepository personaRepository;
    private final UserStatsHistoryRepository userStatsHistoryRepository;
    private final ContextAssemblyService contextAssemblyService;
    private final ConversationSummaryService conversationSummaryService;
    private final PersonaPromptTemplate personaPromptTemplate;
    private final LlmRouter llmRouter;
    private final GeminiService geminiService;
    private final ScheduleService scheduleService;
    private final AffinityScoreService affinityScoreService;
    private final LobCoinService lobCoinService;
    private final ObjectMapper objectMapper;

    /**
     * SSE 스트리밍 응답 생성
     */
    public Flux<ServerSentEvent<StreamChunk>> streamResponse(Long userId, String content, Long personaId) {
        try {
            // 1. 전처리 (동기)
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

            Persona persona = resolvePersona(user, personaId);

            // 2. 컨텍스트 조립
            ContextAssemblyService.AssembledContext context =
                    contextAssemblyService.assembleContext(userId, persona, 6000);

            // 3. 사용자 메시지 저장
            Message userMessage = Message.builder()
                    .user(user)
                    .persona(persona)
                    .role(Message.MessageRole.user)
                    .content(content)
                    .build();
            messageRepository.save(userMessage);

            // 4. 친밀도 분석
            try {
                affinityScoreService.analyzeAndUpdateScore(userMessage);
            } catch (Exception e) {
                log.warn("Affinity analysis failed: {}", e.getMessage());
            }

            // 5. 프롬프트 구성
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
            List<Map<String, Object>> tools = geminiService.buildToolsForMessage(content);
            boolean isWebSearchMode = tools.size() == 1 && tools.get(0).containsKey("google_search");
            if (isWebSearchMode) {
                systemInstruction += geminiService.buildWebSearchDirective();
            }

            LlmRequest llmRequest = LlmRequest.builder()
                    .systemInstruction(systemInstruction)
                    .conversationHistory(context.getRecentMessages())
                    .userMessage(content)
                    .tools(tools)
                    .taskType(LlmTaskType.CHAT_CONVERSATION)
                    .build();

            // 6. 스트리밍 시작 (Function Call 감지 포함)
            AtomicReference<StringBuilder> fullResponse = new AtomicReference<>(new StringBuilder());
            AtomicReference<String> detectedFunctionCall = new AtomicReference<>(null);

            return provider.generateStream(llmRequest)
                    .map(chunk -> {
                        // Function Call 시그널 감지
                        if (chunk.startsWith(GeminiLlmProvider.FC_SIGNAL_PREFIX)) {
                            String fcJson = chunk.substring(GeminiLlmProvider.FC_SIGNAL_PREFIX.length());
                            detectedFunctionCall.set(fcJson);
                            log.info("Function call detected in stream: {}", fcJson);
                            // 빈 청크 반환 (프론트엔드에 전달하지 않음)
                            return ServerSentEvent.<StreamChunk>builder()
                                    .data(StreamChunk.builder().content("").done(false).build())
                                    .build();
                        }

                        // 일반 텍스트 청크
                        fullResponse.get().append(chunk);
                        return ServerSentEvent.<StreamChunk>builder()
                                .data(StreamChunk.text(chunk))
                                .build();
                    })
                    .filter(sse -> sse.data() != null
                            && sse.data().getContent() != null
                            && !sse.data().getContent().isEmpty())
                    .concatWith(Flux.defer(() -> {
                        String fcJson = detectedFunctionCall.get();
                        if (fcJson != null) {
                            // Function Call 처리 (boundedElastic 스레드에서 실행 - Netty 스레드 차단 방지)
                            return Mono.fromCallable(() ->
                                    executeFunctionCallBlocking(llmRequest, fcJson, userId, user, persona, provider.getProviderName())
                            )
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMapMany(aiResponse -> Flux.just(
                                    ServerSentEvent.<StreamChunk>builder()
                                            .data(StreamChunk.text(aiResponse))
                                            .build(),
                                    ServerSentEvent.<StreamChunk>builder()
                                            .event("done")
                                            .data(StreamChunk.done())
                                            .build()
                            ));
                        }

                        // 일반 응답: DB 저장 + done 이벤트 (boundedElastic에서 실행)
                        String finalResponse = fullResponse.get().toString();
                        return Mono.fromCallable(() -> {
                            saveCompletedResponse(user, persona, finalResponse, provider.getProviderName());
                            return true;
                        })
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMapMany(ok -> Flux.just(ServerSentEvent.<StreamChunk>builder()
                                .event("done")
                                .data(StreamChunk.done())
                                .build()));
                    }))
                    .onErrorResume(e -> {
                        log.error("Streaming error for user {}: {}", userId, e.getMessage());
                        return Flux.just(ServerSentEvent.<StreamChunk>builder()
                                .event("error")
                                .data(StreamChunk.builder()
                                        .content("스트리밍 중 오류가 발생했습니다.")
                                        .done(true)
                                        .build())
                                .build());
                    });

        } catch (Exception e) {
            log.error("Failed to start streaming for user {}", userId, e);
            return Flux.just(ServerSentEvent.<StreamChunk>builder()
                    .event("error")
                    .data(StreamChunk.builder()
                            .content("죄송해요, 지금 대화가 어려워요.")
                            .done(true)
                            .build())
                    .build());
        }
    }

    /**
     * 스트리밍 중 감지된 Function Call 처리 (blocking - boundedElastic 스레드에서 실행)
     *
     * 1) GeminiService.handleFunctionCall()로 함수 실행 + 자연어 응답 생성
     * 2) DB 저장
     * 3) 응답 텍스트 반환
     */
    private String executeFunctionCallBlocking(
            LlmRequest originalRequest, String fcJson, Long userId,
            User user, Persona persona, String providerName) {
        try {
            JsonNode fcNode = objectMapper.readTree(fcJson);
            String functionName = fcNode.path("name").asText();
            String argsJson = fcNode.path("args").toString();

            log.info("Executing function call in stream: {} with args: {}", functionName, argsJson);

            // GeminiService에 위임: 함수 실행 + continueWithFunctionResult
            String aiResponse = geminiService.handleFunctionCall(originalRequest, functionName, argsJson, userId);

            // DB 저장
            saveCompletedResponse(user, persona, aiResponse, providerName);

            return aiResponse;
        } catch (Exception e) {
            log.error("Failed to handle function call in stream", e);
            String errorMsg = "일정 처리 중 오류가 발생했습니다.";
            saveCompletedResponse(user, persona, errorMsg, providerName);
            return errorMsg;
        }
    }

    @Transactional
    protected void saveCompletedResponse(User user, Persona persona, String responseText, String providerName) {
        try {
            // 봇 응답 저장
            Message botMessage = Message.builder()
                    .user(user)
                    .persona(persona)
                    .role(Message.MessageRole.bot)
                    .content(responseText)
                    .llmProvider(providerName)
                    .build();
            messageRepository.save(botMessage);

            // Stats 업데이트
            Integer newHappiness = user.getCurrentHappiness() + 2;
            user.updateStats(null, null, newHappiness);
            userRepository.save(user);

            // Stats 히스토리
            UserStatsHistory history = UserStatsHistory.builder()
                    .user(user)
                    .hunger(user.getCurrentHunger())
                    .energy(user.getCurrentEnergy())
                    .happiness(user.getCurrentHappiness())
                    .actionType(UserStatsHistory.ActionType.chat)
                    .build();
            userStatsHistoryRepository.save(history);

            // 비동기 요약
            conversationSummaryService.summarizeIfNeeded(user, persona);

            log.info("Streaming completed for user {}: {} chars via {}", user.getId(), responseText.length(), providerName);
        } catch (Exception e) {
            log.error("Failed to save streaming response for user {}", user.getId(), e);
        }
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
        }
        return persona;
    }
}
