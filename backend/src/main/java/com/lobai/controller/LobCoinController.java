package com.lobai.controller;

import com.lobai.dto.request.EarnLobCoinRequest;
import com.lobai.dto.request.SpendLobCoinRequest;
import com.lobai.dto.response.ApiResponse;
import com.lobai.dto.response.LobCoinBalanceResponse;
import com.lobai.dto.response.TransactionResponse;
import com.lobai.service.LobCoinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LobCoinController
 *
 * LobCoin 토큰 이코노미 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/lobcoin")
@RequiredArgsConstructor
public class LobCoinController {

    private final LobCoinService lobCoinService;

    /**
     * 잔액 조회
     *
     * GET /api/lobcoin/balance
     */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<LobCoinBalanceResponse>> getBalance() {
        log.info("Get LobCoin balance request");

        LobCoinBalanceResponse balance = lobCoinService.getMyBalance();

        return ResponseEntity.ok(ApiResponse.success("잔액 조회 성공", balance));
    }

    /**
     * 거래 내역 조회
     *
     * GET /api/lobcoin/transactions?limit=20
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(
            @RequestParam(required = false) Integer limit) {
        log.info("Get LobCoin transactions request (limit: {})", limit);

        List<TransactionResponse> transactions = lobCoinService.getMyTransactionHistory(limit);

        return ResponseEntity.ok(ApiResponse.success("거래 내역 조회 성공", transactions));
    }

    /**
     * LobCoin 획득 (테스트용 - 실제로는 시스템 내부에서 자동 호출)
     *
     * POST /api/lobcoin/earn
     */
    @PostMapping("/earn")
    public ResponseEntity<ApiResponse<TransactionResponse>> earnLobCoin(
            @Valid @RequestBody EarnLobCoinRequest request) {
        log.info("Earn LobCoin request: {} from {}", request.getAmount(), request.getSource());

        TransactionResponse transaction = lobCoinService.earnLobCoin(request);

        return ResponseEntity.ok(ApiResponse.success("LobCoin 획득 성공", transaction));
    }

    /**
     * LobCoin 사용 (서비스 내 아이템 구매)
     *
     * POST /api/lobcoin/spend
     */
    @PostMapping("/spend")
    public ResponseEntity<ApiResponse<TransactionResponse>> spendLobCoin(
            @Valid @RequestBody SpendLobCoinRequest request) {
        log.info("Spend LobCoin request: {} for {}", request.getAmount(), request.getSource());

        TransactionResponse transaction = lobCoinService.spendLobCoin(request);

        return ResponseEntity.ok(ApiResponse.success("LobCoin 사용 성공", transaction));
    }
}
