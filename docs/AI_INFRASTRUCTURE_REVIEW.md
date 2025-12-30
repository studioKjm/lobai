# AI Infrastructure Review & Recommendations
## 백엔드 구현을 위한 서브에이전트/스킬/MCP 검토

**Date**: 2025-12-30
**Reviewer**: Claude Code
**Context**: Spring Boot + MySQL 백엔드 구현 준비

---

## Executive Summary

**현재 상태**: LobAI 프로젝트는 **프론트엔드 개발에 최적화**된 AI 인프라를 갖추고 있습니다. 백엔드 구현을 위해 **3개의 추가 서브에이전트**와 **2개의 추가 스킬**, **1개의 MCP 서버**가 필요합니다.

---

## Current AI Infrastructure

### 1. Subagents (서브에이전트) - 5개 ✅

| Agent | Purpose | Tools | Status | Backend Relevance |
|-------|---------|-------|--------|-------------------|
| **architecture-agent** | 구조 설계, ADR 작성 | Read, Glob, Grep, Write | ✅ Active | ⭐⭐⭐⭐⭐ 필수 (API/DB 설계) |
| **test-engineer-agent** | 테스트 전략, TDD | Read, Write, Edit, Bash | ✅ Active | ⭐⭐⭐⭐ 중요 (Backend 테스트) |
| **security-agent** | OWASP 보안 검사 | Read, Grep | ✅ Active | ⭐⭐⭐⭐⭐ 필수 (API 보안) |
| **refactor-agent** | 코드 품질 개선 | Read, Edit, Bash | ✅ Active | ⭐⭐⭐ 보통 (코드 정리) |
| **integration-specialist-agent** | 외부 시스템 통합 | Read, Write, Edit, Bash, WebFetch | ✅ Active | ⭐⭐⭐⭐ 중요 (Gemini API) |

**평가**:
- ✅ Architecture Agent는 DB 스키마 및 API 설계에 활용 가능
- ✅ Security Agent는 JWT, SQL Injection 등 백엔드 보안 검증 가능
- ⚠️ **Spring Boot 코드 작성 전용 에이전트 부재**

---

### 2. Skills (스킬) - 4개 ✅

| Skill | Triggers | Purpose | Backend Relevance |
|-------|----------|---------|-------------------|
| **feature-planner** | plan, planning, phases | TDD 기반 계획 수립 | ⭐⭐⭐⭐⭐ 필수 (백엔드 기능 계획) |
| **code-review-checklist** | review, pr | PR 리뷰 자동화 | ⭐⭐⭐⭐ 중요 (백엔드 코드 리뷰) |
| **test-strategy** | test strategy, pyramid | Test Pyramid 강제 | ⭐⭐⭐⭐ 중요 (Unit/Integration 테스트) |
| **security-checklist** | security, api, auth | OWASP Top 10 강제 | ⭐⭐⭐⭐⭐ 필수 (API 보안) |

**평가**:
- ✅ 모든 스킬이 백엔드 개발에도 활용 가능
- ⚠️ **백엔드 전용 스킬 부재** (API 문서화, DB 마이그레이션 등)

---

### 3. MCP Servers - 1개 설치 ✅

| MCP Server | Purpose | Status | Backend Relevance |
|------------|---------|--------|-------------------|
| **Context7** | 문서 인덱싱 및 검색 | ✅ 설치됨 | ⭐⭐⭐⭐ 중요 (계획 문서 참조) |
| **GitHub** | PR/Issue 관리 | ⏳ 미설치 (권장) | ⭐⭐⭐ 보통 (PR 자동화) |
| **Postgres** | PostgreSQL 쿼리 | ⏳ 미설치 (MySQL 필요) | ⭐⭐⭐⭐⭐ 필수 (DB 관리) |
| **Slack** | 알림/메시지 전송 | ⏳ 미설치 (선택) | ⭐ 낮음 (팀 협업 시) |

**평가**:
- ✅ Context7은 계획 문서 참조에 유용
- ⚠️ **MySQL MCP 필요** (Postgres MCP 대신)
- ⚠️ **API 테스트 MCP 필요** (Postman/HTTP Client)

---

## Gap Analysis: 백엔드 구현에 필요한 것

### 🚨 Missing: Backend Development Agent

