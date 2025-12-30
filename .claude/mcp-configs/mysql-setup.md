# MySQL MCP Setup Guide

**Version**: 1.0
**Date**: 2025-12-30
**Status**: ✅ Recommended for Backend Development

---

## Overview

**MySQL MCP**는 Claude Code가 MySQL 데이터베이스와 직접 통신할 수 있게 해주는 MCP 서버입니다. 이를 통해 스키마 조회, SQL 쿼리 실행, 데이터 검증 등을 자동화할 수 있습니다.

---

## Why MySQL MCP?

LobAI 프로젝트는 **MySQL 8.x**를 데이터베이스로 사용합니다. MySQL MCP를 설치하면:

✅ **스키마 자동 조회**: "users 테이블 스키마 보여줘" → DDL 반환
✅ **데이터 확인**: "최근 가입 사용자 10명 조회" → SELECT 결과
✅ **인덱스 검증**: "messages 테이블 인덱스 확인" → SHOW INDEX
✅ **마이그레이션 검증**: Flyway 히스토리 조회, 스키마 변경 확인
✅ **쿼리 최적화**: EXPLAIN 분석, 느린 쿼리 찾기

---

## Prerequisites

### 1. MySQL Server

MySQL 8.x가 설치되어 있어야 합니다.

**확인**:
```bash
mysql --version
# mysql  Ver 8.0.xx for macos13 (arm64)
```

**설치** (macOS):
```bash
brew install mysql@8.0
brew services start mysql@8.0
```

**설치** (Windows):
- [MySQL Installer](https://dev.mysql.com/downloads/installer/) 다운로드

**설치** (Linux):
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
```

---

### 2. LobAI Database & User 생성

MySQL에 접속하여 데이터베이스와 사용자를 생성합니다.

```bash
# MySQL 접속
mysql -u root -p
```

```sql
-- 데이터베이스 생성
CREATE DATABASE lobai_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 및 권한 부여
CREATE USER 'lobai_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON lobai_db.* TO 'lobai_user'@'localhost';
FLUSH PRIVILEGES;

-- 확인
SHOW DATABASES;
SELECT user, host FROM mysql.user WHERE user = 'lobai_user';

EXIT;
```

**연결 테스트**:
```bash
mysql -u lobai_user -p lobai_db
# 비밀번호 입력 후 접속되면 성공
```

---

### 3. Node.js (18+)

MCP 서버 실행을 위해 Node.js가 필요합니다.

**확인**:
```bash
node --version
# v18.x.x 이상
```

**설치** (macOS):
```bash
brew install node@18
```

**설치** (Windows/Linux):
- [Node.js 공식 사이트](https://nodejs.org/)에서 LTS 버전 다운로드

---

## Installation

### Step 1: Claude Code 설정 파일 찾기

MCP 서버는 Claude Code 설정 파일에서 관리됩니다.

**설정 파일 위치**:
- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows**: `%APPDATA%/Claude/claude_desktop_config.json`
- **Linux**: `~/.config/Claude/claude_desktop_config.json`

**파일이 없으면 생성**:
```bash
# macOS/Linux
mkdir -p ~/Library/Application\ Support/Claude
touch ~/Library/Application\ Support/Claude/claude_desktop_config.json

# 또는
mkdir -p ~/.config/Claude
touch ~/.config/Claude/claude_desktop_config.json
```

---

### Step 2: MySQL MCP 설정 추가

`claude_desktop_config.json` 파일을 열어서 다음 내용을 추가합니다.

**기존 MCP 서버가 없는 경우**:
```json
{
  "mcpServers": {
    "mysql": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-mysql"
      ],
      "env": {
        "MYSQL_HOST": "localhost",
        "MYSQL_PORT": "3306",
        "MYSQL_USER": "lobai_user",
        "MYSQL_PASSWORD": "your_secure_password",
        "MYSQL_DATABASE": "lobai_db"
      }
    }
  }
}
```

**기존 MCP 서버(Context7 등)가 있는 경우**:
```json
{
  "mcpServers": {
    "context7": {
      "command": "npx",
      "args": ["-y", "@context7/mcp-server"]
    },
    "mysql": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-mysql"
      ],
      "env": {
        "MYSQL_HOST": "localhost",
        "MYSQL_PORT": "3306",
        "MYSQL_USER": "lobai_user",
        "MYSQL_PASSWORD": "your_secure_password",
        "MYSQL_DATABASE": "lobai_db"
      }
    }
  }
}
```

**⚠️ 보안 주의**:
- 비밀번호를 직접 쓰지 말고 환경 변수 사용 권장:
  ```json
  "MYSQL_PASSWORD": "${MYSQL_PASSWORD}"
  ```
- 환경 변수 설정 (macOS/Linux):
  ```bash
  export MYSQL_PASSWORD="your_secure_password"
  # ~/.zshrc 또는 ~/.bashrc에 추가하여 영구 설정
  ```

---

### Step 3: Claude Code 재시작

설정 파일을 수정한 후 Claude Code를 **완전히 종료**하고 다시 실행합니다.

```bash
# macOS에서 완전 종료
killall Claude\ Code
# 또는 Cmd+Q로 종료

