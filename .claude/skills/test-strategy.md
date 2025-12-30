---
name: test-strategy
description: 효율적인 테스트 작성 전략 (Test Pyramid). 과잉/부족 테스트 방지.
triggers: ["test strategy", "test pyramid", "coverage", "testing approach", "test plan"]
---

# Test Strategy

## Purpose

**Test Pyramid 원칙**을 기반으로 효율적인 테스트 전략을 수립합니다. 과도한 E2E 테스트나 부족한 단위 테스트를 방지하여 **빠르고 안정적인 테스트 스위트**를 구축합니다.

---

## Test Pyramid

```
        ▲
       /E2E\          10% (느림, 최소한)
      /─────\         - 핵심 사용자 시나리오만
     /Integ.\        20% (중간 속도)
    /────────\       - 컴포넌트 간 상호작용
   /  Unit    \      70% (빠름, 많이)
  /────────────\     - 비즈니스 로직 중심
```

---

## Test Types

### 1. Unit Tests (70% 권장)

**Target**: 개별 함수, 메서드, 클래스

**Characteristics**:
- **빠름**: < 100ms per test
- **독립적**: 외부 의존성 없음 (모두 mocking/stubbing)
- **많이**: 수백~수천 개
- **격리됨**: 데이터베이스, 네트워크, 파일 시스템 접근 안 함

**What to Test**:
- ✅ 비즈니스 로직
- ✅ 유틸리티 함수
- ✅ 데이터 변환 로직
- ✅ 복잡한 조건문/루프
- ✅ 에러 핸들링

**What NOT to Test**:
- ❌ Getter/Setter (단순 데이터 접근)
- ❌ Private 메서드 (public API만 테스트)
- ❌ 라이브러리 내부 (신뢰 가정)
- ❌ 단순 상수

**Example**:
```typescript
// ✅ 좋은 Unit Test 예시
describe('getBarColor', () => {
  it('should return blue for values > 60', () => {
    expect(getBarColor(80)).toBe('bg-blue-400')
  })

  it('should return yellow for values 31-60', () => {
    expect(getBarColor(50)).toBe('bg-yellow-400')
  })

  it('should return red for values <= 30', () => {
    expect(getBarColor(20)).toBe('bg-red-500')
  })
})
```

---

### 2. Integration Tests (20% 권장)

**Target**: 컴포넌트 간 상호작용

**Characteristics**:
- **중간 속도**: < 1s per test
- **일부 의존성**: 실제 또는 Mock
- **적당히**: 수십~수백 개
- **컴포넌트 경계**: 레이어 간 통신 검증

**What to Test**:
- ✅ API 호출 + 상태 업데이트
- ✅ 데이터베이스 CRUD
- ✅ 컴포넌트 간 데이터 흐름
- ✅ 이벤트 핸들링 → 상태 변경
- ✅ 외부 서비스 통합 (Mock 서버)

**Example**:
```typescript
// ✅ 좋은 Integration Test 예시
describe('Stats + Actions Integration', () => {
  it('should increase hunger when Feed button clicked', async () => {
    render(<LOBI_APP />)

    const feedButton = screen.getByText(/FEED UNIT/i)
    const initialHunger = 50

    fireEvent.click(feedButton)

    await waitFor(() => {
      // handleAction → setStats → UI 업데이트 통합 확인
      expect(screen.getByText(/65%/i)).toBeInTheDocument()
    })
  })
})
```

---

### 3. End-to-End Tests (10% 권장)

**Target**: 전체 사용자 여정

**Characteristics**:
- **느림**: 초~분 단위
- **실제 환경**: 브라우저, 데이터베이스, 네트워크
- **최소한**: 수개~수십 개
- **크리티컬 패스**: 핵심 비즈니스 시나리오만

**What to Test**:
- ✅ 회원가입 → 로그인 → 주요 기능 사용 플로우
- ✅ 결제 프로세스 (중요 매출 경로)
- ✅ 데이터 생성 → 조회 → 수정 → 삭제 플로우
- ✅ 크로스 브라우저 호환성 (선택적)

**What NOT to Test**:
- ❌ 모든 엣지 케이스 (Unit에서 처리)
- ❌ 단순 UI 렌더링
- ❌ 에러 메시지 문구

