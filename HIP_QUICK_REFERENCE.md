# HIP 빠른 참조 가이드

> **Human Identity Protocol (HIP)** 구현 핵심 요약

---

## ✅ 완료된 작업 (Phase 1 - 80%)

### 파일 생성 완료
```
✅ backend/src/main/java/com/lobai/
   ├── entity/
   │   ├── HumanIdentityProfile.java
   │   ├── IdentityMetrics.java
   │   ├── CommunicationSignature.java
   │   ├── BehavioralFingerprint.java
   │   └── IdentityVerificationLog.java
   ├── repository/ (5개)
   ├── service/ (2개)
   ├── controller/ (1개)
   └── util/HipIdGenerator.java

✅ backend/src/main/resources/db/migration/
   └── V4__Create_HIP_Tables.sql (실행 완료)

✅ build.gradle (의존성 추가)
✅ 빌드 성공 (lobai-backend-0.0.1-SNAPSHOT.jar)
```

---

## 🚀 즉시 실행 가능

### 1. 애플리케이션 실행
```bash
cd /Users/jimin/lobai/lobai/backend
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
./gradlew bootRun
```

### 2. 예상 결과
```
=== HIP Initialization Service Started ===
Created HIP for user: 1
Created HIP for user: 2
Created HIP for user: 3
Created HIP for user: 4
HIP initialization completed: 4 created
Reanalysis completed: 4 profiles analyzed
=== HIP Initialization Service Completed ===
```

### 3. API 테스트
```bash
# 1. JWT 토큰 얻기 (기존 로그인 사용)
TOKEN="your-token"

# 2. 내 HIP 조회
curl http://localhost:8080/api/hip/me \
  -H "Authorization: Bearer $TOKEN"

# 3. HIP 랭킹 (공개)
curl http://localhost:8080/api/hip/ranking
```

---

## 📊 주요 개념

### HIP ID v1.0 (현재)
```
포맷: HIP-01-A7F2E9C4-B3A1
       │   │     │      └─ Checksum
       │   │     └──────── User Hash (SHA-256)
       │   │────────────── Version (01 = Basic)
       └────────────────── Prefix
```

### 🔐 HIP ID v2.0 (계획) - 블록체인 기반 ⭐
```
포맷: HIP-02-B4F8C3A9-7E2D-0x4f3a8b2c
       │   │     │      │    └─ Blockchain TX Hash
       │   │     │      └────── Digital Signature
       │   │     └───────────── User Hash
       │   └─────────────────── Version (02 = Blockchain)
       └─────────────────────── Prefix

특징:
✅ 블록체인 불변성 (변조 불가)
✅ 동적 연동 (평판 업데이트 시 자동 반영)
✅ IPFS 분산 저장
✅ Public/Private Key 암호화
✅ Smart Contract 검증
```

### Core Scores (6개)
1. **Cognitive Flexibility** - 인지적 유연성
2. **Collaboration Pattern** - 협업 패턴
3. **Information Processing** - 정보 처리
4. **Emotional Intelligence** - 감정 지능
5. **Creativity** - 창의성
6. **Ethical Alignment** - 윤리적 정렬

### Identity Level (1-10)
- **1-2**: Unrecognized
- **3-4**: Emerging
- **5-6**: Established
- **7-8**: Distinguished
- **9-10**: Exemplary

---

## 📋 데이터베이스

### 생성된 테이블 (5개)
```sql
human_identity_profiles       -- 메인 HIP 프로필
identity_metrics              -- 상세 지표
communication_signatures      -- 대화 패턴
behavioral_fingerprints       -- 행동 지문
identity_verification_logs    -- 검증 이력
```

### 확인 쿼리
```sql
-- HIP 수 확인
SELECT COUNT(*) FROM human_identity_profiles;

-- HIP 조회
SELECT hip_id, user_id, overall_hip_score, identity_level
FROM human_identity_profiles;

-- 점수 분포
SELECT
    FLOOR(overall_hip_score / 10) * 10 AS score_range,
    COUNT(*) AS count
FROM human_identity_profiles
GROUP BY score_range;
```

---

## 🔄 API 엔드포인트

### 인증 필요
```
GET    /api/hip/me                - 내 HIP 조회
POST   /api/hip/me/reanalyze      - HIP 재분석
POST   /api/hip/me/verify         - HIP 검증
GET    /api/hip/me/stats          - HIP 통계
```

### 공개
```
GET    /api/hip/ranking           - HIP 랭킹
GET    /api/hip/{hipId}           - 공개 프로필 조회
```

### 🔐 블록체인 (Phase 1.5 - 예정)
```
GET    /api/hip/me/blockchain/status        - 블록체인 동기화 상태
GET    /api/hip/me/blockchain/transactions  - 트랜잭션 이력
POST   /api/hip/me/blockchain/sync          - 수동 동기화
GET    /api/hip/{hipId}/blockchain/verify   - 블록체인 검증 (공개)
```

---

## 🔐 블록체인 아키텍처 (Phase 1.5)

### 동적 연동 흐름
```
1. 평판 업데이트 (AffinityScore, ResilienceReport 등)
   ↓
2. Spring Event 발행 (자동)
   ↓
3. HIP 재계산 (Service Layer)
   ↓
4. Smart Contract 호출 (Blockchain)
   ↓
5. IPFS 데이터 저장 (분산 스토리지)
   ↓
6. Transaction Hash 기록 (DB)
   ↓
7. 완료 (실시간 반영)
```

