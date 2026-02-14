package com.lobai.service;

import com.lobai.dto.request.EarnLobCoinRequest;
import com.lobai.dto.request.SpendLobCoinRequest;
import com.lobai.dto.response.LobCoinBalanceResponse;
import com.lobai.dto.response.TransactionResponse;
import com.lobai.entity.LobCoinBalance;
import com.lobai.entity.LobCoinTransaction;
import com.lobai.entity.User;
import com.lobai.repository.LobCoinBalanceRepository;
import com.lobai.repository.LobCoinTransactionRepository;
import com.lobai.repository.UserRepository;
import com.lobai.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LobCoinService {

    private final LobCoinBalanceRepository balanceRepository;
    private final LobCoinTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Get user's LobCoin balance
     */
    @Transactional(readOnly = true)
    public LobCoinBalanceResponse getBalance(Long userId) {
        LobCoinBalance balance = balanceRepository.findByUserId(userId)
            .orElseGet(() -> createInitialBalance(userId));

        return LobCoinBalanceResponse.builder()
            .balance(balance.getBalance())
            .totalEarned(balance.getTotalEarned())
            .totalSpent(balance.getTotalSpent())
            .valueInUsd(balance.getBalance() / 100.0)
            .updatedAt(balance.getUpdatedAt().format(ISO_FORMATTER))
            .build();
    }

    /**
     * Get current user's balance
     */
    @Transactional(readOnly = true)
    public LobCoinBalanceResponse getMyBalance() {
        Long userId = SecurityUtil.getCurrentUserId();
        return getBalance(userId);
    }

    /**
     * Earn LobCoins
     */
    @Transactional
    public TransactionResponse earnLobCoin(Long userId, int amount, String source, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("획득량은 양수여야 합니다");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // Get or create balance
        LobCoinBalance balance = balanceRepository.findByUserId(userId)
            .orElseGet(() -> createInitialBalance(userId));

        // Update balance
        balance.earn(amount);
        balanceRepository.save(balance);

        // Create transaction record
        LobCoinTransaction transaction = LobCoinTransaction.builder()
            .user(user)
            .amount(amount)
            .balanceAfter(balance.getBalance())
            .type(LobCoinTransaction.LobCoinType.EARN)
            .source(source)
            .description(description)
            .build();

        LobCoinTransaction saved = transactionRepository.save(transaction);

        log.info("LobCoin earned: {} coins from {} for user {}", amount, source, userId);
        return toTransactionResponse(saved);
    }

    /**
     * Earn LobCoins (authenticated user)
     */
    @Transactional
    public TransactionResponse earnLobCoin(EarnLobCoinRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return earnLobCoin(userId, request.getAmount(), request.getSource(), request.getDescription());
    }

    /**
     * Spend LobCoins
     */
    @Transactional
    public TransactionResponse spendLobCoin(Long userId, int amount, String source, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("사용량은 양수여야 합니다");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // Get balance
        LobCoinBalance balance = balanceRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("잔액 정보를 찾을 수 없습니다"));

        // Check sufficient balance
        if (balance.getBalance() < amount) {
            throw new IllegalArgumentException(
                String.format("잔액이 부족합니다 (현재: %d, 필요: %d)", balance.getBalance(), amount)
            );
        }

        // Update balance
        balance.spend(amount);
        balanceRepository.save(balance);

        // Create transaction record
        LobCoinTransaction transaction = LobCoinTransaction.builder()
            .user(user)
            .amount(-amount)  // Negative for spending
            .balanceAfter(balance.getBalance())
            .type(LobCoinTransaction.LobCoinType.SPEND)
            .source(source)
            .description(description)
            .build();

        LobCoinTransaction saved = transactionRepository.save(transaction);

        log.info("LobCoin spent: {} coins for {} by user {}", amount, source, userId);
        return toTransactionResponse(saved);
    }

    /**
     * Spend LobCoins (authenticated user)
     */
    @Transactional
    public TransactionResponse spendLobCoin(SpendLobCoinRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return spendLobCoin(userId, request.getAmount(), request.getSource(), request.getDescription());
    }

    /**
     * Get transaction history
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(Long userId, Integer limit) {
        List<LobCoinTransaction> transactions;

        if (limit != null && limit > 0) {
            transactions = transactionRepository.findRecentTransactions(
                userId,
                PageRequest.of(0, limit)
            );
        } else {
            transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        return transactions.stream()
            .map(this::toTransactionResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get my transaction history
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransactionHistory(Integer limit) {
        Long userId = SecurityUtil.getCurrentUserId();
        return getTransactionHistory(userId, limit);
    }

    /**
     * Get transaction history by type
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByType(
        Long userId,
        LobCoinTransaction.LobCoinType type
    ) {
        List<LobCoinTransaction> transactions = transactionRepository
            .findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);

        return transactions.stream()
            .map(this::toTransactionResponse)
            .collect(Collectors.toList());
    }

    /**
     * Create initial balance for new user
     */
    private LobCoinBalance createInitialBalance(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        LobCoinBalance balance = new LobCoinBalance();
        balance.setUser(user);  // @MapsId will automatically set userId from user.id
        balance.setBalance(0);
        balance.setTotalEarned(0);
        balance.setTotalSpent(0);

        return balanceRepository.save(balance);
    }

    /**
     * Convert Transaction to TransactionResponse
     */
    private TransactionResponse toTransactionResponse(LobCoinTransaction transaction) {
        return TransactionResponse.builder()
            .id(transaction.getId())
            .amount(transaction.getAmount())
            .balanceAfter(transaction.getBalanceAfter())
            .type(transaction.getType().name())
            .source(transaction.getSource())
            .description(transaction.getDescription())
            .createdAt(transaction.getCreatedAt().format(ISO_FORMATTER))
            .build();
    }
}
