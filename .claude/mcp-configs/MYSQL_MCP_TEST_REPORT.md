# MySQL MCP 테스트 리포트

**Date**: 2026-01-05
**Status**: ✅ 모든 테스트 통과
**MCP Server**: `@f4ww4z/mcp-mysql-server`

---

## 테스트 요약

MySQL MCP가 정상적으로 연결되어 `lobai_db` 데이터베이스에 접근할 수 있습니다.

| Test Case | Status | Details |
|-----------|--------|---------|
| **연결 테스트** | ✅ Pass | MCP 서버 정상 연결 |
| **테이블 조회** | ✅ Pass | 11개 테이블 확인 |
| **데이터 조회** | ✅ Pass | 3명 사용자, 2개 출석 기록 |
| **스키마 조회** | ✅ Pass | users 테이블 스키마 확인 |
| **DB 통계** | ✅ Pass | 테이블 크기 정보 조회 |

---

## 데이터베이스 현황

### 테이블 목록 (11개)

```
1. users                          # 사용자 정보
2. messages                       # 채팅 메시지
3. attendance_records             # 출석 기록
4. affinity_scores                # 친밀도 점수
5. affinity_analysis_details      # 친밀도 분석 상세
6. resilience_reports             # AI 적응력 리포트
7. user_stats_history             # 사용자 통계 히스토리
8. personas                       # AI 페르소나
9. user_subscriptions             # 구독 정보
10. purchase_history              # 구매 내역
11. refresh_tokens                # JWT 리프레시 토큰
```

### 현재 데이터

- **총 사용자**: 3명
  - `affinitytest@test.com` (USER)
  - `phase2@test.com` (USER)
  - `admin@lobai.com` (ADMIN)

- **총 메시지**: 0개
- **출석 기록**: 2개
- **DB 크기**: 약 0.81 MB

### users 테이블 스키마

```sql
CREATE TABLE users (
  id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
  email                  VARCHAR(255) UNIQUE NOT NULL,
  password_hash          VARCHAR(255) NOT NULL,
  username               VARCHAR(100) NOT NULL,
  profile_image_url      VARCHAR(500),
  
  -- 게임 스탯
  current_hunger         INT DEFAULT 80,
  current_energy         INT DEFAULT 90,
  current_happiness      INT DEFAULT 70,
  current_persona_id     BIGINT,
  
  -- OAuth
  oauth_provider         VARCHAR(50),
  oauth_id               VARCHAR(255),
  
  -- 타임스탬프
  created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  last_login_at          TIMESTAMP,
  
  -- 상태 및 권한
  is_active              TINYINT(1) DEFAULT 1,
  role                   VARCHAR(20) DEFAULT 'USER' NOT NULL,
  subscription_tier      ENUM('free','basic','premium') DEFAULT 'free' NOT NULL,
  
  -- 출석 정보
  total_attendance_days  INT DEFAULT 0,
  max_streak_days        INT DEFAULT 0,
  last_attendance_date   DATE,
  
  -- Indexes
  INDEX idx_email (email),
  INDEX idx_role (role),
  INDEX idx_oauth (oauth_provider),
  INDEX idx_created_at (created_at)
);
```

---

## MySQL MCP 사용 예시

### 기본 쿼리

MySQL MCP가 활성화되면, Claude Code에서 자연어로 데이터베이스를 조회할 수 있습니다:

#### 1. 테이블 조회
```
> "Show me all tables in lobai_db"
```

**예상 응답**:
```
lobai_db 데이터베이스에는 다음 11개의 테이블이 있습니다:
1. users - 사용자 정보
2. messages - 채팅 메시지
3. attendance_records - 출석 기록
...
```

#### 2. 사용자 수 조회
```
> "How many users are registered?"
```

**예상 응답**:
```
현재 3명의 사용자가 등록되어 있습니다:
- 2명의 일반 사용자 (USER)
- 1명의 관리자 (ADMIN)
```

#### 3. 스키마 확인
```
> "Describe the users table schema"
```

**예상 응답**:
```
users 테이블은 다음과 같은 구조를 가지고 있습니다:
- id (BIGINT, PRIMARY KEY)
- email (VARCHAR(255), UNIQUE)
- username (VARCHAR(100))
- role (VARCHAR(20), DEFAULT 'USER')
- current_hunger (INT, DEFAULT 80)
- current_energy (INT, DEFAULT 90)
- current_happiness (INT, DEFAULT 70)
...
```

#### 4. 최근 사용자 조회
```
> "Show me the 5 most recent users"
```

**예상 응답**:
```
최근 가입한 사용자 5명:
1. admin@lobai.com (LobAI Admin) - ADMIN - 2026-01-04
2. phase2@test.com (phase2tester) - USER - 2026-01-02
3. affinitytest@test.com (affinityuser) - USER - 2026-01-02
```

#### 5. 출석 기록 조회
```
> "How many attendance records do we have?"
```

**예상 응답**:
```
현재 2개의 출석 기록이 있습니다.
```

---

## 고급 사용 예시

### 데이터 분석

#### 1. 사용자 통계
```
> "What's the average happiness level of all users?"
```

#### 2. 출석 분석
```
> "Show me users with the longest attendance streak"
```

#### 3. 메시지 분석
```
> "Count messages sent in the last 7 days"
```

### 데이터 검증

