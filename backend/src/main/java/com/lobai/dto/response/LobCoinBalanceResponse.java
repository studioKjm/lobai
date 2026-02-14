package com.lobai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LobCoinBalanceResponse {

    private Integer balance;
    private Integer totalEarned;
    private Integer totalSpent;
    private Double valueInUsd;  // balance / 100
    private String updatedAt;   // ISO-8601 format
}
