# Playwright MCP Setup Guide

**Version**: 1.0
**Date**: 2025-12-30
**Status**: ✅ Recommended for E2E Testing & Workflow Automation

---

## Overview

**Playwright MCP**는 Claude Code가 Playwright를 사용하여 **자동화된 브라우저 테스팅**과 **워크플로우 자동화**를 수행할 수 있게 해주는 MCP 서버입니다.

---

## Why Playwright MCP?

LobAI 프로젝트에서 Playwright MCP를 사용하면:

✅ **E2E 테스트 자동화**: 회원가입 → 로그인 → 메시지 전송 플로우 자동 검증
✅ **UI 테스트**: 버튼 클릭, 입력 필드 검증, 스크린샷 비교
✅ **크로스 브라우저 테스트**: Chrome, Firefox, Safari 동시 테스트
✅ **시각적 회귀 테스트**: UI 변경 감지
✅ **성능 측정**: 페이지 로드 시간, API 응답 시간
✅ **워크플로우 자동화**: 반복 작업 자동화 (폼 작성, 데이터 수집 등)

---

## Prerequisites

### 1. Node.js (18+)

```bash
node --version
# v18.x.x 이상
```

### 2. Playwright 설치

```bash
npm install -D @playwright/test
npx playwright install
```

---

## Installation

### Step 1: Claude Code 설정 파일 찾기

**설정 파일 위치**:
- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows**: `%APPDATA%/Claude/claude_desktop_config.json`
- **Linux**: `~/.config/Claude/claude_desktop_config.json`

---

### Step 2: Playwright MCP 설정 추가

`claude_desktop_config.json` 파일을 열어서 Playwright MCP를 추가합니다.

**기존 MCP 서버가 있는 경우**:
```json
{
  "mcpServers": {
    "context7": {
      "command": "npx",
      "args": ["-y", "@context7/mcp-server"]
    },
    "mysql": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-mysql"],
      "env": {
        "MYSQL_HOST": "localhost",
        "MYSQL_PORT": "3306",
        "MYSQL_USER": "lobai_user",
        "MYSQL_PASSWORD": "${MYSQL_PASSWORD}",
        "MYSQL_DATABASE": "lobai_db"
      }
    },
    "playwright": {
      "command": "npx",
      "args": ["-y", "@playwright/mcp-server"],
      "env": {
        "PLAYWRIGHT_HEADLESS": "false",
        "PLAYWRIGHT_BROWSER": "chromium"
      }
    }
  }
}
```

**환경 변수 설명**:
- `PLAYWRIGHT_HEADLESS`: `"false"`면 브라우저가 보임, `"true"`면 백그라운드 실행
- `PLAYWRIGHT_BROWSER`: `"chromium"`, `"firefox"`, `"webkit"` (기본값: chromium)

---

### Step 3: Claude Code 재시작

설정 파일 수정 후 Claude Code를 완전히 종료하고 다시 실행합니다.

```bash
# macOS에서 완전 종료
killall Claude\ Code

# 재시작
open -a "Claude Code"
```

---

### Step 4: 동작 확인

Claude Code에서 다음 쿼리로 Playwright MCP가 정상 작동하는지 확인합니다.

**테스트 쿼리**:
```
"Playwright로 Google 홈페이지 열어줘"
```

**기대 결과**:
- Chrome 브라우저가 열림
- https://google.com 페이지 로드
- 스크린샷 또는 페이지 정보 반환

**추가 테스트**:
```
"localhost:3000 페이지 스크린샷 찍어줘"
"LobAI 로컬 서버에서 Login 버튼 클릭해줘"
```

---

## Usage Examples

### Example 1: E2E Test - 회원가입 플로우

```
User: "Playwright로 회원가입 플로우 테스트해줘"

Playwright MCP:
1. http://localhost:3000 접속
2. "Login" 버튼 클릭
3. 이메일 입력: test@example.com
4. 비밀번호 입력: password123
5. "회원가입" 버튼 클릭
6. 환영 메시지 확인: "안녕하세요! 저는 당신의 AI 동반자 Lobi입니다."

Output:
✅ 회원가입 성공
✅ 환영 메시지 표시됨
Screenshot: /tmp/signup-success.png
```

