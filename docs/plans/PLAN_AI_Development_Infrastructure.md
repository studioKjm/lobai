# Implementation Plan: AI Development Infrastructure Setup

**Status**: 🔄 In Progress
**Started**: 2025-12-30
**Last Updated**: 2025-12-30
**Estimated Completion**: 2025-12-31

---

**⚠️ CRITICAL INSTRUCTIONS**: After completing each phase:
1. ✅ Check off completed task checkboxes
2. 🧪 Run all quality gate validation commands
3. ⚠️ Verify ALL quality gate items pass
4. 📅 Update "Last Updated" date above
5. 📝 Document learnings in Notes section
6. ➡️ Only then proceed to next phase

⛔ **DO NOT skip quality gates or proceed with failing checks**

---

## 📋 Overview

### Feature Description
GENKUB/LobAI 프로젝트에 AI 기반 개발 인프라를 구축합니다. 5개의 전문 서브에이전트, 재사용 가능한 스킬 문서, MCP 서버 통합을 통해 체계적이고 안전한 AI 협업 개발 환경을 조성합니다.

### Success Criteria
- [ ] 5개 서브에이전트 정의 완료 및 작동 검증
- [ ] 3개 이상의 클로드 스킬 문서 생성
- [ ] Context7 MCP 서버 설치 및 연동
- [ ] 전체 인프라 통합 테스트 통과
- [ ] 개발 워크플로우 문서화

### User Impact
- **개발 속도 향상**: 전문 에이전트가 역할별로 작업 처리
- **코드 품질 보장**: 자동화된 테스트/보안/리팩터링 검증
- **일관된 개발 표준**: 스킬 기반 규칙 강제로 스파게티 코드 방지
- **지식 축적**: MCP를 통한 프로젝트 문서 컨텍스트 고정

---

## 🏗️ Architecture Decisions

| Decision | Rationale | Trade-offs |
|----------|-----------|------------|
| **서브에이전트 YAML 포맷 사용** | Claude Code 표준 형식, 도구/제약사항 명확화 | 초기 학습 곡선 존재 |
| **스킬은 Markdown + Frontmatter** | 가독성 우수, Git 버전 관리 용이 | 실행 로직은 별도 구현 필요 |
| **Context7 MCP 우선 도입** | 프로젝트 문서 자동 인덱싱 | 추가 설정 필요 |
| **TDD 방식 계획 수립** | 검증 가능한 각 단계 완료 기준 | 계획 수립 시간 증가 |
| **.claude/ 폴더 구조 표준화** | 향후 다른 프로젝트 재사용 가능 | 초기 구조 설계 중요 |

---

## 📦 Dependencies

### Required Before Starting
- [ ] TECHNICAL_GUIDE.md 작성 완료 ✅
- [ ] .claude/reference/ 가이드라인 파일 존재 확인 ✅
- [ ] Git 저장소 초기화 완료 ✅

### External Dependencies
- **Claude Code CLI**: 최신 버전
- **Node.js**: 18+ (MCP 서버 실행용)
- **npx**: MCP 패키지 실행

---

## 🧪 Test Strategy

### Testing Approach
각 서브에이전트와 스킬은 **실제 사용 시나리오 기반 검증**을 진행합니다. TDD 원칙을 적용하되, 에이전트 정의는 코드가 아니므로 "기대 동작 명세 → 구현 → 검증" 사이클을 따릅니다.

### Verification Checklist per Phase
| Component | Verification Method | Success Criteria |
|-----------|---------------------|------------------|
| **서브에이전트** | 간단한 작업 요청 후 올바른 도구 사용 확인 | 에이전트가 지정된 역할만 수행 |
| **스킬 문서** | 키워드 트리거 후 규칙 준수 여부 확인 | 체크리스트 강제 적용 |
| **MCP 서버** | 문서 쿼리 후 정확한 정보 반환 확인 | Context7이 프로젝트 문서 인식 |

### Manual Test Scenarios
1. **Architecture Agent**: "Stats 컴포넌트 분리 설계해줘" → ADR 문서 생성 확인
2. **Test Engineer Agent**: "handleAction 함수 테스트 작성해줘" → Vitest 테스트 파일 생성
3. **Security Agent**: "API Key 보안 취약점 검사해줘" → 보안 리포트 + 개선 코드
4. **Feature Planner Skill**: "/feature-planner 사용자 인증 시스템" → TDD 기반 단계별 계획
5. **Context7 MCP**: "TECHNICAL_GUIDE.md에서 테스트 전략 알려줘" → 정확한 섹션 반환

