# AI 개발 도구 저장소 가이드라인

## 📌 저장소 목적

이 저장소는 **실제 프로젝트에서 즉시 사용 가능한 AI 개발 도구들의 중앙 저장소**입니다.

### 사용 시나리오
- 새 프로젝트 시작 시 필요한 서브에이전트/스킬 파일 복사
- MCP/플러그인 설정 가이드라인 참고
- 검증된 템플릿 기반으로 프로젝트 구조 설계
- AI 협업 워크플로우 표준화

---

## 📁 현재 저장소 구조

```
.
├── SKILL.md              # Feature Planner 스킬 정의
├── plan-tamplate.md      # TDD 기반 구현 계획 템플릿
├── CLAUDE.md             # Claude Code를 위한 컨텍스트 문서
└── guideline.md          # 이 파일 - 종합 가이드라인
```

### 향후 확장 계획
```
.
├── skills/               # 클로드 스킬 모음
│   ├── feature-planner/
│   ├── code-review-checklist/
│   └── security-checklist/
├── subagents/           # 서브에이전트 정의 모음
│   ├── architecture-agent/
│   ├── test-engineer-agent/
│   └── debug-agent/
├── templates/           # 프로젝트 템플릿 모음
│   ├── adr-template.md
│   ├── design-doc-template.md
│   └── postmortem-template.md
├── mcp-configs/         # MCP 서버 설정 예제
│   ├── context7-setup.md
│   └── custom-mcp-guide.md
└── workflows/           # 통합 워크플로우 예제
    └── feature-development-flow.md
```

---

## 🎯 핵심 파일 가이드

### 1. SKILL.md - Feature Planner 스킬

**목적**: AI가 스파게티 코드를 만드는 것을 방지하는 구조화된 개발 계획 스킬

**핵심 원칙**:
- TDD(Test-Driven Development) 강제
- 단계별 품질 게이트 검증
- 사용자 승인 후 실행
- 1-4시간 단위 작은 단계로 분할

**언제 사용하나**:
- 기능 추가가 "한 파일 수정"을 넘어설 때
- 여러 컴포넌트/모듈이 연관될 때
- 복잡도가 높아 계획 없이 시작하면 위험할 때

**워크플로우**:
```
1. 요구사항 분석
   ↓
2. TDD 기반 단계 분해 (3-7 단계)
   ↓
3. plan-template.md 기반 문서 생성
   ↓
4. 사용자 승인 (AskUserQuestion)
   ↓
5. docs/plans/PLAN_<기능명>.md 생성
```

**실무 활용 팁**:
- 단계 크기 가이드:
  - 소규모: 2-3단계 (3-6시간)
  - 중규모: 4-5단계 (8-15시간)
  - 대규모: 6-7단계 (15-25시간)
- 각 단계는 독립적으로 롤백 가능해야 함
- 품질 게이트를 통과하지 못하면 다음 단계로 절대 진행 금지

### 2. plan-tamplate.md - TDD 구현 계획 템플릿

**목적**: 모든 개발 계획이 동일한 구조와 품질 기준을 따르도록 강제

**핵심 구조**:

#### Phase 구조 (각 단계마다 반복)
```
🔴 RED: 실패하는 테스트 먼저 작성
  ├─ 테스트 시나리오 정의
  ├─ 테스트 코드 작성
  └─ 테스트 실행 → 실패 확인

🟢 GREEN: 테스트를 통과시키는 최소 코드 작성
  ├─ 최소한의 구현
  └─ 테스트 실행 → 통과 확인

🔵 REFACTOR: 테스트는 유지하며 코드 품질 개선
  ├─ 중복 제거
  ├─ 네이밍 개선
  ├─ 구조 최적화
  └─ 테스트 실행 → 여전히 통과 확인
```

#### 품질 게이트 체크리스트
모든 단계 완료 후 반드시 확인:

**TDD 준수** (가장 중요):
- [ ] 테스트가 구현 코드보다 먼저 작성됨
- [ ] Red → Green → Refactor 사이클 준수
- [ ] 커버리지 목표 달성 (비즈니스 로직 ≥80%)

**빌드 & 테스트**:
- [ ] 빌드/컴파일 성공
- [ ] 모든 테스트 통과 (100%)
- [ ] 불안정한 테스트 없음 (3회 이상 실행)

**코드 품질**:
- [ ] 린트 에러 없음
- [ ] 포맷팅 일관성
- [ ] 타입 체크 통과 (해당 시)

