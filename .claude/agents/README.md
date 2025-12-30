# Subagents Directory

**Purpose**: 전문 역할을 수행하는 AI 서브에이전트 정의

---

## Overview

이 디렉터리는 GENKUB/LobAI 프로젝트의 개발 워크플로우를 지원하는 전문 서브에이전트들을 포함합니다. 각 에이전트는 특정 역할에 특화되어 있으며, YAML 형식으로 정의됩니다.

---

## Available Agents

| Agent | Role | Primary Tools | Output |
|-------|------|---------------|--------|
| **architecture-agent** | 구조 설계 및 ADR 작성 | Read, Glob, Grep, Write | ADR 문서, 폴더 구조 제안 |
| **test-engineer-agent** | 테스트 전략 및 코드 작성 | Read, Write, Edit, Bash | 테스트 파일, 커버리지 리포트 |
| **security-agent** | 보안 검사 및 취약점 분석 | Read, Grep | 보안 리포트, 개선 제안 |
| **refactor-agent** | 코드 품질 개선 | Read, Edit, Bash | 리팩터링된 코드, 개선 리포트 |
| **integration-specialist-agent** | 외부 시스템 통합 | Read, Write, Edit, Bash, WebFetch | 통합 코드, 설정 파일, 문서 |
| **backend-developer-agent** | Spring Boot 백엔드 코드 작성 | Read, Write, Edit, Bash | Entity, Repository, Service, Controller, DTO |

---

## Usage

### Method 1: Explicit Agent Call
```
"Architecture Agent를 사용해서 Stats 컴포넌트 분리 설계해줘"
"Test Engineer Agent로 handleAction 함수 테스트 작성해줘"
"Security Agent로 API Key 노출 여부 검사해줘"
```

### Method 2: Context-Based Suggestion
Claude Code가 작업 컨텍스트를 분석하여 적절한 에이전트를 자동으로 추천할 수 있습니다.

---

## Agent Structure

모든 에이전트는 다음 구조를 따릅니다:

```yaml
name: agent-name               # kebab-case 이름
description: string            # 역할 설명
tools: [...]                   # 사용 가능한 도구
workflow: [...]                # 작업 단계
constraints: [...]             # 제약사항
output: string                 # 기대 출력
```

전체 스키마는 [`docs/plans/SUBAGENT_SCHEMA.md`](../../docs/plans/SUBAGENT_SCHEMA.md)를 참조하세요.

---

## Workflow Integration

에이전트들은 다음과 같이 협업합니다:

```
1. Feature Planner Skill
   계획 수립 및 단계 정의
        ↓
2. Architecture Agent
   구조 설계 및 ADR 작성
        ↓
3. Test Engineer Agent
   테스트 코드 작성 (TDD)
        ↓
4. Implementation
   실제 기능 구현 (수동 또는 다른 에이전트)
        ↓
5. Security Agent
   보안 검증
        ↓
6. Refactor Agent
   코드 품질 개선
        ↓
7. Code Review Checklist Skill
   최종 검토
```

---

## Examples

### Example 1: Component Refactoring

**Task**: index.tsx (634줄)를 컴포넌트로 분리

**Workflow**:
```bash
1. "Architecture Agent로 index.tsx 컴포넌트 분리 설계해줘"
   → ADR 문서 생성: docs/adr/ADR-001-component-separation.md
   → 폴더 구조 제안: src/components/Stats/, src/components/Chat/ 등

2. "Refactor Agent로 위 설계에 따라 Stats 컴포넌트 분리해줘"
   → src/components/Stats/StatsPanel.tsx 생성
   → index.tsx에서 Stats 관련 코드 이동
   → 테스트 실행하여 기능 유지 확인

3. "Test Engineer Agent로 StatsPanel 컴포넌트 테스트 작성해줘"
   → __tests__/components/Stats/StatsPanel.test.tsx 생성
```