---

## 🚀 Implementation Phases

### Phase 1: 폴더 구조 및 표준 정의
**Goal**: Claude Code가 인식할 수 있는 표준 폴더 구조 생성
**Estimated Time**: 1 hour
**Status**: ⏳ Pending

#### Tasks

**🔴 RED: 기대 동작 명세**
- [ ] **Spec 1.1**: 폴더 구조 명세 작성
  - File(s): `docs/plans/FOLDER_STRUCTURE_SPEC.md`
  - Expected: 서브에이전트/스킬/MCP 설정 파일 위치 정의
  - Details:
    ```
    .claude/
    ├── agents/           # 서브에이전트 정의
    ├── skills/           # 재사용 가능한 스킬
    ├── reference/        # 가이드라인 (기존)
    └── mcp-configs/      # MCP 서버 설정
    ```

- [ ] **Spec 1.2**: 서브에이전트 YAML 스키마 정의
  - File(s): `docs/plans/SUBAGENT_SCHEMA.md`
  - Expected: 필수 필드(name, description, tools, workflow, constraints) 명세
  - Details: Claude Code 공식 서브에이전트 형식 참고

**🟢 GREEN: 폴더 및 README 생성**
- [ ] **Task 1.3**: .claude/ 하위 폴더 생성
  - Directories: `.claude/agents/`, `.claude/skills/`, `.claude/mcp-configs/`
  - Goal: 표준 구조 확립
  - Commands:
    ```bash
    mkdir -p .claude/agents
    mkdir -p .claude/skills
    mkdir -p .claude/mcp-configs
    ```

- [ ] **Task 1.4**: 각 폴더에 README.md 생성
  - Files:
    - `.claude/agents/README.md`: 서브에이전트 사용법
    - `.claude/skills/README.md`: 스킬 트리거 방법
    - `.claude/mcp-configs/README.md`: MCP 설정 가이드
  - Goal: 폴더 용도 명확화

**🔵 REFACTOR: 문서 품질 개선**
- [ ] **Task 1.5**: README 예시 및 템플릿 추가
  - Files: 각 README에 실전 예시 추가
  - Checklist:
    - [ ] 명확한 사용 예시
    - [ ] 트러블슈팅 섹션
    - [ ] 다음 단계 가이드

#### Quality Gate ✋

**Build & Tests**:
- [ ] 폴더 구조가 계획대로 생성됨
- [ ] 모든 README 파일 존재
- [ ] Markdown 문법 오류 없음

**Documentation**:
- [ ] 폴더별 용도 명확히 문서화
- [ ] 예시 코드/설정 포함

**Validation Commands**:
```bash
# 폴더 구조 확인
tree .claude/

# Markdown 검증 (markdownlint 설치 시)
npx markdownlint .claude/**/*.md

# Git 추적 확인
git status
```

**Manual Test Checklist**:
- [ ] .claude/agents/ 폴더 존재
- [ ] .claude/skills/ 폴더 존재
- [ ] .claude/mcp-configs/ 폴더 존재
- [ ] 각 폴더에 README.md 존재
- [ ] README에 사용 예시 포함

---

### Phase 2: 5개 서브에이전트 생성
**Goal**: Architecture, Test Engineer, Security, Refactor, Integration Specialist 에이전트 정의
**Estimated Time**: 3 hours
**Status**: ⏳ Pending

#### Tasks

**🔴 RED: 에이전트 역할 명세**
- [ ] **Spec 2.1**: 각 에이전트 역할 문서 작성
  - File(s): `docs/plans/AGENT_ROLES.md`
  - Expected: 5개 에이전트의 역할, 입력, 출력, 제약사항 명세
  - Details:
    - Architecture Agent: ADR 작성, 컴포넌트 분리 설계
    - Test Engineer Agent: Vitest 설정, 테스트 코드 생성
    - Security Agent: OWASP 체크, API Key 검증
    - Refactor Agent: 코드 분리, 중복 제거
    - Integration Specialist: Spline/Gemini/Spring Boot 통합

- [ ] **Spec 2.2**: 에이전트 테스트 시나리오 작성
  - File(s): `docs/plans/AGENT_TEST_SCENARIOS.md`
  - Expected: 각 에이전트당 3개 이상 테스트 케이스
  - Details: "입력 → 에이전트 동작 → 기대 출력" 형식

