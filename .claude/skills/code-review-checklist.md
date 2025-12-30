---
name: code-review-checklist
description: PR에서 반드시 확인해야 할 항목 강제 적용. 사람 리뷰 전 1차 자동 검증.
triggers: ["review", "pr", "pull request", "checklist", "code review"]
---

# Code Review Checklist

## Purpose

PR(Pull Request) 리뷰 시 **놓치기 쉬운 항목을 자동으로 체크**하여 일관된 코드 품질을 유지합니다. 사람 리뷰 전 1차 필터로 사용하면 리뷰 시간을 50% 단축할 수 있습니다.

---

## Automatic Checklist

### ✅ 기능 (Functionality)

- [ ] **요구사항 충족**: PR이 이슈/티켓의 모든 요구사항을 만족하는가?
- [ ] **엣지 케이스 처리**: 빈 값, null, undefined, 경계값 등 예외 상황 처리
- [ ] **에러 핸들링**: try-catch, 에러 메시지, 사용자 피드백
- [ ] **기능 완결성**: 부분 구현이 아닌 완전한 기능
- [ ] **의도하지 않은 부작용 없음**: 기존 기능에 영향 없음

---

### 🧪 테스트 (Tests)

- [ ] **단위 테스트 추가**: 새 함수/클래스에 대한 테스트 존재
- [ ] **통합 테스트** (필요 시): 컴포넌트 간 상호작용 테스트
- [ ] **모든 테스트 통과**: CI에서 100% 성공
- [ ] **커버리지 기준 충족**: 비즈니스 로직 ≥80%
- [ ] **불안정한 테스트 없음**: 3회 이상 실행하여 안정성 확인
- [ ] **테스트 이름 명확**: "should..." 형식, 동작 명시
- [ ] **AAA 패턴 준수**: Arrange - Act - Assert 구조

---

### 🔒 보안 (Security)

- [ ] **입력 검증**: 모든 외부 입력(query params, body, headers) 검증
- [ ] **API Key 노출 없음**: 하드코딩 금지, 환경 변수 사용
- [ ] **XSS 방지**: 출력 이스케이핑, dangerouslySetInnerHTML 사용 확인
- [ ] **CSRF 방지**: 토큰 또는 SameSite 쿠키
- [ ] **SQL Injection** (백엔드): Prepared Statement 사용
- [ ] **민감정보 로그 제외**: 비밀번호, 토큰 등 로그에 미포함
- [ ] **권한 체크**: 인증/인가 필요 엔드포인트 보호
- [ ] **Rate Limiting** (API): 과도한 요청 방지

---

### ⚡ 성능 (Performance)

- [ ] **N+1 쿼리 없음**: 반복문 안에서 DB 조회 금지
- [ ] **불필요한 리렌더링 없음**: React.memo, useMemo, useCallback 활용
- [ ] **메모리 누수 없음**: 이벤트 리스너 해제, useEffect cleanup
- [ ] **큰 데이터 가상화**: 긴 리스트는 react-window 등 사용
- [ ] **이미지 최적화**: WebP, lazy loading, 적절한 해상도
- [ ] **번들 크기 증가 확인**: 새 라이브러리 추가 시 번들 분석
- [ ] **캐싱 전략**: API 응답 캐싱 (TanStack Query 등)

---

### 🎨 코드 품질 (Code Quality)

- [ ] **린트 에러 없음**: ESLint, Prettier 통과
- [ ] **타입 체크 통과**: TypeScript 컴파일 성공
- [ ] **중복 코드 없음**: DRY 원칙 준수, 함수/컴포넌트 재사용
- [ ] **명확한 네이밍**: 변수/함수명이 역할을 드러냄
- [ ] **복잡도 낮음**: Cyclomatic Complexity < 10
- [ ] **함수 길이 적절**: 한 함수는 하나의 일만 (< 50줄 권장)
- [ ] **주석 적절**: 복잡한 로직에만 "왜"를 설명
- [ ] **Magic Number 제거**: 상수로 추출하여 의미 부여

---

### 📚 문서화 (Documentation)

- [ ] **복잡한 로직 주석**: "왜"를 설명 ("무엇"은 코드가 설명)
- [ ] **API 문서 업데이트**: 새 엔드포인트 추가 시
- [ ] **README 업데이트**: 사용법 변경 시
- [ ] **CHANGELOG**: 중요한 변경사항 기록
- [ ] **타입 정의**: TypeScript 인터페이스/타입 명확

---

### 🔧 설정 및 환경 (Config)

- [ ] **환경 변수 사용**: 설정값 하드코딩 금지
- [ ] **.gitignore 업데이트**: 민감 파일, 빌드 아티팩트 제외
- [ ] **의존성 버전 고정**: package.json 정확한 버전
- [ ] **DB 마이그레이션**: 스키마 변경 시 마이그레이션 파일
- [ ] **롤백 가능성**: 배포 실패 시 되돌릴 수 있는가?

---

## Severity Levels

체크 항목을 우선순위별로 분류:

### 🔴 **Blocker** (PR 머지 불가)
- 테스트 실패
- 보안 취약점 (High/Critical)
- 기능 미완성
- 린트/타입 에러

### 🟡 **Major** (수정 권장)
- 커버리지 미달
- 성능 이슈 (메모리 누수, N+1)
- 중복 코드
- 문서 누락

