package com.lobai.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 선제 대화 트리거 평가 결과 값 객체
 */
@Getter
@AllArgsConstructor
public class TriggerResult {

    private final String type;
    private final String detail;

    public static TriggerResult of(String type, String detail) {
        return new TriggerResult(type, detail);
    }
}
