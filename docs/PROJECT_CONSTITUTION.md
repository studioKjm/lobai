# PROJECT CONSTITUTION
## LobAI 프로젝트 불변 원칙

**Version**: 1.0.0
**Last Updated**: 2026-01-04
**Purpose**: 프로젝트의 변하지 않는 핵심 원칙과 제약사항 정의

---

## 이 문서의 목적

이 헌법은 LobAI 프로젝트의 **불변 규칙(Invariants)**과 **핵심 원칙(Principles)**을 정의합니다.

- **포함**: 절대 위반할 수 없는 제약사항, 아키텍처 경계, 권한 규칙
- **제외**: 구현 세부사항, 도구 버전, 숫자 임계값, 단계별 절차

모든 코드 변경은 이 헌법을 준수해야 하며, 위반 시 자동으로 거부됩니다.

---

## Section 1: Architecture Invariants

### 1.1 API Layer Structure

**원칙**: Controller → Service → Repository 계층 구조 엄격 준수

**필수 사항**:
- ✅ Controller는 HTTP 요청/응답만 처리
- ✅ Service는 비즈니스 로직만 포함
- ✅ Repository는 데이터 접근만 담당
- ✅ 각 계층은 바로 아래 계층만 호출

**금지 사항**:
- ❌ Controller에서 Repository 직접 호출
- ❌ Controller에 비즈니스 로직 작성
- ❌ Service 레이어 생략
- ❌ 계층 건너뛰기

**이유**:
- 관심사 분리
- 테스트 용이성
- 코드 재사용성
- 유지보수성

---

### 1.2 Database Schema Management

**원칙**: 모든 스키마 변경은 Flyway Migration 필수

**필수 사항**:
- ✅ 스키마 변경은 `db/migration/V{version}__{description}.sql` 파일로만
- ✅ Migration은 순차적 버전 번호 사용
- ✅ DDL 변경은 롤백 가능하게 설계
- ✅ 외래키 제약조건 필수 정의

**금지 사항**:
- ❌ `ddl-auto: update` 사용 (프로덕션)
- ❌ 수동 SQL 실행으로 스키마 변경
- ❌ Migration 파일 직접 수정 (배포 후)
- ❌ 외래키 없이 관계 정의

**이유**:
- 스키마 버전 관리
- 롤백 가능성
- 팀 협업 안정성
- 데이터 무결성

---

### 1.3 Database Constraints

**원칙**: 비즈니스 규칙은 DB 레벨에서도 강제

**필수 CHECK 제약조건**:

```sql
-- Stats 범위 (0-100)
CHECK (current_hunger >= 0 AND current_hunger <= 100)
CHECK (current_energy >= 0 AND current_energy <= 100)
CHECK (current_happiness >= 0 AND current_happiness <= 100)

-- Email 형식
CHECK (email LIKE '%@%')

-- 비밀번호 해시 필수
CHECK (password_hash IS NOT NULL AND LENGTH(password_hash) > 50)
```

**금지 사항**:
- ❌ NULL 허용 시 NOT NULL 제약 해제
- ❌ 범위 검증 없이 숫자 컬럼 생성
- ❌ UNIQUE 제약 없이 중복 가능한 이메일

**이유**:
- 데이터 무결성 보장
- 애플리케이션 버그로부터 보호
- 일관성 강제

---

### 1.4 React Component Structure

**원칙**: 관심사 분리 및 단일 책임 원칙

**필수 사항**:
- ✅ 컴포넌트는 적절한 크기 유지
- ✅ 비즈니스 로직은 Custom Hook으로 분리
- ✅ UI 로직과 상태 관리 분리
- ✅ Props는 명확한 타입 정의

**금지 사항**:
- ❌ 한 파일에 모든 로직 작성
- ❌ Props drilling 과다 (depth > 3)
- ❌ 타입 정의 없는 any 사용
- ❌ 인라인 스타일 과다 사용

**이유**:
- 코드 재사용성
- 테스트 가능성
- 가독성
- 타입 안전성

---

### 1.5 AI Infrastructure (.claude/ Folder)

**원칙**: AI 개발 인프라는 정의된 구조 엄수

**필수 폴더 구조**:
```
.claude/
├── agents/         # 서브에이전트 YAML 파일만
├── skills/         # 스킬 Markdown 파일만
├── mcp-configs/    # MCP 설정 가이드만
└── reference/      # 참고 문서만
```

