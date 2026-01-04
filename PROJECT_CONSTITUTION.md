# PROJECT CONSTITUTION

**LobAI 프로젝트 헌법**
**Version**: 1.0.0
**Last Updated**: 2026-01-04
**Status**: IMMUTABLE (수정 시 전체 팀 합의 필요)

---

## 헌법의 목적

이 문서는 LobAI 프로젝트의 **불변 원칙(Invariants)**만을 정의합니다.
운영 가이드, 개발 프로세스, 테스트 방법론 등은 별도 가이드 문서를 참조하십시오.

**헌법 수정 절차**:
1. 수정 제안서 작성 (이유, 영향 범위, 마이그레이션 계획 포함)
2. 전체 개발팀 검토 및 합의
3. 버전 번호 갱신 (Major version up)
4. 변경 이력 기록

---

## SECTION 1: ARCHITECTURE INVARIANTS

### 1.1 프로젝트 구조

#### 1.1.1 폴더 구조 불변 규칙

```
lobai/
├── backend/                    # Spring Boot (Java 17+)
│   ├── src/main/java/com/lobai/
│   │   ├── controller/         # REST API endpoints
│   │   ├── service/            # Business logic layer
│   │   ├── repository/         # JPA repositories
│   │   ├── entity/             # Database entities
│   │   ├── dto/                # Data transfer objects
│   │   ├── config/             # Configuration classes
│   │   ├── security/           # JWT, authentication
│   │   └── util/               # Utility classes
│   └── src/main/resources/
│       ├── application.yml     # Spring configuration
│       └── data.sql            # Initial data (if any)
│
├── src/                        # Frontend (React + TypeScript + Vite)
│   ├── components/             # React components
│   │   ├── common/             # Shared components (Button, Card, etc.)
│   │   ├── layout/             # Layout components (Header, Footer, etc.)
│   │   ├── chat/               # Chat-related components
│   │   ├── stats/              # Stats display components
│   │   ├── attendance/         # Attendance components
│   │   ├── affinity/           # Affinity score components
│   │   └── admin/              # Admin panel components
│   ├── pages/                  # Page-level components
│   ├── hooks/                  # Custom React hooks
│   ├── services/               # API client services
│   ├── stores/                 # Zustand state management
│   ├── types/                  # TypeScript type definitions
│   ├── lib/                    # Utilities and helpers
│   └── assets/                 # Static assets (images, fonts, etc.)
│
└── docs/                       # Documentation
    ├── guides/                 # Operational guides
    │   ├── DEV_GUIDE.md
    │   ├── TEST_GUIDE.md
    │   ├── CICD_GUIDE.md
    │   └── CONTEXT_GUIDE.md
    └── runbooks/
        └── INCIDENT_PLAYBOOK.md
```

**INVARIANT**: 위 폴더 구조는 변경 불가. 새 카테고리 추가만 허용.

#### 1.1.2 컴포넌트 계층 구조

```
Page Components (pages/)
    ↓ (uses)
Feature Components (components/[feature]/)
    ↓ (uses)
Common Components (components/common/)
    ↓ (uses)
Hooks (hooks/) + Services (services/) + Stores (stores/)
```

**INVARIANT**:
- Common components는 feature-specific components를 import할 수 없음
- Pages는 services를 직접 호출하지 않음 (hooks를 통해서만)
- Services는 React hooks를 사용할 수 없음 (pure TypeScript)

#### 1.1.3 API Layer Pattern

**Backend**:
```
HTTP Request
    ↓
Controller (validation, auth check)
    ↓
Service (business logic)
    ↓
Repository (database access)
    ↓
Database
```

**Frontend**:
```
Component
    ↓
Custom Hook
    ↓
API Service (src/services/*Api.ts)
    ↓
API Client (src/lib/api.ts)
    ↓
Backend REST API
```

**INVARIANT**:
- Controller는 business logic을 포함하지 않음 (Service 위임만)
- Service는 HTTP 응답 형식을 알지 못함 (DTO 반환만)
- Repository는 JPA 인터페이스만 사용 (native query는 최소화)
- Frontend API service는 항상 `ApiResponse<T>` 형식 반환

### 1.2 Database Schema Invariants

#### 1.2.1 Core Tables

