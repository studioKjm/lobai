# Session 4: Frontend Dashboard 빠른 시작 가이드

> **담당 모듈**: Frontend Module
> **브랜치**: feature/frontend-dashboard
> **우선순위**: 🔥 긴급 (Week 1-2)
> **목표**: HIP Dashboard MVP 완성

---

## 🚀 즉시 시작

### 1. 브랜치 생성

```bash
cd frontend
git checkout develop
git pull
git checkout -b feature/frontend-dashboard
```

### 2. 의존성 설치

```bash
npm install

# 추가 라이브러리 (필요 시)
npm install @tanstack/react-query axios recharts lucide-react
npm install -D @types/node
```

### 3. Mock API 설정

**파일**: `src/api/mockHIPApi.ts`

```typescript
// Mock API (Backend 완성 전까지 사용)
import { HIPProfile, ReanalyzeResponse } from './types';

export const mockHIPApi = {
  // GET /api/hip/me
  getMyProfile: async (): Promise<HIPProfile> => {
    // 1초 지연 (실제 API처럼)
    await new Promise(resolve => setTimeout(resolve, 1000));

    return {
      hipId: "HIP-01-A7F2E9C4-B3A1",
      userId: 1,
      overallHipScore: 78.5,
      identityLevel: 7,
      reputationTier: "Distinguished",
      coreScores: {
        cognitiveFlexibility: 82.0,
        collaborationPattern: 85.0,
        informationProcessing: 75.0,
        emotionalIntelligence: 78.0,
        creativity: 70.0,
        ethicalAlignment: 81.0
      },
      createdAt: "2026-02-01T10:00:00Z",
      lastUpdatedAt: "2026-02-08T15:30:00Z",
      totalInteractions: 142,
      verificationStatus: "VERIFIED"
    };
  },

  // POST /api/hip/me/reanalyze
  reanalyze: async (): Promise<ReanalyzeResponse> => {
    await new Promise(resolve => setTimeout(resolve, 2000));

    return {
      hipId: "HIP-01-A7F2E9C4-B3A1",
      previousScore: 78.5,
      newScore: 81.2,
      scoreChange: 2.7,
      previousLevel: 7,
      newLevel: 8,
      levelChanged: true,
      updatedScores: {
        cognitiveFlexibility: { old: 82.0, new: 84.0, change: 2.0 },
        collaborationPattern: { old: 85.0, new: 87.0, change: 2.0 },
        informationProcessing: { old: 75.0, new: 78.0, change: 3.0 },
        emotionalIntelligence: { old: 78.0, new: 80.0, change: 2.0 },
        creativity: { old: 70.0, new: 73.0, change: 3.0 },
        ethicalAlignment: { old: 81.0, new: 85.0, change: 4.0 }
      },
      message: "HIP profile updated successfully",
      updatedAt: new Date().toISOString()
    };
  }
};
```

### 4. 개발 서버 실행

```bash
npm run dev
```

**접속**: http://localhost:3000

---

## 📦 구현할 컴포넌트

### Phase 1: 핵심 컴포넌트 (Week 1)

#### 1. HIPDashboard.tsx (메인 페이지)

**위치**: `src/pages/HIPDashboard.tsx`

**기능**:
- HIP Profile 조회 및 표시
- Overall Score 표시 (큰 숫자)
- Identity Level 표시 (1-10)
- Reputation Tier 뱃지

**디자인**:
```
┌─────────────────────────────────────────┐
│  HIP Dashboard                          │
├─────────────────────────────────────────┤
│                                          │
│  HIP ID: HIP-01-A7F2E9C4-B3A1           │
│                                          │
│       Overall HIP Score                 │
│            78.5                          │
│       ════════════                       │
│     Level 7 - Distinguished             │
│                                          │
│  ┌──────────────────────────────────┐  │
│  │  [6 Core Scores Radar Chart]    │  │
│  └──────────────────────────────────┘  │
│                                          │
│  [Reanalyze Button] [Share Button]     │
│                                          │
└─────────────────────────────────────────┘
```