**보안 & 성능**:
- [ ] 새로운 보안 취약점 없음
- [ ] 성능 저하 없음
- [ ] 리소스 누수 없음

**문서화**:
- [ ] 복잡한 로직에 주석 추가
- [ ] API 문서 업데이트

**실무 활용 팁**:
- 템플릿 복사 후 프로젝트별로 커스터마이징
- 검증 명령어 섹션을 프로젝트 환경에 맞게 수정
- 커버리지 목표는 프로젝트 성격에 따라 조정 (너무 높으면 비효율)

---

## 🤖 서브에이전트 가이드

서브에이전트는 **특정 역할에 특화된 AI 에이전트**로, 복잡한 작업을 역할별로 분담합니다.

### 1. Architecture / Design Agent
**역할**:
- 기능 추가 전 구조 설계
- 폴더 구조, 레이어 책임 정의
- 기존 구조와의 충돌 지점 분석

**언제 사용**:
- 기능이 "한 파일 수정"을 넘는 순간
- AI가 기존 구조를 망가뜨릴 것 같을 때
- 새로운 모듈/레이어 추가 시

**실무 포인트**:
```
⭐ 가장 중요한 서브에이전트
설계 없이 Implementation Agent 단독 사용 금지
ADR(Architecture Decision Record)과 함께 사용 시 효과 극대화
```

**에이전트 정의 예시**:
```yaml
name: architecture-agent
description: 기능 구현 전 아키텍처 설계 및 기존 구조 분석
tools: [Read, Glob, Grep, Task]
workflow:
  1. 기존 코드베이스 구조 분석
  2. 새 기능이 들어갈 위치 결정
  3. 레이어 간 책임 분리 확인
  4. 충돌 가능성 있는 지점 식별
  5. 설계안 문서화 (ADR 형식)
output: 설계 문서 + 파일 구조 제안
```

### 2. Implementation Agent (Feature Builder)
**역할**:
- 설계된 계획을 기준으로 코드 구현
- "지금 단계에서 해야 할 코드만" 작성
- 과도한 기능 추가 방지

**언제 사용**:
- 계획/설계가 완료된 후
- 바로 코드 작성이 필요할 때

**주의사항**:
```
❌ 설계 없이 단독 사용 금지
✅ Planner/Architecture Agent 이후에만 사용
```

**에이전트 정의 예시**:
```yaml
name: implementation-agent
description: 승인된 계획에 따라 코드 구현
tools: [Read, Edit, Write, Bash]
constraints:
  - 계획서에 명시된 기능만 구현
  - TDD 사이클 준수 (Red-Green-Refactor)
  - 한 단계씩 구현 (phase-by-phase)
workflow:
  1. 현재 phase의 RED 단계 실행 (테스트 작성)
  2. GREEN 단계 실행 (최소 구현)
  3. REFACTOR 단계 실행 (코드 개선)
  4. 품질 게이트 검증
  5. 다음 phase로 진행
```

### 3. Code Review Agent
**역할**:
- PR 리뷰 자동화
- 중복 코드, 복잡도, 스타일 불일치 탐지
- "이 코드의 냄새"를 말로 설명

**언제 사용**:
- 커밋 전 / PR 전
- AI가 만든 코드 신뢰도 검증 필요 시
- 사람 리뷰 전 1차 필터링

**실무 포인트**:
```
사람 리뷰 전에 돌리면 리뷰 시간 50% 단축
SonarQube, Snyk 같은 도구와 함께 사용
```

**에이전트 정의 예시**:
```yaml
name: code-review-agent
description: 코드 품질 자동 리뷰 및 개선 제안
tools: [Read, Grep, Bash]
checklist:
  - 중복 코드 탐지
  - 복잡도 분석 (Cyclomatic Complexity)
  - 네이밍 컨벤션 검사
  - 보안 취약점 체크
  - 테스트 커버리지 확인
  - 성능 병목 지점 식별
output: 리뷰 코멘트 + 개선 제안
```

### 4. Refactor Agent
**역할**:
- 기능 유지 + 구조 개선
- 중복 제거, 책임 분리, 네이밍 개선

**언제 사용**:
- 기능은 작동하지만 코드가 지저분할 때
- 스파게티 코드 초기 단계
- 기술 부채 정리 시

