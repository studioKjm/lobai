# Phase 2 Implementation Plan
## Frontend-Backend Integration + Admin Panel

**Created**: 2025-12-31
**Scope**: Phase 2 - Frontend Integration + Admin Panel
**Timeline**: 3-4 weeks
**Status**: Planning

---

## Executive Summary

Phase 1 백엔드 구축이 완료되었으므로, Phase 2에서는:
1. **프론트엔드-백엔드 완전 연동** (Gemini 직접 호출 제거 → Backend API 사용)
2. **관리자 페이지 구현** (사용자 관리, 대화 모니터링, 통계 대시보드)
3. **실무 표준 적용** (에러 처리, 로딩 상태, 폼 검증, 보안)
4. **MCP/Agent/Skill 활용** (자동화된 개발 워크플로우)

---

## 🏗️ 관리자 페이지 구축 위치 추천

### ✅ 추천: 메인 프론트엔드 내부 (/admin 라우트)

**구조**:
```
lobai/
├── index.html
├── src/
│   ├── App.tsx                    # 메인 애플리케이션 (기존)
│   ├── pages/
│   │   ├── LandingPage.tsx       # 기존 랜딩 페이지 분리
│   │   ├── ChatPage.tsx          # 메인 채팅 페이지
│   │   └── admin/
│   │       ├── AdminLayout.tsx   # 관리자 레이아웃
│   │       ├── Dashboard.tsx     # 대시보드
│   │       ├── UserManagement.tsx
│   │       ├── MessageMonitor.tsx
│   │       └── PersonaConfig.tsx
│   ├── components/
│   │   ├── common/               # 공통 컴포넌트
│   │   ├── chat/                 # 채팅 관련
│   │   └── admin/                # 관리자 전용
│   └── router.tsx                # React Router 설정
```

### 선택 근거

#### ✅ 장점
1. **코드 재사용성**
   - 이미 구현된 UI 컴포넌트 재사용 (glassmorphism, 폰트, 색상)
   - API 클라이언트 공유 (axios 인스턴스)
   - 인증 로직 공유 (JWT 토큰 관리)

2. **개발 효율성**
   - 단일 빌드 파이프라인
   - 하나의 개발 서버 (`npm run dev`)
   - 통합된 타입 정의 (TypeScript)

3. **배포 단순화**
   - 단일 프로덕션 빌드 (`npm run build`)
   - 하나의 도메인/서브도메인
   - 간소한 CI/CD 파이프라인

4. **보안**
   - 백엔드에서 역할 기반 접근 제어 (ROLE_ADMIN)
   - React Router로 클라이언트 측 라우트 보호
   - JWT 토큰에 role 클레임 포함

#### ⚠️ 고려사항
1. **번들 크기 증가** → Code splitting으로 해결
2. **일반 사용자 노출** → 역할 기반 라우트 가드

### 대안 (권장하지 않음)

**옵션 A: 별도 React 애플리케이션**
- ❌ 중복 코드 (컴포넌트, API 클라이언트)
- ❌ 2개의 빌드/배포 파이프라인
- ❌ 개발 환경 관리 복잡도 증가
- ✅ 완전한 격리 (보안상 장점)
- **결론**: 현재 프로젝트 규모에 오버엔지니어링

**옵션 B: 백엔드 템플릿 엔진 (Thymeleaf)**
- ❌ Spring Boot + React 혼용 (일관성 부족)
- ❌ 프론트엔드 기술 스택 분리
- ❌ 상태 관리 복잡도
- ✅ 간단한 CRUD는 빠름
- **결론**: 이미 React로 구축된 프로젝트와 맞지 않음

---

## 📋 Phase 2 구현 범위

### 1️⃣ 프론트엔드-백엔드 연동 (Week 1-2)

#### A. 기술 스택 업그레이드
- **상태 관리**: Zustand 또는 Jotai (가벼운 상태 관리)
- **라우팅**: React Router v6
- **HTTP 클라이언트**: Axios (인터셉터로 JWT 자동 첨부)
- **폼 관리**: React Hook Form + Zod (타입 안전 검증)
- **UI 컴포넌트**: Headless UI (접근성) + 기존 glassmorphism

