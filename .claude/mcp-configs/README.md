# MCP Configs Directory

**Purpose**: Model Context Protocol 서버 설정 및 사용 가이드

---

## Overview

MCP (Model Context Protocol)는 **Claude Code가 외부 시스템과 통신**할 수 있게 하는 표준 프로토콜입니다. 이 디렉터리는 프로젝트에서 사용하는 MCP 서버의 설정 파일과 가이드를 포함합니다.

---

## What is MCP?

MCP를 사용하면 Claude Code가 다음과 같은 작업을 수행할 수 있습니다:

- **문서 인덱싱**: 프로젝트 문서를 자동으로 검색 (Context7)
- **데이터베이스 쿼리**: SQL 실행 및 스키마 확인 (Postgres MCP)
- **API 호출**: GitHub, Slack 등 외부 서비스 연동
- **파일 시스템**: 로컬 파일 읽기/쓰기 (기본 제공)

---

## Available MCP Servers

### 1. Context7 (Recommended 🔥)

**Purpose**: 프로젝트 문서 자동 인덱싱 및 검색

**Status**: ✅ 설치 권장

**What it does**:
- `.md` 파일 자동 인덱싱
- 자연어 문서 쿼리 지원
- 코드 컨텍스트 유지

**Use Cases**:
```
"TECHNICAL_GUIDE.md에서 테스트 전략 알려줘"
"guideline.md에 서브에이전트 설명 있어?"
"ADR 문서에서 컴포넌트 분리 결정 이유 찾아줘"
```

**Setup Guide**: [context7-setup.md](./context7-setup.md)

---

### 2. GitHub MCP (Optional)

**Purpose**: GitHub API 통합 (Issues, PRs, Commits)

**Status**: ⏳ 선택적 설치

**What it does**:
- PR 자동 생성/업데이트
- Issue 조회 및 코멘트
- Commit 히스토리 분석

**Use Cases**:
```
"PR #123 코멘트 확인해줘"
"최근 커밋 10개 분석해줘"
"이슈 #45 해결을 위한 계획 수립"
```

