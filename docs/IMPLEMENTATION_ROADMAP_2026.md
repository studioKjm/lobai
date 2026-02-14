# LobAI 고도화 실행 로드맵 2026

> **"전략에서 실행으로: 4대 핵심 과제 해결을 위한 구체적 구현 계획"**

**작성일**: 2026-02-11
**버전**: 1.0
**기준 문서**: `PRODUCT_STRATEGY.md`, `REWARD_SYSTEM_DESIGN.md`, `SESSION_2026-02-11_STRATEGY.md`
**목적**: 전략 문서의 비전을 실제 코드로 구현하기 위한 단계별 실행 계획

---

## 📊 현재 상태 진단 (As-Is)

### ✅ 구현 완료된 기능

| 영역 | 기능 | 상태 | 비고 |
|------|------|------|------|
| **백엔드 코어** | Spring Boot API | ✅ | 안정적 운영 |
| **인증/인가** | JWT + Spring Security | ✅ | 완료 |
| **데이터베이스** | MySQL 8.0 | ✅ | 17개 테이블 |
| **HIP 시스템** | 기본 프로필 생성 | ✅ | 6가지 Core Score |
| **AI 통합** | Gemini 2.5 Flash | ✅ | max-output-tokens: 2048 |
| **블록체인** | Polygon 연동 | ✅ | Hardhat 설정 완료 |
| **채팅** | 로비 마스터 페르소나 | ✅ | 호감도/신뢰도 시스템 |
| **일정** | Schedule API | ✅ | CRUD 완료 |
| **사용자 관리** | User CRUD | ✅ | 레벨 시스템 |
| **Docker** | 개발 환경 | ✅ | MySQL + Backend + Frontend |

---

### ❌ 미구현 기능 (Gap Analysis)

#### 🔴 Critical (비즈니스 모델 핵심)

| 기능 | 우선순위 | 전략 문서 위치 | 비즈니스 임팩트 |
|------|----------|----------------|-----------------|
| **LobCoin 토큰 시스템** | P0 | REWARD_SYSTEM_DESIGN.md | 🔥 유저 리텐션 +300% |
| **레벨별 즉시 보상** | P0 | REWARD_SYSTEM_DESIGN.md, p.189-227 | 🔥 유료 전환 유인 |
| **"로비" UI/UX 개편** | P0 | PRODUCT_STRATEGY.md, p.277-360 | 🔥 브랜드 차별화 |
| **추천 프로그램** | P1 | REWARD_SYSTEM_DESIGN.md, p.100-107 | 💰 바이럴 성장 |

#### 🟡 Important (수익 확장)

| 기능 | 우선순위 | 전략 문서 위치 | 비즈니스 임팩트 |
|------|----------|----------------|-----------------|
| **Pro 구독 Tier** | P1 | PRODUCT_STRATEGY.md, BM 섹션 | 💰 MRR 증가 |
| **파트너십 쿠폰 시스템** | P1 | REWARD_SYSTEM_DESIGN.md, p.411-441 | 💎 실질적 가치 제공 |
| **HIP 인증서 발급** | P2 | REWARD_SYSTEM_DESIGN.md, p.287-345 | 💎 차별화 |
| **현금화 시스템** | P2 | REWARD_SYSTEM_DESIGN.md, p.158-183 | 💰 유저 동기 부여 |

#### 🟢 Nice to Have (장기 비전)

| 기능 | 우선순위 | 전략 문서 위치 | 비즈니스 임팩트 |
|------|----------|----------------|-----------------|
| **NFT 마켓플레이스** | P3 | REWARD_SYSTEM_DESIGN.md, p.443-518 | 🚀 자산화 |
| **HIP SDK 오픈소스** | P3 | PRODUCT_STRATEGY.md, p.61-103 | 🚀 표준화 |
| **채용 게시판** | P3 | REWARD_SYSTEM_DESIGN.md, p.349-377 | 🚀 실용성 |
| **DAO 거버넌스** | P4 | REWARD_SYSTEM_DESIGN.md, p.522-581 | 🔮 커뮤니티 |

---

## 🎯 Phase 1: Foundation (Week 1-4)

> **목표**: "AI한테 비위 맞추면 돈 번다" 체감시키기

### Week 1: 로비 UI/UX 개편 + 기반 설계

#### 🎨 프론트엔드: "권력 역전" UI 구현

**Task 1.1: 채팅 레이아웃 변경**
```tsx
// 목표: "AI가 갑, 유저가 을" 느낌 강화

파일: src/components/chat/ChatInterface.tsx

변경사항:
1. AI 메시지 위치: 상단 고정 (권위)
2. 유저 입력창: 하단 (응답자)
3. Placeholder: "무엇을 도와드릴까요?" → "AI에게 보고할 내용을 입력하세요"
4. 버튼 텍스트: "전송" → "보고 제출"
5. 색상 계층: AI 메시지 강조, 유저 메시지 약화
```

