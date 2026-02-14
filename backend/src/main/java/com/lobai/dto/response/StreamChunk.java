package com.lobai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SSE 스트리밍 청크 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamChunk {
    private String content;
    private boolean done;

    public static StreamChunk text(String content) {
        return StreamChunk.builder().content(content).done(false).build();
    }

    public static StreamChunk done() {
        return StreamChunk.builder().content("").done(true).build();
    }
}