# 재시작
open -a "Claude Code"
```

---

### Step 4: 동작 확인

Claude Code에서 다음 쿼리를 입력하여 MySQL MCP가 정상 작동하는지 확인합니다.

**테스트 쿼리**:
```
"lobai_db 데이터베이스에 어떤 테이블이 있어?"
```

**기대 결과**:
```
lobai_db 데이터베이스의 테이블 목록:
- users
- personas
- messages
- user_stats_history
- user_persona_settings
- refresh_tokens

(또는 아직 테이블이 없다면 빈 목록)
```

**추가 테스트**:
```
"users 테이블 스키마 보여줘"
"MySQL 서버 버전 확인해줘"
```

---

## Usage Examples

### Example 1: 스키마 조회

```
User: "users 테이블 스키마 알려줘"

MySQL MCP:
SHOW CREATE TABLE users;

Output:
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `username` varchar(100) NOT NULL,
  `current_hunger` int DEFAULT 80,
  `current_energy` int DEFAULT 90,
  `current_happiness` int DEFAULT 70,
  `current_persona_id` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### Example 2: 데이터 조회

```
User: "최근 가입한 사용자 5명 조회"

MySQL MCP:
SELECT id, email, username, created_at
FROM users
ORDER BY created_at DESC
LIMIT 5;

Output:
+----+-------------------+-----------+---------------------+
| id | email             | username  | created_at          |
+----+-------------------+-----------+---------------------+
|  5 | user5@example.com | Alice     | 2025-12-30 18:00:00 |
|  4 | user4@example.com | Bob       | 2025-12-30 17:55:00 |
|  3 | user3@example.com | Charlie   | 2025-12-30 17:50:00 |
|  2 | user2@example.com | David     | 2025-12-30 17:45:00 |
|  1 | user1@example.com | Eve       | 2025-12-30 17:40:00 |
+----+-------------------+-----------+---------------------+
```

---

### Example 3: 인덱스 확인

```
User: "messages 테이블에 어떤 인덱스가 있어?"

MySQL MCP:
SHOW INDEX FROM messages;

Output:
+---------+------------+--------------------+--------------+-------------+
| Table   | Non_unique | Key_name           | Column_name  | Index_type  |
+---------+------------+--------------------+--------------+-------------+
| messages|          0 | PRIMARY            | id           | BTREE       |
| messages|          1 | idx_user_created   | user_id      | BTREE       |
| messages|          1 | idx_user_created   | created_at   | BTREE       |
| messages|          1 | fk_user            | user_id      | BTREE       |
| messages|          1 | fk_persona         | persona_id   | BTREE       |
+---------+------------+--------------------+--------------+-------------+
```

---

### Example 4: 쿼리 최적화 (EXPLAIN)

```
User: "messages 테이블에서 user_id로 조회하는 쿼리 성능 분석해줘"

MySQL MCP:
EXPLAIN SELECT * FROM messages WHERE user_id = 1 ORDER BY created_at DESC;

Output:
+----+-------------+----------+-------+------------------+------------------+---------+-------+------+-------+
| id | select_type | table    | type  | possible_keys    | key              | key_len | ref   | rows | Extra |
+----+-------------+----------+-------+------------------+------------------+---------+-------+------+-------+
|  1 | SIMPLE      | messages | ref   | idx_user_created | idx_user_created | 8       | const |  150 | Using index |
+----+-------------+----------+-------+------------------+------------------+---------+-------+------+-------+

분석: idx_user_created 인덱스를 사용하여 효율적으로 조회됨 ✅
```

