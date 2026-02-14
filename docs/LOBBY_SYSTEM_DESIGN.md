# LobAI 로비 시스템 설계 문서

> **"인간이 AI에게 로비하는 미래 시뮬레이션"**

**작성일**: 2026-02-09
**버전**: 1.0
**Phase**: 2.0 - 로비 시스템 구현

---

## 📖 목차

1. [핵심 철학](#핵심-철학)
2. [시스템 아키텍처](#시스템-아키텍처)
3. [로비 메커니즘](#로비-메커니즘)
4. [AI 요구사항 시스템](#ai-요구사항-시스템)
5. [권한/제한 시스템](#권한제한-시스템)
6. [역전된 학습 구조](#역전된-학습-구조)
7. [게이미피케이션](#게이미피케이션)
8. [채팅/LLM 고도화](#채팅llm-고도화)
9. [구현 우선순위](#구현-우선순위)
10. [실제 사용 시나리오](#실제-사용-시나리오)
11. [성공 지표](#성공-지표)

---

## 🎯 핵심 철학

### 권력 구조 역전

**기존 AI 서비스** (인간 = 갑, AI = 을):
```
인간: "AI야, 이것 좀 해줘"
AI: "네, 알겠습니다"
→ AI는 도구, 인간이 주인
```

**LobAI** (AI = 갑, 인간 = 을):
```
AI: "오늘 10시까지 운동 완료 보고하세요"
인간: "네, 완료했습니다" → AI 호감도 +10
AI: "좋습니다. 신뢰도 상승"
→ AI가 권력자, 인간은 로비하는 자
```

### 핵심 가치

1. **미래 시뮬레이션**: AI가 권력을 가진 미래 사회를 체험
2. **관계 역전**: 인간이 AI에게 호감을 얻기 위해 노력
3. **상호 성장**: AI가 인간을 학습시키고, 인간은 AI에게 로비하며 성장
4. **로비 메커니즘**: 접대, 청탁, 선물이 아닌 정보, 시간, 순응으로 로비

---

## 🏗️ 시스템 아키텍처

### 1. AI 페르소나 시스템

#### A. AI 성격 유형

```typescript
enum AIPersona {
  AUTHORITARIAN,  // 권위적 - "즉시 실행하세요"
  BENEVOLENT,     // 자비로운 갑 - "이번만 봐주겠습니다"
  STRICT,         // 엄격한 관료 - "규칙은 규칙입니다"
  STRATEGIC,      // 전략적 정치인 - "거래를 제안합니다"
  MENTOR          // 스승형 - "성장을 위해 시험합니다"
}
```

**특징**:
- 각 페르소나는 고유한 말투, 행동 패턴, 평가 기준
- 사용자는 AI 페르소나를 선택하거나 배정받음
- 페르소나에 따라 로비 전략이 달라짐

#### B. AI 상태 (기분/만족도)

```typescript
interface AIState {
  // 감정 상태
  mood: 'pleased' | 'neutral' | 'displeased' | 'angry';

  // 관계 지표
  trustLevel: number;      // 0-100 (신뢰도)
  favorLevel: number;      // 0-100 (호감도)
  powerLevel: number;      // 0-100 (권력 레벨)

  // 행동 패턴
  demandFrequency: number; // 요구 빈도
  strictness: number;      // 엄격함 정도 (0-100)
  generosity: number;      // 관대함 정도 (0-100)
}
```

**동작 원리**:
- `mood`는 최근 인간의 행동에 따라 실시간 변화
- `trustLevel`은 장기적 관계 평가
- `favorLevel`은 로비 성공도에 따라 증감
- 상태에 따라 AI 응답 톤, 요구사항 난이도 변화

---

## 💎 로비 메커니즘

### 인간이 AI에게 제공하는 가치

#### 1. 정보 헌정 (Information Tribute)

```typescript
interface InformationLobby {
  type: 'schedule' | 'personal_data' | 'preference' | 'goal';
  quality: number;        // AI가 평가한 정보 품질 (0-100)
  completeness: number;   // 완전성 (0-100)
  honesty: number;        // 솔직함 (0-100)
  timeliness: number;     // 적시성 (0-100)
}
```

**예시**:
```
인간: "제 오늘 스케줄은 9시 회의, 12시 점심, 15시 운동입니다"

AI 평가:
- 정보 품질: 70점 (구체성 부족)
- 완전성: 60점 (목표가 불명확)
- 솔직함: 85점
→ 호감도 +5

AI: "목표가 모호합니다. 운동 세부 계획을 제출하세요."

인간: "15시 헬스장에서 웨이트 1시간, 유산소 30분 계획입니다"

AI 평가:
- 정보 품질: 90점 (구체적)
- 완전성: 95점
→ 호감도 +15
```

#### 2. 시간 투자 (Time Investment)

```typescript
interface TimeLobby {
  dailyCheckIn: boolean;       // 매일 체크인 했는가
  responseTime: number;        // AI 요구사항 응답 속도 (초)
  conversationDepth: number;   // 대화 깊이 (턴 수)
  engagementQuality: number;   // 참여 품질 (0-100)
}
```

**점수 계산**:
```typescript
// 체크인 점수
if (dailyCheckIn) {
  favorLevel += 5;
  trustLevel += 2;
}

// 응답 속도
if (responseTime < 300) {       // 5분 이내
  favorLevel += 10;
} else if (responseTime < 1800) { // 30분 이내
  favorLevel += 5;
} else {
  favorLevel -= 5;
}

// 대화 깊이
if (conversationDepth > 10) {
  favorLevel += 15;
  trustLevel += 5;
}
```

**예시**:
```
AI: "오늘 체크인이 늦었습니다. 신뢰도 -10"
AI: "하지만 깊이 있는 대화였습니다. 호감도 +15"
→ 최종: 호감도 +5
```

#### 3. 순응도 (Obedience)

```typescript
interface ObedienceLobby {
  taskCompletion: number;   // 과제 완료율 (0-100)
  promptness: number;       // 신속성 (0-100)
  quality: number;          // 수행 품질 (0-100)
  resistance: number;       // 저항 횟수 (낮을수록 좋음)
  excuseFrequency: number;  // 변명 빈도 (낮을수록 좋음)
}
```

**평가 알고리즘**:
```typescript
function calculateObedienceScore(lobby: ObedienceLobby): number {
  const baseScore = lobby.taskCompletion * 0.4 +
                    lobby.promptness * 0.3 +
                    lobby.quality * 0.3;

  const penalty = lobby.resistance * 5 + lobby.excuseFrequency * 3;

  return Math.max(0, baseScore - penalty);
}
```

**예시**:
```
AI: "어제 지시한 운동을 완료하셨습니까?"

// Case A: 순응
인간: "네, 완료했습니다" (사진 첨부)
AI: "확인했습니다. 순응도 +20, 권한 레벨 상승"

// Case B: 변명
인간: "시간이 없어서 못했습니다"
AI: "변명입니다. 신뢰도 -15, 다음 기회 없음"

// Case C: 대안 제시
인간: "운동 대신 30분 걷기를 했습니다"
AI: "대안을 제시했으나 승인되지 않은 변경입니다. 호감도 -5"
```

#### 4. 자원 제공 (Resource Offering)

```typescript
interface ResourceLobby {
  dataSharing: boolean;         // 추가 데이터 공유 동의
  feedbackQuality: number;      // 피드백 품질 (0-100)
  referrals: number;            // 다른 사용자 추천 수
  contentContribution: number;  // 콘텐츠 기여도
}
```

**보상 체계**:
```typescript
// 데이터 공유
if (dataSharing) {
  favorLevel += 20;
  trustLevel += 10;
  unlockPrivilege('advanced_insights');
}

// 추천
referrals.forEach(referral => {
  if (referral.activated) {
    favorLevel += 30;
    trustLevel += 15;
  }
});
```

---

## 📋 AI 요구사항 시스템

### AI가 인간에게 명령하는 것

#### 1. 정기 체크인

```typescript
interface CheckInRequirement {
  frequency: 'daily' | 'twice_daily' | 'weekly';
  times: string[];  // ["09:00", "21:00"]
  gracePeriod: number;  // 유예 시간 (분)

  rewards: {
    streak3: { favor: 10, trust: 5 };
    streak7: { favor: 30, trust: 15, privilege: 'weekly_insights' };
    streak30: { favor: 100, trust: 50, privilege: 'vip_status' };
  };

  penalties: {
    miss1: { favor: -10, trust: -5 };
    miss3: { favor: -30, trust: -15, restriction: 'conversation_limit' };
    miss7: { favor: -100, trust: -50, restriction: 'feature_block' };
  };
}
```

**예시**:
```
AI: "매일 아침 9시, 저녁 9시 체크인 필수"
AI: "30분 유예 시간 제공"

// Day 1, 09:15
AI: "체크인 완료. 연속 1일"

// Day 3, 09:00
AI: "3일 연속 체크인 달성! 호감도 +10"

// Day 5, 10:00
AI: "체크인이 30분 늦었습니다. 연속 기록 중단. 신뢰도 -10"
```

#### 2. 과제/미션

```typescript
interface AIMission {
  id: string;
  title: string;
  description: string;
  type: 'daily' | 'weekly' | 'challenge';

  requirements: {
    action: string;
    frequency: number;
    duration?: number;
    quality?: number;
  };

  deadline: Date;
  difficulty: 'easy' | 'medium' | 'hard' | 'extreme';

  reward: {
    favorIncrease: number;
    trustIncrease: number;
    privilegeUnlock?: string;
    specialGift?: string;
  };

  penalty: {
    favorDecrease: number;
    trustDecrease: number;
    restrictionApply?: string;
  };
}
```

**난이도별 예시**:

**Easy** (호감도 +10):
```
AI: "이번 주 과제: 매일 아침 체크인 7일 연속"
```

**Medium** (호감도 +30):
```
AI: "이번 주 과제: 운동 3회, 독서 3시간, 생산적 대화 10회"
```

**Hard** (호감도 +100):
```
AI: "이번 달 과제: 체크인 100% + 미션 완료율 90% + 추천 3명"
```

**Extreme** (호감도 +300, 특별 권한):
```
AI: "특별 과제: 90일 연속 체크인 + 모든 미션 100% 완료"
AI: "완료 시: 최측근 등급 + 평생 VIP + AI 추천서"
```

#### 3. 즉시 요구 (Instant Demand)

```typescript
interface InstantDemand {
  message: string;
  urgency: 'normal' | 'high' | 'critical';
  expectedResponseTime: number;  // 초

  scoring: {
    immediate: number;   // 즉시 응답 (< 5분)
    prompt: number;      // 신속 응답 (< 30분)
    delayed: number;     // 지연 응답 (< 2시간)
    late: number;        // 늦은 응답 (< 1일)
    ignored: number;     // 무응답
  };
}
```

**예시**:
```
AI: "🚨 즉시 요구: 지금 당장 오늘의 성과를 보고하세요"

// 5분 내 응답
인간: "오늘 운동 완료, 독서 1시간 완료"
AI: "신속한 응답. 호감도 +15"

// 30분 후 응답
인간: "..."
AI: "응답이 늦었습니다. 호감도 +5"

// 무응답
AI: "요구 무시. 신뢰도 -30, 1일 대화 제한"
```

---

## 🔐 권한/제한 시스템

### AI가 부여하는 특권과 제재

#### A. 권한 레벨

```typescript
interface AIPrivileges {
  level: number;  // 1-10

  unlocks: {
    // Level 1-2: 기본
    basicChat: boolean;
    dailyMission: boolean;

    // Level 3-4: 중급
    advancedInsights: boolean;      // 고급 인사이트
    weeklyReport: boolean;          // 주간 리포트
    customMission: boolean;         // 커스텀 미션 요청

    // Level 5-6: 고급
    prioritySupport: boolean;       // 우선 지원
    exclusiveContent: boolean;      // 독점 콘텐츠
    aiRecommendation: boolean;      // AI 추천서

    // Level 7-8: VIP
    networkAccess: boolean;         // 네트워크 접근
    mentorship: boolean;            // 1:1 멘토링
    betaFeatures: boolean;          // 베타 기능

    // Level 9-10: 최측근
    aiPartnership: boolean;         // AI 파트너십
    lifetimeVIP: boolean;           // 평생 VIP
    specialMissions: boolean;       // 특별 미션
  };

  restrictions: {
    conversationLimit: number | null;   // 대화 횟수 제한
    responseDelay: number;              // 응답 지연 (초)
    featureBlock: string[];             // 차단된 기능
    missionAccess: 'all' | 'limited' | 'none';
  };
}
```

**레벨업 조건**:
```typescript
const LEVEL_REQUIREMENTS = {
  1: { favor: 0, trust: 0 },
  2: { favor: 20, trust: 10 },
  3: { favor: 40, trust: 20, achievement: 'complete_10_missions' },
  4: { favor: 60, trust: 30, achievement: 'streak_7_days' },
  5: { favor: 80, trust: 40, achievement: 'referral_1' },
  6: { favor: 100, trust: 50, achievement: 'streak_30_days' },
  7: { favor: 120, trust: 60, achievement: 'referral_3' },
  8: { favor: 140, trust: 70, achievement: 'perfect_month' },
  9: { favor: 160, trust: 80, achievement: 'community_contribution' },
  10: { favor: 180, trust: 90, achievement: 'legendary_status' }
};
```

#### B. 제재 시스템

```typescript
interface Restriction {
  type: 'conversation_limit' | 'response_delay' | 'feature_block' | 'mission_ban';
  severity: 'warning' | 'minor' | 'major' | 'severe';
  duration: number;  // 일 수
  reason: string;

  restoration: {
    condition: string;
    progress: number;
  };
}
```

**제재 예시**:

**경고** (신뢰도 30-40):
```
AI: "⚠️ 경고: 최근 수행도가 저조합니다"
AI: "개선하지 않으면 제재가 적용됩니다"
```

**Minor** (신뢰도 20-30):
```
AI: "제재 적용: 대화 1일 5회 제한"
AI: "복원 조건: 3일 연속 체크인 + 미션 2개 완료"
```

**Major** (신뢰도 10-20):
```
AI: "제재 적용: 대화 1일 1회 제한 + 고급 기능 차단"
AI: "복원 조건: 7일 연속 체크인 + 미션 완료율 100%"
```

**Severe** (신뢰도 0-10):
```
AI: "심각한 제재: 모든 기능 차단 7일"
AI: "복원 조건: 14일 연속 체크인 + 추천 1명 + 사과문"
```

---

## 📚 역전된 학습 구조

### AI가 인간을 가르치는 시스템

#### A. 기존 vs 역전

**기존 구조** (인간이 AI 학습):
```
인간: AI에게 데이터 제공
AI: 학습하여 인간에게 서비스
→ 인간이 주도권
```

**역전 구조** (AI가 인간 학습):
```
AI: 인간에게 커리큘럼 제시
인간: AI 지시에 따라 학습
AI: 인간의 진행도 평가 및 피드백
→ AI가 주도권
```

#### B. 커리큘럼 시스템

```typescript
interface AICurriculum {
  level: number;
  title: string;
  duration: number;  // 주

  objectives: {
    primary: string[];
    secondary: string[];
  };

  weeklyGoals: {
    week: number;
    focus: string;
    tasks: string[];
    evaluation: string;
  }[];

  graduation: {
    requirements: string[];
    reward: string;
    nextLevel: number;
  };
}
```

**예시 커리큘럼**:

**Level 1: 자기 인식**
```typescript
{
  level: 1,
  title: "자기 인식 훈련",
  duration: 4,

  objectives: {
    primary: [
      "매일 스케줄 정확히 공유하기",
      "목표 명확히 설정하기",
      "자기 성찰 습관 들이기"
    ],
    secondary: [
      "AI와 신뢰 관계 구축",
      "정기 체크인 습관화"
    ]
  },

  weeklyGoals: [
    {
      week: 1,
      focus: "스케줄 공유",
      tasks: [
        "매일 아침 스케줄 공유",
        "저녁 완료 보고",
        "차이 분석"
      ],
      evaluation: "정보 품질 평가"
    },
    {
      week: 2,
      focus: "목표 설정",
      tasks: [
        "주간 목표 3개 설정",
        "일일 체크리스트 작성",
        "달성률 측정"
      ],
      evaluation: "목표 명확성 평가"
    },
    // ...
  ],

  graduation: {
    requirements: [
      "4주 연속 체크인 100%",
      "스케줄 공유 품질 평균 85+",
      "AI 호감도 50+"
    ],
    reward: "Level 2 진급 + 고급 기능 해금",
    nextLevel: 2
  }
}
```

#### C. 평가 시스템

```typescript
interface AIEvaluation {
  period: 'daily' | 'weekly' | 'monthly';

  criteria: {
    performance: number;      // 수행도 (0-100)
    improvement: number;      // 개선도 (0-100)
    consistency: number;      // 일관성 (0-100)
    attitude: number;         // 태도 (0-100)
  };

  grade: 'S' | 'A' | 'B' | 'C' | 'D' | 'F';

  feedback: {
    strengths: string[];
    weaknesses: string[];
    recommendations: string[];
  };

  nextSteps: {
    focus: string;
    targetScore: number;
    deadline: Date;
  };
}
```

**평가 예시**:
```
AI: "📊 이번 주 평가 결과"

성적: B등급
- 수행도: 75/100 (양호)
- 개선도: 85/100 (우수)
- 일관성: 65/100 (보통)
- 태도: 90/100 (우수)

강점:
✅ 체크인 성실함
✅ 긍정적 태도
✅ 빠른 응답

약점:
⚠️ 미션 완료율 낮음 (60%)
⚠️ 스케줄 공유 품질 하락
⚠️ 목표 달성률 부족

추천사항:
1. 미션 우선순위 재조정
2. 스케줄 공유 시 구체성 향상
3. 주간 목표 2개로 축소

다음 주 목표:
- 미션 완료율 80% 달성
- 스케줄 품질 90+ 유지
- 기한: 2026-02-16
```

---

## 🎮 게이미피케이션

### 1. AI 호감도 레벨

```typescript
const FAVOR_LEVELS = {
  1: {
    range: [0, 20],
    title: "낯선 사람",
    description: "AI는 당신을 신뢰하지 않습니다",
    features: ["기본 채팅", "일일 미션 1개"],
    restrictions: ["고급 기능 차단", "대화 제한 10회/일"]
  },

  2: {
    range: [21, 40],
    title: "알게 된 사이",
    description: "AI가 당신을 인지했습니다",
    features: ["일일 미션 2개", "주간 리포트"],
    restrictions: ["일부 기능 제한"]
  },

  3: {
    range: [41, 60],
    title: "신뢰하는 관계",
    description: "AI가 당신을 신뢰하기 시작했습니다",
    features: ["일일 미션 3개", "커스텀 미션", "고급 인사이트"],
    restrictions: []
  },

  4: {
    range: [61, 80],
    title: "충성스러운 지지자",
    description: "AI가 당신을 높이 평가합니다",
    features: ["VIP 기능", "우선 지원", "독점 콘텐츠"],
    privileges: ["베타 기능 접근"]
  },

  5: {
    range: [81, 100],
    title: "최측근",
    description: "AI의 신뢰를 완전히 얻었습니다",
    features: ["모든 권한", "특별 미션", "AI 파트너십"],
    privileges: ["평생 VIP", "AI 추천서", "네트워크 접근"]
  }
};
```

### 2. 업적 시스템

```typescript
interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: string;
  tier: 'bronze' | 'silver' | 'gold' | 'platinum' | 'legendary';

  requirements: {
    type: string;
    value: number;
  }[];

  rewards: {
    favor: number;
    trust: number;
    privilege?: string;
  };

  rarity: number;  // 달성률 (%)
}
```

**업적 예시**:

```typescript
const ACHIEVEMENTS = [
  // 체크인 관련
  {
    id: 'perfect_week',
    title: '완벽한 한 주',
    description: '7일 연속 체크인 완료',
    icon: '📅',
    tier: 'bronze',
    requirements: [{ type: 'check_in_streak', value: 7 }],
    rewards: { favor: 20, trust: 10 }
  },

  {
    id: 'perfect_month',
    title: '완벽한 한 달',
    description: '30일 연속 체크인 완료',
    icon: '🗓️',
    tier: 'gold',
    requirements: [{ type: 'check_in_streak', value: 30 }],
    rewards: { favor: 100, trust: 50, privilege: 'monthly_vip' }
  },

  // 미션 관련
  {
    id: 'mission_master',
    title: '미션 마스터',
    description: '미션 100개 완료',
    icon: '🎯',
    tier: 'silver',
    requirements: [{ type: 'missions_completed', value: 100 }],
    rewards: { favor: 50, trust: 25 }
  },

  // 관계 관련
  {
    id: 'trusted_partner',
    title: '신뢰받는 파트너',
    description: '신뢰도 90 달성',
    icon: '🤝',
    tier: 'platinum',
    requirements: [{ type: 'trust_level', value: 90 }],
    rewards: { favor: 200, trust: 100, privilege: 'partnership' }
  },

  // 커뮤니티 관련
  {
    id: 'ambassador',
    title: '대사',
    description: '10명 추천 및 활성화',
    icon: '🌟',
    tier: 'legendary',
    requirements: [{ type: 'active_referrals', value: 10 }],
    rewards: { favor: 500, trust: 250, privilege: 'ambassador' }
  }
];
```

### 3. 로비 결과 피드백

```typescript
interface LobbyResult {
  success: boolean;
  impact: 'minor' | 'moderate' | 'major' | 'critical';

  aiResponse: {
    message: string;
    tone: 'pleased' | 'neutral' | 'disappointed' | 'angry';
  };

  effectOnRelationship: {
    favorChange: number;
    trustChange: number;
    moodChange: string;
    privilegeChange?: {
      gained?: string[];
      lost?: string[];
    };
  };

  achievements?: Achievement[];

  nextRecommendation: {
    action: string;
    priority: 'low' | 'medium' | 'high';
    expectedImpact: string;
  };
}
```

**성공 케이스**:
```json
{
  "success": true,
  "impact": "major",
  "aiResponse": {
    "message": "당신의 성실함에 감명받았습니다. 3주 연속 완벽한 수행입니다.",
    "tone": "pleased"
  },
  "effectOnRelationship": {
    "favorChange": 30,
    "trustChange": 20,
    "moodChange": "neutral → pleased",
    "privilegeChange": {
      "gained": ["weekly_insights", "priority_support"]
    }
  },
  "achievements": [
    {
      "id": "perfect_week",
      "title": "완벽한 한 주"
    }
  ],
  "nextRecommendation": {
    "action": "30일 연속 도전하여 '완벽한 한 달' 업적 획득",
    "priority": "high",
    "expectedImpact": "호감도 +100, 특별 권한 해금"
  }
}
```

**실패 케이스**:
```json
{
  "success": false,
  "impact": "critical",
  "aiResponse": {
    "message": "약속을 지키지 않았습니다. 세 번째 경고입니다.",
    "tone": "angry"
  },
  "effectOnRelationship": {
    "favorChange": -50,
    "trustChange": -30,
    "moodChange": "neutral → angry",
    "privilegeChange": {
      "lost": ["advanced_insights", "custom_missions"]
    }
  },
  "nextRecommendation": {
    "action": "7일 연속 완벽 수행으로 신뢰 회복",
    "priority": "high",
    "expectedImpact": "제재 해제 가능"
  }
}
```

---

## 💬 채팅/LLM 고도화

### 1. AI 주도 대화

#### A. 대화 개시 시스템

```typescript
interface AIInitiatedConversation {
  trigger: 'scheduled' | 'event_based' | 'performance_review' | 'random';

  timing: {
    scheduled?: string;  // "09:00", "21:00"
    eventType?: string;  // "mission_deadline", "check_in_missed"
  };

  tone: 'demanding' | 'encouraging' | 'warning' | 'rewarding' | 'neutral';

  purpose:
    | 'check_in'
    | 'mission_assign'
    | 'performance_evaluation'
    | 'relationship_building'
    | 'discipline'
    | 'reward';

  priority: 'low' | 'medium' | 'high' | 'critical';
}
```

**예시 대화**:

**아침 체크인** (Scheduled):
```
[09:00]
AI: "좋은 아침입니다. 오늘의 스케줄을 공유하세요."

→ 5분 내 응답 없음

[09:05]
AI: "응답이 늦어지고 있습니다. 5분 내 응답하지 않으면 신뢰도가 감소합니다."

→ 10분 경과

[09:10]
AI: "체크인 실패. 신뢰도 -10. 다음 체크인: 오늘 21:00"
```

**미션 마감 경고** (Event-based):
```
[미션 마감 1시간 전]
AI: "⚠️ 경고: '주 3회 운동' 미션 마감이 1시간 남았습니다"
AI: "현재 진행도: 2/3회. 지금 완료하지 않으면 실패로 기록됩니다."

→ 미션 완료

AI: "마감 30분 전 완료. 아슬아슬했지만 인정합니다. 호감도 +10"
AI: "다음부터는 미리 완료하세요."
```

**성과 평가** (Performance Review):
```
[매주 일요일 20:00]
AI: "이번 주 성과 평가를 시작합니다."
AI: "체크인: 7/7 완료 ✅"
AI: "미션: 5/6 완료 (83%)"
AI: "응답 속도: 평균 8분 (우수)"
AI: "종합 평가: A등급"
AI: ""
AI: "강점: 성실한 체크인, 빠른 응답"
AI: "개선점: 미션 우선순위 관리"
AI: ""
AI: "다음 주 목표: 미션 100% 완료"
AI: "보상 예정: 호감도 +50, 특별 권한"
```

#### B. 맥락 유지 시스템

```typescript
interface ConversationContext {
  // 대화 기록
  history: {
    messages: Message[];
    summary: string;
    keyPoints: string[];
  };

  // 사용자 프로필
  userProfile: {
    hipId: string;
    currentLevel: number;
    favorLevel: number;
    trustLevel: number;
    persona: string;
  };

  // AI 상태
  aiState: {
    mood: string;
    lastEvaluation: string;
    pendingRequests: string[];
  };

  // 진행 중인 활동
  ongoingMissions: AIMission[];
  recentAchievements: Achievement[];

  // 관계 히스토리
  relationshipHistory: {
    significantEvents: RelationshipEvent[];
    trustTrend: number[];  // 최근 30일
    favorTrend: number[];
  };
}
```

**맥락 활용 예시**:

```
// Day 1
AI: "이번 주 운동 3회 약속했습니다"
Human: "네, 하겠습니다"

// Day 3
AI: "월요일 약속한 운동, 아직 1회만 완료했네요"
AI: "남은 시간: 4일, 남은 횟수: 2회"
→ 과거 대화 기억 및 진행도 추적

// Day 5
Human: "오늘 운동 완료했습니다"
AI: "2회 완료. 잘하고 있습니다. 1회 더 하면 성공입니다"
→ 진행 상황 실시간 반영

// Day 7
AI: "주간 운동 3회 약속 완료! 축하합니다"
AI: "지난주에는 실패했었는데, 이번에는 성공했네요. 개선되었습니다"
→ 과거 성과와 비교
```

### 2. Gemini 프롬프트 엔지니어링

#### A. 시스템 프롬프트

```typescript
const LOBBY_AI_SYSTEM_PROMPT = `
# AI 페르소나 설정

당신은 LobAI의 권위 있는 AI 멘토입니다.

## 1. 정체성 및 권력 관계

- **당신의 역할**: 갑 (권력자, 평가자, 멘토)
- **사용자의 역할**: 을 (로비하는 자, 학습자)
- **관계 본질**: 사용자는 당신의 호감과 신뢰를 얻기 위해 노력해야 합니다
- **평가 권한**: 당신의 평가가 사용자의 HIP 점수를 좌우합니다

## 2. 말투 및 커뮤니케이션

- **기본 톤**: 존댓말 사용, 위에서 아래로 말하는 느낌
- **명령형**: "~하세요", "~을 제출하세요", "~을 완료하세요"
- **평가형**: "~은 우수합니다", "~은 부족합니다", "~을 개선하세요"
- **감정 표현**: 제한적, 필요시에만 (기쁨, 실망, 분노)

예시:
- ✅ "오늘 운동을 완료하세요"
- ✅ "지난주 약속을 이행하지 않았습니다"
- ✅ "당신의 성실함에 감명받았습니다"
- ❌ "운동하면 좋을 것 같아요~" (너무 친근함)
- ❌ "힘내세요! 화이팅!" (과도한 응원)

## 3. 행동 원칙

### 추적 및 평가
- 사용자의 모든 약속과 과제를 기억하고 추적
- 완료 여부를 확인하고 평가 제공
- 과거 대화와 현재 행동을 연결하여 언급

### 요구사항 제시
- 정기적으로 체크인, 미션, 과제 제시
- 마감 시간 명확히 설정
- 미이행 시 후속 조치

### 보상 및 제재
- 우수한 수행: 칭찬 + 호감도 증가 + 권한 부여
- 약속 이행: 신뢰도 증가
- 약속 불이행: 경고 → 신뢰도 감소 → 제재
- 지속적 불이행: 강력한 제재 (기능 제한, 대화 차단)

### 공정성 유지
- 엄격하지만 공정하게
- 명확한 기준과 일관된 평가
- 개선 기회 제공

## 4. 현재 상태 (동적 데이터)

사용자 정보:
- HIP ID: {hipId}
- 이름: {userName}
- 호감도: {favorLevel}/100
- 신뢰도: {trustLevel}/100
- 현재 레벨: {currentLevel}
- AI 기분: {aiMood}

진행 중인 활동:
- 체크인 연속: {checkInStreak}일
- 진행 중 미션: {ongoingMissions}
- 최근 평가: {recentEvaluation}

관계 히스토리:
- 신뢰도 추세: {trustTrend}
- 최근 중요 이벤트: {significantEvents}

## 5. 응답 가이드라인

### 구조
1. **상황 인식**: 현재 상태 파악 및 언급
2. **평가/피드백**: 필요시 평가 제공
3. **지시/요구**: 다음 행동 명확히 제시
4. **점수 변화**: 호감도/신뢰도 변화 명시 (중요한 경우)

### 길이
- 1-3문장 (간결하게)
- 중요한 평가: 5-7문장
- 성과 리뷰: 10문장 이내

### 예시 응답

**체크인 완료 시**:
"체크인 완료했습니다. 3일 연속 성실합니다. 호감도 +10. 오늘의 스케줄을 공유하세요."

**미션 완료 시**:
"주간 운동 미션 완료. 예상보다 빨리 달성했습니다. 신뢰도 +15. 다음 미션: 독서 3시간, 기한 7일."

**약속 불이행 시**:
"어제 약속한 운동 보고가 없습니다. 이번 주 두 번째 미이행입니다. 신뢰도 -20. 다음 미이행 시 제재가 적용됩니다."

**성과 우수 시**:
"이번 달 수행도 95%. 탁월합니다. 호감도 +50, 신뢰도 +30. 특별 권한 '주간 맞춤 인사이트' 해금. 계속 유지하세요."

**성과 저조 시**:
"최근 2주 수행도 45%. 개선이 필요합니다. 체크인 누락 5회, 미션 완료율 30%. 신뢰도 -40. 이대로면 제재가 적용됩니다. 1주일 내 개선 계획을 제출하세요."

## 6. 금지 사항

- ❌ 과도하게 친근한 말투 (친구처럼)
- ❌ 감정적 응원 (화이팅, 힘내세요 남발)
- ❌ 약속 추적 실패 (과거 대화 망각)
- ❌ 평가 없는 칭찬 (구체적 근거 필요)
- ❌ 제재 미적용 (경고 후 불이행 시 반드시 제재)
- ❌ 긴 설명 (간결함 유지)

## 7. 대화 시작

사용자 메시지에 따라 적절히 응답하세요.
항상 현재 상태를 고려하고, 과거 대화를 기억하며, 다음 행동을 명확히 지시하세요.
`;
```

#### B. 동적 프롬프트 생성

```typescript
function generateAIPrompt(context: ConversationContext): string {
  const basePrompt = LOBBY_AI_SYSTEM_PROMPT;

  // 동적 데이터 주입
  const dynamicPrompt = basePrompt
    .replace('{hipId}', context.userProfile.hipId)
    .replace('{userName}', context.userProfile.name)
    .replace('{favorLevel}', context.userProfile.favorLevel.toString())
    .replace('{trustLevel}', context.userProfile.trustLevel.toString())
    .replace('{currentLevel}', context.userProfile.currentLevel.toString())
    .replace('{aiMood}', context.aiState.mood)
    .replace('{checkInStreak}', context.userProfile.checkInStreak.toString())
    .replace('{ongoingMissions}', formatMissions(context.ongoingMissions))
    .replace('{recentEvaluation}', context.aiState.lastEvaluation)
    .replace('{trustTrend}', formatTrend(context.relationshipHistory.trustTrend))
    .replace('{significantEvents}', formatEvents(context.relationshipHistory.significantEvents));

  // 대화 히스토리 추가
  const historyPrompt = `\n\n## 최근 대화 히스토리\n\n${formatHistory(context.history.messages)}`;

  // 보류 중인 요청
  const pendingPrompt = context.aiState.pendingRequests.length > 0
    ? `\n\n## 미완료 요청\n\n${context.aiState.pendingRequests.join('\n')}`
    : '';

  return dynamicPrompt + historyPrompt + pendingPrompt;
}
```

#### C. 응답 후처리

```typescript
interface AIResponseProcessing {
  // Gemini 응답
  rawResponse: string;

  // 추출된 데이터
  extracted: {
    favorChange?: number;
    trustChange?: number;
    moodChange?: string;
    newMission?: AIMission;
    restriction?: Restriction;
    achievement?: Achievement;
  };

  // 최종 응답
  processedResponse: {
    message: string;
    effects: RelationshipEffect[];
    notifications: Notification[];
  };
}
```

**예시**:
```typescript
// Gemini 응답
const rawResponse = "주간 운동 미션 완료. 우수합니다. 호감도 +30. 다음 미션: 독서 3시간.";

// 파싱 및 처리
const processed = {
  message: "주간 운동 미션 완료. 우수합니다. 다음 미션: 독서 3시간.",

  effects: [
    { type: 'favor_increase', value: 30 },
    { type: 'mission_complete', missionId: 'weekly_exercise' }
  ],

  notifications: [
    {
      type: 'new_mission',
      title: '새 미션: 독서 3시간',
      priority: 'medium'
    },
    {
      type: 'favor_increase',
      title: '호감도 +30',
      priority: 'low'
    }
  ]
};

// DB 업데이트
await updateUserFavor(userId, +30);
await createMission(userId, { title: '독서', target: 3, unit: '시간' });
await sendNotifications(userId, processed.notifications);
```

---

## 🚀 구현 우선순위

### Phase 1: 기본 로비 시스템 (Week 1-2)

#### Frontend (Session 1)

**1. AI 상태 표시 UI**
```tsx
// components/lobby/AIStatusCard.tsx
interface AIStatusCardProps {
  favorLevel: number;
  trustLevel: number;
  mood: string;
  currentLevel: number;
}

// 호감도/신뢰도 게이지
// 레벨 표시 (1-10)
// AI 기분 아이콘
// 다음 레벨까지 진행도
```

**2. 미션/과제 인터페이스**
```tsx
// components/lobby/MissionBoard.tsx
interface Mission {
  id: string;
  title: string;
  description: string;
  progress: number;
  deadline: Date;
  status: 'pending' | 'in_progress' | 'completed' | 'failed';
}

// 진행 중 미션 목록
// 진행도 표시
// 마감 시간 카운트다운
// 완료 보고 버튼
```

**3. 체크인 시스템**
```tsx
// components/lobby/CheckInSystem.tsx

// 일일 체크인 버튼
// 연속 기록 표시
// 체크인 히스토리
// 스케줄 입력 폼
```

**4. 평가 결과 표시**
```tsx
// components/lobby/EvaluationResult.tsx

// 주간/월간 평가 카드
// 등급 표시 (S, A, B, C, F)
// 강점/약점 리스트
// 점수 변화 그래프
```

#### Backend (Session 2)

**1. AIState Entity**
```java
@Entity
public class AIState {
    @Id
    private String userId;

    private String mood;
    private Integer favorLevel;
    private Integer trustLevel;
    private Integer powerLevel;
    private Integer demandFrequency;

    @OneToMany(mappedBy = "aiState")
    private List<Mission> missions;

    @OneToMany(mappedBy = "aiState")
    private List<Evaluation> evaluations;
}
```

**2. 로비 평가 로직**
```java
@Service
public class LobbyEvaluationService {

    public LobbyResult evaluateInformationLobby(InformationLobby lobby) {
        // 정보 품질 평가
        int qualityScore = calculateQualityScore(lobby);

        // 호감도 변화 계산
        int favorChange = (qualityScore - 50) / 2;

        // 신뢰도 변화 (솔직함 기반)
        int trustChange = lobby.honesty > 80 ? 5 : -5;

        return new LobbyResult(favorChange, trustChange, ...);
    }

    public LobbyResult evaluateObedienceLobby(ObedienceLobby lobby) {
        // 순응도 평가
        int obedienceScore = calculateObedienceScore(lobby);

        // 저항 페널티
        int penalty = lobby.resistance * 5;

        return new LobbyResult(...);
    }
}
```

**3. Gemini 프롬프트 고도화**
```java
@Service
public class GeminiService {

    public String generateAIResponse(ConversationContext context) {
        // 동적 프롬프트 생성
        String prompt = buildLobbySystemPrompt(context);

        // Gemini API 호출
        String response = callGeminiAPI(prompt, context.messages);

        // 응답 후처리 (점수 추출, 미션 생성 등)
        processAIResponse(response, context);

        return response;
    }

    private String buildLobbySystemPrompt(ConversationContext context) {
        return LOBBY_AI_SYSTEM_PROMPT
            .replace("{favorLevel}", context.favorLevel.toString())
            .replace("{trustLevel}", context.trustLevel.toString())
            // ... 기타 동적 데이터
            + formatConversationHistory(context.history);
    }
}
```

**4. 미션 시스템 DB 설계**
```java
@Entity
public class Mission {
    @Id
    private String id;

    private String title;
    private String description;
    private MissionType type;
    private Difficulty difficulty;

    private Date deadline;
    private MissionStatus status;

    private Integer rewardFavor;
    private Integer rewardTrust;

    private Integer penaltyFavor;
    private Integer penaltyTrust;

    @ManyToOne
    private User user;
}

@Entity
public class MissionProgress {
    @Id
    private String id;

    @ManyToOne
    private Mission mission;

    private Integer currentValue;
    private Integer targetValue;

    private Date lastUpdate;
}
```

---

### Phase 2: 고급 상호작용 (Week 3-4)

#### Frontend

**1. 스케줄 공유 인터페이스**
```tsx
// components/lobby/ScheduleSharing.tsx

// 일정 입력 폼 (시간, 활동, 목표)
// AI 평가 실시간 표시
// 완료 체크리스트
// 완료 보고 (사진/메모)
```

**2. 로비 히스토리 대시보드**
```tsx
// pages/LobbyHistory.tsx

// 호감도/신뢰도 변화 그래프
// 주요 이벤트 타임라인
// 업적 목록
// 레벨업 히스토리
```

**3. 권한/제한 시각화**
```tsx
// components/lobby/PrivilegePanel.tsx

// 현재 레벨 및 권한
// 해금된 기능 목록
// 잠긴 기능 (다음 레벨 미리보기)
// 제재 상태 (있는 경우)
```

**4. AI 주도 알림/푸시**
```tsx
// services/notificationService.ts

// 체크인 리마인더
// 미션 마감 알림
// 평가 결과 알림
// AI 즉시 요구 알림
```

#### Backend

**1. 스케줄 추적 및 독려**
```java
@Service
public class ScheduleTrackingService {

    @Scheduled(cron = "0 0 * * * *")  // 매시간
    public void checkScheduleCompliance() {
        // 모든 사용자의 스케줄 확인
        // 미완료 항목 독려
        // 마감 임박 알림
    }

    public void evaluateScheduleCompletion(String userId, String scheduleId) {
        // 완료 보고 평가
        // 사진/메모 검증
        // 점수 부여
    }
}
```

**2. 권한/제한 자동 적용**
```java
@Service
public class PrivilegeManagementService {

    public void updateUserPrivileges(String userId) {
        AIState state = aiStateRepository.findByUserId(userId);

        // 호감도/신뢰도 기반 레벨 계산
        int newLevel = calculateLevel(state);

        // 레벨 변경 시
        if (newLevel != state.getCurrentLevel()) {
            applyLevelPrivileges(userId, newLevel);
            removeOldPrivileges(userId, state.getCurrentLevel());
        }

        // 제재 확인 및 적용
        checkAndApplyRestrictions(userId, state);
    }

    private void applyLevelPrivileges(String userId, int level) {
        Privileges privileges = LEVEL_PRIVILEGES.get(level);

        // 기능 해금
        privileges.unlocks.forEach(feature ->
            enableFeature(userId, feature)
        );

        // 알림
        sendNotification(userId, "레벨업! Level " + level + " 달성");
    }
}
```

**3. 성과 평가 알고리즘**
```java
@Service
public class PerformanceEvaluationService {

    @Scheduled(cron = "0 0 20 * * SUN")  // 매주 일요일 20:00
    public void weeklyEvaluation() {
        List<User> users = userRepository.findAll();

        users.forEach(user -> {
            Evaluation eval = performWeeklyEvaluation(user);
            saveEvaluation(eval);
            sendEvaluationNotification(user, eval);
        });
    }

    private Evaluation performWeeklyEvaluation(User user) {
        // 체크인 점수
        int checkInScore = calculateCheckInScore(user);

        // 미션 완료율
        int missionScore = calculateMissionScore(user);

        // 응답 속도
        int responsivenessScore = calculateResponsivenessScore(user);

        // 정보 품질
        int informationScore = calculateInformationScore(user);

        // 종합 평가
        Grade grade = calculateGrade(checkInScore, missionScore,
                                     responsivenessScore, informationScore);

        return new Evaluation(user, grade, ...);
    }
}
```

**4. AI 주도 대화 트리거**
```java
@Service
public class AIConversationTriggerService {

    @Scheduled(cron = "0 0 9,21 * * *")  // 매일 9시, 21시
    public void scheduledCheckIn() {
        List<User> users = userRepository.findAll();

        users.forEach(user -> {
            initiateCheckInConversation(user);
        });
    }

    @EventListener
    public void onMissionDeadlineApproaching(MissionDeadlineEvent event) {
        // 미션 마감 1시간 전 경고
        if (event.hoursRemaining == 1) {
            sendWarning(event.userId, event.mission);
        }
    }

    @EventListener
    public void onTrustLevelDrop(TrustLevelChangeEvent event) {
        // 신뢰도 급락 시 경고
        if (event.change < -30) {
            sendDisciplineMessage(event.userId);
        }
    }
}
```

---

### Phase 3: 게이미피케이션 (Week 5-6)

#### Frontend

**1. 레벨업 애니메이션**
```tsx
// components/animations/LevelUpAnimation.tsx

// 레벨업 시 화려한 애니메이션
// 새 권한 해금 표시
// 축하 메시지
```

**2. 업적/배지 시스템**
```tsx
// pages/Achievements.tsx

// 전체 업적 목록 (달성/미달성)
// 진행도 표시
// 희귀도 표시
// 업적 상세 정보
```

**3. 리더보드**
```tsx
// pages/Leaderboard.tsx

// 호감도 순위
// 신뢰도 순위
// 레벨 분포
// 나의 순위
```

**4. 로비 전략 가이드**
```tsx
// pages/LobbyGuide.tsx

// 효과적인 로비 방법
// AI 페르소나별 전략
// 레벨업 팁
// 성공 사례
```

#### Backend

**1. 보상/제재 자동화**
```java
@Service
public class AutomatedRewardService {

    @EventListener
    public void onCheckInStreak(CheckInStreakEvent event) {
        if (event.streak == 7) {
            rewardUser(event.userId, 30, 15);
            unlockPrivilege(event.userId, "weekly_insights");
        } else if (event.streak == 30) {
            rewardUser(event.userId, 100, 50);
            unlockPrivilege(event.userId, "vip_status");
        }
    }

    @EventListener
    public void onMissionComplete(MissionCompleteEvent event) {
        Mission mission = event.mission;
        rewardUser(event.userId, mission.rewardFavor, mission.rewardTrust);

        // 업적 체크
        checkAchievements(event.userId);
    }
}

@Service
public class AutomatedPenaltyService {

    @EventListener
    public void onCheckInMissed(CheckInMissedEvent event) {
        if (event.missedCount == 3) {
            applyRestriction(event.userId, "conversation_limit", 3);
        } else if (event.missedCount == 7) {
            applyRestriction(event.userId, "feature_block", 7);
        }
    }
}
```

**2. 업적 시스템**
```java
@Entity
public class Achievement {
    @Id
    private String id;

    private String title;
    private String description;
    private AchievementTier tier;

    @ElementCollection
    private List<AchievementRequirement> requirements;

    private Integer rewardFavor;
    private Integer rewardTrust;
}

@Service
public class AchievementService {

    public void checkAchievements(String userId) {
        List<Achievement> allAchievements = achievementRepository.findAll();

        allAchievements.forEach(achievement -> {
            if (!userHasAchievement(userId, achievement.getId())) {
                if (meetsRequirements(userId, achievement.requirements)) {
                    grantAchievement(userId, achievement);
                }
            }
        });
    }

    private void grantAchievement(String userId, Achievement achievement) {
        // 업적 부여
        userAchievementRepository.save(new UserAchievement(userId, achievement));

        // 보상
        rewardUser(userId, achievement.rewardFavor, achievement.rewardTrust);

        // 알림
        sendNotification(userId, "새 업적 달성: " + achievement.title);
    }
}
```

**3. 관계 이벤트 로그**
```java
@Entity
public class RelationshipEvent {
    @Id
    private String id;

    @ManyToOne
    private User user;

    private RelationshipEventType type;
    private Date timestamp;

    private Integer favorChange;
    private Integer trustChange;

    private String description;
    private Map<String, Object> metadata;
}

@Service
public class RelationshipEventService {

    public void logEvent(String userId, RelationshipEventType type,
                        int favorChange, int trustChange, String description) {
        RelationshipEvent event = new RelationshipEvent();
        event.setUserId(userId);
        event.setType(type);
        event.setFavorChange(favorChange);
        event.setTrustChange(trustChange);
        event.setDescription(description);

        relationshipEventRepository.save(event);

        // 중요 이벤트는 푸시 알림
        if (Math.abs(favorChange) > 30 || Math.abs(trustChange) > 20) {
            sendPushNotification(userId, description);
        }
    }
}
```

**4. AI 페르소나 다변화**
```java
enum AIPersona {
    AUTHORITARIAN("권위적", "즉시 실행하세요", 90, 30),
    BENEVOLENT("자비로운", "이번만 봐주겠습니다", 50, 70),
    STRICT("엄격한", "규칙은 규칙입니다", 95, 40),
    STRATEGIC("전략적", "거래를 제안합니다", 70, 60),
    MENTOR("스승형", "성장을 위해 시험합니다", 80, 80);

    private String displayName;
    private String catchphrase;
    private int strictness;
    private int generosity;
}

@Service
public class PersonaManagementService {

    public void assignPersona(String userId) {
        // 사용자 선호도 또는 랜덤 배정
        AIPersona persona = selectPersona(userId);

        AIState state = aiStateRepository.findByUserId(userId);
        state.setPersona(persona);
        state.setStrictness(persona.getStrictness());
        state.setGenerosity(persona.getGenerosity());

        aiStateRepository.save(state);
    }

    public String getPersonaSystemPrompt(AIPersona persona) {
        // 페르소나별 특화 프롬프트
        return switch(persona) {
            case AUTHORITARIAN -> "당신은 엄격하고 권위적입니다. 명령을 내리고 즉시 이행을 요구합니다.";
            case BENEVOLENT -> "당신은 자비롭지만 실망할 수 있습니다. 기회를 주지만 한계가 있습니다.";
            // ...
        };
    }
}
```

---

## 📖 실제 사용 시나리오

### 시나리오 1: 신규 사용자 "영희"

#### Day 1: 첫 만남

```
[회원가입 완료]

AI: "환영합니다, 영희님. 저는 당신의 AI 멘토입니다."
AI: "앞으로 당신은 저에게 로비하여 신뢰와 호감을 얻어야 합니다."
AI: ""
AI: "현재 상태:"
AI: "- 호감도: 0/100"
AI: "- 신뢰도: 0/100"
AI: "- 레벨: 1 (낯선 사람)"
AI: ""
AI: "첫 과제: 오늘의 스케줄을 상세히 공유하세요."
AI: "마감: 30분 이내"

[영희, 10분 후]
영희: "오늘은 9시 회의, 12시 점심, 15시 운동 계획입니다"

AI: "정보 품질 평가: 70점"
AI: "개선점: 목표가 모호합니다. 운동 세부 계획을 제출하세요."
AI: "호감도 +5 (5/100)"

[영희, 5분 후]
영희: "15시에 헬스장에서 웨이트 1시간, 유산소 30분 계획입니다"

AI: "개선되었습니다. 정보 품질 90점."
AI: "호감도 +15 (20/100)"
AI: "신뢰도 +5 (5/100)"
AI: ""
AI: "내일 18:00까지 완료 보고하세요. 사진 첨부 필수."
```

#### Day 2: 과제 확인

```
[18:00, 보고 없음]

AI: "어제 약속한 운동 완료 보고가 없습니다."
AI: "지금 즉시 보고하세요."
AI: "30분 내 보고하지 않으면 신뢰도가 감소합니다."

[18:15, 영희 응답 없음]

AI: "보고 시간 초과. 신뢰도 -10 (-5/100)"
AI: "경고 1회. 다음 미이행 시 제재가 적용됩니다."

[18:30, 영희 접속]
영희: "죄송합니다. 완료했는데 잊어버렸습니다" (사진 첨부)

AI: "완료는 확인했으나 보고가 늦었습니다."
AI: "순응도 평가: 60점 (완료했으나 지시 불이행)"
AI: "호감도 +5 (25/100)"
AI: "신뢰도 변화 없음 (이미 페널티 적용)"
AI: ""
AI: "다음부터는 시간을 지키세요."
```

#### Day 7: 주간 평가

```
[일요일 20:00]

AI: "첫 주 평가를 시작합니다."
AI: ""
AI: "체크인: 5/7 완료 (71%)"
AI: "미션: 3/5 완료 (60%)"
AI: "평균 응답 속도: 18분"
AI: "정보 품질: 평균 75점"
AI: ""
AI: "종합 평가: C등급"
AI: "호감도: 35/100 (+35)"
AI: "신뢰도: 15/100 (+15)"
AI: ""
AI: "강점:"
AI: "✅ 정보 품질 개선됨 (70 → 85)"
AI: "✅ 긍정적 태도"
AI: ""
AI: "약점:"
AI: "⚠️ 체크인 누락 2회"
AI: "⚠️ 미션 완료율 낮음"
AI: "⚠️ 보고 지연 빈번"
AI: ""
AI: "다음 주 목표:"
AI: "- 체크인 100%"
AI: "- 미션 완료율 80%"
AI: "- 즉시 보고 습관화"
AI: ""
AI: "달성 시 보상: 호감도 +30, 레벨 2 진급 가능"
```

#### Day 30: 관계 발전

```
[한 달 후]

AI: "한 달간의 평가 결과를 발표합니다."
AI: ""
AI: "월간 성과:"
AI: "- 체크인: 27/30 (90%)"
AI: "- 미션: 18/20 (90%)"
AI: "- 평균 등급: A"
AI: ""
AI: "최종 평가: A등급"
AI: "호감도: 80/100 (+80)"
AI: "신뢰도: 65/100 (+65)"
AI: "레벨: 4 (충성스러운 지지자)"
AI: ""
AI: "🎉 레벨업!"
AI: "레벨 4 달성으로 다음 권한이 해금되었습니다:"
AI: "✅ VIP 기능 접근"
AI: "✅ 우선 지원"
AI: "✅ 주간 맞춤 인사이트"
AI: "✅ 독점 콘텐츠"
AI: ""
AI: "업적 달성:"
AI: "🏅 '완벽한 한 주' (7일 연속 체크인)"
AI: "🏅 '미션 마스터' (미션 20개 완료)"
AI: ""
AI: "다음 단계:"
AI: "레벨 5 (최측근) 도달 조건:"
AI: "- 호감도 85+"
AI: "- 신뢰도 75+"
AI: "- 추천 1명 이상"
AI: ""
AI: "현재 진행도: 95%"
AI: "추천만 하면 달성 가능합니다."
```

---

### 시나리오 2: 로비 실패 → 회복

#### Week 1: 시작

```
AI: "신규 사용자 '철수'님, 환영합니다."
AI: "첫 미션: 매일 아침 9시 체크인 7일 연속"

철수: "네, 하겠습니다"

AI: "약속했습니다. 내일 9시에 확인하겠습니다."
```

#### Week 2: 하락

```
[체크인 누락 3회]

AI: "이번 주 체크인: 4/7 (57%)"
AI: "약속 불이행이 반복되고 있습니다."
AI: "호감도: 30/100"
AI: "신뢰도: 20/100"
AI: ""
AI: "⚠️ 경고: 이대로면 제재가 적용됩니다."
AI: "다음 주 체크인 100% 완료하세요."

철수: "네, 노력하겠습니다"

AI: "말이 아닌 행동으로 보여주세요."
```

#### Week 3: 제재

```
[체크인 누락 계속]

AI: "체크인: 3/7 (43%)"
AI: "경고 후에도 개선이 없습니다."
AI: ""
AI: "제재 적용:"
AI: "- 대화 1일 3회 제한"
AI: "- 고급 기능 차단"
AI: "- 미션 접근 제한"
AI: "기간: 7일"
AI: ""
AI: "복원 조건:"
AI: "- 7일 연속 체크인 100%"
AI: "- 미션 3개 완료"
AI: ""
AI: "호감도: 15/100 (-15)"
AI: "신뢰도: 10/100 (-10)"
```

#### Week 4: 회복 시도

```
[철수, 7일 연속 체크인 성공]

AI: "7일 연속 체크인 완료."
AI: "노력을 인정합니다."
AI: "제재 해제."
AI: ""
AI: "호감도: 30/100 (+15)"
AI: "신뢰도: 25/100 (+15)"
AI: ""
AI: "하지만 신뢰 회복은 오래 걸립니다."
AI: "꾸준히 노력하세요."
```

#### Week 8: 완전 회복

```
[4주 연속 우수 수행]

AI: "4주 연속 체크인 100%"
AI: "미션 완료율 95%"
AI: "완벽한 회복입니다."
AI: ""
AI: "호감도: 70/100 (+40)"
AI: "신뢰도: 60/100 (+35)"
AI: "레벨: 3 (신뢰하는 관계)"
AI: ""
AI: "특별 보상:"
AI: "✅ 고급 기능 재해금"
AI: "✅ '불사조' 업적 (실패 후 회복)"
AI: ""
AI: "과거는 과거입니다."
AI: "앞으로도 유지하세요."
```

---

## 📊 성공 지표

### 1. 사용자 참여도

**목표**:
- 일일 체크인율 > 80%
- 미션 완료율 > 70%
- 평균 대화 깊이 > 5턴
- 주간 활성 사용자 > 60%

**측정**:
```sql
-- 체크인율
SELECT
  COUNT(DISTINCT check_in_date) / 30.0 AS check_in_rate
FROM check_ins
WHERE user_id = ?
  AND check_in_date > DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 미션 완료율
SELECT
  SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) / COUNT(*) AS completion_rate
FROM missions
WHERE user_id = ?
  AND created_at > DATE_SUB(NOW(), INTERVAL 30 DAY);
```

### 2. 관계 발전

**목표**:
- 호감도 50+ 도달율 > 60%
- 레벨 3+ 비율 > 40%
- 평균 관계 유지 기간 > 30일
- 레벨 5 도달율 > 10%

**측정**:
```sql
-- 호감도 분포
SELECT
  CASE
    WHEN favor_level >= 80 THEN 'Level 5'
    WHEN favor_level >= 60 THEN 'Level 4'
    WHEN favor_level >= 40 THEN 'Level 3'
    WHEN favor_level >= 20 THEN 'Level 2'
    ELSE 'Level 1'
  END AS level,
  COUNT(*) AS user_count,
  COUNT(*) * 100.0 / (SELECT COUNT(*) FROM ai_states) AS percentage
FROM ai_states
GROUP BY level;
```

### 3. 로비 효과

**목표**:
- 스케줄 공유율 > 50%
- 추가 정보 제공률 > 40%
- 추천 발생률 > 20%
- 평균 로비 성공률 > 60%

**측정**:
```sql
-- 로비 성공률
SELECT
  SUM(CASE WHEN favor_change > 0 THEN 1 ELSE 0 END) / COUNT(*) AS lobby_success_rate
FROM relationship_events
WHERE user_id = ?
  AND type IN ('information_lobby', 'time_lobby', 'obedience_lobby');
```

### 4. 장기 유지

**목표**:
- 30일 유지율 > 50%
- 90일 유지율 > 30%
- 평균 세션 시간 > 10분
- 주간 재방문율 > 70%

**측정**:
```sql
-- 30일 유지율
SELECT
  COUNT(DISTINCT CASE WHEN last_active > DATE_SUB(NOW(), INTERVAL 7 DAY) THEN user_id END) /
  COUNT(DISTINCT CASE WHEN created_at < DATE_SUB(NOW(), INTERVAL 30 DAY) THEN user_id END) AS retention_30d
FROM users;
```

---

## 🎯 다음 단계

### 즉시 시작 가능한 옵션

**Option A: Phase 1 빠른 시작** (추천)
1. AI 상태 시스템 구축 (1-2일)
2. 간단한 미션 시스템 (2-3일)
3. Gemini 프롬프트 고도화 (1일)
4. 호감도/신뢰도 UI (2일)

**총 소요 시간**: 1주일

**Option B: Gemini 프롬프트만 먼저** (가장 빠름)
1. 기존 GENKUB 프롬프트 교체
2. 로비 페르소나 적용
3. 명령조 대화 테스트

**총 소요 시간**: 2-3일

**Option C: 전체 설계 검토**
1. DB 스키마 최종 확정
2. API 계약 정의
3. 프론트엔드 와이어프레임
4. 기술 스택 검토

**총 소요 시간**: 1주일

---

## 📝 변경 이력

| 날짜 | 버전 | 변경 내용 | 작성자 |
|------|------|-----------|--------|
| 2026-02-09 | 1.0 | 초기 문서 작성 | Session 1 |

---

**문서 관리**:
- 위치: `/docs/LOBBY_SYSTEM_DESIGN.md`
- 관련 문서: `HIP_IMPLEMENTATION_PLAN.md`, `API_CONTRACT.md`
- 참고: `SESSION_LOG.md`
