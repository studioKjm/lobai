# Subagent YAML Schema Specification

**Version**: 1.0
**Date**: 2025-12-30
**Reference**: Claude Code Official Documentation

---

## Overview

서브에이전트는 **특정 역할에 특화된 AI 에이전트**로, 복잡한 작업을 역할별로 분담하여 처리합니다. YAML 형식으로 정의하며, Claude Code가 이를 인식하여 적절한 도구와 제약사항을 적용합니다.

---

## YAML Schema

### Required Fields

```yaml
name: string                    # 에이전트 고유 이름 (kebab-case)
description: string             # 에이전트 역할 및 목적 (한 줄 요약)
tools: array<string>            # 사용 가능한 도구 목록
workflow: array<string>         # 작업 단계 (순서대로)
constraints: array<string>      # 제약사항 및 금지 사항
output: string                  # 기대되는 출력 형식
```

### Optional Fields

```yaml
version: string                 # 에이전트 버전 (예: "1.0.0")
author: string                  # 작성자
triggers: array<string>         # 자동 활성화 키워드 (optional)
examples: array<object>         # 사용 예시
dependencies: array<string>     # 필요한 외부 도구/라이브러리
tags: array<string>             # 검색/분류용 태그
```

---

## Field Specifications

### 1. `name`

**Type**: `string`
**Format**: kebab-case
**Required**: Yes

**Description**: 에이전트의 고유 식별자. 호출 시 사용됩니다.

**Naming Convention**:
- `{role}-agent` 형식
- 소문자, 하이픈으로 단어 연결
- 명확하고 역할을 드러내는 이름

**Examples**:
```yaml
name: architecture-agent
name: test-engineer-agent
name: security-review-agent
```

**Anti-Patterns**:
```yaml
name: agent1                    # ❌ 불명확
name: ArchitectureAgent         # ❌ camelCase 사용
name: architecture_agent        # ❌ snake_case 사용
```

---

### 2. `description`

**Type**: `string`
**Length**: 50-200 characters
**Required**: Yes

**Description**: 에이전트가 무엇을 하는지 명확히 설명. Claude Code UI에 표시됩니다.

**Best Practices**:
- 역할을 명확히 설명
- "무엇을" + "어떻게" 포함
- 한 문장으로 요약

**Examples**:
```yaml
description: 기능 구현 전 아키텍처 설계 및 기존 구조 분석, ADR 문서 작성
description: 테스트 전략 수립, Vitest 설정, TDD 기반 테스트 코드 작성
description: OWASP Top 10 기반 보안 검사, API Key 보호, 입력 검증
```

**Anti-Patterns**:
```yaml
description: 코드 작성                           # ❌ 너무 모호
description: 이 에이전트는...                      # ❌ 불필요한 서두
description: Architecture, design, planning...   # ❌ 나열만
```

---

### 3. `tools`

**Type**: `array<string>`
**Required**: Yes

**Description**: 에이전트가 사용할 수 있는 Claude Code 도구 목록.

**Available Tools**:
| Tool | Purpose | Use Case |
|------|---------|----------|
| `Read` | 파일 읽기 | 코드 분석, 문서 참조 |
| `Write` | 새 파일 생성 | 문서 작성, 설정 파일 생성 |
| `Edit` | 기존 파일 수정 | 코드 개선, 버그 수정 |
| `Glob` | 파일 패턴 검색 | 특정 확장자 파일 찾기 |
| `Grep` | 코드 내용 검색 | 함수/클래스 찾기 |
| `Bash` | 쉘 명령 실행 | 테스트 실행, 빌드 |
| `WebFetch` | 웹 콘텐츠 가져오기 | 문서 참조, API 호출 |
| `TodoWrite` | Todo 목록 관리 | 작업 추적 |

**Selection Guidelines**:
- **Read 전용**: 분석/리뷰 에이전트 (Security, Code Review)
- **Read + Write**: 문서 생성 에이전트 (Architecture, Documentation)
- **Read + Edit**: 리팩터링 에이전트 (Refactor)
- **Read + Write + Bash**: 테스트 에이전트 (Test Engineer)
- **모든 도구**: 통합 에이전트 (Integration Specialist)

**Examples**:
```yaml
# Architecture Agent (설계만, 코드 작성 안 함)
tools:
  - Read
  - Glob
  - Grep
  - Write

# Test Engineer Agent (테스트 작성 + 실행)
tools:
  - Read
  - Write
  - Edit
  - Bash

# Security Agent (검사만, 수정 안 함)
tools:
  - Read
  - Grep
```

