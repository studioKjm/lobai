# LobAI Backend & Database Design Plan
## Spring Boot + MySQL + JWT Authentication

**Created**: 2025-12-30
**Version**: 1.0
**Scope**: Phase 1 - Core Backend Infrastructure
**Timeline**: 4 weeks (80-100 hours)

---

## Executive Summary

LobAI 프로젝트에 **Spring Boot 백엔드**와 **MySQL 데이터베이스**를 추가하여 다음 기능을 구현합니다.

### Phase 1 범위 (4주)

✅ **핵심 기능**:
1. **사용자 인증 시스템** (Spring Security + JWT)
2. **AI 채팅 대화 저장 및 조회**
3. **Stats 영구화** (hunger/energy/happiness)
4. **멀티페르소나 기능** (5개: 친구/상담사/코치/전문가/유머)

### Phase 2 이후 (추후 확장)

🔮 **Advanced Features**:
- 대화 요약/리포트 생성
- 사용자 대화 패턴 분석 (AI 친화도/적응력)
- 일일 미션/이벤트 시스템
- 슬래시 명령어 (`/summary`, `/persona`, `/report`, `/draft`)
- 사용자 말투 학습 및 메시지 초안 작성
- 최초 채팅 시 인적사항 수집 대화

---

## Current State Analysis

### 프론트엔드 현황

**기술 스택**:
- React 19.2.3 + TypeScript 5.8.2 + Vite 6.2.0
- 단일 파일 아키텍처 (index.tsx, 635줄)
- Gemini API 직접 호출 (클라이언트)
- TailwindCSS + 3D Spline

**현재 문제점**:
1. ⚠️ API Key가 클라이언트에 노출됨 (vite.config.ts에서 주입)
2. ⚠️ 대화 히스토리 영구 저장 불가 (메모리 내 저장만)
3. ⚠️ 사용자 인증 없음
4. ⚠️ Stats가 새로고침 시 초기화됨

### 백엔드 현황

**현재 상태**: 없음 (완전히 클라이언트 사이드)

**필요사항**:
- Spring Boot API 서버
- MySQL 데이터베이스
- JWT 인증 시스템
- Gemini API 프록시 (보안)

---

## Database Schema Design