---

### Example 2: UI Element 검증

```
User: "Stats 패널이 제대로 표시되는지 확인해줘"

Playwright MCP:
1. http://localhost:3000 접속
2. ".glass" 클래스 요소 찾기
3. "HUNGER", "ENERGY", "HAPPINESS" 텍스트 존재 확인
4. 진행바 3개 확인
5. 버튼 3개 확인 (FEED UNIT, INITIATE PLAY, SLEEP MODE)

Output:
✅ Stats 패널 정상 표시
✅ 모든 UI 요소 존재
Screenshot: /tmp/stats-panel.png
```

---

### Example 3: 메시지 전송 플로우

```
User: "채팅 메시지 전송 테스트해줘"

Playwright MCP:
1. http://localhost:3000 접속
2. 채팅 입력 필드 찾기: input[placeholder*="Lobi"]
3. 메시지 입력: "안녕하세요"
4. Enter 키 또는 전송 버튼 클릭
5. 봇 응답 대기 (최대 10초)
6. 응답 메시지 확인

Output:
✅ 사용자 메시지 전송됨
✅ 봇 응답 수신됨: "안녕하세요! 반갑습니다..."
Screenshot: /tmp/chat-message.png
```

---

### Example 4: 크로스 브라우저 테스트

```
User: "Chrome, Firefox, Safari에서 모두 테스트해줘"

Playwright MCP:
for browser in [chromium, firefox, webkit]:
  1. 브라우저 실행
  2. http://localhost:3000 접속
  3. 페이지 로드 확인
  4. Stats 패널 표시 확인
  5. 스크린샷 저장

Output:
✅ Chrome: 정상
✅ Firefox: 정상
✅ Safari: 정상
Screenshots: /tmp/chrome.png, /tmp/firefox.png, /tmp/safari.png
```

---

### Example 5: 성능 측정

```
User: "페이지 로드 성능 측정해줘"

Playwright MCP:
const startTime = Date.now();
await page.goto('http://localhost:3000');
await page.waitForLoadState('networkidle');
const loadTime = Date.now() - startTime;

Output:
⏱️ 페이지 로드 시간: 1.2초
⏱️ DOMContentLoaded: 0.8초
⏱️ First Contentful Paint: 0.5초
```

---

### Example 6: 시각적 회귀 테스트

```
User: "현재 UI와 이전 스크린샷 비교해줘"

Playwright MCP:
1. 현재 페이지 스크린샷: /tmp/current.png
2. 이전 스크린샷 로드: /tmp/baseline.png
3. 픽셀 차이 계산

Output:
📊 시각적 변경: 5.2% (허용 범위: 1%)
⚠️ 경고: UI가 크게 변경되었습니다.
Diff 이미지: /tmp/diff.png
```

---

## Integration with Backend Testing

### Workflow: Frontend + Backend E2E Test

```
1. Backend Developer Agent로 User API 작성
   → POST /api/auth/register

2. Backend Testing Strategy로 Unit Test 작성
   → UserServiceTest.java

3. Playwright MCP로 E2E Test 자동화
   → 브라우저에서 회원가입 폼 작성
   → API 호출 확인 (Network 탭)
   → 성공 메시지 UI 확인

4. GitHub MCP로 테스트 결과 PR에 코멘트
   → "✅ E2E Test Passed"
```

---

## Advanced Features

### 1. Network Request Interception

```
User: "Gemini API 호출 시 응답 시간 측정해줘"

Playwright MCP:
page.on('request', request => {
  if (request.url().includes('gemini')) {
    console.log('Gemini API 호출:', request.method());
  }
});

page.on('response', response => {
  if (response.url().includes('gemini')) {
    console.log('응답 시간:', response.timing());
  }
});

Output:
🌐 Gemini API 호출: POST
⏱️ 응답 시간: 1.8초
📊 페이로드 크기: 2.3KB
```