**필수 사항**:
- ✅ 에이전트는 `.yml` 형식만
- ✅ 스킬은 Frontmatter 포함 `.md` 형식만
- ✅ 모든 파일은 Git 추적
- ✅ 역할별 한 에이전트만 (중복 금지)

**금지 사항**:
- ❌ 임의 폴더 추가
- ❌ 코드 파일 저장 (.js, .java 등)
- ❌ 역할 중복된 에이전트 생성
- ❌ `.gitignore`에 .claude/ 추가

**이유**:
- 표준화된 AI 워크플로우
- 팀 협업 일관성
- 재사용성

---

## Section 2: Business Logic Invariants

### 2.1 Stats System Rules

**원칙**: Stats 값과 변화량은 엄격한 범위 제한

**필수 규칙**:
- ✅ 모든 Stats는 0-100 범위만 허용
- ✅ 자동 감소는 서버 시간 기준
- ✅ 증가량은 정의된 액션별 상수 사용
- ✅ 클라이언트 값은 서버 검증 필수

**액션별 변화량**:
```typescript
FEED:  hunger +15, happiness +5
PLAY:  happiness +15, energy -5
SLEEP: energy +15, happiness +5
CHAT:  happiness +2
```

**금지 사항**:
- ❌ 100 초과 또는 0 미만 값
- ❌ 클라이언트 시간 기준 계산
- ❌ 임의 변화량 허용
- ❌ 서버 검증 없이 Stats 업데이트

**이유**:
- 게임 밸런스
- 치트 방지
- 일관성

---

### 2.2 Attendance System Rules

**원칙**: 출석은 하루 1회, 연속 출석 추적

**필수 규칙**:
- ✅ 출석은 UTC 기준 하루 1회만
- ✅ 연속 출석 끊기면 0으로 초기화
- ✅ 출석 보상은 정의된 규칙만
- ✅ 중복 출석 체크는 DB 레벨에서도 강제

**연속 출석 계산**:
```sql
-- 전날 출석 있으면 +1, 없으면 1로 리셋
IF (DATEDIFF(CURRENT_DATE, last_attendance_date) = 1) THEN
  streak_days = streak_days + 1
ELSE
  streak_days = 1
END IF
```

**금지 사항**:
- ❌ 하루 2회 이상 출석
- ❌ 클라이언트 시간 기준 출석
- ❌ Streak 수동 조작
- ❌ 출석 날짜 소급 적용

**이유**:
- 공정성
- 데이터 무결성
- 사용자 참여 유도

---

### 2.3 AI Report Generation Rules

**원칙**: 리포트 생성은 충분한 데이터 필수

**필수 조건**:
- ✅ 최소 10개 이상 메시지 교환
- ✅ 최소 3일 이상 활동 기록
- ✅ 분석 알고리즘은 명시된 기준만 사용
- ✅ 리포트는 익명화된 데이터 기반

**분석 지표**:
```
AI Affinity Score:
- 의사소통 명확성 (0-100)
- 협조적 태도 (0-100)
- 맥락 유지 능력 (0-100)

AI Resilience Score:
- AI 활용 적합도 (0-100)
- 자동화 위험도 (0-100)
```

**금지 사항**:
- ❌ 데이터 부족 시 리포트 생성
- ❌ 개인 식별 정보 포함
- ❌ 임의 점수 부여
- ❌ 알고리즘 기준 외 평가

**이유**:
- 리포트 신뢰성
- 개인정보 보호
- 공정성

---

### 2.4 Message System Rules

**원칙**: 메시지는 발신자 검증 필수

**필수 검증**:
- ✅ 발신자 ID는 인증된 사용자만
- ✅ 메시지 길이 제한 (최대 500자)
- ✅ 페르소나 ID는 유효한 값만
- ✅ 모든 메시지는 DB 영구 저장

**메시지 구조**:
```sql
messages (
  id,
  user_id,           -- FK to users (본인만)
  persona_id,        -- FK to personas
  role,              -- 'user' | 'bot'
  content,           -- TEXT (500자 제한)
  created_at
)
```

**금지 사항**:
- ❌ 타인 user_id로 메시지 전송
- ❌ 500자 초과 메시지
- ❌ 유효하지 않은 persona_id
- ❌ role 임의 변경

**이유**:
- 데이터 무결성
- 보안
- 일관성

---

### 2.5 Persona System Rules

**원칙**: 페르소나는 5개 고정, 시스템 프롬프트 변경 금지

