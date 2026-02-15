package com.lobai.llm.prompt;

import com.lobai.entity.Persona;
import com.lobai.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프롬프트 렌더링에 필요한 컨텍스트 데이터
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptContext {

    private Persona persona;
    private User user;

    // 상태값
    private Integer hunger;
    private Integer energy;
    private Integer happiness;
    private Integer trustLevel;

    // 메모리 블록 (ContextAssemblyService에서 조립)
    private String userProfileBlock;
    private String conversationSummaryBlock;

    // 시간 정보
    private String currentTime;

    // Provider 이름 (Provider별 프롬프트 조정용)
    private String providerName;

    // 오늘 일정 블록 (AI가 scheduleId를 알 수 있도록)
    private String todayScheduleBlock;

    // 선제 대화 트리거 정보
    private String proactiveTriggerType;
    private String proactiveTriggerDetail;
}