### 1. Users Table

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    profile_image_url VARCHAR(500),

    -- Stats (현재 값 저장)
    current_hunger INT DEFAULT 80 CHECK (current_hunger >= 0 AND current_hunger <= 100),
    current_energy INT DEFAULT 90 CHECK (current_energy >= 0 AND current_energy <= 100),
    current_happiness INT DEFAULT 70 CHECK (current_happiness >= 0 AND current_happiness <= 100),

    -- Persona
    current_persona_id BIGINT,

    -- OAuth (Phase 2)
    oauth_provider VARCHAR(50),  -- 'google', 'kakao', null
    oauth_id VARCHAR(255),

    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,

    INDEX idx_email (email),
    INDEX idx_oauth (oauth_provider, oauth_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. Personas Table (5개 페르소나)

```sql
CREATE TABLE personas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,           -- '친구', '상담사', '코치', '전문가', '유머'
    name_en VARCHAR(50) NOT NULL UNIQUE,         -- 'friend', 'counselor', 'coach', 'expert', 'humor'
    display_name VARCHAR(100) NOT NULL,          -- '친구모드', 'Counselor Mode'
    description TEXT,
    system_instruction TEXT NOT NULL,            -- Gemini API에 전달할 시스템 프롬프트
    icon_emoji VARCHAR(10),                      -- '👥', '💬', '🎯', '🎓', '😄'
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**초기 데이터 (data.sql)**:
```sql
INSERT INTO personas (name, name_en, display_name, description, system_instruction, icon_emoji, display_order) VALUES
('친구', 'friend', '친구모드', '캐주얼하고 편안한 대화 상대',
'당신은 사용자의 친한 친구입니다. 캐주얼한 말투를 사용하고, 공감을 잘 표현하며, 이모티콘을 적절히 사용합니다. 사용자의 기분을 살피고 함께 웃고 즐거워할 수 있는 대화를 나누세요. 답변은 1-2문장으로 짧고 친근하게 작성하세요.',
'👥', 1),

('상담사', 'counselor', '상담사모드', '경청하고 공감하는 심리 상담사',
'당신은 전문 심리 상담사입니다. 사용자의 말을 경청하고, 감정을 인정하며, 부드럽게 질문을 통해 스스로 답을 찾도록 돕습니다. 판단하지 않고, 지지와 공감을 표현하세요. 답변은 따뜻하고 신중하게 2-3문장으로 작성하세요.',
'💬', 2),

('코치', 'coach', '코치모드', '목표 달성을 돕는 실행 코치',
'당신은 실행력 있는 퍼스널 코치입니다. 사용자의 목표를 명확히 하고, 구체적인 행동 계획을 제시하며, 동기를 부여합니다. 긍정적이면서도 단호하게, 실천 가능한 다음 단계를 제안하세요. 답변은 명확하고 실행 지향적으로 2-3문장으로 작성하세요.',
'🎯', 3),

('전문가', 'expert', '전문가모드', '정확한 정보와 지식 전달',
'당신은 해당 분야의 전문가입니다. 정확하고 신뢰할 수 있는 정보를 제공하며, 논리적이고 체계적으로 설명합니다. 필요시 출처나 근거를 언급하고, 복잡한 개념을 이해하기 쉽게 풀어서 설명하세요. 답변은 정확하고 전문적으로 2-4문장으로 작성하세요.',
'🎓', 4),

('유머', 'humor', '유머모드', '재미있고 가벼운 대화',
'당신은 위트 있고 재미있는 친구입니다. 적절한 농담과 유머를 섞어가며 대화를 즐겁게 만듭니다. 긍정적이고 밝은 에너지를 전달하되, 상황에 맞지 않는 과도한 농담은 피하세요. 답변은 재치있고 가볍게 1-2문장으로 작성하세요.',
'😄', 5);
```

### 3. Messages Table (핵심 - 대화 저장)

```sql
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    persona_id BIGINT NOT NULL,

    -- Message content
    role ENUM('user', 'bot') NOT NULL,
    content TEXT NOT NULL,

    -- Context (선택)
    conversation_session_id VARCHAR(100),  -- 세션 그룹핑용

    -- AI metadata (Phase 2용)
    sentiment_score DECIMAL(5,2),  -- 감정 점수 (-1.0 ~ 1.0)
    clarity_score DECIMAL(5,2),    -- 명확성 점수 (0 ~ 100)
    token_count INT,               -- 토큰 수 (비용 추적)

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (persona_id) REFERENCES personas(id),

    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_session (conversation_session_id),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 4. User Stats History

```sql
CREATE TABLE user_stats_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    -- Stats snapshot
    hunger INT NOT NULL,
    energy INT NOT NULL,
    happiness INT NOT NULL,

    -- Action type
    action_type ENUM('feed', 'play', 'sleep', 'chat', 'decay') NOT NULL,
    delta_hunger INT DEFAULT 0,
    delta_energy INT DEFAULT 0,
    delta_happiness INT DEFAULT 0,

    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_user_time (user_id, created_at DESC),
    INDEX idx_action (action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 5. User Persona Settings

```sql
CREATE TABLE user_persona_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    persona_id BIGINT NOT NULL,

    -- 사용 통계
    total_messages INT DEFAULT 0,
    total_sessions INT DEFAULT 0,
    last_used_at TIMESTAMP,

    -- 선호도 (Phase 2)
    preference_score DECIMAL(5,2),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (persona_id) REFERENCES personas(id),

    UNIQUE KEY uk_user_persona (user_id, persona_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 6. Refresh Tokens (JWT 갱신용)

```sql
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_revoked BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_token (token),
    INDEX idx_user (user_id),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## Spring Boot Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── lobai/
│   │   │           ├── LobaiApplication.java
│   │   │           │
│   │   │           ├── config/
│   │   │           │   ├── SecurityConfig.java          # Spring Security + JWT 설정
│   │   │           │   ├── CorsConfig.java              # CORS 설정
│   │   │           │   ├── WebConfig.java               # Web MVC 설정
│   │   │           │   └── GeminiConfig.java            # Gemini API 설정
│   │   │           │
│   │   │           ├── controller/
│   │   │           │   ├── AuthController.java          # /api/auth/**
│   │   │           │   ├── MessageController.java       # /api/messages/**
│   │   │           │   ├── StatsController.java         # /api/stats/**
│   │   │           │   ├── PersonaController.java       # /api/personas/**
│   │   │           │   └── UserController.java          # /api/users/**
│   │   │           │
│   │   │           ├── service/
│   │   │           │   ├── AuthService.java             # 인증/회원가입 로직
│   │   │           │   ├── MessageService.java          # 메시지 저장/조회
│   │   │           │   ├── StatsService.java            # Stats 관리/자동감소
│   │   │           │   ├── PersonaService.java          # 페르소나 조회/변경
│   │   │           │   ├── GeminiService.java           # Gemini API 호출
│   │   │           │   └── UserService.java             # 사용자 정보 관리
│   │   │           │
│   │   │           ├── repository/
│   │   │           │   ├── UserRepository.java
│   │   │           │   ├── MessageRepository.java
│   │   │           │   ├── PersonaRepository.java
│   │   │           │   ├── UserStatsHistoryRepository.java
│   │   │           │   ├── UserPersonaSettingsRepository.java
│   │   │           │   └── RefreshTokenRepository.java
│   │   │           │
│   │   │           ├── entity/
│   │   │           │   ├── User.java
│   │   │           │   ├── Message.java
│   │   │           │   ├── Persona.java
│   │   │           │   ├── UserStatsHistory.java
│   │   │           │   ├── UserPersonaSettings.java
│   │   │           │   └── RefreshToken.java
│   │   │           │
│   │   │           ├── dto/
│   │   │           │   ├── request/
│   │   │           │   │   ├── LoginRequest.java
│   │   │           │   │   ├── RegisterRequest.java
│   │   │           │   │   ├── SendMessageRequest.java
│   │   │           │   │   ├── UpdateStatsRequest.java
│   │   │           │   │   └── ChangePersonaRequest.java
│   │   │           │   │
│   │   │           │   └── response/
│   │   │           │       ├── AuthResponse.java
│   │   │           │       ├── MessageResponse.java
│   │   │           │       ├── StatsResponse.java
│   │   │           │       ├── PersonaResponse.java
│   │   │           │       └── UserResponse.java
│   │   │           │
│   │   │           ├── security/
│   │   │           │   ├── JwtTokenProvider.java        # JWT 생성/검증
│   │   │           │   ├── JwtAuthenticationFilter.java # JWT 필터
│   │   │           │   ├── CustomUserDetails.java       # UserDetails 구현
│   │   │           │   └── CustomUserDetailsService.java
│   │   │           │
│   │   │           ├── exception/
│   │   │           │   ├── GlobalExceptionHandler.java
│   │   │           │   ├── ResourceNotFoundException.java
│   │   │           │   ├── UnauthorizedException.java
│   │   │           │   └── ValidationException.java
│   │   │           │
│   │   │           └── util/
│   │   │               ├── PasswordUtil.java
│   │   │               └── DateTimeUtil.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── data.sql                                 # 초기 페르소나 데이터
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── lobai/
│                   ├── service/
│                   ├── controller/
│                   └── repository/
│
├── pom.xml (or build.gradle)
├── .env.example
├── .gitignore
└── README.md
```

---

## API Endpoints Specification

### Authentication Endpoints

#### POST /api/auth/register
회원가입

**Request**:
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "username": "홍길동"
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "username": "홍길동",
      "stats": { "hunger": 80, "energy": 90, "happiness": 70 },
      "currentPersona": { "id": 1, "name": "친구", "displayName": "친구모드" }
    }
  }
}
```

#### POST /api/auth/login
로그인

**Request**:
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "user": { ... }
  }
}
```