#### B. 인증 시스템
- 로그인/회원가입 모달
- JWT 토큰 관리 (localStorage + 메모리)
- 토큰 자동 갱신 (Refresh Token)
- 로그아웃 처리
- Protected Routes (인증 필요한 페이지)

#### C. 채팅 기능 연동
- Gemini 직접 호출 **제거**
- `POST /api/messages` 사용
- 메시지 히스토리 로드 (`GET /api/messages`)
- 페르소나 전환 UI
- 실시간 Stats 동기화

#### D. Stats 시스템 연동
- `GET /api/stats` - 현재 Stats 조회
- `PUT /api/stats` - Feed/Play/Sleep 버튼
- `POST /api/stats/decay` - 5초마다 자동 감소
- Stats 히스토리 그래프 (선택사항)

### 2️⃣ 관리자 페이지 구현 (Week 2-3)

#### A. 관리자 인증
- **백엔드**: User 엔티티에 `role` 필드 추가 (ENUM: USER, ADMIN)
- **프론트엔드**: JWT 토큰에서 role 확인
- **라우트 가드**: AdminRoute 컴포넌트

#### B. 대시보드 (Dashboard)
```
/admin/dashboard
```
**기능**:
- 전체 사용자 수
- 오늘 가입자 수
- 총 메시지 수 (user + bot)
- 오늘 메시지 수
- 페르소나별 사용 통계 (파이 차트)
- 시간대별 활동 그래프 (라인 차트)
- 평균 Stats (hunger/energy/happiness)

**구현**:
- 백엔드 API: `GET /api/admin/stats`
- 차트 라이브러리: Recharts 또는 Chart.js

#### C. 사용자 관리 (User Management)
```
/admin/users
```
**기능**:
- 사용자 목록 (페이지네이션)
- 검색 (이메일, 이름)
- 필터링 (가입일, 활동 상태)
- 사용자 상세 조회
  - 기본 정보 (이메일, 이름, 가입일, 마지막 로그인)
  - 현재 Stats
  - 총 메시지 수
  - 선호 페르소나
- 사용자 비활성화/활성화
- 사용자 삭제 (Soft Delete)

**구현**:
- 백엔드 API:
  - `GET /api/admin/users?page=0&size=20&search=...`
  - `GET /api/admin/users/{id}`
  - `PUT /api/admin/users/{id}/status` (활성화/비활성화)
  - `DELETE /api/admin/users/{id}` (Soft Delete)

#### D. 대화 모니터링 (Message Monitor)
```
/admin/messages
```
**기능**:
- 전체 메시지 조회 (실시간 스트림)
- 사용자별 필터
- 페르소나별 필터
- 날짜 범위 필터
- 메시지 검색 (키워드)
- 문제 메시지 플래그 (부적절한 내용)
- 메시지 삭제

**구현**:
- 백엔드 API:
  - `GET /api/admin/messages?userId=...&personaId=...&keyword=...`
  - `PUT /api/admin/messages/{id}/flag`
  - `DELETE /api/admin/messages/{id}`

#### E. 페르소나 설정 (Persona Configuration)
```
/admin/personas
```
**기능**:
- 5개 페르소나 목록
- 페르소나 수정
  - 이름 (한글, 영문)
  - 아이콘 이모지
  - System Instruction 편집
  - 활성화/비활성화
- 페르소나 추가 (선택사항 - Phase 3)
- 사용 통계 보기

**구현**:
- 백엔드 API:
  - `GET /api/admin/personas`
  - `PUT /api/admin/personas/{id}`
  - `POST /api/admin/personas` (Phase 3)

#### F. 시스템 설정 (System Settings)
```
/admin/settings
```
**기능**:
- Gemini API 키 관리
- Stats 기본값 설정 (초기 hunger/energy/happiness)
- Stats 감소율 설정
- Action 효과 설정 (feed +20, play +15/-10, etc.)
- 로그 레벨 설정
- 시스템 헬스 체크

**구현**:
- 백엔드 API:
  - `GET /api/admin/settings`
  - `PUT /api/admin/settings`
- 설정 저장: DB 또는 application.yml (재시작 필요)

---

## 🛠️ 기술 스택 상세

