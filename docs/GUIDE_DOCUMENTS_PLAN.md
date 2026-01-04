# 가이드 문서 분리 계획

**작성일**: 2026-01-04
**상태**: 계획 수립 완료

---

## 1. 개요

PROJECT_CONSTITUTION.md에서 불변 원칙(Invariants)만 남기고, 운영 가이드와 프로세스 문서를 별도로 분리합니다.

**헌법 vs 가이드 구분 기준**:
- **헌법**: 절대 변하지 않는 원칙, 위반 시 시스템 무결성 훼손
- **가이드**: 프로세스, 방법론, 권장 사항 (개선 가능)

---

## 2. 문서 구조

```
lobai/
├── PROJECT_CONSTITUTION.md     # 헌법 (300-600 lines, 불변)
│   ├── Section 1: Architecture Invariants
│   ├── Section 8: Business Logic Invariants
│   └── Section 10: Permission & Responsibility Boundary
│
└── docs/
    ├── README.md                # 문서 인덱스 (내비게이션)
    │
    ├── guides/                  # 운영 가이드
    │   ├── DEV_GUIDE.md         # 개발 프로세스, 코딩 컨벤션
    │   ├── TEST_GUIDE.md        # 테스트 전략, TDD, E2E
    │   ├── CICD_GUIDE.md        # CI/CD 파이프라인, 배포
    │   └── CONTEXT_GUIDE.md     # Claude 컨텍스트 관리
    │
    └── runbooks/                # 장애 대응 매뉴얼
        └── INCIDENT_PLAYBOOK.md # 인시던트 대응 절차
```

---

## 3. 각 가이드 문서 상세 계획

### 3.1 DEV_GUIDE.md (개발 가이드)

**원본 섹션 출처**:
- Section 2: Development Guidelines
- Section 11: Development Methodology
- Section 12: Version Control & Release Strategy

**목차 (예상)**:

```markdown
# Development Guide

## 1. 개발 환경 설정
- Node.js, Java 버전
- IDE 추천 설정 (VSCode, IntelliJ)
- 환경 변수 설정

## 2. 코딩 컨벤션
- TypeScript/JavaScript 스타일 가이드
- Java 코딩 컨벤션
- Naming conventions (변수, 함수, 클래스)
- 주석 작성 규칙

## 3. Git Workflow
- Branch 전략 (main, develop, feature/*, hotfix/*)
- Commit message 규칙 (Conventional Commits)
- Pull Request 프로세스
- Code Review 체크리스트

## 4. 개발 프로세스
- 이슈 생성 → 브랜치 생성 → 개발 → PR → 리뷰 → 머지
- Feature flag 사용 가이드
- 실험적 기능 개발 방법

## 5. 버전 관리 및 릴리스
- Semantic Versioning (MAJOR.MINOR.PATCH)
- Release note 작성 가이드
- Hotfix 배포 절차
```

**예상 분량**: 800-1,200 lines

---

### 3.2 TEST_GUIDE.md (테스트 가이드)

**원본 섹션 출처**:
- Section 3: Testing Strategy
- Section 7: E2E Testing & Deployment

**목차 (예상)**:

```markdown
# Testing Guide

## 1. 테스트 철학
- Test Pyramid (Unit > Integration > E2E)
- TDD 권장 사항
- 테스트 커버리지 목표 (80% 이상)

## 2. Unit Testing
### 2.1 Frontend (Vitest)
- Component 테스트 (React Testing Library)
- Hook 테스트
- Service 테스트

### 2.2 Backend (JUnit 5)
- Service layer 테스트
- Repository 테스트 (H2 in-memory DB)
- Controller 테스트 (MockMvc)

## 3. Integration Testing
- API 통합 테스트
- Database 마이그레이션 테스트
- 외부 API mocking (Gemini AI)

## 4. E2E Testing
- Playwright 설정 및 실행
- 주요 시나리오 테스트 케이스
  - 회원가입 → 로그인 → 채팅 → 출석 → 리포트 생성
- Visual regression testing

## 5. 테스트 데이터 관리
- Fixture 작성 가이드
- Seed data 생성 (data.sql)
- Test isolation 전략

## 6. CI에서의 테스트 실행
- GitHub Actions 설정
- 테스트 실패 시 대응 방법
```

**예상 분량**: 600-900 lines

---

### 3.3 CICD_GUIDE.md (CI/CD 가이드)

**원본 섹션 출처**:
- Section 6: CI/CD Pipeline
- Section 7: E2E Testing & Deployment (일부)

**목차 (예상)**:

```markdown
# CI/CD Guide

## 1. CI/CD 개요
- GitHub Actions 기반 파이프라인
- 환경별 분리 (dev, staging, production)

## 2. CI (Continuous Integration)
### 2.1 Pull Request CI
- Lint 검사 (ESLint, Checkstyle)
- TypeScript/Java 컴파일
- Unit & Integration 테스트 실행
- 테스트 커버리지 체크

### 2.2 Main Branch CI
- E2E 테스트 실행
- Docker 이미지 빌드
- Staging 환경 자동 배포

## 3. CD (Continuous Deployment)
### 3.1 Staging 배포
- 자동 배포 (main 머지 시)
- Database 마이그레이션 자동 실행
- Health check

### 3.2 Production 배포
- Manual approval 필수
- Blue-Green 배포 전략
- Rollback 절차

## 4. 환경 변수 관리
- GitHub Secrets 사용
- 환경별 `.env` 파일 관리
- 민감 정보 보호

## 5. 모니터링 및 알림
- 배포 성공/실패 알림 (Slack, Discord)
- 에러 로그 수집 (Sentry)
- 성능 모니터링 (Grafana)
```