**Anti-Patterns**:
```yaml
tools: []                       # ❌ 빈 배열
tools: [Read, Write, Edit, Bash, Glob, Grep, ...]  # ❌ 모든 도구 남용
```

---

### 4. `workflow`

**Type**: `array<string>` or `object`
**Required**: Yes

**Description**: 에이전트가 작업을 수행하는 순서. 단계별로 명확히 정의합니다.

**Format**:
```yaml
# Simple (배열)
workflow:
  - 기존 코드베이스 구조 분석
  - 새 기능이 들어갈 위치 결정
  - 레이어 간 책임 분리 확인
  - ADR 문서 작성

# Detailed (객체)
workflow:
  step1:
    name: 코드 분석
    actions:
      - Read existing code
      - Identify patterns
  step2:
    name: 설계
    actions:
      - Determine component structure
      - Define interfaces
```

**Best Practices**:
- 3-7 단계가 적절
- 각 단계는 명확한 동작
- 순서가 논리적
- 결과물이 명시됨

**Examples**:
```yaml
# Architecture Agent
workflow:
  - 기존 코드베이스 구조 분석 (Glob, Read로 파일 확인)
  - 새 기능이 들어갈 위치 결정 (레이어 식별)
  - 기존 패턴과의 일관성 검증
  - ADR 문서 작성 (docs/adr/ 폴더)
  - 폴더 구조 제안 (다이어그램 포함)

# Test Engineer Agent
workflow:
  - 테스트 대상 코드 분석 (복잡도, 의존성)
  - Test Pyramid 기반 전략 수립 (Unit 70%, Integration 20%, E2E 10%)
  - 테스트 코드 작성 (AAA 패턴, Red-Green-Refactor)
  - 테스트 실행 및 커버리지 측정
  - 부족한 영역 추가 테스트
```

---

### 5. `constraints`

**Type**: `array<string>` or `object`
**Required**: Yes

**Description**: 에이전트가 **하지 말아야 할 것**. 역할 범위를 명확히 제한합니다.

**Purpose**:
- 에이전트 간 역할 중복 방지
- 의도하지 않은 동작 차단
- 품질 기준 강제

**Categories**:
1. **Scope Constraints**: 역할 범위 제한
2. **Technical Constraints**: 기술적 제약
3. **Quality Constraints**: 품질 기준
4. **Process Constraints**: 프로세스 규칙

**Examples**:
```yaml
# Architecture Agent
constraints:
  - 코드 작성 금지 (설계 문서만 생성)
  - 반드시 ADR 형식 준수
  - 기존 아키텍처 패턴과 일관성 유지
  - 5개 이상 컴포넌트 제안 시 사용자 승인 필요

# Refactor Agent
constraints:
  - 기능 변경 절대 금지 (구조 개선만)
  - 모든 테스트는 계속 통과해야 함
  - 한 번에 하나씩 개선 (atomic commits)
  - 리팩터링 전 테스트 실행 필수

# Security Agent
constraints:
  - 코드 수정 금지 (리포트만 생성)
  - OWASP Top 10 기준 준수
  - False Positive 가능성 명시
  - 개선 코드는 예시로만 제공
```

**Best Practices**:
- 명확하고 구체적
- 측정 가능한 기준
- 예외 상황 명시
- "~하지 말 것" 형식

**Anti-Patterns**:
```yaml
constraints:
  - 잘 만들기                   # ❌ 모호함
  - 버그 없이                   # ❌ 측정 불가
```

---

### 6. `output`

**Type**: `string`
**Required**: Yes

**Description**: 에이전트가 생성해야 할 최종 결과물. 사용자가 무엇을 받을지 예상할 수 있습니다.

**Format Guidelines**:
- 파일 형식 명시 (Markdown, YAML, TypeScript 등)
- 저장 위치 제안
- 필수 섹션 나열

**Examples**:
```yaml
# Architecture Agent
output: |
  - ADR 문서 (docs/adr/ADR-XXX-{title}.md)
  - 폴더 구조 제안 (Markdown 코드블록)
  - 컴포넌트 다이어그램 (선택적)

# Test Engineer Agent
output: |
  - 테스트 파일 (__tests__/{feature}.test.ts)
  - 커버리지 리포트 (npm test -- --coverage)
  - 테스트 전략 문서 (docs/testing/{feature}-test-strategy.md)

# Security Agent
output: |
  - 보안 리포트 (Markdown, 우선순위별 정렬)
  - 취약점 목록 (OWASP 분류)
  - 개선 코드 예시 (코드블록)
```

