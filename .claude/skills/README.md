# Skills Directory

**Purpose**: 특정 키워드로 자동 트리거되는 개발 규칙 및 체크리스트

---

## Overview

스킬은 **특정 작업 시 자동으로 적용되는 규칙 집합**입니다. 서브에이전트가 "누가 할 것인가"라면, 스킬은 "어떻게 할 것인가"를 정의합니다. Markdown 형식으로 작성되며, Frontmatter의 트리거 키워드로 자동 활성화됩니다.

---

## Available Skills

| Skill | Triggers | Purpose | Enforces |
|-------|----------|---------|----------|
| **feature-planner** | plan, planning, phases | 기능 개발 계획 수립 | TDD 단계별 계획, 품질 게이트 |
| **code-review-checklist** | review, pr, pull request | PR 리뷰 자동화 | 기능/테스트/보안/성능 체크 |
| **test-strategy** | test strategy, pyramid, coverage | 프론트엔드 테스트 전략 | Test Pyramid (React), 커버리지 목표 |
| **backend-test-strategy** | backend test, spring test, integration test | 백엔드 테스트 전략 | Spring Boot Unit/Integration/E2E |
| **security-checklist** | security, api, auth | 보안 검증 | OWASP Top 10, 입력 검증 |

---

## Usage

### Automatic Trigger

키워드가 포함된 메시지를 입력하면 스킬이 자동으로 활성화됩니다:

```
✅ "planning 사용자 인증 시스템" → Feature Planner 활성화
✅ "review this PR" → Code Review Checklist 표시
✅ "test strategy for Stats component" → Test Strategy 가이드 제공
✅ "security check for API" → Security Checklist 강제 적용
```

### Manual Trigger

스킬을 명시적으로 호출할 수도 있습니다:

```
"/feature-planner 사용자 프로필 저장"
"Code Review Checklist 스킬 사용해서 PR 검토"
```

---

## Skill Structure

모든 스킬은 다음 형식을 따릅니다:

```markdown
---
name: skill-name
description: 스킬 설명
triggers: ["keyword1", "keyword2", "keyword3"]
---

# Skill Title

## Purpose
무엇을 하는 스킬인지

## Rules / Checklist
강제되는 규칙 또는 체크리스트

## Examples
사용 예시

## Anti-Patterns
하지 말아야 할 것
```

---

## Skill Descriptions

### 1. Feature Planner

**Triggers**: `plan`, `planning`, `phases`, `breakdown`, `strategy`

**Purpose**: 신규 기능 개발 시 TDD 기반 단계별 계획을 강제합니다.

**Enforces**:
- 기능을 3-7 단계로 분할
- 각 단계마다 Red-Green-Refactor 사이클
- 품질 게이트 체크리스트 (빌드/테스트/린트/보안)
- 사용자 승인 후 실행

**Output**: `docs/plans/PLAN_{feature-name}.md`

**Example**:
```
Input: "planning 사용자 프로필 저장 기능"
Output:
  - PLAN_user_profile_save.md 생성
  - Phase 1: 데이터 모델 설계
  - Phase 2: API 엔드포인트 구현
  - Phase 3: 프론트엔드 통합
  - 각 Phase마다 TDD 체크리스트
```

---

### 2. Code Review Checklist

**Triggers**: `review`, `pr`, `pull request`, `checklist`

**Purpose**: PR 리뷰 시 놓치기 쉬운 항목을 자동으로 체크합니다.

**Enforces**:
- **기능**: 요구사항 충족, 엣지 케이스, 에러 핸들링
- **테스트**: 단위/통합 테스트, 커버리지 80% 이상
- **보안**: 입력 검증, API Key 노출, XSS/CSRF 방지
- **성능**: N+1 쿼리, 리렌더링, 메모리 누수
- **코드 품질**: 린트, 타입 체크, 중복 제거, 네이밍

**Example**:
```
Input: "review this PR"
Output:
  ✅ 기능 체크리스트 표시
  ✅ 테스트 커버리지 확인 요청
  ✅ 보안 취약점 검사 요청
  ✅ 성능 이슈 확인
```

---

### 3. Test Strategy

**Triggers**: `test strategy`, `test pyramid`, `coverage`, `testing approach`

**Purpose**: 테스트 작성 시 Test Pyramid 원칙을 강제하여 효율적인 테스트를 작성하도록 합니다.

**Enforces**:
- **Unit Tests (70%)**: 빠름, 많이, 비즈니스 로직 중심
- **Integration Tests (20%)**: 중간 속도, 컴포넌트 간 상호작용
- **E2E Tests (10%)**: 느림, 최소한, 핵심 시나리오만

**Anti-Patterns**:
- ❌ 모든 코드에 E2E 테스트
- ❌ Private 메서드 테스트
- ❌ 단순 getter/setter 테스트

**Example**:
```
Input: "test strategy for Stats component"
Output:
  - Unit: getBarColor, handleAction 함수 테스트
  - Integration: Stats + Actions 통합 테스트
  - E2E: Feed 버튼 → 스탯 증가 시나리오
  - 커버리지 목표: 80% (비즈니스 로직)
```