---

### 2. Mobile Emulation

```
User: "iPhone에서 어떻게 보이는지 확인해줘"

Playwright MCP:
const iPhone = devices['iPhone 13'];
const context = await browser.newContext({ ...iPhone });
const page = await context.newPage();
await page.goto('http://localhost:3000');

Output:
📱 iPhone 13 뷰포트: 390x844
Screenshot: /tmp/iphone-view.png
```

---

### 3. Accessibility Testing

```
User: "접근성 문제 확인해줘"

Playwright MCP:
const accessibilityScanResults = await new AxeBuilder({ page })
  .analyze();

Output:
⚠️ 접근성 문제 발견: 3개
- ARIA 라벨 누락: 2개
- 색상 대비 부족: 1개
상세: /tmp/accessibility-report.html
```

---

## Troubleshooting

### Issue 1: Playwright MCP Not Starting

**Symptoms**:
- "Playwright MCP가 응답하지 않음" 에러

**Solutions**:

1. **Playwright 설치 확인**:
   ```bash
   npx playwright --version
   # Version 1.40.0 이상
   ```

2. **브라우저 바이너리 설치**:
   ```bash
   npx playwright install
   npx playwright install-deps
   ```

3. **설정 파일 JSON 검증**:
   ```bash
   cat ~/Library/Application\ Support/Claude/claude_desktop_config.json | python -m json.tool
   ```

4. **Claude Code 재시작**

---

### Issue 2: Headless Mode 문제

**Symptoms**:
- 브라우저가 열리지 않거나 스크린샷이 검은 화면

**Solutions**:

1. **Headless 모드 비활성화**:
   ```json
   "env": {
     "PLAYWRIGHT_HEADLESS": "false"
   }
   ```

2. **Display 설정 (Linux)**:
   ```bash
   export DISPLAY=:0
   ```

---

### Issue 3: Timeout Errors

**Symptoms**:
- "Timeout 30000ms exceeded" 에러

**Solutions**:

1. **타임아웃 증가**:
   ```json
   "env": {
     "PLAYWRIGHT_TIMEOUT": "60000"
   }
   ```

2. **네트워크 상태 대기**:
   ```
   "페이지 로드 완료 후 실행해줘"
   ```

---

### Issue 4: localhost 연결 실패

**Symptoms**:
- "net::ERR_CONNECTION_REFUSED" 에러

**Solutions**:

1. **개발 서버 실행 확인**:
   ```bash
   npm run dev
   # ✔ Local: http://localhost:3000
   ```

2. **포트 변경 시 URL 업데이트**:
   ```
   "http://localhost:5173 페이지 열어줘"
   ```

---

## Best Practices

### 1. Page Object Pattern

```
User: "Login 페이지 객체 패턴으로 테스트 작성해줘"

Output:
class LoginPage {
  constructor(page) {
    this.page = page;
    this.emailInput = page.locator('input[type="email"]');
    this.passwordInput = page.locator('input[type="password"]');
    this.loginButton = page.locator('button:has-text("로그인")');
  }

  async login(email, password) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.loginButton.click();
  }
}

// 사용
const loginPage = new LoginPage(page);
await loginPage.login('test@example.com', 'password123');
```

---

### 2. Test Data Fixtures

```
User: "테스트 데이터 픽스처 만들어줘"

Output:
// fixtures/users.json
{
  "validUser": {
    "email": "test@example.com",
    "password": "SecurePass123!",
    "username": "테스트유저"
  },
  "invalidUser": {
    "email": "invalid",
    "password": "weak"
  }
}

// 사용
const { validUser } = require('./fixtures/users.json');
await page.fill('input[type="email"]', validUser.email);
```

---

### 3. Retry Logic

