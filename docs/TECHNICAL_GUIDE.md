# GENKUB/LobAI 기술 문서

**프로젝트**: AI-powered Virtual Tamagotchi Robot Companion
**버전**: MVP v1.0
**최종 업데이트**: 2025-12-30

---

## 📋 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [기술 스택](#기술-스택)
3. [아키텍처 설계](#아키텍처-설계)
4. [개발 환경 설정](#개발-환경-설정)
5. [핵심 기능 구현](#핵심-기능-구현)
6. [테스트 전략](#테스트-전략)
7. [배포 및 CI/CD](#배포-및-cicd)
8. [보안 가이드라인](#보안-가이드라인)
9. [성능 최적화](#성능-최적화)
10. [AI 개발 워크플로우](#ai-개발-워크플로우)
11. [문제 해결 가이드](#문제-해결-가이드)

---

## 프로젝트 개요

### 목적
**GENKUB**는 AI 기반 가상 타마고치 로봇 동반자로, 사용자가 AI 로봇(Lobi)을 돌보며 상호작용하는 웹 애플리케이션입니다. 이 프로젝트는 **LobAI** 플랫폼의 프로토타입으로, 사용자의 AI 적응도(AI Readiness)와 커뮤니케이션 패턴을 분석하는 것을 목표로 합니다.

### 핵심 가치
- **AI 시대 준비도 분석**: 사용자-AI 상호작용 패턴 추적
- **자연스러운 학습**: 게임화된 경험을 통한 AI 친숙도 향상
- **개인화된 피드백**: AI 커뮤니케이션 스타일 개선 코칭

### 주요 기능
1. **Stats 시스템**: Hunger, Energy, Happiness 3가지 스탯 자동 감소
2. **액션 버튼**: Feed, Play, Sleep 기능으로 스탯 복구
3. **AI 챗봇**: Google Gemini API 기반 한국어 대화
4. **3D 비주얼**: Spline 3D 캐릭터 렌더링
5. **랜딩 페이지**: LobAI 플랫폼 소개 (Features, About, CTA)

---

## 기술 스택

### Frontend Framework
| 기술 | 버전 | 용도 |
|------|------|------|
| **React** | 19.2.3 | UI 컴포넌트 라이브러리 |
| **TypeScript** | 5.8.2 | 타입 안전성 및 코드 품질 |
| **Vite** | 6.2.0 | 빌드 도구 및 개발 서버 |

### AI & 3D
| 기술 | 버전 | 용도 |
|------|------|------|
| **@google/genai** | 1.34.0 | Google Gemini AI API 클라이언트 |
| **@splinetool/react-spline** | 4.1.0 | 3D 캐릭터 렌더링 |
| **@splinetool/runtime** | 1.12.28 | Spline 런타임 엔진 |

### Styling
| 기술 | 버전 | 용도 |
|------|------|------|
| **TailwindCSS** | CDN | 유틸리티 우선 CSS 프레임워크 |
| **Custom CSS** | - | Glassmorphism, 그라디언트, 애니메이션 |

### Environment
- **Node.js**: 개발 환경
- **npm**: 패키지 관리자

---

## 아키텍처 설계

### 전체 구조

```
┌─────────────────────────────────────────┐
│         Browser (Client)                │
├─────────────────────────────────────────┤
│  React Application (index.tsx)          │
│  ├─ State Management (useState)         │
│  ├─ UI Components (JSX)                 │
│  ├─ Event Handlers                      │
│  └─ Effects (useEffect)                 │
├─────────────────────────────────────────┤
│  External Dependencies                  │
│  ├─ Spline 3D Renderer                  │
│  └─ Google Gemini AI API                │
└─────────────────────────────────────────┘
```

### 파일 구조

```
lobai/
├── index.html              # HTML 진입점 (TailwindCSS CDN, fonts)
├── index.tsx               # React 애플리케이션 전체 로직
├── vite.config.ts          # Vite 설정 (환경 변수 주입)
├── tsconfig.json           # TypeScript 설정
├── package.json            # 의존성 정의
├── .env.local              # 환경 변수 (GEMINI_API_KEY)
├── public/
│   └── scene.splinecode    # Spline 3D 씬 파일
├── .claude/
│   └── reference/          # AI 개발 가이드라인
│       ├── guideline.md
│       ├── plan-tamplate.md
│       └── SKILL.md
└── docs/
    └── TECHNICAL_GUIDE.md  # 이 문서
```

### 컴포넌트 계층 구조

```
LOBI_APP (Root Component)
├── Navigation Bar
├── Hero Section
│   ├── Stats Panel (Left)
│   ├── 3D Character (Center)
│   └── Chat Interface (Right)
├── Features Section
├── How It Works Section
├── CTA Section
└── Footer
```

### 상태 관리

**React Hooks 기반 상태 관리**:
```typescript
// Core States
const [stats, setStats] = useState<Stats>({
  hunger: 80,
  energy: 90,
  happiness: 70
});
const [messages, setMessages] = useState<Message[]>([...]);
const [inputValue, setInputValue] = useState('');
const [isTyping, setIsTyping] = useState(false);
const [navbarVisible, setNavbarVisible] = useState(true);
const [isCrying, setIsCrying] = useState(false);
const [splineReady, setSplineReady] = useState(false);

// Refs
const chatEndRef = useRef<HTMLDivElement>(null);
const splineRef = useRef<any>(null);
const mouthObjRef = useRef<any>(null);
const eyesObjRef = useRef<any>(null);
```

**상태 플로우**:
1. **Stats 자동 감소**: `useEffect` 타이머 → 5초마다 스탯 감소
2. **액션 버튼**: 사용자 클릭 → `handleAction` → 스탯 증가 + 봇 메시지
3. **채팅**: 사용자 입력 → `sendMessage` → Gemini API 호출 → 응답 표시

---

## 개발 환경 설정

### 1. Prerequisites
```bash
# Node.js 18+ 설치 확인
node --version

# npm 9+ 설치 확인
npm --version
```

### 2. 프로젝트 설정
```bash
# 저장소 클론 (또는 프로젝트 디렉터리 이동)
cd /path/to/lobai

# 의존성 설치
npm install
```

### 3. 환경 변수 설정
`.env.local` 파일 생성:
```bash
# Google Gemini API Key
GEMINI_API_KEY=your_actual_api_key_here
```

**API Key 발급**:
1. [Google AI Studio](https://makersuite.google.com/app/apikey) 방문
2. API Key 생성
3. `.env.local`에 복사

### 4. 개발 서버 실행
```bash
# 개발 모드 (localhost:3000)
npm run dev

# 브라우저에서 http://localhost:3000 접속
```

### 5. 빌드 및 배포
```bash
# 프로덕션 빌드
npm run build

# 빌드 결과 미리보기
npm run preview
```

---

## 핵심 기능 구현

### 1. Stats 시스템

**자동 감소 메커니즘**:
```typescript
useEffect(() => {
  const timer = setInterval(() => {
    setStats(prev => ({
      hunger: Math.max(0, prev.hunger - 0.5),
      energy: Math.max(0, prev.energy - 0.3),
      happiness: Math.max(0, prev.happiness - 0.4)
    }));
  }, 5000); // 5초마다 실행
  return () => clearInterval(timer); // 클린업
}, []);
```

**액션 핸들러**:
```typescript
const handleAction = (type: keyof Stats) => {
  setStats(prev => ({
    ...prev,
    [type]: Math.min(100, prev[type] + 15), // 해당 스탯 +15
    happiness: Math.min(100, prev.happiness + 5) // 행복도 +5
  }));

  const responses: Record<string, string> = {
    hunger: "냠냠! 에너지가 충전되고 있어요.",
    energy: "꿀잠 자고 일어났어요. 몸이 가볍네요!",
    happiness: "와! 같이 노니까 정말 즐거워요!"
  };

  setMessages(prev => [...prev, {
    role: 'bot',
    text: responses[type] || "기분이 좋아졌어요!"
  }]);
};
```

**프로그레스 바 스타일링**:
```typescript
const getBarColor = (val: number) => {
  if (val > 60) return 'bg-blue-400';   // 건강
  if (val > 30) return 'bg-yellow-400'; // 주의
  return 'bg-red-500';                  // 위험
};
```

### 2. AI 챗봇 (Gemini API)

**메시지 전송 로직**:
```typescript
const sendMessage = async () => {
  if (!inputValue.trim() || isTyping) return;

  const userText = inputValue;
  setMessages(prev => [...prev, { role: 'user', text: userText }]);
  setInputValue('');
  setIsTyping(true);

  try {
    const ai = new GoogleGenAI({
      apiKey: process.env.API_KEY || ''
    });

    const response = await ai.models.generateContent({
      model: 'gemini-3-flash-preview',
      contents: userText,
      config: {
        systemInstruction: `당신은 이름이 'Lobi'인 작고 귀여운 하이테크 로봇 타마고치입니다.
        현재 당신의 상태는 다음과 같습니다:
        배고픔 ${Math.round(stats.hunger)}%,
        에너지 ${Math.round(stats.energy)}%,
        행복도 ${Math.round(stats.happiness)}%.
        상태에 맞게 반응하세요.
        말투는 아주 친절하고 약간 기계적인 느낌이 섞인 귀여운 말투를 사용하세요.
        답변은 아주 짧고 간결하게 한두 문장으로 하세요.`,
        temperature: 0.8, // 자연스러운 변화
      }
    });

    const botText = response.text || "삐빅... 통신 오류가 발생했습니다.";
    setMessages(prev => [...prev, { role: 'bot', text: botText }]);

    // 대화 시 행복도 미세 증가
    setStats(prev => ({
      ...prev,
      happiness: Math.min(100, prev.happiness + 2)
    }));
  } catch (error) {
    console.error(error);
    setMessages(prev => [...prev, {
      role: 'bot',
      text: "삐비빅... 지금은 대화가 조금 어려워요."
    }]);
  } finally {
    setIsTyping(false);
  }
};
```

**System Instruction 설계 원칙**:
- **컨텍스트 주입**: 현재 스탯 값 전달로 상황에 맞는 응답 유도
- **페르소나 정의**: "Lobi", "타마고치", "로봇" 정체성 강화
- **톤 앤 매너**: 친절함 + 기계적 느낌 (예: ~입니다, 삐빅)
- **응답 길이**: 1-2 문장으로 제한 (간결성)

### 3. 3D 캐릭터 (Spline)

**Spline 로드 핸들러**:
```typescript
const onSplineLoad = (splineApp: any) => {
  splineRef.current = splineApp;

  try {
    // Spline 씬에서 오브젝트 찾기
    const mouth = splineApp.findObjectByName('Mouth Move 2');
    const eyes = splineApp.findObjectByName('Eyes Move 2');

    if (mouth) mouthObjRef.current = mouth;
    if (eyes) eyesObjRef.current = eyes;
  } catch (error) {
    console.error('Error finding objects:', error);
  }

  // 3D 씬 완전히 로드될 때까지 대기
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        setTimeout(() => {
          setSplineReady(true); // Fade-in 트리거
        }, 200);
      });
    });
  });
};
```

**캐릭터 상태 변경 (울기/정상)**:
```typescript
const handleCharacterClick = () => {
  if (!mouthObjRef.current || !eyesObjRef.current) {
    console.warn('⚠️ Objects not loaded yet.');
    return;
  }

  const newState = !isCrying;
  const stateName = newState ? 'cry' : 'State';

  try {
    // Spline 오브젝트의 state 프로퍼티 직접 변경
    mouthObjRef.current.state = stateName;
    eyesObjRef.current.state = stateName;

    setIsCrying(newState);
    console.log(`🎭 Character state: ${newState ? 'crying 😢' : 'normal 😊'}`);
  } catch (error) {
    console.error('❌ Error changing state:', error);
  }
};
```

**Spline 컴포넌트 렌더링**:
```typescript
<div
  className="w-full h-full transition-opacity duration-700 ease-out"
  style={{
    opacity: splineReady ? 1 : 0,
    visibility: splineReady ? 'visible' : 'hidden'
  }}
>
  <Spline
    scene="/scene.splinecode"
    onLoad={onSplineLoad}
    className="w-full h-full cursor-pointer"
  />
</div>
```

### 4. 네비게이션 자동 숨김

**스크롤 및 마우스 이벤트 기반 토글**:
```typescript
useEffect(() => {
  const handleScroll = () => {
    const currentScrollY = window.scrollY;

    // 최상단(100px 이내) 또는 위로 스크롤 시 표시
    if (currentScrollY < 100) {
      setNavbarVisible(true);
      resetHideTimer();
    } else if (currentScrollY < lastScrollY.current) {
      setNavbarVisible(true);
      resetHideTimer();
    }

    lastScrollY.current = currentScrollY;
  };

  const handleMouseMove = (e: MouseEvent) => {
    // 상단 80px 이내에 마우스 진입 시 표시
    if (e.clientY < 80) {
      setNavbarVisible(true);
      resetHideTimer();
    }
  };

  const resetHideTimer = () => {
    if (hideTimeoutRef.current) {
      clearTimeout(hideTimeoutRef.current);
    }
    // 3초 후 자동 숨김
    hideTimeoutRef.current = setTimeout(() => {
      if (window.scrollY > 100) {
        setNavbarVisible(false);
      }
    }, 3000);
  };

  // 초기 타이머 설정
  resetHideTimer();

  window.addEventListener('scroll', handleScroll);
  window.addEventListener('mousemove', handleMouseMove);

  return () => {
    window.removeEventListener('scroll', handleScroll);
    window.removeEventListener('mousemove', handleMouseMove);
    if (hideTimeoutRef.current) {
      clearTimeout(hideTimeoutRef.current);
    }
  };
}, []);
```

---

## 테스트 전략

### 현재 상태
⚠️ **주의**: 현재 프로젝트에는 테스트 코드가 구현되어 있지 않습니다.

### 권장 테스트 전략

#### Test Pyramid 구조
```
        ▲
       / \
      /E2E\      10% - End-to-End Tests (Playwright/Cypress)
     /─────\
    /Integ.\    20% - Integration Tests (React Testing Library)
   /────────\
  /  Unit    \  70% - Unit Tests (Vitest/Jest)
 /────────────\
```

#### 1. Unit Tests (70% 권장)

**테스트 대상**:
- 헬퍼 함수 (`getBarColor`)
- 상태 업데이트 로직 (`handleAction`)
- 유틸리티 함수

**테스트 프레임워크**: Vitest (Vite와 통합 우수)

**설치**:
```bash
npm install -D vitest @testing-library/react @testing-library/jest-dom
```

**예시 테스트**:
```typescript
// __tests__/utils.test.ts
import { describe, it, expect } from 'vitest';
import { getBarColor } from '../src/utils';

describe('getBarColor', () => {
  it('should return blue for values > 60', () => {
    expect(getBarColor(80)).toBe('bg-blue-400');
  });

  it('should return yellow for values 31-60', () => {
    expect(getBarColor(50)).toBe('bg-yellow-400');
  });

  it('should return red for values <= 30', () => {
    expect(getBarColor(20)).toBe('bg-red-500');
  });
});
```

#### 2. Integration Tests (20% 권장)

**테스트 대상**:
- React 컴포넌트 상호작용
- 사용자 이벤트 플로우
- 상태 변경 시나리오

**예시 테스트**:
```typescript
// __tests__/App.integration.test.tsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { LOBI_APP } from '../index';

describe('LOBI_APP Integration', () => {
  it('should increase hunger when Feed button is clicked', async () => {
    render(<LOBI_APP />);

    const feedButton = screen.getByText(/FEED UNIT/i);
    const initialHunger = screen.getByText(/HUNGER/i);

    fireEvent.click(feedButton);

    await waitFor(() => {
      // 스탯이 증가했는지 확인
      expect(initialHunger).toHaveTextContent(/\d+%/);
    });
  });

  it('should send message and receive bot response', async () => {
    render(<LOBI_APP />);

    const input = screen.getByPlaceholderText(/Lobi에게 메시지/i);
    const sendButton = screen.getByRole('button', { name: /send/i });

    fireEvent.change(input, { target: { value: '안녕' } });
    fireEvent.click(sendButton);

    await waitFor(() => {
      expect(screen.getByText(/안녕/i)).toBeInTheDocument();
    }, { timeout: 5000 });
  });
});
```

#### 3. E2E Tests (10% 권장)

**테스트 대상**:
- 전체 사용자 여정
- 크리티컬 패스

**테스트 프레임워크**: Playwright

**설치**:
```bash
npm install -D @playwright/test
npx playwright install
```

**예시 테스트**:
```typescript
// e2e/app.spec.ts
import { test, expect } from '@playwright/test';

test('complete user interaction flow', async ({ page }) => {
  await page.goto('http://localhost:3000');

  // 페이지 로드 확인
  await expect(page.locator('text=GENKUB')).toBeVisible();

  // Feed 버튼 클릭
  await page.click('button:has-text("FEED UNIT")');

  // 채팅 메시지 전송
  await page.fill('input[placeholder*="Lobi"]', '안녕하세요');
  await page.press('input[placeholder*="Lobi"]', 'Enter');

  // 봇 응답 대기
  await expect(page.locator('.bg-white\\/5').last()).toBeVisible({ timeout: 10000 });

  // 3D 캐릭터 클릭
  await page.click('.spline-container');
});
```

### 테스트 커버리지 목표

| 레이어 | 커버리지 목표 | 우선순위 |
|--------|--------------|----------|
| 비즈니스 로직 | ≥90% | 최상 |
| UI 컴포넌트 | ≥70% | 상 |
| API 호출 | ≥80% | 상 |
| 유틸리티 | ≥90% | 중 |

### TDD (Test-Driven Development) 워크플로우

**새 기능 개발 시**:
```
1. 🔴 RED: 실패하는 테스트 작성
   - 기능 요구사항을 테스트로 명세
   - 테스트 실행 → 실패 확인

2. 🟢 GREEN: 최소 코드 작성
   - 테스트를 통과시키는 최소한의 구현
   - 테스트 실행 → 통과 확인

3. 🔵 REFACTOR: 코드 개선
   - 중복 제거, 네이밍 개선, 구조 최적화
   - 테스트 실행 → 여전히 통과 확인
```

---

## 배포 및 CI/CD

### 배포 옵션

#### 1. Vercel (권장)
**장점**: Zero-config, Vite 최적화, 무료 호스팅

**배포 절차**:
```bash
# 1. Vercel CLI 설치
npm i -g vercel

# 2. 프로젝트 배포
vercel

# 3. 환경 변수 설정 (Vercel Dashboard)
# GEMINI_API_KEY 추가

# 4. 프로덕션 배포
vercel --prod
```

**환경 변수 설정** (Vercel Dashboard):
1. Project Settings → Environment Variables
2. `GEMINI_API_KEY` 추가
3. Production, Preview, Development 체크

#### 2. Netlify
```bash
# 1. Netlify CLI 설치
npm i -g netlify-cli

# 2. 빌드 설정
# Build command: npm run build
# Publish directory: dist

# 3. 배포
netlify deploy --prod

# 4. 환경 변수 설정 (Netlify Dashboard)
# Site settings → Environment variables
```

#### 3. GitHub Pages
```bash
# vite.config.ts에 base 추가
export default defineConfig({
  base: '/lobai/',
  // ...
});

# package.json에 deploy 스크립트 추가
"scripts": {
  "deploy": "vite build && gh-pages -d dist"
}

# 배포
npm run deploy
```

### CI/CD 파이프라인 (GitHub Actions)

**`.github/workflows/ci.yml`**:
```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main, master]
  pull_request:
    branches: [main, master]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'

      - name: Install dependencies
        run: npm ci

      - name: Run linter
        run: npm run lint

      - name: Run type check
        run: npx tsc --noEmit

      - name: Run tests
        run: npm test

      - name: Build
        run: npm run build

  deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v3

      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'

      - name: Install and Build
        run: |
          npm ci
          npm run build

      - name: Deploy to Vercel
        uses: amondnet/vercel-action@v20
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.VERCEL_ORG_ID }}
          vercel-project-id: ${{ secrets.VERCEL_PROJECT_ID }}
```

---

## 보안 가이드라인

### 1. API Key 보호

**❌ 절대 금지**:
```typescript
// 코드에 직접 하드코딩
const apiKey = "AIzaSyABC123..."; // ❌ NEVER DO THIS
```

**✅ 올바른 방법**:
```typescript
// .env.local에서 로드
const apiKey = process.env.API_KEY || '';
```

**.gitignore 필수**:
```
.env.local
.env*.local
```

### 2. 환경 변수 검증

**서버 사이드에서만 접근**:
```typescript
// vite.config.ts에서 주입
export default defineConfig({
  define: {
    'process.env.API_KEY': JSON.stringify(process.env.GEMINI_API_KEY)
  }
});
```

### 3. 입력 검증

**XSS 방지**:
```typescript
const sendMessage = async () => {
  // 입력값 검증
  if (!inputValue.trim() || inputValue.length > 500) {
    return; // 빈 값 또는 너무 긴 입력 차단
  }

  // React는 기본적으로 XSS 방지하지만, dangerouslySetInnerHTML 사용 시 주의
};
```

### 4. Rate Limiting (서버 구현 시)

**클라이언트 측 임시 제한**:
```typescript
const [lastRequestTime, setLastRequestTime] = useState(0);

const sendMessage = async () => {
  const now = Date.now();
  if (now - lastRequestTime < 2000) {
    // 2초 이내 재요청 차단
    return;
  }
  setLastRequestTime(now);
  // ... API 호출
};
```

### 5. HTTPS 강제

**프로덕션 배포 시**:
- Vercel/Netlify는 자동 HTTPS
- 커스텀 도메인 사용 시 SSL 인증서 필수

### 6. 보안 헤더 (서버 설정)

**`vercel.json` 예시**:
```json
{
  "headers": [
    {
      "source": "/(.*)",
      "headers": [
        {
          "key": "X-Content-Type-Options",
          "value": "nosniff"
        },
        {
          "key": "X-Frame-Options",
          "value": "DENY"
        },
        {
          "key": "X-XSS-Protection",
          "value": "1; mode=block"
        },
        {
          "key": "Referrer-Policy",
          "value": "strict-origin-when-cross-origin"
        }
      ]
    }
  ]
}
```

---

## 성능 최적화

### 1. 번들 크기 최적화

**현재 번들 분석**:
```bash
npm install -D rollup-plugin-visualizer

# vite.config.ts에 추가
import { visualizer } from 'rollup-plugin-visualizer';

export default defineConfig({
  plugins: [
    react(),
    visualizer()
  ]
});

# 빌드 후 stats.html 확인
npm run build
```

**Code Splitting**:
```typescript
// React.lazy로 동적 임포트
const SplineComponent = React.lazy(() => import('@splinetool/react-spline'));

// Suspense로 감싸기
<Suspense fallback={<div>Loading 3D...</div>}>
  <SplineComponent scene="/scene.splinecode" />
</Suspense>
```

### 2. 이미지 최적화

**WebP 포맷 사용**:
```html
<picture>
  <source srcset="image.webp" type="image/webp">
  <img src="image.jpg" alt="Fallback">
</picture>
```

**Lazy Loading**:
```jsx
<img src="hero.jpg" loading="lazy" alt="Hero" />
```

### 3. Spline 최적화

**3D 씬 경량화**:
- Spline Editor에서 폴리곤 수 감소
- 텍스처 해상도 최적화 (1024x1024 이하)
- 불필요한 애니메이션 제거

**로딩 전략**:
```typescript
// 페이드인 효과로 로딩 체감 개선
<div
  style={{
    opacity: splineReady ? 1 : 0,
    transition: 'opacity 0.7s ease-out'
  }}
>
  <Spline scene="/scene.splinecode" onLoad={onSplineLoad} />
</div>
```

### 4. React 최적화

**메모이제이션**:
```typescript
import { useMemo, useCallback } from 'react';

// 계산 비용이 큰 작업
const barColor = useMemo(() => getBarColor(stats.hunger), [stats.hunger]);

// 이벤트 핸들러 캐싱
const handleFeed = useCallback(() => {
  handleAction('hunger');
}, []);
```

**Virtual Scrolling** (채팅 메시지가 많을 때):
```bash
npm install react-window
```

### 5. API 호출 최적화

**디바운싱**:
```typescript
import { useDebounce } from 'use-debounce';

const [inputValue, setInputValue] = useState('');
const [debouncedValue] = useDebounce(inputValue, 500);

useEffect(() => {
  if (debouncedValue) {
    // API 호출
  }
}, [debouncedValue]);
```

**응답 캐싱**:
```typescript
const messageCache = useRef<Map<string, string>>(new Map());

const sendMessage = async () => {
  const cached = messageCache.current.get(userText);
  if (cached) {
    setMessages(prev => [...prev, { role: 'bot', text: cached }]);
    return;
  }
  // API 호출 후 캐시에 저장
};
```

---

## AI 개발 워크플로우

### Feature Planner Skill 사용

**새 기능 개발 시**:
1. `/feature-planner` 스킬 호출
2. TDD 기반 단계별 계획 생성
3. `docs/plans/PLAN_<feature-name>.md` 생성
4. 사용자 승인 후 구현 시작

**예시**:
```markdown
# PLAN: Add User Profile System

## Phase 1: 데이터 모델 설계
### 🔴 RED: 테스트 작성
- [ ] User 타입 정의 테스트
- [ ] Profile CRUD 테스트

### 🟢 GREEN: 최소 구현
- [ ] User 인터페이스 구현
- [ ] LocalStorage 저장 로직

### 🔵 REFACTOR: 개선
- [ ] 중복 코드 제거
- [ ] 타입 안전성 강화

### Quality Gate ✋
- [ ] 모든 테스트 통과
- [ ] 타입 체크 통과
- [ ] 린트 에러 없음
```

### TDD 사이클 준수

**모든 코드 변경 시**:
```
1. 🔴 RED: 실패하는 테스트 작성
   예: "Feed 버튼 클릭 시 hunger 15 증가"

2. 🟢 GREEN: 테스트 통과시키기
   예: handleAction 함수 구현

3. 🔵 REFACTOR: 코드 개선
   예: 중복 제거, 네이밍 개선
```

### Code Review Checklist

**PR 생성 전 확인**:
- [ ] 테스트 커버리지 ≥80%
- [ ] 빌드 성공 (`npm run build`)
- [ ] 타입 체크 통과 (`npx tsc --noEmit`)
- [ ] 린트 에러 없음
- [ ] 보안 취약점 없음
- [ ] 성능 저하 없음
- [ ] 문서 업데이트 (필요 시)

### Security Checklist

**외부 API 연동 시**:
- [ ] API Key 환경 변수 사용
- [ ] 입력값 검증
- [ ] Rate Limiting 구현
- [ ] 에러 핸들링
- [ ] 로그에 민감정보 제외

---

## 문제 해결 가이드

### 1. Spline 3D 캐릭터가 로드되지 않음

**증상**:
- 빈 화면 또는 검은 화면
- 콘솔 에러: "Failed to load scene"

**해결 방법**:
```typescript
// 1. scene.splinecode 파일 경로 확인
<Spline scene="/scene.splinecode" /> // public/ 폴더 기준

// 2. CORS 문제 확인 (로컬 개발 시)
// vite.config.ts에서 서버 설정
export default defineConfig({
  server: {
    fs: {
      allow: ['..']
    }
  }
});

// 3. Spline 버전 호환성 확인
npm list @splinetool/react-spline
npm list @splinetool/runtime
```

### 2. Gemini API 호출 실패

**증상**:
- "통신 오류" 메시지
- 콘솔 에러: "API key not valid"

**해결 방법**:
```bash
# 1. .env.local 파일 확인
cat .env.local
# GEMINI_API_KEY=your_key 형식 확인

# 2. 환경 변수 주입 확인
# vite.config.ts에서 define 설정 확인

# 3. API Key 권한 확인
# Google AI Studio에서 API Key 활성화 상태 확인

# 4. 개발 서버 재시작
npm run dev
```

### 3. Stats가 자동으로 감소하지 않음

**진단**:
```typescript
// useEffect 의존성 배열 확인
useEffect(() => {
  const timer = setInterval(() => {
    setStats(prev => ({
      hunger: Math.max(0, prev.hunger - 0.5),
      energy: Math.max(0, prev.energy - 0.3),
      happiness: Math.max(0, prev.happiness - 0.4)
    }));
  }, 5000);
  return () => clearInterval(timer); // ✅ 클린업 필수
}, []); // ✅ 빈 배열로 한 번만 실행
```

### 4. 빌드 실패

**타입 에러**:
```bash
# TypeScript 타입 체크
npx tsc --noEmit

# 일반적인 에러: process.env 타입 정의
# vite-env.d.ts 생성
/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly API_KEY: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
```

**의존성 충돌**:
```bash
# package-lock.json 삭제 후 재설치
rm -rf node_modules package-lock.json
npm install
```

### 5. 채팅 스크롤 문제

**자동 스크롤 개선**:
```typescript
useEffect(() => {
  if (messages.length > 1) {
    chatEndRef.current?.scrollIntoView({
      behavior: 'smooth',
      block: 'end' // 하단 정렬
    });
  }
}, [messages]);

// ref 정확히 배치
<div ref={chatEndRef} />
```

---

## 향후 개발 로드맵

### Phase 1: 테스트 인프라 구축 (우선순위: 최상)
- [ ] Vitest 설정
- [ ] React Testing Library 통합
- [ ] 기존 기능에 대한 Unit/Integration 테스트 작성
- [ ] CI/CD 파이프라인에 테스트 추가

### Phase 2: 사용자 인증 시스템
- [ ] Firebase Auth 또는 Supabase 연동
- [ ] 사용자 프로필 관리
- [ ] Stats 데이터 영구 저장 (LocalStorage → Database)

### Phase 3: AI 분석 기능 (LobAI 핵심)
- [ ] 대화 패턴 분석 알고리즘
- [ ] AI Readiness Score 계산 로직
- [ ] 개인화된 리포트 생성

### Phase 4: 게임화 확장
- [ ] 업적 시스템 (Achievements)
- [ ] 레벨 시스템
- [ ] 일일 미션

### Phase 5: 백엔드 구축
- [ ] API 서버 (Node.js/Express 또는 Fastify)
- [ ] 데이터베이스 (PostgreSQL + Prisma)
- [ ] 실시간 기능 (WebSocket)

---

## 참고 자료

### 공식 문서
- [React 19 Docs](https://react.dev/)
- [Vite Guide](https://vitejs.dev/guide/)
- [Google Gemini API](https://ai.google.dev/docs)
- [Spline Documentation](https://docs.spline.design/)
- [TailwindCSS](https://tailwindcss.com/docs)

### 개발 가이드라인
- [`.claude/reference/guideline.md`](./.claude/reference/guideline.md) - AI 개발 도구 종합 가이드
- [`.claude/reference/SKILL.md`](./.claude/reference/SKILL.md) - Feature Planner 스킬
- [`.claude/reference/plan-tamplate.md`](./.claude/reference/plan-tamplate.md) - TDD 구현 계획 템플릿

### 추천 리소스
- [React Testing Best Practices](https://kentcdodds.com/blog/common-mistakes-with-react-testing-library)
- [TDD by Example](https://www.amazon.com/Test-Driven-Development-Kent-Beck/dp/0321146530)
- [Vite Performance Tips](https://vitejs.dev/guide/performance.html)

---

**문서 버전**: 1.0.0
**최종 업데이트**: 2025-12-30
**다음 업데이트 예정**: 테스트 인프라 구축 완료 후