**🟢 GREEN: YAML 파일 생성**
- [ ] **Task 2.3**: Architecture Agent 정의
  - File(s): `.claude/agents/architecture-agent.yml`
  - Goal: 구조 설계 전문 에이전트 생성
  - Details:
    ```yaml
    name: architecture-agent
    description: 기능 구현 전 아키텍처 설계 및 기존 구조 분석
    tools: [Read, Glob, Grep, Write]
    workflow:
      - 기존 코드베이스 구조 분석
      - 새 기능이 들어갈 위치 결정
      - 레이어 간 책임 분리 확인
      - ADR 문서 작성
    constraints:
      - 코드 작성 금지 (설계만)
      - 반드시 ADR 형식 준수
      - 기존 패턴과 일관성 유지
    output: ADR 문서 + 폴더 구조 제안
    ```

- [ ] **Task 2.4**: Test Engineer Agent 정의
  - File(s): `.claude/agents/test-engineer-agent.yml`
  - Goal: 테스트 전략 수립 및 테스트 코드 작성
  - Details:
    ```yaml
    name: test-engineer-agent
    description: 테스트 전략 수립, Vitest 설정, 테스트 코드 작성
    tools: [Read, Write, Edit, Bash]
    workflow:
      - 테스트 대상 코드 분석
      - Test Pyramid 기반 전략 수립
      - 테스트 코드 작성 (AAA 패턴)
      - 커버리지 측정 및 리포트
    constraints:
      - TDD 원칙 준수 (Red-Green-Refactor)
      - 단위 테스트 70%, 통합 20%, E2E 10%
      - 커버리지 목표: 비즈니스 로직 80% 이상
    output: 테스트 파일 + 커버리지 리포트
    ```

- [ ] **Task 2.5**: Security Review Agent 정의
  - File(s): `.claude/agents/security-agent.yml`
  - Goal: 보안 취약점 검사 및 개선 제안
  - Details:
    ```yaml
    name: security-agent
    description: OWASP Top 10 기반 보안 검사, API Key 보호, 입력 검증
    tools: [Read, Grep, Write]
    checklist:
      - API Key 하드코딩 검사
      - 입력 검증 누락 확인
      - XSS/CSRF 취약점
      - SQL Injection (백엔드 추가 시)
      - 민감정보 로그 노출
      - Rate Limiting 구현 여부
    constraints:
      - 코드 수정 금지 (리포트만)
      - OWASP Top 10 기준 준수
      - 개선 코드 예시 제공
    output: 보안 리포트 + 개선 제안 코드
    ```

- [ ] **Task 2.6**: Refactor Agent 정의
  - File(s): `.claude/agents/refactor-agent.yml`
  - Goal: 기능 유지하며 코드 구조 개선
  - Details:
    ```yaml
    name: refactor-agent
    description: 기능 변경 없이 코드 품질 개선 (중복 제거, 네이밍, 구조 최적화)
    tools: [Read, Edit, Bash]
    workflow:
      - 리팩터링 대상 코드 분석
      - 테스트 실행 (기준선 확인)
      - 코드 개선 (작은 단위로)
      - 테스트 재실행 (통과 확인)
      - 커밋
    constraints:
      - 기능 변경 절대 금지
      - 모든 테스트 계속 통과해야 함
      - 한 번에 하나씩 개선
      - 각 개선마다 커밋
    output: 개선된 코드 + 리팩터링 리포트
    ```

- [ ] **Task 2.7**: Integration Specialist Agent 정의
  - File(s): `.claude/agents/integration-specialist-agent.yml`
  - Goal: 외부 시스템 통합 및 디버깅
  - Details:
    ```yaml
    name: integration-specialist-agent
    description: Spline/Gemini/Spring Boot 통합, CORS, WebSocket, 에러 핸들링
    tools: [Read, Write, Edit, Bash, WebFetch]
    workflow:
      - 통합 대상 시스템 분석
      - 통합 지점 설계
      - API 클라이언트 구현
      - 에러 핸들링 및 재시도 로직
      - 통합 테스트 작성
    constraints:
      - 각 통합은 독립적으로 테스트 가능
      - Exponential Backoff 재시도 구현
      - CORS/인증 설정 명확히 문서화
    output: 통합 코드 + 통합 테스트 + 문서
    ```

**🔵 REFACTOR: 에이전트 정의 개선**
- [ ] **Task 2.8**: 에이전트 간 중복 제거
  - Files: 모든 .yml 파일 검토
  - Goal: 공통 패턴 추출, 명확한 역할 분리
  - Checklist:
    - [ ] 역할 중복 없음
    - [ ] 도구 사용 명확
    - [ ] 제약사항 구체적