### Example 2: Security Audit

**Task**: API Key 보안 취약점 검사

**Workflow**:
```bash
1. "Security Agent로 API Key 노출 여부 검사해줘"
   → 보안 리포트 생성
   → 취약점 우선순위 정리
   → 개선 코드 예시 제공

2. "Integration Specialist Agent로 Spring Boot BFF 패턴 구현해줘"
   → API 프록시 설계
   → CORS 설정
   → 인증 토큰 관리
```

### Example 3: New Feature Development

**Task**: 사용자 프로필 저장 기능 추가

**Workflow**:
```bash
1. "/feature-planner 사용자 프로필 저장"
   → docs/plans/PLAN_user_profile_save.md 생성

2. "Architecture Agent로 프로필 저장 구조 설계해줘"
   → ADR 문서 작성
   → API 엔드포인트 설계

3. "Test Engineer Agent로 프로필 저장 테스트 작성해줘"
   → Unit 테스트: __tests__/unit/services/profileService.test.ts
   → Integration 테스트: __tests__/integration/profile-save.test.ts

4. "Security Agent로 프로필 데이터 보안 검증해줘"
   → 민감정보 검증
   → 입력 검증 체크
```

---

## Adding New Agents

새 에이전트를 추가하려면:

1. **스키마 참조**: [`docs/plans/SUBAGENT_SCHEMA.md`](../../docs/plans/SUBAGENT_SCHEMA.md) 확인
2. **YAML 파일 작성**: `.claude/agents/{name}-agent.yml` 생성
3. **검증**:
   ```bash
   npx js-yaml .claude/agents/{name}-agent.yml
   ```
4. **테스트**: 실제 작업으로 동작 확인
5. **문서 업데이트**: 이 README에 에이전트 추가

---

## Troubleshooting

### Agent Not Recognized
**Issue**: Claude Code가 에이전트를 인식하지 못함

**Solutions**:
- YAML 파일 경로 확인: `.claude/agents/` 폴더에 있는지
- YAML 문법 오류 확인: `npx js-yaml` 실행
- Claude Code 재시작
- 에이전트 이름 명시적으로 호출

### Wrong Tools Used
**Issue**: 에이전트가 허용되지 않은 도구 사용

**Solutions**:
- `tools` 필드에 필요한 도구만 나열
- `constraints` 필드에 금지 사항 명시
- 워크플로우 단계 명확히 정의

### Role Overlap
**Issue**: 여러 에이전트가 같은 작업을 수행

**Solutions**:
- 각 에이전트의 `description`을 명확히 구분
- `constraints`로 역할 범위 제한
- 워크플로우 문서에서 에이전트 선택 기준 명시

---

## Best Practices

1. **Single Responsibility**: 한 에이전트 = 한 역할
2. **Clear Constraints**: 하지 말아야 할 것을 명확히
3. **Minimal Tools**: 필요한 도구만 사용
4. **Workflow First**: 에이전트 호출 전 워크플로우 확인
5. **Documentation**: ADR, 테스트 등 문서 생성 습관화

---

## Related Documentation

- **에이전트 스키마**: [`docs/plans/SUBAGENT_SCHEMA.md`](../../docs/plans/SUBAGENT_SCHEMA.md)
- **폴더 구조**: [`docs/plans/FOLDER_STRUCTURE_SPEC.md`](../../docs/plans/FOLDER_STRUCTURE_SPEC.md)
- **개발 워크플로우**: [`docs/workflows/AI_DEVELOPMENT_WORKFLOW.md`](../../docs/workflows/AI_DEVELOPMENT_WORKFLOW.md) (생성 예정)
- **기술 가이드**: [`docs/TECHNICAL_GUIDE.md`](../../docs/TECHNICAL_GUIDE.md)

---

**Last Updated**: 2025-12-30
**Agent Count**: 6
**Next Update**: 에이전트 추가/수정 시