### 프론트엔드 추가 라이브러리

```json
{
  "dependencies": {
    "react-router-dom": "^6.22.0",        // 라우팅
    "zustand": "^4.5.0",                   // 상태 관리 (선택: Jotai)
    "axios": "^1.6.5",                     // HTTP 클라이언트
    "react-hook-form": "^7.49.3",         // 폼 관리
    "zod": "^3.22.4",                      // 스키마 검증
    "@headlessui/react": "^1.7.18",       // 접근성 UI
    "recharts": "^2.12.0",                 // 차트 (선택: chart.js)
    "date-fns": "^3.2.0",                  // 날짜 포맷팅
    "react-hot-toast": "^2.4.1"           // 토스트 알림
  },
  "devDependencies": {
    "@tanstack/react-query": "^5.17.19",  // 서버 상태 관리 (선택사항)
    "tailwindcss": "^3.4.1"                // CSS 프레임워크 (CDN → npm)
  }
}
```

### 백엔드 추가 구현

#### 1. Role 기반 인증
```java
// User.java
public enum UserRole {
    USER, ADMIN
}

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private UserRole role = UserRole.USER;
```

#### 2. Admin 전용 컨트롤러
```
AdminStatsController.java
AdminUserController.java
AdminMessageController.java
AdminPersonaController.java
AdminSettingsController.java
```

