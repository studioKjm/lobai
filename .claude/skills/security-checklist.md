---
name: security-checklist
description: 보안 체크리스트 자동 적용. OWASP Top 10 기반 검증.
triggers: ["security", "secure", "api", "auth", "authentication", "authorization", "vulnerability"]
---

# Security Checklist

## Purpose

**OWASP Top 10** 기준으로 보안 취약점을 자동으로 검사합니다. 외부 API 연동, 사용자 입력 처리, 인증/인가 코드 작성 시 자동으로 트리거되어 보안 실수를 방지합니다.

---

## OWASP Top 10 Checklist

### 1️⃣ Broken Access Control (접근 제어 취약)

- [ ] **인증 필요 엔드포인트 보호**: 로그인 필요한 페이지/API 보호
- [ ] **권한 체크**: 사용자 역할(Role) 기반 접근 제어 (RBAC)
- [ ] **직접 객체 참조 방지**: `/api/users/123` → 본인 또는 권한 확인
- [ ] **CORS 설정**: 허용된 도메인만 접근 가능
- [ ] **Rate Limiting**: 과도한 요청 차단 (IP별, 사용자별)

**Example**:
```typescript
// ✅ 좋은 예: 권한 체크
const getUser = async (userId: string, requestUserId: string) => {
  if (userId !== requestUserId && !isAdmin(requestUserId)) {
    throw new ForbiddenError('권한이 없습니다')
  }
  return await db.users.findById(userId)
}

// ❌ 나쁜 예: 권한 체크 없음
const getUser = async (userId: string) => {
  return await db.users.findById(userId) // 누구나 접근 가능
}
```

---

### 2️⃣ Cryptographic Failures (암호화 실패)

- [ ] **비밀번호 해싱**: bcrypt, scrypt, argon2 사용 (MD5/SHA1 금지)
- [ ] **API Key 환경 변수**: 하드코딩 금지, `.env` 파일 사용
- [ ] **민감정보 암호화**: 데이터베이스에 저장 시 암호화
- [ ] **HTTPS 강제**: HTTP → HTTPS 리다이렉트
- [ ] **로그에 민감정보 제외**: 비밀번호, 토큰, 카드 번호 등

**Example**:
```typescript
// ✅ 좋은 예: bcrypt 해싱
import bcrypt from 'bcrypt'

const hashPassword = async (password: string) => {
  const salt = await bcrypt.genSalt(10)
  return await bcrypt.hash(password, salt)
}

// ❌ 나쁜 예: 평문 또는 약한 해싱
const hashPassword = (password: string) => {
  return md5(password) // 취약
}
```

**API Key 관리**:
```typescript
// ✅ 좋은 예: 환경 변수
const apiKey = process.env.GEMINI_API_KEY

// ❌ 나쁜 예: 하드코딩
const apiKey = 'AIzaSyABC123...' // Git에 노출
```

---

### 3️⃣ Injection (인젝션)

- [ ] **입력 검증**: 모든 외부 입력 검증 (query params, body, headers)
- [ ] **Prepared Statement**: SQL Injection 방지
- [ ] **화이트리스트 검증**: 허용된 값만 수용
- [ ] **Command Injection 방지**: 쉘 명령 실행 시 입력 검증
- [ ] **NoSQL Injection 방지**: MongoDB 쿼리 파라미터 검증

**SQL Injection 방지**:
```typescript
// ✅ 좋은 예: Prepared Statement
const getUser = async (email: string) => {
  return await db.query('SELECT * FROM users WHERE email = ?', [email])
}

// ❌ 나쁜 예: 동적 쿼리
const getUser = async (email: string) => {
  return await db.query(`SELECT * FROM users WHERE email = '${email}'`)
  // email = "' OR '1'='1" → 모든 사용자 조회
}
```