### 기술 스택
- **Blockchain**: Polygon (L2) - 낮은 Gas Fee
- **Smart Contract**: Solidity 0.8.x
- **Java 통합**: Web3j 4.10.x
- **분산 저장**: IPFS (Pinata/Infura)
- **암호화**: AES-256-GCM, ECDSA
- **Event**: Spring ApplicationEventPublisher

### 보안 계층
1. **Public/Private Key**: ECDSA 기반
2. **암호화 저장**: AES-256-GCM
3. **블록체인 불변성**: 변조 불가
4. **Zero-Knowledge Proofs**: 선택적 공개 (향후)
5. **Multi-Signature**: 중요 업데이트 시

---

## 📅 로드맵

### Phase 1 (완료 - 100% ✅)
- ✅ Entity, Repository, Service, Controller
- ✅ 데이터베이스 마이그레이션
- ✅ 빌드 성공
- ✅ 애플리케이션 실행 및 테스트
- ✅ 모든 API 정상 작동
- ⏳ 프론트엔드 통합

### Phase 1.5 (3개월) - 🔐 블록체인 강화 ⭐ NEW
- Blockchain Integration (Polygon)
- Smart Contract 배포
- IPFS 분산 저장
- 동적 연동 (Event-driven)
- Public/Private Key 관리
- 보안 암호화

### Phase 2 (6개월)
- Multi-AI Integration (GPT, Claude, Gemini)
- Identity Certificate 발급
- Cross-AI Identity Mapping

### Phase 3 (12개월)
- Reputation System
- AI-Human Relationship Graph
- NFT Integration

### Phase 4 (18개월)
- AI Agent Preference System
- Human-AI Matching
- AI-First Marketplace

---

## 🎯 다음 단계

### ✅ 완료 (Phase 1)
1. ✅ 애플리케이션 실행 및 HIP 초기화
2. ✅ API 테스트 (모든 엔드포인트 정상 작동)
3. ✅ 데이터 검증 (5명 사용자 HIP 생성)

### 🔐 Phase 1.5: 블록체인 강화 (다음 3개월)

**Week 1-4: 인프라 구축**
- [ ] Polygon Mumbai 테스트넷 설정
- [ ] Smart Contract 개발 (HumanIdentityRegistry.sol)
- [ ] Web3j Java 통합
- [ ] IPFS 연동 (Pinata/Infura)

**Week 5-8: 동적 연동**
- [ ] Event-driven Architecture 구현
- [ ] AffinityScore → HIP 자동 업데이트
- [ ] ResilienceReport → HIP 자동 업데이트
- [ ] 블록체인 Transaction 기록

**Week 9-12: 보안 및 배포**
- [ ] Public/Private Key 관리
- [ ] 암호화 시스템 (AES-256-GCM)
- [ ] Transaction Monitoring
- [ ] Mainnet 배포

### 이번 주 (준비 작업)
- [ ] 프론트엔드 HIP 대시보드 설계
- [ ] API 문서화 (Swagger)
- [ ] 블록체인 개발 환경 설정
  - Hardhat 설치
  - Polygon Mumbai 계정 생성
  - 테스트 MATIC 받기

---

## 🛠️ 문제 해결

### 빌드 실패 시
```bash
# Clean build
./gradlew clean build -x test

# 의존성 확인
./gradlew dependencies
```

### DB 연결 실패 시
```bash
# MySQL 상태 확인
ps aux | grep mysql

# 재시작
brew services restart mysql@8.0

# 접속 테스트
mysql -u lobai_user -p lobai_db
```

### Java 버전 문제 시
```bash
# Java 17 설치 확인
java -version

# PATH 설정
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
export PATH="$JAVA_HOME/bin:$PATH"
```

---

## 📚 참고 문서

- **HIP_IMPLEMENTATION_PLAN.md** - 전체 계획 (73KB, 1000+ 줄)
- **LobAI_PRD_v3.md** - Product Requirements
- **CLAUDE.md** - 개발 가이드

---

## 💡 핵심 파일 경로

### Entity
```
backend/src/main/java/com/lobai/entity/
- HumanIdentityProfile.java       (218줄)
- IdentityMetrics.java             (172줄)
- CommunicationSignature.java      (71줄)
- BehavioralFingerprint.java       (73줄)
- IdentityVerificationLog.java     (89줄)
```

### Service
```
backend/src/main/java/com/lobai/service/
- HumanIdentityProfileService.java (340줄) - 핵심 비즈니스 로직
- HipInitializationService.java    (120줄) - 자동 초기화
```

### Utility
```
backend/src/main/java/com/lobai/util/
- HipIdGenerator.java              (125줄) - HIP ID 생성/검증
```

### Migration
```
backend/src/main/resources/db/migration/
- V4__Create_HIP_Tables.sql        (200줄) - DB 스키마
```

---

**빠른 질문?**
- 전체 계획: `HIP_IMPLEMENTATION_PLAN.md` 참조
- 즉시 실행: 위 "다음 단계" 섹션 참조
- 문제 발생: "문제 해결" 섹션 참조

**문서 버전**: 1.0 | **최종 수정**: 2026-02-08