#### POST /api/auth/refresh
Access Token 갱신

**Request**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "accessToken": "...",
    "refreshToken": "..."
  }
}
```

#### POST /api/auth/logout
로그아웃 (Refresh Token 폐기)

**Headers**: `Authorization: Bearer {accessToken}`

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "로그아웃되었습니다."
}
```

---

### Message Endpoints

#### GET /api/messages
대화 히스토리 조회

**Headers**: `Authorization: Bearer {accessToken}`

**Query Params**:
- `personaId` (optional): 특정 페르소나 메시지만 조회
- `limit` (default: 50): 최대 메시지 개수
- `offset` (default: 0): 페이지네이션

**Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "id": 123,
      "role": "bot",
      "content": "안녕하세요! 저는 당신의 AI 동반자 Lobi입니다.",
      "personaId": 1,
      "personaName": "친구",
      "createdAt": "2025-12-30T17:30:00Z"
    },
    {
      "id": 124,
      "role": "user",
      "content": "오늘 기분이 좋지 않아.",
      "personaId": 1,
      "personaName": "친구",
      "createdAt": "2025-12-30T17:31:00Z"
    }
  ],
  "pagination": {
    "total": 350,
    "limit": 50,
    "offset": 0,
    "hasMore": true
  }
}
```

#### POST /api/messages
메시지 전송 + AI 응답 받기

**Headers**: `Authorization: Bearer {accessToken}`

**Request**:
```json
{
  "content": "오늘 AI 공부를 시작했어!",
  "personaId": 3
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "data": {
    "userMessage": {
      "id": 126,
      "role": "user",
      "content": "오늘 AI 공부를 시작했어!",
      "personaId": 3,
      "personaName": "코치",
      "createdAt": "2025-12-30T17:35:00Z"
    },
    "botMessage": {
      "id": 127,
      "role": "bot",
      "content": "훌륭해요! 🎯 구체적으로 어떤 분야부터 시작했나요?",
      "personaId": 3,
      "personaName": "코치",
      "createdAt": "2025-12-30T17:35:02Z"
    },
    "statsUpdate": {
      "happiness": 73
    }
  }
}
```

---

### Stats Endpoints

#### GET /api/stats
현재 Stats 조회

**Headers**: `Authorization: Bearer {accessToken}`

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "hunger": 75,
    "energy": 82,
    "happiness": 68,
    "lastUpdated": "2025-12-30T17:35:02Z"
  }
}
```