**고정 페르소나**:
1. 친구 (friend)
2. 상담사 (counselor)
3. 코치 (coach)
4. 전문가 (expert)
5. 유머 (humor)

**필수 규칙**:
- ✅ 페르소나 추가/삭제는 마이그레이션 필수
- ✅ 시스템 프롬프트 변경은 DB 업데이트로만
- ✅ 페르소나 전환 시 대화 컨텍스트 유지
- ✅ 각 페르소나는 고유한 아이콘/설명 보유

**금지 사항**:
- ❌ 사용자가 임의 페르소나 생성
- ❌ 하드코딩된 시스템 프롬프트
- ❌ 페르소나 ID 충돌
- ❌ 비활성 페르소나 노출

**이유**:
- 일관된 사용자 경험
- AI 품질 관리
- 데이터 추적 가능성

---

## Section 3: Permission & Responsibility Boundaries

### 3.1 AI Agent Permissions

#### Architecture Agent

**허용 작업**:
- ✅ ADR 문서 작성 (docs/adr/)
- ✅ 폴더 구조 제안
- ✅ 컴포넌트 다이어그램 작성
- ✅ 기존 코드 읽기 (분석용)

**금지 작업**:
- ❌ 코드 작성
- ❌ 파일 수정 (문서 외)
- ❌ 5개 초과 컴포넌트 승인 없이 제안
- ❌ 기존 아키텍처 무시한 설계

**이유**: 설계와 구현 분리, 일관성 유지

---

#### Backend Developer Agent

**허용 작업**:
- ✅ Entity, Repository, Service, Controller 작성
- ✅ DTO 및 Exception 작성
- ✅ Lombok/Validation 어노테이션 사용
- ✅ JPA 쿼리 메서드 작성

**금지 작업**:
- ❌ DB 스키마 직접 변경 (DDL)
- ❌ 보안 관련 코드 작성 (Security Agent 전담)
- ❌ 테스트 코드 작성 (Test Engineer 전담)
- ❌ Controller에 비즈니스 로직 작성

**이유**: 역할 분리, 전문성

---

#### Test Engineer Agent

**허용 작업**:
- ✅ Unit/Integration/E2E 테스트 작성
- ✅ Mock/Stub 설정
- ✅ Test Pyramid 비율 준수
- ✅ 커버리지 리포트 생성

**금지 작업**:
- ❌ 프로덕션 코드 수정
- ❌ 테스트 비율 무시 (E2E > Unit)
- ❌ Implementation Details 테스트
- ❌ Over-mocking

**이유**: 테스트 품질, 빠른 피드백

---

#### Security Agent

**허용 작업**:
- ✅ Security Checklist 검증
- ✅ OWASP Top 10 검사
- ✅ 입력 검증 확인
- ✅ 보안 리포트 생성

**금지 작업**:
- ❌ 보안 문제 직접 수정 (리포트만)
- ❌ 성능 최적화 (별도 담당)
- ❌ 기능 추가

**이유**: 검증과 구현 분리

---

### 3.2 User Approval Requirements

#### Critical Changes (사용자 승인 필수)

- ❌ DB 스키마 변경 (컬럼 추가/삭제/타입 변경)
- ❌ API 엔드포인트 삭제 또는 Breaking Change
- ❌ 인증/인가 로직 변경
- ❌ 5개 이상 파일 생성
- ❌ 아키텍처 패턴 변경

**승인 프로세스**:
1. 변경 사항 문서화
2. 영향 범위 분석
3. 사용자 확인 대기
4. 승인 후 진행

---

#### High Priority (권장 승인)

- ⚠️ 새 의존성 추가
- ⚠️ 환경 변수 추가
- ⚠️ 배포 설정 변경
- ⚠️ CI/CD 파이프라인 수정

---

#### Low Priority (자동 진행 가능)

- ✅ 버그 수정 (기존 기능 범위 내)
- ✅ 테스트 추가
- ✅ 문서 업데이트
- ✅ 리팩터링 (동작 변경 없음)

**이유**: 위험 관리, 예측 가능성

---

### 3.3 User Role Permissions (RBAC)

#### SUPER_ADMIN

**권한**:
- ✅ 모든 사용자 데이터 조회
- ✅ 시스템 설정 변경
- ✅ 페르소나 추가/수정
- ✅ 사용자 삭제

---

#### ADMIN

**권한**:
- ✅ 사용자 목록 조회
- ✅ 리포트 강제 생성
- ✅ 콘텐츠 관리

**제한**:
- ❌ 시스템 설정 변경
- ❌ 다른 ADMIN 권한 수정