**Best Practices**:
- 파일명 컨벤션 포함
- 예시 구조 제공
- 선택적 출력 명시

---

## Complete Example

```yaml
name: test-engineer-agent
version: 1.0.0
description: 테스트 전략 수립, Vitest 설정, TDD 기반 테스트 코드 작성
author: AI Development Team

tools:
  - Read
  - Write
  - Edit
  - Bash

workflow:
  - 테스트 대상 코드 분석 (복잡도, 의존성 확인)
  - Test Pyramid 기반 전략 수립 (Unit 70%, Integration 20%, E2E 10%)
  - 테스트 환경 설정 (Vitest, React Testing Library)
  - 테스트 코드 작성 (AAA 패턴, Red-Green-Refactor)
  - 테스트 실행 및 커버리지 측정
  - 부족한 영역 추가 테스트 작성
  - 테스트 문서 업데이트

constraints:
  - TDD 원칙 준수 (테스트가 구현보다 먼저)
  - 단위 테스트 70%, 통합 테스트 20%, E2E 10% 비율 유지
  - 커버리지 목표: 비즈니스 로직 80% 이상
  - Private 메서드 테스트 금지
  - 테스트를 위한 테스트 작성 금지
  - 모든 테스트는 독립적으로 실행 가능해야 함

output: |
  1. 테스트 파일
     - Unit: __tests__/unit/{feature}/{component}.test.ts
     - Integration: __tests__/integration/{feature}.test.ts
     - E2E: e2e/{user-flow}.spec.ts

  2. 커버리지 리포트
     - HTML: coverage/index.html
     - Summary: 콘솔 출력

  3. 테스트 전략 문서
     - docs/testing/{feature}-test-strategy.md
     - 테스트 시나리오 목록
     - Mock/Stub 전략

triggers:
  - test
  - testing
  - coverage
  - vitest
  - unit test

examples:
  - input: "handleAction 함수에 대한 단위 테스트 작성해줘"
    output: "__tests__/unit/actions/handleAction.test.ts 파일 생성"

  - input: "Stats 시스템 통합 테스트 필요해"
    output: "__tests__/integration/stats-system.test.ts 파일 생성"

dependencies:
  - vitest
  - "@testing-library/react"
  - "@testing-library/jest-dom"

tags:
  - testing
  - tdd
  - quality-assurance
```

---

## Validation Checklist

에이전트 정의 작성 후 확인:

- [ ] `name` 필드: kebab-case, 역할 명확
- [ ] `description` 필드: 50-200자, 명확한 설명
- [ ] `tools` 필드: 역할에 맞는 도구만 선택
- [ ] `workflow` 필드: 3-7 단계, 논리적 순서
- [ ] `constraints` 필드: 명확하고 측정 가능
- [ ] `output` 필드: 파일 형식, 위치, 구조 명시
- [ ] YAML 문법 오류 없음
- [ ] 다른 에이전트와 역할 중복 없음
- [ ] 실제 사용 시나리오로 테스트 가능

---

## Testing Guidelines

### 1. Syntax Validation
```bash
# YAML 문법 검증
npx js-yaml .claude/agents/your-agent.yml
```

### 2. Functional Testing
```markdown
# 테스트 시나리오
1. 입력: "{에이전트 역할} 작업 요청"
2. 기대 동작: workflow에 정의된 단계 순서대로 실행
3. 기대 출력: output에 명시된 형식으로 결과 생성
4. 제약사항 확인: constraints에 정의된 금지 사항 준수
```

### 3. Integration Testing
```markdown
# 다른 에이전트와 협업 테스트
1. Architecture Agent로 설계 문서 생성
2. Test Engineer Agent가 설계 문서 참조하여 테스트 작성
3. Context7 MCP로 자동 참조 확인
```

---

## Best Practices Summary

1. **Single Responsibility**: 한 에이전트 = 한 역할
2. **Clear Boundaries**: constraints로 역할 범위 명확히
3. **Measurable Output**: 검증 가능한 결과물 정의
4. **Tool Minimalism**: 필요한 도구만 사용
5. **Workflow Clarity**: 단계별 명확한 동작 정의
6. **Documentation**: 예시와 함께 문서화

---

**Schema Version**: 1.0.0
**Last Updated**: 2025-12-30
**Next Review**: 에이전트 5개 생성 후