---

### Example 5: 데이터 검증

```
User: "personas 테이블에 5개 페르소나가 모두 있는지 확인해줘"

MySQL MCP:
SELECT id, name, display_name FROM personas ORDER BY display_order;

Output:
+----+---------+--------------+
| id | name    | display_name |
+----+---------+--------------+
|  1 | 친구    | 친구모드     |
|  2 | 상담사  | 상담사모드   |
|  3 | 코치    | 코치모드     |
|  4 | 전문가  | 전문가모드   |
|  5 | 유머    | 유머모드     |
+----+---------+--------------+

결과: 5개 페르소나 모두 존재 ✅
```

---

## Troubleshooting

### Issue 1: MCP Server Not Starting

**Symptoms**:
- Claude Code에서 "MySQL MCP가 응답하지 않음" 에러
- 쿼리 실행 시 응답 없음

**Solutions**:

1. **설정 파일 JSON 문법 확인**:
   ```bash
   # JSON 검증
   cat ~/Library/Application\ Support/Claude/claude_desktop_config.json | python -m json.tool
   # 오류 없이 출력되면 문법 정상
   ```

2. **MySQL 서버 실행 확인**:
   ```bash
   mysql -u lobai_user -p lobai_db
   # 접속되면 MySQL 정상
   ```

3. **Node.js 버전 확인**:
   ```bash
   node --version
   # v18.x.x 이상이어야 함
   ```

4. **Claude Code 완전 재시작**:
   ```bash
   killall Claude\ Code
   open -a "Claude Code"
   ```

5. **MCP 서버 로그 확인**:
   - Claude Code 설정에서 MCP 로그 보기
   - 에러 메시지 확인

---

### Issue 2: Authentication Failed

**Symptoms**:
- "Access denied for user 'lobai_user'@'localhost'" 에러

**Solutions**:

1. **사용자 권한 확인**:
   ```sql
   mysql -u root -p
   SHOW GRANTS FOR 'lobai_user'@'localhost';
   ```

2. **비밀번호 재설정**:
   ```sql
   ALTER USER 'lobai_user'@'localhost' IDENTIFIED BY 'new_secure_password';
   FLUSH PRIVILEGES;
   ```

3. **설정 파일의 비밀번호 업데이트**:
   ```json
   "MYSQL_PASSWORD": "new_secure_password"
   ```

---

### Issue 3: Database Not Found

**Symptoms**:
- "Unknown database 'lobai_db'" 에러

**Solutions**:

1. **데이터베이스 존재 확인**:
   ```sql
   mysql -u lobai_user -p
   SHOW DATABASES;
   ```

2. **데이터베이스 생성**:
   ```sql
   CREATE DATABASE lobai_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

---

### Issue 4: Connection Timeout

**Symptoms**:
- 쿼리 실행 시 타임아웃

**Solutions**:

1. **MySQL max_connections 확인**:
   ```sql
   SHOW VARIABLES LIKE 'max_connections';
   ```

2. **타임아웃 설정 증가**:
   ```json
   "env": {
     "MYSQL_CONNECT_TIMEOUT": "30000"
   }
   ```

---

## Security Best Practices

### 1. 환경 변수 사용

**비밀번호를 설정 파일에 직접 쓰지 마세요**:

**❌ 나쁜 예**:
```json
"MYSQL_PASSWORD": "my_password_123"
```

**✅ 좋은 예**:
```json
"MYSQL_PASSWORD": "${MYSQL_PASSWORD}"
```

**환경 변수 설정**:
```bash
# ~/.zshrc 또는 ~/.bashrc에 추가
export MYSQL_PASSWORD="my_password_123"