**에이전트 정의 예시**:
```yaml
name: refactor-agent
description: 기능 변경 없이 코드 구조 개선
tools: [Read, Edit, Bash]
constraints:
  - 기능 변경 절대 금지
  - 모든 테스트는 계속 통과해야 함
  - 작은 단위로 커밋 (atomic commits)
workflow:
  1. 리팩터링 대상 식별
  2. 테스트 실행 (기준선 확인)
  3. 코드 개선
  4. 테스트 재실행 (여전히 통과해야 함)
  5. 커밋
```

### 5. Debug / Error Analysis Agent
**역할**:
- 에러 로그, 스택트레이스 분석
- 재현 시나리오 추정
- 원인 후보 정리

**언제 사용**:
- "왜 깨졌는지 모르겠다" 상태
- 운영/테스트 환경 에러 분석
- 간헐적 버그 추적

**에이전트 정의 예시**:
```yaml
name: debug-agent
description: 에러 분석 및 원인 추적
tools: [Read, Grep, Bash, WebFetch]
workflow:
  1. 에러 로그 수집 및 분석
  2. 스택트레이스에서 관련 코드 추적
  3. 재현 시나리오 가설 수립
  4. 원인 후보 정리 (가능성 순)
  5. 수정 방안 제시
output: 디버깅 리포트 + 수정 제안
```

### 6. Test Engineer Agent
**역할**:
- 테스트 시나리오 설계
- 단위/통합/E2E 테스트 코드 생성
- 커버리지 기준 점검

**언제 사용**:
- 테스트가 항상 밀릴 때
- QA 리소스 부족할 때
- 레거시 코드에 테스트 추가 시

**에이전트 정의 예시**:
```yaml
name: test-engineer-agent
description: 테스트 전략 수립 및 테스트 코드 작성
tools: [Read, Write, Bash]
test-pyramid:
  - Unit Tests: 70% (빠름, 많이)
  - Integration Tests: 20% (중간)
  - E2E Tests: 10% (느림, 핵심만)
workflow:
  1. 테스트 대상 코드 분석
  2. 테스트 시나리오 설계
  3. 테스트 코드 작성 (AAA 패턴)
  4. 커버리지 측정
  5. 부족한 영역 추가 테스트
```

### 7. DevOps / Infra Agent
**역할**:
- CI/CD 파이프라인 설계
- 배포 전략 (blue-green, canary, rollback)
- 환경 변수/설정 구조 점검

**언제 사용**:
- 배포 자동화 구축 시
- 인프라 변경 전 검증
- 장애 대응 프로세스 설계

**에이전트 정의 예시**:
```yaml
name: devops-agent
description: CI/CD 및 인프라 자동화
tools: [Read, Write, Bash]
workflow:
  1. 현재 배포 프로세스 분석
  2. 병목 지점 식별
  3. 자동화 파이프라인 설계
  4. 롤백 전략 수립
  5. 모니터링 설정
```

### 8. Security Review Agent
**역할**:
- 입력 검증/인가/권한 체크
- 민감 정보 노출 가능성 점검
- OWASP Top 10 기준 리뷰

**언제 사용**:
- 외부 API 연동 시
- 인증/결제/알림/웹훅 구현 시
- 사용자 입력 처리 코드 작성 시

**에이전트 정의 예시**:
```yaml
name: security-agent
description: 보안 취약점 검사 및 개선 제안
tools: [Read, Grep, Bash]
checklist:
  - SQL Injection 방지
  - XSS 방지
  - CSRF 토큰 검증
  - 인증/인가 체크
  - 민감정보 암호화
  - Rate Limiting
  - Input Validation
output: 보안 리포트 + 개선 코드
```

### 9. Documentation Agent
**역할**:
- README, ADR, 주석 자동 생성
- "왜 이렇게 했는지" 문서화
- API 문서 업데이트

**언제 사용**:
- 기능 구현 완료 후
- 협업/인수인계 대비
- 복잡한 로직 설명 필요 시

**에이전트 정의 예시**:
```yaml
name: documentation-agent
description: 자동 문서화 및 주석 생성
tools: [Read, Write]
workflow:
  1. 변경된 코드 분석
  2. 핵심 로직 파악
  3. 적절한 수준의 주석 추가
  4. README 업데이트
  5. ADR 생성 (아키텍처 변경 시)
```

### 🔥 서브에이전트 실전 조합

