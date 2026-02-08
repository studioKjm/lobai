# Mumbai 테스트넷 배포 체크리스트

> **Phase 1.5 - Week 3**: Polygon Mumbai 실제 배포

---

## 📋 사전 준비 (필수)

### 1. MetaMask 지갑 설정

- [ ] **MetaMask 설치**
  - Chrome Extension: https://metamask.io/download/

- [ ] **Polygon Mumbai 네트워크 추가**
  ```
  Settings → Networks → Add Network 클릭

  Network Name: Polygon Mumbai
  RPC URL: https://rpc-mumbai.maticvigil.com
  Chain ID: 80001
  Currency Symbol: MATIC
  Block Explorer: https://mumbai.polygonscan.com/
  ```

- [ ] **지갑 주소 확인**
  ```
  MetaMask → 계정 클릭 → 주소 복사
  예: 0x1234...abcd
  ```

### 2. 테스트 MATIC 받기

- [ ] **Faucet에서 MATIC 받기** (옵션 1)
  - URL: https://faucet.polygon.technology/
  - 지갑 주소 입력 → Mumbai 선택 → Submit
  - 대기 시간: 1-5분
  - 받는 양: 0.5 MATIC

- [ ] **Faucet 대체 사이트** (옵션 2, 옵션 1 실패 시)
  - URL: https://mumbaifaucet.com/
  - Alchemy 계정 필요 (무료)

- [ ] **잔액 확인**
  ```bash
  # MetaMask에서 확인: Mumbai 네트워크 선택 → 잔액 확인
  # 또는 PolygonScan에서:
  https://mumbai.polygonscan.com/address/YOUR_ADDRESS

  필요량: 최소 0.3 MATIC (배포 + 테스트)
  ```

### 3. Private Key 준비

⚠️ **주의**: Private Key는 절대 공유하지 마세요!

- [ ] **Private Key 내보내기**
  ```
  MetaMask → 계정 세부정보 (3점 메뉴)
  → "프라이빗 키 내보내기" 클릭
  → 비밀번호 입력
  → Private Key 복사 (0x로 시작)
  ```

- [ ] **.env 파일 생성**
  ```bash
  cd blockchain
  cp .env.example .env
  ```

- [ ] **.env 파일 편집**
  ```bash
  # .env 파일 열기
  nano .env   # 또는 code .env

  # 다음 내용 입력:
  POLYGON_RPC_URL=https://rpc-mumbai.maticvigil.com
  PRIVATE_KEY=여기에_private_key_붙여넣기  # 0x 제거
  POLYGONSCAN_API_KEY=  # 검증용 (선택사항)
  ```

- [ ] **보안 확인**
  ```bash
  # .env가 gitignore에 포함되어 있는지 확인
  cat .gitignore | grep ".env"
  # 출력: .env (있어야 함)
  ```

### 4. Polygonscan API Key (선택사항, Contract 검증용)

- [ ] **Polygonscan 계정 생성**
  - URL: https://polygonscan.com/register

- [ ] **API Key 발급**
  - 로그인 → My Account → API Keys
  - Create New API Key
  - Key 복사하여 .env에 추가

---

## 🚀 배포 실행

### Step 1: 환경 확인

```bash
cd blockchain

# Node.js 버전 확인 (18+ 권장)
node --version

# 의존성 설치 확인
npm list hardhat
# 출력: hardhat@2.22.0 (또는 유사)

# .env 파일 확인
cat .env | grep PRIVATE_KEY
# PRIVATE_KEY가 설정되어 있어야 함
```

### Step 2: 배포 전 테스트

```bash
# 로컬 테스트 재실행 (선택사항)
npm test

# 컴파일 확인
npm run compile
```

### Step 3: Mumbai 배포

```bash
npm run deploy:mumbai
```

**예상 출력**:
```
🚀 HumanIdentityRegistry 배포 시작...

📝 배포 계정: 0xYourAddress...
💰 계정 잔액: 0.5 MATIC

📦 Contract 배포 중...
✅ HumanIdentityRegistry 배포 완료!
📍 Contract Address: 0xNewContractAddress...
🔗 Network: mumbai
⛽ Gas Used: ~1,500,000

📋 Etherscan 검증 명령어:
npx hardhat verify --network mumbai 0xNewContractAddress...
```

- [ ] **Contract Address 기록**
  ```
  Contract Address: 0x_________________
  TX Hash: 0x_________________
  배포 시간: ____________________
  ```