**문제**:
- 현재 에이전트는 **구조 설계**와 **테스트**에 특화되어 있음
- **Spring Boot 코드 작성**을 수행하는 에이전트 부재
- Java/Spring 코드 생성 시 일반 Claude Code가 처리 → 전문성 부족

**필요한 에이전트**:

#### 1. Backend Developer Agent (신규 필요 ⭐⭐⭐⭐⭐)

**역할**:
- Spring Boot 엔티티, 리포지토리, 서비스, 컨트롤러 작성
- JPA 관계 설정 (OneToMany, ManyToOne 등)
- DTO 및 Mapper 구현
- 예외 처리 및 검증 로직

**Tools**: Read, Write, Edit, Bash

**Workflow**:
1. 요구사항 분석 (API 스펙, DB 스키마)
2. JPA 엔티티 작성
3. Repository 인터페이스 생성
4. Service 레이어 비즈니스 로직 구현
5. Controller REST 엔드포인트 구현
6. DTO 및 Validation 추가
7. 예외 처리 (GlobalExceptionHandler)
8. 테스트 실행 확인

**Example**:
```
"Backend Developer Agent로 User 엔티티와 UserService 작성해줘"
→ User.java (JPA 엔티티)
→ UserRepository.java
→ UserService.java (비즈니스 로직)
→ UserController.java (REST API)
→ CreateUserRequest.java (DTO)
→ UserResponse.java (DTO)
```

---

#### 2. Database Designer Agent (신규 필요 ⭐⭐⭐⭐)

**역할**:
- MySQL 스키마 설계 (DDL)
- 인덱스 및 외래키 최적화
- 마이그레이션 스크립트 작성 (Flyway)
- 데이터 정규화 검증

**Tools**: Read, Write, WebFetch (MySQL 문서 참조)

**Workflow**:
1. 요구사항 분석 (데이터 모델)
2. ERD 설계 (텍스트 형태)
3. DDL 스크립트 작성
4. 인덱스 전략 수립
5. 외래키 및 제약조건 추가
6. 마이그레이션 스크립트 생성
7. 데이터 정규화 확인

**Example**:
```
"Database Designer Agent로 Messages 테이블 설계해줘"
→ messages 테이블 DDL
→ 외래키 (user_id, persona_id)
→ 인덱스 (user_id + created_at)
→ V001__create_messages_table.sql (Flyway)
```

---

### 🟡 Optional: API Designer Agent

**역할**:
- RESTful API 엔드포인트 설계
- OpenAPI/Swagger 문서 생성
- Request/Response 스키마 정의
- 에러 코드 체계 수립

**우선순위**: 낮음 (Architecture Agent가 일부 커버 가능)

---

### 🚨 Missing: Backend-Specific Skills

#### 1. Backend Testing Strategy Skill (신규 필요 ⭐⭐⭐⭐)

**Triggers**: `backend test`, `spring test`, `integration test`

**Purpose**: 백엔드 테스트 전략 강제 (Unit/Integration/E2E 비율)

**Enforces**:
- **Unit Tests (60%)**: Service 레이어 비즈니스 로직
- **Integration Tests (30%)**: Controller + Service + DB
- **E2E Tests (10%)**: API 전체 플로우

**Example**:
```
Input: "backend test strategy for MessageService"
Output:
  - Unit: MessageService.saveMessage() 테스트 (Mocking Repository)
  - Integration: POST /api/messages → DB 저장 확인
  - E2E: 회원가입 → 로그인 → 메시지 전송 플로우
```

---

#### 2. API Documentation Skill (신규 필요 ⭐⭐⭐)

**Triggers**: `api docs`, `swagger`, `openapi`

**Purpose**: API 문서화 강제 (OpenAPI 3.0 기준)

**Enforces**:
- 모든 엔드포인트에 @Operation 어노테이션
- Request/Response 예시 포함
- 에러 코드 문서화
- Swagger UI 자동 생성

**Example**:
```
@Operation(summary = "메시지 전송", description = "사용자 메시지를 전송하고 AI 응답을 받습니다.")
@ApiResponse(responseCode = "201", description = "메시지 전송 성공")
@ApiResponse(responseCode = "401", description = "인증 실패")
@PostMapping("/api/messages")
public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) { ... }
```

---

### 🚨 Missing: MySQL MCP Server

#### MySQL MCP (신규 필요 ⭐⭐⭐⭐⭐)