**Example**:
```typescript
// ✅ 좋은 E2E Test 예시
test('complete user interaction flow', async ({ page }) => {
  await page.goto('http://localhost:3000')

  // Feed 버튼 클릭 → 스탯 증가
  await page.click('button:has-text("FEED UNIT")')
  await expect(page.locator('text=/Hunger.*65%/')).toBeVisible()

  // 채팅 메시지 전송 → 봇 응답
  await page.fill('input[placeholder*="Lobi"]', '안녕')
  await page.press('input[placeholder*="Lobi"]', 'Enter')
  await expect(page.locator('.bg-white\\/5').last()).toBeVisible({ timeout: 10000 })

  // 3D 캐릭터 클릭 → 상태 변경
  await page.click('.spline-container')
})
```

---

## Coverage Goals

| Layer | Coverage Target | Priority |
|-------|----------------|----------|
| **비즈니스 로직** | ≥90% | 최상 |
| **API/Controller** | ≥70% | 상 |
| **UI Components** | Integration으로 대체 | 중 |
| **Utilities** | ≥90% | 상 |
| **Config/Constants** | 불필요 | 낮음 |

---

## Test Patterns

### AAA Pattern (Arrange-Act-Assert)

```typescript
test('should update stats when action triggered', () => {
  // Arrange: 준비
  const initialStats = { hunger: 50, energy: 50, happiness: 50 }
  const { result } = renderHook(() => useStats(initialStats))

  // Act: 실행
  act(() => {
    result.current.handleAction('hunger')
  })

  // Assert: 검증
  expect(result.current.stats.hunger).toBe(65) // +15
  expect(result.current.stats.happiness).toBe(55) // +5
})
```

### Given-When-Then (BDD Style)

```typescript
test('user receives bot response after sending message', async () => {
  // Given: 사용자가 채팅 화면에 있고
  render(<ChatInterface />)

  // When: 메시지를 보내면
  fireEvent.change(screen.getByPlaceholderText(/메시지/), {
    target: { value: '안녕' }
  })
  fireEvent.click(screen.getByRole('button', { name: /send/i }))

  // Then: 봇 응답이 표시된다
  await waitFor(() => {
    expect(screen.getByText(/안녕하세요/i)).toBeInTheDocument()
  })
})
```

### Mocking External Dependencies

```typescript
// ✅ 좋은 Mocking 예시
import { vi } from 'vitest'
import { GoogleGenAI } from '@google/genai'

vi.mock('@google/genai', () => ({
  GoogleGenAI: vi.fn(() => ({
    models: {
      generateContent: vi.fn(async () => ({
        text: '안녕하세요! 반갑습니다.'
      }))
    }
  }))
}))

test('should call Gemini API and display response', async () => {
  render(<ChatInterface />)

  fireEvent.click(screen.getByText(/send/i))

  await waitFor(() => {
    expect(screen.getByText(/안녕하세요/i)).toBeInTheDocument()
  })
})
```

---

## Anti-Patterns (하지 말 것)

### ❌ Test Inverted Pyramid

```
잘못된 비율:
       /Unit\         10% ← 너무 적음
      /─────\
     /Integ.\        20%
    /────────\
   /   E2E    \      70% ← 너무 많음 (느림)
  /────────────\
```

**문제**:
- E2E 테스트가 느려서 CI 시간 10분+
- Flaky Test 발생 (네트워크, 타임아웃)
- 디버깅 어려움 (어디서 깨졌는지 불명확)

**해결**:
- E2E를 Unit으로 대체
- 핵심 시나리오만 E2E
- Mock을 활용한 Integration Test

---

### ❌ Testing Implementation Details

```typescript
// ❌ 나쁜 예: Private 메서드 테스트
test('should call _internalCalculation', () => {
  const component = new MyComponent()
  expect(component._internalCalculation(5)).toBe(10)
})

// ✅ 좋은 예: Public API 테스트
test('should calculate result correctly', () => {
  const component = new MyComponent()
  expect(component.calculate(5)).toBe(10)
})
```

---

### ❌ Over-Mocking

```typescript
// ❌ 나쁜 예: 모든 것을 Mock
test('should update stats', () => {
  const mockSetStats = vi.fn()
  const mockGetStats = vi.fn()
  const mockValidate = vi.fn()
  // ... 실제 로직이 하나도 테스트 안 됨
})

// ✅ 좋은 예: 외부 의존성만 Mock
test('should update stats', () => {
  const mockApiCall = vi.fn() // 외부 API만 Mock
  // 나머지는 실제 로직 실행
})
```

---

### ❌ Brittle Tests