#### Quality Gate ✋

**Agent Definition Quality**:
- [ ] 5개 에이전트 모두 생성됨
- [ ] YAML 문법 오류 없음
- [ ] 필수 필드 모두 포함 (name, description, tools, workflow, constraints, output)
- [ ] 역할 간 중복 없음

**Documentation**:
- [ ] 각 에이전트 역할 명확히 문서화
- [ ] 테스트 시나리오 3개 이상 작성

**Validation Commands**:
```bash
# YAML 문법 검증
npx js-yaml .claude/agents/*.yml

# 파일 존재 확인
ls -la .claude/agents/

# Git 추가
git add .claude/agents/
git status
```

**Manual Test Checklist**:
- [ ] Architecture Agent에게 "Stats 컴포넌트 분리 설계" 요청 → ADR 생성
- [ ] Test Engineer Agent에게 "getBarColor 테스트 작성" 요청 → Vitest 파일 생성
- [ ] Security Agent에게 "API Key 검증" 요청 → 보안 리포트 생성
- [ ] Refactor Agent에게 "handleAction 함수 개선" 요청 → 중복 제거
- [ ] Integration Agent에게 "Gemini API 재시도 로직 추가" 요청 → Exponential Backoff 구현

---

### Phase 3: 클로드 스킬 문서 생성
**Goal**: Feature Planner, Code Review Checklist, Test Strategy 스킬 생성
**Estimated Time**: 2 hours
**Status**: ⏳ Pending

#### Tasks

**🔴 RED: 스킬 트리거 명세**
- [ ] **Spec 3.1**: 스킬 트리거 키워드 정의
  - File(s): `docs/plans/SKILL_TRIGGERS.md`
  - Expected: 각 스킬의 활성화 키워드 목록
  - Details:
    - Feature Planner: "plan", "planning", "phases", "breakdown"
    - Code Review Checklist: "review", "pr", "checklist"
    - Test Strategy: "test strategy", "test pyramid", "coverage"

- [ ] **Spec 3.2**: 스킬 동작 검증 시나리오
  - File(s): `docs/plans/SKILL_TEST_SCENARIOS.md`
  - Expected: 키워드 입력 → 스킬 활성화 → 체크리스트 강제 확인

**🟢 GREEN: Markdown 스킬 파일 생성**
- [ ] **Task 3.3**: Feature Planner Skill (이미 존재하므로 복사)
  - File(s): `.claude/skills/feature-planner.md`
  - Goal: 기존 SKILL.md를 표준 위치로 이동
  - Details:
    ```bash
    cp .claude/reference/SKILL.md .claude/skills/feature-planner.md
    ```

- [ ] **Task 3.4**: Code Review Checklist Skill 생성
  - File(s): `.claude/skills/code-review-checklist.md`
  - Goal: PR 리뷰 시 필수 확인 항목 강제
  - Details:
    ```markdown
    ---
    name: code-review-checklist
    description: PR에서 반드시 확인해야 할 항목 강제 적용
    triggers: ["review", "pr", "pull request", "checklist"]
    ---

    # Code Review Checklist

    ## 자동 체크 항목

    **기능**:
    - [ ] 요구사항 충족
    - [ ] 엣지 케이스 처리
    - [ ] 에러 핸들링

    **테스트**:
    - [ ] 단위 테스트 추가 (커버리지 80% 이상)
    - [ ] 통합 테스트 (필요 시)
    - [ ] 모든 테스트 통과

    **보안**:
    - [ ] 입력 검증
    - [ ] API Key 노출 없음
    - [ ] XSS/CSRF 방지

    **성능**:
    - [ ] N+1 쿼리 없음
    - [ ] 불필요한 리렌더링 없음
    - [ ] 메모리 누수 없음

    **코드 품질**:
    - [ ] 린트 에러 없음
    - [ ] 타입 체크 통과
    - [ ] 중복 코드 없음
    - [ ] 명확한 네이밍
    ```