```sql
-- Users (절대 삭제 불가, 컬럼 추가만 허용)
users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)

-- Stats (컬럼 이름/타입 변경 금지)
stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    hunger INT DEFAULT 50 CHECK (hunger >= 0 AND hunger <= 100),
    energy INT DEFAULT 50 CHECK (energy >= 0 AND energy <= 100),
    happiness INT DEFAULT 50 CHECK (happiness >= 0 AND happiness <= 100),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
)

-- Attendance (출석 체크 로직 변경 금지)
attendance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, check_in_date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
)

-- Messages (AI 대화 기록, 삭제 정책 변경 금지)
messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_user BOOLEAN NOT NULL,
    sentiment_score FLOAT,
    word_count INT,
    question_count INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
)

-- Affinity Scores (친밀도, 계산 로직 변경 금지)
affinity_scores (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    overall_score INT DEFAULT 0 CHECK (overall_score >= 0 AND overall_score <= 100),
    level INT DEFAULT 1 CHECK (level >= 1 AND level <= 5),
    sentiment_score INT DEFAULT 0,
    clarity_score INT DEFAULT 0,
    context_score INT DEFAULT 0,
    usage_score INT DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
)

-- AI Resilience Reports (AI 적응력 리포트, 생성 조건 변경 금지)
resilience_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    readiness_score FLOAT NOT NULL,
    readiness_level INT NOT NULL,
    readiness_level_name VARCHAR(50),
    communication_score FLOAT,
    collaboration_score FLOAT,
    analyzed_message_count INT,
    report_data JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
)
```

**INVARIANT**:
- Primary key는 항상 `id BIGINT AUTO_INCREMENT`
- Foreign key는 항상 `ON DELETE CASCADE` (orphan 방지)
- Timestamp 컬럼은 `created_at`, `updated_at` 명명 규칙 준수
- CHECK constraint는 비즈니스 로직과 동기화 필수
- 테이블 삭제 금지, 컬럼 추가만 허용 (deprecation flag 사용)

#### 1.2.2 Schema Migration Rules

**INVARIANT**:
- Schema 변경은 항상 마이그레이션 스크립트로 관리
- 롤백 스크립트는 필수 작성
- Production 배포 전 staging에서 마이그레이션 검증 필수
- 컬럼 삭제는 3단계 절차 필요:
  1. `@Deprecated` 표시 + 사용 중단
  2. 1개월 후 nullable로 변경
  3. 3개월 후 실제 삭제

### 1.3 API Contract Invariants

#### 1.3.1 Response Format

**모든 API 응답은 다음 형식을 준수**:

```typescript
interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: {
    code: string;
    details?: any;
  };
}
```

**INVARIANT**:
- 성공 시: `success: true`, `data: T`, `message: "성공 메시지"`
- 실패 시: `success: false`, `message: "에러 메시지"`, `error.code: "ERROR_CODE"`
- HTTP 상태 코드는 RESTful 원칙 준수:
  - 200: 성공
  - 201: 생성 성공
  - 400: 잘못된 요청
  - 401: 인증 실패
  - 403: 권한 없음
  - 404: 리소스 없음
  - 500: 서버 오류

#### 1.3.2 Authentication & Authorization

**JWT Token 구조**:

```json
{
  "sub": "user_id",
  "email": "user@example.com",
  "role": "USER | ADMIN",
  "type": "access | refresh",
  "iat": 1234567890,
  "exp": 1234571490
}
```

**INVARIANT**:
- Access token 만료: 15분
- Refresh token 만료: 7일
- Token은 `Authorization: Bearer <token>` 헤더로 전송
- Role 기반 권한 검증은 `@PreAuthorize("hasRole('ADMIN')")` 사용
- 민감한 정보는 JWT payload에 포함 금지

---

## SECTION 8: BUSINESS LOGIC INVARIANTS

### 8.1 Stats System

#### 8.1.1 Stats Range

**INVARIANT**:
- 모든 스탯(hunger, energy, happiness)은 **0~100** 범위
- 범위 초과/미만 값은 자동으로 clamping (0 또는 100으로 보정)
- Database CHECK constraint와 application logic 동기화 필수

#### 8.1.2 Stats Decay

**INVARIANT**:
- 스탯 감소는 **5초마다 1씩** 감소 (고정값)
- 감소 로직은 frontend에서만 실행 (backend는 저장만)
- 사용자가 비활성화 상태여도 감소 지속 (재접속 시 계산된 값 적용)

#### 8.1.3 Stats Recovery

**Action별 회복량** (변경 금지):
- **Feed** (먹이 주기): hunger +20
- **Play** (놀아주기): happiness +15
- **Sleep** (재우기): energy +25