#### 1. 중복 확인
```
> "Check if there are any duplicate emails in the users table"
```

#### 2. NULL 값 확인
```
> "Find users with NULL profile_image_url"
```

#### 3. 인덱스 확인
```
> "Show all indexes on the users table"
```

### 데이터베이스 최적화

#### 1. 테이블 크기
```
> "What are the largest tables in lobai_db?"
```

**예상 응답**:
```
테이블 크기 (큰 순서):
1. users - 0.11 MB
2. affinity_scores - 0.08 MB
3. personas - 0.08 MB
...
```

#### 2. 쿼리 성능
```
> "Explain the query plan for selecting users by email"
```

---

## 개발 워크플로우 통합

### TDD 사이클에서 MySQL MCP 활용

#### Phase 1: 테스트 작성 (RED)
```
> "Show me the attendance_records table schema"
→ 스키마 확인 후 테스트 케이스 작성
```

#### Phase 2: 구현 (GREEN)
```java
@Test
void checkIn_Success() {
    // Arrange
    Long userId = 1L;
    
    // Act
    attendanceService.checkIn(userId);
    
    // Assert
    // ...
}
```

#### Phase 3: 검증 (REFACTOR)
```
> "Count attendance records for user ID 1"
→ 실제 데이터 확인
```

### 마이그레이션 검증

#### 1. 마이그레이션 전
```
> "Show the current schema of the users table"
```

#### 2. 마이그레이션 실행
```bash
./gradlew flywayMigrate
```

#### 3. 마이그레이션 후
```
> "Did any columns get added to the users table?"
> "Check if the new index exists"
```

---

## 디버깅 예시

### 1. 로그인 실패 디버깅
```
User: "로그인이 안 돼요"
Developer: "Check if test@test.com exists in the users table"
MySQL MCP: "Yes, user exists with role USER and is_active=1"
Developer: "What's the password_hash for test@test.com?"
MySQL MCP: "$2a$10$..."
Developer: "→ 비밀번호 해싱 문제 확인"
```

### 2. 출석 체크 안 되는 문제
```
Developer: "Show attendance records for user ID 2"
MySQL MCP: "User 2 has 0 attendance records"
Developer: "What's the last_attendance_date for user 2?"
MySQL MCP: "NULL"
Developer: "→ 출석 로직 버그 확인"
```

### 3. 메시지가 안 보이는 문제
```
Developer: "Count messages for user ID 1"
MySQL MCP: "0 messages"
Developer: "Check if messages table has any data at all"
MySQL MCP: "messages table is empty"
Developer: "→ 메시지 저장 로직 확인"
```

---

## 보안 고려사항

### ✅ 현재 설정

```json
{
  "env": {
    "MYSQL_HOST": "localhost",
    "MYSQL_PORT": "3306",
    "MYSQL_USER": "lobai_user",
    "MYSQL_PASSWORD": "lobai_dev_password",
    "MYSQL_DATABASE": "lobai_db"
  }
}
```

### ⚠️ 프로덕션 환경

프로덕션 환경에서는:

1. **비밀번호를 환경 변수로 관리**:
```json
{
  "env": {
    "MYSQL_PASSWORD": "${MYSQL_PASSWORD}"
  }
}
```

2. **.mcp.json을 .gitignore에 추가**:
```gitignore
.mcp.json
```

3. **읽기 전용 사용자 생성**:
```sql
CREATE USER 'lobai_readonly'@'localhost' IDENTIFIED BY 'secure_password';
GRANT SELECT ON lobai_db.* TO 'lobai_readonly'@'localhost';
```

---

## 문제 해결

### MCP 연결 실패

**증상**: `claude mcp list`에서 "✗ Failed to connect"

**해결**:

1. MySQL 서비스 확인:
```bash
brew services list | grep mysql
brew services start mysql@8.0
```

2. 사용자 권한 확인:
```bash
mysql -u lobai_user -p'lobai_dev_password' -e "SELECT 1;"
```

3. MCP 서버 재시작:
```bash
claude mcp remove mysql
claude mcp add --transport stdio mysql --scope project \
  --env MYSQL_HOST=localhost \
  --env MYSQL_PORT=3306 \
  --env MYSQL_USER=lobai_user \
  --env MYSQL_PASSWORD=lobai_dev_password \
  --env MYSQL_DATABASE=lobai_db \
  -- npx -y @f4ww4z/mcp-mysql-server
```

---

## 다음 단계

### 1. 실제 사용 테스트
Claude Code에서 다음 쿼리 실행:
```
> "Show me all tables in lobai_db"
> "Count how many users are registered"
> "Show the schema of the users table"
```

### 2. 워크플로우 통합
- TDD 사이클에 MySQL MCP 통합
- 마이그레이션 검증 자동화
- 데이터 분석 자동화

### 3. 팀 공유
- `.mcp.json` 설정 팀원들과 공유
- 사용 가이드 문서 배포
- 베스트 프랙티스 정립

---

## 관련 문서

- [MCP Installation Report](./INSTALLATION_REPORT.md)
- [MySQL MCP Setup Guide](./mysql-setup.md)
- [Automated Workflow](../workflows/AUTOMATED_WORKFLOW.md)

---

## Changelog

| Date       | Changes                |
|------------|------------------------|
| 2026-01-05 | Initial test report    |

---

**© 2026 LobAI. All rights reserved.**
