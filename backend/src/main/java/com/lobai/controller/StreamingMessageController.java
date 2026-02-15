package com.lobai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lobai.dto.request.SendMessageRequest;
import com.lobai.dto.response.StreamChunk;
import com.lobai.security.SecurityUtil;
import com.lobai.service.StreamingMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * SSE 스트리밍 메시지 컨트롤러
 *
 * SseEmitter를 사용하여 개별 청크를 즉시 flush.
 * Flux<ServerSentEvent> 방식은 Spring MVC에서 버퍼링 이슈가 있어 직접 제어.
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class StreamingMessageController {

    private final StreamingMessageService streamingMessageService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(
            @Valid @RequestBody SendMessageRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

        // 5분 타임아웃
        SseEmitter emitter = new SseEmitter(300_000L);

        streamingMessageService.streamResponse(
                userId, request.getContent(), request.getPersonaId())
            .subscribe(
                sse -> {
                    try {
                        StreamChunk chunk = sse.data();
                        if (chunk == null) return;

                        String eventName = sse.event();
                        String json = objectMapper.writeValueAsString(chunk);

                        if (eventName != null) {
                            emitter.send(SseEmitter.event()
                                    .name(eventName)
                                    .data(json, MediaType.APPLICATION_JSON));
                        } else {
                            emitter.send(SseEmitter.event()
                                    .data(json, MediaType.APPLICATION_JSON));
                        }
                    } catch (IOException e) {
                        log.debug("SSE send failed (client disconnected): {}", e.getMessage());
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    log.error("Streaming error: {}", error.getMessage());
                    try {
                        String errorJson = objectMapper.writeValueAsString(
                                StreamChunk.builder().content("스트리밍 중 오류가 발생했습니다.").done(true).build());
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data(errorJson, MediaType.APPLICATION_JSON));
                    } catch (IOException ignored) {}
                    emitter.complete();
                },
                emitter::complete
            );

        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> log.debug("SSE emitter error: {}", e.getMessage()));

        return emitter;
    }
}