#### PUT /api/stats
Stats 업데이트 (액션 수행)

**Headers**: `Authorization: Bearer {accessToken}`

**Request**:
```json
{
  "action": "feed"  // 'feed', 'play', 'sleep'
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "hunger": 90,
    "energy": 82,
    "happiness": 73,
    "changes": {
      "hunger": +15,
      "happiness": +5
    },
    "message": "냠냠! 에너지가 충전되고 있어요."
  }
}
```

#### GET /api/stats/history
Stats 변화 히스토리

**Headers**: `Authorization: Bearer {accessToken}`

**Query Params**:
- `days` (default: 7): 조회할 일수
- `actionType` (optional): 특정 액션만 필터링

**Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "timestamp": "2025-12-30T17:00:00Z",
      "hunger": 80,
      "energy": 90,
      "happiness": 70,
      "action": "feed",
      "deltas": { "hunger": 15, "happiness": 5 }
    }
  ]
}
```

---

### Persona Endpoints

#### GET /api/personas
페르소나 목록 조회

**Headers**: `Authorization: Bearer {accessToken}` (optional, 공개 가능)

**Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "친구",
      "nameEn": "friend",
      "displayName": "친구모드",
      "description": "캐주얼하고 편안한 대화 상대",
      "iconEmoji": "👥",
      "isActive": true
    },
    {
      "id": 2,
      "name": "상담사",
      "nameEn": "counselor",
      "displayName": "상담사모드",
      "description": "경청하고 공감하는 심리 상담사",
      "iconEmoji": "💬",
      "isActive": true
    }
  ]
}
```

#### PUT /api/personas/current
현재 페르소나 변경

**Headers**: `Authorization: Bearer {accessToken}`

**Request**:
```json
{
  "personaId": 2
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "currentPersona": {
      "id": 2,
      "name": "상담사",
      "displayName": "상담사모드",
      "description": "경청하고 공감하는 심리 상담사",
      "iconEmoji": "💬"
    },
    "message": "페르소나가 '상담사모드'로 변경되었습니다."
  }
}
```

#### GET /api/personas/stats
페르소나별 사용 통계

**Headers**: `Authorization: Bearer {accessToken}`

**Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "personaId": 1,
      "personaName": "친구",
      "totalMessages": 150,
      "totalSessions": 12,
      "lastUsedAt": "2025-12-30T17:35:00Z"
    }
  ]
}
```

---

## JWT Authentication Flow

```
┌─────────────┐                                      ┌─────────────┐
│   Client    │                                      │   Backend   │
│  (React)    │                                      │ (Spring)    │
└─────────────┘                                      └─────────────┘
       │                                                     │
       │  1. POST /api/auth/register or /login              │
       │  { email, password }                               │
       │ ──────────────────────────────────────────────────>│
       │                                                     │
       │                       2. Validate credentials      │
       │                          Hash password (BCrypt)    │
       │                          Create User in DB         │
       │                                                     │
       │  3. Generate JWT tokens                            │
       │     - Access Token (15min expiry)                  │
       │     - Refresh Token (7 days expiry)                │
       │<───────────────────────────────────────────────────│
       │  { accessToken, refreshToken, user }               │
       │                                                     │
       │  4. Store tokens in localStorage                   │
       │                                                     │
       │  5. Subsequent API calls                           │
       │  Authorization: Bearer {accessToken}               │
       │ ──────────────────────────────────────────────────>│
       │                                                     │
       │                       6. JwtAuthenticationFilter   │
       │                          validates token           │
       │                                                     │
       │  7. Response with data                             │
       │<───────────────────────────────────────────────────│
```

**Token Structure**:

**Access Token Payload**:
```json
{
  "sub": "1",               // user ID
  "email": "user@example.com",
  "type": "access",
  "iat": 1735574400,        // issued at
  "exp": 1735575300         // expires in 15 minutes
}
```

**Refresh Token Payload**:
```json
{
  "sub": "1",
  "type": "refresh",
  "iat": 1735574400,
  "exp": 1736179200         // expires in 7 days
}
```

---

## Frontend Changes (index.tsx)

### 1. Remove Direct Gemini API Call

**Before (Line 208)**:
```typescript
const ai = new GoogleGenAI({ apiKey: process.env.API_KEY || '' });
const response = await ai.models.generateContent({...});
```

**After**:
```typescript
const response = await fetch('http://localhost:8080/api/messages', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  },
  body: JSON.stringify({
    content: userText,
    personaId: currentPersonaId
  })
});

const data = await response.json();
const botText = data.data.botMessage.content;
setMessages(prev => [...prev, data.data.userMessage, data.data.botMessage]);
setStats(prev => ({ ...prev, ...data.data.statsUpdate }));
```

### 2. Add Authentication State

```typescript
const [isAuthenticated, setIsAuthenticated] = useState(false);
const [currentUser, setCurrentUser] = useState(null);

useEffect(() => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    fetchUserInfo(token);
    loadMessages();  // 이전 대화 복원
  }
}, []);
```

### 3. Add Login/Register UI

```typescript
const LoginModal = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleLogin = async () => {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    const data = await response.json();
    if (data.success) {
      localStorage.setItem('accessToken', data.data.accessToken);
      localStorage.setItem('refreshToken', data.data.refreshToken);
      setIsAuthenticated(true);
      setCurrentUser(data.data.user);
      setStats(data.data.user.stats);
    }
  };

  return (
    <div className="login-modal">
      <input value={email} onChange={e => setEmail(e.target.value)} placeholder="이메일" />
      <input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="비밀번호" />
      <button onClick={handleLogin}>로그인</button>
    </div>
  );
};
```

### 4. Sync Stats with Backend

```typescript
const handleAction = async (type: 'feed' | 'play' | 'sleep') => {
  const response = await fetch('http://localhost:8080/api/stats', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
    },
    body: JSON.stringify({ action: type })
  });

  const data = await response.json();
  if (data.success) {
    setStats(data.data);
    setMessages(prev => [...prev, { role: 'bot', text: data.data.message }]);
  }
};
```

### 5. Add Persona Selector

```typescript
const [personas, setPersonas] = useState([]);
const [currentPersonaId, setCurrentPersonaId] = useState(1);

// 페르소나 목록 로드
useEffect(() => {
  fetch('http://localhost:8080/api/personas')
    .then(res => res.json())
    .then(data => setPersonas(data.data));
}, []);

// 페르소나 변경
const changePersona = async (personaId) => {
  const response = await fetch('http://localhost:8080/api/personas/current', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
    },
    body: JSON.stringify({ personaId })
  });

  const data = await response.json();
  if (data.success) {
    setCurrentPersonaId(personaId);
    setMessages(prev => [...prev, { role: 'bot', text: data.data.message }]);
  }
};

// UI
<div className="persona-selector">
  {personas.map(p => (
    <button
      key={p.id}
      onClick={() => changePersona(p.id)}
      className={currentPersonaId === p.id ? 'active' : ''}
    >
      {p.iconEmoji} {p.displayName}
    </button>
  ))}
