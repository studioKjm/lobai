// Check if identity is registered on blockchain
const hre = require("hardhat");

async function main() {
  const contractAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
  const userAddress = "0x78329E94d36dB46B52D1d1b2fD1cE6fB127b1E61";

  console.log("🔍 블록체인 신원 조회 중...\n");
  console.log(`📍 Contract: ${contractAddress}`);
  console.log(`👤 User: ${userAddress}\n`);

  // Get contract instance
  const HumanIdentityRegistry = await hre.ethers.getContractFactory("HumanIdentityRegistry");
  const contract = HumanIdentityRegistry.attach(contractAddress);

  // Try to get HIP ID by address
  try {
    console.log("1️⃣ 주소로 HIP ID 조회...");
    const hipId = await contract.getHipIdByAddress(userAddress);

    if (hipId && hipId !== "") {
      console.log(`✅ HIP ID 발견: ${hipId}\n`);

      // Get full identity info
      console.log("2️⃣ 신원 정보 조회...");
      const identity = await contract.getIdentity(hipId);

      console.log("\n📊 블록체인 신원 정보:");
      console.log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      console.log(`HIP ID:          ${identity.hipId}`);
      console.log(`IPFS Hash:       ${identity.ipfsHash}`);
      console.log(`소유자:          ${identity.owner}`);
      console.log(`검증 상태:       ${identity.isVerified ? '✅ 검증됨' : '⏳ 미검증'}`);
      console.log(`평판 레벨:       ${identity.reputationLevel} / 5`);
      console.log(`상호작용:        ${identity.totalInteractions.toString()}회`);
      console.log(`등록일:          ${new Date(identity.createdAt.toNumber() * 1000).toLocaleString('ko-KR')}`);
      console.log(`마지막 업데이트: ${new Date(identity.updatedAt.toNumber() * 1000).toLocaleString('ko-KR')}`);
      console.log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

      console.log("🎉 등록 완료!");
    } else {
      console.log("❌ HIP ID를 찾을 수 없습니다.");
      console.log("\n원인:");
      console.log("1. 등록이 아직 완료되지 않았습니다.");
      console.log("2. 트랜잭션이 실패했습니다.");
      console.log("3. 다른 계정으로 등록했습니다.\n");
    }
  } catch (error) {
    console.log("❌ 조회 실패");
    console.log("\n원인:");
    console.log("1. 아직 등록되지 않았습니다.");
    console.log("2. Contract 연결 오류");
    console.log(`\n상세 오류: ${error.message}\n`);
  }

  // Check all registered identities
  console.log("\n3️⃣ 전체 등록 현황 확인...");
  try {
    const count = await contract.getTotalIdentities();
    console.log(`📊 총 등록된 신원 수: ${count.toString()}개\n`);

    if (count.toNumber() > 0) {
      console.log("등록된 신원 목록:");
      const identities = await contract.getAllIdentities(0, Math.min(count.toNumber(), 10));
      identities.forEach((id, index) => {
        console.log(`${index + 1}. ${id.hipId} (${id.owner})`);
      });
    }
  } catch (error) {
    console.log("⚠️  getTotalIdentities 함수 없음 (정상)");
  }
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error("❌ 오류:", error.message);
    process.exit(1);
  });
