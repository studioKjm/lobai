package com.lobai.service;

import com.lobai.entity.ChatSession;
import com.lobai.entity.Persona;
import com.lobai.entity.User;
import com.lobai.repository.ChatSessionRepository;
import com.lobai.repository.PersonaRepository;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ChatSessionService
 * 채팅 세션 관리 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final PersonaRepository personaRepository;

    /**
     * Get or create active session for user
     */
    @Transactional
    public ChatSession getOrCreateActiveSession(Long userId, Long personaId) {
        Optional<ChatSession> activeSession = sessionRepository.findActiveSession(userId);

        if (activeSession.isPresent()) {
            ChatSession session = activeSession.get();

            // Check if session has expired
            if (session.hasExpired()) {
                endSession(session.getId());
                return createNewSession(userId, personaId);
            }

            session.updateActivity();
            return sessionRepository.save(session);
        }

        return createNewSession(userId, personaId);
    }

    /**
     * Create new session
     */
    @Transactional
    public ChatSession createNewSession(Long userId, Long personaId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Persona persona = null;
        if (personaId != null) {
            persona = personaRepository.findById(personaId)
                .orElse(null);
        }

        ChatSession session = ChatSession.builder()
            .user(user)
            .persona(persona)
            .build();

        ChatSession saved = sessionRepository.save(session);
        log.info("Created new chat session {} for user {}", saved.getId(), userId);
        return saved;
    }

    /**
     * End session
     */
    @Transactional
    public void endSession(Long sessionId) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다"));

        if (!session.getIsActive()) {
            return; // Already ended
        }

        session.endSession();
        sessionRepository.save(session);

        log.info("Ended chat session {} for user {} (Duration: {} minutes)",
            sessionId, session.getUser().getId(), session.getDurationMinutes());
    }

    /**
     * Record message in session
     */
    @Transactional
    public void recordMessage(Long sessionId, int tokens) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다"));

        session.incrementMessageCount();
        session.addTokens(tokens);
        session.updateActivity();

        sessionRepository.save(session);
    }

    /**
     * Set session title from first message
     */
    @Transactional
    public void setSessionTitle(Long sessionId, String firstMessage) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다"));

        if (session.getSessionTitle() == null || session.getSessionTitle().isEmpty()) {
            session.generateTitle(firstMessage);
            sessionRepository.save(session);
        }
    }

    /**
     * Get all sessions for user
     */
    @Transactional(readOnly = true)
    public List<ChatSession> getUserSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId);
    }

    /**
     * Get recent sessions for user
     */
    @Transactional(readOnly = true)
    public List<ChatSession> getRecentSessions(Long userId, int limit) {
        return sessionRepository.findRecentSessions(userId, limit);
    }

    /**
     * Get active session for user
     */
    @Transactional(readOnly = true)
    public Optional<ChatSession> getActiveSession(Long userId) {
        return sessionRepository.findActiveSession(userId);
    }

    /**
     * Get session by ID
     */
    @Transactional(readOnly = true)
    public ChatSession getSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다"));
    }

    /**
     * End all active sessions for user
     */
    @Transactional
    public void endAllActiveSessions(Long userId) {
        List<ChatSession> activeSessions = sessionRepository.findByUserIdAndIsActiveTrueOrderByStartedAtDesc(userId);

        for (ChatSession session : activeSessions) {
            endSession(session.getId());
        }

        log.info("Ended all active sessions for user {}", userId);
    }

    /**
     * Auto-expire old sessions (scheduled task)
     */
    @Transactional
    public void autoExpireSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(30);
        List<ChatSession> expiredSessions = sessionRepository.findExpiredActiveSessions(cutoffTime);

        for (ChatSession session : expiredSessions) {
            endSession(session.getId());
        }

        if (!expiredSessions.isEmpty()) {
            log.info("Auto-expired {} sessions", expiredSessions.size());
        }
    }

    /**
     * Get session statistics for user
     */
    @Transactional(readOnly = true)
    public SessionStatistics getStatistics(Long userId) {
        Long totalSessions = sessionRepository.countByUserId(userId);
        Integer totalMessages = sessionRepository.sumTotalMessages(userId);
        Integer totalChatTime = sessionRepository.sumTotalChatTime(userId);
        Double avgDuration = sessionRepository.calculateAverageSessionDuration(userId);

        return SessionStatistics.builder()
            .totalSessions(totalSessions != null ? totalSessions : 0)
            .totalMessages(totalMessages != null ? totalMessages : 0)
            .totalChatTimeMinutes(totalChatTime != null ? totalChatTime : 0)
            .averageSessionDurationMinutes(avgDuration != null ? avgDuration : 0.0)
            .build();
    }

    /**
     * Session statistics DTO
     */
    @lombok.Builder
    @lombok.Getter
    public static class SessionStatistics {
        private long totalSessions;
        private int totalMessages;
        private int totalChatTimeMinutes;
        private double averageSessionDurationMinutes;
    }
}
