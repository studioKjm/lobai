# Folder Structure Specification

**Version**: 1.0
**Date**: 2025-12-30

---

## Overview

GENKUB/LobAI 프로젝트의 AI 개발 인프라를 위한 표준 폴더 구조를 정의합니다.

---

## Standard Structure

```
.claude/
├── agents/              # 서브에이전트 정의 파일 (YAML)
│   ├── README.md
│   ├── architecture-agent.yml
│   ├── test-engineer-agent.yml
│   ├── security-agent.yml
│   ├── refactor-agent.yml
│   └── integration-specialist-agent.yml
│
├── skills/              # 재사용 가능한 스킬 문서 (Markdown)
│   ├── README.md
│   ├── feature-planner.md
│   ├── code-review-checklist.md
│   ├── test-strategy.md
│   └── security-checklist.md
│
├── mcp-configs/         # MCP 서버 설정 및 가이드
│   ├── README.md
│   ├── context7-setup.md
│   └── recommended-mcp-servers.md
│
└── reference/           # 참고 가이드라인 (기존)
    ├── guideline.md
    ├── plan-tamplate.md
    └── SKILL.md
```

---

## Directory Purposes

### `.claude/agents/`

**Purpose**: 전문 역할을 수행하는 서브에이전트 정의

**File Format**: YAML (`.yml`)

**Contents**:
- 에이전트 이름 및 설명
- 사용 가능한 도구 목록
- 작업 워크플로우
- 제약사항 및 규칙
- 기대 출력 형식

**Naming Convention**: `{role}-agent.yml`
- 예: `architecture-agent.yml`, `test-engineer-agent.yml`

**Usage**:
```bash
# Claude Code에서 에이전트 호출
"Architecture Agent를 사용해서 Stats 컴포넌트 분리 설계해줘"
```

---

### `.claude/skills/`

**Purpose**: 특정 작업 시 자동으로 트리거되는 규칙 및 체크리스트

**File Format**: Markdown (`.md`) with Frontmatter

**Contents**:
- 스킬 이름 및 설명
- 트리거 키워드
- 강제 체크리스트
- 사용 예시
- Anti-patterns 목록

**Naming Convention**: `{skill-name}.md`
- 예: `feature-planner.md`, `code-review-checklist.md`

**Frontmatter Format**:
```yaml
---
name: skill-name
description: 스킬 설명
triggers: ["keyword1", "keyword2", "keyword3"]
---
```

**Usage**:
```bash
# 키워드 자동 감지
"planning 사용자 인증 시스템" → Feature Planner 스킬 자동 활성화
"review this PR" → Code Review Checklist 자동 표시
```

---

### `.claude/mcp-configs/`

**Purpose**: Model Context Protocol 서버 설정 및 사용 가이드

**File Format**: Markdown (`.md`)

**Contents**:
- MCP 서버 설치 방법
- 설정 예시 (JSON)
- 사용법 및 쿼리 예시
- 트러블슈팅
- 추천 MCP 서버 목록

**Naming Convention**: `{mcp-server-name}-setup.md`
- 예: `context7-setup.md`, `github-mcp-setup.md`

**Usage**:
```bash
# Context7 MCP를 통한 문서 쿼리
"TECHNICAL_GUIDE.md에서 테스트 전략 섹션 알려줘"
```

---

### `.claude/reference/`

**Purpose**: AI 개발 베스트 프랙티스 및 템플릿

**File Format**: Markdown (`.md`)

**Contents** (기존):
- `guideline.md`: AI 개발 도구 종합 가이드
- `plan-tamplate.md`: TDD 기반 구현 계획 템플릿
- `SKILL.md`: Feature Planner 스킬 원본

**Usage**: 새 에이전트/스킬 작성 시 참고 자료

---

## File Lifecycle

### 1. Creation Phase
```
1. 명세 작성 (docs/plans/에 스펙 문서)
2. 파일 생성 (.claude/ 하위 해당 폴더)
3. 테스트 시나리오 실행
4. 문서화 (README.md 업데이트)
```

