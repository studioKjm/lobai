package com.lobai.llm;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 토큰 수 추정기
 *
 * 한국어 텍스트는 약 1.5자 당 1토큰, 영어는 약 4자 당 1토큰으로 추정.
 */
@Component
public class TokenEstimator {

    private static final double KOREAN_CHARS_PER_TOKEN = 1.5;
    private static final double ENGLISH_CHARS_PER_TOKEN = 4.0;
    private static final int MESSAGE_OVERHEAD_TOKENS = 4; // per-message overhead

    /**
     * 텍스트의 토큰 수 추정
     */
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;

        int koreanChars = 0;
        int otherChars = 0;

        for (char c : text.toCharArray()) {
            if (isKorean(c)) {
                koreanChars++;
            } else {
                otherChars++;
            }
        }

        int koreanTokens = (int) Math.ceil(koreanChars / KOREAN_CHARS_PER_TOKEN);
        int otherTokens = (int) Math.ceil(otherChars / ENGLISH_CHARS_PER_TOKEN);

        return koreanTokens + otherTokens;
    }

    /**
     * 메시지 목록의 총 토큰 수 추정
     */
    public int estimateTokens(List<LlmMessage> messages) {
        if (messages == null || messages.isEmpty()) return 0;

        int total = 0;
        for (LlmMessage msg : messages) {
            total += estimateTokens(msg.getContent()) + MESSAGE_OVERHEAD_TOKENS;
        }
        return total;
    }

    /**
     * 주어진 토큰 예산 내에서 최대 메시지 수 계산 (최신 메시지부터)
     */
    public int maxMessagesWithinBudget(List<LlmMessage> messages, int tokenBudget) {
        if (messages == null || messages.isEmpty()) return 0;

        int total = 0;
        int count = 0;

        // 최신 메시지부터 역순으로 (리스트의 끝에서부터)
        for (int i = messages.size() - 1; i >= 0; i--) {
            int msgTokens = estimateTokens(messages.get(i).getContent()) + MESSAGE_OVERHEAD_TOKENS;
            if (total + msgTokens > tokenBudget) break;
            total += msgTokens;
            count++;
        }

        return count;
    }

    private boolean isKorean(char c) {
        return (c >= 0xAC00 && c <= 0xD7A3)  // 한글 완성형
            || (c >= 0x1100 && c <= 0x11FF)   // 한글 자모
            || (c >= 0x3130 && c <= 0x318F);  // 한글 호환 자모
    }
}
