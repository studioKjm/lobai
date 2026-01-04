# Development Guide

**LobAI 프로젝트 개발 가이드**
**Version**: 1.0.0
**Last Updated**: 2026-01-04

이 문서는 LobAI 프로젝트의 개발 프로세스, 코딩 컨벤션, Git 전략을 다룹니다.
불변 원칙은 [PROJECT_CONSTITUTION.md](../../PROJECT_CONSTITUTION.md)를 참조하세요.

---

## Table of Contents

1. [개발 환경 설정](#1-개발-환경-설정)
2. [프로젝트 구조](#2-프로젝트-구조)
3. [코딩 컨벤션](#3-코딩-컨벤션)
4. [Git Workflow](#4-git-workflow)
5. [개발 프로세스](#5-개발-프로세스)
6. [버전 관리 및 릴리스](#6-버전-관리-및-릴리스)
7. [트러블슈팅](#7-트러블슈팅)

---

## 1. 개발 환경 설정

### 1.1 필수 요구사항

#### Frontend
```bash
# Node.js (v20 이상 권장)
node --version  # v20.0.0+

# npm (v10 이상)
npm --version   # v10.0.0+
```

#### Backend
```bash
# Java (17 이상 필수)
java --version  # openjdk 17+

# Gradle (wrapper 사용, 별도 설치 불필요)
./gradlew --version
```

#### Database
```bash
# MySQL (8.0 이상 권장)
mysql --version  # 8.0+

# 또는 Docker 사용
docker run --name lobai-mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:8.0
```

### 1.2 프로젝트 클론 및 설치

```bash
# 1. 저장소 클론
git clone https://github.com/your-org/lobai.git
cd lobai

# 2. Frontend 의존성 설치
npm install

# 3. Backend 의존성 설치 (Gradle이 자동으로 처리)
cd backend
./gradlew build
```

### 1.3 환경 변수 설정

#### Frontend: `.env.local`

루트 디렉토리에 생성:

```env
# API Base URL
VITE_API_URL=http://localhost:8080/api

# Gemini API Key (frontend에서는 사용 안 함, backend만 사용)
# GEMINI_API_KEY는 backend에만 설정
```

#### Backend: `backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lobai?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: your_password_here
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update  # Production에서는 validate로 변경
    show-sql: true
    properties:
      hibernate:
        format_sql: true

# Gemini API Configuration
gemini:
  api-key: ${GEMINI_API_KEY}  # 환경 변수로 주입
  model: gemini-2.0-flash-exp
  base-url: https://generativelanguage.googleapis.com/v1beta

# JWT Configuration
jwt:
  secret: your-secret-key-here-should-be-at-least-256-bits
  access-token-expiry: 900000      # 15분 (ms)
  refresh-token-expiry: 604800000  # 7일 (ms)
```

**환경 변수 주입 (권장)**:

```bash
# Backend 실행 시 환경 변수로 주입
export GEMINI_API_KEY=your_api_key_here
export JWT_SECRET=your_jwt_secret_here
./gradlew bootRun
```

### 1.4 IDE 설정

#### VSCode (Frontend 권장)

**필수 확장**:
- ESLint
- Prettier
- Tailwind CSS IntelliSense
- TypeScript Vue Plugin (Volar)

**.vscode/settings.json**:

```json
{
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "typescript.tsdk": "node_modules/typescript/lib",
  "tailwindCSS.experimental.classRegex": [
    ["cva\\(([^)]*)\\)", "[\"'`]([^\"'`]*).*?[\"'`]"]
  ]
}
```

#### IntelliJ IDEA (Backend 권장)

**필수 플러그인**:
- Lombok
- Spring Boot
- Database Navigator

**설정**:
1. Preferences → Build, Execution, Deployment → Compiler → Annotation Processors
   - ✅ Enable annotation processing (Lombok 활성화)
2. Preferences → Editor → Code Style → Java
   - Indent: 4 spaces
   - Continuation indent: 4 spaces

### 1.5 개발 서버 실행

#### Terminal 1: Backend

```bash
cd backend
./gradlew bootRun

# 또는 background 실행
nohup ./gradlew bootRun > backend.log 2>&1 &

# 서버 확인
curl http://localhost:8080/api/auth/me
```

#### Terminal 2: Frontend

```bash
npm run dev

# 브라우저 자동 오픈 (http://localhost:5173)
```

#### Database 초기화 (필요 시)

```sql
-- MySQL 접속
mysql -u root -p

-- Database 생성
CREATE DATABASE lobai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 (선택)
CREATE USER 'lobai_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON lobai.* TO 'lobai_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## 2. 프로젝트 구조

### 2.1 전체 구조

```
lobai/
├── backend/                    # Spring Boot 백엔드
│   ├── src/main/java/com/lobai/
│   │   ├── controller/         # REST API endpoints
│   │   ├── service/            # 비즈니스 로직
│   │   ├── repository/         # JPA 리포지토리
│   │   ├── entity/             # JPA 엔티티
│   │   ├── dto/                # 데이터 전송 객체
│   │   │   ├── request/        # API 요청 DTO
│   │   │   └── response/       # API 응답 DTO
│   │   ├── config/             # Spring 설정 클래스
│   │   ├── security/           # JWT, Spring Security
│   │   ├── exception/          # 예외 처리
│   │   └── util/               # 유틸리티 클래스
│   └── src/main/resources/
│       ├── application.yml     # Spring 설정
│       └── data.sql            # 초기 데이터 (선택)
│
├── src/                        # React 프론트엔드
│   ├── components/             # React 컴포넌트
│   │   ├── common/             # 공통 컴포넌트 (Button, Input 등)
│   │   ├── layout/             # 레이아웃 (Header, Footer 등)
│   │   ├── chat/               # 채팅 기능
│   │   ├── stats/              # 스탯 표시
│   │   ├── attendance/         # 출석 체크
│   │   ├── affinity/           # 친밀도
│   │   └── admin/              # 관리자 페이지
│   ├── pages/                  # 페이지 컴포넌트
│   │   ├── HomePage.tsx
│   │   ├── ChatPage.tsx
│   │   ├── AdminPage.tsx
│   │   └── ...
│   ├── hooks/                  # Custom React hooks
│   │   ├── useAuth.ts
│   │   ├── useStatsDecay.ts
│   │   └── ...
│   ├── services/               # API 클라이언트
│   │   ├── authApi.ts
│   │   ├── chatApi.ts
│   │   └── ...
│   ├── stores/                 # Zustand 상태 관리
│   │   └── authStore.ts
│   ├── types/                  # TypeScript 타입 정의
│   ├── lib/                    # 유틸리티 및 헬퍼
│   │   └── api.ts              # Axios 인스턴스
│   └── assets/                 # 정적 파일
│
├── docs/                       # 문서
│   ├── guides/                 # 개발 가이드
│   └── runbooks/               # 장애 대응 매뉴얼
│
├── PROJECT_CONSTITUTION.md     # 프로젝트 헌법
├── CLAUDE.md                   # Claude Code 지침
├── package.json                # Frontend 의존성
├── vite.config.ts              # Vite 설정
├── tailwind.config.js          # TailwindCSS 설정
└── tsconfig.json               # TypeScript 설정
```

### 2.2 모듈 의존성 규칙

**계층 구조** (헌법 참조):

```
Controller → Service → Repository → Entity
     ↓
    DTO
```

**절대 금지**:
- ❌ Repository에서 Service 호출
- ❌ Entity에서 Service 호출
- ❌ DTO에 비즈니스 로직 포함

**Frontend 계층**:

```
Page → Feature Component → Common Component
  ↓
Hook → Service → API Client
```

**절대 금지**:
- ❌ Common component에서 feature component import
- ❌ Page에서 service 직접 호출 (hook 경유 필수)
- ❌ Service에서 React hooks 사용

---

## 3. 코딩 컨벤션

### 3.1 Java (Backend)

#### 3.1.1 명명 규칙

```java
// Class: PascalCase
public class UserService { }

// Method: camelCase (동사로 시작)
public void updateUserStats() { }

// Variable: camelCase
private String userName;

// Constant: UPPER_SNAKE_CASE
private static final int MAX_RETRY_COUNT = 3;

// Package: lowercase
package com.lobai.service;
```

#### 3.1.2 Lombok 사용

**필수 사용**:
- `@Slf4j` - 로깅
- `@RequiredArgsConstructor` - DI (생성자 주입)
- `@Builder` - 객체 생성 (Entity, DTO)
- `@Getter` - Getter 자동 생성

**사용 금지**:
- `@Data` - 너무 많은 메서드 자동 생성
- `@Setter` - Entity는 불변성 유지, DTO만 제한적 사용

**예시**:

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Register request received for email: {}", request.getEmail());

        // Business logic here

        return AuthResponse.builder()
            .accessToken(token)
            .userId(user.getId())
            .build();
    }
}
```

#### 3.1.3 주석 스타일

**Javadoc (public 메서드 필수)**:

```java
/**
 * 사용자 등록
 *
 * @param request 회원가입 요청 데이터
 * @return 인증 응답 (JWT 토큰 포함)
 * @throws IllegalArgumentException 이메일 중복 시
 */
@Transactional
public AuthResponse register(RegisterRequest request) {
    // Implementation
}
```

**Inline 주석 (복잡한 로직에만)**:

```java
// 1. 이메일 중복 체크
if (userRepository.existsByEmail(request.getEmail())) {
    throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
}

// 2. 비밀번호 해싱 및 사용자 생성
User user = User.builder()
    .email(request.getEmail())
    .passwordHash(passwordEncoder.encode(request.getPassword()))
    .build();
```

#### 3.1.4 에러 처리

**Service 계층**:

```java
// IllegalArgumentException 사용 (비즈니스 로직 위반)
if (userRepository.existsByEmail(email)) {
    throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
}

// Custom exception도 가능 (Phase 3+)
throw new DuplicateEmailException(email);
```

**Controller 계층**:

```java
// GlobalExceptionHandler가 자동 처리
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(e.getMessage(), "INVALID_ARGUMENT"));
}
```

### 3.2 TypeScript (Frontend)

#### 3.2.1 명명 규칙

```typescript
// Component: PascalCase
const UserProfile: React.FC = () => { }

// Function: camelCase
function fetchUserData() { }

// Variable: camelCase
const userName = 'John';

// Constant: UPPER_SNAKE_CASE 또는 camelCase
const API_BASE_URL = 'http://localhost:8080/api';
const maxRetryCount = 3;

// Type/Interface: PascalCase
interface UserData {
  id: number;
  name: string;
}
```

#### 3.2.2 React 컴포넌트 스타일

**함수형 컴포넌트 (권장)**:

```typescript
import React from 'react';

interface ButtonProps {
  label: string;
  onClick: () => void;
  variant?: 'primary' | 'secondary';
}

const Button: React.FC<ButtonProps> = ({ label, onClick, variant = 'primary' }) => {
  return (
    <button
      onClick={onClick}
      className={`btn btn-${variant}`}
    >
      {label}
    </button>
  );
};

export default Button;
```

**Custom Hook**:

```typescript
import { useEffect, useState } from 'react';
import { authApi } from '@/services/authApi';

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const response = await authApi.getCurrentUser();
        if (response.data.success) {
          setUser(response.data.data);
        }
      } catch (error) {
        console.error('Auth check failed:', error);
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, []);

  return { user, loading };
};
```

#### 3.2.3 API 서비스 스타일

```typescript
import api, { ApiResponse } from '@/lib/api';

interface LoginRequest {
  email: string;
  password: string;
}

interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: number;
  email: string;
  username: string;
}