#### 3. 보안 설정 업데이트
```java
// SecurityConfig.java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

---

## 📅 세부 구현 일정 (4 Weeks)

### Week 1: 프론트엔드 기반 구축 (5-7일)

#### Day 1-2: 프로젝트 리팩토링
- **현재 `index.tsx` 분리**:
  - `App.tsx` - 라우터 설정
  - `pages/LandingPage.tsx` - 기존 랜딩
  - `pages/ChatPage.tsx` - 채팅 페이지
  - `components/` 폴더 구조 생성
- **라이브러리 설치**:
  ```bash
  npm install react-router-dom zustand axios react-hook-form zod @headlessui/react recharts date-fns react-hot-toast
  ```
- **Tailwind CSS npm 전환** (CDN 제거)
- **폴더 구조 확립**

**사용 도구**:
- 🤖 **Refactor Agent** - index.tsx 분리
- 🛠️ **Bash** - npm install

#### Day 3-4: 인증 시스템 구현
- **API 클라이언트 설정** (`src/lib/api.ts`):
  - Axios 인스턴스
  - 인터셉터 (JWT 자동 첨부)
  - Refresh Token 자동 갱신
- **인증 상태 관리** (Zustand):
  - `useAuthStore` - login/logout/register
  - 토큰 저장/로드 (localStorage)
- **로그인/회원가입 모달**:
  - React Hook Form + Zod 검증
  - 에러 메시지 표시
  - 로딩 상태
- **Protected Route 컴포넌트**

**사용 도구**:
- 🤖 **Plan Agent** - 아키텍처 설계
- 💻 **직접 구현** - TypeScript 코드 작성
- 🛠️ **Playwright MCP** - E2E 테스트

#### Day 5-7: 채팅 기능 연동
- **Gemini 직접 호출 제거**:
  - `POST /api/messages` 사용
  - 에러 처리 (네트워크, 서버 에러)
  - 로딩 스피너
- **메시지 히스토리 로드**:
  - `GET /api/messages` 호출
  - 페이지네이션 (무한 스크롤 or 페이지 버튼)
- **페르소나 전환 UI**:
  - 5개 페르소나 버튼
  - `PUT /api/personas/current`
  - 현재 페르소나 표시
- **Stats 동기화**:
  - `GET /api/stats` - 초기 로드
  - `PUT /api/stats` - Feed/Play/Sleep
  - `POST /api/stats/decay` - 타이머
  - 낙관적 UI 업데이트

**사용 도구**:
- 🤖 **General-purpose Agent** - 복잡한 상태 로직
- 🛠️ **Bash** - 개발 서버 실행
- 🧪 **Playwright MCP** - 통합 테스트

---

### Week 2: 백엔드 Admin API 구현 (5-7일)

#### Day 1-2: Admin 인증 및 권한
- **User 엔티티 수정**:
  - `role` 필드 추가 (ENUM: USER, ADMIN)
  - 마이그레이션 스크립트 작성
- **JWT에 role 포함**:
  - JwtTokenProvider 수정
  - 토큰 페이로드에 role 추가
- **Security 설정**:
  - `/api/admin/**` → ROLE_ADMIN 필요
  - @PreAuthorize 어노테이션
- **첫 관리자 생성**:
  - data.sql에 admin 계정 추가
  - 또는 별도 스크립트

**사용 도구**:
- 💻 **직접 구현** - Spring Security
- 🗄️ **MySQL MCP** - 스키마 변경 확인
- 🛠️ **Bash** - Gradle 빌드

#### Day 3-4: Admin Stats API
- **AdminStatsController**:
  - `GET /api/admin/stats/overview` - 대시보드 통계
  - `GET /api/admin/stats/persona-usage` - 페르소나별 사용
  - `GET /api/admin/stats/activity` - 시간대별 활동
- **AdminStatsService**:
  - 사용자 수 집계
  - 메시지 수 집계
  - 날짜 범위 필터
- **DTO 생성**:
  - DashboardStatsResponse
  - PersonaUsageResponse
  - ActivityResponse

**사용 도구**:
- 🤖 **Backend Developer Agent** (스킬 활용)
- 🗄️ **MySQL MCP** - 쿼리 테스트
- 🧪 **Postman** - API 테스트

#### Day 5-7: Admin User/Message API
- **AdminUserController**:
  - `GET /api/admin/users` - 페이지네이션, 검색
  - `GET /api/admin/users/{id}` - 상세 조회
  - `PUT /api/admin/users/{id}/status` - 활성화/비활성화
  - `DELETE /api/admin/users/{id}` - Soft Delete
- **AdminMessageController**:
  - `GET /api/admin/messages` - 필터, 검색
  - `PUT /api/admin/messages/{id}/flag`
  - `DELETE /api/admin/messages/{id}`
- **AdminPersonaController**:
  - `GET /api/admin/personas` - 전체 조회 (비활성 포함)
  - `PUT /api/admin/personas/{id}` - 수정

**사용 도구**:
- 🤖 **General-purpose Agent** - CRUD 코드 생성
- 🗄️ **MySQL MCP** - 복잡한 쿼리 최적화
- 🧪 **Postman** - 통합 테스트

---

### Week 3: 관리자 페이지 프론트엔드 (5-7일)

#### Day 1-2: 관리자 레이아웃
- **AdminLayout 컴포넌트**:
  - 사이드바 네비게이션
  - 헤더 (로그아웃 버튼)
  - 푸터
- **AdminRoute 가드**:
  - JWT 토큰에서 role 확인
  - ADMIN 아니면 리디렉트
- **라우터 설정**:
  ```tsx
  <Route path="/admin" element={<AdminRoute><AdminLayout /></AdminRoute>}>
    <Route index element={<Dashboard />} />
    <Route path="users" element={<UserManagement />} />
    <Route path="messages" element={<MessageMonitor />} />
    <Route path="personas" element={<PersonaConfig />} />
  </Route>
  ```

**사용 도구**:
- 🎨 **Frontend Design Skill** - UI/UX 디자인
- 💻 **직접 구현** - React 컴포넌트

#### Day 3-4: 대시보드 구현
- **Dashboard 페이지**:
  - API 호출: `GET /api/admin/stats/overview`
  - 통계 카드 (사용자 수, 메시지 수, etc.)
  - Recharts로 그래프:
    - 페르소나별 사용 파이 차트
    - 시간대별 활동 라인 차트
  - 로딩 상태, 에러 처리
- **실시간 업데이트** (선택사항):
  - React Query로 5초마다 refetch

**사용 도구**:
- 🎨 **Frontend Design Skill** - 차트 디자인
- 📊 **Recharts** - 차트 라이브러리
- 🤖 **General-purpose Agent** - 데이터 변환 로직

#### Day 5-7: 사용자/메시지 관리
- **UserManagement 페이지**:
  - 사용자 테이블 (페이지네이션)
  - 검색창 (debounce)
  - 필터 (가입일, 활동 상태)
  - 상세 모달 (사용자 클릭 시)
  - 활성화/비활성화 버튼
  - 삭제 확인 다이얼로그
- **MessageMonitor 페이지**:
  - 메시지 리스트 (무한 스크롤)
  - 필터 (사용자, 페르소나, 날짜)
  - 검색 (키워드)
  - 플래그/삭제 버튼
- **PersonaConfig 페이지**:
  - 페르소나 카드 (5개)
  - 수정 모달 (이름, 이모지, instruction)
  - 활성화/비활성화 토글

**사용 도구**:
- 🎨 **Frontend Design Skill** - 복잡한 테이블 UI
- 🤖 **General-purpose Agent** - 필터링/검색 로직
- 🛠️ **React Hook Form** - 폼 관리

---

### Week 4: 통합 테스트 및 배포 준비 (5-7일)

#### Day 1-2: E2E 테스트
- **Playwright 테스트 작성**:
  - 로그인 플로우
  - 회원가입 플로우
  - 채팅 메시지 전송
  - Stats 업데이트
  - 페르소나 전환
  - 관리자 로그인
  - 대시보드 조회
  - 사용자 관리 CRUD
- **테스트 자동화**:
  - GitHub Actions에 통합
  - PR마다 자동 실행

**사용 도구**:
- 🧪 **Playwright MCP** - E2E 테스트 자동 생성
- 🤖 **General-purpose Agent** - 테스트 시나리오 작성
- 🔗 **GitHub MCP** - CI/CD 설정

#### Day 3-4: 성능 최적화
- **Code Splitting**:
  - React.lazy로 관리자 페이지 분리
  - 번들 크기 분석 (webpack-bundle-analyzer)
- **API 최적화**:
  - React Query로 캐싱
  - Debounce/Throttle 적용
  - 낙관적 UI 업데이트
- **이미지 최적화**:
  - Spline 로딩 최적화 (이미 완료)
  - Lazy loading

**사용 도구**:
- 🤖 **Performance Agent** (커스텀 에이전트)
- 🛠️ **Vite build** - 번들 분석

#### Day 5-6: 배포 준비
- **환경 변수 분리**:
  - `.env.development`
  - `.env.production`
- **프로덕션 빌드**:
  - `npm run build`
  - 빌드 최적화 확인
- **Docker 컨테이너** (선택사항):
  - Dockerfile (프론트엔드)
  - Dockerfile (백엔드)
  - docker-compose.yml
- **배포 스크립트**:
  - VPS 배포 자동화
  - Nginx 설정
  - HTTPS (Let's Encrypt)

**사용 도구**:
- 🐳 **Docker** - 컨테이너화
- 🤖 **DevOps Agent** (커스텀)
- 🔗 **GitHub MCP** - 배포 자동화

#### Day 7: 문서화 및 핸드오버
- **API 문서 업데이트**:
  - Admin API 엔드포인트 문서화
  - Postman Collection 생성
- **사용자 가이드**:
  - 관리자 페이지 사용법
  - 일반 사용자 가이드
- **코드 리뷰**:
  - 전체 코드 베이스 리뷰
  - 보안 체크리스트
  - 성능 체크리스트

**사용 도구**:
- 🤖 **Documentation Agent** (커스텀)
- 🔍 **Code Review Skill** - 자동 리뷰
- 📝 **Markdown** - 문서 작성

---

## 🤖 MCP/Agent/Skill 활용 전략

### MCP Servers 활용

#### 1. MySQL MCP
**사용 시점**:
- 데이터베이스 스키마 변경 시 (role 필드 추가)
- 복잡한 통계 쿼리 작성 시
- 페이지네이션 쿼리 최적화
- 인덱스 생성/확인

**예시**:
```
"MySQL MCP로 users 테이블에 role 컬럼 추가해줘"
"페르소나별 사용 빈도 쿼리 최적화해줘"
```

#### 2. GitHub MCP
**사용 시점**:
- PR 생성 (각 주요 기능 완료 시)
- 코드 리뷰 요청
- Issue 생성 (버그 추적)
- GitHub Actions 워크플로우 설정

**예시**:
```
"Week 1 완료 후 PR 생성해줘: Frontend Auth System"
"Playwright E2E 테스트 워크플로우 추가해줘"
```

#### 3. Playwright MCP
**사용 시점**:
- E2E 테스트 작성
- UI 회귀 테스트
- 크로스 브라우저 테스트
- 접근성 테스트

**예시**:
```
"로그인 플로우 E2E 테스트 작성해줘"
"관리자 대시보드 로딩 테스트"
```

### Subagents 활용

#### 1. Plan Agent
**사용 시점**: 각 주요 기능 구현 전 아키텍처 설계
```
"Admin Stats API 설계해줘"
→ DTO 구조, Service 로직, Controller 엔드포인트 계획
```

#### 2. Explore Agent
**사용 시점**: 기존 코드베이스 이해 필요할 때
```
"현재 Stats 관리 로직 어디서 처리되는지 찾아줘"
"Gemini API 호출 부분 모두 찾아줘"
```

#### 3. General-purpose Agent
**사용 시점**: 복잡한 멀티 스텝 작업
```
"인증 시스템 전체 구현해줘 (API 클라이언트 + Zustand + 모달)"
"사용자 관리 CRUD 전체 구현해줘"
```

#### 4. Code Review Agent (커스텀 필요)
**사용 시점**: 각 주 완료 시
```
"Week 1 코드 리뷰해줘"
→ 보안 취약점, 성능 이슈, 베스트 프랙티스 위반 체크
```

### Skills 활용

#### 1. Frontend Design Skill
**사용 시점**: UI 컴포넌트 디자인
```
"관리자 대시보드 레이아웃 디자인해줘"
"사용자 관리 테이블 UI 구현해줘"
```

#### 2. Code Review Skill
**사용 시점**: PR 생성 전
```
"/review-pr"
→ 자동으로 코드 품질, 보안, 성능 체크
```

---

## ✅ 실무 표준 적용 항목

### 1. 코드 품질

#### TypeScript 엄격 모드
```json
// tsconfig.json
{
  "compilerOptions": {
    "strict": true,
    "noUncheckedIndexedAccess": true,
    "noImplicitOverride": true
  }
}
```

#### ESLint + Prettier
```json
// .eslintrc.json
{
  "extends": [
    "eslint:recommended",
    "plugin:@typescript-eslint/recommended",
    "plugin:react/recommended",
    "plugin:react-hooks/recommended"
  ]
}
```

#### 네이밍 컨벤션
- **컴포넌트**: PascalCase (`UserManagement.tsx`)
- **함수/변수**: camelCase (`getUserList`)
- **상수**: UPPER_SNAKE_CASE (`API_BASE_URL`)
- **타입/인터페이스**: PascalCase (`UserResponse`)

### 2. 에러 처리

#### API 에러 처리
```typescript
// src/lib/api.ts
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // 토큰 만료 → 자동 갱신 시도
      await refreshToken();
      return axios(error.config);
    }

    if (error.response?.status === 403) {
      // 권한 없음 → 홈으로 리디렉트
      window.location.href = '/';
    }

    // 에러 메시지 표시
    toast.error(error.response?.data?.message || 'An error occurred');
    return Promise.reject(error);
  }
);
```

#### 폼 검증
```typescript
// Zod 스키마
const loginSchema = z.object({
  email: z.string().email('Invalid email format'),
  password: z.string().min(8, 'Password must be at least 8 characters')
});

// React Hook Form
const { register, handleSubmit, formState: { errors } } = useForm({
  resolver: zodResolver(loginSchema)
});
```

### 3. 보안

#### XSS 방지
- React의 자동 이스케이핑 활용
- `dangerouslySetInnerHTML` 사용 금지
- 사용자 입력 sanitize (DOMPurify)

#### CSRF 방지
- JWT 사용으로 기본 방어
- SameSite 쿠키 설정 (선택사항)

#### Secure Headers
```typescript
// Nginx 설정
add_header X-Frame-Options "SAMEORIGIN";
add_header X-Content-Type-Options "nosniff";
add_header X-XSS-Protection "1; mode=block";
add_header Content-Security-Policy "default-src 'self'";
```

### 4. 성능

#### Code Splitting
```typescript
// 관리자 페이지 lazy load
const AdminLayout = lazy(() => import('./pages/admin/AdminLayout'));

<Suspense fallback={<LoadingSpinner />}>
  <AdminLayout />
</Suspense>
```

#### API 캐싱
```typescript
// React Query
const { data, isLoading } = useQuery({
  queryKey: ['users', page, search],
  queryFn: () => fetchUsers(page, search),
  staleTime: 5 * 60 * 1000, // 5분
  cacheTime: 10 * 60 * 1000 // 10분
});
```

#### 이미지 최적화
- WebP 포맷 사용
- Lazy loading (`loading="lazy"`)
- Responsive images (`srcset`)

### 5. 접근성 (a11y)

#### ARIA 속성
```tsx
<button
  aria-label="Close modal"
  aria-expanded={isOpen}
  onClick={handleClose}
>
  <CloseIcon />
</button>
```

#### 키보드 네비게이션
- Tab 순서 적절하게 설정
- 모든 interactive 요소 키보드 접근 가능
- Escape로 모달 닫기

#### 색상 대비
- WCAG 2.1 AA 기준 (4.5:1 이상)
- 색맹 사용자 고려

### 6. 테스트

#### 단위 테스트
```typescript
// src/lib/auth.test.ts
describe('Auth Utils', () => {
  it('should decode JWT token', () => {
    const token = 'eyJhbGciOiJIUzI1NiJ9...';
    const decoded = decodeToken(token);
    expect(decoded.userId).toBe(1);
  });
});
```

#### 통합 테스트
```typescript
// Playwright
test('User login flow', async ({ page }) => {
  await page.goto('http://localhost:3000');
  await page.click('text=Login');
  await page.fill('input[name="email"]', 'test@example.com');
  await page.fill('input[name="password"]', 'password123');
  await page.click('button[type="submit"]');
  await expect(page).toHaveURL('/chat');
});
```

---

## 📊 성공 기준 (Definition of Done)

### Phase 2 완료 조건

#### 프론트엔드-백엔드 연동 ✅
- [x] Gemini 직접 호출 완전 제거
- [x] 모든 API 엔드포인트 연동 완료
- [x] 인증 시스템 작동 (로그인/회원가입/로그아웃)
- [x] 토큰 자동 갱신 작동
- [x] 메시지 히스토리 로드 및 표시
- [x] 페르소나 전환 UI 작동
- [x] Stats 실시간 동기화
- [x] 에러 처리 및 로딩 상태 구현

#### 관리자 페이지 ✅
- [x] 역할 기반 인증 작동 (ADMIN만 접근)
- [x] 대시보드 통계 정확하게 표시
- [x] 사용자 관리 CRUD 작동
- [x] 메시지 모니터링 및 필터링 작동
- [x] 페르소나 설정 수정 작동
- [x] 모든 차트 정상 렌더링
- [x] 반응형 디자인 (모바일 포함)

#### 코드 품질 ✅
- [x] TypeScript 에러 0개
- [x] ESLint 경고 0개
- [x] 모든 컴포넌트 타입 안전
- [x] 재사용 가능한 컴포넌트 분리
- [x] 적절한 폴더 구조

#### 테스트 ✅
- [x] E2E 테스트 커버리지 > 80%
- [x] 주요 플로우 모두 테스트됨
- [x] CI/CD 파이프라인 구축

#### 성능 ✅
- [x] Lighthouse Performance > 90
- [x] First Contentful Paint < 1.5s
- [x] 번들 크기 < 500KB (gzipped)
- [x] API 응답 시간 < 200ms

#### 보안 ✅
- [x] XSS 취약점 없음
- [x] CSRF 방어 구현
- [x] SQL Injection 방어 (JPA 자동)
- [x] 민감 정보 환경 변수로 관리
- [x] HTTPS 적용 (프로덕션)

---

## 📝 체크리스트 (Week by Week)

### Week 1 체크리스트
- [ ] React Router 설치 및 설정
- [ ] index.tsx 분리 (App, LandingPage, ChatPage)
- [ ] 폴더 구조 생성 (components, pages, lib, hooks)
- [ ] Axios 클라이언트 설정
- [ ] Zustand 인증 스토어 생성
- [ ] 로그인/회원가입 모달 UI
- [ ] Protected Route 구현
- [ ] Gemini 호출 제거 → Backend API 연동
- [ ] 메시지 히스토리 로드
- [ ] 페르소나 전환 UI
- [ ] Stats 동기화
- [ ] E2E 테스트 (기본 플로우)

### Week 2 체크리스트
- [ ] User.role 필드 추가 (마이그레이션)
- [ ] JWT에 role 포함
- [ ] Security 설정 업데이트
- [ ] Admin 계정 생성
- [ ] AdminStatsController 구현
- [ ] AdminUserController 구현
- [ ] AdminMessageController 구현
- [ ] AdminPersonaController 구현
- [ ] 모든 Admin API 테스트 (Postman)

### Week 3 체크리스트
- [ ] AdminLayout 컴포넌트
- [ ] AdminRoute 가드
- [ ] Dashboard 페이지 (통계 + 차트)
- [ ] UserManagement 페이지 (테이블 + 모달)
- [ ] MessageMonitor 페이지 (필터 + 검색)
- [ ] PersonaConfig 페이지 (카드 + 수정 모달)
- [ ] 모든 관리자 페이지 반응형 확인

### Week 4 체크리스트
- [ ] Playwright E2E 테스트 작성
- [ ] GitHub Actions CI/CD 설정
- [ ] Code splitting 적용
- [ ] React Query 캐싱 최적화
- [ ] 번들 크기 분석 및 최적화
- [ ] Docker 컨테이너화
- [ ] VPS 배포
- [ ] HTTPS 설정
- [ ] 문서화 (API, 사용자 가이드)
- [ ] 코드 리뷰 및 보안 체크

---

## 🎯 리스크 관리

### 잠재적 리스크

#### 1. 기술적 리스크
**리스크**: React Router 마이그레이션 시 기존 기능 깨짐
**완화 방안**:
- 점진적 마이그레이션 (landing page 먼저, chat page 나중)
- 각 단계마다 E2E 테스트 실행

#### 2. 성능 리스크
**리스크**: 관리자 페이지 추가로 번들 크기 증가
**완화 방안**:
- Code splitting (lazy loading)
- Tree shaking 최적화
- 번들 분석 도구 사용

#### 3. 보안 리스크
**리스크**: 관리자 권한 우회 가능성
**완화 방안**:
- 백엔드에서 이중 검증 (JWT role + DB role 확인)
- 프론트엔드 라우트 가드는 UX 목적만
- 모든 민감한 작업 백엔드에서 재검증

#### 4. 일정 리스크
**리스크**: 예상보다 복잡한 기능으로 일정 지연
**완화 방안**:
- MVP 기능 먼저 구현 (Phase 2.1)
- 부가 기능은 Phase 3로 연기
- 매주 진행 상황 체크포인트

---

## 📈 다음 단계 (Phase 3)

Phase 2 완료 후 고려할 기능:

1. **대화 요약 기능** (`/summary` 명령어)
2. **AI 친화도 리포트 생성**
3. **대화 패턴 분석** (sentiment/clarity score 자동 계산)
4. **일일 미션/이벤트 시스템**
5. **사용자 말투 학습 및 메일 초안 작성**
6. **실시간 알림** (WebSocket)
7. **모바일 앱** (React Native)
8. **다국어 지원** (i18n)

---

## 🚀 시작하기

### 1단계: 승인 및 계획 확정
- 이 계획서 검토
- 우선순위 조정 (필요 시)
- 예산 및 리소스 확인

### 2단계: 개발 환경 준비
```bash
# 프론트엔드 라이브러리 설치
npm install react-router-dom zustand axios react-hook-form zod @headlessui/react recharts date-fns react-hot-toast

# 백엔드 서버 시작
cd backend
./gradlew bootRun

# 프론트엔드 개발 서버 시작
npm run dev
```

### 3단계: Week 1 Day 1 시작
- index.tsx 분리 작업 시작
- Refactor Agent 활용

---

**계획 작성일**: 2025-12-31
**예상 완료일**: 2026-01-28 (4주 후)
**담당자**: Development Team
**승인 대기 중**