- [ ] **Task 3.5**: Test Strategy Skill 생성
  - File(s): `.claude/skills/test-strategy.md`
  - Goal: 테스트 작성 시 Test Pyramid 원칙 강제
  - Details:
    ```markdown
    ---
    name: test-strategy
    description: 효율적인 테스트 작성 전략 (Test Pyramid)
    triggers: ["test strategy", "test pyramid", "coverage"]
    ---

    # Test Strategy

    ## Test Pyramid

    ```
          ▲
         /E2E\      10% (느림, 최소한)
        /─────\
       /Integ.\    20% (중간 속도)
      /────────\
     /  Unit    \  70% (빠름, 많이)
    /────────────\
    ```

    ## 작성 원칙

    **Unit Tests (70%)**:
    - 비즈니스 로직 중심
    - 빠른 실행 (< 100ms)
    - 외부 의존성 모킹

    **Integration Tests (20%)**:
    - 컴포넌트 간 상호작용
    - API 호출 검증
    - 데이터베이스 연동

    **E2E Tests (10%)**:
    - 핵심 사용자 시나리오만
    - 느리므로 최소화
    - CI에서 실행

    ## Anti-Patterns (금지)
    - ❌ 모든 코드에 E2E 테스트
    - ❌ Private 메서드 테스트
    - ❌ 단순 getter/setter 테스트
    ```

- [ ] **Task 3.6**: Security Checklist Skill 생성
  - File(s): `.claude/skills/security-checklist.md`
  - Goal: 보안 관련 코드 작성 시 필수 검증
  - Details:
    ```markdown
    ---
    name: security-checklist
    description: 보안 체크리스트 자동 적용
    triggers: ["security", "api", "auth", "authentication"]
    ---

    # Security Checklist

    ## 입력 검증
    - [ ] 모든 외부 입력 검증
    - [ ] 화이트리스트 기반
    - [ ] 타입/길이/형식 체크

    ## 인증/인가
    - [ ] 인증 필요 엔드포인트 보호
    - [ ] 권한 체크 (RBAC)
    - [ ] 세션/토큰 관리

    ## 민감정보
    - [ ] 비밀번호 해싱 (bcrypt)
    - [ ] 환경변수로 시크릿 관리
    - [ ] 로그에 민감정보 제외

    ## API 보안
    - [ ] Rate Limiting
    - [ ] CORS 설정
    - [ ] HTTPS 강제

    ## 자동 검사 트리거
    - 외부 API 연동 시
    - 사용자 입력 처리 시
    - 인증/인가 코드 작성 시
    ```

**🔵 REFACTOR: 스킬 문서 개선**
- [ ] **Task 3.7**: 스킬 간 일관성 확보
  - Files: 모든 .md 파일 검토
  - Checklist:
    - [ ] Frontmatter 형식 통일
    - [ ] 트리거 키워드 명확
    - [ ] 예시 코드 포함

#### Quality Gate ✋

**Skill Quality**:
- [ ] 4개 스킬 모두 생성됨
- [ ] Frontmatter 형식 올바름
- [ ] 트리거 키워드 명확히 정의
- [ ] 체크리스트 구체적

**Documentation**:
- [ ] 각 스킬 사용 예시 포함
- [ ] 트리거 시나리오 문서화

**Validation Commands**:
```bash
# Markdown 검증
npx markdownlint .claude/skills/*.md

# Frontmatter 파싱 테스트 (gray-matter 설치 시)
npx gray-matter .claude/skills/*.md

# 파일 존재 확인
ls -la .claude/skills/
```

**Manual Test Checklist**:
- [ ] "planning 사용자 인증" 입력 → Feature Planner 활성화
- [ ] "review PR" 입력 → Code Review Checklist 표시
- [ ] "test strategy for Stats" 입력 → Test Pyramid 가이드 제공
- [ ] "security check API" 입력 → Security Checklist 강제

---

### Phase 4: MCP 서버 설치 및 설정
**Goal**: Context7 MCP 서버 설치 및 프로젝트 문서 인덱싱
**Estimated Time**: 1.5 hours
**Status**: ⏳ Pending

#### Tasks

**🔴 RED: MCP 동작 명세**
- [ ] **Spec 4.1**: Context7 기대 동작 정의
  - File(s): `docs/plans/MCP_EXPECTED_BEHAVIOR.md`
  - Expected: "TECHNICAL_GUIDE.md에서 테스트 전략 알려줘" → 정확한 섹션 반환
  - Details: 프로젝트 내 모든 .md 파일 인덱싱 확인

- [ ] **Spec 4.2**: MCP 설정 파일 스키마
  - File(s): `.claude/mcp-configs/context7-config.md`
  - Expected: 설치 방법, 설정 예시, 트러블슈팅

**🟢 GREEN: MCP 서버 설치**
- [ ] **Task 4.3**: Context7 MCP 서버 설정
  - File(s): Claude Code 설정 (CLI 또는 config 파일)
  - Goal: Context7 MCP 활성화
  - Commands:
    ```bash
    # MCP 서버는 Claude Code 설정에서 활성화
    # 설정 파일 위치 확인 필요 (플랫폼별 다름)

    # macOS: ~/Library/Application Support/Claude/claude_desktop_config.json
    # Windows: %APPDATA%/Claude/claude_desktop_config.json
    # Linux: ~/.config/Claude/claude_desktop_config.json
    ```