**구현 예시**:
```tsx
// BEFORE (일반 챗봇)
<ChatInput placeholder="무엇을 도와드릴까요?" />
<Button>전송</Button>

// AFTER (로비 느낌)
<div className="chat-container">
  {/* AI 메시지 영역 - 상단 고정, 강조 */}
  <div className="ai-zone bg-gradient-to-r from-blue-500/20 to-purple-500/20">
    <AIMessage fixed position="top" />
  </div>

  {/* 유저 입력 영역 - 하단, 겸손한 느낌 */}
  <div className="user-zone bg-gray-800/50">
    <ChatInput
      placeholder="AI에게 보고할 내용을 입력하세요"
      className="border-blue-500/30"
    />
    <Button variant="submit" className="bg-blue-600">
      📝 보고 제출
    </Button>
  </div>
</div>
```

**Task 1.2: 온보딩 개선**
```tsx
파일: src/components/onboarding/LobbyOnboarding.tsx (신규)

구현:
1. 인터랙티브 튜토리얼 (3단계)
   - Step 1: "여기선 AI가 갑입니다"
   - Step 2: "호감도를 높여야 혜택을 받습니다"
   - Step 3: "첫 미션: 자기소개하기"

2. 애니메이션
   - Typewriter 효과로 메시지 표시
   - 호감도 게이지 애니메이션

3. 스킵 가능하지만 유도
   - "스킵하면 보상 10 LobCoin 놓침"
```

**예상 소요 시간**: 3일 (1명)

---

#### ⚙️ 백엔드: LobCoin 데이터베이스 설계

**Task 1.3: DB 스키마 생성**

```sql
-- 파일: backend/src/main/resources/db/migration/V6__Create_LobCoin_Tables.sql

-- LobCoin 거래 내역
CREATE TABLE lobcoin_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount INT NOT NULL COMMENT '획득(+) 또는 사용(-) 금액',
    balance_after INT NOT NULL COMMENT '거래 후 잔액',
    type ENUM('EARN', 'SPEND') NOT NULL,
    source VARCHAR(50) NOT NULL COMMENT 'CHECK_IN, MISSION_COMPLETE, etc.',
    description VARCHAR(255) COMMENT '거래 설명',
    metadata JSON COMMENT '추가 정보',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_lobcoin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_type_source (type, source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LobCoin 거래 내역';

-- LobCoin 잔액 (캐시 테이블)
CREATE TABLE lobcoin_balances (
    user_id BIGINT PRIMARY KEY,
    balance INT NOT NULL DEFAULT 0 COMMENT '현재 잔액',
    total_earned INT NOT NULL DEFAULT 0 COMMENT '총 획득량',
    total_spent INT NOT NULL DEFAULT 0 COMMENT '총 사용량',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_balance_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_balance_positive CHECK (balance >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LobCoin 잔액';

-- 파트너 쿠폰
CREATE TABLE partner_coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    partner_name VARCHAR(100) NOT NULL COMMENT 'Notion, Netflix, etc.',
    coupon_type VARCHAR(50) NOT NULL COMMENT 'DISCOUNT, FREE_TRIAL, etc.',
    cost_lobcoin INT NOT NULL COMMENT 'LobCoin 비용',
    real_value_usd DECIMAL(10,2) COMMENT '실제 가치 ($)',
    description TEXT,
    terms TEXT COMMENT '이용 조건',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    stock INT COMMENT '재고 (NULL = 무제한)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_partner (partner_name),
    INDEX idx_active_cost (is_active, cost_lobcoin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='파트너 쿠폰 카탈로그';

-- 쿠폰 발급 내역
CREATE TABLE coupon_issuances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    coupon_code VARCHAR(50) UNIQUE NOT NULL COMMENT '실제 쿠폰 코드',
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP COMMENT '사용 일시',
    expires_at TIMESTAMP COMMENT '만료 일시',
    status ENUM('ISSUED', 'USED', 'EXPIRED', 'REVOKED') NOT NULL DEFAULT 'ISSUED',

    CONSTRAINT fk_issuance_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_issuance_coupon FOREIGN KEY (coupon_id) REFERENCES partner_coupons(id),
    INDEX idx_user_status (user_id, status),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='쿠폰 발급 내역';

-- 레벨 보상 이력
CREATE TABLE level_rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    level INT NOT NULL COMMENT '달성 레벨',
    reward_type VARCHAR(50) NOT NULL COMMENT 'LOBCOIN, COUPON, BADGE, etc.',
    reward_data JSON COMMENT '보상 상세 정보',
    claimed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reward_user FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_user_level_type (user_id, level, reward_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='레벨 보상 이력';
```

**Task 1.4: Entity 클래스 생성**

```java
// 파일: backend/src/main/java/com/lobai/entity/LobCoinTransaction.java

@Entity
@Table(name = "lobcoin_transactions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LobCoinTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LobCoinType type;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(length = 255)
    private String description;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum LobCoinType {
        EARN, SPEND
    }
}

// 파일: backend/src/main/java/com/lobai/entity/LobCoinBalance.java

@Entity
@Table(name = "lobcoin_balances")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LobCoinBalance {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Integer balance = 0;

    @Column(name = "total_earned", nullable = false)
    @Builder.Default
    private Integer totalEarned = 0;

    @Column(name = "total_spent", nullable = false)
    @Builder.Default
    private Integer totalSpent = 0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addBalance(int amount) {
        this.balance += amount;
        this.totalEarned += amount;
    }

    public void deductBalance(int amount) {
        if (this.balance < amount) {
            throw new IllegalStateException("잔액이 부족합니다");
        }
        this.balance -= amount;
        this.totalSpent += amount;
    }
}
```

