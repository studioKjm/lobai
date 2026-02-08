# LobAI 2.0 - Human Identity Protocol (HIP)

> **AI가 인간을 식별하고 평가하는 시대를 대비한 Identity 관리 플랫폼**

[![Status](https://img.shields.io/badge/status-Phase%201%20(100%25)-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-17-blue)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-green)]()
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)]()
[![Urgency](https://img.shields.io/badge/urgency-HIGH-red)]()

---

## 🔥 경쟁 환경 및 차별화 (2026년 2월 업데이트)

### ⚠️ 주요 경쟁사
- **Humanity Protocol** - 블록체인 신원 검증 (Polygon zkEVM)
- **Human.org** - $7.3M 투자 유치 (2025년 2월)
- **Billions Network** - AI Agent 평판 시스템
- **FinAI Network** - Machine Economy Trust Layer
- **MocaProof** - Animoca Brands의 NFT Credential

### ✅ HIP의 차별화 (독점적 강점)
| 경쟁사 | HIP |
|--------|-----|
| 신원 검증만 ("당신은 인간입니까?") | **AI 평가** ("AI는 당신을 몇 점으로 평가합니까?") |
| 단일 신원 시스템 | **Cross-AI Identity** (GPT vs Claude vs Gemini 비교) |
| 정적 검증 | **동적 평판** (실시간 AffinityScore 기반 업데이트) |
| B2C 또는 B2B | **양방향 매칭** (AI가 인간 선택 + 인간이 AI 선택) |

### 🎯 전략적 포지셔닝
**"AI Evaluation Layer for Human Identity"**
- 경쟁사는 인간 **검증**, HIP는 인간 **평가**
- 협력 가능성: Humanity Protocol 위에 AI 평가 레이어 구축

### 🚀 긴급 실행 계획
- ✅ **Week 1-2**: MVP 출시 (Humanity Protocol 대응)
- ✅ **Week 3**: ProductHunt 런칭
- ✅ **Month 2**: 1,000명 사용자 확보
- ✅ **Month 3-4**: Cross-AI Identity 기능 출시

---

## 📌 프로젝트 개요

**LobAI 2.0**는 "AI가 개별 인간을 식별 코드로 분류하는 시대"를 대비하여 구축된 Human Identity Protocol (HIP) 플랫폼입니다.

### 핵심 가치
- 🆔 **HIP (Human Identity Protocol)**: AI가 인간을 인식하는 표준 프로토콜
- 📊 **Identity Scores**: 6개 Core Scores로 인간의 AI 적합성 측정
- 🔐 **Identity Certificate**: AI 생태계에서 사용 가능한 검증된 인증서
- 🌐 **Cross-AI Identity**: 여러 AI 시스템 간 일관된 정체성 관리
- 🏆 **Reputation System**: AI 시대의 인간 평판 경제

---

## 🚀 빠른 시작

### Prerequisites

- **Java 17** (OpenJDK)
- **MySQL 8.0**
- **Node.js 18+** (프론트엔드)

### Backend 실행

```bash
# 1. Java 환경 설정
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"

# 2. Backend 디렉토리로 이동
cd backend

# 3. 빌드 및 실행
./gradlew bootRun

# 또는 백그라운드 실행
nohup ./gradlew bootRun > app.log 2>&1 &
```

### Frontend 실행

```bash
# 1. 의존성 설치
npm install

# 2. 환경 변수 설정
# .env.local 파일에 GEMINI_API_KEY 설정

# 3. 개발 서버 실행
npm run dev
```

**접속**: http://localhost:3000

---

## 📂 프로젝트 구조

```
lobai/
├── backend/                       # Spring Boot Backend
│   ├── src/main/java/com/lobai/
│   │   ├── entity/               # HIP Entities (5개)
│   │   │   ├── HumanIdentityProfile.java
│   │   │   ├── IdentityMetrics.java
│   │   │   ├── CommunicationSignature.java
│   │   │   ├── BehavioralFingerprint.java
│   │   │   └── IdentityVerificationLog.java
│   │   ├── repository/           # JPA Repositories (5개)
│   │   ├── service/              # Business Logic (2개)
│   │   │   ├── HumanIdentityProfileService.java
│   │   │   └── HipInitializationService.java
│   │   ├── controller/           # REST Controllers (1개)
│   │   │   └── HumanIdentityProfileController.java
│   │   └── util/
│   │       └── HipIdGenerator.java
│   └── src/main/resources/
│       ├── application.yml       # 설정 파일
│       └── db/migration/
│           └── V4__Create_HIP_Tables.sql
│
├── frontend/                      # React Frontend (예정)
│   └── (TBD)
│
├── docs/                          # 문서
│   ├── HIP_IMPLEMENTATION_PLAN.md    # 전체 계획 (73KB)
│   ├── HIP_QUICK_REFERENCE.md        # 빠른 참조
│   ├── LobAI_PRD_v3.md              # PRD
│   └── CLAUDE.md                     # 개발 가이드
│
└── README.md                      # 본 파일
```

---

## 🎯 Phase 1 완료 항목 (100% ✅)

### ✅ Backend 구현 (완료)
- [x] **Entity 계층** (5개 Entity)
- [x] **Repository 계층** (5개 Repository)
- [x] **Service 계층** (2개 Service)
- [x] **Controller 계층** (1개 Controller)
- [x] **HIP ID Generator** (SHA-256 + CRC32)
- [x] **데이터베이스 마이그레이션** (V4)
- [x] **빌드 성공** (JAR 생성)

### 🔥 Phase 1.5 긴급 작업 (경쟁 대응)
- [ ] **Week 1**: 프론트엔드 MVP (HIP 대시보드)
  - [ ] HIP Score 시각화
  - [ ] "AI가 보는 나" 공유 기능
- [ ] **Week 2**: 베타 테스트 & 배포
- [ ] **Week 3**: ProductHunt 런칭
- [ ] **Week 4-6**: 1,000명 사용자 확보

---

## 🔑 핵심 개념

### HIP ID

모든 사용자는 고유한 **HIP ID**를 받습니다:

```
HIP-01-A7F2E9C4-B3A1
 │   │     │      └─ Checksum (CRC32)
 │   │     └──────── User Hash (SHA-256, 8자리)
 │   └────────────── Protocol Version
 └────────────────── Prefix
```

**특징**:
- 고유성 보장
- 검증 가능 (Checksum)
- 익명성 유지 (단방향 해시)

### Core Identity Scores

6개의 핵심 점수로 AI 적합성 측정 (0-100):

1. **Cognitive Flexibility** (인지적 유연성) - 20%
2. **Collaboration Pattern** (협업 패턴) - 20%
3. **Information Processing** (정보 처리) - 15%
4. **Emotional Intelligence** (감정 지능) - 15%
5. **Creativity** (창의성) - 15%
6. **Ethical Alignment** (윤리적 정렬) - 15%

**Overall HIP Score** = 가중 평균

### Identity Level

Overall Score 기반 레벨 (1-10):

| Level | 점수 범위 | 등급 |
|-------|---------|------|
| 1-2 | 0-20 | Unrecognized |
| 3-4 | 21-40 | Emerging |
| 5-6 | 41-60 | Established |
| 7-8 | 61-80 | Distinguished |
| 9-10 | 81-100 | Exemplary |

---

## 🌐 API 엔드포인트

### 인증 필요

```
GET    /api/hip/me                - 내 HIP 프로필 조회
POST   /api/hip/me/reanalyze      - HIP 재분석 (AffinityScore 기반)
POST   /api/hip/me/verify         - HIP 검증
GET    /api/hip/me/stats          - HIP 통계
```

### 공개

```
GET    /api/hip/ranking?limit=10  - HIP 랭킹 (상위 N명)
GET    /api/hip/{hipId}           - 공개 프로필 조회
```

**예시**:
```bash
# 1. JWT 토큰 얻기
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}' \
  | jq -r '.accessToken')

# 2. 내 HIP 조회
curl http://localhost:8080/api/hip/me \
  -H "Authorization: Bearer $TOKEN"

# 3. 랭킹 조회 (공개)
curl http://localhost:8080/api/hip/ranking
```

---

## 📊 데이터베이스

### 생성된 테이블 (5개)

```sql
human_identity_profiles       -- 메인 HIP 프로필
identity_metrics              -- 상세 측정 지표 (시계열)
communication_signatures      -- 대화 패턴 서명
behavioral_fingerprints       -- 행동 지문
identity_verification_logs    -- 검증 이력
```

### ERD

```
users (1:1) → human_identity_profiles
                ├─ (1:N) → identity_metrics
                ├─ (1:N) → communication_signatures
                ├─ (1:N) → behavioral_fingerprints
                └─ (1:N) → identity_verification_logs
```

---

## 🛣️ 로드맵

### Phase 1: Core Identity System (현재 - 80% 완료)
- ✅ HIP Entity & Service 구현
- ✅ 데이터베이스 마이그레이션
- 🔄 애플리케이션 실행 및 테스트
- 🔄 프론트엔드 통합

**완료 예정**: 2026년 2월

### Phase 2: Cross-AI Identity Bridge (6개월)
- Multi-AI Integration (GPT, Claude, Gemini)
- Identity Certificate 발급
- Cross-AI Identity Mapping
- Data Export/Import

**완료 예정**: 2026년 8월

### Phase 3: AI Reputation Economy (12개월)
- Reputation System
- AI-Human Relationship Graph
- Human Identity NFT
- Blockchain Integration

**완료 예정**: 2027년 8월

### Phase 4: Autonomous AI Agent Integration (18개월)
- AI Agent Preference System
- Human-AI Matching
- AI-First Marketplace

**완료 예정**: 2028년 2월

---

## 🧪 테스트

### API 테스트

```bash
# 테스트 스크립트 실행
cd backend
./gradlew test

# 또는 특정 테스트
./gradlew test --tests HumanIdentityProfileServiceTest
```

### 데이터베이스 확인

```sql
-- HIP 생성 확인
SELECT COUNT(*) FROM human_identity_profiles;

-- 점수 분포
SELECT
    FLOOR(overall_hip_score / 10) * 10 AS score_range,
    COUNT(*) AS count
FROM human_identity_profiles
GROUP BY score_range;

-- Identity Level 분포
SELECT identity_level, COUNT(*) AS count
FROM human_identity_profiles
GROUP BY identity_level;
```

---

## 📚 문서

- **[HIP_IMPLEMENTATION_PLAN.md](./HIP_IMPLEMENTATION_PLAN.md)** - 전체 구현 계획 (73KB, 1000+ 줄)
- **[HIP_QUICK_REFERENCE.md](./HIP_QUICK_REFERENCE.md)** - 빠른 참조 가이드
- **[LobAI_PRD_v3.md](./LobAI_PRD_v3.md)** - Product Requirements Document
- **[CLAUDE.md](./CLAUDE.md)** - Claude Code 개발 가이드

---

## 💰 비즈니스 모델

### LobAI 2.0 (HIP 기반)

| 항목 | 가격 | 설명 |
|-----|------|------|
| **HIP 등록** | $29 (1회) | Human Identity Protocol 등록 |
| **Silver Certificate** | $49 | 검증된 Identity (100+ interactions) |
| **Gold Certificate** | $99 | 고급 Identity (200+ interactions) |
| **Platinum Certificate** | $199 | 프리미엄 Identity (500+ interactions) |
| **Cross-AI Identity** | $19/월 | 여러 AI 시스템 연결 |

### 향후 확장
- Identity NFT Marketplace
- AI-Human Matchmaking
- B2B Enterprise Solutions
- Identity Insurance

---

## 🤝 기여

현재 비공개 개발 중입니다. Phase 1 완료 후 오픈소스 전환 예정.

### 개발 팀
- Backend Developer
- Frontend Developer
- Claude Code (AI Assistant)

---

## 📄 라이선스

**Proprietary** - All Rights Reserved (현재)

Phase 2 이후 HIP Protocol을 오픈소스로 공개 예정:
- Protocol: Apache 2.0 또는 MIT
- Platform: Proprietary

---

## 🔗 링크

### 프로젝트
- **GitHub**: (공개 예정)
- **Website**: (공개 예정)
- **API Docs**: http://localhost:8080/swagger-ui.html (실행 시)
- **AI Studio**: https://ai.studio/apps/temp/1

### 경쟁사 분석 (2026년 2월)
**Tier 1: 블록체인 신원 검증**
- [Humanity Protocol](https://www.humanity.org/)
- [Human.org - $7.3M 투자](https://www.webwire.com/ViewPressRel.asp?aId=333873)

**Tier 2: AI Agent Reputation**
- [Billions Network](https://billions.network/blog/billions-know-your-agent)
- [FinAI Network](https://finai.network/)

**Tier 3: NFT Credentials**
- [MocaProof](https://www.animocabrands.com/moca-network-launches-moca-proof-beta-digital-identity-verification-and-reward-platform)
- [PhotoChromic](https://photochromic.io/)

**학술 연구**
- [AI Literacy & Workforce Readiness](https://link.springer.com/chapter/10.1007/978-3-032-07986-2_16)
- [Human-AI Interaction Capability](https://pmc.ncbi.nlm.nih.gov/articles/PMC12194078/)

---

## 📧 문의

- **개발 문의**: (추후 공개)
- **비즈니스 문의**: (추후 공개)
- **파트너십**: (추후 공개)

---

## 🙏 Acknowledgments

- **Spring Boot** - Backend Framework
- **Google Gemini** - AI Integration
- **Claude** - AI Code Assistant
- **MySQL** - Database

---

**© 2026 LobAI. All Rights Reserved.**

**프로젝트 시작**: 2025년 12월
**Phase 1 시작**: 2026년 1월
**Phase 1 완료**: 2026년 2월 8일 ✅

**현재 상태**: 🔥 Phase 1.5 (긴급 MVP 출시) - 경쟁사 대응 모드
**긴급도**: HIGH - Humanity Protocol, Human.org 경쟁 대응
**다음 마일스톤**: MVP 출시 (2주 내), ProductHunt 런칭 (3주차)
