# Context Management Guide

**LobAI 프로젝트 컨텍스트 관리 가이드**
**Version**: 1.0.0
**Last Updated**: 2026-01-04

이 문서는 Claude Code와 효과적으로 협업하기 위한 컨텍스트 관리, 문서화 전략, 프롬프트 엔지니어링 팁을 다룹니다.

---

## Table of Contents

1. [CLAUDE.md 관리](#1-claudemd-관리)
2. [문서 작성 원칙](#2-문서-작성-원칙)
3. [컨텍스트 파일 관리](#3-컨텍스트-파일-관리)
4. [프롬프트 엔지니어링](#4-프롬프트-엔지니어링)
5. [문서 갱신 주기](#5-문서-갱신-주기)
6. [Claude Code 활용 팁](#6-claude-code-활용-팁)

---

## 1. CLAUDE.md 관리

### 1.1 CLAUDE.md의 목적

**CLAUDE.md**는 프로젝트 루트에 위치하며, Claude Code가 프로젝트를 이해하는 데 필요한 핵심 정보를 제공합니다.

**포함되어야 할 내용**:
- 프로젝트 개요 (1-2 문단)
- 기술 스택 및 의존성
- 폴더 구조
- 주요 파일 설명
- 개발 환경 설정 방법
- 중요한 제약사항 및 금지 사항

**포함하지 말아야 할 것**:
- ❌ 세부적인 API 문서 (별도 문서로 분리)
- ❌ 긴 코드 예시 (300 lines 이내 유지)
- ❌ 변경 이력 (Git history 활용)
- ❌ 할 일 목록 (Issue tracker 사용)

### 1.2 CLAUDE.md 템플릿

```markdown
# CLAUDE.md

This file provides guidance to Claude Code when working with this codebase.

## Project Overview

**LobAI** - Brief description (1-2 paragraphs)

## Tech Stack

**Frontend**:
- React 19 + TypeScript + Vite
- TailwindCSS
- Zustand (state management)
- Axios (HTTP client)

**Backend**:
- Spring Boot 3.x + Java 17
- MySQL 8.0
- JWT authentication
- Gemini AI integration

## Development Commands

\`\`\`bash
# Frontend
npm install
npm run dev        # http://localhost:5173
npm run build

# Backend
cd backend
./gradlew bootRun  # http://localhost:8080
./gradlew test
\`\`\`

## Environment Setup

\`\`\`env
# .env.local (Frontend)
VITE_API_URL=http://localhost:8080/api

# backend/src/main/resources/application.yml (Backend)
gemini.api-key=${GEMINI_API_KEY}
\`\`\`

## Architecture

### Folder Structure

\`\`\`
lobai/
├── backend/          # Spring Boot
│   └── src/main/java/com/lobai/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       └── entity/
├── src/              # React
│   ├── components/
│   ├── pages/
│   ├── services/
│   └── stores/
└── docs/             # Documentation
\`\`\`

### Key Design Patterns

- **Backend**: Controller → Service → Repository
- **Frontend**: Page → Component → Hook → Service

## Important Constraints

**NEVER**:
- Change database schema without migration script
- Commit secrets or API keys
- Use `any` type in TypeScript without comment
- Skip writing tests for new features

**ALWAYS**:
- Follow naming conventions (see DEV_GUIDE.md)
- Write JSDoc/Javadoc for public methods
- Run tests before committing
- Use environment variables for configuration

## File References

- [DEV_GUIDE.md](docs/guides/DEV_GUIDE.md) - Development conventions
- [TEST_GUIDE.md](docs/guides/TEST_GUIDE.md) - Testing strategy
- [PROJECT_CONSTITUTION.md](PROJECT_CONSTITUTION.md) - Immutable principles
\`\`\`

### 1.3 업데이트 시점

**즉시 업데이트 필요**:
- ✅ 새로운 기술 스택 추가 (예: Redis, RabbitMQ)
- ✅ 폴더 구조 변경
- ✅ 주요 아키텍처 변경 (예: Monolith → Microservices)
- ✅ 개발 환경 설정 변경

**정기 업데이트** (월 1회):
- ✅ 의존성 버전 업데이트
- ✅ 프로젝트 개요 개선
- ✅ 예시 코드 최신화

---

## 2. 문서 작성 원칙

### 2.1 명확성 (Clarity)

**DO**:
```markdown
✅ Good
## Authentication Flow

1. User submits email and password
2. Backend validates credentials
3. JWT token is generated and returned
4. Frontend stores token in localStorage
5. Token is sent in Authorization header for subsequent requests
```

**DON'T**:
```markdown
❌ Bad
## Authentication

The system uses JWT for auth. Tokens are stored and used for requests.
```

### 2.2 간결성 (Conciseness)

**DO**:
```markdown
✅ Good
Use `npm run dev` to start the development server on port 5173.
```

**DON'T**:
```markdown
❌ Bad
In order to start the development server, you need to execute the npm run dev command, which will start the Vite development server that serves your application on localhost port 5173, allowing you to see changes in real-time as you develop.
```

### 2.3 코드 예시 포함

**DO**:
```markdown
✅ Good
## Creating a new API endpoint

1. Define the endpoint in the controller:

\`\`\`java
@GetMapping("/users/{id}")
public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
    UserResponse user = userService.getUser(id);
    return ResponseEntity.ok(ApiResponse.success(user));
}
\`\`\`

2. Implement the service method:

\`\`\`java
public UserResponse getUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    return UserResponse.from(user);
}
\`\`\`
```

**DON'T**:
```markdown
❌ Bad
## Creating a new API endpoint

Create a controller method with @GetMapping annotation and call the service.
```

### 2.4 변경 이력 기록

**DO**:
```markdown
✅ Good
## Changelog

| Version | Date       | Changes                        | Author |
|---------|------------|--------------------------------|--------|
| 1.1.0   | 2026-01-15 | Added Redis caching section    | Alice  |
| 1.0.0   | 2026-01-01 | Initial documentation          | Bob    |
```

---

## 3. 컨텍스트 파일 관리

### 3.1 프로젝트 구조 요약

**docs/PROJECT_STRUCTURE.md**:

```markdown
# Project Structure

## Overview

\`\`\`
lobai/                                  # Root directory
├── backend/                            # Spring Boot backend
│   ├── src/main/java/com/lobai/
│   │   ├── controller/                 # REST API endpoints (35 files)
│   │   │   ├── AuthController.java     # /api/auth/* endpoints
│   │   │   ├── UserAdminController.java # /api/admin/users/* endpoints
│   │   │   └── ...
│   │   ├── service/                    # Business logic (28 files)
│   │   │   ├── AuthService.java        # Authentication logic
│   │   │   ├── AffinityScoreService.java
│   │   │   └── ...
│   │   ├── repository/                 # JPA repositories (15 files)
│   │   ├── entity/                     # Database entities (12 files)
│   │   │   ├── User.java               # User entity (CORE)
│   │   │   ├── Message.java
│   │   │   └── ...
│   │   └── dto/                        # Data transfer objects
│   │       ├── request/                # API request DTOs
│   │       └── response/               # API response DTOs
│   └── src/main/resources/
│       ├── application.yml             # Spring configuration
│       └── db/migration/               # Flyway migrations
│
├── src/                                # React frontend
│   ├── components/                     # React components (45 files)
│   │   ├── common/                     # Shared components (8 files)
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   └── ...
│   │   ├── layout/                     # Layout components
│   │   ├── chat/                       # Chat feature (12 files)
│   │   ├── admin/                      # Admin panel (8 files)
│   │   └── ...
│   ├── pages/                          # Page components (7 files)
│   │   ├── HomePage.tsx
│   │   ├── ChatPage.tsx
│   │   ├── AdminPage.tsx
│   │   └── ...
│   ├── services/                       # API clients (10 files)
│   │   ├── authApi.ts                  # /api/auth API calls
│   │   ├── chatApi.ts                  # /api/chat API calls
│   │   └── ...
│   ├── hooks/                          # Custom React hooks (8 files)
│   ├── stores/                         # Zustand stores (3 files)
│   ├── types/                          # TypeScript types (6 files)
│   └── lib/                            # Utilities
│       └── api.ts                      # Axios instance (CORE)
│
└── docs/                               # Documentation
    ├── guides/
    │   ├── DEV_GUIDE.md
    │   ├── TEST_GUIDE.md
    │   └── CICD_GUIDE.md
    └── runbooks/
        └── INCIDENT_PLAYBOOK.md
\`\`\`

## Core Files (Must Read First)

1. **CLAUDE.md** - Project overview
2. **PROJECT_CONSTITUTION.md** - Immutable principles
3. **backend/src/main/java/com/lobai/entity/User.java** - Core entity
4. **src/lib/api.ts** - Frontend API client
5. **backend/src/main/java/com/lobai/dto/response/ApiResponse.java** - API contract
```

### 3.2 API Endpoint 목록

**docs/API_ENDPOINTS.md**:

```markdown
# API Endpoints

## Authentication

| Method | Endpoint            | Description         | Auth Required |
|--------|---------------------|---------------------|---------------|
| POST   | /api/auth/register  | Register new user   | No            |
| POST   | /api/auth/login     | Login               | No            |
| POST   | /api/auth/refresh   | Refresh access token| No            |
| POST   | /api/auth/logout    | Logout              | Yes           |
| GET    | /api/auth/me        | Get current user    | Optional      |

## Chat

| Method | Endpoint            | Description         | Auth Required |
|--------|---------------------|---------------------|---------------|
| POST   | /api/chat/send      | Send message to AI  | Yes           |
| GET    | /api/chat/history   | Get chat history    | Yes           |

## Attendance

| Method | Endpoint                | Description         | Auth Required |
|--------|-------------------------|---------------------|---------------|
| POST   | /api/attendance/check-in| Check in            | Yes           |
| GET    | /api/attendance/status  | Get attendance stats| Yes           |

## Admin (ADMIN role only)

| Method | Endpoint                      | Description         | Auth Required |
|--------|-------------------------------|---------------------|---------------|
| GET    | /api/admin/users              | List all users      | Yes (ADMIN)   |
| GET    | /api/admin/users/{id}         | Get user details    | Yes (ADMIN)   |
| PUT    | /api/admin/users/{id}         | Update user         | Yes (ADMIN)   |
| DELETE | /api/admin/users/{id}         | Delete user         | Yes (ADMIN)   |
| GET    | /api/admin/stats/overview     | Get system stats    | Yes (ADMIN)   |
```

---

## 4. 프롬프트 엔지니어링

### 4.1 효과적인 요청 방법

#### 명확한 목표 제시

**DO**:
```
✅ Good
I need to add a new API endpoint for user profile updates.

Requirements:
- PUT /api/users/{id}/profile
- Only authenticated users can update their own profile
- Accept fields: username, profileImageUrl
- Return updated user data
- Add validation for username (3-50 chars)
```

**DON'T**:
```
❌ Bad
Can you help me with user profiles?
```

#### 컨텍스트 제공

**DO**:
```
✅ Good
I'm getting a 401 Unauthorized error when calling /api/chat/send.

Context:
- I'm logged in and have an access token
- Token is stored in localStorage
- Browser console shows: "Authorization header: Bearer eyJhbGc..."
- Backend logs show: "JWT validation failed: Token expired"

Request:
Help me implement token refresh logic when the access token expires.
```

**DON'T**:
```
❌ Bad
I'm getting an error. Fix it.
```

### 4.2 코드 리뷰 요청

**DO**:
```
✅ Good
Please review this AuthService.register() method:

Focus areas:
1. Is the password hashing secure?
2. Are there any SQL injection vulnerabilities?
3. Should I add rate limiting?
4. Is error handling appropriate?

\`\`\`java
public AuthResponse register(RegisterRequest request) {
    // ... code here
}
\`\`\`
```

**DON'T**:
```
❌ Bad
Review my code.
```

### 4.3 디버깅 요청

**DO**:
```
✅ Good
I'm experiencing an N+1 query problem in UserService.getAllUsersWithMessages().

Steps to reproduce:
1. Call GET /api/admin/users
2. Check server logs - see 100+ SQL queries for 10 users

Expected: 1-2 queries (users + messages)
Actual: 1 query for users + 100 queries for messages (1 per user)

Code:
\`\`\`java
public List<UserResponse> getAllUsersWithMessages() {
    List<User> users = userRepository.findAll();
    return users.stream()
        .map(user -> {
            List<Message> messages = messageRepository.findByUserId(user.getId());
            return UserResponse.from(user, messages);
        })
        .collect(Collectors.toList());
}
\`\`\`

Request: Help me optimize this to use a fetch join.
```

**DON'T**:
```
❌ Bad
My API is slow. Make it faster.
```

### 4.4 테스트 작성 요청

**DO**:
```
✅ Good
Write unit tests for AffinityScoreService.calculateAffinityScore().

Test cases to cover:
1. Valid input with all scores
2. Edge case: all scores are 0
3. Edge case: all scores are 100
4. Correct weighting (sentiment: 30%, clarity: 20%, context: 25%, usage: 25%)
5. Correct level mapping (0-20: 1, 21-40: 2, etc.)

Use JUnit 5 + Mockito.
```

**DON'T**:
```
❌ Bad
Write tests for my service.
```

### 4.5 문서화 요청

**DO**:
```
✅ Good
Generate JSDoc comments for the calculateAffinityScore function.

Include:
- Function description
- @param descriptions with types
- @returns description with type
- @example usage

\`\`\`typescript
export function calculateAffinityScore(scores: AffinityScores): AffinityResult {
    // ... implementation
}
\`\`\`
```

**DON'T**:
```
❌ Bad
Add comments to my code.
```

---

## 5. 문서 갱신 주기

### 5.1 즉시 업데이트 (Immediate)

다음 변경사항 발생 시 즉시 문서 업데이트:

- ✅ 새로운 API endpoint 추가/변경
- ✅ Database schema 변경
- ✅ 환경 변수 추가
- ✅ 중요한 아키텍처 변경
- ✅ 보안 정책 변경

### 5.2 릴리스 전 업데이트 (Pre-release)

각 릴리스 전에 다음 문서 검토 및 업데이트:

- ✅ CLAUDE.md
- ✅ API_ENDPOINTS.md
- ✅ CHANGELOG.md
- ✅ README.md

### 5.3 정기 리뷰 (Quarterly)

분기별로 전체 문서 리뷰:

- ✅ 모든 가이드 문서 (DEV_GUIDE, TEST_GUIDE 등)
- ✅ 오래된 정보 제거
- ✅ 링크 유효성 검증
- ✅ 코드 예시 최신화

---

## 6. Claude Code 활용 팁

### 6.1 서브에이전트 활용

**Explore Agent (코드베이스 탐색)**:
```
Use the Explore agent to find all files related to user authentication.

Search for:
- Authentication controllers
- JWT token logic
- User repository
- Security configuration
```

**Plan Agent (구현 계획)**:
```
Use the Plan agent to design the implementation for:

Feature: Real-time chat notifications using WebSocket

Requirements:
- Server-side: Spring Boot WebSocket
- Client-side: React + SockJS
- Notify users when new messages arrive
- Show notification badge on navbar
```

### 6.2 MCP 서버 활용

**Context7 (라이브러리 문서)**:
```
Use Context7 to look up:
- React Query v5 documentation for data fetching
- Spring Security JWT configuration examples
```

### 6.3 스킬 활용

**/code-review 스킬**:
```
/code-review

Review the following pull request for:
- Security vulnerabilities
- Performance issues
- Code quality
- Test coverage

PR: #123 - Add user profile update feature
```

### 6.4 효율적인 작업 흐름

**1. 탐색 → 2. 계획 → 3. 구현 → 4. 테스트 → 5. 리뷰**:

```
Step 1: Explore
"Find all files related to attendance feature"

Step 2: Plan
"Create an implementation plan for AI resilience report generation"

Step 3: Implement
"Implement ResilienceAnalysisService.generateReport() based on the plan"

Step 4: Test
"Write unit tests for ResilienceAnalysisService"

Step 5: Review
"/code-review the ResilienceAnalysisService implementation"
```

### 6.5 금지 사항

**Claude Code에게 요청하지 말 것**:

❌ **Production Database 직접 수정**:
```
❌ Bad: "Delete all test users from production database"
✅ Good: "Write a migration script to archive old test users"
```

❌ **민감 정보 포함**:
```
❌ Bad: "Use API key: sk-1234567890abcdef"
✅ Good: "Use API key from environment variable GEMINI_API_KEY"
```

❌ **불명확한 요청**:
```
❌ Bad: "Make it better"
✅ Good: "Refactor this function to reduce complexity (current: 15 → target: < 10)"
```

---

## Changelog

| Version | Date       | Changes                          | Author |
|---------|------------|----------------------------------|--------|
| 1.0.0   | 2026-01-04 | Initial CONTEXT_GUIDE created    | Team   |

---

**관련 문서**:
- [CLAUDE.md](../../CLAUDE.md)
- [DEV_GUIDE.md](./DEV_GUIDE.md)
- [PROJECT_CONSTITUTION.md](../../PROJECT_CONSTITUTION.md)
