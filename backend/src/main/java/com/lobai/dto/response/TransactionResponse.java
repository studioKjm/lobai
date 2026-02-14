package com.lobai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;
    private Integer amount;
    private Integer balanceAfter;
    private String type;          // EARN or SPEND
    private String source;
    private String description;
    private String createdAt;     // ISO-8601 format
}
