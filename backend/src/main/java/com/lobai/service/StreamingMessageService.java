package com.lobai.service;

import com.lobai.dto.response.StreamChunk;
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
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

/**
 * SSE 스트리밍 메시지 서비스
 *
 * MessageService와 동일한 전처리 후 LlmProvider.generateStream()으로 실시간 전달.
 * 스트림 완료 후 전체 응답을 DB에 저장.
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
    private final AffinityScoreService affinityScoreService;
    private final LobCoinService lobCoinService;

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

            PromptContext promptContext = PromptContext.builder()
                    .persona(persona)
                    .user(user)
                    .hunger(user.getCurrentHunger())
                    .energy(user.getCurrentEnergy())
                    .happiness(user.getCurrentHappiness())
                    .userProfileBlock(context.getUserProfileBlock())
                    .conversationSummaryBlock(context.getConversationSummaryBlock())
                    .providerName(provider.getProviderName())
                    .build();

            String systemInstruction = personaPromptTemplate.render(promptContext);

            LlmRequest llmRequest = LlmRequest.builder()
                    .systemInstruction(systemInstruction)
                    .conversationHistory(context.getRecentMessages())
                    .userMessage(content)
                    .taskType(LlmTaskType.CHAT_CONVERSATION)
                    .build();

            // 6. 스트리밍 시작
            AtomicReference<StringBuilder> fullResponse = new AtomicReference<>(new StringBuilder());

            return provider.generateStream(llmRequest)
                    .map(chunk -> {
                        fullResponse.get().append(chunk);
                        return ServerSentEvent.<StreamChunk>builder()
                                .data(StreamChunk.text(chunk))
                                .build();
                    })
                    .concatWith(Mono.fromCallable(() -> {
                        // 스트림 완료: DB 저장 + 후처리
                        String finalResponse = fullResponse.get().toString();
                        saveCompletedResponse(user, persona, finalResponse, provider.getProviderName());
                        return ServerSentEvent.<StreamChunk>builder()
                                .event("done")
                                .data(StreamChunk.done())
                                .build();
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