**예상 분량**: 500-800 lines

---

### 3.4 CONTEXT_GUIDE.md (Claude 컨텍스트 관리 가이드)

**원본 섹션 출처**:
- Section 4: Documentation Management
- Section 5: Context Management for Claude

**목차 (예상)**:

```markdown
# Claude Context Management Guide

## 1. CLAUDE.md 관리
- 프로젝트 개요 업데이트 시점
- Claude에게 전달할 핵심 정보
- 금지 사항 (Claude가 하지 말아야 할 것)

## 2. 문서 작성 원칙
- 명확하고 간결한 표현
- 코드 예시 포함
- 변경 이력 기록

## 3. 컨텍스트 파일 관리
- 프로젝트 구조 요약 (tree 출력)
- 주요 파일 설명
- API endpoint 목록

## 4. 프롬프트 엔지니어링 팁
- Claude에게 작업 요청 시 모범 사례
- 에러 디버깅 요청 방법
- 코드 리뷰 요청 방법

## 5. 문서 갱신 주기
- 주요 기능 추가 시 즉시 업데이트
- 매 릴리스 전 문서 검토
- 분기별 전체 문서 리뷰
```

**예상 분량**: 400-600 lines

---

### 3.5 INCIDENT_PLAYBOOK.md (장애 대응 매뉴얼)

**원본 섹션 출처**:
- Section 9: Incident Response Playbook

**목차 (예상)**:

```markdown
# Incident Response Playbook

## 1. 장애 등급 분류
- P0 (Critical): 서비스 전체 다운
- P1 (High): 주요 기능 불가
- P2 (Medium): 일부 기능 저하
- P3 (Low): 경미한 버그

## 2. 초기 대응 절차
1. 장애 감지 및 알림
2. 영향 범위 파악
3. 긴급 공지 (사용자 알림)
4. 임시 조치 (서비스 복구 우선)

## 3. 근본 원인 분석
- 로그 분석 방법
- Database 쿼리 모니터링
- 외부 API 장애 확인

## 4. 복구 절차
### 4.1 애플리케이션 장애
- 서버 재시작
- 이전 버전 롤백
- Hotfix 배포

### 4.2 Database 장애
- Replica 승격
- Backup 복원
- Data integrity 검증

### 4.3 외부 API 장애 (Gemini)
- Fallback 응답 활성화
- Rate limit 조정
- 대체 AI 모델 전환

## 5. 사후 처리
- Post-mortem 작성
- 재발 방지 대책 수립
- 모니터링 강화

## 6. 연락처 및 에스컬레이션
- On-call 담당자 순번
- 외부 업체 연락처 (클라우드, DB 호스팅)
```

**예상 분량**: 600-900 lines

---

## 4. 문서 작성 우선순위

### Phase 1: 핵심 가이드 (병행 작업 가능)
1. **DEV_GUIDE.md** - 즉시 필요 (개발 진행 중)
2. **TEST_GUIDE.md** - 즉시 필요 (테스트 추가 중)

### Phase 2: 배포 준비
3. **CICD_GUIDE.md** - CI/CD 구축 전 필요
4. **INCIDENT_PLAYBOOK.md** - Production 배포 전 필수

### Phase 3: 최적화
5. **CONTEXT_GUIDE.md** - Claude 활용 고도화
6. **docs/README.md** - 문서 인덱스 (마지막)

---

## 5. 작성 방법

### 5.1 기존 문서 참조
- `LobAI_PRD_v3.md` - 비즈니스 요구사항
- `CLAUDE.md` - 프로젝트 개요
- 기존 코드베이스 - 실제 구현 내용

### 5.2 새로 작성할 내용
- 실무 베스트 프랙티스 참고 (Google, Airbnb style guide)
- GitHub Actions 예제 코드
- Playwright E2E 테스트 시나리오

### 5.3 작성 시 주의사항
- **간결함 유지**: 한 문서당 1,000 lines 이하 권장
- **실행 가능성**: 모든 명령어는 복사-붙여넣기로 실행 가능해야 함
- **예시 포함**: 추상적 설명보다 구체적 코드 예시
- **변경 이력**: 각 문서에 `## Changelog` 섹션 추가

---

## 6. 다음 단계

1. ✅ PROJECT_CONSTITUTION.md 작성 완료
2. ⏳ DEV_GUIDE.md 작성 (우선순위 1)
3. ⏳ TEST_GUIDE.md 작성 (우선순위 1)
4. ⏳ Admin Page Phase 1 개발 (병행)

**예상 소요 시간**:
- DEV_GUIDE.md: 2-3시간
- TEST_GUIDE.md: 2-3시간
- CICD_GUIDE.md: 1-2시간
- CONTEXT_GUIDE.md: 1시간
- INCIDENT_PLAYBOOK.md: 1-2시간
- docs/README.md: 30분

**총 예상**: 7-11.5시간

---

**작성자**: Claude Opus 4.5
**검토 필요**: 전체 팀 리뷰 후 승인