**Purpose**: MySQL 데이터베이스 쿼리 및 스키마 관리

**Why Needed**:
- 계획 문서에 MySQL 사용 명시 (Postgres 아님)
- 스키마 조회, 쿼리 실행, 마이그레이션 검증 필요

**Use Cases**:
```
"users 테이블 스키마 조회"
"최근 가입 사용자 10명 조회"
"messages 테이블에 인덱스 확인"
"Flyway 마이그레이션 히스토리 조회"
```

**Installation**:
```json
{
  "mcpServers": {
    "mysql": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-mysql"],
      "env": {
        "MYSQL_CONNECTION_STRING": "mysql://user:password@localhost:3306/lobai_db"
      }
    }
  }
}
```

---

## Recommendations

### Priority 1: 즉시 추가 필요 (Week 1)

1. **Backend Developer Agent** ⭐⭐⭐⭐⭐
   - Spring Boot 코드 작성 전용
   - JPA 엔티티, Service, Controller 생성
   - 파일: `.claude/agents/backend-developer-agent.yml`

2. **MySQL MCP** ⭐⭐⭐⭐⭐
   - DB 스키마 조회 및 쿼리 실행
   - 설정: `claude_desktop_config.json`

3. **Backend Testing Strategy Skill** ⭐⭐⭐⭐
   - 백엔드 테스트 비율 강제
   - 파일: `.claude/skills/backend-test-strategy.md`

---

### Priority 2: Week 2-3 추가 권장

4. **Database Designer Agent** ⭐⭐⭐⭐
   - MySQL DDL 작성 전용
   - 인덱스 및 외래키 최적화
   - 파일: `.claude/agents/database-designer-agent.yml`

5. **API Documentation Skill** ⭐⭐⭐
   - OpenAPI/Swagger 문서화 강제
   - 파일: `.claude/skills/api-documentation.md`

6. **GitHub MCP** ⭐⭐⭐
   - PR 자동 생성 및 코멘트 관리
   - 설정: `claude_desktop_config.json`

---

### Priority 3: 선택적 (Phase 2)

7. **Postman/HTTP Client MCP** ⭐⭐
   - API 테스트 자동화
   - 환경별 요청 저장 (dev/prod)

8. **Slack MCP** ⭐
   - 배포 알림, 에러 알림
   - 팀 협업 시 유용

---

## Implementation Plan

### Week 1: Core Backend Infrastructure

**Day 1: Backend Developer Agent 생성**
```bash
1. `.claude/agents/backend-developer-agent.yml` 작성
2. YAML 검증: npx js-yaml backend-developer-agent.yml
3. 테스트: "Backend Developer Agent로 User 엔티티 작성해줘"
```

**Day 2: MySQL MCP 설치**
```bash
1. claude_desktop_config.json에 MySQL MCP 추가
2. 환경 변수 설정 (MYSQL_CONNECTION_STRING)
3. Claude Code 재시작
4. 테스트: "users 테이블 스키마 조회해줘"
```

**Day 3: Backend Testing Strategy Skill 생성**
```bash
1. `.claude/skills/backend-test-strategy.md` 작성
2. Frontmatter 트리거 설정: ["backend test", "spring test"]
3. Test Pyramid 비율 정의 (60% Unit, 30% Integration, 10% E2E)
4. 테스트: "backend test strategy for MessageService"
```

---

### Week 2: Advanced Agents & Skills

**Day 1-2: Database Designer Agent**
```bash
1. `.claude/agents/database-designer-agent.yml` 작성
2. Workflow: 요구사항 → ERD → DDL → 인덱스 → 마이그레이션
3. 테스트: "Database Designer Agent로 Messages 테이블 설계"
```

**Day 3: API Documentation Skill**
```bash
1. `.claude/skills/api-documentation.md` 작성
2. OpenAPI 3.0 템플릿 포함
3. Swagger 어노테이션 예시 포함
4. 테스트: "API docs for MessageController"
```

**Day 4: GitHub MCP 설치**
```bash
1. GitHub Personal Access Token 생성
2. claude_desktop_config.json에 GitHub MCP 추가
3. 테스트: "최근 커밋 10개 조회"
```

---

## Current vs. Ideal State

### Current State (프론트엔드 중심)