**샘플 코드**:
```tsx
import { useQuery } from '@tanstack/react-query';
import { mockHIPApi } from '@/api/mockHIPApi';
import { ScoreChart } from '@/components/ScoreChart';
import { IdentityCard } from '@/components/IdentityCard';

export default function HIPDashboard() {
  const { data: profile, isLoading } = useQuery({
    queryKey: ['hipProfile'],
    queryFn: mockHIPApi.getMyProfile
  });

  if (isLoading) return <div>Loading...</div>;
  if (!profile) return <div>No profile found</div>;

  return (
    <div className="container mx-auto p-8">
      <h1 className="text-4xl font-bold mb-8">HIP Dashboard</h1>

      <IdentityCard profile={profile} />

      <div className="mt-8">
        <ScoreChart coreScores={profile.coreScores} />
      </div>

      <div className="mt-8 flex gap-4">
        <button className="btn-primary">Reanalyze</button>
        <button className="btn-secondary">Share</button>
      </div>
    </div>
  );
}
```

---

#### 2. ScoreChart.tsx (Radar Chart)

**위치**: `src/components/ScoreChart.tsx`

**라이브러리**: Recharts

**기능**:
- 6 Core Scores를 Radar Chart로 표시

**샘플 코드**:
```tsx
import { Radar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer } from 'recharts';

interface ScoreChartProps {
  coreScores: {
    cognitiveFlexibility: number;
    collaborationPattern: number;
    informationProcessing: number;
    emotionalIntelligence: number;
    creativity: number;
    ethicalAlignment: number;
  };
}

export function ScoreChart({ coreScores }: ScoreChartProps) {
  const data = [
    { dimension: 'Cognitive Flexibility', value: coreScores.cognitiveFlexibility },
    { dimension: 'Collaboration', value: coreScores.collaborationPattern },
    { dimension: 'Information Processing', value: coreScores.informationProcessing },
    { dimension: 'Emotional Intelligence', value: coreScores.emotionalIntelligence },
    { dimension: 'Creativity', value: coreScores.creativity },
    { dimension: 'Ethical Alignment', value: coreScores.ethicalAlignment }
  ];

  return (
    <ResponsiveContainer width="100%" height={400}>
      <RadarChart data={data}>
        <PolarGrid />
        <PolarAngleAxis dataKey="dimension" />
        <PolarRadiusAxis angle={90} domain={[0, 100]} />
        <Radar name="HIP Scores" dataKey="value" stroke="#8884d8" fill="#8884d8" fillOpacity={0.6} />
      </RadarChart>
    </ResponsiveContainer>
  );
}
```

---

#### 3. IdentityCard.tsx (프로필 카드)

**위치**: `src/components/IdentityCard.tsx`

**기능**:
- HIP ID 표시
- Overall Score (큰 숫자)
- Identity Level + Tier
- 검증 상태 뱃지

**디자인**:
```
┌────────────────────────────────────┐
│  HIP-01-A7F2E9C4-B3A1   [VERIFIED]│
│                                     │
│         Overall Score               │
│            78.5                     │
│                                     │
│   Level 7 - Distinguished          │
│   ■■■■■■■□□□                       │
│                                     │
│   142 interactions                 │
│   Last updated: 2 hours ago        │
└────────────────────────────────────┘
```

---

### Phase 2: 고급 기능 (Week 2)

#### 4. ReanalyzePage.tsx

**기능**:
- Reanalyze 버튼 클릭 시
- 로딩 스피너 (2초)
- Before/After 비교 표시
- Score 변화 애니메이션

#### 5. CertificatePage.tsx (Blockchain 연동)

**기능**:
- Certificate 발급 요청 (SILVER, GOLD, PLATINUM)
- NFT 이미지 미리보기
- OpenSea 링크
- 다운로드 (PNG, PDF)

#### 6. RankingPage.tsx

**기능**:
- 전체 랭킹 조회
- 내 순위 강조
- 페이지네이션

---

## 🎨 디자인 시스템

### Tailwind Config

```javascript
// tailwind.config.js
module.exports = {
  theme: {
    extend: {
      colors: {
        'hip-primary': '#6366f1', // Indigo
        'hip-secondary': '#8b5cf6', // Purple
        'hip-accent': '#ec4899', // Pink
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        display: ['Outfit', 'sans-serif'],
      }
    }
  }
};
```

### 컴포넌트 스타일 가이드