```
User: "불안정한 테스트에 재시도 로직 추가해줘"

Output:
test('채팅 메시지 전송', async ({ page }) => {
  await page.goto('http://localhost:3000');

  // 최대 3번 재시도
  await test.step('메시지 전송', async () => {
    await expect(async () => {
      await page.fill('input', '안녕하세요');
      await page.press('input', 'Enter');
      await expect(page.locator('.bg-white\\/5').last()).toBeVisible({ timeout: 10000 });
    }).toPass({ intervals: [1000, 2000, 5000] });
  });
});
```

---

## Integration with GitHub MCP

### Automated Testing Workflow

```
1. 코드 변경 후 Playwright E2E 테스트 실행
   "Playwright로 전체 플로우 테스트해줘"

2. 테스트 결과 스크린샷 저장
   Screenshots: /tmp/test-results/*.png

3. GitHub MCP로 PR 생성
   "GitHub MCP로 PR 생성해줘"
   Title: "Add user authentication"
   Body: "E2E Test Passed ✅"
   Attachments: 테스트 스크린샷

4. CI/CD 트리거
   GitHub Actions가 Playwright 테스트 재실행
```

---

## Performance Tips

### 1. Parallel Execution

```
User: "여러 테스트 동시 실행해줘"

Playwright MCP:
test.describe.configure({ mode: 'parallel' });

test('테스트 1', async ({ page }) => { ... });
test('테스트 2', async ({ page }) => { ... });
test('테스트 3', async ({ page }) => { ... });

Output:
⚡ 3개 테스트 병렬 실행: 5초 완료 (순차: 15초)
```

---

### 2. Reuse Browser Context

```
User: "브라우저 컨텍스트 재사용해줘"

Playwright MCP:
const context = await browser.newContext({ storageState: 'auth.json' });
// 로그인 상태 유지됨

Output:
🚀 로그인 단계 생략: 2초 절약
```

---

## Test Organization (Recommended)

```
/Users/jimin/lobai/lobai/
├── e2e/
│   ├── auth/
│   │   ├── login.spec.ts
│   │   └── register.spec.ts
│   ├── chat/
│   │   ├── send-message.spec.ts
│   │   └── persona-switch.spec.ts
│   ├── stats/
│   │   ├── feed-action.spec.ts
│   │   └── stats-decay.spec.ts
│   └── fixtures/
│       ├── users.json
│       └── messages.json
├── playwright.config.ts
└── package.json
```

---

## playwright.config.ts Example

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
    {
      name: 'Mobile Safari',
      use: { ...devices['iPhone 13'] },
    },
  ],

  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
  },
});
```

---

## Security Considerations

### 1. Sensitive Data

```bash
# .gitignore에 추가
e2e/fixtures/secrets.json
playwright/.auth/
test-results/
playwright-report/
```

---

### 2. Screenshot Privacy

```
User: "스크린샷에서 민감정보 마스킹해줘"

Playwright MCP:
await page.locator('input[type="password"]').fill('***masked***');
await page.screenshot({ path: 'screenshot.png', mask: [page.locator('.sensitive')] });
```

---

## Related Documentation

- **Backend Test Strategy**: `.claude/skills/backend-test-strategy.md`
- **GitHub MCP**: `.claude/mcp-configs/github-setup.md`
- **Workflow Automation**: `docs/workflows/AUTOMATED_WORKFLOW.md`

---

## Next Steps

1. **설치 완료 후**:
   - "Playwright로 localhost:3000 페이지 열어줘" 테스트
   - E2E 테스트 작성

2. **GitHub MCP와 통합**:
   - 테스트 결과를 PR에 자동 코멘트
   - 스크린샷을 Issue에 첨부

3. **CI/CD 통합**:
   - GitHub Actions에 Playwright 테스트 추가
   - 자동 시각적 회귀 테스트

---

**Last Updated**: 2025-12-30
**Status**: Production Ready ✅
**Recommended**: ⭐⭐⭐⭐⭐ (E2E 테스트 + 워크플로우 자동화 필수)