**INVARIANT**:
- 회복량은 clamping 후 적용 (최대 100 초과 불가)
- 동시에 여러 액션 실행 불가 (debouncing 필수)

### 8.2 Attendance System

#### 8.2.1 Check-in Rules

**INVARIANT**:
- 하루 1회만 출석 가능 (날짜 기준: `check_in_date DATE`)
- 중복 출석 시도는 `409 Conflict` 반환
- 출석 기록은 절대 삭제 불가 (감사 목적)

#### 8.2.2 Streak Calculation

**연속 출석일 계산 로직** (변경 금지):

```java
// Pseudo-code
int currentStreak = 0;
Date today = LocalDate.now();
Date checkDate = today;

while (attendanceExists(userId, checkDate)) {
    currentStreak++;
    checkDate = checkDate.minusDays(1);
}
```

**INVARIANT**:
- 하루라도 빠지면 streak는 0으로 리셋
- `maxStreak`는 역대 최고 기록 (절대 감소하지 않음)

### 8.3 AI Resilience Analysis

#### 8.3.1 Report Generation Conditions

**INVARIANT**:
- **최소 메시지 수**: 10개 이상
- 메시지 수 미달 시 리포트 생성 불가 (`400 Bad Request`)

#### 8.3.2 Readiness Score Calculation

**점수 산출 공식** (변경 금지):

```
readinessScore = (communicationScore * 0.4) + (collaborationScore * 0.3) + (consistencyScore * 0.3)
```

**Level 매핑** (고정값):
- 0-20: Level 1 - "낯선 사이"
- 21-40: Level 2 - "알아가는 중"
- 41-60: Level 3 - "친근한 사이"
- 61-80: Level 4 - "가까운 친구"
- 81-100: Level 5 - "최고의 파트너"

**INVARIANT**:
- 점수는 항상 0~100 범위
- Level은 항상 1~5 범위
- 산출 공식은 Gemini AI 호출 결과 기반 (직접 계산 금지)

### 8.4 Affinity Score System

#### 8.4.1 Score Components

**4가지 세부 점수** (변경 금지):
1. **sentimentScore**: 감정 분석 (긍정적 대화 비율)
2. **clarityScore**: 명확성 (질문 명확도, 문장 구조)
3. **contextScore**: 맥락 유지 (이전 대화 참조 비율)
4. **usageScore**: 사용 빈도 (출석일, 메시지 수)

#### 8.4.2 Overall Score Calculation

**종합 점수 산출** (가중치 고정):

```
overallScore = (sentimentScore * 0.3) + (clarityScore * 0.2) + (contextScore * 0.25) + (usageScore * 0.25)
```

**Level 매핑** (Readiness Level과 동일):
- 0-20: Level 1
- 21-40: Level 2
- 41-60: Level 3
- 61-80: Level 4
- 81-100: Level 5

**INVARIANT**:
- 점수는 매 대화 후 실시간 업데이트
- 가중치 합은 항상 1.0
- Level은 overall score에만 의존

### 8.5 Chat Message Processing

#### 8.5.1 Message Storage

**INVARIANT**:
- 모든 메시지는 `messages` 테이블에 저장 필수
- 사용자 메시지: `is_user = true`
- AI 응답: `is_user = false`
- 메시지 삭제는 사용자 삭제 시에만 허용 (CASCADE)

#### 8.5.2 Sentiment Analysis

**감정 분석 기준** (Gemini AI 기반):
- `sentiment_score`: -1.0 (부정) ~ +1.0 (긍정)
- 분석 실패 시 `null` 저장 (기본값 0 아님)

**INVARIANT**:
- 사용자 메시지에만 sentiment 분석 적용
- AI 응답은 sentiment_score = null

---

## SECTION 10: PERMISSION & RESPONSIBILITY BOUNDARY

### 10.1 Frontend vs Backend

#### 10.1.1 Frontend Responsibilities

**ALLOWED**:
- UI 렌더링 및 사용자 인터랙션 처리
- 클라이언트 사이드 validation (UX 개선 목적)
- Stats decay 계산 (5초마다 1씩 감소)
- JWT token 저장 및 관리 (localStorage)
- API 호출 및 응답 처리

**FORBIDDEN**:
- 직접 데이터베이스 접근
- 서버 사이드 validation 우회
- 민감 정보 하드코딩 (API keys, secrets)
- Business logic 중복 구현 (backend가 source of truth)

#### 10.1.2 Backend Responsibilities