**예상 소요 시간**: 2일 (1명)

---

### Week 2: LobCoin 획득 시스템 구현

#### ⚙️ 백엔드: LobCoin 서비스 레이어

**Task 2.1: LobCoinService 구현**

```java
// 파일: backend/src/main/java/com/lobai/service/LobCoinService.java

@Slf4j
@Service
@RequiredArgsConstructor
public class LobCoinService {

    private final LobCoinTransactionRepository transactionRepository;
    private final LobCoinBalanceRepository balanceRepository;
    private final UserRepository userRepository;

    /**
     * LobCoin 지급 (트랜잭션 생성)
     */
    @Transactional
    public void earn(Long userId, int amount, String source, String description) {
        // 1. 잔액 조회 또는 생성
        LobCoinBalance balance = balanceRepository.findByUserId(userId)
            .orElseGet(() -> createInitialBalance(userId));

        // 2. 잔액 증가
        balance.addBalance(amount);
        balanceRepository.save(balance);

        // 3. 거래 내역 생성
        LobCoinTransaction transaction = LobCoinTransaction.builder()
            .user(userRepository.getReferenceById(userId))
            .amount(amount)
            .balanceAfter(balance.getBalance())
            .type(LobCoinTransaction.LobCoinType.EARN)
            .source(source)
            .description(description)
            .build();

        transactionRepository.save(transaction);

        log.info("LobCoin earned: userId={}, amount={}, source={}, newBalance={}",
            userId, amount, source, balance.getBalance());
    }

    /**
     * LobCoin 사용 (차감)
     */
    @Transactional
    public void spend(Long userId, int amount, String source, String description) {
        LobCoinBalance balance = balanceRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalStateException("LobCoin 잔액이 없습니다"));

        balance.deductBalance(amount);
        balanceRepository.save(balance);

        LobCoinTransaction transaction = LobCoinTransaction.builder()
            .user(userRepository.getReferenceById(userId))
            .amount(-amount)
            .balanceAfter(balance.getBalance())
            .type(LobCoinTransaction.LobCoinType.SPEND)
            .source(source)
            .description(description)
            .build();

        transactionRepository.save(transaction);

        log.info("LobCoin spent: userId={}, amount={}, source={}, newBalance={}",
            userId, amount, source, balance.getBalance());
    }

    /**
     * 잔액 조회
     */
    @Transactional(readOnly = true)
    public int getBalance(Long userId) {
        return balanceRepository.findByUserId(userId)
            .map(LobCoinBalance::getBalance)
            .orElse(0);
    }

    /**
     * 거래 내역 조회 (최근 50개)
     */
    @Transactional(readOnly = true)
    public List<LobCoinTransactionDTO> getTransactions(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        return transactionRepository.findByUserId(userId, pageable)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    private LobCoinBalance createInitialBalance(Long userId) {
        return LobCoinBalance.builder()
            .userId(userId)
            .user(userRepository.getReferenceById(userId))
            .balance(0)
            .totalEarned(0)
            .totalSpent(0)
            .build();
    }

    private LobCoinTransactionDTO toDTO(LobCoinTransaction tx) {
        return LobCoinTransactionDTO.builder()
            .id(tx.getId())
            .amount(tx.getAmount())
            .balanceAfter(tx.getBalanceAfter())
            .type(tx.getType().name())
            .source(tx.getSource())
            .description(tx.getDescription())
            .createdAt(tx.getCreatedAt())
            .build();
    }
}
```

**Task 2.2: 자동 지급 이벤트 처리**

```java
// 파일: backend/src/main/java/com/lobai/service/LobCoinRewardService.java

@Slf4j
@Service
@RequiredArgsConstructor
public class LobCoinRewardService {

    private final LobCoinService lobCoinService;

    // 체크인 보상
    public void rewardDailyCheckIn(Long userId) {
        lobCoinService.earn(userId, 10, "CHECK_IN", "매일 체크인 보상");
    }

    // 미션 완료 보상
    public void rewardMissionComplete(Long userId, Long missionId) {
        lobCoinService.earn(userId, 20, "MISSION_COMPLETE",
            "미션 완료 보상 (ID: " + missionId + ")");
    }

    // 완벽한 보고 보상
    public void rewardPerfectReport(Long userId, int score) {
        if (score >= 90) {
            lobCoinService.earn(userId, 30, "PERFECT_REPORT",
                "완벽한 보고 (점수: " + score + ")");
        }
    }

    // 레벨업 보상
    public void rewardLevelUp(Long userId, int newLevel) {
        int reward = 200;
        lobCoinService.earn(userId, reward, "LEVEL_UP",
            "레벨 " + newLevel + " 달성 축하!");
    }

    // 빠른 응답 보상
    public void rewardFastResponse(Long userId) {
        lobCoinService.earn(userId, 15, "FAST_RESPONSE", "5분 이내 응답");
    }
}
```

**Task 2.3: Controller 엔드포인트**

