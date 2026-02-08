// HumanIdentityRegistry 스마트 컨트랙트 배포 스크립트
const hre = require("hardhat");

async function main() {
  console.log("🚀 HumanIdentityRegistry 배포 시작...\n");

  // 배포자 계정 정보
  const [deployer] = await hre.ethers.getSigners();
  console.log("📝 배포 계정:", deployer.address);

  const balance = await hre.ethers.provider.getBalance(deployer.address);
  console.log("💰 계정 잔액:", hre.ethers.formatEther(balance), "ETH\n");

  // Contract 가져오기
  const HumanIdentityRegistry = await hre.ethers.getContractFactory(
    "HumanIdentityRegistry"
  );

  console.log("📦 Contract 배포 중...");
  const registry = await HumanIdentityRegistry.deploy();

  await registry.waitForDeployment();

  const contractAddress = await registry.getAddress();

  console.log("✅ HumanIdentityRegistry 배포 완료!");
  console.log("📍 Contract Address:", contractAddress);
  console.log("🔗 Network:", hre.network.name);
  console.log("⛽ Gas Used: 배포 완료");

  // Etherscan 검증 안내 (Mumbai 또는 Polygon인 경우)
  if (hre.network.name === "mumbai" || hre.network.name === "polygon") {
    console.log("\n📋 Etherscan 검증 명령어:");
    console.log(
      `npx hardhat verify --network ${hre.network.name} ${contractAddress}`
    );
  }

  // 배포 정보 저장
  const deploymentInfo = {
    network: hre.network.name,
    contractAddress: contractAddress,
    deployer: deployer.address,
    timestamp: new Date().toISOString(),
    blockNumber: await hre.ethers.provider.getBlockNumber(),
  };

  console.log("\n📄 배포 정보:");
  console.log(JSON.stringify(deploymentInfo, null, 2));

  // .env 파일 업데이트 안내
  console.log("\n⚠️  다음 환경 변수를 .env 파일에 추가하세요:");
  console.log(`CONTRACT_ADDRESS=${contractAddress}`);

  return contractAddress;
}

// 에러 핸들링
main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error("❌ 배포 실패:", error);
    process.exit(1);
  });