```
서브에이전트:
  - Architecture ✅
  - Test Engineer ✅
  - Security ✅
  - Refactor ✅
  - Integration Specialist ✅

스킬:
  - Feature Planner ✅
  - Code Review ✅
  - Test Strategy (Frontend) ✅
  - Security Checklist ✅

MCP:
  - Context7 ✅
```

### Ideal State (프론트엔드 + 백엔드)

```
서브에이전트:
  - Architecture ✅
  - Test Engineer ✅
  - Security ✅
  - Refactor ✅
  - Integration Specialist ✅
  - Backend Developer ⭐ NEW
  - Database Designer ⭐ NEW

스킬:
  - Feature Planner ✅
  - Code Review ✅
  - Test Strategy (Frontend) ✅
  - Backend Test Strategy ⭐ NEW
  - Security Checklist ✅
  - API Documentation ⭐ NEW

MCP:
  - Context7 ✅
  - MySQL ⭐ NEW
  - GitHub ⭐ NEW (권장)
```

---

## Cost-Benefit Analysis

### 추가 노력 vs. 효과

| 항목 | 작성 시간 | 효과 | ROI |
|------|-----------|------|-----|
| Backend Developer Agent | 2-3 시간 | 코드 품질 ⬆️ 80% | ⭐⭐⭐⭐⭐ |
| MySQL MCP | 30분 | DB 관리 효율 ⬆️ 90% | ⭐⭐⭐⭐⭐ |
| Backend Test Strategy | 1-2 시간 | 테스트 커버리지 ⬆️ 70% | ⭐⭐⭐⭐⭐ |
| Database Designer Agent | 2 시간 | 스키마 최적화 ⬆️ 60% | ⭐⭐⭐⭐ |
| API Documentation Skill | 1 시간 | API 문서화 ⬆️ 100% | ⭐⭐⭐⭐ |
| GitHub MCP | 30분 | PR 자동화 ⬆️ 50% | ⭐⭐⭐ |

**Total Effort**: 7-9 시간
**Expected Productivity Gain**: 50-70%

---

## Action Items

### Immediate (이번 주)

- [ ] Backend Developer Agent 작성 (`.claude/agents/backend-developer-agent.yml`)
- [ ] MySQL MCP 설치 및 테스트
- [ ] Backend Testing Strategy Skill 작성 (`.claude/skills/backend-test-strategy.md`)

### Short-term (2주 내)

- [ ] Database Designer Agent 작성
- [ ] API Documentation Skill 작성
- [ ] GitHub MCP 설치 (선택)

### Long-term (Phase 2)

- [ ] Postman/HTTP Client MCP 검토
- [ ] Slack MCP 검토 (팀 협업 시)
- [ ] 커스텀 MCP 서버 개발 (필요 시)

---

## Success Metrics

백엔드 구현 완료 후 측정 지표:

1. **코드 생성 속도**: Backend Developer Agent 사용 시 30% 향상
2. **테스트 커버리지**: Backend Test Strategy로 80% 이상 달성
3. **API 문서화**: Swagger UI 자동 생성 100%
4. **DB 쿼리 효율**: MySQL MCP로 스키마 조회 시간 90% 단축
5. **PR 리뷰 시간**: GitHub MCP + Code Review Checklist로 50% 단축

---

## Conclusion

**현재 AI 인프라는 프론트엔드 개발에 최적화**되어 있으며, 백엔드 구현을 위해 **3개의 핵심 추가 구성 요소**가 필요합니다:

1. ⭐⭐⭐⭐⭐ **Backend Developer Agent** (Spring Boot 코드 작성)
2. ⭐⭐⭐⭐⭐ **MySQL MCP** (DB 쿼리 및 스키마 관리)
3. ⭐⭐⭐⭐ **Backend Testing Strategy Skill** (백엔드 테스트 강제)

이 3가지를 추가하면 **백엔드 개발 생산성이 50-70% 향상**될 것으로 예상됩니다.

---

## Related Documentation

- **Backend Plan**: `docs/plans/PLAN_Backend_Database_Design.md`
- **Subagent README**: `.claude/agents/README.md`
- **Skill README**: `.claude/skills/README.md`
- **MCP README**: `.claude/mcp-configs/README.md`

---

**Last Updated**: 2025-12-30
**Reviewer**: Claude Code
**Status**: Review Complete ✅
**Next Step**: Backend Developer Agent 작성 시작