**ALLOWED**:
- 모든 비즈니스 로직 실행
- 데이터베이스 CRUD 작업
- JWT 발급 및 검증
- 외부 API 호출 (Gemini AI)
- Validation 및 인증/인가

**FORBIDDEN**:
- UI 렌더링 로직 포함
- Frontend 상태 관리 (stateless API만)
- CORS 전체 허용 (`*` 금지, 명시적 origin만)

**INVARIANT**:
- Frontend는 backend API 없이 작동 불가
- Backend는 frontend 없이 API 테스트 가능해야 함

### 10.2 User vs Admin Permissions

#### 10.2.1 USER Role

**ALLOWED**:
- 자신의 프로필 조회/수정
- 자신의 stats 조회/업데이트
- 채팅 메시지 전송
- 출석 체크
- 친밀도/AI 리포트 조회

**FORBIDDEN**:
- 다른 사용자 정보 접근
- 관리자 API 접근 (`/api/admin/**`)
- Database 직접 조회

#### 10.2.2 ADMIN Role

**ALLOWED**:
- 모든 사용자 정보 조회 (비밀번호 제외)
- 사용자 계정 생성/수정/삭제
- 통계 데이터 조회
- 시스템 설정 변경

**FORBIDDEN**:
- 다른 사용자 비밀번호 평문 조회 (해시만 가능)
- Production 데이터베이스 직접 수정 (마이그레이션 스크립트 필수)

**INVARIANT**:
- Role은 `USER` 또는 `ADMIN`만 허용 (enum 확장 금지)
- Role 변경은 ADMIN만 가능
- 최소 1명의 ADMIN은 항상 존재해야 함 (마지막 ADMIN 삭제 금지)

### 10.3 External API Access

#### 10.3.1 Gemini AI Integration

**INVARIANT**:
- API Key는 환경 변수로만 관리 (`GEMINI_API_KEY`)
- 코드에 하드코딩 절대 금지
- Production/Staging 환경별 별도 키 사용

**Rate Limiting**:
- 사용자당 분당 최대 10회 AI 호출
- 초과 시 `429 Too Many Requests`

**Error Handling**:
- Gemini API 장애 시 graceful degradation
- 사용자에게 명확한 에러 메시지 반환

#### 10.3.2 Database Access Rules

**INVARIANT**:
- Production DB는 application server만 접근 가능
- 개발자 직접 접근은 read-only replica 사용
- Write 작업은 마이그레이션 스크립트 또는 admin API 경유 필수

### 10.4 Security Boundaries

#### 10.4.1 Input Validation

**INVARIANT**:
- 모든 사용자 입력은 backend에서 validation 필수
- Frontend validation은 UX 개선 목적만 (보안 검증 아님)
- SQL Injection 방지: JPA prepared statements만 사용
- XSS 방지: 사용자 입력은 항상 escape 처리

#### 10.4.2 Password Security

**INVARIANT**:
- 비밀번호는 BCrypt로 해싱 (strength: 12)
- 평문 비밀번호는 절대 저장 금지
- 비밀번호 재설정 시에도 평문 전송 금지 (임시 토큰 사용)

#### 10.4.3 CORS Policy

**ALLOWED ORIGINS** (환경별):
- Development: `http://localhost:5173`, `http://localhost:3000`
- Production: `https://lobai.com` (배포 시 업데이트)

**INVARIANT**:
- `Access-Control-Allow-Origin: *` 절대 금지
- Credentials 포함 요청은 명시적 origin만 허용

---

## 헌법 변경 이력

| Version | Date       | Changed Sections | Reason                     | Approved By |
|---------|------------|------------------|----------------------------|-------------|
| 1.0.0   | 2026-01-04 | Initial version  | Project constitution setup | Team        |

---

## 부록: 참조 문서

이 헌법은 **불변 원칙**만 다룹니다. 구체적인 개발 가이드는 다음 문서를 참조하세요:

- **DEV_GUIDE.md**: 개발 프로세스, 코딩 컨벤션, Git 전략
- **TEST_GUIDE.md**: 테스트 방법론, TDD 가이드, E2E 테스트
- **CICD_GUIDE.md**: CI/CD 파이프라인, 배포 프로세스
- **CONTEXT_GUIDE.md**: AI 컨텍스트 관리, 프롬프트 엔지니어링
- **INCIDENT_PLAYBOOK.md**: 장애 대응 절차, 롤백 가이드

---

**END OF CONSTITUTION**
**이 문서의 수정은 프로젝트 전체의 합의를 필요로 합니다.**