**입력 검증**:
```typescript
// ✅ 좋은 예: 화이트리스트 검증
const ALLOWED_SORT_FIELDS = ['name', 'createdAt', 'updatedAt']

const getUsers = (sortBy: string) => {
  if (!ALLOWED_SORT_FIELDS.includes(sortBy)) {
    throw new ValidationError('Invalid sort field')
  }
  return db.users.find().sort(sortBy)
}

// ❌ 나쁜 예: 검증 없음
const getUsers = (sortBy: string) => {
  return db.users.find().sort(sortBy) // 임의 필드 주입 가능
}
```

---

### 4️⃣ Insecure Design (안전하지 않은 설계)

- [ ] **보안 요구사항 초기 정의**: 기획 단계에서 보안 고려
- [ ] **Threat Modeling**: 공격 시나리오 분석
- [ ] **최소 권한 원칙**: 필요한 권한만 부여
- [ ] **Fail-Safe Defaults**: 기본값은 안전하게 (예: 기본 비공개)
- [ ] **Secure by Design**: 보안은 나중이 아닌 처음부터

**Example**:
```typescript
// ✅ 좋은 예: Fail-Safe Default
const createPost = async (data: PostData, isPublic = false) => {
  // 기본값은 비공개
  return await db.posts.create({ ...data, isPublic })
}

// ❌ 나쁜 예: 기본 공개
const createPost = async (data: PostData, isPublic = true) => {
  // 실수로 민감정보 공개 가능
}
```

---

### 5️⃣ Security Misconfiguration (보안 설정 오류)

- [ ] **기본 설정 변경**: 기본 비밀번호, 기본 포트 변경
- [ ] **불필요한 기능 비활성화**: 디버그 모드, 샘플 페이지 제거
- [ ] **보안 헤더 설정**: X-Frame-Options, CSP, HSTS 등
- [ ] **에러 메시지 통제**: 스택트레이스 노출 금지 (프로덕션)
- [ ] **최신 패치 적용**: 의존성 정기 업데이트

**보안 헤더 (vercel.json)**:
```json
{
  "headers": [
    {
      "source": "/(.*)",
      "headers": [
        {
          "key": "X-Frame-Options",
          "value": "DENY"
        },
        {
          "key": "X-Content-Type-Options",
          "value": "nosniff"
        },
        {
          "key": "X-XSS-Protection",
          "value": "1; mode=block"
        },
        {
          "key": "Strict-Transport-Security",
          "value": "max-age=31536000; includeSubDomains"
        },
        {
          "key": "Content-Security-Policy",
          "value": "default-src 'self'; script-src 'self' 'unsafe-inline'"
        }
      ]
    }
  ]
}
```

**에러 핸들링**:
```typescript
// ✅ 좋은 예: 프로덕션에서 스택트레이스 숨김
app.use((err, req, res, next) => {
  console.error(err.stack) // 로그에만 기록
  res.status(500).json({
    error: '서버 오류가 발생했습니다',
    ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
  })
})

// ❌ 나쁜 예: 스택트레이스 노출
app.use((err, req, res, next) => {
  res.status(500).json({ error: err.stack }) // 공격자에게 정보 제공
})
```

---

### 6️⃣ Vulnerable and Outdated Components (취약한 구성 요소)

- [ ] **의존성 감사**: `npm audit`, `snyk` 정기 실행
- [ ] **자동 업데이트**: Dependabot, Renovate 사용
- [ ] **사용하지 않는 패키지 제거**: 불필요한 의존성 삭제
- [ ] **라이선스 확인**: 상용 사용 가능 여부 체크
- [ ] **CVE 모니터링**: 알려진 취약점 추적

**Commands**:
```bash
# 취약점 검사
npm audit
npm audit fix

# Snyk (더 상세)
npx snyk test
npx snyk monitor

# 의존성 업데이트
npm outdated
npm update
```

---

### 7️⃣ Identification and Authentication Failures (인증 실패)

- [ ] **강력한 비밀번호 정책**: 8자 이상, 대소문자/숫자/특수문자
- [ ] **다중 인증 (MFA)**: 2FA, OTP 지원
- [ ] **세션 관리**: 타임아웃, httpOnly 쿠키, secure 플래그
- [ ] **Brute Force 방지**: 로그인 시도 제한 (5회/분)
- [ ] **비밀번호 재설정 안전**: 토큰 기반, 시간 제한

