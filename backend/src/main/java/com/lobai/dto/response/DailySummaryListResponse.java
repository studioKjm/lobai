package com.lobai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySummaryListResponse {
    private LocalDate date;
    private String summaryPreview;
    private Integer messageCount;
    private String keyFactsPreview;
}