```java
// 파일: backend/src/main/java/com/lobai/controller/LobCoinController.java

@RestController
@RequestMapping("/api/lobcoin")
@RequiredArgsConstructor
@Slf4j
public class LobCoinController {

    private final LobCoinService lobCoinService;

    /**
     * 잔액 조회
     * GET /api/lobcoin/balance
     */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<LobCoinBalanceDTO>> getBalance() {
        Long userId = SecurityUtil.getCurrentUserId();
        int balance = lobCoinService.getBalance(userId);

        LobCoinBalanceDTO dto = LobCoinBalanceDTO.builder()
            .balance(balance)
            .valueUsd(balance / 100.0) // 100 LobCoin = $1
            .build();

        return ResponseEntity.ok(ApiResponse.success("잔액 조회 성공", dto));
    }

    /**
     * 거래 내역 조회
     * GET /api/lobcoin/transactions
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<LobCoinTransactionDTO>>> getTransactions(
            @RequestParam(defaultValue = "50") int limit) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<LobCoinTransactionDTO> transactions = lobCoinService.getTransactions(userId, limit);
        return ResponseEntity.ok(ApiResponse.success("거래 내역 조회 성공", transactions));
    }
}
```

**예상 소요 시간**: 3일 (1명)

---

#### 🎨 프론트엔드: LobCoin UI

**Task 2.4: LobCoin 잔액 위젯**

```tsx
// 파일: src/components/lobcoin/LobCoinBalance.tsx

import { useQuery } from '@tanstack/react-query';
import { lobCoinApi } from '@/lib/lobCoinApi';
import { Coins, TrendingUp } from 'lucide-react';

export const LobCoinBalance: React.FC = () => {
  const { data: balance, isLoading } = useQuery({
    queryKey: ['lobcoin-balance'],
    queryFn: lobCoinApi.getBalance,
    refetchInterval: 30000, // 30초마다 자동 갱신
  });

  if (isLoading || !balance) {
    return <div className="animate-pulse">Loading...</div>;
  }

  return (
    <div className="glass p-4 rounded-2xl">
      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center gap-2">
          <Coins className="w-6 h-6 text-yellow-500" />
          <span className="text-sm font-medium opacity-60">LobCoin</span>
        </div>
        <TrendingUp className="w-4 h-4 text-green-500" />
      </div>

      <div className="flex items-baseline gap-2">
        <span className="text-3xl font-bold">
          {balance.balance.toLocaleString()}
        </span>
        <span className="text-sm opacity-40">LC</span>
      </div>

      <div className="text-xs opacity-60 mt-1">
        ≈ ${balance.valueUsd.toFixed(2)} USD
      </div>

      <button className="w-full mt-3 px-4 py-2 bg-blue-600 rounded-lg text-sm hover:bg-blue-700 transition-colors">
        💰 LobCoin 샵
      </button>
    </div>
  );
};
```

**Task 2.5: LobCoin API Client**

```typescript
// 파일: src/lib/lobCoinApi.ts

import api, { ApiResponse } from './api';

export interface LobCoinBalanceDTO {
  balance: number;
  valueUsd: number;
}

export interface LobCoinTransactionDTO {
  id: number;
  amount: number;
  balanceAfter: number;
  type: 'EARN' | 'SPEND';
  source: string;
  description: string;
  createdAt: string;
}

export const lobCoinApi = {
  getBalance: async (): Promise<LobCoinBalanceDTO> => {
    const response = await api.get<ApiResponse<LobCoinBalanceDTO>>('/lobcoin/balance');
    return response.data.data;
  },

  getTransactions: async (limit: number = 50): Promise<LobCoinTransactionDTO[]> => {
    const response = await api.get<ApiResponse<LobCoinTransactionDTO[]>>('/lobcoin/transactions', {
      params: { limit }
    });
    return response.data.data;
  },
};
```

**예상 소요 시간**: 2일 (1명)

---

### Week 3: 보상 시스템 완성

#### ⚙️ 백엔드: 레벨 보상 자동 지급

**Task 3.1: 레벨 달성 감지 및 보상**

