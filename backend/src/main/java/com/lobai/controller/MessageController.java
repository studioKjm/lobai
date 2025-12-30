package com.lobai.controller;

import com.lobai.dto.request.SendMessageRequest;
import com.lobai.dto.response.ApiResponse;
import com.lobai.dto.response.ChatResponse;
import com.lobai.dto.response.MessageResponse;
import com.lobai.security.SecurityUtil;
import com.lobai.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MessageController
 *
 * 메시지 및 채팅 관련 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 메시지 전송 및 AI 응답 받기
     *
     * POST /api/messages
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> sendMessage(
            @Valid @RequestBody SendMessageRequest request) {

        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Send message request from user {}: content length={}", userId, request.getContent().length());

        ChatResponse response = messageService.sendMessage(userId, request);

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    /**
     * 대화 히스토리 조회
     *
     * GET /api/messages?limit=50
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessageHistory(
            @RequestParam(required = false, defaultValue = "50") Integer limit) {

        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Get message history request from user {}: limit={}", userId, limit);

        List<MessageResponse> messages = messageService.getMessageHistory(userId, limit);

        return ResponseEntity
                .ok(ApiResponse.success(messages));
    }

    /**
     * 특정 페르소나와의 대화 히스토리 조회
     *
     * GET /api/messages/persona/{personaId}
     */
    @GetMapping("/persona/{personaId}")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessagesByPersona(
            @PathVariable Long personaId) {

        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Get messages by persona request from user {}: personaId={}", userId, personaId);

        List<MessageResponse> messages = messageService.getMessagesByPersona(userId, personaId);

        return ResponseEntity
                .ok(ApiResponse.success(messages));
    }
}
