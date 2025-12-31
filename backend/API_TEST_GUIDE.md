# LobAI Backend API 테스트 가이드

**서버 주소**: http://localhost:8080/api
**서버 상태**: ✅ 실행 중 (PID: 확인 필요)

---

## 🚀 Quick Start

### 1. 서버 Health Check

```bash
curl http://localhost:8080/api/health
```

**Expected Response**:
```json
{
  "service": "lobai-backend",
  "version": "0.0.1-SNAPSHOT",
  "status": "UP",
  "timestamp": "2025-12-31T02:02:43.041669"
}
```

---

## 📝 Step-by-Step Testing Guide

### Step 1: 회원가입 (Register)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "username": "테스트유저"
  }'
```

**Expected Response**:
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJ0ZXN0QGV4YW1wbGUuY29tIiwidHlwZSI6ImFjY2VzcyIsImlhdCI6MTczNTU3NDU2MywiZXhwIjoxNzM1NTc1NDYzfQ.xxx",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE3MzU1NzQ1NjMsImV4cCI6MTczNjE3OTM2M30.xxx",
    "expiresIn": 900000,
    "tokenType": "Bearer",
    "userId": 1,
    "email": "test@example.com",
    "username": "테스트유저"
  }
}
```

**💡 중요**: 응답에서 `accessToken` 값을 복사해서 환경 변수로 저장하세요!

```bash
export TOKEN="여기에_accessToken_붙여넣기"
```

예시:
```bash
export TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJ0ZXN0QGV4YW1wbGUuY29tIiwidHlwZSI6ImFjY2VzcyIsImlhdCI6MTczNTU3NDU2MywiZXhwIjoxNzM1NTc1NDYzfQ.xxx"
```

---

### Step 2: 로그인 (Login)

이미 회원가입한 계정으로 다시 로그인:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Expected Response**: 회원가입과 동일한 형식 (새로운 accessToken 발급)

---

### Step 3: 페르소나 목록 조회 (Public API - 토큰 불필요)

```bash
curl http://localhost:8080/api/personas
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "name": "친구",
      "nameEn": "friend",
      "displayName": "친구모드",
      "iconEmoji": "👥",
      "displayOrder": 1,
      "isActive": true
    },
    {
      "id": 2,
      "name": "상담사",
      "nameEn": "counselor",
      "displayName": "상담사모드",
      "iconEmoji": "💬",
      "displayOrder": 2,
      "isActive": true
    },
    {
      "id": 3,
      "name": "코치",
      "nameEn": "coach",
      "displayName": "코치모드",
      "iconEmoji": "🎯",
      "displayOrder": 3,
      "isActive": true
    },
    {
      "id": 4,
      "name": "전문가",
      "nameEn": "expert",
      "displayName": "전문가모드",
      "iconEmoji": "🎓",
      "displayOrder": 4,
      "isActive": true
    },
    {
      "id": 5,
      "name": "유머",
      "nameEn": "humor",
      "displayName": "유머모드",
      "iconEmoji": "😄",
      "displayOrder": 5,
      "isActive": true
    }
  ]
}
```

---

### Step 4: 현재 Stats 조회

**⚠️ JWT 토큰 필수** - Step 1에서 저장한 `$TOKEN` 사용

```bash
curl http://localhost:8080/api/stats \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "hunger": 80,
    "energy": 90,
    "happiness": 70
  }
}
```

**초기값**: hunger=80, energy=90, happiness=70

---

### Step 5: 메시지 전송 (AI 채팅)

**⚠️ JWT 토큰 필수**

#### 5-1. 친구모드로 대화 (Persona ID: 1)

```bash
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "content": "안녕! 오늘 날씨가 정말 좋네",
    "personaId": 1
  }'
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "userMessage": {
      "id": 1,
      "personaId": 1,
      "personaName": "친구",
      "personaEmoji": "👥",
      "role": "user",
      "content": "안녕! 오늘 날씨가 정말 좋네",
      "createdAt": "2025-12-31T02:05:23.123456"
    },
    "botMessage": {
      "id": 2,
      "personaId": 1,
      "personaName": "친구",
      "personaEmoji": "👥",
      "role": "bot",
      "content": "그러게! 날씨 좋은 날엔 기분이 절로 좋아지지 😊 밖에 나가고 싶어지네~",
      "createdAt": "2025-12-31T02:05:24.456789"
    },
    "statsUpdate": {
      "hunger": 80,
      "energy": 90,
      "happiness": 72
    }
  }
}
```

**💡 주목**:
- `happiness`가 70 → 72로 증가 (대화 1회당 +2)
- Gemini AI가 친구 페르소나로 실제 응답 생성

#### 5-2. 코치모드로 대화 (Persona ID: 3)

```bash
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "content": "오늘 운동을 시작하고 싶은데 어떻게 해야 할까요?",
    "personaId": 3
  }'
```