- [ ] **.env 파일 업데이트**
  ```bash
  echo "CONTRACT_ADDRESS=0xYourContractAddress" >> .env
  ```

### Step 4: Contract 검증

```bash
# Contract 주소를 실제 주소로 교체
npx hardhat verify --network mumbai 0xYourContractAddress
```

**성공 시 출력**:
```
Successfully verified contract HumanIdentityRegistry on Polygonscan.
https://mumbai.polygonscan.com/address/0xYourAddress#code
```

- [ ] **Polygonscan에서 확인**
  - URL: https://mumbai.polygonscan.com/address/CONTRACT_ADDRESS
  - "Contract" 탭 → 소스 코드 확인
  - "Read Contract" / "Write Contract" 탭 사용 가능

---

## 🧪 배포 후 테스트

### Step 5: 상호작용 테스트

```bash
npm run interact:mumbai
```

**예상 결과**:
```
✅ User1 신원 등록 완료
✅ 신원 검증 완료
✅ 평판 레벨 업데이트: 1 → 4
✅ 상호작용 5회 추가
✅ IPFS 해시 업데이트 완료
```

### Step 6: Polygonscan에서 수동 테스트

1. **Polygonscan 방문**
   ```
   https://mumbai.polygonscan.com/address/CONTRACT_ADDRESS#writeContract
   ```

2. **Connect Wallet** 클릭 → MetaMask 연결

3. **registerIdentity 함수 테스트**
   ```
   hipId: HIP-02-TEST-12345678
   ipfsHash: QmTestHash123456789
   → Write 클릭 → MetaMask에서 확인
   ```

4. **getIdentity 함수로 조회**
   ```
   Read Contract 탭 이동
   → getIdentity 입력: HIP-02-TEST-12345678
   → Query
   ```

---

## 📊 배포 완료 체크리스트

- [ ] Contract 배포 성공
- [ ] Contract Address 기록
- [ ] Polygonscan 검증 완료
- [ ] 상호작용 테스트 통과
- [ ] Mumbai Transaction 확인
- [ ] .env 파일 업데이트
- [ ] SESSION_LOG.md 업데이트

---

## 🐛 문제 해결

### 배포 실패: "insufficient funds"

**원인**: MATIC 부족

**해결**:
```bash
# Faucet에서 더 받기
https://faucet.polygon.technology/

# 잔액 확인
https://mumbai.polygonscan.com/address/YOUR_ADDRESS
```

### 배포 실패: "nonce too low"

**원인**: Hardhat 캐시 문제

**해결**:
```bash
npm run clean
npm run compile
npm run deploy:mumbai
```

### 검증 실패: "Already Verified"

**원인**: 이미 검증됨

**확인**:
```bash
# Polygonscan에서 확인
https://mumbai.polygonscan.com/address/CONTRACT_ADDRESS#code
```

### 배포 실패: "Invalid nonce"

**원인**: Private Key 잘못 입력

**해결**:
1. .env 파일 확인
2. Private Key에 0x가 포함되어 있으면 제거
3. 올바른 Private Key 재입력

---

## 📝 배포 완료 후 작업

### 문서 업데이트

- [ ] **SESSION_LOG.md 업데이트**
  ```markdown
  ✅ Mumbai 테스트넷 배포 완료
  - Contract Address: 0x...
  - TX Hash: 0x...
  - Verified: ✅
  ```

- [ ] **DEPLOYMENT_GUIDE.md 업데이트**
  ```markdown
  ### Mumbai Testnet
  - 배포 일시: 2026-02-XX
  - Contract Address: 0x...
  - Deployer: 0x...
  - TX Hash: 0x...
  - Verified: ✅
  ```

### 커밋 & 푸시

```bash
git add blockchain/.env.example SESSION_LOG.md
git commit -m "feat(blockchain): Mumbai 테스트넷 배포 완료

- Contract Address: 0x...
- Polygonscan 검증 완료
- 상호작용 테스트 통과

Phase 1.5 진행률: 70%

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

git push origin master
```

---

## 🎯 다음 단계 (Week 4)

- [ ] IPFS 통합 (Pinata/Infura)
- [ ] Web3j Spring Boot 연동
- [ ] Event Listener 구현
- [ ] 자동 블록체인 동기화

---

**작성일**: 2026-02-08
**작성자**: 세션3 (블록체인 인프라)
**Phase 1.5 진행률**: 50% → 목표 70%
