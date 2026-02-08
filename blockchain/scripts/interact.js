// 배포된 HumanIdentityRegistry Contract와 상호작용하는 스크립트
const hre = require("hardhat");

async function main() {
  console.log("🔗 HumanIdentityRegistry 상호작용 테스트\n");

  // Contract 주소 (배포 시 출력된 주소)
  const contractAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";

  // Contract 인스턴스 가져오기
  const HumanIdentityRegistry = await hre.ethers.getContractFactory(
    "HumanIdentityRegistry"
  );
  const registry = await HumanIdentityRegistry.attach(contractAddress);

  console.log("📍 Contract Address:", contractAddress);
  console.log("🔗 Network:", hre.network.name, "\n");

  // 테스트 계정 가져오기
  const [owner, user1, user2] = await hre.ethers.getSigners();

  console.log("👤 Owner:", owner.address);
  console.log("👤 User1:", user1.address);
  console.log("👤 User2:", user2.address, "\n");

  // 1. 신원 등록
  console.log("📝 1. 신원 등록 테스트");
  console.log("─────────────────────────────────────");

  const hipId1 = "HIP-02-A1B2C3D4-E5F6-0x1234abcd";
  const ipfsHash1 = "QmYwAPJzv5CZsnA625s3Xf2nemtYgPpHdWEz79ojWnPbdG";

  const tx1 = await registry.connect(user1).registerIdentity(hipId1, ipfsHash1);
  await tx1.wait();
  console.log("✅ User1 신원 등록 완료");
  console.log("   HIP ID:", hipId1);
  console.log("   TX:", tx1.hash, "\n");

  // 2. 신원 조회
  console.log("📖 2. 신원 조회");
  console.log("─────────────────────────────────────");

  const identity = await registry.getIdentity(hipId1);
  console.log("HIP ID:", identity[0]);
  console.log("IPFS Hash:", identity[1]);
  console.log("Owner:", identity[2]);
  console.log("Created At:", new Date(Number(identity[3]) * 1000).toISOString());
  console.log("Verified:", identity[5]);
  console.log("Reputation Level:", identity[6].toString());
  console.log("Total Interactions:", identity[7].toString(), "\n");

  // 3. 주소로 HIP ID 조회
  console.log("🔍 3. 주소로 HIP ID 조회");
  console.log("─────────────────────────────────────");

  const foundHipId = await registry.getHipIdByAddress(user1.address);
  console.log("User1 Address:", user1.address);
  console.log("Found HIP ID:", foundHipId, "\n");

  // 4. 신원 검증 (관리자)
  console.log("✓ 4. 신원 검증 (관리자)");
  console.log("─────────────────────────────────────");

  const tx2 = await registry.connect(owner).verifyIdentity(hipId1, true);
  await tx2.wait();
  console.log("✅ 신원 검증 완료");

  const isVerified = await registry.isIdentityVerified(hipId1);
  console.log("Verified Status:", isVerified, "\n");

  // 5. 평판 업데이트 (관리자)
  console.log("⭐ 5. 평판 업데이트");
  console.log("─────────────────────────────────────");

  const tx3 = await registry.connect(owner).updateReputation(hipId1, 4);
  await tx3.wait();
  console.log("✅ 평판 레벨 업데이트: 1 → 4");

  const updatedIdentity = await registry.getIdentity(hipId1);
  console.log("New Reputation Level:", updatedIdentity[6].toString(), "\n");

  // 6. 상호작용 카운트 증가
  console.log("📈 6. AI 상호작용 카운트 증가");
  console.log("─────────────────────────────────────");

  for (let i = 0; i < 5; i++) {
    const tx = await registry.connect(owner).incrementInteractions(hipId1);
    await tx.wait();
  }
  console.log("✅ 상호작용 5회 추가");

  const finalIdentity = await registry.getIdentity(hipId1);
  console.log("Total Interactions:", finalIdentity[7].toString(), "\n");

  // 7. IPFS 해시 업데이트
  console.log("🔄 7. IPFS 해시 업데이트");
  console.log("─────────────────────────────────────");

  const newIpfsHash = "QmZ4tDuvesekSs4qM5ZBKpXiZGun7S2CYtEZRB3DYXkjGx";
  const tx4 = await registry.connect(user1).updateIdentity(hipId1, newIpfsHash);
  await tx4.wait();
  console.log("✅ IPFS 해시 업데이트 완료");
  console.log("Old:", ipfsHash1);
  console.log("New:", newIpfsHash, "\n");

  // 8. 전체 통계
  console.log("📊 8. 전체 통계");
  console.log("─────────────────────────────────────");

  const totalIdentities = await registry.getTotalIdentities();
  console.log("등록된 신원 수:", totalIdentities.toString());

  if (totalIdentities > 0n) {
    const firstHipId = await registry.getHipIdByIndex(0);
    console.log("첫 번째 HIP ID:", firstHipId);
  }

  console.log("\n✅ 모든 상호작용 테스트 완료!");
  console.log("═══════════════════════════════════════\n");

  // 최종 신원 정보 출력
  const finalResult = await registry.getIdentity(hipId1);
  console.log("📋 최종 신원 정보:");
  console.log(JSON.stringify({
    hipId: finalResult[0],
    ipfsHash: finalResult[1],
    owner: finalResult[2],
    createdAt: new Date(Number(finalResult[3]) * 1000).toISOString(),
    updatedAt: new Date(Number(finalResult[4]) * 1000).toISOString(),
    isVerified: finalResult[5],
    reputationLevel: finalResult[6].toString(),
    totalInteractions: finalResult[7].toString(),
  }, null, 2));
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error("❌ 에러 발생:", error);
    process.exit(1);
  });
