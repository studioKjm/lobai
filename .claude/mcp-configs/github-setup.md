# GitHub MCP Setup Guide

**Version**: 1.0
**Date**: 2025-12-30
**Status**: ✅ Recommended for PR/Issue Automation

---

## Overview

**GitHub MCP**는 Claude Code가 GitHub API와 직접 통신하여 **PR 자동 생성**, **Issue 관리**, **코멘트 작성**, **Commit 분석** 등을 수행할 수 있게 해주는 MCP 서버입니다.

---

## Why GitHub MCP?

LobAI 프로젝트에서 GitHub MCP를 사용하면:

✅ **PR 자동 생성**: 코드 작성 후 PR 자동 생성 (제목, 본문, 라벨)
✅ **PR 리뷰 자동화**: 코드 변경 분석 후 리뷰 코멘트 자동 작성
✅ **Issue 관리**: Issue 조회, 생성, 코멘트, 상태 변경
✅ **Commit 분석**: 최근 커밋 히스토리 분석, 변경 사항 요약
✅ **브랜치 관리**: 브랜치 생성, 삭제, 비교
✅ **워크플로우 자동화**: 테스트 결과 PR에 자동 코멘트

---

## Prerequisites

### 1. GitHub 계정

GitHub 계정이 있어야 합니다.

**Repository 확인**:
```bash
git remote -v
# origin  https://github.com/your-username/lobai.git (fetch)
# origin  https://github.com/your-username/lobai.git (push)
```

---

### 2. GitHub Personal Access Token 생성

**Step 1**: GitHub Settings로 이동
1. https://github.com/settings/tokens 접속
2. "Generate new token" → "Generate new token (classic)" 클릭

**Step 2**: Token 권한 설정

필수 권한:
- [x] `repo` (Full control of private repositories)
  - [x] `repo:status`
  - [x] `repo_deployment`
  - [x] `public_repo`
  - [x] `repo:invite`
  - [x] `security_events`
- [x] `read:org` (Read org and team membership)
- [x] `workflow` (Update GitHub Action workflows)

선택 권한 (팀 협업 시):
- [ ] `write:discussion`
- [ ] `admin:repo_hook`

**Step 3**: Token 생성 및 복사

1. "Generate token" 클릭
2. 생성된 토큰 복사 (예: `ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`)
3. ⚠️ **중요**: 이 토큰은 다시 볼 수 없으므로 안전한 곳에 저장

---

### 3. 환경 변수 설정

**macOS/Linux**:
```bash
# ~/.zshrc 또는 ~/.bashrc에 추가
export GITHUB_TOKEN="ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

# 적용
source ~/.zshrc
```

**Windows**:
```cmd
setx GITHUB_TOKEN "ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
```

**확인**:
```bash
echo $GITHUB_TOKEN
# ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

---

## Installation

### Step 1: Claude Code 설정 파일 찾기

**설정 파일 위치**:
- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows**: `%APPDATA%/Claude/claude_desktop_config.json`
- **Linux**: `~/.config/Claude/claude_desktop_config.json`

---

### Step 2: GitHub MCP 설정 추가

`claude_desktop_config.json` 파일을 열어서 GitHub MCP를 추가합니다.

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
        "MYSQL_USER": "lobai_user",
        "MYSQL_PASSWORD": "${MYSQL_PASSWORD}",
        "MYSQL_DATABASE": "lobai_db"
      }
    },
    "playwright": {
      "command": "npx",
      "args": ["-y", "@playwright/mcp-server"]
    },
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_TOKEN": "${GITHUB_TOKEN}"
      }
    }
  }
}
```

**⚠️ 보안 주의**:
- 토큰을 직접 쓰지 말고 환경 변수 사용 필수
- `.gitignore`에 설정 파일 추가

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

Claude Code에서 다음 쿼리로 GitHub MCP가 정상 작동하는지 확인합니다.

**테스트 쿼리**:
```
"GitHub에서 최근 커밋 5개 보여줘"
```

