package com.lobai.service;

import com.lobai.dto.request.SendMessageRequest;
import com.lobai.dto.response.ChatResponse;
import com.lobai.dto.response.MessageResponse;
import com.lobai.dto.response.StatsResponse;
import com.lobai.entity.Message;
import com.lobai.entity.Persona;
import com.lobai.entity.User;
import com.lobai.entity.UserStatsHistory;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MessageService
 *
 * 메시지 및 채팅 관련 비즈니스 로직
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
    private final AffinityScoreService affinityScoreService;

    /**
     * 메시지 전송 및 AI 응답 생성
     *
     * @param userId 사용자 ID
     * @param request 메시지 요청
     * @return 채팅 응답 (사용자 메시지 + AI 응답 + Stats 업데이트)
     */
    @Transactional
    public ChatResponse sendMessage(Long userId, SendMessageRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 2. 페르소나 결정 (요청에 포함되지 않은 경우 사용자의 현재 페르소나 사용)
        Persona persona;
        if (request.getPersonaId() != null) {
            persona = personaRepository.findById(request.getPersonaId())
                    .orElseThrow(() -> new IllegalArgumentException("페르소나를 찾을 수 없습니다: " + request.getPersonaId()));
        } else {
            persona = user.getCurrentPersona();
            if (persona == null) {
                // 기본 페르소나 (친구모드) 사용
                persona = personaRepository.findByNameEn("friend")
                        .orElseThrow(() -> new IllegalStateException("기본 페르소나를 찾을 수 없습니다"));
                user.changePersona(persona);
                userRepository.save(user);
            }
        }

        // 3. 최근 대화 히스토리 조회 (최근 6개 = 3번의 대화 왕복)
        // gemini-2.5-flash 모델의 503 에러 방지를 위해 히스토리 제한
        Pageable historyPageable = PageRequest.of(0, 6);
        List<Message> recentHistory = messageRepository
                .findByUserIdOrderByCreatedAtDesc(userId, historyPageable)
                .getContent();

        // 역순으로 변환 (오래된 것부터 최신 것 순으로)
        List<Message> conversationHistory = new ArrayList<>();
        for (int i = recentHistory.size() - 1; i >= 0; i--) {
            conversationHistory.add(recentHistory.get(i));
        }

        // 4. 사용자 메시지 저장
        Message userMessage = Message.builder()
                .user(user)
                .persona(persona)
                .role(Message.MessageRole.user)
                .content(request.getContent())
                .build();
        userMessage = messageRepository.save(userMessage);

        // 4-1. 친밀도 점수 분석 (비동기 권장, 현재는 동기)
        try {
            affinityScoreService.analyzeAndUpdateScore(userMessage);
        } catch (Exception e) {
            log.warn("Affinity score analysis failed, continuing: userId={}, error={}",
                    userId, e.getMessage());
        }

        // 5. Gemini API 호출하여 AI 응답 생성 (대화 히스토리 포함)
        String aiResponseText = geminiService.generateResponse(
                request.getContent(),
                conversationHistory,
                persona,
                user.getCurrentHunger(),
                user.getCurrentEnergy(),
                user.getCurrentHappiness()
        );

        // 6. AI 응답 저장
        Message botMessage = Message.builder()
                .user(user)
                .persona(persona)
                .role(Message.MessageRole.bot)
                .content(aiResponseText)
                .build();
        botMessage = messageRepository.save(botMessage);

        // 7. Stats 업데이트 (대화 시 행복도 소폭 증가)
        Integer newHappiness = user.getCurrentHappiness() + 2;  // 대화 1회당 +2
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

        log.info("Chat completed for user {}: persona={}, history={} msgs, happiness {} -> {}",
                userId, persona.getNameEn(), conversationHistory.size(), newHappiness - 2, newHappiness);

        // 9. 응답 생성
        return ChatResponse.builder()
                .userMessage(MessageResponse.from(userMessage))
                .botMessage(MessageResponse.from(botMessage))
                .statsUpdate(StatsResponse.from(user))
                .build();
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
}
