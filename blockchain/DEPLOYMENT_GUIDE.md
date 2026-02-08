# HumanIdentityRegistry 배포 가이드

> Polygon Mumbai 테스트넷 및 Mainnet 배포 완전 가이드

---

## 📋 목차

1. [로컬 배포 (완료)](#로컬-배포)
2. [Mumbai 테스트넷 배포](#mumbai-테스트넷-배포)
3. [Mainnet 배포](#mainnet-배포)
4. [배포 후 검증](#배포-후-검증)

---

## ✅ 로컬 배포 (완료)

### 배포 결과
- **Network**: localhost (Hardhat)
- **Contract Address**: `0x5FbDB2315678afecb367f032d93F642f64180aa3`
- **Status**: ✅ 100% 테스트 통과
- **Interactions**: 8가지 기능 모두 정상 작동

### 실행 방법
```bash
# Terminal 1: 로컬 노드 실행
npm run node

# Terminal 2: 배포
npm run deploy:localhost

# Terminal 3: 상호작용 테스트
npx hardhat run scripts/interact.js --network localhost
```

---

## 🌐 Mumbai 테스트넷 배포

### Step 1: 준비물

#### 1.1 MetaMask 지갑 설정

1. **MetaMask 설치** (없다면)
   - https://metamask.io/

2. **Polygon Mumbai 네트워크 추가**
   ```
   Network Name: Polygon Mumbai
   RPC URL: https://rpc-mumbai.maticvigil.com
   Chain ID: 80001
   Currency Symbol: MATIC
   Block Explorer: https://mumbai.polygonscan.com/
   ```

3. **Private Key 내보내기**
   - MetaMask → 계정 세부정보 → 프라이빗 키 내보내기
   - ⚠️ **절대 공유하지 마세요!**

#### 1.2 테스트 MATIC 받기

**Faucet 사이트** (둘 중 하나 사용):
- https://faucet.polygon.technology/
- https://mumbaifaucet.com/

**필요량**: 최소 0.5 MATIC (배포 + 몇 번의 트랜잭션)

**확인**:
```bash
# 잔액 확인 (MetaMask 또는 PolygonScan)
https://mumbai.polygonscan.com/address/<YOUR_ADDRESS>
```

#### 1.3 환경 변수 설정

```bash
cd blockchain
cp .env.example .env
```

**.env 파일 편집**:
```bash
# Polygon Mumbai Testnet
POLYGON_RPC_URL=https://rpc-mumbai.maticvigil.com
PRIVATE_KEY=your_private_key_here  # 0x 제거하고 입력

# Polygonscan API Key (선택사항, Contract 검증용)
POLYGONSCAN_API_KEY=your_api_key

# Contract Address (배포 후 자동 출력됨)
CONTRACT_ADDRESS=
```

**Polygonscan API Key 발급** (검증용):
1. https://polygonscan.com/apis 접속
2. 계정 생성 및 로그인
3. API Keys → Create New API Key

---

### Step 2: Mumbai 배포 실행

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
⛽ Gas Used: ~1,234,567

📋 Etherscan 검증 명령어:
npx hardhat verify --network mumbai 0xNewContractAddress...

⚠️  다음 환경 변수를 .env 파일에 추가하세요:
CONTRACT_ADDRESS=0xNewContractAddress...
```

---

### Step 3: Contract 검증 (Polygonscan)

**배포 후 즉시 실행**:
```bash
npx hardhat verify --network mumbai <CONTRACT_ADDRESS>
```

**성공 시**:
```
Successfully verified contract HumanIdentityRegistry on Polygonscan.
https://mumbai.polygonscan.com/address/<CONTRACT_ADDRESS>#code
```

**검증 완료 후**:
- ✅ Polygonscan에서 소스 코드 확인 가능
- ✅ Read/Write Contract 탭에서 직접 상호작용 가능
- ✅ 다른 사용자들이 검증된 코드 확인 가능

---

### Step 4: Mumbai 배포 테스트

**상호작용 스크립트 실행**:
```bash
npx hardhat run scripts/interact.js --network mumbai
```

**또는 Polygonscan에서 직접**:
1. https://mumbai.polygonscan.com/address/<CONTRACT_ADDRESS>#writeContract
2. Connect Wallet (MetaMask)
3. 함수 실행:
   - `registerIdentity("HIP-02-TEST-1234", "QmHash...")`
   - `getIdentity("HIP-02-TEST-1234")`

---

## 🚀 Mainnet 배포 (추후)

### ⚠️ 배포 전 체크리스트

- [ ] Mumbai에서 충분한 테스트 완료
- [ ] Security Audit 완료 (권장)
- [ ] Gas Fee 최적화 검토
- [ ] Mainnet MATIC 준비 (최소 5 MATIC)
- [ ] 백업 Private Key 안전하게 보관

### 배포 명령어

```bash
# .env 파일 수정
POLYGON_MAINNET_RPC=https://polygon-rpc.com
PRIVATE_KEY=mainnet_private_key

# 배포
npm run deploy:polygon
```

---

## 🔍 배포 후 검증

### 1. Contract 정보 확인

```bash
# Polygonscan에서 확인
https://mumbai.polygonscan.com/address/<CONTRACT_ADDRESS>

# 또는 스크립트로 확인
npx hardhat run scripts/verify-deployment.js --network mumbai
```

### 2. 기능 테스트

```javascript
// scripts/verify-deployment.js
const registry = await HumanIdentityRegistry.attach(CONTRACT_ADDRESS);

// Owner 확인
const owner = await registry.owner();
console.log("Owner:", owner);

// 전체 통계
const total = await registry.getTotalIdentities();
console.log("Total Identities:", total);
```

### 3. Events 모니터링

**Polygonscan에서**:
- Contract → Events 탭
- IdentityRegistered, IdentityUpdated 등 이벤트 확인

---

## 🐛 문제 해결

### 배포 실패: "insufficient funds"
```bash
# 해결: Faucet에서 더 많은 MATIC 받기
https://faucet.polygon.technology/
```

### 검증 실패: "Already Verified"
```bash
# 이미 검증됨 - Polygonscan에서 확인
https://mumbai.polygonscan.com/address/<CONTRACT_ADDRESS>#code
```

### 배포 실패: "nonce too low"
```bash
# 해결: Hardhat 캐시 삭제
npx hardhat clean
rm -rf cache artifacts
npm run compile
```

### Gas 부족
```bash
# hardhat.config.js에서 gasPrice 조정
mumbai: {
  gasPrice: 30000000000, // 30 Gwei
}
```

---

## 📊 배포 비용 (예상)

| 네트워크 | Gas Used | Gas Price | 총 비용 (USD) |
|----------|----------|-----------|---------------|
| Localhost | ~1.5M | 0 | $0 |
| Mumbai | ~1.5M | 30 Gwei | $0 (무료) |
| Polygon Mainnet | ~1.5M | 30 Gwei | ~$0.01 |

---

## 🔐 보안 체크리스트

배포 전 확인:

- [ ] `.env` 파일이 `.gitignore`에 포함됨
- [ ] Private Key를 절대 공유하지 않음
- [ ] Private Key를 코드에 하드코딩하지 않음
- [ ] Contract에 치명적 버그가 없음
- [ ] Access Control 제대로 설정됨 (onlyOwner)
- [ ] 테스트 100% 통과

---

## 📝 배포 기록

### Mumbai Testnet
- **배포 일시**: (배포 후 기록)
- **Contract Address**: (배포 후 기록)
- **Deployer**: (배포 후 기록)
- **TX Hash**: (배포 후 기록)
- **Verified**: (검증 후 체크)

### Polygon Mainnet
- **배포 일시**: (추후)
- **Contract Address**: (추후)
- **Deployer**: (추후)
- **TX Hash**: (추후)
- **Verified**: (추후)

---

**마지막 업데이트**: 2026-02-08
**작성자**: 세션3 (블록체인 인프라)