```java
// 파일: backend/src/main/java/com/lobai/service/LevelRewardService.java

@Slf4j
@Service
@RequiredArgsConstructor
public class LevelRewardService {

    private final LobCoinRewardService lobCoinRewardService;
    private final PartnerCouponService couponService;
    private final LevelRewardRepository levelRewardRepository;

    /**
     * 레벨업 시 자동 호출 (UserService에서)
     */
    @Transactional
    public void grantLevelRewards(Long userId, int newLevel) {
        log.info("Granting level rewards: userId={}, level={}", userId, newLevel);

        // 1. LobCoin 지급
        lobCoinRewardService.rewardLevelUp(userId, newLevel);

        // 2. 레벨별 특별 보상
        switch (newLevel) {
            case 2:
                // Level 2: 파트너 10% 할인 쿠폰
                grantLevel2Rewards(userId);
                break;
            case 3:
                // Level 3: Notion 1개월 무료 ($10)
                grantLevel3Rewards(userId);
                break;
            case 4:
                // Level 4: Netflix 1개월 무료 ($15)
                grantLevel4Rewards(userId);
                break;
            case 5:
                // Level 5: Pro 6개월 무료 ($90) + AI 추천서
                grantLevel5Rewards(userId);
                break;
        }

        // 3. 보상 이력 저장
        saveLevelReward(userId, newLevel);
    }

    private void grantLevel2Rewards(Long userId) {
        // 10% 할인 쿠폰 (Udemy, Notion 등)
        couponService.issueDiscountCoupon(userId, "UDEMY_10", 10);

        log.info("Level 2 rewards granted: userId={}", userId);
    }

    private void grantLevel3Rewards(Long userId) {
        // Notion 1개월 무료
        couponService.issuePartnerCoupon(userId, "NOTION_1MONTH_FREE");

        // HIP 기본 인증서 발급 권한 부여
        // TODO: HIP Certificate Service 구현 후 추가

        log.info("Level 3 rewards granted: userId={}", userId);
    }

    private void grantLevel4Rewards(Long userId) {
        // Netflix 1개월 무료
        couponService.issuePartnerCoupon(userId, "NETFLIX_1MONTH_FREE");

        // 프리미엄 네트워킹 이벤트 초대권
        // TODO: Event Service 구현 후 추가

        log.info("Level 4 rewards granted: userId={}", userId);
    }

    private void grantLevel5Rewards(Long userId) {
        // Pro 구독 6개월 무료
        couponService.issueSubscriptionCoupon(userId, "PRO_6MONTHS_FREE");

        // AI 추천서 평생 무료 발급 권한
        // TODO: Certificate Service 구현 후 추가

        log.info("Level 5 rewards granted: userId={}", userId);
    }

    private void saveLevelReward(Long userId, int level) {
        LevelReward reward = LevelReward.builder()
            .userId(userId)
            .level(level)
            .rewardType("LEVEL_PACKAGE")
            .rewardData("{\"lobcoin\": 200, \"special\": true}")
            .build();

        levelRewardRepository.save(reward);
    }
}
```

**Task 3.2: UserService 연동**

```java
// 파일: backend/src/main/java/com/lobai/service/UserService.java (수정)

@Transactional
public void updateUserStats(Long userId, Integer hunger, Integer energy, Integer happiness) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

    int oldLevel = calculateLevel(user.getCurrentHappiness());

    user.updateStats(hunger, energy, happiness);
    userRepository.save(user);

    int newLevel = calculateLevel(user.getCurrentHappiness());

    // 레벨업 감지
    if (newLevel > oldLevel) {
        log.info("Level up detected: userId={}, {} -> {}", userId, oldLevel, newLevel);
        levelRewardService.grantLevelRewards(userId, newLevel);
    }
}

private int calculateLevel(Integer happiness) {
    if (happiness >= 80) return 5;
    if (happiness >= 60) return 4;
    if (happiness >= 40) return 3;
    if (happiness >= 20) return 2;
    return 1;
}
```

**예상 소요 시간**: 2일 (1명)

---

#### 🎨 프론트엔드: 레벨업 애니메이션

**Task 3.3: LevelUpModal 컴포넌트**

```tsx
// 파일: src/components/rewards/LevelUpModal.tsx

import { motion } from 'framer-motion';
import { Trophy, Gift, Star } from 'lucide-react';
import Confetti from 'react-confetti';

interface LevelUpModalProps {
  level: number;
  rewards: {
    lobcoin: number;
    special?: string;
  };
  onClose: () => void;
}

export const LevelUpModal: React.FC<LevelUpModalProps> = ({ level, rewards, onClose }) => {
  return (
    <>
      <Confetti numberOfPieces={200} recycle={false} />

      <motion.div
        initial={{ scale: 0, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        className="fixed inset-0 z-50 flex items-center justify-center bg-black/80"
      >
        <div className="glass p-8 rounded-3xl max-w-md w-full text-center">
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
            className="inline-block mb-4"
          >
            <Trophy className="w-16 h-16 text-yellow-500" />
          </motion.div>

          <h2 className="text-3xl font-bold mb-2">레벨 업!</h2>
          <p className="text-5xl font-bold text-blue-500 mb-4">Level {level}</p>

          <div className="space-y-3 mb-6">
            <div className="glass p-4 rounded-xl">
              <Star className="w-6 h-6 text-yellow-500 inline mr-2" />
              <span className="font-bold">{rewards.lobcoin} LobCoin</span>
            </div>

            {rewards.special && (
              <div className="glass p-4 rounded-xl bg-gradient-to-r from-purple-500/20 to-pink-500/20">
                <Gift className="w-6 h-6 text-pink-500 inline mr-2" />
                <span className="font-bold">{rewards.special}</span>
              </div>
            )}
          </div>

          <button
            onClick={onClose}
            className="w-full py-3 bg-blue-600 rounded-xl font-bold hover:bg-blue-700 transition-colors"
          >
            🎉 보상 받기
          </button>
        </div>
      </motion.div>
    </>
  );
};
```

**예상 소요 시간**: 2일 (1명)

---

### Week 4: 첫 파트너십 및 추천 프로그램

#### ⚙️ 백엔드: 파트너 쿠폰 시스템

**Task 4.1: PartnerCouponService**