---

#### USER (기본)

**권한**:
- ✅ 본인 프로필 조회/수정
- ✅ 본인 메시지 조회/전송
- ✅ 본인 Stats 조회
- ✅ 본인 리포트 생성

**제한**:
- ❌ 타인 데이터 접근
- ❌ 시스템 설정 조회
- ❌ 관리자 기능 접근

**검증 위치**:
```java
@PreAuthorize("hasRole('USER') and #userId == authentication.principal.id")
```

**이유**: 최소 권한 원칙, 데이터 보호

---

## Section 4: Core Principles

### 4.1 Development Principles

#### Test-Driven Development (TDD)

**필수 프로세스**:
1. 🔴 RED: 실패하는 테스트 작성
2. 🟢 GREEN: 최소 코드로 통과
3. 🔵 REFACTOR: 코드 개선

**금지 사항**:
- ❌ 테스트 없이 코드 작성
- ❌ 테스트를 나중에 작성
- ❌ 테스트를 건너뛰고 리팩터링

**이유**: 회귀 방지, 설계 개선, 문서화 효과

---

#### Documentation-First

**원칙**: 코드 작성 전 계획 문서화

**필수 문서**:
- ✅ 기능 계획: `docs/plans/PLAN_{feature}.md`
- ✅ 설계 결정: `docs/adr/ADR-{number}-{title}.md`
- ✅ API 스펙: OpenAPI/Swagger
- ✅ 구현 전 사용자 승인

**금지 사항**:
- ❌ 문서 없이 큰 기능 구현
- ❌ ADR 없이 아키텍처 변경
- ❌ README 업데이트 누락

**이유**: 명확한 의사소통, 추적 가능성

---

#### Code Review Mandatory

**원칙**: 모든 PR은 체크리스트 통과 필수

**필수 검증**:
- ✅ 테스트 커버리지 적절
- ✅ 빌드 성공
- ✅ 타입 체크 통과
- ✅ Lint 에러 없음
- ✅ 보안 체크리스트 통과

**금지 사항**:
- ❌ 리뷰 없이 master 머지
- ❌ 테스트 실패 상태 머지
- ❌ 체크리스트 건너뛰기

**이유**: 품질 보장, 지식 공유

---

### 4.2 Deployment Principles

#### Automated Deployment Only

**원칙**: 수동 배포 금지, CI/CD 파이프라인 사용

**필수 단계**:
1. 테스트 실행
2. 보안 스캔
3. 빌드
4. Staging 배포
5. E2E 테스트
6. Production 배포

**금지 사항**:
- ❌ SSH 접속 후 수동 배포
- ❌ 테스트 건너뛰고 배포
- ❌ Staging 없이 Production 배포

**이유**: 재현 가능성, 안정성, 롤백 용이

---

#### Rollback Capability

**원칙**: 모든 배포는 즉시 롤백 가능

**필수 조건**:
- ✅ DB 마이그레이션은 롤백 가능하게 작성
- ✅ 이전 버전 아티팩트 보관
- ✅ Blue-Green 또는 Canary 배포
- ✅ 롤백 테스트 주기적 실행

**금지 사항**:
- ❌ 불가역 마이그레이션 (DROP TABLE)
- ❌ 이전 버전 삭제
- ❌ 롤백 절차 미문서화

**이유**: 장애 복구, 다운타임 최소화

---

#### Zero-Downtime Deployment

**원칙**: 배포 중에도 서비스 중단 없음

**전략**:
- ✅ Blue-Green Deployment
- ✅ Rolling Update
- ✅ Database Migration 먼저, 코드 배포는 나중
- ✅ Health Check 엔드포인트 필수

**금지 사항**:
- ❌ 전체 서버 동시 재시작
- ❌ Breaking Change 직접 배포
- ❌ Health Check 없이 로드밸런서 연결

**이유**: 가용성, 사용자 경험

---

### 4.3 Quality Principles

#### Adequate Test Coverage

**원칙**: 핵심 로직은 철저히 테스트

**우선순위**:
1. 비즈니스 로직 (최고)
2. API 엔드포인트 (높음)
3. 유틸리티 함수 (높음)
4. UI 컴포넌트 (중간)

**필수 테스트**:
- ✅ 모든 비즈니스 로직 Unit Test
- ✅ 핵심 플로우 E2E Test
- ✅ 보안 관련 기능 Integration Test

