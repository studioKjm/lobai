package com.lobai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerRequest {
    private Long sessionId;
    private String answer;
    private Integer timeTakenSeconds; // 클라이언트에서 측정한 소요 시간
}