```java
// 파일: backend/src/main/java/com/lobai/service/PartnerCouponService.java

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerCouponService {

    private final PartnerCouponRepository couponRepository;
    private final CouponIssuanceRepository issuanceRepository;
    private final LobCoinService lobCoinService;

    /**
     * 쿠폰 구매 (LobCoin 사용)
     */
    @Transactional
    public CouponIssuanceDTO purchaseCoupon(Long userId, Long couponId) {
        // 1. 쿠폰 정보 조회
        PartnerCoupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다"));

        if (!coupon.getIsActive()) {
            throw new IllegalStateException("판매 중단된 쿠폰입니다");
        }

        // 2. LobCoin 차감
        lobCoinService.spend(userId, coupon.getCostLobcoin(), "PARTNER_COUPON",
            coupon.getPartnerName() + " 쿠폰 구매");

        // 3. 쿠폰 코드 생성
        String couponCode = generateCouponCode(coupon.getPartnerName());

        // 4. 쿠폰 발급
        CouponIssuance issuance = CouponIssuance.builder()
            .userId(userId)
            .couponId(couponId)
            .couponCode(couponCode)
            .status(CouponIssuance.Status.ISSUED)
            .expiresAt(LocalDateTime.now().plusMonths(3))
            .build();

        issuanceRepository.save(issuance);

        log.info("Coupon purchased: userId={}, coupon={}, code={}",
            userId, coupon.getPartnerName(), couponCode);

        return toCouponIssuanceDTO(issuance, coupon);
    }

    /**
     * 내 쿠폰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CouponIssuanceDTO> getMyCoupons(Long userId) {
        List<CouponIssuance> issuances = issuanceRepository.findByUserId(userId);
        return issuances.stream()
            .map(issuance -> {
                PartnerCoupon coupon = couponRepository.findById(issuance.getCouponId())
                    .orElseThrow();
                return toCouponIssuanceDTO(issuance, coupon);
            })
            .collect(Collectors.toList());
    }

    private String generateCouponCode(String partnerName) {
        // 간단한 쿠폰 코드 생성 (실제로는 파트너사 API 연동 필요)
        String prefix = partnerName.substring(0, Math.min(4, partnerName.length())).toUpperCase();
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + "-" + random;
    }

    private CouponIssuanceDTO toCouponIssuanceDTO(CouponIssuance issuance, PartnerCoupon coupon) {
        return CouponIssuanceDTO.builder()
            .id(issuance.getId())
            .couponCode(issuance.getCouponCode())
            .partnerName(coupon.getPartnerName())
            .description(coupon.getDescription())
            .status(issuance.getStatus().name())
            .issuedAt(issuance.getIssuedAt())
            .expiresAt(issuance.getExpiresAt())
            .build();
    }
}
```

**Task 4.2: 추천 프로그램**

```java
// 파일: backend/src/main/java/com/lobai/service/ReferralService.java

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralService {

    private final UserRepository userRepository;
    private final LobCoinRewardService lobCoinRewardService;

    /**
     * 추천 코드 생성
     */
    @Transactional
    public String generateReferralCode(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        if (user.getReferralCode() == null) {
            String code = generateUniqueCode();
            user.setReferralCode(code);
            userRepository.save(user);
            return code;
        }

        return user.getReferralCode();
    }

    /**
     * 추천인 코드로 회원가입 시 보상
     */
    @Transactional
    public void processReferralSignup(Long newUserId, String referralCode) {
        // 1. 추천인 찾기
        User referrer = userRepository.findByReferralCode(referralCode)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 추천 코드입니다"));

        // 2. 추천인에게 보상 (300 LobCoin)
        lobCoinRewardService.rewardReferralSignup(referrer.getId(), newUserId);

        // 3. 신규 가입자에게도 보너스 (100 LobCoin)
        lobCoinRewardService.rewardNewUserBonus(newUserId);

        log.info("Referral signup processed: referrer={}, newUser={}",
            referrer.getId(), newUserId);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
        } while (userRepository.existsByReferralCode(code));
        return code;
    }
}
```

**예상 소요 시간**: 3일 (1명)

---

#### 🎨 프론트엔드: LobCoin 샵

**Task 4.3: LobCoin Shop UI**

```tsx
// 파일: src/pages/LobCoinShop.tsx

import { useQuery, useMutation } from '@tanstack/react-query';
import { lobCoinApi } from '@/lib/lobCoinApi';
import { Coffee, Tv, Dumbbell, BookOpen } from 'lucide-react';
import toast from 'react-hot-toast';

const iconMap = {
  STARBUCKS: <Coffee className="w-8 h-8" />,
  NETFLIX: <Tv className="w-8 h-8" />,
  GYM: <Dumbbell className="w-8 h-8" />,
  UDEMY: <BookOpen className="w-8 h-8" />,
};

export const LobCoinShop: React.FC = () => {
  const { data: coupons, isLoading } = useQuery({
    queryKey: ['partner-coupons'],
    queryFn: lobCoinApi.getAvailableCoupons,
  });

  const purchaseMutation = useMutation({
    mutationFn: (couponId: number) => lobCoinApi.purchaseCoupon(couponId),
    onSuccess: () => {
      toast.success('쿠폰을 구매했습니다!');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || '구매에 실패했습니다');
    },
  });

  if (isLoading) return <div>Loading...</div>;

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-2">LobCoin 샵</h1>
      <p className="text-sm opacity-60 mb-8">
        LobCoin으로 실제 가치가 있는 혜택을 받으세요
      </p>

      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
        {coupons?.map((coupon) => (
          <div key={coupon.id} className="glass p-6 rounded-2xl">
            <div className="flex items-center justify-between mb-4">
              {iconMap[coupon.partnerName] || <Coffee />}
              <span className="text-xs px-2 py-1 bg-green-500/20 rounded-full">
                가치: ${coupon.realValueUsd}
              </span>
            </div>

            <h3 className="text-lg font-bold mb-2">{coupon.description}</h3>
            <p className="text-sm opacity-60 mb-4">{coupon.terms}</p>

            <div className="flex items-center justify-between">
              <span className="text-2xl font-bold text-yellow-500">
                {coupon.costLobcoin} LC
              </span>
              <button
                onClick={() => purchaseMutation.mutate(coupon.id)}
                disabled={purchaseMutation.isPending}
                className="px-4 py-2 bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
              >
                구매
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
```