- [ ] **Task 4.4**: Context7 설정 문서 작성
  - File(s): `.claude/mcp-configs/context7-setup.md`
  - Goal: 설치 가이드 및 사용법
  - Details:
    ```markdown
    # Context7 MCP Setup

    ## Installation

    Context7 MCP는 Claude Code에 기본 내장되어 있습니다.

    ## Configuration

    프로젝트의 문서 파일들이 자동으로 인덱싱됩니다:
    - docs/**/*.md
    - .claude/**/*.md
    - *.md (루트 레벨)

    ## Usage

    "TECHNICAL_GUIDE.md에서 테스트 전략 섹션 알려줘"와 같이
    자연어로 문서 내용을 질의할 수 있습니다.

    ## Troubleshooting

    - 문서가 인식되지 않는 경우: Claude Code 재시작
    - 오래된 정보 반환 시: 인덱스 재생성 (설정에서)
    ```

- [ ] **Task 4.5**: 추가 유용한 MCP 서버 조사 및 문서화
  - File(s): `.claude/mcp-configs/recommended-mcp-servers.md`
  - Goal: 프로젝트에 유용할 수 있는 다른 MCP 서버 리스트
  - Details:
    - **filesystem**: 파일 시스템 접근 (기본 제공)
    - **github**: GitHub API 통합
    - **postgres**: 데이터베이스 쿼리 (백엔드 추가 시)
    - **slack**: 알림 통합 (팀 협업 시)

**🔵 REFACTOR: MCP 설정 문서 개선**
- [ ] **Task 4.6**: 설정 예시 및 스크린샷 추가
  - Files: context7-setup.md 개선
  - Checklist:
    - [ ] 설정 JSON 예시
    - [ ] 동작 확인 방법
    - [ ] 일반적인 오류 해결

#### Quality Gate ✋

**MCP Integration**:
- [ ] Context7 MCP 서버 활성화됨
- [ ] 프로젝트 문서 인덱싱 확인
- [ ] 문서 쿼리 테스트 통과

**Documentation**:
- [ ] 설치 가이드 작성
- [ ] 트러블슈팅 섹션 포함
- [ ] 추가 MCP 서버 조사 완료

**Validation Commands**:
```bash
# Claude Code MCP 상태 확인 (명령어는 플랫폼별로 다를 수 있음)
# 일반적으로 Claude Code UI에서 확인

# 문서 파일 확인
find . -name "*.md" -type f
```

**Manual Test Checklist**:
- [ ] Claude Code에 "TECHNICAL_GUIDE.md에서 테스트 전략 알려줘" 입력
- [ ] 정확한 섹션 내용 반환 확인
- [ ] "guideline.md에서 Feature Planner 설명 알려줘" 입력 확인
- [ ] 문서 업데이트 후 자동 재인덱싱 확인

---

### Phase 5: 통합 검증 및 문서화
**Goal**: 모든 컴포넌트 통합 테스트 및 워크플로우 문서 작성
**Estimated Time**: 2 hours
**Status**: ⏳ Pending

#### Tasks

**🔴 RED: 통합 시나리오 명세**
- [ ] **Spec 5.1**: End-to-End 워크플로우 정의
  - File(s): `docs/workflows/COMPLETE_FEATURE_WORKFLOW.md`
  - Expected: 신규 기능 개발 시 에이전트/스킬 사용 순서
  - Details:
    ```
    1. Feature Planner Skill로 계획 수립
    2. Architecture Agent로 구조 설계 (ADR 생성)
    3. Test Engineer Agent로 테스트 먼저 작성
    4. Implementation (수동 또는 다른 에이전트)
    5. Security Agent로 보안 검증
    6. Refactor Agent로 코드 개선
    7. Code Review Checklist로 최종 검토
    ```

- [ ] **Spec 5.2**: 통합 테스트 시나리오
  - File(s): `docs/plans/INTEGRATION_TEST_SCENARIOS.md`
  - Expected: 실제 기능 개발 흐름 시뮬레이션
  - Details: "사용자 프로필 시스템 추가"를 예시로 전체 워크플로우 실행