```typescript
// ❌ 나쁜 예: 구현 세부사항에 의존
expect(component.state.counter).toBe(5) // 내부 상태 직접 접근

// ✅ 좋은 예: 사용자가 보는 것 검증
expect(screen.getByText('Count: 5')).toBeInTheDocument()
```

---

## Test Organization

### Folder Structure

```
__tests__/
├── unit/                   # 단위 테스트
│   ├── utils/
│   │   ├── statsUtils.test.ts
│   │   └── validation.test.ts
│   ├── hooks/
│   │   ├── useStats.test.ts
│   │   └── useChat.test.ts
│   └── services/
│       └── geminiService.test.ts
│
├── integration/            # 통합 테스트
│   ├── stats-system.test.ts
│   ├── chat-flow.test.ts
│   └── api-integration.test.ts
│
└── e2e/                    # E2E 테스트
    ├── complete-user-flow.spec.ts
    └── critical-paths.spec.ts
```

### Naming Convention

```
{feature/module}.test.ts      # Unit/Integration
{user-flow}.spec.ts           # E2E

Examples:
- getBarColor.test.ts
- stats-system.test.ts
- complete-user-flow.spec.ts
```

---

## Frameworks & Tools

### GENKUB 프로젝트 권장 스택

| Type | Tool | Purpose |
|------|------|---------|
| **Unit** | Vitest | 빠른 실행, Vite 통합 |
| **Integration** | @testing-library/react | React 컴포넌트 테스트 |
| **E2E** | Playwright | 크로스 브라우저, 안정적 |
| **Mocking** | Vitest (내장) | API, 모듈 Mocking |
| **Coverage** | Vitest (내장) | 커버리지 리포트 |

### Setup Commands

```bash
# Vitest + React Testing Library
npm install -D vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event

# Playwright (E2E)
npm install -D @playwright/test
npx playwright install

# Run Tests
npm test                     # Unit + Integration
npm test -- --coverage       # with coverage
npx playwright test          # E2E
```

---

## Best Practices

### ✅ 1. Test Behavior, Not Implementation

```typescript
// ❌ 나쁜 예
test('should call setStats with correct parameters', () => {
  const mockSetStats = vi.fn()
  // setStats 호출 여부 확인
})

// ✅ 좋은 예
test('should increase hunger by 15 when Feed clicked', () => {
  // 결과적 동작 확인
  expect(result.current.stats.hunger).toBe(65)
})
```

### ✅ 2. Keep Tests Fast

- Unit: < 100ms
- Integration: < 1s
- E2E: < 30s

### ✅ 3. Independent Tests

```typescript
// ✅ 각 테스트는 독립적
beforeEach(() => {
  // 초기화
})

test('test 1', () => {
  // test 2에 영향 없음
})

test('test 2', () => {
  // test 1에 영향 없음
})
```

### ✅ 4. Clear Test Names

```typescript
// ❌ 불명확
test('it works', () => {})

// ✅ 명확
test('should return red color for values below 30', () => {})
```

### ✅ 5. One Assertion per Concept

```typescript
// ✅ 좋은 예: 한 개념당 하나의 테스트
test('should increase hunger when Feed clicked', () => {
  expect(stats.hunger).toBe(65)
})

test('should increase happiness when Feed clicked', () => {
  expect(stats.happiness).toBe(55)
})
```

---

## Coverage Commands

```bash
# Vitest
npm test -- --coverage
open coverage/index.html

# Coverage Thresholds (vitest.config.ts)
export default defineConfig({
  test: {
    coverage: {
      lines: 80,
      functions: 80,
      branches: 80,
      statements: 80
    }
  }
})
```

---

## TDD Workflow

```
1. 🔴 RED: 실패하는 테스트 작성
   test('should increase hunger by 15', () => {
     expect(stats.hunger).toBe(65) // Fails
   })

2. 🟢 GREEN: 최소 코드로 통과
   const handleAction = (type) => {
     setStats(prev => ({ ...prev, hunger: prev.hunger + 15 }))
   }

3. 🔵 REFACTOR: 코드 개선
   const handleAction = (type: StatType) => {
     setStats(prev => ({
       ...prev,
       [type]: Math.min(100, prev[type] + STAT_INCREMENT[type]),
       happiness: Math.min(100, prev.happiness + 5)
     }))
   }
```

---

**Skill Version**: 1.0.0
**Last Updated**: 2025-12-30
**Next Review**: 테스트 인프라 구축 후