**가장 현실적인 워크플로우**:
```
1. Feature Planner (계획 수립)
   ↓
2. Architecture Agent (구조 설계)
   ↓
3. Implementation Agent (구현)
   ↓
4. Code Review Agent (리뷰)
   ↓
5. Test Engineer Agent (테스트 보강)
   ↓
6. (문제 발생 시) Debug Agent (디버깅)
   ↓
7. Documentation Agent (문서화)
```

---

## ⚡ 스킬 가이드

스킬은 **AI 행동을 제약하는 규칙**입니다. 서브에이전트가 "누가 할 것인가"라면, 스킬은 "어떻게 할 것인가"입니다.

### 1. Feature Planner Skill
**(현재 저장소의 SKILL.md)**

**목적**: AI 스파게티 방지 핵심 스킬

**핵심 규칙**:
- 기능을 3-7개 단계로 강제 분할
- 각 단계마다 품질 게이트 강제
- 승인 → 실행 흐름 유지
- TDD 사이클 준수 강제

**실무 활용**:
```yaml
triggers:
  - "plan", "planning", "phases"
  - "breakdown", "strategy", "roadmap"
enforcement:
  - 단계별 체크리스트 강제
  - 품질 게이트 통과 전 다음 단계 진행 금지
  - 사용자 승인 없이 구현 시작 금지
```

### 2. Code Review Checklist Skill

**목적**: 리뷰 품질 균질화

**핵심 규칙**:
```yaml
name: code-review-checklist
description: PR에서 반드시 확인해야 할 항목 강제
checklist:
  기능:
    - [ ] 요구사항 충족
    - [ ] 엣지 케이스 처리
    - [ ] 에러 핸들링

  테스트:
    - [ ] 단위 테스트 추가
    - [ ] 통합 테스트 (필요 시)
    - [ ] 커버리지 기준 충족

  보안:
    - [ ] 입력 검증
    - [ ] 인증/인가 체크
    - [ ] 민감정보 노출 없음

  성능:
    - [ ] N+1 쿼리 없음
    - [ ] 불필요한 리렌더링 없음
    - [ ] 메모리 누수 없음

  코드 품질:
    - [ ] 중복 코드 없음
    - [ ] 네이밍 명확
    - [ ] 복잡도 낮음 (CC < 10)
```

### 3. Debugging Workflow Skill

**목적**: "찍어 맞추기 디버깅" 방지

**핵심 규칙**:
```yaml
name: debugging-workflow
description: 체계적 디버깅 프로세스 강제
workflow:
  1. 로그 수집:
     - 에러 메시지
     - 스택트레이스
     - 재현 조건

  2. 가설 수립:
     - 원인 후보 3개 이상 나열
     - 가능성 순으로 정렬

  3. 검증:
     - 가설별로 최소 재현 코드 작성
     - 로그 추가로 흐름 추적
     - 하나씩 검증

  4. 수정:
     - 원인 확정 후에만 코드 수정
     - 테스트로 재발 방지
     - 수정 전후 비교

constraints:
  - 무작정 코드 수정 금지
  - "될 때까지 시도" 금지
  - 원인 불명 시 수정 금지
```

### 4. Refactoring Rules Skill

**목적**: 대형 리팩터링 사고 방지

**핵심 규칙**:
```yaml
name: refactoring-rules
description: 안전한 리팩터링 프로세스
rules:
  1. 기능 변경 절대 금지:
     - 리팩터링은 구조 개선만
     - 새 기능 추가 금지
     - 버그 수정도 별도 커밋

  2. 테스트 유지:
     - 리팩터링 전 테스트 통과 확인
     - 리팩터링 중 테스트 계속 통과
     - 테스트 깨지면 즉시 되돌림

  3. 작은 단위 커밋:
     - 한 번에 하나씩 개선
     - 각 개선마다 커밋
     - 큰 변경은 여러 단계로 분할

  4. 롤백 가능성:
     - 언제든 이전 커밋으로 복귀 가능
     - 각 커밋은 독립적으로 의미 있어야 함
```

### 5. Test Strategy Skill

**목적**: 테스트 품질 ↑, 속도 ↓ 방지

**핵심 규칙**:
```yaml
name: test-strategy
description: 효율적인 테스트 작성 전략
test-pyramid:
  Unit Tests (70%):
    - 빠름 (< 100ms)
    - 많이
    - 격리됨
    - 비즈니스 로직 중심

  Integration Tests (20%):
    - 중간 속도 (< 1s)
    - 적당히
    - 컴포넌트 간 상호작용

  E2E Tests (10%):
    - 느림 (초~분)
    - 최소한
    - 핵심 사용자 시나리오만

anti-patterns:
  - 모든 코드에 E2E 테스트 작성 ❌
  - Private 메서드 테스트 ❌
  - 단순 getter/setter 테스트 ❌
  - 테스트를 위한 테스트 ❌

focus:
  - 비즈니스 로직 우선
  - 복잡한 조건문/루프
  - 외부 의존성 상호작용
  - 에러 처리 경로
```

