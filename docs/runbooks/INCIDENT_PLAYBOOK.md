# Incident Response Playbook

**LobAI 장애 대응 매뉴얼**
**Version**: 1.0.0
**Last Updated**: 2026-01-04

이 문서는 LobAI 서비스 장애 발생 시 초기 대응, 원인 분석, 복구 절차를 다룹니다.

---

## Table of Contents

1. [장애 등급 분류](#1-장애-등급-분류)
2. [초기 대응 절차](#2-초기-대응-절차)
3. [근본 원인 분석](#3-근본-원인-분석)
4. [복구 절차](#4-복구-절차)
5. [사후 처리](#5-사후-처리)
6. [연락처 및 에스컬레이션](#6-연락처-및-에스컬레이션)

---

## 1. 장애 등급 분류

### 1.1 P0 (Critical) - 즉시 대응

**정의**: 서비스 전체 다운 또는 심각한 보안 문제

**예시**:
- ❌ 웹사이트 접속 불가 (500 에러)
- ❌ Database 서버 다운
- ❌ 데이터 유출 또는 보안 침해
- ❌ 결제 시스템 완전 중단

**SLA**: **15분 이내** 초기 대응 시작

**대응 팀**: On-call 엔지니어 즉시 소집

---

### 1.2 P1 (High) - 긴급 대응

**정의**: 주요 기능 불가 (일부 사용자 영향)

**예시**:
- ⚠️ 로그인 실패율 >50%
- ⚠️ AI 채팅 응답 실패 (Gemini API 장애)
- ⚠️ 출석 체크 기능 오류
- ⚠️ 관리자 페이지 접근 불가

**SLA**: **1시간 이내** 초기 대응 시작

**대응 팀**: On-call 엔지니어 + 관련 팀

---

### 1.3 P2 (Medium) - 일부 기능 저하

**정의**: 일부 기능 저하 또는 성능 문제

**예시**:
- ⚠️ API 응답 시간 >3초 (정상: <500ms)
- ⚠️ 특정 페이지 로딩 느림
- ⚠️ 이미지 업로드 간헐적 실패

**SLA**: **4시간 이내** 대응 시작

**대응 팀**: 담당 개발자

---

### 1.4 P3 (Low) - 경미한 버그

**정의**: 사용자 경험에 미미한 영향

**예시**:
- 🔹 UI 표시 오류 (기능은 정상)
- 🔹 오타 또는 번역 오류
- 🔹 로그 출력 오류

**SLA**: **1일 이내** 대응

**대응 팀**: 담당 개발자

---

## 2. 초기 대응 절차

### 2.1 장애 감지 및 알림

#### 자동 감지 (모니터링 시스템)

**Health Check 실패**:
```bash
# Health check endpoint
curl https://api.lobai.com/health

# Expected response (HTTP 200)
{
  "status": "UP",
  "timestamp": "2026-01-04T10:30:00",
  "version": "1.0.0"
}

# 연속 3회 실패 시 → Slack 알림 발송
```

**에러율 급증**:
- Sentry: 에러율 >5% (정상: <1%)
- CloudWatch/Grafana: 5xx 에러 >10/min

#### 수동 보고

**사용자 신고 → Slack #support 채널**:
```
긴급 장애 신고
- 신고자: 사용자 이름 또는 이메일
- 증상: 로그인 시 500 에러 발생
- 발생 시각: 2026-01-04 10:25
- 브라우저: Chrome 120
- 스크린샷: (첨부)
```

### 2.2 영향 범위 파악

**체크리스트**:

```bash
# 1. 서비스 상태 확인
curl https://lobai.com           # Frontend
curl https://api.lobai.com/health # Backend

# 2. 사용자 영향 확인
# - 전체 사용자? 일부 사용자?
# - 특정 브라우저? 특정 지역?

# 3. Database 연결 확인
mysql -h prod-db.lobai.com -u admin -p -e "SELECT 1;"

# 4. 외부 API 상태 확인
curl https://generativelanguage.googleapis.com/v1beta/models # Gemini API

# 5. 최근 배포 확인
git log -5 --oneline  # 최근 5개 커밋
```

### 2.3 긴급 공지

**사용자 알림** (P0/P1 장애 시):

```markdown
# Slack #announcements

🚨 **장애 공지**

현재 LobAI 서비스에 일시적인 장애가 발생했습니다.

- 발생 시각: 2026-01-04 10:25
- 영향 범위: 로그인 기능 전체
- 예상 복구: 30분 이내

복구 중이며, 진행 상황은 계속 업데이트하겠습니다.
```

**Twitter/Discord/홈페이지 배너**:
```html
<div class="alert alert-danger">
  ⚠️ 현재 서비스 점검 중입니다. 불편을 드려 죄송합니다.
</div>
```

### 2.4 임시 조치

**우선 복구 후 원인 분석**:

```bash
# 1. 서버 재시작 (빠른 복구)
aws ecs update-service --cluster lobai-prod --service lobai-backend --force-new-deployment

# 2. 최근 배포 롤백
./scripts/rollback.sh

# 3. Traffic 우회 (Circuit Breaker)
# Load Balancer에서 장애 서버 제외

# 4. Rate Limiting 강화
# 공격으로 의심되는 경우
```

---

## 3. 근본 원인 분석

### 3.1 로그 분석

#### Backend 로그

```bash
# AWS CloudWatch Logs
aws logs tail /ecs/lobai-backend --follow

# 특정 시간대 로그 필터링
aws logs filter-log-events \
  --log-group-name /ecs/lobai-backend \
  --start-time $(date -d '10 minutes ago' +%s)000 \
  --filter-pattern "ERROR"

# 로컬 로그 파일
tail -f backend/logs/application.log | grep ERROR
```

**주요 확인 사항**:
- ❌ `OutOfMemoryError`: 메모리 부족
- ❌ `SQLException`: Database 연결 문제
- ❌ `HttpClientErrorException: 500`: 외부 API 장애
- ❌ `NullPointerException`: 코드 버그

#### Frontend 로그

```bash
# Vercel Logs
vercel logs lobai.com --follow

# Browser Console (Sentry)
# https://sentry.io/organizations/lobai/issues/
```

**주요 확인 사항**:
- ❌ `TypeError: Cannot read property 'data' of undefined`
- ❌ `CORS error`: Backend CORS 설정 문제
- ❌ `401 Unauthorized`: Token 만료 또는 인증 실패

### 3.2 Database 쿼리 모니터링

```sql
-- MySQL: 현재 실행 중인 쿼리 확인
SHOW FULL PROCESSLIST;

-- 느린 쿼리 로그
SELECT * FROM mysql.slow_log
WHERE query_time > 1
ORDER BY start_time DESC
LIMIT 10;

-- Lock 대기 확인
SELECT * FROM information_schema.INNODB_LOCKS;
```

**문제 식별**:
- ⚠️ Long-running queries (>10초)
- ⚠️ Lock contention (LOCK WAIT)
- ⚠️ Connection pool exhaustion

### 3.3 외부 API 장애 확인

**Gemini API**:

```bash
# Status page
curl https://status.cloud.google.com/

# API 직접 테스트
curl -X POST https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent \
  -H "Content-Type: application/json" \
  -H "x-goog-api-key: $GEMINI_API_KEY" \
  -d '{"contents":[{"parts":[{"text":"Hello"}]}]}'
```

**AWS 서비스**:
```bash
# AWS Health Dashboard
aws health describe-events --filter eventTypeCategories=issue
```

---

## 4. 복구 절차

### 4.1 애플리케이션 장애

#### Case 1: 서버 재시작

```bash
# ECS 서비스 재시작
aws ecs update-service \
  --cluster lobai-production \
  --service lobai-backend \
  --force-new-deployment

# 재시작 확인
aws ecs describe-services \
  --cluster lobai-production \
  --services lobai-backend \
  --query 'services[0].deployments'

# Health check
curl https://api.lobai.com/health
```

#### Case 2: 이전 버전 롤백

```bash
# Rollback script 실행
./scripts/rollback.sh

# 확인
curl https://api.lobai.com/health
git log -1  # 현재 배포된 버전 확인
```

#### Case 3: Hotfix 배포

```bash
# 1. Hotfix 브랜치 생성
git checkout -b hotfix/critical-bug main

# 2. 버그 수정 및 커밋
git commit -m "fix(auth): Fix JWT validation bug"

# 3. 배포 (CI/CD 자동 실행)
git push origin hotfix/critical-bug

# 4. Production 배포 승인 (GitHub Actions)
# GitHub → Actions → Deploy to Production → Approve

# 5. 검증
curl https://api.lobai.com/health
```

### 4.2 Database 장애

#### Case 1: Replica 승격

```bash
# 1. Primary DB 상태 확인
mysql -h primary-db.lobai.com -e "SHOW MASTER STATUS;"

# 2. Replica를 Primary로 승격
aws rds promote-read-replica \
  --db-instance-identifier lobai-replica

# 3. Application 설정 변경
# backend/src/main/resources/application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://lobai-replica.rds.amazonaws.com:3306/lobai

# 4. 서비스 재시작
./scripts/restart.sh
```

#### Case 2: Backup 복원

```bash
# 1. 최신 백업 확인
aws rds describe-db-snapshots \
  --db-instance-identifier lobai-prod \
  --query 'DBSnapshots[0]'

# 2. 새 DB 인스턴스로 복원
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier lobai-restore-$(date +%Y%m%d) \
  --db-snapshot-identifier lobai-prod-snapshot-2026-01-04

# 3. 데이터 무결성 검증
mysql -h lobai-restore.rds.amazonaws.com \
  -e "SELECT COUNT(*) FROM users; SELECT COUNT(*) FROM messages;"

# 4. Application 설정 변경 및 재시작
```

#### Case 3: Data Integrity 검증

```sql
-- 1. 사용자 데이터 확인
SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS active_users FROM users WHERE is_active = TRUE;

-- 2. 메시지 데이터 확인
SELECT COUNT(*) AS total_messages FROM messages;
SELECT user_id, COUNT(*) AS message_count
FROM messages
GROUP BY user_id
ORDER BY message_count DESC
LIMIT 10;

-- 3. 출석 데이터 확인
SELECT COUNT(*) AS total_attendance FROM attendance_records;
SELECT check_in_date, COUNT(*) AS users_count
FROM attendance_records
GROUP BY check_in_date
ORDER BY check_in_date DESC
LIMIT 7;

-- 4. 제약 조건 확인
SELECT CONSTRAINT_NAME, TABLE_NAME
FROM information_schema.TABLE_CONSTRAINTS
WHERE CONSTRAINT_TYPE = 'FOREIGN KEY'
  AND TABLE_SCHEMA = 'lobai';
```

### 4.3 외부 API 장애 (Gemini)

#### Fallback 응답 활성화

**backend/src/main/java/com/lobai/service/GeminiService.java**:

```java
@Service
public class GeminiService {

    @Value("${gemini.fallback.enabled:false}")
    private boolean fallbackEnabled;

    public String generateResponse(String userMessage) {
        try {
            // 실제 Gemini API 호출
            return callGeminiApi(userMessage);
        } catch (Exception e) {
            log.error("Gemini API failed, using fallback", e);

            if (fallbackEnabled) {
                return getFallbackResponse(userMessage);
            } else {
                throw new ServiceUnavailableException("AI 서비스 일시 중단");
            }
        }
    }

    private String getFallbackResponse(String userMessage) {
        return "죄송합니다. AI 서비스가 일시적으로 불안정합니다. "
             + "잠시 후 다시 시도해주세요. 😊";
    }
}
```

**긴급 활성화**:

```bash
# Environment variable 설정
aws ecs update-service \
  --cluster lobai-production \
  --service lobai-backend \
  --task-definition lobai-backend-prod \
  --force-new-deployment

# 또는 application.yml 수정 후 배포
gemini:
  fallback:
    enabled: true
```

#### Rate Limit 조정

```yaml
# backend/src/main/resources/application-prod.yml
gemini:
  rate-limit:
    requests-per-minute: 30  # 기본: 60 → 30으로 감소
```

---

## 5. 사후 처리

### 5.1 Post-mortem 작성

**템플릿: docs/postmortems/YYYY-MM-DD-incident-name.md**:

```markdown
# Post-mortem: 로그인 서비스 장애

**Date**: 2026-01-04
**Duration**: 10:25 - 11:05 (40분)
**Severity**: P1 (High)
**Incident Commander**: Alice

## Summary

JWT 토큰 검증 로직 버그로 인해 모든 로그인 시도가 실패했습니다.

## Timeline

- **10:25** - Slack 알림: Health check 실패
- **10:27** - On-call 엔지니어 확인 시작
- **10:30** - 영향 범위 파악: 로그인 기능 전체 다운
- **10:32** - 긴급 공지 발송
- **10:35** - 로그 분석: JwtTokenProvider에서 NullPointerException 발견
- **10:40** - 이전 버전으로 롤백 결정
- **10:45** - 롤백 완료
- **10:50** - 서비스 정상화 확인
- **11:00** - Hotfix PR 생성 (#456)
- **11:05** - 정상화 공지

## Root Cause

**변경사항**:
- Commit: a1b2c3d - "refactor(auth): Simplify JWT validation"
- 변경 파일: JwtTokenProvider.java

**문제**:
```java
// Before (정상)
String userId = claims.get("sub", String.class);
if (userId == null) {
    throw new InvalidTokenException("Invalid token");
}

// After (버그)
String userId = claims.get("userId", String.class);  // ❌ Wrong key
```

JWT payload에는 `sub` 필드가 있지만, 코드에서 `userId`로 조회하여 항상 null 반환.

## Impact

- **영향 받은 사용자**: 전체 (약 1,000명)
- **영향 받은 기능**: 로그인, 회원가입
- **Downtime**: 40분
- **손실**: 약 50명의 로그인 시도 실패 (재시도 성공)

## Resolution

1. 이전 버전(v1.2.3)으로 롤백
2. Hotfix PR 생성 및 리뷰
3. Hotfix 배포 (v1.2.4)

## Lessons Learned

### What Went Well

✅ 빠른 감지 (5분 이내)
✅ 명확한 On-call 프로세스
✅ 롤백 스크립트 정상 작동

### What Went Wrong

❌ 코드 리뷰에서 버그 미발견
❌ 테스트 커버리지 부족 (JwtTokenProvider 미테스트)
❌ Staging 환경 테스트 부실

### Action Items

| Action | Owner | Due Date | Status |
|--------|-------|----------|--------|
| JwtTokenProvider 단위 테스트 추가 | Bob | 2026-01-07 | ✅ Done |
| Staging 배포 후 E2E 테스트 자동 실행 | Alice | 2026-01-10 | 🔄 In Progress |
| Code review 체크리스트에 "테스트 커버리지" 항목 추가 | Charlie | 2026-01-05 | ✅ Done |
```

### 5.2 재발 방지 대책

**1. 테스트 커버리지 강화**:

```java
// backend/src/test/java/com/lobai/security/JwtTokenProviderTest.java
@Test
void validateToken_WithValidToken_Success() {
    String token = jwtTokenProvider.createAccessToken(1L, "test@test.com", "USER");
    Long userId = jwtTokenProvider.getUserIdFromToken(token);
    assertThat(userId).isEqualTo(1L);
}

@Test
void validateToken_WithInvalidToken_ThrowsException() {
    assertThatThrownBy(() -> jwtTokenProvider.validateToken("invalid_token"))
        .isInstanceOf(InvalidTokenException.class);
}
```

**2. CI/CD 파이프라인 강화**:

```yaml
# .github/workflows/staging-deploy.yml
  e2e-tests:
    name: E2E Tests (Mandatory)
    needs: deploy-backend
    steps:
      - name: Run E2E tests
        run: npx playwright test

      - name: Block deployment if tests fail
        if: failure()
        run: |
          echo "E2E tests failed. Deployment blocked."
          exit 1
```

**3. 모니터링 강화**:

```yaml
# CloudWatch Alarm
AlarmName: HighErrorRate
MetricName: 5XXError
Threshold: 10  # 5xx 에러 10개 이상 시 알림
EvaluationPeriods: 1
ComparisonOperator: GreaterThanThreshold
```

### 5.3 문서 업데이트

**업데이트 필요 문서**:
- ✅ Incident Playbook (이 문서)
- ✅ TEST_GUIDE.md - JwtTokenProvider 테스트 예시 추가
- ✅ CICD_GUIDE.md - E2E 테스트 필수화

---

## 6. 연락처 및 에스컬레이션

### 6.1 On-call 담당자

| 주차 | 담당자 | Slack | 전화 |
|------|--------|-------|------|
| Week 1 | Alice | @alice | +82-10-1234-5678 |
| Week 2 | Bob | @bob | +82-10-2345-6789 |
| Week 3 | Charlie | @charlie | +82-10-3456-7890 |

### 6.2 에스컬레이션 체인

```
Level 1: On-call 엔지니어
    ↓ (15분 내 해결 불가 시)
Level 2: 팀 리드
    ↓ (30분 내 해결 불가 시)
Level 3: CTO
    ↓ (1시간 내 해결 불가 시)
Level 4: CEO (P0만)
```

### 6.3 외부 업체 연락처

| 서비스 | 연락처 | 용도 |
|--------|--------|------|
| **AWS Support** | aws-support@amazon.com | 인프라 장애 |
| **Vercel Support** | support@vercel.com | Frontend 배포 문제 |
| **Google Cloud (Gemini)** | https://cloud.google.com/support | AI API 장애 |
| **MySQL Hosting** | support@planetscale.com | Database 문제 |

### 6.4 긴급 연락망

**Slack Channels**:
- `#incidents` - 장애 대응 채널 (P0/P1 전용)
- `#on-call` - On-call 엔지니어 채널
- `#support` - 사용자 문의

**PagerDuty** (또는 Opsgenie):
- P0/P1 장애 발생 시 자동 호출
- 15분 내 응답 없으면 에스컬레이션

---

## Appendix: 자주 발생하는 장애 및 해결법

### A.1 "Database connection pool exhausted"

**증상**:
```
com.zaxxer.hikari.pool.HikariPool$PoolInitializationException:
Failed to initialize pool: Connection is not available
```

**원인**: Database connection leak 또는 과도한 트래픽

**해결**:
```yaml
# application.yml - Connection pool 크기 증가
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # 기본: 10 → 20
      connection-timeout: 30000
```

```java
// Connection leak 확인
@Autowired
private HikariDataSource dataSource;

public void checkPoolStatus() {
    HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
    log.info("Active connections: {}", poolBean.getActiveConnections());
    log.info("Idle connections: {}", poolBean.getIdleConnections());
    log.info("Total connections: {}", poolBean.getTotalConnections());
}
```

### A.2 "OutOfMemoryError: Java heap space"

**증상**:
```
java.lang.OutOfMemoryError: Java heap space
```

**원인**: 메모리 누수 또는 Heap 크기 부족

**해결**:
```bash
# 1. Heap dump 생성
jmap -dump:format=b,file=heap.bin <PID>

# 2. Heap dump 분석 (Eclipse MAT)
# Download: https://www.eclipse.org/mat/

# 3. JVM 옵션 조정
java -Xms2g -Xmx4g -jar app.jar
```

### A.3 "CORS policy error"

**증상**:
```
Access to XMLHttpRequest has been blocked by CORS policy
```

**원인**: Backend CORS 설정 누락

**해결**:
```java
// backend/src/main/java/com/lobai/config/CorsConfig.java
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins(
                        "http://localhost:5173",
                        "https://lobai.com"
                    )
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

---

## Changelog

| Version | Date       | Changes                             | Author |
|---------|------------|-------------------------------------|--------|
| 1.0.0   | 2026-01-04 | Initial INCIDENT_PLAYBOOK created   | Team   |

---

**관련 문서**:
- [CICD_GUIDE.md](../guides/CICD_GUIDE.md)
- [DEV_GUIDE.md](../guides/DEV_GUIDE.md)
- [PROJECT_CONSTITUTION.md](../../PROJECT_CONSTITUTION.md)