**금지 사항**:
- ❌ 테스트 없이 복잡한 로직 배포
- ❌ E2E만 작성하고 Unit Test 생략
- ❌ Coverage 수치에만 집중

**이유**: 버그 예방, 리팩터링 안정성

---

#### Performance Monitoring

**원칙**: 성능은 측정 가능하게 관리

**필수 지표**:
- ✅ API 응답 시간
- ✅ DB 쿼리 속도
- ✅ 페이지 로드 시간
- ✅ 에러율

**모니터링 도구**:
- ✅ APM (Application Performance Monitoring)
- ✅ 로그 수집 (Sentry, LogStash)
- ✅ 알림 설정 (Slack, Email)

**금지 사항**:
- ❌ 성능 저하 방치
- ❌ N+1 쿼리 무시
- ❌ 모니터링 없이 배포

**이유**: 조기 문제 발견, SLA 보장

---

#### Security-First

**원칙**: 보안은 선택이 아닌 필수

**필수 검증**:
- ✅ OWASP Top 10 체크
- ✅ API Key 환경 변수 사용
- ✅ 입력 검증 (화이트리스트)
- ✅ 인증/인가 모든 엔드포인트

**금지 사항**:
- ❌ API Key 하드코딩
- ❌ SQL Injection 취약점
- ❌ XSS 취약점
- ❌ 권한 체크 누락

**이유**: 데이터 보호, 신뢰성

---

### 4.4 Context Management Principles

#### Document Hierarchy

**원칙**: 문서는 계층적으로 관리

**계층 구조**:
```
PROJECT_CONSTITUTION.md (최상위 - 불변 원칙)
    ↓
TECHNICAL_GUIDE.md (아키텍처 가이드)
    ↓
docs/plans/ (기능별 계획)
    ↓
docs/adr/ (설계 결정 기록)
    ↓
Code Comments (구현 세부사항)
```

**우선순위**:
1. PROJECT_CONSTITUTION (절대 우선)
2. TECHNICAL_GUIDE (가이드라인)
3. PLAN (기능 계획)
4. ADR (설계 결정)
5. Code (구현)

**충돌 시**:
- ✅ 상위 문서가 항상 우선
- ✅ 하위 문서는 상위 문서 준수
- ✅ 불일치 발견 시 상위 문서 기준 수정

**이유**: 일관성, 명확한 권위

---

#### Session Context Cleanup

**원칙**: AI 세션은 컨텍스트 정리 필수

**세션 종료 시**:
- ✅ 생성된 파일 목록 정리
- ✅ 완료/미완료 작업 명시
- ✅ 다음 세션을 위한 TODO 작성
- ✅ 중요 결정사항 문서화

**금지 사항**:
- ❌ 작업 중단 상태 방치
- ❌ 임시 파일 그대로 남김
- ❌ 다음 담당자에게 컨텍스트 전달 없음

**이유**: 연속성, 협업 효율

---

#### Context7 Integration

**원칙**: 모든 중요 문서는 Context7 인덱싱

**대상 문서**:
- ✅ PROJECT_CONSTITUTION.md
- ✅ TECHNICAL_GUIDE.md
- ✅ docs/plans/*.md
- ✅ docs/adr/*.md
- ✅ .claude/skills/*.md

**사용 방법**:
```
"TECHNICAL_GUIDE에서 테스트 전략 알려줘"
→ Context7 MCP가 자동으로 검색
```

**금지 사항**:
- ❌ 중요 문서를 .gitignore에 추가
- ❌ Markdown 형식 위반
- ❌ 문서 업데이트 후 커밋 누락

**이유**: 빠른 정보 접근, 일관성

---

## Enforcement

### Violation Handling

**Level 1 - Warning (경고)**:
- 문서화 누락
- 테스트 커버리지 부족
- 코드 스타일 위반

**Level 2 - Rejection (거부)**:
- 계층 구조 위반 (Controller → Repository)
- DB 제약조건 누락
- 보안 체크리스트 미통과

**Level 3 - Rollback (롤백)**:
- Breaking Change 무단 배포
- 스키마 변경 승인 없이 진행
- 프로덕션 데이터 직접 수정

---

### Amendment Process

이 헌법 수정은 다음 조건 필요:

1. 명확한 문제 정의 (현재 원칙의 한계)
2. 제안된 변경사항 문서화
3. 영향 범위 분석
4. 팀 합의 (PR Review)
5. 버전 업데이트 (1.0.0 → 1.1.0)

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-01-04 | Initial constitution |

---

**문서 종료**