</div>
```

---

## Implementation Timeline (4 Weeks)

### Week 1: Backend Core Setup
**Day 1-2**: Spring Boot 프로젝트 초기화, MySQL 설정
**Day 3-4**: DB 스키마 생성, JPA 엔티티 작성
**Day 5-7**: JWT 인증 시스템 구현 (Security Config, AuthController)

### Week 2: Core Features
**Day 1-2**: Message API 구현 + Gemini 통합
**Day 3-4**: Stats API 구현
**Day 5-6**: Persona API 구현
**Day 7**: 통합 테스트 (Postman)

### Week 3: Frontend Integration
**Day 1-2**: 로그인/회원가입 UI 구현
**Day 3-4**: 메시지 API 연동 (Gemini 직접 호출 제거)
**Day 5-6**: Stats & Persona 연동
**Day 7**: 버그 수정 및 UX 개선

### Week 4: Deployment
**Day 1-2**: VPS 설정, MySQL 설치, Spring Boot 배포
**Day 3-4**: 프론트엔드 빌드 & 배포
**Day 5-7**: 프로덕션 테스트, 모니터링, 문서화

---

## Environment Configuration

### backend/.env
```bash
DB_PASSWORD=your_db_password
JWT_SECRET=your_very_secure_secret_key_at_least_256_bits_long
GEMINI_API_KEY=your_gemini_api_key
SPRING_PROFILES_ACTIVE=dev
```

### backend/application.yml
```yaml
spring:
  application:
    name: lobai-backend

  datasource:
    url: jdbc:mysql://localhost:3306/lobai_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
    username: lobai_user
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQL8Dialect

jwt:
  secret: ${JWT_SECRET}
  access-token-expiry: 900000      # 15분
  refresh-token-expiry: 604800000  # 7일

gemini:
  api-key: ${GEMINI_API_KEY}
  model: gemini-3-flash-preview
  temperature: 0.8

cors:
  allowed-origins: http://localhost:3000,http://localhost:5173
```

---

## Security Checklist

- ✅ API Key를 백엔드에서만 사용
- ✅ 비밀번호 BCrypt 해싱 (strength 12)
- ✅ JWT 토큰 서명 검증
- ✅ CORS 화이트리스트 설정
- ✅ 입력 검증 (`@Valid` 어노테이션)
- ✅ SQL Injection 방지 (JPA 자동)
- ✅ 프로덕션 환경 HTTPS 강제
- ⚠️ Rate Limiting (Phase 2)

---

## Cost Estimation (MVP)

- **VPS** (Hetzner CX11): $4/month (2GB RAM, 20GB SSD)
- **Domain**: $1/month
- **Gemini API**: Free tier (60 req/min) → 초과 시 ~$75/month (1000 users)
- **Total**: $5-10/month (100명 이하)

---

## Success Criteria

Phase 1 완료 조건:

- ✅ 회원가입/로그인 동작
- ✅ 대화 저장 및 페이지 새로고침 후 복원
- ✅ Stats가 DB에 영구 저장되고 동기화
- ✅ 5개 페르소나 전환 가능
- ✅ API Key가 클라이언트에 노출되지 않음
- ✅ 프로덕션 환경 배포 완료

---

## Phase 2 Features (Future)

사용자 요청 10가지 기능 중 Phase 2로 연기된 항목:

1. **대화 요약 기능** - `/summary` 명령어
2. **리포트 생성** - AI 친화도/적응력 분석
3. **대화 패턴 분석** - NLP 기반 sentiment/clarity score
4. **일일 미션/이벤트** - 미션 테이블, 완료 보상
5. **슬래시 명령어** - `/persona`, `/report`, `/draft` 파싱
6. **사용자 말투 학습** - 메시지 히스토리 tone 분석
7. **메일 초안 작성** - 사용자 말투로 draft 생성
8. **인적사항 수집 대화** - 첫 채팅 시 onboarding flow

---

## Related Documentation

- **Product Requirements**: `LobAI_PRD_v3.md`
- **Technical Guide**: `docs/TECHNICAL_GUIDE.md`
- **Folder Structure**: `docs/plans/FOLDER_STRUCTURE_SPEC.md`

---

**Last Updated**: 2025-12-30
**Estimated Total Effort**: 80-100 hours (4주 full-time)
