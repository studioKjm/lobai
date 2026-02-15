package com.lobai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySummaryDetailResponse {
    private LocalDate date;
    private String summaryText;
    private String keyFacts;
    private Integer messageCount;
    private List<MessageResponse> messages;
}