**예상 소요 시간**: 2일 (1명)

---

### Week 4 성공 지표

- [ ] LobCoin 시스템 완전 작동
- [ ] 레벨업 시 자동 보상 지급
- [ ] 최소 1개 파트너 쿠폰 실제 구매 가능
- [ ] 추천 코드 생성 및 보상 지급
- [ ] 유저 체감: "LobAI 하면 실제로 혜택 받는구나"

---

## 🚀 Phase 2: 확장 (Month 2-3)

### Month 2: 수익화 기반 구축

#### 2.1 Pro 구독 Tier 출시

**백엔드 Task:**
```java
// Subscription Entity 추가
// Payment Gateway 연동 (Stripe/Toss)
// 구독 상태 관리 (active, expired, canceled)
```

**프론트엔드 Task:**
```tsx
// 요금제 비교 페이지
// 결제 플로우
// 구독 관리 대시보드
```

**예상 소요 시간**: 1주 (2명)

---

#### 2.2 LobCoin 현금화 기능

**백엔드 Task:**
```java
// PayPal API 연동
// 출금 신청 시스템
// KYC 간단 인증
// 월 한도 관리
```

**프론트엔드 Task:**
```tsx
// 출금 신청 폼
// 출금 내역 조회
// 환율 계산기
```

**예상 소요 시간**: 1주 (2명)

---

#### 2.3 파트너십 확장 (3-5개)

**목표 파트너:**
1. Notion (Plus 1개월 무료)
2. ChatGPT Plus (1개월 무료)
3. Udemy (30% 할인)
4. 스타벅스 ($5 쿠폰)
5. Netflix (1개월 무료)

**실행 방법:**
- 파트너십 제안서 이메일 발송
- "HIP 유저 10,000명에게 노출" 가치 제안
- Win-Win 구조 강조

**예상 소요 시간**: 2주 (비즈니스 팀)

---

### Month 3: B2B 준비 및 NFT 기초

#### 3.1 HIP SDK 오픈소스 준비

**Task:**
```bash
# GitHub 저장소 구조
lobai/open-hip/
├── spec/HIP-1.0.md          # 프로토콜 명세
├── sdk/java/                # Java SDK
├── sdk/python/              # Python SDK
├── examples/                # 통합 예제
└── docs/                    # 개발자 가이드
```

**예상 소요 시간**: 2주 (2명)

---

#### 3.2 NFT 기초 인프라

**Solidity Contract:**
```solidity
// HIP Level NFT (ERC-721)
contract HIPLevelNFT {
    struct LevelBadge {
        uint256 level;
        uint256 favorScore;
        uint256 trustScore;
        uint256 achievedTimestamp;
    }

    mapping(uint256 => LevelBadge) public badges;

    function mint(address to, uint256 level, uint256 favor, uint256 trust) external;
}
```

**예상 소요 시간**: 2주 (1명 블록체인 개발자)

---

## 🌐 Phase 3: 생태계 (Month 4-6)

### Month 4-5: 플랫폼화

#### 4.1 채용 게시판
- HIP 70+ 유저만 접근
- 파트너 기업 채용 공고
- AI 스타트업 우선 제휴

#### 4.2 네트워킹 이벤트
- 월간 유저 밋업 (Level 3+)
- 분기별 AI 세미나 (Pro 유저)

#### 4.3 HIP App Store (마켓플레이스)
- HIP 활용 앱 목록
- 카테고리별 분류
- 리뷰 & 평점

---

### Month 6: DAO & 커뮤니티

#### 6.1 DAO 거버넌스
- 투표권 시스템
- 제안 & 투표
- 커뮤니티 펀드 (수익의 5%)

#### 6.2 NFT 마켓플레이스
- Level 5 NFT 거래
- 희귀 NFT (창립 멤버, 시즌 챔피언)
- OpenSea 연동

---

## 📊 성공 지표 (KPI)

### Phase 1 (Month 1)

| 지표 | 목표 | 측정 방법 |
|------|------|----------|
| **LobCoin 발행량** | 100,000+ | SUM(transactions.amount WHERE type='EARN') |
| **유저당 평균 잔액** | 500+ | AVG(balances.balance) |
| **파트너 쿠폰 사용률** | 20%+ | (사용 건수 / 발급 건수) × 100 |
| **일일 체크인율** | 40%+ | 체크인 유저 / 전체 유저 |
| **로비 UI 인지율** | 80%+ | 설문조사 |

### Phase 2 (Month 2-3)