**세션 관리**:
```typescript
// ✅ 좋은 예: 안전한 쿠키 설정
res.cookie('sessionId', token, {
  httpOnly: true,    // JavaScript 접근 불가 (XSS 방지)
  secure: true,      // HTTPS only
  sameSite: 'strict',// CSRF 방지
  maxAge: 3600000    // 1시간
})

// ❌ 나쁜 예: 안전하지 않은 쿠키
res.cookie('sessionId', token) // 기본 설정 사용
```

**Rate Limiting (로그인)**:
```typescript
import rateLimit from 'express-rate-limit'

const loginLimiter = rateLimit({
  windowMs: 60 * 1000, // 1분
  max: 5,              // 5회 시도
  message: '너무 많은 로그인 시도입니다. 1분 후 다시 시도하세요.'
})

app.post('/api/login', loginLimiter, loginHandler)
```

---

### 8️⃣ Software and Data Integrity Failures (무결성 실패)

- [ ] **코드 서명**: 배포 패키지 서명 검증
- [ ] **CI/CD 파이프라인 보호**: 무단 수정 방지
- [ ] **의존성 무결성**: package-lock.json 커밋
- [ ] **SRI (Subresource Integrity)**: CDN 스크립트 해시 검증
- [ ] **자동 업데이트 검증**: 업데이트 전 서명 확인

**SRI Example**:
```html
<!-- ✅ 좋은 예: SRI 해시 -->
<script src="https://cdn.example.com/library.js"
        integrity="sha384-oqVuAfXRKap7fdgcCY5uykM6+R9GqQ8K/uxy9rx7HNQlGYl1kPzQho1wx4JwY8wC"
        crossorigin="anonymous"></script>

<!-- ❌ 나쁜 예: SRI 없음 -->
<script src="https://cdn.example.com/library.js"></script>
```

---

### 9️⃣ Security Logging and Monitoring Failures (로깅 및 모니터링 실패)

- [ ] **보안 이벤트 로깅**: 로그인 실패, 권한 거부, 입력 검증 실패
- [ ] **로그 보호**: 로그 파일 접근 제한
- [ ] **실시간 모니터링**: 이상 패턴 감지 (Sentry, DataDog)
- [ ] **알림 설정**: 보안 이벤트 발생 시 알림
- [ ] **로그 보관**: 최소 30일 이상

**Logging Example**:
```typescript
// ✅ 좋은 예: 보안 이벤트 로깅
logger.warn({
  event: 'LOGIN_FAILED',
  username: email,
  ip: req.ip,
  timestamp: new Date()
})

// ❌ 나쁜 예: 로그 없음
if (password !== user.password) {
  return res.status(401).json({ error: 'Invalid credentials' })
  // 로그인 실패 추적 불가
}
```

---

### 🔟 Server-Side Request Forgery (SSRF)