### 6. Security Checklist Skill

**목적**: "AI는 보안을 모른다" 문제 해결

**핵심 규칙**:
```yaml
name: security-checklist
description: 보안 체크리스트 자동 적용
checklist:
  입력 검증:
    - [ ] 모든 외부 입력 검증
    - [ ] 화이트리스트 기반 검증
    - [ ] 타입/길이/형식 체크

  인증/인가:
    - [ ] 인증 필요 엔드포인트 보호
    - [ ] 권한 체크 (RBAC/ABAC)
    - [ ] 세션/토큰 관리

  민감정보:
    - [ ] 비밀번호 해싱 (bcrypt)
    - [ ] 환경변수로 시크릿 관리
    - [ ] 로그에 민감정보 제외

  API 보안:
    - [ ] Rate Limiting
    - [ ] CORS 설정
    - [ ] HTTPS 강제

  SQL/NoSQL:
    - [ ] Prepared Statement 사용
    - [ ] ORM 사용 (가능 시)
    - [ ] 동적 쿼리 최소화

  XSS/CSRF:
    - [ ] 출력 이스케이핑
    - [ ] CSP 헤더
    - [ ] CSRF 토큰

enforcement:
  - 외부 API 연동 시 자동 체크
  - 사용자 입력 처리 코드에서 자동 경고
```

### 7. CI/CD Planning Skill

**목적**: 배포 사고 예방

**핵심 규칙**:
```yaml
name: cicd-planning
description: 안전한 CI/CD 파이프라인 설계
pipeline-stages:
  1. 빌드:
     - 의존성 설치
     - 컴파일/번들링
     - 실패 시 즉시 중단

  2. 정적 분석:
     - 린트
     - 타입 체크
     - 보안 스캔 (Snyk)

  3. 테스트:
     - 단위 테스트
     - 통합 테스트
     - 커버리지 체크

  4. 빌드 아티팩트:
     - Docker 이미지
     - 배포 패키지

  5. 배포:
     - 스테이징 먼저
     - 스모크 테스트
     - 프로덕션 (승인 후)

failure-handling:
  - 어느 단계든 실패 시 즉시 중단
  - 알림 발송 (Slack/Email)
  - 자동 롤백 트리거

constraints:
  - 테스트 없이 배포 금지
  - 린트 에러 있으면 빌드 실패
  - 프로덕션 직접 배포 금지
```

### 8. Rollback & Failure Handling Skill

**목적**: 실서비스 필수 - 망했을 때 대응

**핵심 규칙**:
```yaml
name: rollback-handling
description: 실패 시나리오 및 복구 전략 강제
requirements:
  배포 전 필수 문서:
    - [ ] 롤백 절차 문서화
    - [ ] 데이터 마이그레이션 롤백 스크립트
    - [ ] 장애 감지 기준 정의
    - [ ] 담당자 연락처

rollback-strategy:
  1. 감지:
     - 에러율 임계값
     - 응답시간 증가
     - 사용자 신고

  2. 판단:
     - 심각도 분류 (P0/P1/P2)
     - 롤백 vs 긴급 패치 결정

  3. 실행:
     - 이전 버전으로 복구
     - DB 마이그레이션 되돌림
     - 캐시 초기화

  4. 사후:
     - Postmortem 작성
     - 재발 방지 대책

auto-rollback-conditions:
  - 에러율 > 5%
  - 응답시간 > 2x 평균
  - Health Check 실패
```

### 9. Architecture Consistency Skill

**목적**: AI가 구조 깨는 거 방지