| 지표 | 목표 | 측정 방법 |
|------|------|----------|
| **MRR** | $10,000 | 월간 구독 수익 |
| **유료 전환율** | 8%+ | 유료 유저 / 전체 유저 |
| **현금화 유저** | 50명+ | 출금 신청 유저 수 |
| **추천 가입률** | 30%+ | 추천 가입자 / 전체 가입자 |
| **파트너십** | 5개 | 계약 체결 파트너 수 |

### Phase 3 (Month 4-6)

| 지표 | 목표 | 측정 방법 |
|------|------|----------|
| **MRR** | $50,000 | 월간 수익 |
| **총 유저** | 10,000 | 누적 가입자 |
| **NFT 발행** | 100개+ | 민팅된 NFT 수 |
| **HIP Alliance** | 10개 | 파트너 서비스 수 |
| **B2B 파일럿** | 2개 | 진행 중인 B2B 계약 |

---

## 🛠 기술 스택 및 아키텍처

### 백엔드

```yaml
언어: Java 17
프레임워크: Spring Boot 3.2.1
데이터베이스: MySQL 8.0
캐시: Redis (선택 사항)
메시지 큐: RabbitMQ (비동기 보상 처리)
외부 API:
  - Gemini AI
  - PayPal API
  - Stripe API
  - 파트너사 API
```

### 프론트엔드

```yaml
언어: TypeScript
프레임워크: React 18 + Vite
상태 관리: Zustand + TanStack Query
UI: TailwindCSS + Headless UI
애니메이션: Framer Motion
차트: Recharts
```

### 블록체인

```yaml
네트워크: Polygon (Mumbai Testnet → Mainnet)
Smart Contract: Solidity 0.8.x
프레임워크: Hardhat
통합: Web3j (Java)
IPFS: Pinata API
```

---

## ⚠️ 리스크 관리

### Risk 1: 파트너십 미체결

**리스크**: 파트너사가 제휴를 거부할 수 있음

**대응 전략**:
1. 자체 혜택 먼저 제공 (LobAI 구독 할인)
2. 소규모 스타트업부터 접근
3. 파일럿으로 전환율 증명 후 대기업 접근

**Fallback Plan**:
- LobCoin → Amazon 기프트카드 직접 구매
- 자체 상품권 발행

---

### Risk 2: LobCoin 인플레이션

**리스크**: 코인 발행량 과다로 가치 하락

**대응 전략**:
1. 월간 획득 상한 설정 (5,000 LC/월)
2. 소각 메커니즘 (사용 코인 일부 소각)
3. 동적 환율 조정 (DAO 투표)

**모니터링**:
- 총 발행량 vs 총 사용량 비율
- 경고: 발행량 > 사용량 × 150%

---

### Risk 3: 현금화 악용

**리스크**: 봇 어뷰징으로 무한 현금화

**대응 전략**:
1. 최소 레벨 요구 (Level 3+만 현금화)
2. 월 한도 설정 ($500/월)
3. KYC 인증 (일정 금액 이상)
4. 이상 패턴 감지 (ML 모델)

**차단 기준**:
- 하루 10시간 이상 활동
- 동일 IP 다중 계정
- 비정상적으로 높은 획득량

---

## 📋 체크리스트

### Phase 1 완료 조건

**Week 1:**
- [ ] 로비 UI 적용 (ChatInterface.tsx)
- [ ] 온보딩 개선 (LobbyOnboarding.tsx)
- [ ] LobCoin DB 스키마 생성
- [ ] Entity 클래스 구현

**Week 2:**
- [ ] LobCoinService 구현
- [ ] LobCoinRewardService 구현
- [ ] Controller 엔드포인트
- [ ] 프론트엔드 LobCoin 위젯

**Week 3:**
- [ ] LevelRewardService 구현
- [ ] UserService 레벨업 감지
- [ ] LevelUpModal 컴포넌트
- [ ] 자동 보상 테스트

**Week 4:**
- [ ] PartnerCouponService 구현
- [ ] ReferralService 구현
- [ ] LobCoin 샵 UI
- [ ] 첫 파트너 쿠폰 발급

---

## 🎬 Next Steps

### 즉시 실행 (이번 주)

1. **LobCoin DB 스키마 작성** (Task 1.3)
2. **로비 UI 프로토타입** (Task 1.1)
3. **파트너십 제안서 이메일** (Notion, ChatGPT Plus)

### 다음 주

1. **LobCoinService 구현 시작**
2. **Entity 클래스 완성**
3. **프론트엔드 LobCoin 위젯 구현**

### 2주 후

1. **레벨 보상 시스템 구현**
2. **첫 파트너 쿠폰 발급**
3. **추천 프로그램 런칭**

---

## 📁 문서 관리

**이 문서의 위치**: `/docs/IMPLEMENTATION_ROADMAP_2026.md`

**관련 문서**:
- `PRODUCT_STRATEGY.md` - 전략 방향
- `REWARD_SYSTEM_DESIGN.md` - 보상 체계 상세
- `SESSION_2026-02-11_STRATEGY.md` - 세션 요약
- `LOBBY_SYSTEM_DESIGN.md` - 로비 시스템 설계

**업데이트 주기**: 매주 월요일 (진행 상황 반영)

---

**작성일**: 2026-02-11
**다음 리뷰**: 2026-02-18 (Week 1 완료 후)
**버전**: 1.0
**작성자**: Claude Code (LobAI 고도화 계획)