**Setup Guide**: [recommended-mcp-servers.md](./recommended-mcp-servers.md#github-mcp)

---

### 3. MySQL MCP (Recommended 🔥)

**Purpose**: MySQL 데이터베이스 쿼리 및 스키마 관리

**Status**: ✅ 설치 권장 (백엔드 개발 시 필수)

**What it does**:
- 스키마 조회 (SHOW CREATE TABLE)
- SQL 쿼리 실행 (SELECT, INSERT 등)
- 인덱스 확인 (SHOW INDEX)
- 마이그레이션 검증 (Flyway 히스토리)

**Use Cases**:
```
"users 테이블 스키마 알려줘"
"최근 가입 사용자 10명 조회"
"messages 테이블 인덱스 확인"
"느린 쿼리 찾아줘"
```

**Setup Guide**: [mysql-setup.md](./mysql-setup.md)

---

### 4. Playwright MCP (Recommended 🔥)

**Purpose**: E2E 테스트 및 UI 자동화

**Status**: ✅ 설치 권장 (워크플로우 자동화 필수)

**What it does**:
- E2E 테스트 자동화 (회원가입, 로그인, 메시지 전송)
- UI 요소 검증 (버튼, 입력 필드, 스크린샷)
- 크로스 브라우저 테스트 (Chrome, Firefox, Safari)
- 성능 측정 (페이지 로드 시간)
- 시각적 회귀 테스트

**Use Cases**:
```
"Playwright로 로그인 플로우 테스트해줘"
"Stats 패널이 제대로 표시되는지 확인해줘"
"Chrome, Firefox, Safari에서 모두 테스트해줘"
"페이지 로드 성능 측정해줘"
```

**Setup Guide**: [playwright-setup.md](./playwright-setup.md)

---

### 5. GitHub MCP (Recommended 🔥)

**Purpose**: PR/Issue 자동화 및 워크플로우 통합

**Status**: ✅ 설치 권장 (워크플로우 자동화 필수)

**What it does**:
- PR 자동 생성 (제목, 본문, 라벨)
- PR 리뷰 자동화 (코멘트 작성)
- Issue 관리 (조회, 생성, 코멘트)
- Commit 분석 (히스토리 요약)
- 브랜치 관리 (생성, 삭제, 비교)

**Use Cases**:
```
"GitHub MCP로 PR 생성해줘"
"PR #5 리뷰해줘"
"최근 커밋 10개 분석해줘"
"Issue #12에 진행 상황 코멘트해줘"
"테스트 결과를 PR에 코멘트해줘"
```

**Setup Guide**: [github-setup.md](./github-setup.md)

---

### 6. Filesystem MCP (Built-in)

**Purpose**: 로컬 파일 시스템 접근

**Status**: ✅ 기본 제공

**What it does**:
- 파일 읽기/쓰기
- 디렉터리 탐색
- 파일 존재 확인

**Use Cases**:
```
"index.tsx 파일 읽어줘"
"새 컴포넌트 파일 생성해줘"
"src/ 폴더 구조 보여줘"
```

**Setup**: 별도 설정 불필요 (Claude Code 기본 기능)

---

## Installation

### Prerequisites

- **Claude Code CLI**: 최신 버전
- **Node.js**: 18+ (일부 MCP 서버 실행용)
- **npx**: npm과 함께 설치됨

### Step 1: Context7 MCP 설정 (권장)

Context7은 Claude Code에 기본 내장되어 있으므로, 별도 설치가 필요 없습니다.

**활성화 확인**:
1. Claude Code 실행
2. 다음 쿼리 테스트:
   ```
   "TECHNICAL_GUIDE.md에서 테스트 전략 섹션 알려줘"
   ```
3. 정확한 내용 반환되면 활성화 성공

**설정 상세**: [context7-setup.md](./context7-setup.md)

### Step 2: 추가 MCP 서버 (선택적)

필요에 따라 다른 MCP 서버를 설치할 수 있습니다:

- **GitHub MCP**: [recommended-mcp-servers.md](./recommended-mcp-servers.md#github-mcp)
- **Slack MCP**: [recommended-mcp-servers.md](./recommended-mcp-servers.md#slack-mcp)
- **Postgres MCP**: [recommended-mcp-servers.md](./recommended-mcp-servers.md#postgres-mcp)

---

## Usage Examples

### Example 1: Document Query (Context7)

```
User: "TECHNICAL_GUIDE.md에서 Gemini API 호출 부분 알려줘"

Context7 MCP:
1. docs/TECHNICAL_GUIDE.md 인덱스 검색
2. "Gemini API" 관련 섹션 추출
3. sendMessage 함수 코드 반환

Output:
"메시지 전송 로직은 다음과 같습니다:
[sendMessage 함수 코드 블록]
- GoogleGenAI 클라이언트 생성
- generateContent 호출
- systemInstruction으로 Lobi 페르소나 주입
- temperature 0.8로 자연스러운 응답"
```

### Example 2: Cross-Document Reference

```
User: "Architecture Agent로 설계한 ADR 문서 참고해서 테스트 작성해줘"

Workflow:
1. Context7로 최근 생성된 ADR 문서 검색
2. Test Engineer Agent가 ADR 내용 참조
3. 설계에 맞는 테스트 코드 생성

Output:
- 테스트 파일 생성
- ADR의 컴포넌트 구조 반영
- 설계된 인터페이스 기반 Mock 생성
```

### Example 3: GitHub PR Review (GitHub MCP)

```
User: "PR #10 코멘트 확인해서 리뷰 반영해줘"

Workflow:
1. GitHub MCP로 PR #10 코멘트 조회
2. Code Review Checklist로 항목별 분류
3. Security Agent로 보안 코멘트 우선 처리
4. Refactor Agent로 코드 품질 개선

Output:
- 코멘트별 수정 완료
- 리뷰 반영 내역 요약
- PR 업데이트 커밋
```

---

## Configuration

### Config File Location

MCP 서버는 Claude Code 설정 파일에서 관리됩니다:

- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows**: `%APPDATA%/Claude/claude_desktop_config.json`
- **Linux**: `~/.config/Claude/claude_desktop_config.json`

### Sample Configuration

```json
{
  "mcpServers": {
    "context7": {
      "command": "npx",
      "args": ["-y", "@context7/mcp-server"]
    },
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_TOKEN": "your_github_token_here"
      }
    }
  }
}
```

**주의**: API 토큰은 환경 변수 또는 `.env` 파일에서 관리하세요.

---

## Troubleshooting

### MCP Server Not Working

**Issue**: MCP 서버가 응답하지 않음

**Solutions**:
1. Claude Code 재시작
2. 설정 파일 경로 확인
3. Node.js 버전 확인 (18+)
4. MCP 서버 로그 확인 (Claude Code 설정에서)

### Document Not Indexed

**Issue**: Context7이 문서를 찾지 못함

**Solutions**:
1. 파일 확장자 확인 (`.md` 파일만 인덱싱)
2. 파일 경로 확인 (프로젝트 루트 기준)
3. Claude Code 재시작 후 재인덱싱
4. 명시적으로 파일명 포함하여 쿼리

### API Token Error

**Issue**: GitHub/Slack MCP에서 인증 오류

**Solutions**:
1. 토큰 유효성 확인
2. 환경 변수 설정 확인
3. 토큰 권한 확인 (repo, read:org 등)
4. 설정 파일 JSON 문법 확인

---

## Best Practices

### 1. Document Organization

MCP가 효과적으로 작동하려면 문서 구조가 중요합니다:

```
docs/
├── TECHNICAL_GUIDE.md      # 기술 문서 (상세)
├── adr/                     # 설계 결정 기록
│   └── ADR-001-*.md
├── plans/                   # 구현 계획
│   └── PLAN_*.md
└── workflows/               # 워크플로우 가이드
    └── AI_DEVELOPMENT_WORKFLOW.md
```

### 2. Semantic File Names

파일명을 의미있게 작성하면 검색이 쉬워집니다:

```
✅ PLAN_user_authentication_system.md
✅ ADR-001-component-separation-strategy.md
✅ security-audit-2025-12-30.md

❌ plan1.md
❌ doc.md
❌ temp.md
```

### 3. Clear Section Headers

문서 내 섹션 헤더를 명확히 하면 Context7이 정확히 추출합니다:

```markdown
## Gemini API 호출 로직          ✅ 명확
## 메시지 전송                   ✅ 명확
## sendMessage 함수 구현          ✅ 명확

## 코드                          ❌ 모호
## 기타                          ❌ 모호
```

### 4. Regular Updates

문서를 최신 상태로 유지:
- 코드 변경 시 관련 문서 업데이트
- 오래된 ADR은 "폐기됨" 표시
- 계획 문서는 완료 후 "✅ Complete" 표시

---

## Security Considerations

### API Tokens

**절대 Git에 커밋하지 마세요**:

```bash
# .gitignore에 추가
.env.local
claude_desktop_config.json
*_token.txt
```

**환경 변수 사용**:

```json
{
  "mcpServers": {
    "github": {
      "env": {
        "GITHUB_TOKEN": "${GITHUB_TOKEN}"  // 환경 변수 참조
      }
    }
  }
}
```

### Rate Limiting

외부 API 호출 시 Rate Limit 고려:
- GitHub API: 5000 requests/hour (인증 시)
- Slack API: Tier별 다름
- Context7: 로컬 인덱싱 (제한 없음)

---

## Related Documentation

- **Context7 Setup**: [context7-setup.md](./context7-setup.md)
- **Recommended MCP Servers**: [recommended-mcp-servers.md](./recommended-mcp-servers.md)
- **폴더 구조**: [`../../docs/plans/FOLDER_STRUCTURE_SPEC.md`](../../docs/plans/FOLDER_STRUCTURE_SPEC.md)
- **개발 워크플로우**: [`../../docs/workflows/AI_DEVELOPMENT_WORKFLOW.md`](../../docs/workflows/AI_DEVELOPMENT_WORKFLOW.md) (생성 예정)

---

**Last Updated**: 2025-12-30
**Active MCP Servers**: 1 (Context7)
**Recommended for Backend**: MySQL MCP
**Next Update**: MCP 서버 추가 시