**핵심 규칙**:
```yaml
name: architecture-consistency
description: 기존 구조와의 일관성 검사
checks:
  레이어 분리:
    - [ ] Controller → Service → Repository 순서 준수
    - [ ] 상위 레이어가 하위 레이어 호출 (역방향 금지)
    - [ ] 레이어 건너뛰기 금지

  의존성 방향:
    - [ ] Domain은 다른 레이어에 의존 안 함
    - [ ] Infrastructure는 가장 바깥
    - [ ] Circular Dependency 없음

  네이밍 컨벤션:
    - [ ] 기존 패턴과 일치
    - [ ] 파일명/클래스명/함수명 일관성

  폴더 구조:
    - [ ] 기존 모듈 구조 준수
    - [ ] 새 폴더 생성 시 기존 패턴 따름

enforcement:
  - 구조 변경 시 Architecture Agent 먼저 호출
  - ADR 작성 강제
  - 기존 코드 분석 후 동일 패턴 적용
```

### 🔒 스킬 실전 조합

**항상 함께 쓰는 조합**:
```
Feature Planner Skill (계획 강제)
+ Code Review Checklist (품질 강제)
+ Security Checklist (보안 강제)
+ Test Strategy (테스트 효율)
```

이 4개는 거의 모든 프로젝트에서 기본으로 적용

---

## 🔌 MCP / 플러그인 / 템플릿 가이드

### 구조 설계 · 아키텍처 보조

#### 1. Context7 MCP (문서 기준 고정)
**목적**: 프로젝트 문서를 AI 컨텍스트로 고정

**사용 방법**:
```json
{
  "mcpServers": {
    "context7": {
      "command": "npx",
      "args": ["-y", "@context7/mcp-server"]
    }
  }
}
```

**실무 활용**:
- 내부 PRD / ADR / 아키텍처 문서를 컨텍스트로 고정
- "이 프로젝트의 구조 기준은 이것"을 AI에게 강제
- 대규모 코드베이스에서 스파게티 방지 핵심

**효과**:
```
설계 기준을 MCP로 고정 → AI가 구조를 망가뜨릴 확률 급감
```

#### 2. ADR Template (Architecture Decision Record)
**목적**: 설계 결정 기록

**템플릿 구조**:
```markdown
# ADR-001: [결정 제목]

## Status
제안됨 / 승인됨 / 폐기됨

## Context
어떤 상황이었나?
어떤 문제를 해결하려 했나?

## Decision
무엇을 결정했나?

## Consequences
이 결정의 결과:
- 긍정적 영향
- 부정적 영향
- 트레이드오프

## Alternatives Considered
고려했지만 선택하지 않은 대안들:
- 대안 1: [이유]
- 대안 2: [이유]
```

**실무 활용**:
- 넷플릭스/아마존식 설계 기록
- AI에게 설계 변경 시 참조 기준 제공
- 거의 모든 중대형 팀에서 사용

#### 3. C4 Model Templates
**목적**: 시스템 아키텍처 시각화

**레벨**:
1. System Context (가장 넓음)
2. Container (서비스/앱 단위)
3. Component (모듈 단위)
4. Code (클래스/함수 단위)

**실무 활용**:
- AI에게 "지금 어느 레벨 이야기 중인지" 명확히 전달
- 설계 논의 시 혼란 방지

#### 4. Design Doc Template (Google 스타일)
**목적**: 설계안 비교 및 결정

**템플릿 구조**:
```markdown
# [기능명] 설계 문서

## 배경
왜 이게 필요한가?

## 목표
무엇을 달성할 것인가?

## 비목표
무엇은 하지 않을 것인가?

## 제안 설계
어떻게 구현할 것인가?

## 대안 비교
| 대안 | 장점 | 단점 | 선택 |
|------|------|------|------|
| A    |      |      | ✅   |
| B    |      |      | ❌   |

## 리스크
어떤 위험이 있나?

## 타임라인
(필요 시 - 단계별 구현 순서만)
```

**실무 활용**:
- AI 설계 비교 능력을 가장 잘 끌어냄
- 여러 대안 중 최적안 선택 시 사용

### 코드 리뷰 · 품질 통제

#### 1. GitHub Copilot Review
**목적**: PR 자동 리뷰

**설정**:
```yaml
# .github/workflows/copilot-review.yml
name: Copilot Review
on: [pull_request]
jobs:
  review:
    runs-on: ubuntu-latest
    steps:
      - uses: github/copilot-review-action@v1
```

**효과**:
- 사람 리뷰 전 1차 필터
- 스타일, 중복, 보안 힌트 제공

#### 2. Snyk
**목적**: 의존성 보안 취약점 자동 탐지

**설정**:
```yaml
# .github/workflows/snyk.yml
name: Snyk Security
on: [push, pull_request]
jobs:
  security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: snyk/actions/node@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
```

**효과**:
- PR 단계에서 취약점 차단
- 스타트업~엔터프라이즈 실사용률 높음