```tsx
// 버튼
<button className="px-6 py-3 bg-hip-primary text-white rounded-lg hover:bg-hip-primary/90 transition">
  Reanalyze
</button>

// 카드
<div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
  {/* content */}
</div>

// 뱃지
<span className="px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-medium">
  VERIFIED
</span>
```

---

## 🧪 테스트 전략

### 1. Component Tests (Vitest)

```bash
npm install -D vitest @testing-library/react @testing-library/jest-dom
```

**예시**:
```typescript
// ScoreChart.test.tsx
import { render, screen } from '@testing-library/react';
import { ScoreChart } from './ScoreChart';

test('renders 6 core scores', () => {
  const mockScores = {
    cognitiveFlexibility: 80,
    collaborationPattern: 85,
    informationProcessing: 75,
    emotionalIntelligence: 78,
    creativity: 70,
    ethicalAlignment: 81
  };

  render(<ScoreChart coreScores={mockScores} />);

  expect(screen.getByText('Cognitive Flexibility')).toBeInTheDocument();
  expect(screen.getByText('Collaboration')).toBeInTheDocument();
});
```

### 2. E2E Tests (Playwright)

```bash
npx playwright install
```

**예시**:
```typescript
// e2e/hip-dashboard.spec.ts
import { test, expect } from '@playwright/test';

test('loads HIP dashboard', async ({ page }) => {
  await page.goto('http://localhost:3000/dashboard');

  // HIP ID 표시 확인
  await expect(page.locator('text=HIP-01')).toBeVisible();

  // Overall Score 표시 확인
  await expect(page.locator('text=78.5')).toBeVisible();

  // Radar Chart 렌더링 확인
  await expect(page.locator('.recharts-radar')).toBeVisible();
});
```

---

## 📊 Backend 연동 준비

### Real API Client (Backend 완성 후)

**파일**: `src/api/hipApiClient.ts`

```typescript
import axios from 'axios';
import { HIPProfile, ReanalyzeResponse } from './types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// JWT 토큰 인터셉터
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const hipApi = {
  getMyProfile: async (): Promise<HIPProfile> => {
    const { data } = await apiClient.get('/hip/me');
    return data;
  },

  reanalyze: async (): Promise<ReanalyzeResponse> => {
    const { data } = await apiClient.post('/hip/me/reanalyze');
    return data;
  },

  getRanking: async (limit = 10, offset = 0) => {
    const { data } = await apiClient.get(`/hip/ranking?limit=${limit}&offset=${offset}`);
    return data;
  }
};
```

### 환경 변수 설정

**파일**: `.env.local`

```
VITE_API_URL=http://localhost:8080/api
VITE_USE_MOCK=true  # true: Mock API, false: Real API
```

**사용**:
```typescript
// src/api/index.ts
import { mockHIPApi } from './mockHIPApi';
import { hipApi } from './hipApiClient';

const useMock = import.meta.env.VITE_USE_MOCK === 'true';

export const api = useMock ? mockHIPApi : hipApi;
```

---

## 📅 Week 1-2 체크리스트

### Week 1
- [ ] ✅ 브랜치 생성 (feature/frontend-dashboard)
- [ ] ✅ Mock API 구현
- [ ] ✅ HIPDashboard 페이지 구현
- [ ] ✅ ScoreChart 컴포넌트 구현
- [ ] ✅ IdentityCard 컴포넌트 구현
- [ ] ✅ 반응형 디자인 (Mobile, Tablet, Desktop)

### Week 2
- [ ] ✅ Reanalyze 기능 구현
- [ ] ✅ Share 기능 구현 (URL 복사, SNS 공유)
- [ ] ✅ Ranking 페이지 구현
- [ ] ✅ Component Tests 작성
- [ ] ✅ E2E Tests 작성
- [ ] ✅ PR 생성 (develop 브랜치로)

---

## 🎯 성공 기준

```
✅ HIP Dashboard 페이지 완성
✅ 6 Core Scores Radar Chart 동작
✅ Reanalyze 버튼 동작 (Mock)
✅ 반응형 디자인 (Mobile, Tablet, Desktop)
✅ Component Test 커버리지 70%+
✅ E2E Test 3개 이상 작성
✅ 코드 리뷰 완료
```

---

**Mock API로 먼저 개발하고, Backend (Session 1) 완성 후 연동하세요!**

**Let's Build the Frontend! 🎨**