---

### 4. Security Checklist

**Triggers**: `security`, `api`, `auth`, `authentication`, `authorization`

**Purpose**: 보안 관련 코드 작성 시 OWASP Top 10 기반 체크리스트를 자동 적용합니다.

**Enforces**:
- **입력 검증**: 모든 외부 입력, 화이트리스트, 타입/길이/형식
- **인증/인가**: 엔드포인트 보호, 권한 체크, 세션/토큰 관리
- **민감정보**: 비밀번호 해싱, 환경변수 시크릿, 로그 제외
- **API 보안**: Rate Limiting, CORS, HTTPS
- **SQL/NoSQL**: Prepared Statement, ORM, 동적 쿼리 최소화
- **XSS/CSRF**: 출력 이스케이핑, CSP 헤더, CSRF 토큰

**Example**:
```
Input: "security check for API integration"
Output:
  ✅ API Key 환경변수 확인
  ✅ 입력 검증 로직 확인
  ✅ Rate Limiting 구현 확인
  ✅ CORS 설정 검토
  ✅ HTTPS 강제 여부
```

---

## Workflow Integration

스킬은 서브에이전트와 함께 사용됩니다:

```
Feature Planner Skill (계획 수립)
    ↓
Architecture Agent (구조 설계)
    ↓
Test Strategy Skill (테스트 전략)
    ↓
Test Engineer Agent (테스트 작성)
    ↓
Implementation (기능 구현)
    ↓
Security Checklist Skill (보안 검증)
    ↓
Security Agent (보안 검사)
    ↓
Refactor Agent (코드 개선)
    ↓
Code Review Checklist Skill (최종 검토)
```

---

## Examples

### Example 1: New Feature Development

```
User: "planning 사용자 알림 시스템"

Feature Planner Skill 자동 활성화:
1. 요구사항 분석
2. 3-7 단계로 분할
3. TDD 기반 계획 수립
4. 품질 게이트 정의
5. docs/plans/PLAN_notification_system.md 생성
```

### Example 2: PR Review

```
User: "review this code"

Code Review Checklist Skill 자동 활성화:
- [ ] 기능: 요구사항 충족, 엣지 케이스
- [ ] 테스트: 단위/통합, 커버리지 80%+
- [ ] 보안: 입력 검증, API Key 노출 없음
- [ ] 성능: N+1 없음, 메모리 누수 없음
- [ ] 품질: 린트, 타입, 중복 없음
```

### Example 3: Security Audit

```
User: "security check for authentication"

Security Checklist Skill 자동 활성화:
- [ ] 비밀번호 해싱 (bcrypt)
- [ ] 토큰 관리 (JWT, Refresh Token)
- [ ] 세션 보안 (httpOnly, secure)
- [ ] Rate Limiting (로그인 시도 제한)
- [ ] CSRF 토큰
- [ ] XSS 방지
```

---

## Adding New Skills

새 스킬을 추가하려면:

1. **Markdown 파일 생성**: `.claude/skills/{skill-name}.md`
2. **Frontmatter 작성**:
   ```yaml
   ---
   name: skill-name
   description: 스킬 설명
   triggers: ["keyword1", "keyword2"]
   ---
   ```
3. **내용 작성**:
   - Purpose
   - Rules / Checklist
   - Examples
   - Anti-Patterns
4. **테스트**: 트리거 키워드로 자동 활성화 확인
5. **문서 업데이트**: 이 README에 스킬 추가

---

## Troubleshooting

### Skill Not Triggered
**Issue**: 키워드 입력했지만 스킬이 활성화되지 않음

**Solutions**:
- Frontmatter 형식 확인 (YAML 문법)
- 트리거 키워드 철자 확인
- Claude Code 재시작
- 명시적으로 스킬 이름 호출

### Wrong Checklist Applied
**Issue**: 다른 스킬의 체크리스트가 적용됨

**Solutions**:
- 트리거 키워드 중복 확인
- 더 구체적인 키워드 사용
- 스킬 이름 명시적으로 호출

---

## Best Practices

1. **Specific Triggers**: 트리거 키워드는 구체적으로 (일반적인 단어 피하기)
2. **Measurable Checklist**: 체크 항목은 측정 가능하게
3. **Examples First**: 예시를 먼저 보여주기
4. **Anti-Patterns**: 하지 말아야 할 것 명시
5. **Tool Agnostic**: 특정 도구에 의존하지 않게 (범용성)

---

## Related Documentation

- **폴더 구조**: [`docs/plans/FOLDER_STRUCTURE_SPEC.md`](../../docs/plans/FOLDER_STRUCTURE_SPEC.md)
- **서브에이전트**: [`.claude/agents/README.md`](../agents/README.md)
- **Feature Planner 템플릿**: [`../../.claude/reference/plan-tamplate.md`](../reference/plan-tamplate.md)
- **개발 가이드**: [`../../.claude/reference/guideline.md`](../reference/guideline.md)

---

**Last Updated**: 2025-12-30
**Skill Count**: 5
**Next Update**: 스킬 추가/수정 시
