# HumanIdentityRegistry - Blockchain Module

> **Human Identity Protocol (HIP) Phase 1.5** - 블록체인 기반 신원 등록 시스템

## 📋 개요

이 모듈은 LobAI의 HIP (Human Identity Protocol) 시스템을 위한 **블록체인 기반 신원 등록 및 검증 시스템**입니다.

**핵심 차별점**:
- ❌ 경쟁사: "당신은 진짜 인간입니까?" (신원 확인)
- ✅ **HIP**: "GPT-4는 당신을 어떻게 인식합니까?" (AI 평가)

## 🏗️ 프로젝트 구조

```
blockchain/
├── contracts/                    # Smart Contracts
│   └── HumanIdentityRegistry.sol # HIP 신원 등록 컨트랙트
├── scripts/                      # 배포 스크립트
│   └── deploy.js                 # 배포 자동화
├── test/                         # 테스트
│   └── HumanIdentityRegistry.test.js
├── hardhat.config.js             # Hardhat 설정
├── package.json
└── README.md
```

## 🚀 빠른 시작

### 1. 환경 설정

```bash
# 1. 의존성 설치
npm install

# 2. 환경 변수 설정
cp .env.example .env
# .env 파일 편집하여 실제 값 입력

# 3. 컴파일
npm run compile
```

### 2. 로컬 테스트

```bash
# 테스트 실행
npm test

# 테스트 결과 (예상):
# ✔ 17 passing (375ms)
```

### 3. 로컬 블록체인 노드 실행

```bash
# Terminal 1: 로컬 Hardhat 노드 실행
npm run node

# Terminal 2: 로컬에 배포
npm run deploy:localhost

# Terminal 3: 상호작용 테스트
npm run interact:localhost
```

**테스트 결과** (예상):
```
✅ User1 신원 등록 완료
✅ 신원 검증 완료
✅ 평판 레벨 업데이트: 1 → 4
✅ 상호작용 5회 추가
✅ IPFS 해시 업데이트 완료
✅ 모든 상호작용 테스트 완료!
```

### 4. Polygon Mumbai 테스트넷 배포

```bash
# Mumbai 테스트넷 배포
npm run deploy:mumbai

# 배포 완료 후 Contract Address를 .env에 저장
# CONTRACT_ADDRESS=0x...
```

### 5. Contract 검증 (Polygonscan)

```bash
npx hardhat verify --network mumbai <CONTRACT_ADDRESS>
```

## 📜 Smart Contract 기능

### HumanIdentityRegistry.sol

**주요 함수**:

| 함수 | 권한 | 설명 |
|------|------|------|
| `registerIdentity(hipId, ipfsHash)` | 누구나 | 새로운 HIP 신원 등록 |
| `updateIdentity(hipId, ipfsHash)` | 소유자 | IPFS 해시 업데이트 |
| `verifyIdentity(hipId, verified)` | 관리자 | 신원 검증 상태 변경 |
| `updateReputation(hipId, level)` | 관리자 | 평판 레벨 업데이트 (1-5) |
| `incrementInteractions(hipId)` | 관리자 | AI 상호작용 카운트 증가 |
| `getIdentity(hipId)` | 누구나 | 신원 정보 조회 |
| `getHipIdByAddress(address)` | 누구나 | 주소로 HIP ID 조회 |

**Events**:
- `IdentityRegistered`: 신원 등록 시
- `IdentityUpdated`: 데이터 업데이트 시
- `IdentityVerified`: 검증 상태 변경 시
- `ReputationUpdated`: 평판 레벨 변경 시

## 🔧 개발 스크립트

```bash
# 컴파일
npm run compile

# 테스트
npm test

# 로컬 노드 실행
npm run node

# 배포
npm run deploy:localhost   # 로컬
npm run deploy:mumbai      # Mumbai 테스트넷
npm run deploy:polygon     # Polygon Mainnet

# 상호작용 테스트
npm run interact:localhost # 로컬
npm run interact:mumbai    # Mumbai

# Contract 검증
npm run verify:mumbai <CONTRACT_ADDRESS>

# 캐시 정리
npm run clean
```

## 📊 테스트 커버리지

**17개 테스트 케이스 (100% 통과)**:

- ✅ 배포 테스트 (2)
- ✅ 신원 등록 (5)
- ✅ 신원 업데이트 (3)
- ✅ 신원 검증 (2)
- ✅ 평판 업데이트 (2)
- ✅ 상호작용 카운트 (1)
- ✅ 페이지네이션 (2)

## 🔐 보안 고려사항

1. **Private Key 관리**
   - `.env` 파일은 절대 Git에 커밋하지 않음
   - `.gitignore`에 `.env` 추가됨

2. **Access Control**
   - `onlyOwner`: 관리자만 접근 (검증, 평판 업데이트)
   - `onlyIdentityOwner`: 신원 소유자만 접근 (데이터 업데이트)

3. **입력 검증**
   - HIP ID 중복 방지
   - 주소당 하나의 신원만 등록 가능
   - 평판 레벨 범위 제한 (1-5)

## 🌐 네트워크 설정

### 지원 네트워크

| 네트워크 | Chain ID | RPC URL |
|----------|----------|---------|
| Hardhat (로컬) | 1337 | http://127.0.0.1:8545 |
| Polygon Mumbai | 80001 | https://rpc-mumbai.maticvigil.com |
| Polygon Mainnet | 137 | https://polygon-rpc.com |

## 📈 다음 단계 (Phase 1.5)

### Month 1 (현재)
- [x] Hardhat 프로젝트 초기화
- [x] Smart Contract 작성
- [x] 테스트 작성 (17개, 100% 통과)
- [x] 로컬 테스트 통과
- [x] 로컬 배포 및 상호작용 테스트
- [x] 배포 가이드 작성 (DEPLOYMENT_GUIDE.md)
- [ ] Mumbai 테스트넷 배포
- [ ] Contract 검증

### Month 2
- [ ] IPFS 통합 (Pinata/Infura)
- [ ] Event-Driven Architecture (Spring Events)
- [ ] Web3j 통합 (Spring Boot)

### Month 3
- [ ] Public/Private Key 관리
- [ ] Gas Fee 최적화
- [ ] 블록체인 모니터링

## 🤝 기여

세션3 (블록체인 인프라) 전담

## 📄 라이선스

MIT License

---

**마지막 업데이트**: 2026-02-08
**Phase**: 1.5 (Blockchain Integration)
**진행률**: 30%