**Expected Response**: 코치 페르소나가 구체적이고 실행 가능한 조언 제공

#### 5-3. 페르소나 지정 안 함 (현재 페르소나 사용)

```bash
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "content": "좋은 아침이야!"
  }'
```

**동작**: 사용자의 현재 페르소나 사용 (없으면 자동으로 "친구"모드 선택)

---

### Step 6: Stats 업데이트

**⚠️ JWT 토큰 필수**

#### 6-1. 먹이기 (Feed) - Hunger +20

```bash
curl -X PUT http://localhost:8080/api/stats \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{ "action": "feed" }'
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "hunger": 100,
    "energy": 90,
    "happiness": 72
  }
}
```

#### 6-2. 놀기 (Play) - Happiness +15, Energy -10

```bash
curl -X PUT http://localhost:8080/api/stats \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{ "action": "play" }'
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "hunger": 100,
    "energy": 80,
    "happiness": 87
  }
}
```

#### 6-3. 재우기 (Sleep) - Energy +30, Hunger -5

```bash
curl -X PUT http://localhost:8080/api/stats \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{ "action": "sleep" }'
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "hunger": 95,
    "energy": 100,
    "happiness": 87
  }
}
```

**💡 Stats 범위**: 자동으로 0-100 범위로 제한됨 (100 초과 시 100, 0 미만 시 0)

---

### Step 7: 페르소나 변경

**⚠️ JWT 토큰 필수**

#### 7-1. 현재 페르소나 조회

```bash
curl http://localhost:8080/api/personas/current \
  -H "Authorization: Bearer $TOKEN"
```

#### 7-2. 페르소나 변경 (예: 유머모드로 변경)

```bash
curl -X PUT http://localhost:8080/api/personas/current \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{ "personaId": 5 }'
```

**Expected Response**:
```json
{
  "success": true,
  "message": "페르소나가 변경되었습니다",
  "data": {
    "id": 5,
    "name": "유머",
    "nameEn": "humor",
    "displayName": "유머모드",
    "iconEmoji": "😄",
    "displayOrder": 5,
    "isActive": true
  }
}
```

이제 다음 채팅부터 유머모드로 응답합니다!

---

### Step 8: 대화 히스토리 조회

**⚠️ JWT 토큰 필수**

#### 8-1. 최근 대화 50개 조회 (기본값)

```bash
curl "http://localhost:8080/api/messages?limit=50" \
  -H "Authorization: Bearer $TOKEN"
```

#### 8-2. 최근 대화 10개만 조회

```bash
curl "http://localhost:8080/api/messages?limit=10" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "personaId": 1,
      "personaName": "친구",
      "personaEmoji": "👥",
      "role": "user",
      "content": "안녕! 오늘 날씨가 정말 좋네",
      "createdAt": "2025-12-31T02:05:23.123456"
    },
    {
      "id": 2,
      "personaId": 1,
      "personaName": "친구",
      "personaEmoji": "👥",
      "role": "bot",
      "content": "그러게! 날씨 좋은 날엔 기분이 절로 좋아지지 😊",
      "createdAt": "2025-12-31T02:05:24.456789"
    }
  ]
}
```

#### 8-3. 특정 페르소나와의 대화만 조회

```bash
curl http://localhost:8080/api/messages/persona/3 \
  -H "Authorization: Bearer $TOKEN"
```

코치모드(ID: 3)와의 대화만 조회

---

### Step 9: Stats 히스토리 조회

**⚠️ JWT 토큰 필수**

```bash
curl "http://localhost:8080/api/stats/history?limit=20" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "hunger": 80,
      "energy": 90,
      "happiness": 72,
      "actionType": "chat",
      "timestamp": "2025-12-31T02:05:24"
    },
    {
      "id": 2,
      "hunger": 100,
      "energy": 90,
      "happiness": 72,
      "actionType": "feed",
      "timestamp": "2025-12-31T02:06:15"
    }
  ]
}
```

---

### Step 10: Access Token 갱신 (Refresh)

**⚠️ Refresh Token 필수** - Step 1 회원가입 시 받은 `refreshToken` 사용

Access Token이 만료되었을 때 (15분 후):

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "여기에_refreshToken_붙여넣기"
  }'
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "accessToken": "새로운_accessToken",
    "expiresIn": 900000,
    "tokenType": "Bearer"
  }
}
```

**💡 사용법**: 새로운 accessToken을 다시 환경 변수로 저장
```bash
export TOKEN="새로운_accessToken"
```

---

### Step 11: 로그아웃

**⚠️ JWT 토큰 필수**

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response**:
```json
{
  "success": true,
  "message": "로그아웃되었습니다"
}
```

**동작**: Refresh Token이 revoke되어 더 이상 사용할 수 없음

---

## 🎯 실전 시나리오 테스트

### 시나리오 1: 완전한 사용자 여정

```bash
# 1. 회원가입
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user1@test.com","password":"pass1234","username":"유저1"}'