export const authApi = {
  login: async (data: LoginRequest) => {
    return api.post<ApiResponse<AuthResponse>>('/auth/login', data);
  },

  getCurrentUser: async () => {
    return api.get<ApiResponse<UserResponse>>('/auth/me');
  },
};
```

#### 3.2.4 타입 안정성

**any 사용 금지** (불가피한 경우 주석 필수):

```typescript
// ❌ Bad
const handleData = (data: any) => {
  console.log(data);
};

// ✅ Good
interface UserData {
  id: number;
  name: string;
}

const handleData = (data: UserData) => {
  console.log(data.name);
};

// ⚠️ Unavoidable (주석 필수)
// TODO: Gemini API response 타입 정의 필요
const handleAIResponse = (response: any) => {
  console.log(response);
};
```

### 3.3 CSS/Tailwind

#### 3.3.1 TailwindCSS 우선

**권장**:

```tsx
<button className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600">
  Click me
</button>
```

**커스텀 CSS (공통 패턴에만)**:

```css
/* src/index.css */
@layer components {
  .btn-primary {
    @apply px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition;
  }
}
```

#### 3.3.2 반응형 디자인

```tsx
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
  {/* Mobile: 1열, Tablet: 2열, Desktop: 3열 */}
</div>
```

---

## 4. Git Workflow

### 4.1 Branch 전략

**Main Branches**:
- `main` - Production 코드 (항상 배포 가능)
- `develop` - 개발 브랜치 (다음 릴리스 준비)

**Supporting Branches**:
- `feature/*` - 새 기능 개발
- `bugfix/*` - 버그 수정
- `hotfix/*` - 긴급 Production 버그 수정
- `release/*` - 릴리스 준비

**Branch 명명 규칙**:

```bash
feature/user-authentication
feature/admin-dashboard
bugfix/stats-decay-calculation
hotfix/critical-security-patch
release/v1.2.0
```

### 4.2 Git Commit Convention

**Conventional Commits** 사용:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat` - 새 기능 추가
- `fix` - 버그 수정
- `docs` - 문서 변경
- `style` - 코드 포맷팅 (동작 변경 없음)
- `refactor` - 리팩토링
- `test` - 테스트 추가/수정
- `chore` - 빌드/도구 설정 변경

**예시**:

```bash
# Good
git commit -m "feat(auth): Add JWT refresh token rotation"
git commit -m "fix(stats): Clamp stats to 0-100 range"
git commit -m "docs(readme): Update installation instructions"

# Bad
git commit -m "update code"
git commit -m "fix bug"
git commit -m "WIP"
```

**Body 포함 예시** (복잡한 변경):

```
feat(affinity): Implement affinity score calculation

- Add AffinityScoreService with weighted scoring
- Create /api/affinity/score endpoint
- Add sentiment analysis integration with Gemini

Closes #123
```

### 4.3 PR (Pull Request) 프로세스

#### 4.3.1 PR 생성 전 체크리스트

```bash
# 1. 최신 develop 반영
git checkout develop
git pull origin develop
git checkout feature/your-feature
git merge develop

# 2. 테스트 실행
npm run test          # Frontend
./gradlew test        # Backend

# 3. 빌드 확인
npm run build         # Frontend
./gradlew build       # Backend

# 4. Lint 검사
npm run lint          # Frontend (ESLint)

# 5. TypeScript 컴파일 체크
npx tsc --noEmit      # Frontend
```

#### 4.3.2 PR 템플릿

```markdown
## 📋 Summary
Brief description of what this PR does.

## 🎯 Motivation
Why is this change necessary? What problem does it solve?

## 🔧 Changes
- Change 1
- Change 2
- Change 3

## 🧪 Test Plan
How did you test this?
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manually tested on local environment
- [ ] E2E tests pass

## 📸 Screenshots (if applicable)
Before / After screenshots for UI changes

## ✅ Checklist
- [ ] Code follows project conventions
- [ ] Self-reviewed the code
- [ ] Added/updated tests
- [ ] Documentation updated (if needed)
- [ ] No console.log or debug statements
- [ ] All tests pass locally
- [ ] Build succeeds

## 🔗 Related Issues
Closes #123
Relates to #456
```

#### 4.3.3 Code Review 가이드

**Reviewer 체크사항**:
1. ✅ 비즈니스 로직이 헌법(PROJECT_CONSTITUTION.md)을 위반하지 않는가?
2. ✅ 테스트가 충분한가? (커버리지 80% 이상)
3. ✅ 보안 취약점이 없는가? (SQL injection, XSS 등)
4. ✅ 성능 이슈가 없는가? (N+1 쿼리, 무한 루프 등)
5. ✅ 에러 처리가 적절한가?
6. ✅ 코드가 읽기 쉬운가? (복잡도 낮은가?)

**승인 기준**:
- 최소 1명의 승인 필요
- CI 통과 필수 (모든 테스트 통과)
- 충돌 없음

### 4.4 Git 명령어 Quick Reference

```bash
# 새 기능 브랜치 생성
git checkout -b feature/new-feature develop

# 변경사항 확인
git status
git diff

# 스테이징 및 커밋
git add .
git commit -m "feat(chat): Add real-time message updates"

# Remote에 푸시
git push origin feature/new-feature

# PR 머지 후 브랜치 삭제
git checkout develop
git pull origin develop
git branch -d feature/new-feature

# Hotfix (긴급 수정)
git checkout -b hotfix/critical-bug main
# ... fix and commit
git checkout main
git merge hotfix/critical-bug
git tag v1.0.1
git push origin main --tags
```

---

## 5. 개발 프로세스

### 5.1 이슈 → 개발 → PR → 머지 플로우

```
1. GitHub Issue 생성
   ↓
2. Feature Branch 생성
   ↓
3. 개발 (TDD 권장)
   ↓
4. 로컬 테스트
   ↓
5. PR 생성
   ↓
6. CI 실행 (자동)
   ↓
7. Code Review
   ↓
8. Merge to develop
   ↓
9. Staging 배포 (자동)
   ↓
10. QA 검증
   ↓
11. Release Branch 생성
   ↓
12. Merge to main
   ↓
13. Production 배포
```

### 5.2 이슈 관리

**Issue Labels**:
- `feature` - 새 기능
- `bug` - 버그
- `documentation` - 문서
- `enhancement` - 기능 개선
- `priority: high` - 높은 우선순위
- `priority: low` - 낮은 우선순위
- `good first issue` - 초보자 적합

**Issue Template**:

```markdown
## 🐛 Bug Report / ✨ Feature Request

### Description
Clear description of the bug or feature

### Steps to Reproduce (for bugs)
1. Go to '...'
2. Click on '...'
3. See error

### Expected Behavior
What should happen

### Actual Behavior
What actually happens

### Environment
- OS: macOS 14
- Browser: Chrome 120
- Node: v20.0.0
- Java: 17

### Additional Context
Any other relevant information
```

### 5.3 Feature Flag 사용 (Phase 3+)

**실험적 기능 개발 시**:

```typescript
// Frontend
const FEATURE_FLAGS = {
  ENABLE_NEW_CHAT_UI: import.meta.env.VITE_FEATURE_NEW_CHAT === 'true',
  ENABLE_VOICE_INPUT: false,
};

// Usage
{FEATURE_FLAGS.ENABLE_NEW_CHAT_UI ? <NewChatUI /> : <OldChatUI />}
```

```java
// Backend
@Value("${feature.enable-advanced-ai:false}")
private boolean enableAdvancedAI;

if (enableAdvancedAI) {
    return geminiService.generateAdvancedResponse(message);
} else {
    return geminiService.generateBasicResponse(message);
}
```

---

## 6. 버전 관리 및 릴리스

### 6.1 Semantic Versioning

**형식**: `MAJOR.MINOR.PATCH`

- **MAJOR**: 호환성 깨지는 변경 (Breaking changes)
- **MINOR**: 하위 호환 가능한 기능 추가
- **PATCH**: 하위 호환 가능한 버그 수정

**예시**:
- `1.0.0` → `1.0.1` (버그 수정)
- `1.0.1` → `1.1.0` (새 기능 추가, 하위 호환)
- `1.1.0` → `2.0.0` (API 변경, 하위 호환 불가)

### 6.2 Release 프로세스

```bash
# 1. Release 브랜치 생성
git checkout -b release/v1.2.0 develop

# 2. 버전 업데이트
# - package.json (frontend)
# - build.gradle (backend)
# - CHANGELOG.md 업데이트

# 3. 최종 테스트
npm run test
./gradlew test

# 4. Merge to main
git checkout main
git merge release/v1.2.0

# 5. Tag 생성
git tag -a v1.2.0 -m "Release version 1.2.0"
git push origin main --tags

# 6. Merge back to develop
git checkout develop
git merge release/v1.2.0

# 7. Release 브랜치 삭제
git branch -d release/v1.2.0
```

### 6.3 CHANGELOG.md 관리

**형식**:

```markdown
# Changelog

All notable changes to this project will be documented in this file.

## [1.2.0] - 2026-01-15

### Added
- Affinity score calculation feature
- Admin dashboard with user statistics

### Changed
- Improved AI response quality with Gemini 2.0

### Fixed
- Stats decay calculation bug
- JWT token refresh race condition

### Security
- Updated dependencies to fix CVE-2024-XXXXX

## [1.1.0] - 2026-01-01

...
```

### 6.4 Hotfix 프로세스

```bash
# 1. main에서 hotfix 브랜치 생성
git checkout -b hotfix/v1.1.1 main

# 2. 버그 수정 및 커밋
git commit -m "fix(auth): Fix critical JWT validation bug"

# 3. Merge to main
git checkout main
git merge hotfix/v1.1.1
git tag v1.1.1
git push origin main --tags

# 4. Merge to develop
git checkout develop
git merge hotfix/v1.1.1

# 5. Hotfix 브랜치 삭제
git branch -d hotfix/v1.1.1
```

---

## 7. 트러블슈팅

### 7.1 자주 발생하는 문제

#### Frontend

**문제**: `TypeError: Cannot read property 'data' of undefined`

```typescript
// ❌ Bad
const user = response.data.data.user;

// ✅ Good
const user = response?.data?.data?.user;
// or
if (response && response.data && response.data.success) {
  const user = response.data.data.user;
}
```

**문제**: CORS 에러

```
Access to XMLHttpRequest at 'http://localhost:8080/api/auth/login'
from origin 'http://localhost:5173' has been blocked by CORS policy
```

**해결**:
1. Backend의 `CorsConfig.java` 확인
2. `allowedOrigins`에 `http://localhost:5173` 포함 확인

**문제**: Vite 환경 변수가 로드되지 않음

```typescript
// ❌ Wrong
console.log(process.env.API_URL);

// ✅ Correct
console.log(import.meta.env.VITE_API_URL);
```

#### Backend

**문제**: `java.lang.NullPointerException` at `SecurityUtil.getCurrentUserId()`

**원인**: 인증되지 않은 사용자가 인증 필요 endpoint 호출

**해결**:
```java
// Controller에서 Optional 처리 또는 @PreAuthorize 사용
@GetMapping("/me")
public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
    try {
        Long userId = SecurityUtil.getCurrentUserId();
        // ...
    } catch (IllegalStateException e) {
        return ResponseEntity.ok(ApiResponse.success("Not authenticated", null));
    }
}
```

**문제**: Lombok이 작동하지 않음

**해결**:
1. IntelliJ: Preferences → Plugins → Lombok 설치
2. Preferences → Annotation Processors → Enable annotation processing ✅
3. Rebuild project

### 7.2 성능 최적화 팁

#### N+1 쿼리 문제

```java
// ❌ Bad (N+1 발생)
@OneToMany(mappedBy = "user")
private List<Message> messages;

// ✅ Good (Fetch Join 사용)
@Query("SELECT u FROM User u LEFT JOIN FETCH u.messages WHERE u.id = :userId")
User findByIdWithMessages(@Param("userId") Long userId);
```

#### React 불필요한 리렌더링

```typescript
// ❌ Bad (매번 새 객체 생성 → 리렌더링)
const config = { apiUrl: API_BASE_URL };

// ✅ Good (useMemo 사용)
const config = useMemo(() => ({ apiUrl: API_BASE_URL }), []);
```

### 7.3 디버깅 도구

#### Frontend

```bash
# React DevTools 사용
# Chrome Extension: React Developer Tools

# Network 탭에서 API 요청 확인
# Application 탭에서 localStorage 확인
```

#### Backend

```bash
# 로그 레벨 조정 (application.yml)
logging:
  level:
    com.lobai: DEBUG
    org.springframework.security: DEBUG

# H2 Console 활성화 (개발 환경)
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

---

## Changelog

| Version | Date       | Changes                                 | Author |
|---------|------------|-----------------------------------------|--------|
| 1.0.0   | 2026-01-04 | Initial DEV_GUIDE created               | Team   |

---

**다음 문서**: [TEST_GUIDE.md](./TEST_GUIDE.md)
**관련 문서**: [PROJECT_CONSTITUTION.md](../../PROJECT_CONSTITUTION.md), [CICD_GUIDE.md](./CICD_GUIDE.md)