- [ ] **URL 검증**: 사용자 입력 URL 화이트리스트 검증
- [ ] **내부 IP 차단**: 127.0.0.1, 192.168.x.x, 10.x.x.x 접근 금지
- [ ] **프로토콜 제한**: HTTP/HTTPS만 허용 (file://, ftp:// 금지)
- [ ] **리다이렉트 추적 제한**: 무한 리다이렉트 방지

**Example**:
```typescript
// ✅ 좋은 예: URL 검증
const ALLOWED_DOMAINS = ['api.example.com', 'cdn.example.com']

const fetchExternal = async (url: string) => {
  const parsedUrl = new URL(url)

  if (!ALLOWED_DOMAINS.includes(parsedUrl.hostname)) {
    throw new Error('허용되지 않은 도메인입니다')
  }

  if (parsedUrl.hostname.startsWith('192.168.') ||
      parsedUrl.hostname.startsWith('10.')) {
    throw new Error('내부 IP 접근 금지')
  }

  return await fetch(url)
}

// ❌ 나쁜 예: 검증 없음
const fetchExternal = async (url: string) => {
  return await fetch(url) // 내부 서비스 공격 가능
}
```

---

## GENKUB 프로젝트 특화 체크리스트

### Gemini API 보안

- [ ] **API Key 환경 변수**: `process.env.GEMINI_API_KEY` 사용
- [ ] **클라이언트 노출 방지**: 프론트엔드에서 직접 호출 금지
- [ ] **BFF 패턴 사용**: Spring Boot 프록시 통해 호출
- [ ] **Rate Limiting**: Gemini API 제한 준수 (60 requests/minute)
- [ ] **입력 검증**: 채팅 메시지 길이 제한 (500자)
- [ ] **Prompt Injection 방어**: 악의적 프롬프트 필터링

**Current Issue**:
```typescript
// ❌ 현재 문제: API Key 클라이언트 노출
const ai = new GoogleGenAI({ apiKey: process.env.API_KEY })
// Vite define으로 브라우저에 주입됨

// ✅ 개선 방안: Spring Boot BFF
// Frontend → Spring Boot → Gemini API
```

### Stats 시스템 보안

- [ ] **클라이언트 검증**: 스탯 값 범위 확인 (0-100)
- [ ] **서버 검증** (백엔드 추가 시): 클라이언트 값 신뢰 금지
- [ ] **치트 방지**: 비정상적 스탯 증가 감지

### 3D Spline 보안

- [ ] **iframe 샌드박스**: `sandbox` 속성 사용
- [ ] **CSP 설정**: Spline CDN만 허용
- [ ] **XSS 방지**: Spline 오브젝트 이름 사용자 입력 금지

---

## Automated Tools

### 1. npm audit
```bash
# 취약점 검사
npm audit

# 자동 수정 (가능한 것만)
npm audit fix

# 강제 업데이트 (주의)
npm audit fix --force
```

### 2. Snyk
```bash
# 설치
npm install -g snyk

# 인증
snyk auth

# 테스트
snyk test

# 모니터링 (CI에서 사용)
snyk monitor
```

### 3. OWASP ZAP (동적 분석)
```bash
# Docker로 실행
docker run -t owasp/zap2docker-stable zap-baseline.py -t https://your-app.com
```

### 4. SonarQube (정적 분석)
```bash
# 실행
sonar-scanner \
  -Dsonar.projectKey=genkub \
  -Dsonar.sources=src \
  -Dsonar.security.hotspots.onlyYours=false
```

---

## Security Code Review Template

```markdown
## Security Review Checklist

### 🔐 인증/인가
- [ ] 엔드포인트 보호 확인
- [ ] 권한 체크 구현
- [ ] 세션/토큰 관리 안전

### 🛡️ 입력 검증
- [ ] 모든 외부 입력 검증
- [ ] 화이트리스트 기반
- [ ] SQL/NoSQL Injection 방지

### 🔒 민감정보
- [ ] API Key 환경 변수 사용
- [ ] 비밀번호 해싱 (bcrypt)
- [ ] 로그에 민감정보 제외

### 🌐 네트워크
- [ ] HTTPS 강제
- [ ] CORS 설정 적절
- [ ] Rate Limiting 구현

### 📦 의존성
- [ ] npm audit 통과
- [ ] 최신 버전 사용
- [ ] 취약점 없음
```

---

## Emergency Response

### 취약점 발견 시 대응

1. **즉시 조치**:
   - 배포 중단 (필요 시)
   - 취약점 패치 또는 임시 완화 조치
   - 영향 범위 파악

2. **사용자 통지**:
   - 민감정보 노출 시 사용자 알림
   - 비밀번호 재설정 요청 (필요 시)

3. **사후 조치**:
   - Postmortem 작성
   - 재발 방지책 수립
   - 보안 테스트 강화

---

**Skill Version**: 1.0.0
**Last Updated**: 2025-12-30
**Next Review**: Spring Boot 백엔드 추가 후