# 2. 토큰 저장 (응답에서 accessToken 복사)
export TOKEN="..."

# 3. 페르소나 목록 확인
curl http://localhost:8080/api/personas

# 4. 친구모드로 첫 대화
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"content":"안녕 로비! 처음 만나서 반가워","personaId":1}'

# 5. 먹이기
curl -X PUT http://localhost:8080/api/stats \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"action":"feed"}'

# 6. 코치모드로 변경
curl -X PUT http://localhost:8080/api/personas/current \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"personaId":3}'

# 7. 코치모드로 대화
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"content":"오늘 할 일을 정리하고 싶어요"}'

# 8. 대화 히스토리 확인
curl "http://localhost:8080/api/messages?limit=10" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🧪 에러 케이스 테스트

### 에러 1: 잘못된 이메일 형식

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"invalid-email","password":"pass1234","username":"유저"}'
```

**Expected Response**:
```json
{
  "success": false,
  "message": "입력값 검증에 실패했습니다",
  "errorCode": "VALIDATION_ERROR"
}
```

### 에러 2: JWT 토큰 없이 보호된 엔드포인트 호출

```bash
curl http://localhost:8080/api/stats
```

**Expected Response**: 401 Unauthorized 또는 403 Forbidden

### 에러 3: 잘못된 비밀번호로 로그인

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"wrongpassword"}'
```

**Expected Response**: 인증 실패 메시지

### 에러 4: 존재하지 않는 페르소나 선택

```bash
curl -X PUT http://localhost:8080/api/personas/current \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"personaId":999}'
```

**Expected Response**:
```json
{
  "success": false,
  "message": "페르소나를 찾을 수 없습니다: 999"
}
```

---

## 📊 데이터베이스 직접 확인 (Optional)

MySQL에 직접 접속해서 데이터 확인:

```bash
mysql -u lobai_user -p lobai_db
# 비밀번호: lobai_dev_password
```

```sql
-- 사용자 목록
SELECT * FROM users;

-- 페르소나 목록
SELECT * FROM personas;

-- 메시지 목록
SELECT m.id, u.username, p.name as persona, m.role, m.content, m.created_at
FROM messages m
JOIN users u ON m.user_id = u.id
JOIN personas p ON m.persona_id = p.id
ORDER BY m.created_at DESC
LIMIT 10;

-- Stats 히스토리
SELECT * FROM user_stats_history
ORDER BY created_at DESC
LIMIT 20;

-- Refresh Tokens
SELECT * FROM refresh_tokens
WHERE is_revoked = FALSE;
```

---

## 🔧 서버 관리 명령어

### 서버 상태 확인

```bash
# Health check
curl http://localhost:8080/api/health

# 프로세스 확인
ps aux | grep java | grep lobai
```

### 서버 중지

```bash
# 포트 8080 사용 프로세스 확인
lsof -ti:8080

# 프로세스 종료
lsof -ti:8080 | xargs kill -9
```

### 서버 재시작

```bash
cd /Users/jimin/lobai/lobai/backend
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew bootRun
```

### 로그 확인 (백그라운드 실행 시)

```bash
# Gradle 빌드 로그
tail -f build/libs/*.log

# Spring Boot 로그 (콘솔 출력)
# (백그라운드 실행 중이므로 Task Output에서 확인)
```

---

## 🎓 학습 포인트

1. **JWT 인증 Flow**
   - Register/Login → Access Token 발급
   - 보호된 엔드포인트 호출 시 `Authorization: Bearer {token}` 헤더 필수
   - 15분 후 만료 시 Refresh Token으로 갱신

2. **RESTful API 설계**
   - GET: 조회
   - POST: 생성
   - PUT: 업데이트
   - DELETE: 삭제 (아직 미구현)

3. **페르소나 시스템**
   - 같은 메시지라도 페르소나에 따라 AI 응답이 다름
   - 페르소나별 system instruction이 Gemini에 전달됨

4. **Stats 시스템**
   - 채팅, 먹이기, 놀기, 재우기에 따라 stats 변화
   - 모든 변화가 user_stats_history에 기록됨

5. **에러 처리**
   - @Valid 어노테이션으로 입력 검증
   - GlobalExceptionHandler로 일관된 에러 응답
   - HTTP 상태 코드 활용 (200, 201, 400, 401, 404, 500)

---

## 📝 다음 단계

1. **프론트엔드 연동**
   - React `index.tsx`에서 Gemini 직접 호출 제거
   - 백엔드 API로 교체
   - 로그인/회원가입 UI 추가

2. **테스트 자동화**
   - JUnit 5 + MockMvc
   - Postman Collection 생성

3. **배포**
   - VPS 설정
   - MySQL 프로덕션 설정
   - HTTPS 적용

---

**문서 작성일**: 2025-12-31
**서버 버전**: 0.0.1-SNAPSHOT
**마지막 업데이트**: 백엔드 Phase 1 완료
