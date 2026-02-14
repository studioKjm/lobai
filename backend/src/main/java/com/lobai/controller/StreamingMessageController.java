package com.lobai.controller;

import com.lobai.dto.request.SendMessageRequest;
import com.lobai.dto.response.StreamChunk;
import com.lobai.security.SecurityUtil;
import com.lobai.service.StreamingMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * SSE 스트리밍 메시지 컨트롤러
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class StreamingMessageController {

    private final StreamingMessageService streamingMessageService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StreamChunk>> streamMessage(
            @Valid @RequestBody SendMessageRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return streamingMessageService.streamResponse(
                userId, request.getContent(), request.getPersonaId());
    }
}