**🟢 GREEN: 통합 테스트 실행**
- [ ] **Task 5.3**: 예시 기능으로 전체 워크플로우 테스트
  - Scenario: "사용자 프로필 저장 기능 추가"
  - Steps:
    1. "/feature-planner 사용자 프로필 저장" 실행 → 계획 문서 생성 확인
    2. Architecture Agent 호출 → ADR 문서 생성 확인
    3. Test Engineer Agent 호출 → 테스트 파일 생성 확인
    4. Security Agent 호출 → 보안 리포트 확인
    5. Refactor Agent 호출 (필요 시) → 코드 개선 확인
  - Expected: 각 단계가 순차적으로 완료되고 문서 생성됨

- [ ] **Task 5.4**: 에이전트 간 협업 테스트
  - Scenario: "Architecture Agent의 ADR을 Test Engineer Agent가 참조"
  - Steps:
    1. Architecture Agent로 설계 문서 생성
    2. Test Engineer Agent에게 "위 설계를 기반으로 테스트 작성" 요청
    3. Context7 MCP로 ADR 내용 자동 참조 확인
  - Expected: 에이전트 간 컨텍스트 공유 성공

- [ ] **Task 5.5**: 스킬 자동 트리거 테스트
  - Scenarios:
    1. "plan new authentication system" 입력 → Feature Planner 자동 활성화
    2. "review this code" 입력 → Code Review Checklist 자동 표시
    3. "test coverage strategy" 입력 → Test Strategy 가이드 제공
  - Expected: 키워드 감지 및 스킬 자동 실행

**🔵 REFACTOR: 워크플로우 문서 개선**
- [ ] **Task 5.6**: 통합 워크플로우 문서 작성
  - File(s): `docs/workflows/AI_DEVELOPMENT_WORKFLOW.md`
  - Goal: 개발자 온보딩용 종합 가이드
  - Contents:
    - 에이전트 선택 플로차트
    - 스킬 사용 타이밍
    - 일반적인 실수 및 해결 방법
    - 실전 예시 3개 이상

- [ ] **Task 5.7**: README.md 업데이트
  - File(s): 프로젝트 루트 `README.md` (없으면 생성)
  - Goal: AI 인프라 사용법 추가
  - Sections:
    - AI Development Infrastructure 섹션 추가
    - 서브에이전트 목록 및 사용법
    - 스킬 트리거 키워드 요약
    - MCP 서버 활성화 방법

**📝 Task 5.8**: 최종 체크리스트 작성
- [ ] **File**: `docs/AI_INFRASTRUCTURE_CHECKLIST.md`
- [ ] **Contents**:
  ```markdown
  # AI Infrastructure Setup Checklist

  ## 서브에이전트
  - [ ] architecture-agent.yml 존재 및 테스트 완료
  - [ ] test-engineer-agent.yml 존재 및 테스트 완료
  - [ ] security-agent.yml 존재 및 테스트 완료
  - [ ] refactor-agent.yml 존재 및 테스트 완료
  - [ ] integration-specialist-agent.yml 존재 및 테스트 완료

  ## 스킬
  - [ ] feature-planner.md 존재 및 트리거 테스트 완료
  - [ ] code-review-checklist.md 존재 및 사용 확인
  - [ ] test-strategy.md 존재 및 가이드 제공 확인
  - [ ] security-checklist.md 존재 및 자동 적용 확인

  ## MCP
  - [ ] Context7 MCP 활성화
  - [ ] 문서 인덱싱 확인
  - [ ] 문서 쿼리 테스트 통과

  ## 문서
  - [ ] AI_DEVELOPMENT_WORKFLOW.md 작성
  - [ ] README.md 업데이트
  - [ ] 각 폴더 README.md 존재
  ```

#### Quality Gate ✋

**Integration Testing**:
- [ ] 전체 워크플로우 테스트 통과
- [ ] 에이전트 간 협업 동작 확인
- [ ] 스킬 자동 트리거 정상 작동

**Documentation**:
- [ ] 종합 워크플로우 문서 작성
- [ ] README.md 업데이트
- [ ] 최종 체크리스트 작성

**Code Quality**:
- [ ] 모든 Markdown 파일 린트 통과
- [ ] YAML 파일 문법 오류 없음
- [ ] Git 커밋 메시지 명확

**Validation Commands**:
```bash
# 전체 파일 구조 확인
tree .claude/

# Markdown 검증
npx markdownlint .claude/**/*.md docs/**/*.md

# YAML 검증
npx js-yaml .claude/agents/*.yml

# Git 상태 확인
git status
git log --oneline -10
```