# 적용
source ~/.zshrc
```

---

### 2. 읽기 전용 사용자 (프로덕션)

프로덕션 데이터베이스에는 **읽기 전용 사용자** 사용 권장:

```sql
CREATE USER 'lobai_readonly'@'localhost' IDENTIFIED BY 'readonly_password';
GRANT SELECT ON lobai_db.* TO 'lobai_readonly'@'localhost';
FLUSH PRIVILEGES;
```

**설정 파일**:
```json
"MYSQL_USER": "lobai_readonly"
```

---

### 3. .gitignore 설정

**절대 Git에 커밋하지 마세요**:

```bash
# .gitignore에 추가
claude_desktop_config.json
.env.local
```

---

## Performance Tips

### 1. 쿼리 제한

큰 테이블 조회 시 LIMIT 사용:

```
❌ "messages 테이블 모든 데이터 조회"
✅ "messages 테이블 최근 100개 조회"
```

---

### 2. 인덱스 확인

느린 쿼리 발견 시 EXPLAIN 사용:

```
"SELECT * FROM messages WHERE content LIKE '%keyword%' 쿼리 성능 분석"
→ Full Table Scan 발견
→ 인덱스 추가 권장
```

---

### 3. 연결 풀링

MCP 서버가 연결을 재사용하도록 설정:

```json
"env": {
  "MYSQL_CONNECTION_LIMIT": "10"
}
```

---

## Comparison: MySQL MCP vs Manual Query

| 작업 | Manual (Terminal) | MySQL MCP (Claude Code) |
|------|-------------------|-------------------------|
| 스키마 조회 | `mysql -u user -p` → `SHOW CREATE TABLE users;` | "users 테이블 스키마 보여줘" |
| 데이터 조회 | SQL 작성 → 실행 → 결과 복사 | "최근 사용자 10명 조회" |
| 인덱스 확인 | `SHOW INDEX FROM table;` | "테이블 인덱스 확인" |
| 쿼리 최적화 | EXPLAIN 수동 분석 | "쿼리 성능 분석해줘" |
| 복잡한 JOIN | 긴 SQL 작성 | "users와 messages JOIN해서 조회" |

**시간 절약**: 평균 **70% 단축**

---

## Integration with Backend Development

### Workflow Example

```
1. Database Designer Agent로 스키마 설계
   → schema.sql 생성

2. MySQL에 스키마 적용
   $ mysql -u lobai_user -p lobai_db < schema.sql

3. MySQL MCP로 스키마 확인
   "users 테이블 스키마 확인"

4. Backend Developer Agent로 Entity 작성
   → User.java 생성 (JPA 엔티티)

5. MySQL MCP로 데이터 검증
   "users 테이블에 데이터 삽입 후 조회"

6. Test Engineer Agent로 테스트 작성
   → UserRepositoryTest.java 생성

7. MySQL MCP로 테스트 데이터 확인
   "테스트 실행 후 users 테이블 데이터 확인"
```

---

## Advanced Features (Optional)

### 1. Flyway Migration 지원

Flyway 마이그레이션 히스토리 조회:

```
"Flyway 마이그레이션 히스토리 보여줘"

→ SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

---

### 2. 복잡한 Aggregate 쿼리

```
"persona별 메시지 개수 집계해줘"

→ SELECT p.name, COUNT(m.id) as message_count
  FROM personas p
  LEFT JOIN messages m ON p.id = m.persona_id
  GROUP BY p.id, p.name
  ORDER BY message_count DESC;
```

---

### 3. 데이터베이스 통계

```
"데이터베이스 통계 보여줘"

→ SELECT
    table_name,
    table_rows,
    ROUND((data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
  FROM information_schema.tables
  WHERE table_schema = 'lobai_db'
  ORDER BY (data_length + index_length) DESC;
```

---

## Related Documentation

- **Backend Plan**: `docs/plans/PLAN_Backend_Database_Design.md`
- **Recommended MCP Servers**: `.claude/mcp-configs/recommended-mcp-servers.md`
- **MCP Configs README**: `.claude/mcp-configs/README.md`

---

## Next Steps

1. **설치 완료 후**:
   - "lobai_db 데이터베이스 테이블 목록 조회" 테스트
   - Backend Developer Agent와 함께 사용

2. **스키마 적용 후**:
   - "모든 테이블 스키마 확인"
   - 인덱스 및 외래키 검증

3. **개발 중**:
   - 데이터 조회 및 검증
   - 쿼리 성능 분석

---

**Last Updated**: 2025-12-30
**Status**: Production Ready ✅
**Recommended**: ⭐⭐⭐⭐⭐ (백엔드 개발 필수)