#### 3. SonarQube
**목적**: 코드 품질 수치화

**측정 항목**:
- 코드 스멜 (Code Smell)
- 순환 복잡도 (Cyclomatic Complexity)
- 중복 코드
- 테스트 커버리지

**효과**:
```
"감으로 리뷰" ❌ → "수치로 리뷰" ✅
```

#### 4. Danger.js
**목적**: PR 룰 자동화

**설정 예시**:
```javascript
// dangerfile.js
import { danger, warn, fail } from 'danger'

// 테스트 누락 체크
const hasTests = danger.git.created_files.some(f => f.includes('.test.'))
if (!hasTests) {
  warn('테스트 파일이 없습니다.')
}

// 큰 PR 경고
if (danger.github.pr.additions > 500) {
  warn('PR이 너무 큽니다. 분할을 고려하세요.')
}

// 문서 업데이트 체크
const touchedFiles = danger.git.modified_files
const hasSourceChanges = touchedFiles.some(f => f.includes('src/'))
const hasDocChanges = touchedFiles.some(f => f.includes('README'))
if (hasSourceChanges && !hasDocChanges) {
  warn('문서 업데이트가 필요할 수 있습니다.')
}
```

**효과**:
- 리뷰 피로도 감소
- 일관된 PR 품질

### 오류 해결 · 디버깅

#### 1. Sentry
**목적**: 런타임 에러 추적

**설정**:
```javascript
import * as Sentry from "@sentry/node"

Sentry.init({
  dsn: process.env.SENTRY_DSN,
  environment: process.env.NODE_ENV,
})
```

**효과**:
- 스택트레이스 + 컨텍스트 자동 수집
- AI로 원인 요약하기 매우 좋음

#### 2. OpenTelemetry
**목적**: 분산 추적

**사용 시나리오**:
- 마이크로서비스 환경
- 요청이 여러 서비스를 거칠 때
- 어디서 느려지는지 추적

**효과**:
- AI에게 장애 흐름 설명할 때 강력

#### 3. Error Playbook Template
**목적**: 에러별 대응 자동화

**템플릿**:
```markdown
# Error Playbook

## ERR_DB_CONNECTION
**증상**: 데이터베이스 연결 실패
**원인**:
- DB 서버 다운
- 연결 풀 고갈
- 네트워크 문제

**대응**:
1. DB 서버 상태 확인
2. 연결 풀 설정 확인
3. 네트워크 진단
4. 필요 시 재시작

**자동화**:
- Health Check에서 감지
- 알림 발송
- 자동 재시도 (3회)
```

#### 4. Postmortem Template
**목적**: 장애 후 분석

**템플릿**:
```markdown
# Postmortem: [장애명]

## Summary
한 줄 요약

## Timeline
- 14:23: 최초 에러 감지
- 14:25: 알림 수신
- 14:30: 원인 파악
- 14:35: 롤백 완료

## Impact
- 영향받은 사용자: X명
- 다운타임: Y분
- 데이터 손실: 없음

## Root Cause
근본 원인

## Resolution
어떻게 해결했나

## Action Items
- [ ] 재발 방지책 1
- [ ] 모니터링 개선
- [ ] 문서 업데이트
```

### 테스트 · 품질 · 배포

#### 1. Test Strategy Template
**목적**: 과잉/부족 테스트 방지

**템플릿**:
```markdown
# Test Strategy

## Test Pyramid
- Unit: 70% (빠름, 많이)
- Integration: 20% (중간)
- E2E: 10% (느림, 핵심만)

## Coverage Goals
- 비즈니스 로직: ≥90%
- API 레이어: ≥70%
- UI: Integration으로 대체

## What NOT to Test
- Getter/Setter
- Private methods
- 라이브러리 내부
```

#### 2. PACT (Contract Test)
**목적**: API 변경으로 깨지는 문제 방지

**사용 시나리오**:
- 마이크로서비스
- 프론트엔드 ↔ 백엔드 분리
- 외부 API 연동

#### 3. Feature Flag Template
**목적**: 장애 시 즉시 비활성화

**설정**:
```javascript
const features = {
  newCheckout: process.env.FEATURE_NEW_CHECKOUT === 'true',
  aiRecommendation: process.env.FEATURE_AI_RECOMMEND === 'true',
}

if (features.newCheckout) {
  // 새 체크아웃 로직
} else {
  // 기존 체크아웃 로직
}
```