### 2. Usage Phase
```
1. 사용자가 키워드 입력 또는 에이전트 명시적 호출
2. Claude Code가 해당 파일 참조
3. 정의된 워크플로우/체크리스트 실행
4. 결과물 생성 (코드, 문서, 리포트 등)
```

### 3. Maintenance Phase
```
1. 사용 패턴 분석
2. 불필요한 규칙 제거
3. 새로운 케이스 추가
4. 버전 업데이트 (Git 태그)
```

---

## Quality Standards

### Agents (YAML)
- [ ] 필수 필드: name, description, tools, workflow, constraints, output
- [ ] 역할 중복 없음 (한 에이전트 = 한 책임)
- [ ] 도구 사용 명확히 정의
- [ ] 예시 작업 시나리오 포함

### Skills (Markdown)
- [ ] Frontmatter 형식 올바름
- [ ] 트리거 키워드 3개 이상
- [ ] 체크리스트 구체적
- [ ] Anti-patterns 명시
- [ ] 실전 예시 포함

### MCP Configs (Markdown)
- [ ] 설치 단계 명확
- [ ] 설정 JSON 예시 포함
- [ ] 트러블슈팅 섹션 존재
- [ ] 쿼리 예시 3개 이상

---

## Version Control

### Git Tracking
- **모든 `.claude/` 파일 Git 추적**
- `.gitignore`에 제외하지 않음 (팀 공유 필요)

### Commit Convention
```
feat(agents): add Architecture Agent
feat(skills): add Code Review Checklist skill
docs(mcp): update Context7 setup guide
fix(agents): correct Test Engineer workflow
```

### Branching Strategy
- 새 에이전트/스킬 추가: `feature/add-{name}-agent` 브랜치
- 기존 파일 수정: `improve/{name}` 브랜치
- 버그 수정: `fix/{issue}` 브랜치

---

## Integration Points

### With Claude Code
- `.claude/` 폴더는 Claude Code가 자동으로 인식
- 서브에이전트는 명시적 호출 또는 컨텍스트 기반 추천
- 스킬은 트리거 키워드로 자동 활성화

### With MCP Servers
- Context7: `.md` 파일 자동 인덱싱
- GitHub MCP: PR 리뷰 자동화 (향후)
- Postgres MCP: DB 스키마 쿼리 (백엔드 추가 시)

### With Development Workflow
```
계획 수립 (Feature Planner Skill)
    ↓
설계 (Architecture Agent)
    ↓
테스트 작성 (Test Engineer Agent)
    ↓
구현 (수동 또는 Implementation Agent)
    ↓
보안 검증 (Security Agent)
    ↓
리팩터링 (Refactor Agent)
    ↓
최종 리뷰 (Code Review Checklist Skill)
```

---

## Expected Outcomes

### Developer Experience
- ✅ 명확한 에이전트 역할 분리 → 혼란 감소
- ✅ 키워드 기반 스킬 트리거 → 빠른 접근
- ✅ 표준화된 폴더 구조 → 쉬운 온보딩

### Code Quality
- ✅ 자동화된 체크리스트 → 실수 방지
- ✅ TDD 워크플로우 강제 → 테스트 커버리지 향상
- ✅ 보안 검증 자동화 → 취약점 조기 발견

### Collaboration
- ✅ 재사용 가능한 에이전트/스킬 → 팀 전체 활용
- ✅ Git 버전 관리 → 변경 이력 추적
- ✅ 문서화 강제 → 지식 공유

---

## Next Steps

1. **Phase 1**: 폴더 생성 및 README 작성 ✅ (현재)
2. **Phase 2**: 5개 서브에이전트 정의 작성
3. **Phase 3**: 4개 스킬 문서 생성
4. **Phase 4**: Context7 MCP 설정
5. **Phase 5**: 통합 테스트 및 워크플로우 문서화

---

**Specification Version**: 1.0
**Author**: AI Development Team
**Last Updated**: 2025-12-30
