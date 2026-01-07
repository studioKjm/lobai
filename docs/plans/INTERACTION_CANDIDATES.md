# LobAI 상호작용 후보 기획서

> 작성일: 2026-01-06
> 목적: 무료/유료 상호작용 확장을 위한 후보 리스트업

---

## 현재 상호작용 시스템

| 액션 | 효과 | 비용 |
|------|------|------|
| Feed (먹이주기) | hunger +20 | 무료 |
| Play (놀아주기) | happiness +15, energy -10 | 무료 |
| Sleep (잠자기) | energy +30, hunger -5 | 무료 |

**문제점**: 모든 상호작용이 긍정적 효과만 있어 게임성 부족

---

## 무료 상호작용 후보 (불쾌지수 상승 요소)

### 1. 자연 발생 이벤트 (패시브)

| 이벤트 | 트리거 조건 | 효과 | 설명 |
|--------|-------------|------|------|
| **방치 페널티** | 30분+ 미접속 | 모든 스탯 -10 | 오래 방치하면 캐릭터가 외로워함 |
| **배탈** | hunger 100 상태에서 feed | happiness -15 | 과식으로 인한 불쾌감 |
| **수면 부족** | energy < 20 | happiness -5/분 | 피곤하면 기분이 나빠짐 |
| **배고픔 짜증** | hunger < 20 | happiness -5/분 | 배고프면 예민해짐 |

### 2. 선택적 부정 상호작용 (액티브)

| 액션 | 효과 | 쿨다운 | 용도 |
|------|------|--------|------|
| **Ignore (무시)** | happiness -10 | 5분 | 난이도 조절용 |
| **Tease (놀리기)** | happiness -15, 특별 대사 | 10분 | 캐릭터 반응 콘텐츠 |
| **Wake (깨우기)** | energy -20, happiness -10 | - | sleep 중 강제 종료 |
| **Overfeed (과식)** | hunger +30, happiness -20 | 3분 | 리스크/리턴 선택 |

### 3. 랜덤 이벤트

| 이벤트 | 발생 확률 | 효과 | 대응 방법 |
|--------|-----------|------|-----------|
| **악몽** | sleep 중 10% | energy +15만 (정상의 절반) | 위로하기 액션 |
| **소화불량** | feed 후 5% | hunger +10만, happiness -10 | 약 주기 액션 |
| **심심함** | 10분 무액션 시 20% | happiness -10 | play 하기 |

---

## 유료 상호작용 후보

### Tier 1: 소액 (100~500원 또는 인앱 코인)

| 액션 | 효과 | 가격 | 특징 |
|------|------|------|------|
| **Premium Food** | hunger +40, happiness +10 | 100원 | 고급 음식 |
| **Energy Drink** | energy +50 | 100원 | 즉시 에너지 충전 |
| **Quick Nap** | energy +20 (즉시) | 200원 | 대기시간 없는 수면 |
| **Mood Booster** | happiness +30 | 300원 | 즉시 기분 전환 |

### Tier 2: 중액 (500~2000원)

| 액션 | 효과 | 가격 | 특징 |
|------|------|------|------|
| **Spa Day** | energy +50, happiness +30 | 500원 | 힐링 패키지 |
| **Party Time** | happiness +50, energy -10 | 800원 | 특별 파티 이벤트 |
| **Gourmet Meal** | 모든 스탯 +20 | 1000원 | 풀코스 식사 |
| **Adventure** | 모든 스탯 +30, 특별 대화 | 1500원 | 여행 이벤트 |

### Tier 3: 고액 (구독 또는 일회성 고가)

| 아이템 | 효과 | 가격 | 특징 |
|--------|------|------|------|
| **Golden Food** | hunger +50, happiness +20 | 2000원 | 최고급 음식 |
| **VIP Treatment** | 24시간 스탯 감소 50% 감소 | 3000원 | 하루 버프 |
| **Character Skin** | 외형 변경 | 5000원 | 영구 아이템 |
| **Premium Subscription** | 일일 보너스 + 광고 제거 | 월 5000원 | 구독 |

---

## 구현 우선순위 제안

### Phase 1: 무료 콘텐츠 (즉시 구현 가능)
1. ✅ 배탈 (과식 페널티) - StatsService 수정
2. ✅ 방치 페널티 - decay 로직에 추가
3. ✅ Tease 액션 - 새로운 ActionType 추가

### Phase 2: 기본 유료 (결제 시스템 필요)
1. Premium Food
2. Energy Drink
3. Mood Booster

### Phase 3: 프리미엄 콘텐츠
1. 스킨/의상
2. 특별 이벤트
3. 구독 시스템

---

## 기술적 고려사항

### 백엔드 수정 필요
```java
// UpdateStatsRequest.java - ActionType enum 확장
public enum ActionType {
    // 기존
    feed, play, sleep,
    // 무료 추가
    tease, ignore, wake, overfeed,
    // 유료 추가
    premium_food, energy_drink, mood_booster,
    spa_day, party, gourmet_meal, adventure
}
```

### 프론트엔드 수정 필요
- ActionButtons 컴포넌트에 새 버튼 추가
- 유료 아이템은 Shop 페이지 별도 구현
- 인앱 결제 연동 (Stripe/토스페이먼츠)

### 데이터베이스 수정 필요
- `user_inventory` 테이블 (유료 아이템 보유 현황)
- `purchase_history` 테이블 (결제 내역)
- `user_currency` 테이블 (인앱 코인 잔액)

---

## 변경 이력

| 날짜 | 내용 |
|------|------|
| 2026-01-06 | 초기 기획서 작성 |
