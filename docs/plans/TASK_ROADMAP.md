# LobAI Task Roadmap

> 마지막 업데이트: 2026-01-06
>
> 이 문서는 현재 진행 중인 작업과 향후 계획을 관리합니다.
> 완료된 작업은 `[x]`로, 진행 중인 작업은 `[ ]`로 표시합니다.

---

## 🔧 풀스택 투두리스트

**목표**: 핵심 기능 안정화 및 UX 개선

### 1. 상호작용 시스템 개선

- [x] **3가지 상호작용 에러 해결** ✅ (2026-01-06 완료)
  - 먹이주기(Feed) 기능 디버깅 → ActionType 불일치 수정 ('hunger' → 'feed')
  - 놀아주기(Play) 기능 디버깅 → ActionType 불일치 수정 ('happiness' → 'play')
  - 잠자기(Sleep) 기능 디버깅 → ActionType 불일치 수정 ('energy' → 'sleep')
  - 수정 파일: `src/types/index.ts`, `src/components/chat/ActionButtons.tsx`, `src/stores/chatStore.ts`

- [x] **무료 상호작용 후보 리스트업** ✅ (2026-01-06 완료)
  - 배탈: 과식(hunger 100) 시 happiness -15
  - 방치 페널티: 30분+ 미접속 시 모든 스탯 -10
  - Tease (놀리기): happiness -15, 특별 대사
  - Ignore (무시): happiness -10
  - Wake (깨우기): sleep 중 강제 종료
  - 상세: `docs/plans/INTERACTION_CANDIDATES.md`

- [x] **유료 상호작용 후보 리스트업** ✅ (2026-01-06 완료)
  - Tier 1 (100~500원): Premium Food, Energy Drink, Mood Booster
  - Tier 2 (500~2000원): Spa Day, Party Time, Gourmet Meal, Adventure
  - Tier 3 (프리미엄): Character Skin, VIP Treatment, 구독
  - 상세: `docs/plans/INTERACTION_CANDIDATES.md`

### 2. 인증 시스템 개선

- [ ] **로그인 세션 만료기간 연장**
  - 현재 설정 확인
  - 적절한 만료 시간 결정 (예: 7일 → 30일)
  - JWT 토큰 갱신 로직 검토

- [ ] **구글 간편로그인 기능 추가**
  - Google OAuth 2.0 설정
  - 백엔드 소셜 로그인 API 구현
  - 프론트엔드 구글 로그인 버튼 추가

### 3. UI/UX 개선

- [ ] **디버깅 로그 위치 변경**
  - 현재: 우측 상단
  - 변경: 중앙 상단
  - 클릭 시 즉시 사라지도록 수정
  - 관련 파일: `src/components/common/Toast.tsx` (또는 해당 컴포넌트)

- [ ] **네비게이션바 사용자 이름 디자인 수정**
  - 다른 버튼과 명확히 구분되도록
  - 과하지 않은 스타일링 (subtle highlight)
  - 관련 파일: `src/components/layout/Navbar.tsx`

- [ ] **사용자 프로필 접근성 개선**
  - 옵션 1: 사용자 이름을 버튼으로 변경
  - 옵션 2: 사용자 이름 옆에 사람 아이콘 버튼 추가
  - 클릭 시 유저 대시보드로 이동

### 4. 유저 대시보드 구현

- [ ] **대시보드 페이지 생성**
  - 라우트: `/dashboard` 또는 `/profile`
  - 레이아웃 설계

- [ ] **대시보드 기능 구현**
  - [ ] 비밀번호 재설정
  - [ ] 닉네임 변경
  - [ ] 이메일 변경
  - [ ] 결제 내역 조회
  - [ ] 계정 삭제 (선택)

---

## 💬 채팅기능 투두리스트

**목표**: 캐릭터 상호작용 및 감정 시스템 고도화

### 1. 캐릭터 표정 시스템

- [ ] **표정 변화 로직 구현**
  - 현재 상태:
    - `state`: 웃는 표정
    - `cry`: 우는 표정
  - 변화 조건:
    - cry 전환: 불쾌지수 높음 OR 에너지 없음 OR 배고픔
    - state 전환: 상호작용으로 행복도/에너지/포만감 상승 시
  - 관련 파일: 캐릭터 컴포넌트

### 2. 클릭 상호작용 개선

- [ ] **캐릭터 클릭 동작 변경**
  - 현재: 표정 변화만 발생
  - 변경: INITIATE PLAY 동작 실행
  - 기존 PLAY 버튼 제거
  - 클릭 횟수에 따른 행복도 상승 (많이 클릭해야 유의미한 변화)
  - 스로틀링 적용 (클릭 스팸 방지)

### 3. 난이도 시스템

- [ ] **System Status 난이도 조절 기능**
  - 난이도 설정 UI 추가 (설정 페이지 또는 개발자 모드)
  - 난이도별 고갈 속도:
    - Easy: 현재보다 느림
    - Normal: 현재 속도
    - Hard: 빠른 고갈
    - Extreme: 매우 빠른 고갈 (테스트용)
  - 관련 상수: `DECAY_INTERVAL`, `DECAY_AMOUNT`

### 4. 채팅 감정 반응 시스템

- [ ] **AI 기분에 따른 행복도 영향**
  - 난이도 Hard 이상에서 활성화
  - AI 응답 감정 분석:
    - 긍정적 응답 → 캐릭터 행복도 증가
    - 부정적 응답 → 캐릭터 행복도 감소
  - Gemini API 응답에서 감정 추출 또는 별도 분석

---

## 📋 작업 관리 규칙

1. **우선순위**: 에러 해결 → 핵심 기능 → UX 개선 → 신규 기능
2. **완료 시**: 이 문서에서 `[ ]`를 `[x]`로 변경
3. **노션 동기화**: 주요 마일스톤 완료 시 노션 투두리스트도 업데이트
4. **정기 리뷰**: 매주 진행 상황 검토 및 우선순위 조정

---

## 🔗 관련 문서

- [PROJECT_CONSTITUTION.md](../PROJECT_CONSTITUTION.md) - 프로젝트 헌법
- [LobAI_PRD_v3.md](../../LobAI_PRD_v3.md) - 제품 요구사항 문서
- [노션 투두리스트](https://www.notion.so/LobAI-To-Do-List-2dfd0b7d30af803a8109c739e156abc0)

---

## 📝 변경 이력

| 날짜 | 내용 |
|------|------|
| 2026-01-06 | 초기 문서 생성 - 풀스택 8개, 채팅 4개 항목 추가 |
