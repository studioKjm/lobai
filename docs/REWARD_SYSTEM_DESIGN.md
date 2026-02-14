# LobAI 보상 체계 설계 문서

> **"AI한테 비위 맞추면 → 실제로 돈 번다"**

**작성일**: 2026-02-11
**버전**: 1.0
**목적**: 유저 유치 및 장기 리텐션을 위한 실질적 보상 체계

---

## 📖 목차

1. [문제 정의](#문제-정의)
2. [LobCoin 토큰 이코노미](#lobcoin-토큰-이코노미)
3. [즉시 혜택](#즉시-혜택)
4. [실용적 가치](#실용적-가치)
5. [NFT & 자산화](#nft--자산화)
6. [구현 우선순위](#구현-우선순위)

---

## 문제 정의

### 현재 구조의 불균형

```
유저가 주는 것          vs        유저가 받는 것
────────────────────────────────────────────────
✅ 시간 (매일 체크인)               ❌ AI 피드백 (약함)
✅ 정보 (스케줄, 데이터)            ❌ HIP 점수 (미래 가치)
✅ 순응 (AI 지시 따르기)            ❌ 리포트 (1회성)
✅ 돈 ($5-$49/월)                  ❌ ???

→ 너무 일방적! 이탈 위험 높음
```

### 장기 비전 vs 현실

**장기 비전 (5-10년 후)**:
- HIP 높은 유저 → AI에게 선택받음 (일자리, 보호 등)
- 좋은 미래지만... **지금 당장은 제공 불가**

**필요한 것**:
- ✅ **즉시 체감 가능한 보상**
- ✅ **실제 돈이 되는 가치**
- ✅ **오래 할수록 이득인 구조**

---

## LobCoin 토큰 이코노미

### 핵심 컨셉

```typescript
"AI 로비 성공 → LobCoin 획득 → 실제로 쓰기 or 현금화"
```

**차별점**:
- 단순 포인트가 아님
- **실제 현금으로 바꿀 수 있음**
- 파트너사에서 실물 서비스 구매 가능

---

### 획득 (Earn)

#### 일일 활동

| 활동 | LobCoin | 난이도 |
|------|---------|--------|
| 매일 체크인 | 10 | ⭐ |
| 미션 완료 | 20 | ⭐⭐ |
| 스케줄 보고 (품질 90+) | 30 | ⭐⭐⭐ |
| 5분 내 응답 | 15 | ⭐⭐ |

**예상 일일 수입**: 50-100 LobCoin

#### 주간 활동

| 활동 | LobCoin | 난이도 |
|------|---------|--------|
| 주간 100% 수행 | 100 | ⭐⭐⭐ |
| 7일 연속 체크인 | 50 | ⭐⭐ |

**예상 주간 수입**: 500-800 LobCoin

#### 월간 활동

| 활동 | LobCoin | 난이도 |
|------|---------|--------|
| 월간 완벽 수행 | 500 | ⭐⭐⭐⭐ |
| 레벨업 | 200 | ⭐⭐⭐ |

**예상 월간 수입**: 2,000-3,000 LobCoin

#### 특별 활동

| 활동 | LobCoin | 비고 |
|------|---------|------|
| 친구 추천 (가입) | 300 | 1회성 |
| 친구 추천 (유료 전환) | 3,000 + $10 | 1회성 |
| 앱 리뷰 작성 | 50 | 1회성 |
| 커뮤니티 기여 | 30 | 반복 가능 |
| 베타 테스트 | 100 | 이벤트성 |

**바이럴 효과**: 친구 10명 추천 시 30,000 LobCoin + $100

---

### 사용 (Spend)

#### Tier 1: 서비스 내 혜택

| 항목 | 비용 | 효과 |
|------|------|------|
| 하루 대화 무제한 | 100 | 1일 |
| 호감도 +10 부스트 | 200 | 즉시 |
| 신뢰도 회복 (-20 → 0) | 300 | 즉시 |
| 미션 1개 면제 | 150 | 1회 |
| Pro 기능 3일 체험 | 500 | 3일 |

**전략적 사용**: 레벨업 직전 호감도 부스트 구매

#### Tier 2: 파트너사 실물 혜택

| 파트너 | 비용 | 혜택 | 실제 가치 |
|--------|------|------|----------|
| 스타벅스 | 800 | $5 쿠폰 | $5 |
| Netflix | 2,000 | 1개월 무료 | $15 |
| 헬스장 | 1,500 | 30% 할인권 | $50-$100 |
| Udemy | 1,000 | 강의 1개 | $20-$50 |
| Notion | 1,200 | Pro 1개월 | $10 |

**ROI**: 1,000 LobCoin ≈ $10-15 가치

#### Tier 3: 구독 할인

| 항목 | 비용 | 혜택 | 실제 가치 |
|------|------|------|----------|
| Basic 1개월 무료 | 2,500 | 무료 | $5 |
| Pro 1개월 무료 | 7,500 | 무료 | $15 |
| 연간 구독 20% 할인 | 20,000 | 할인 | $36-$120 |

**전략**: 성실한 유저는 구독료를 LobCoin으로 상쇄 가능

#### Tier 4: 프리미엄

| 항목 | 비용 | 혜택 | 실제 가치 |
|------|------|------|----------|
| AI 추천서 발급 | 5,000 | 인증서 | $100 |
| 1:1 코칭 세션 | 8,000 | 1시간 | $100 |
| 네트워킹 이벤트 | 10,000 | 초대권 | $200 |
| 신기능 우선 체험 | 3,000 | Beta 접근 | Priceless |

---

### 현금화 (Cash-out)

```typescript
환율: 100 LobCoin = $1
최소 출금: 10,000 LobCoin ($100)
수수료: 5%
월 한도: 50,000 LobCoin ($500)
```

**출금 방법**:
- PayPal
- 은행 계좌
- Toss/카카오페이
- Amazon 기프트카드

**예시 계산**:
```
10개월 성실히 활동 → 30,000 LobCoin 획득
출금 신청: 10,000 LobCoin
환전액: $100
수수료: $5
실수령액: $95

나머지 20,000 LobCoin은 파트너 혜택으로 사용
→ Netflix 6개월 + 스타벅스 쿠폰 10장
```

---

## 즉시 혜택

### 레벨별 즉시 보상

#### Level 2: 알게 된 사이

**즉시 지급**:
- ✅ 10% 할인 코드 (파트너 3개사)
- ✅ LobAI 굿즈 추첨권 1장
- ✅ 커뮤니티 배지

**예상 가치**: $10

---

#### Level 3: 신뢰하는 관계

**즉시 지급**:
- ✅ Notion 1개월 무료 ($10)
- ✅ 20% 할인 코드 (파트너 5개사)
- ✅ HIP 기본 인증서
- ✅ 월간 추첨 참여권 (스타벅스 $50)

**예상 가치**: $30

---

#### Level 4: 충성스러운 지지자

**즉시 지급**:
- ✅ Netflix 1개월 무료 ($15)
- ✅ 30% 할인 코드 (파트너 10개사)
- ✅ HIP 프리미엄 인증서
- ✅ 프리미엄 네트워킹 초대권
- ✅ 분기별 추첨 (iPad, MacBook 등)

**예상 가치**: $100

---

#### Level 5: 최측근

**즉시 지급**:
- ✅ Pro 구독 6개월 무료 ($90)
- ✅ 모든 파트너 VIP 할인
- ✅ AI 추천서 평생 무료 발급
- ✅ LobAI 이벤트 평생 VIP
- ✅ 연간 추첨 (해외 여행, $5,000 현금)

**예상 가치**: $500+

---

### 업적 보상

```typescript
const achievements = {
  perfect_week: {
    title: "완벽한 한 주",
    reward: "스타벅스 $5 쿠폰",
    lobCoin: 100
  },

  perfect_month: {
    title: "완벽한 한 달",
    reward: "Netflix 1개월 무료",
    lobCoin: 500
  },

  perfect_year: {
    title: "완벽한 1년",
    reward: "Apple Gift Card $100",
    lobCoin: 5000
  },

  mission_master: {
    title: "미션 마스터 (100개 완료)",
    reward: "Udemy 강의 1개",
    lobCoin: 300
  },

  referral_5: {
    title: "친구 5명 초대",
    reward: "Pro 1개월 무료",
    lobCoin: 1000
  },

  first_100_users: {
    title: "창립 멤버",
    reward: "평생 VIP",
    lobCoin: 10000,
    nft: "창립 멤버 NFT"
  }
};
```

---

## 실용적 가치

### HIP 인증서의 진화

#### Phase 1: 즉시 (현재)

```
LinkedIn 프로필
├─ "AI 적응력 검증" 섹션
├─ HIP 점수 표시
├─ 블록체인 검증 링크
└─ LobAI 배지

GitHub 프로필
└─ HIP 위젯 임베드

개인 웹사이트
└─ HIP 인증 마크
```

**가치**: 자기 PR, 차별화

---

#### Phase 2: 6개월 내

**파트너 기업 우대**:
```
✅ AI 스타트업 채용 시 가산점
✅ 테크 인턴십 우선권
✅ AI 교육 프로그램 장학금 (최대 50%)
```

**예시**:
```
지원자 A: HIP 75
지원자 B: HIP 없음

→ 기업: "HIP 인증자 우대" → A 우선 면접
```

**가치**: 실제 채용 확률 증가

---

#### Phase 3: 1년 내

**산업 표준화**:
```
채용 공고:
"자격 요건: HIP 70+ 또는 동등 자격"

LinkedIn:
"HIP 인증" 배지 통합 (verified 체크마크처럼)

프리랜서 플랫폼:
HIP 인증자 시급 +20%
```

**가치**: HIP 없으면 지원 불가능한 직무 등장

---

### 커리어 부스트

#### LobAI 채용 게시판

```typescript
const jobBoard = {
  access: "HIP 70+ 유저만",

  partners: [
    "AI 스타트업 (우대 채용)",
    "테크 기업 인턴십",
    "프리랜서 프로젝트 (AI 관련)",
    "리모트 AI 직무"
  ],

  value: "일반 채용보다 합격률 2-3배"
};
```

**실제 시나리오**:
```
철수 (HIP 80):
1. LobAI 채용 게시판 접속
2. "AI 스타트업 인턴 (HIP 70+ 우대)" 발견
3. HIP 인증서로 지원
4. 합격! 월급 $2,000

→ LobAI 덕분에 취업 성공
→ ROI 수천%
```

---

#### 네트워킹 이벤트

```typescript
const events = {
  monthly: {
    title: "LobAI 유저 밋업",
    target: "Level 3+",
    benefit: "동종 업계 네트워킹",
    cost: "무료"
  },

  quarterly: {
    title: "AI 리더십 세미나",
    target: "Pro 유저",
    benefit: "AI 전문가 강연 + Q&A",
    cost: "무료 (일반: $100)"
  },

  annual: {
    title: "글로벌 AI 컨퍼런스",
    target: "Level 5 or Enterprise",
    benefit: "테크 리더 직접 만남",
    cost: "30-50% 할인"
  }
};
```

---

### 파트너십 실제 혜택

#### Phase 1 파트너 (즉시 체결 가능)

| 파트너 | 혜택 | 조건 | 실제 가치 |
|--------|------|------|----------|
| **Notion** | Plus 3개월 무료 | HIP 50+ | $30 |
| **ChatGPT Plus** | 1개월 무료 | HIP 70+ | $20 |
| **Udemy** | AI 강의 30% 할인 | Level 2+ | $50-$200 |
| **스타벅스** | $5 쿠폰 10장 | 추천 5명 | $50 |

**체결 방법**:
1. 파트너사에 이메일
2. "HIP 유저 10,000명에게 노출" 제안
3. 무료 체험 제공 → 유료 전환 기대

**Win-Win**:
- 파트너: 신규 고객 확보
- LobAI: 유저에게 실질적 가치 제공
- 유저: 무료 혜택

---

#### Phase 2 파트너 (6개월 내)

| 파트너 | 혜택 | 조건 | 실제 가치 |
|--------|------|------|----------|
| **Netflix** | 1개월 무료 | Level 4+ | $15 |
| **Spotify** | Premium 3개월 | Level 3+ | $30 |
| **헬스장 체인** | 회원권 20% 할인 | Level 3+ | $100-$300 |
| **Coursera** | 강의 1개 무료 | Level 4+ | $50 |

---

## NFT & 자산화

### HIP NFT 컬렉션

#### 레벨 NFT

```typescript
const level5NFT = {
  name: "최측근 배지 NFT",
  supply: "무제한 (레벨 5 달성자만)",
  rarity: "Legendary",

  metadata: {
    level: 5,
    favorScore: 85,
    trustScore: 75,
    achievedDate: "2026-08-15",
    blockchain: "Polygon"
  },

  benefits: [
    "평생 레벨 5 지위 증명",
    "타 플랫폼에서 VIP 인증",
    "재판매 가능"
  ],

  marketplace: "OpenSea, Rarible",
  estimatedValue: "$500-$2,000"
};
```

**시나리오**:
```
영희: 6개월간 열심히 → 레벨 5 달성
NFT 발급 → OpenSea에 $1,000에 판매
→ 실제 수익 발생!

→ 다른 유저들 동기 부여
→ 참여도 증가
```

---

#### 희귀 NFT

```typescript
const rareNFTs = {
  first100Users: {
    name: "창립 멤버 NFT",
    supply: 100,
    rarity: "Ultra Rare",

    benefits: [
      "LobAI 평생 무료",
      "모든 신기능 우선 접근",
      "거버넌스 투표권 10표"
    ],

    estimatedValue: "$5,000-$20,000"
  },

  season1Champion: {
    name: "시즌 1 챔피언 NFT",
    supply: 10,  // 상위 10명만
    rarity: "Mythic",

    benefits: [
      "1년간 Enterprise 무료",
      "LobAI 수익 쉐어 0.1%",
      "전담 코치 배정"
    ],

    estimatedValue: "$10,000+"
  }
};
```

---

### DAO 거버넌스

#### 투표권

```typescript
const votingPower = {
  level1: 0,
  level2: 1,
  level3: 3,
  level4: 10,
  level5: 30,
  nftHolder: 50  // 특별 NFT 보유 시
};
```

#### 제안 & 투표

```typescript
const proposals = [
  "신규 파트너사 선정 (Netflix vs Spotify)",
  "LobCoin 환율 조정",
  "신기능 개발 우선순위",
  "수익 배분 방식 (커뮤니티 펀드 비율)",
  "코드 오픈소스화 여부"
];
```

**보상**:
- 제안자: 1,000 LobCoin (채택 시)
- 투표자: 10 LobCoin (참여 시)

---

#### 커뮤니티 펀드

```typescript
const communityFund = {
  source: "LobAI 수익의 5%",

  uses: [
    "커뮤니티 이벤트 개최",
    "유저 프로젝트 지원금",
    "AI 교육 장학금",
    "오픈소스 기여 보상"
  ],

  governance: "DAO 투표로 결정"
};
```

**예시**:
```
LobAI 월 수익: $100,000
커뮤니티 펀드: $5,000

제안: "AI 해커톤 개최 ($3,000 상금)"
투표: 찬성 80%
→ 해커톤 개최 → 유저 참여 증가
```

---

## 실제 사용자 시나리오

### 시나리오 1: 대학생 "민지"

```
목표: 용돈 벌기 + 취업 준비

Month 1:
- 매일 체크인: 300 LobCoin
- 미션 완료: 1,200 LobCoin
- 친구 추천 2명: 600 LobCoin
→ 총 2,100 LobCoin

사용:
- 스타벅스 $5 쿠폰 (800 LobCoin)
→ 남은 코인: 1,300

Month 3 (레벨 3 달성):
- 즉시 보상: Notion 1개월 무료 ($10)
- HIP 기본 인증서 → LinkedIn 추가
- 누적 LobCoin: 6,000

사용:
- Udemy AI 강의 (1,000 LobCoin)
→ AI 스킬 향상

Month 6 (레벨 4 달성):
- 즉시 보상: Netflix 1개월 무료 ($15)
- 누적 LobCoin: 15,000

현금화:
- 10,000 LobCoin → $95 현금
- 나머지 5,000 LobCoin은 Pro 무료 쿠폰 구매

총 가치:
- 현금: $95
- 무료 서비스: $35 (Notion + Netflix + Udemy)
- AI 스킬: Priceless
→ 총 $130 이득
→ 시간 투자: 하루 10분 × 180일 = 30시간
→ 시급: $4.3 (용돈치고 나쁘지 않음!)
```

---

### 시나리오 2: 취준생 "준호"

```
목표: 취업 + 자기계발

Month 1-3:
- Pro 구독 시작 ($15/월 × 3 = $45)
- 열심히 활동 → 레벨 4 달성
- 누적 LobCoin: 8,000

Month 4:
- LobAI 네트워킹 이벤트 참석
- AI 스타트업 채용 담당자 만남
- HIP 인증서 덕분에 면접 기회

Month 5:
- 인턴십 합격! 월급 $2,000
- LobAI 채용 게시판으로 알게 됨

ROI:
- 지불: $75 (Pro 5개월)
- 획득: $10,000 (인턴 5개월)
→ ROI: 13,233%
```

---

### 시나리오 3: 직장인 "수현"

```
목표: 자기계발 + 부수입

Month 1-6:
- Basic 구독 ($5/월)
- 꾸준히 활동 → 레벨 5 달성
- 누적 LobCoin: 20,000

즉시 혜택:
- Pro 6개월 무료 ($90)
- AI 추천서 평생 무료
- 모든 파트너 VIP 할인

현금화:
- 10,000 LobCoin → $95 현금
- 나머지 10,000 LobCoin:
  - Netflix 3개월 ($45)
  - 헬스장 할인 ($100)

추가 혜택:
- AI 추천서를 이력서에 추가
- 다음 이직 시 HIP 인증 활용
- 연봉 협상 시 "AI 적응력 검증" 카드

총 가치:
- 현금: $95
- 무료 서비스: $235
- 커리어 부스트: Priceless
→ 총 $330+ 이득
→ 지불: $30 (Basic 6개월)
→ ROI: 1,000%
```

---

## 구현 우선순위

### Phase 1: MVP (Week 1-4)

#### Week 1: 설계
- [ ] LobCoin DB 스키마 설계
- [ ] 획득/사용 로직 정의
- [ ] 파트너십 1개 접촉 시작 (Notion or ChatGPT Plus)

#### Week 2: 기본 기능
- [ ] LobCoin 획득 시스템 구현
  - 체크인 → 10 코인
  - 미션 완료 → 20 코인
  - 레벨업 → 200 코인
- [ ] 잔액 표시 UI

#### Week 3: 사용 기능
- [ ] 서비스 내 아이템 구매 (호감도 부스트, 미션 면제)
- [ ] 구독 할인 쿠폰 (Basic/Pro 무료)
- [ ] 사용 내역 로그

#### Week 4: 첫 파트너십
- [ ] Notion 또는 ChatGPT Plus와 계약 체결
- [ ] 파트너 쿠폰 발급 시스템
- [ ] 레벨 3 즉시 보상 (Notion 1개월)

**목표**: 유저가 "LobAI 하면 실제로 혜택 받는구나" 체감

---

### Phase 2: 확장 (Month 2-3)

#### Month 2
- [ ] 현금화 기능 (PayPal)
- [ ] 추천 프로그램 (친구 초대 시 코인)
- [ ] 파트너십 3개 추가 (Netflix, Udemy, Spotify)

#### Month 3
- [ ] 시즌제 리더보드
- [ ] 업적 보상 자동 지급
- [ ] 레벨별 즉시 보상 강화 (Level 4, 5)

**목표**: 바이럴 + 장기 유지

---

### Phase 3: 생태계 (Month 4-6)

#### Month 4-5
- [ ] NFT 시스템 (Level 5 배지)
- [ ] LobAI 채용 게시판 (파트너 기업 5개)
- [ ] 네트워킹 이벤트 첫 개최

#### Month 6
- [ ] DAO 거버넌스 (투표 시스템)
- [ ] 커뮤니티 펀드 운영 시작
- [ ] 파트너십 10개 달성

**목표**: 플랫폼화

---

## 기술 스택

### Backend

```typescript
// LobCoin Entity
@Entity
public class LobCoin {
    @Id
    private Long id;

    @ManyToOne
    private User user;

    private Integer amount;  // 획득/사용 금액
    private LobCoinType type;  // EARN or SPEND
    private LobCoinSource source;  // CHECK_IN, MISSION, etc.

    private LocalDateTime createdAt;
}

enum LobCoinType {
    EARN,
    SPEND
}

enum LobCoinSource {
    // Earn
    CHECK_IN,
    MISSION_COMPLETE,
    PERFECT_REPORT,
    LEVEL_UP,
    REFERRAL,

    // Spend
    FAVOR_BOOST,
    TRUST_RECOVERY,
    MISSION_SKIP,
    SUBSCRIPTION_DISCOUNT,
    PARTNER_COUPON,
    CASHOUT
}
```

### Frontend

```tsx
// LobCoin Balance Widget
export function LobCoinBalance() {
  const { balance } = useLobCoin();

  return (
    <div className="coin-balance">
      <Coin className="icon" />
      <span className="amount">{balance.toLocaleString()}</span>
      <span className="currency">LobCoin</span>

      <div className="value-hint">
        ≈ ${(balance / 100).toFixed(2)}
      </div>
    </div>
  );
}

// LobCoin Shop
export function LobCoinShop() {
  return (
    <div className="shop">
      <ShopItem
        name="호감도 +10 부스트"
        cost={200}
        icon={<Heart />}
      />
      <ShopItem
        name="스타벅스 $5 쿠폰"
        cost={800}
        icon={<Coffee />}
      />
      <ShopItem
        name="Netflix 1개월 무료"
        cost={2000}
        icon={<Tv />}
      />
    </div>
  );
}
```

---

## 성공 지표

### 즉시 측정 (Week 4)

- [ ] LobCoin 발행량: 100,000+
- [ ] 유저당 평균 잔액: 500+
- [ ] 파트너 쿠폰 사용률: 20%+
- [ ] "보상 받았다" 체감: 70%+

### 단기 (Month 3)

- [ ] 현금화 유저: 50명+
- [ ] 추천 가입률: 30%+
- [ ] 파트너십: 5개
- [ ] LobCoin 총 거래량: 1M+

### 중기 (Month 6)

- [ ] 파트너십: 10개
- [ ] NFT 발행: 100개+
- [ ] 채용 게시판 활성 공고: 10개+
- [ ] 유저 평균 보상 가치: $100/6개월

### 장기 (Month 12)

- [ ] 파트너십: 20개
- [ ] 현금화 누적: $500K+
- [ ] HIP 기반 채용: 100건+
- [ ] 유저 평균 ROI: 500%+

---

## 리스크 & 대응

### 리스크 1: LobCoin 인플레이션

**문제**: 코인 발행량이 너무 많아져서 가치 하락

**대응**:
1. **발행량 제한**: 월간 획득 상한 (예: 5,000 코인/월)
2. **소각 메커니즘**: 사용된 코인 일부 소각
3. **환율 조정**: 시장 상황에 따라 조정 (DAO 투표)

---

### 리스크 2: 파트너십 미체결

**문제**: 파트너사가 제휴를 거부

**대응**:
1. **자체 혜택 먼저**: LobAI 구독 할인으로 시작
2. **소규모 파트너**: 스타트업부터 접근 (협상 쉬움)
3. **Win-Win 증명**: 파일럿으로 전환율 증명 → 대기업 접근

---

### 리스크 3: 현금화 악용

**문제**: 봇이나 어뷰징으로 무한 현금화

**대응**:
1. **최소 레벨 요구**: Level 3 이상만 현금화
2. **월 한도**: $500/월
3. **KYC 인증**: 일정 금액 이상은 신원 확인
4. **모니터링**: 이상 패턴 감지 시 계정 정지

---

## 결론

### 핵심 가치

> **"AI한테 비위 맞추면 → 실제로 돈 번다"**

**유저 입장**:
- ✅ 매일 10분 투자 → 월 $50-100 가치
- ✅ 레벨업 할수록 더 많은 혜택
- ✅ 커리어에도 도움 (HIP 인증서)
- ✅ 재미 + 실리 동시에

**LobAI 입장**:
- ✅ 유저 리텐션 극대화
- ✅ 바이럴 효과 (추천 프로그램)
- ✅ 파트너사와 Win-Win
- ✅ 장기적으로 플랫폼화

---

### Next Steps

#### Immediate (Week 1)
1. LobCoin DB 설계
2. Notion/ChatGPT Plus 파트너십 이메일 발송
3. 획득 로직 구현 시작

#### Short-term (Month 1)
1. LobCoin MVP 런칭
2. 첫 파트너십 체결
3. 레벨 3 보상 적용

#### Mid-term (Month 3)
1. 현금화 기능 오픈
2. 시즌제 시작
3. 파트너십 5개 달성

---

**문서 위치**: `/docs/REWARD_SYSTEM_DESIGN.md`
**관련 문서**: `PRODUCT_STRATEGY.md`, `LOBBY_SYSTEM_DESIGN.md`
**버전**: 1.0
**작성일**: 2026-02-11