**Manual Test Checklist**:
- [ ] 신규 기능 개발 시뮬레이션 (사용자 프로필 저장) 성공
- [ ] Architecture → Test → Security → Refactor 순차 실행 확인
- [ ] 스킬 키워드 자동 감지 확인
- [ ] Context7 MCP 문서 참조 성공
- [ ] 모든 README 문서 가독성 확인

---

## ⚠️ Risk Assessment

| Risk | Probability | Impact | Mitigation Strategy |
|------|-------------|--------|---------------------|
| Claude Code가 YAML 형식 에이전트를 인식하지 못함 | Medium | High | 공식 문서 참조, Markdown 형식으로 대체 가능 |
| MCP 서버 설정 실패 | Low | Medium | Context7 대신 수동 문서 참조로 대체 |
| 스킬 트리거가 자동으로 동작하지 않음 | Medium | Medium | 수동으로 스킬 호출, 문서에 명시적 사용법 추가 |
| 에이전트 간 컨텍스트 공유 실패 | Low | Medium | 명시적으로 이전 출력 참조하도록 가이드 |
| 통합 테스트 시나리오 실패 | Medium | Low | 각 컴포넌트 독립 테스트 우선, 통합은 선택적 |

---

## 🔄 Rollback Strategy

### If Phase 1 Fails
**Steps to revert**:
- `.claude/agents/`, `.claude/skills/`, `.claude/mcp-configs/` 폴더 삭제
- Git reset: `git reset --hard HEAD`

### If Phase 2 Fails
**Steps to revert**:
- `.claude/agents/*.yml` 파일 삭제
- Phase 1 상태로 복구

### If Phase 3 Fails
**Steps to revert**:
- `.claude/skills/*.md` 파일 삭제 (feature-planner.md 제외)
- Phase 2 상태로 복구

### If Phase 4 Fails
**Steps to revert**:
- MCP 설정 비활성화
- `.claude/mcp-configs/*.md` 파일 삭제
- Phase 3 상태로 복구 (MCP 없이도 작동)

### If Phase 5 Fails
**Steps to revert**:
- 통합 문서만 삭제
- Phase 4까지는 유효하므로 개별 컴포넌트 사용 가능

---

## 📊 Progress Tracking

### Completion Status
- **Phase 1**: ⏳ 0%
- **Phase 2**: ⏳ 0%
- **Phase 3**: ⏳ 0%
- **Phase 4**: ⏳ 0%
- **Phase 5**: ⏳ 0%

**Overall Progress**: 0% complete

### Time Tracking
| Phase | Estimated | Actual | Variance |
|-------|-----------|--------|----------|
| Phase 1 | 1 hour | - | - |
| Phase 2 | 3 hours | - | - |
| Phase 3 | 2 hours | - | - |
| Phase 4 | 1.5 hours | - | - |
| Phase 5 | 2 hours | - | - |
| **Total** | 9.5 hours | - | - |

---

## 📝 Notes & Learnings

### Implementation Notes
- [Phase별 발견사항 기록]

### Blockers Encountered
- **Blocker**: [설명] → **Resolution**: [해결 방법]

### Improvements for Future Plans
- [다음에 개선할 점]

---

## 📚 References

### Documentation
- [.claude/reference/guideline.md](./.claude/reference/guideline.md) - AI 개발 도구 종합 가이드
- [.claude/reference/SKILL.md](./.claude/reference/SKILL.md) - Feature Planner 스킬
- [.claude/reference/plan-tamplate.md](./.claude/reference/plan-tamplate.md) - TDD 계획 템플릿
- [docs/TECHNICAL_GUIDE.md](../TECHNICAL_GUIDE.md) - 프로젝트 기술 문서

### External Resources
- [Claude Code Documentation](https://claude.com/claude-code)
- [MCP Servers List](https://github.com/anthropics/model-context-protocol/tree/main/servers)
- [YAML Specification](https://yaml.org/spec/)

---

## ✅ Final Checklist

**Before marking plan as COMPLETE**:
- [ ] 모든 Phase Quality Gate 통과
- [ ] 5개 서브에이전트 동작 검증
- [ ] 4개 스킬 트리거 테스트 완료
- [ ] Context7 MCP 문서 쿼리 성공
- [ ] 통합 워크플로우 문서 작성
- [ ] README.md 업데이트
- [ ] Git 커밋 및 푸시 완료
- [ ] 팀원에게 사용법 공유 (해당 시)

---

**Plan Status**: 🔄 In Progress
**Next Action**: Phase 1 - 폴더 구조 및 표준 정의 시작
**Blocked By**: None
