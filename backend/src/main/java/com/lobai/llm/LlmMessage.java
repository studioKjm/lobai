package com.lobai.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Provider-agnostic 메시지 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmMessage {

    public enum Role {
        USER, ASSISTANT, SYSTEM
    }

    private Role role;
    private String content;
    private List<Attachment> attachments;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        private String mimeType;
        private String base64Data;
    }

    public static LlmMessage user(String content) {
        return LlmMessage.builder().role(Role.USER).content(content).build();
    }

    public static LlmMessage assistant(String content) {
        return LlmMessage.builder().role(Role.ASSISTANT).content(content).build();
    }

    public static LlmMessage system(String content) {
        return LlmMessage.builder().role(Role.SYSTEM).content(content).build();
    }
}
