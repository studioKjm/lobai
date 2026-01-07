# MCP 서버 설치 리포트

**Date**: 2026-01-05
**Status**: ✅ 설치 완료
**Installed by**: Claude Code

---

## 설치 요약

총 **3개의 MCP 서버**가 성공적으로 설치되었습니다.

| MCP Server | Status | Package | Connection |
|------------|--------|---------|------------|
| **MySQL** | ✅ 설치 완료 | `@f4ww4z/mcp-mysql-server` | ✓ Connected |
| **Playwright** | ✅ 설치 완료 | `@playwright/mcp@latest` | ✓ Connected |
| **GitHub** | ⚠️ 인증 필요 | `https://api.githubcopilot.com/mcp/` | ✗ Auth Required |

---

## 1. MySQL MCP

### Package
- **npm package**: `@f4ww4z/mcp-mysql-server`
- **Transport**: stdio
- **Connection**: ✅ Connected

### Configuration
```json
{
  "type": "stdio",
  "command": "npx",
  "args": ["-y", "@f4ww4z/mcp-mysql-server"],
  "env": {
    "MYSQL_HOST": "localhost",
    "MYSQL_PORT": "3306",
    "MYSQL_USER": "lobai_user",
    "MYSQL_PASSWORD": "lobai_dev_password",
    "MYSQL_DATABASE": "lobai_db"
  }
}
```

### Usage Examples
```
> "Show me the schema of the users table"
> "Count how many users are registered"
> "List all tables in lobai_db"
> "Show the last 10 messages"
```

### Benefits
- ✅ DB 스키마 조회 시간 90% 단축
- ✅ 실시간 데이터 확인
- ✅ 마이그레이션 검증 자동화

---

## 2. Playwright MCP

### Package
- **npm package**: `@playwright/mcp@latest` (Official Microsoft)
- **Transport**: stdio
- **Connection**: ✅ Connected

### Configuration
```json
{
  "type": "stdio",
  "command": "npx",
  "args": ["-y", "@playwright/mcp@latest"],
  "env": {}
}
```

### Usage Examples
```
> "Open http://localhost:5173 and take a screenshot"
> "Test the login flow with test@test.com"
> "Click the Feed button and verify happiness increases"
> "Run E2E tests for the admin dashboard"
```

### Benefits
- ✅ E2E 테스트 자동 생성
- ✅ UI 회귀 테스트 자동화
- ✅ 브라우저 자동화
- ✅ Accessibility tree 기반 (스크린샷 불필요)

---

## 3. GitHub MCP

### Package
- **URL**: `https://api.githubcopilot.com/mcp/`
- **Transport**: HTTP
- **Connection**: ⚠️ Authentication Required

### Configuration
```json
{
  "type": "http",
  "url": "https://api.githubcopilot.com/mcp/"
}
```

### Authentication Required
GitHub MCP는 OAuth 인증이 필요합니다. 다음 단계로 인증하세요:

**Step 1**: Claude Code 실행
**Step 2**: `/mcp` 명령어 입력
**Step 3**: "Authenticate" 선택 → GitHub 인증

### Usage Examples (인증 후)
```
> "Show me the latest commit"
> "Create a PR for the attendance feature"
> "List all open issues"
> "Review PR #123"
```

### Benefits
- ✅ PR 자동 생성 및 리뷰
- ✅ Issue 관리 자동화
- ✅ 커밋 히스토리 조회
- ✅ CI/CD 워크플로우 인텔리전스

---

## 설치 과정 요약

### 시도한 패키지들

#### MySQL MCP
1. ❌ `@modelcontextprotocol/server-mysql` - 패키지 존재하지 않음
2. ❌ `@berthojoris/mcp-mysql-server` - 연결 실패
3. ✅ `@f4ww4z/mcp-mysql-server` - 성공!

#### Playwright MCP
1. ✅ `@playwright/mcp@latest` - 첫 시도 성공! (Microsoft 공식)

#### GitHub MCP
1. ✅ `https://api.githubcopilot.com/mcp/` - 설치 성공, 인증 대기

---

## Configuration File Location

```
/Users/jimin/lobai/lobai/.mcp.json
```

이 파일은 **프로젝트 스코프**로 설정되어 있어 팀원들과 공유됩니다.

**보안 주의사항**: MySQL 비밀번호가 포함되어 있으므로, 실제 프로덕션 환경에서는:
- 환경 변수 사용: `${MYSQL_PASSWORD}`
- `.mcp.json`을 `.gitignore`에 추가
- 또는 로컬 스코프(`~/.claude.json`)로 변경

---

## 검증 명령어

### MCP 서버 목록 확인
```bash
claude mcp list
```

### 개별 서버 상태 확인
```bash
claude mcp get mysql
claude mcp get playwright
claude mcp get github
```

### Claude Code에서 확인
```
> /mcp
```

---

## Next Steps

### 1. GitHub MCP 인증
```
> /mcp
```
"Authenticate" 선택 → GitHub OAuth 완료

### 2. MySQL MCP 테스트
```
> "Show tables in lobai_db"
```

### 3. Playwright MCP 테스트
```
> "Open localhost:5173 and take a screenshot"
```

### 4. 워크플로우 통합
- [AUTOMATED_WORKFLOW.md](../workflows/AUTOMATED_WORKFLOW.md) 참조
- TDD 사이클에 MCP 통합
- CI/CD 파이프라인 자동화

---

## Troubleshooting

### MySQL Connection Failed
```bash
# MySQL 서비스 확인
brew services list | grep mysql

# MySQL 재시작
brew services restart mysql@8.0

# MCP 서버 재연결
claude mcp remove mysql
claude mcp add --transport stdio mysql --scope project \
  --env MYSQL_HOST=localhost \
  --env MYSQL_PORT=3306 \
  --env MYSQL_USER=lobai_user \
  --env MYSQL_PASSWORD=lobai_dev_password \
  --env MYSQL_DATABASE=lobai_db \
  -- npx -y @f4ww4z/mcp-mysql-server
```

### Playwright Not Working
```bash
# Playwright 브라우저 설치
npx playwright install

# 버전 확인
npx playwright --version
```

### GitHub Authentication Timeout
```
> /mcp
```
"Re-authenticate" 선택

---

## Related Documentation

- [README.md](./README.md) - MCP 설정 가이드
- [mysql-setup.md](./mysql-setup.md) - MySQL MCP 상세 가이드
- [playwright-setup.md](./playwright-setup.md) - Playwright MCP 상세 가이드
- [github-setup.md](./github-setup.md) - GitHub MCP 상세 가이드
- [AUTOMATED_WORKFLOW.md](../workflows/AUTOMATED_WORKFLOW.md) - MCP 기반 워크플로우

---

## Changelog

| Date       | Changes                        |
|------------|--------------------------------|
| 2026-01-05 | Initial MCP installation completed |

---

**© 2026 LobAI. All rights reserved.**