### 🟢 **Minor** (선택적)
- 네이밍 개선
- 주석 추가
- 리팩터링 제안

---

## Review Workflow

```
1. 🤖 자동 체크 (이 스킬)
   - 체크리스트 자동 적용
   - Blocker 항목 확인

2. 🔍 도구 기반 검증
   - Lint: npm run lint
   - Test: npm test
   - Coverage: npm test -- --coverage
   - Build: npm run build

3. 👤 사람 리뷰
   - 비즈니스 로직 검증
   - 아키텍처 일관성
   - UX/디자인 확인

4. ✅ 승인 및 머지
```

---

## Examples

### Example 1: 기본 PR 리뷰

```
User: "review this PR"

Code Review Checklist 스킬 활성화:

✅ 기능
- [x] 요구사항 충족
- [x] 엣지 케이스 처리
- [x] 에러 핸들링

🧪 테스트
- [x] 단위 테스트 추가
- [ ] 커버리지 미달 (현재 65%, 목표 80%) ← 🟡 Major

🔒 보안
- [x] 입력 검증
- [ ] API Key 환경 변수 사용 확인 필요 ← 🔴 Blocker

⚡ 성능
- [x] 메모리 누수 없음
- [x] 리렌더링 최적화

🎨 코드 품질
- [x] 린트 통과
- [x] 타입 체크 통과
- [ ] handleAction 함수 복잡도 높음 (CC: 12) ← 🟡 Major

📚 문서화
- [x] 주석 추가
- [ ] README 업데이트 필요 ← 🟢 Minor

🔴 Blocker: 1개
🟡 Major: 2개
🟢 Minor: 1개

**Recommendation**: API Key 이슈 해결 후 재리뷰 요청
```

### Example 2: 보안 중심 리뷰

```
User: "review security for authentication PR"

Security Checklist 집중 적용:

🔒 보안
- [x] 비밀번호 해싱 (bcrypt)
- [x] JWT 토큰 httpOnly 쿠키
- [ ] CSRF 토큰 미구현 ← 🔴 Blocker
- [x] Rate Limiting (로그인 5회/분)
- [x] 입력 검증 (email, password 형식)
- [ ] 세션 타임아웃 설정 누락 ← 🟡 Major

**Recommendation**: CSRF 토큰 추가 필수
```

---

## Integration with Tools

### GitHub Actions
```yaml
# .github/workflows/pr-review.yml
name: Automated Code Review
on: [pull_request]
jobs:
  review:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - run: npm install
      - run: npm run lint
      - run: npm test -- --coverage
      - run: npm run build
```

### SonarQube
```bash
# 코드 품질 수치화
sonar-scanner \
  -Dsonar.projectKey=genkub \
  -Dsonar.sources=src \
  -Dsonar.tests=__tests__
```

### Danger.js
```javascript
// dangerfile.js
import { danger, warn, fail } from 'danger'

if (danger.github.pr.additions > 500) {
  warn('PR이 너무 큽니다. 분할을 고려하세요.')
}

const hasTests = danger.git.created_files.some(f => f.includes('.test.'))
if (!hasTests) {
  fail('테스트 파일이 없습니다.')
}
```

---

## Anti-Patterns (하지 말 것)

❌ **체크리스트 건너뛰기**: "급하니까 나중에..." → 기술 부채 누적
❌ **형식적 체크**: 실제 확인 없이 체크만 → 무의미
❌ **도구 의존**: 자동화 도구만 믿고 사람 리뷰 생략 → 비즈니스 로직 검증 부족
❌ **과도한 완벽주의**: Minor 이슈로 PR 블록 → 생산성 저하
❌ **리뷰 없이 머지**: "내가 작성했으니 괜찮아" → 버그/보안 취약점 배포

---

## Best Practices

✅ **Blocker는 무조건 수정**: 보안, 테스트 실패는 머지 금지
✅ **Major는 협의**: 시간 제약 있으면 이슈로 남기고 다음 PR에서 처리
✅ **Minor는 제안**: 강제하지 않되, 개선 기회로 활용
✅ **자동화 우선**: CI에서 자동 체크 → 사람은 비즈니스 로직 집중
✅ **피드백은 구체적으로**: "이상해요" ❌ → "handleAction 함수가 3가지 책임을 가져서 분리 권장" ✅

---

## Checklist Template (Copy & Paste)

```markdown
## Code Review Checklist

### ✅ 기능
- [ ] 요구사항 충족
- [ ] 엣지 케이스 처리
- [ ] 에러 핸들링

### 🧪 테스트
- [ ] 단위 테스트 추가
- [ ] 통합 테스트 (필요 시)
- [ ] 모든 테스트 통과
- [ ] 커버리지 ≥80%

### 🔒 보안
- [ ] 입력 검증
- [ ] API Key 노출 없음
- [ ] XSS 방지
- [ ] 권한 체크

### ⚡ 성능
- [ ] N+1 쿼리 없음
- [ ] 메모리 누수 없음
- [ ] 리렌더링 최적화

### 🎨 코드 품질
- [ ] 린트 통과
- [ ] 타입 체크 통과
- [ ] 중복 코드 없음
- [ ] 명확한 네이밍

### 📚 문서화
- [ ] 복잡한 로직 주석
- [ ] README 업데이트 (필요 시)
```

---

**Skill Version**: 1.0.0
**Last Updated**: 2025-12-30
**Next Review**: PR 리뷰 워크플로우 개선 후