**기대 결과**:
```
최근 커밋 5개:
1. aac80f3 - Initial project setup (2025-12-28)
2. 455e5a2 - AI Development Infrastructure 구축 완료 (2025-12-30)
...
```

**추가 테스트**:
```
"현재 브랜치 확인해줘"
"Open된 PR 목록 보여줘"
```

---

## Usage Examples

### Example 1: PR 자동 생성

```
User: "GitHub MCP로 PR 생성해줘"

GitHub MCP:
1. 현재 브랜치 확인: feature/backend-setup
2. 변경 사항 분석: git diff master...HEAD
3. PR 제목 생성: "Add Spring Boot backend infrastructure"
4. PR 본문 작성:
   - Summary (3 bullet points)
   - Test plan
   - Screenshots (if any)
5. gh pr create 실행

Output:
✅ PR 생성 완료
🔗 https://github.com/your-username/lobai/pull/1
```

**생성된 PR 본문 예시**:
````markdown
## Summary

- Add Spring Boot backend with MySQL database
- Implement JWT authentication system
- Create User, Message, Persona entities

## Test plan

- [ ] Unit tests pass (mvn test)
- [ ] Integration tests pass
- [ ] API endpoints accessible (Postman)
- [ ] Database schema created

🤖 Generated with [Claude Code](https://claude.com/claude-code)
````

---

### Example 2: PR 리뷰 자동화

```
User: "PR #5 리뷰해줘"

GitHub MCP:
1. PR #5 정보 조회: gh pr view 5
2. 변경된 파일 목록: gh pr diff 5
3. Code Review Checklist 스킬 적용
4. Security Agent로 보안 검증
5. 리뷰 코멘트 작성: gh pr review 5 --comment

Output:
📝 리뷰 코멘트 작성 완료:

**기능 ✅**
- 요구사항 충족
- 엣지 케이스 처리됨

**보안 ⚠️**
- API Key 노출 발견 (index.tsx:208)
- 환경 변수로 이동 권장

**테스트 ❌**
- 단위 테스트 누락
- 커버리지: 45% (목표: 80%)

**추천**: Changes requested
```

---

### Example 3: Issue 관리

```
User: "Issue #12 상세 내용 보여줘"

GitHub MCP:
gh issue view 12

Output:
#12: Stats 자동 감소 로직 구현
Author: @user
Status: Open
Labels: enhancement, backend
Created: 2025-12-29

Description:
Stats (hunger, energy, happiness)를 5초마다 자동 감소시키는 백엔드 로직 추가
- Spring @Scheduled 사용
- 모든 활성 사용자에 대해 실행
- user_stats_history에 기록

Comments: 3
```

**Issue에 코멘트 추가**:
```
User: "Issue #12에 진행 상황 코멘트해줘"

GitHub MCP:
gh issue comment 12 --body "Stats 자동 감소 로직 구현 완료 ✅
- StatsScheduler.java 작성
- @Scheduled(fixedRate = 5000) 설정
- Unit Test 작성 완료
- 다음: Integration Test"

Output:
✅ 코멘트 작성 완료
```

---

### Example 4: Commit 분석

```
User: "최근 10개 커밋 분석해서 변경 사항 요약해줘"

GitHub MCP:
1. gh log --oneline -10
2. 각 커밋 메시지 분석
3. 카테고리별 그룹핑 (Feature, Fix, Refactor, Docs)
4. 요약 생성

Output:
📊 최근 10개 커밋 분석:

**Features (5)**:
- aac80f3: Initial project setup
- 455e5a2: AI Development Infrastructure
- e7f3b1a: Backend Developer Agent 추가
- 9a2c4d3: MySQL MCP 설정
- 1b5e8f9: Playwright MCP 설정

**Refactor (3)**:
- 3c7d9a2: 채팅 스크롤 버그 수정
- 8e1f4b3: Stats 컴포넌트 분리
- 2d9a5c1: API 호출 로직 개선

**Docs (2)**:
- 6f3e2a4: TECHNICAL_GUIDE.md 업데이트
- 4a8d1c5: README 업데이트
```

---

### Example 5: 브랜치 비교

```
User: "master와 feature/backend-setup 브랜치 비교해줘"

GitHub MCP:
gh compare master...feature/backend-setup

Output:
📊 브랜치 비교 (master → feature/backend-setup):

**변경된 파일**: 25개
**추가**: +1,250 lines
**삭제**: -120 lines

**주요 변경**:
- backend/ 디렉터리 추가 (15 files)
- docs/plans/ 업데이트 (3 files)
- .claude/agents/ 업데이트 (2 files)

**커밋**: 8개 ahead
```

---

### Example 6: PR에 테스트 결과 코멘트

```
User: "Playwright 테스트 결과를 PR #5에 코멘트해줘"

Workflow:
1. Playwright MCP로 E2E 테스트 실행
   → 테스트 결과: 15 passed, 2 failed

2. 스크린샷 저장
   → /tmp/test-results/

3. GitHub MCP로 PR에 코멘트
   gh pr comment 5 --body "..."

Output:
📊 E2E Test Results

**Passed**: 15 ✅
- Auth flow
- Message sending
- Stats update
- Persona switching
- ...

**Failed**: 2 ❌
- Chat scroll behavior (timeout)
- 3D character loading (intermittent)

**Screenshots**: [View Test Report](link)

**Recommendation**: ⚠️ Fix failing tests before merge
```

---

## Integration with Workflow Automation

### Automated Development Cycle

```
1. 코드 작성 (Backend Developer Agent)
   "Backend Developer Agent로 User API 작성"

2. 테스트 작성 (Test Engineer Agent)
   "Test Engineer로 UserServiceTest 작성"

3. 로컬 테스트 실행 (Bash)
   mvn test

4. E2E 테스트 (Playwright MCP)
   "Playwright로 회원가입 플로우 테스트"

5. Git Commit (Bash)
   git add . && git commit -m "feat: Add user registration API"

6. PR 생성 (GitHub MCP)
   "GitHub MCP로 PR 생성해줘"

7. 테스트 결과 PR에 코멘트 (GitHub MCP)
   "테스트 결과 PR에 코멘트해줘"

8. CI/CD 트리거 (GitHub Actions)
   자동으로 백엔드 테스트 및 배포
```

---

## Advanced Features

### 1. PR Template 사용

```
User: "PR 템플릿 사용해서 PR 생성해줘"

GitHub MCP:
# .github/pull_request_template.md 읽기
템플릿 채우기:
- [ ] Summary
- [ ] Breaking changes
- [ ] Test plan
- [ ] Screenshots

gh pr create --template

Output:
✅ PR 생성 완료 (템플릿 적용됨)
```

---

### 2. Label 자동 추가

```
User: "PR에 'backend', 'enhancement' 라벨 추가해줘"

GitHub MCP:
gh pr edit 5 --add-label "backend,enhancement"

Output:
✅ 라벨 추가 완료
🏷️ backend, enhancement
```

---

### 3. Milestone 설정

```
User: "PR을 'v1.0 Backend' 마일스톤에 추가해줘"

GitHub MCP:
gh pr edit 5 --milestone "v1.0 Backend"

Output:
✅ 마일스톤 설정 완료
🎯 v1.0 Backend (Due: 2026-01-31)
```

---

### 4. Draft PR 생성

```
User: "Draft PR로 생성해줘 (WIP)"

GitHub MCP:
gh pr create --draft --title "[WIP] Add backend infrastructure"

Output:
✅ Draft PR 생성 완료
📝 Review not requested (draft mode)
```

---

### 5. PR Merge

```
User: "PR #5 머지해줘 (squash)"

GitHub MCP:
gh pr merge 5 --squash --delete-branch

Output:
✅ PR 머지 완료 (squash and merge)
🗑️ 브랜치 삭제됨: feature/backend-setup
```

---

## Troubleshooting

### Issue 1: Authentication Failed

**Symptoms**:
- "HTTP 401: Bad credentials" 에러

**Solutions**:

1. **토큰 유효성 확인**:
   ```bash
   echo $GITHUB_TOKEN
   # ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   ```

2. **토큰 권한 확인**:
   - https://github.com/settings/tokens 접속
   - 토큰의 `repo`, `read:org` 권한 확인

3. **토큰 재생성**:
   - 기존 토큰 삭제
   - 새 토큰 생성 (권한 동일하게)
   - 환경 변수 업데이트

4. **Claude Code 재시작**

---

### Issue 2: Rate Limit Exceeded

**Symptoms**:
- "API rate limit exceeded" 에러

**Solutions**:

1. **Rate Limit 확인**:
   ```bash
   gh api rate_limit
   # resources.core.remaining: 4872/5000
   ```

2. **대기 시간 확인**:
   ```
   # Reset time: 2025-12-30 18:00:00
   ```

3. **인증된 요청 사용** (Rate Limit ⬆️):
   - 인증 없음: 60 requests/hour
   - 인증 있음: 5,000 requests/hour

---

### Issue 3: Repository Not Found

**Symptoms**:
- "Repository not found" 에러

**Solutions**:

1. **Repository 경로 확인**:
   ```bash
   git remote -v
   # origin이 올바른지 확인
   ```

2. **토큰 권한 확인**:
   - Private repo면 `repo` 권한 필요

3. **Repository 명시**:
   ```
   "your-username/lobai 저장소에서 PR 생성해줘"
   ```

---

### Issue 4: gh CLI Not Found

**Symptoms**:
- "gh command not found" 에러

**Solutions**:

1. **gh CLI 설치**:
   ```bash
   # macOS
   brew install gh

   # Windows
   winget install GitHub.cli

   # Linux
   sudo apt install gh
   ```

2. **인증**:
   ```bash
   gh auth login
   # 토큰 입력
   ```

---

## Best Practices

### 1. PR 제목 규칙

```
✅ Good:
feat: Add user authentication API
fix: Resolve chat scroll bug
docs: Update README with backend setup

❌ Bad:
Update code
Fix bug
Changes
```

---

### 2. PR 본문 구조

```markdown
## Summary
- 3-5 bullet points of changes

## Breaking Changes (if any)
- List breaking changes

## Test Plan
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing done

## Screenshots (if UI changes)
[Attach screenshots]

🤖 Generated with Claude Code
```

---

### 3. Commit 메시지 규칙

```bash
# Conventional Commits
feat: Add new feature
fix: Fix bug
docs: Update documentation
refactor: Refactor code
test: Add tests
chore: Update dependencies
```

---

## Security Considerations

### 1. Token 보안

```bash
# .gitignore에 추가
.env
.env.local
claude_desktop_config.json
github_token.txt
```

**환경 변수 사용**:
```json
{
  "env": {
    "GITHUB_TOKEN": "${GITHUB_TOKEN}"  // ✅
  }
}

// ❌ 절대 하지 말 것
{
  "env": {
    "GITHUB_TOKEN": "ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
  }
}
```

---

### 2. 최소 권한 원칙

필요한 권한만 부여:
- 읽기만: `read:org`, `public_repo`
- 쓰기 필요: `repo`, `workflow`

---

### 3. Token 갱신

토큰은 주기적으로 갱신:
- 권장: 3-6개월마다
- 유출 의심 시 즉시 폐기 후 재생성

---

## Related Documentation

- **Playwright MCP**: `.claude/mcp-configs/playwright-setup.md`
- **Workflow Automation**: `docs/workflows/AUTOMATED_WORKFLOW.md`
- **Code Review Checklist**: `.claude/skills/code-review-checklist.md`

---

## Next Steps

1. **설치 완료 후**:
   - "GitHub에서 최근 커밋 조회" 테스트
   - PR 자동 생성 테스트

2. **Playwright MCP와 통합**:
   - E2E 테스트 결과 PR에 자동 코멘트
   - 스크린샷 첨부

3. **워크플로우 자동화**:
   - 코드 작성 → 테스트 → PR 생성 → 리뷰 → 머지 전체 자동화

---

**Last Updated**: 2025-12-30
**Status**: Production Ready ✅
**Recommended**: ⭐⭐⭐⭐⭐ (PR/Issue 자동화 필수)
