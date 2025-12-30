# Recommended MCP Servers

**Version**: 1.0
**Date**: 2025-12-30

---

## Overview

GENKUB/LobAI 프로젝트에 유용한 MCP (Model Context Protocol) 서버 목록입니다. 프로젝트 단계에 따라 필요한 MCP 서버를 선택적으로 설치할 수 있습니다.

---

## Quick Reference

| MCP Server | Status | Priority | Use Case |
|------------|--------|----------|----------|
| **Context7** | ✅ 기본 제공 | 최상 | 문서 인덱싱 및 검색 |
| **GitHub** | 선택적 | 상 | PR, Issue, Commit 조회 |
| **Postgres** | 백엔드 추가 시 | 중 | DB 스키마, 쿼리 |
| **Slack** | 팀 협업 시 | 하 | 알림, 메시지 전송 |
| **Filesystem** | ✅ 기본 제공 | 최상 | 파일 읽기/쓰기 |

---

## 1. Context7 MCP

**Status**: ✅ 기본 제공 (설치 완료)

**Purpose**: 프로젝트 문서 자동 인덱싱 및 자연어 검색

**Setup**: [context7-setup.md](./context7-setup.md) 참조

**Usage**:
```
"TECHNICAL_GUIDE.md에서 테스트 전략 알려줘"
"모든 ADR 문서 요약해줘"
```

---

## 2. GitHub MCP

**Status**: ⏳ 선택적 (PR/Issue 관리 필요 시 설치)

**Purpose**: GitHub API 통합 (PR, Issue, Commit 조회 및 생성)

### Installation

```bash
# 1. GitHub Personal Access Token 생성
# https://github.com/settings/tokens
# 권한: repo, read:org

# 2. 환경 변수 설정
export GITHUB_TOKEN=your_github_token_here

# 3. Claude Code 설정 파일에 추가
# macOS: ~/Library/Application Support/Claude/claude_desktop_config.json
```

```json
{
  "mcpServers": {
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

### Usage Examples

```
"PR #10 코멘트 확인해줘"
"최근 커밋 10개 분석해서 변경 사항 요약"
"이슈 #45 해결을 위한 계획 수립"
"새 PR 생성해줘: 제목 '사용자 인증 시스템 추가'"
```

### Use Cases

- PR 자동 리뷰 요청
- Issue 조회 및 코멘트
- Commit 히스토리 분석
- 브랜치 비교

---

## 3. Postgres MCP

**Status**: 🔮 백엔드 추가 시 설치 (현재 불필요)

**Purpose**: PostgreSQL 데이터베이스 스키마 조회 및 쿼리 실행

### Installation (Spring Boot 추가 후)

```bash
# 1. 환경 변수 설정
export DATABASE_URL=postgresql://user:password@localhost:5432/lobai

# 2. Claude Code 설정 추가
```

```json
{
  "mcpServers": {
    "postgres": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres"],
      "env": {
        "DATABASE_URL": "${DATABASE_URL}"
      }
    }
  }
}
```

### Usage Examples

```
"users 테이블 스키마 보여줘"
"최근 가입한 사용자 10명 조회"
"stats 테이블에서 평균 happiness 계산"
"느린 쿼리 찾아서 최적화 제안"
```

### Use Cases

- 스키마 설계 검증
- 데이터 조회 및 분석
- 마이그레이션 검증
- 성능 튜닝

---

## 4. Slack MCP

**Status**: 선택적 (팀 협업 시)

**Purpose**: Slack 메시지 전송 및 채널 관리

### Installation

```bash
# 1. Slack App 생성
# https://api.slack.com/apps
# 권한: chat:write, channels:read

# 2. Bot Token 생성
export SLACK_BOT_TOKEN=xoxb-your-token-here

# 3. Claude Code 설정 추가
```

```json
{
  "mcpServers": {
    "slack": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-slack"],
      "env": {
        "SLACK_BOT_TOKEN": "${SLACK_BOT_TOKEN}"
      }
    }
  }
}
```

### Usage Examples

```
"#dev 채널에 '배포 완료' 메시지 보내줘"
"최근 #support 메시지 10개 가져와서 요약"
"알림: PR 리뷰 요청"
```

### Use Cases

- 배포 알림
- 에러 알림 (Sentry 연동)
- PR 리뷰 요청
- 일일 리포트 전송

---

## 5. Filesystem MCP

**Status**: ✅ 기본 제공

**Purpose**: 로컬 파일 시스템 접근 (Claude Code 기본 기능)

### Usage

```
"index.tsx 파일 읽어줘"
"src/components/ 폴더 구조 보여줘"
"새 파일 생성: src/utils/validation.ts"
```

**Note**: 별도 설정 불필요 (Read, Write, Edit 도구로 자동 제공)

---

## Installation Priority

### 프로젝트 초기 (MVP)
1. ✅ Context7 (기본 제공)
2. ✅ Filesystem (기본 제공)

### PR/Issue 관리 시작
3. GitHub MCP 설치

### 백엔드 추가 후
4. Postgres MCP 설치

### 팀 협업 시작
5. Slack MCP 설치 (선택적)

---

## Configuration Example (All-in-One)

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
        "GITHUB_TOKEN": "${GITHUB_TOKEN}"
      }
    },
    "postgres": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres"],
      "env": {
        "DATABASE_URL": "${DATABASE_URL}"
      }
    },
    "slack": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-slack"],
      "env": {
        "SLACK_BOT_TOKEN": "${SLACK_BOT_TOKEN}"
      }
    }
  }
}
```

---

## Security Best Practices

### 환경 변수 관리

```bash
# .env 파일 (Git 제외)
GITHUB_TOKEN=ghp_xxxx...
DATABASE_URL=postgresql://...
SLACK_BOT_TOKEN=xoxb-...

# .gitignore에 추가
.env
.env.local
claude_desktop_config.json
```

### 토큰 권한 최소화

- **GitHub**: repo (필요 시), read:org만
- **Postgres**: 읽기 전용 계정 권장 (프로덕션 DB)
- **Slack**: chat:write, channels:read만

---

## Troubleshooting

### MCP 서버 인식 안 됨

**Solutions**:
1. Claude Code 재시작
2. 설정 파일 경로 확인
3. JSON 문법 오류 확인 (trailing comma 등)
4. npx 실행 권한 확인

### 인증 오류

**Solutions**:
1. 토큰 유효성 확인 (만료, 권한)
2. 환경 변수 설정 확인 (`echo $GITHUB_TOKEN`)
3. 토큰 재생성

### 성능 저하

**Solutions**:
1. 불필요한 MCP 서버 비활성화
2. 쿼리 최적화 (구체적으로)
3. Rate Limit 확인

---

## Related Documentation

- **Context7 Setup**: [context7-setup.md](./context7-setup.md)
- **MCP Configs README**: [README.md](./README.md)
- **Folder Structure**: [../../docs/plans/FOLDER_STRUCTURE_SPEC.md](../../docs/plans/FOLDER_STRUCTURE_SPEC.md)

---

**Last Updated**: 2025-12-30
**Active MCP Servers**: 2 (Context7, Filesystem)
**Next Update**: 추가 MCP 서버 설치 시