**효과**:
- 배포 ≠ 릴리스
- 문제 시 코드 수정 없이 비활성화

#### 4. Release Checklist
**목적**: 배포 전 확인사항

**템플릿**:
```markdown
# Release Checklist

## Pre-Deploy
- [ ] 모든 테스트 통과
- [ ] 코드 리뷰 완료
- [ ] 스테이징 테스트 완료
- [ ] 롤백 계획 수립
- [ ] 모니터링 설정 확인

## Deploy
- [ ] 배포 공지
- [ ] Blue-Green 배포
- [ ] 헬스 체크 확인
- [ ] 스모크 테스트

## Post-Deploy
- [ ] 에러율 모니터링 (30분)
- [ ] 성능 메트릭 확인
- [ ] 사용자 피드백 확인
- [ ] 배포 완료 공지
```

---

## 🔥 실전 조합 (가장 중요)

### 김지민님 스타일 최적 조합

```
📚 문서 기준 고정
Context7 MCP + ADR Template + Design Doc Template

🤖 AI 통제
feature-planner (SKILL.md)
+ Architecture Consistency Skill
+ Security Checklist Skill

🔍 품질 관리
SonarQube (코드 품질)
+ Snyk (보안)
+ Danger.js (PR 자동화)

🧪 테스트
Test Strategy Skill
+ Playwright (E2E)
+ PACT (Contract Test)

📊 모니터링
Sentry (에러 추적)
+ OpenTelemetry (분산 추적)
```

### 이 조합의 특징
```
✅ AI가 자유롭게 날뛰지 못함
✅ 구조·보안·품질이 문서로 통제됨
✅ "AI를 쓰는 개발자" ❌
✅ "AI를 관리하는 개발자" ✅
```

---

## 📝 저장소 사용 가이드

### 새 프로젝트 시작 시
1. 이 저장소를 북마크
2. `SKILL.md` 복사 → 프로젝트의 `.claude/skills/` 폴더
3. `plan-tamplate.md` 복사 → 프로젝트의 `docs/templates/` 폴더
4. 필요한 서브에이전트 정의 복사
5. MCP 설정 참고하여 프로젝트 설정

### 기존 프로젝트 개선 시
1. 현재 문제점 파악 (구조 혼란? 테스트 부족? 보안 취약?)
2. 해당 문제에 맞는 스킬/에이전트 선택
3. 점진적으로 도입 (한 번에 전부 X)

### 이 저장소 확장 시
1. 새 스킬/에이전트 추가 시 디렉터리 구조 준수
2. 각 파일에 목적/사용법/예시 포함
3. 실무에서 검증된 것만 추가
4. 가이드라인 업데이트

---

## 🎯 다음 단계

### 우선순위 1: 서브에이전트 구현
```
skills/
├── feature-planner/      (✅ 완료)
├── code-review-checklist/  (🔜 다음)
├── security-checklist/     (🔜 다음)
└── test-strategy/          (🔜 다음)
```

### 우선순위 2: 템플릿 추가
```
templates/
├── adr-template.md         (🔜 다음)
├── design-doc-template.md  (🔜 다음)
├── postmortem-template.md  (🔜 다음)
└── release-checklist.md    (🔜 다음)
```

### 우선순위 3: MCP 설정 가이드
```
mcp-configs/
├── context7-setup.md      (🔜 다음)
└── custom-mcp-guide.md    (🔜 다음)
```

---

## 📌 핵심 원칙

1. **AI는 도구, 아키텍처는 사람이**
   - AI에게 구조 설계를 맡기지 말 것
   - Architecture Agent → 사람 승인 → Implementation Agent

2. **문서가 코드보다 먼저**
   - ADR 없이 구조 변경 금지
   - 계획 없이 구현 시작 금지

3. **품질 게이트는 절대**
   - 테스트 실패 시 다음 단계 진행 금지
   - 보안 체크 통과 전 배포 금지

4. **작게, 자주, 안전하게**
   - 큰 PR보다 작은 PR 여러 개
   - 한 번에 하나씩
   - 언제든 롤백 가능하게

5. **AI를 관리하는 개발자**
   - AI가 날뛰지 못하게 제약 설정
   - 스킬과 에이전트로 행동 통제
   - 문서와 MCP로 컨텍스트 고정

---

**마지막 업데이트**: 2025-12-30
**다음 업데이트 예정**: 서브에이전트 구현 파일 추가 후
