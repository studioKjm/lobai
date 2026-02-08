// Simple check for registered identity
const hre = require("hardhat");

async function main() {
  const contractAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
  const userAddress = "0x78329E94d36dB46B52D1d1b2fD1cE6fB127b1E61";

  console.log("🔍 블록체인 신원 확인\n");

  const HumanIdentityRegistry = await hre.ethers.getContractFactory("HumanIdentityRegistry");
  const contract = HumanIdentityRegistry.attach(contractAddress);

  try {
    const hipId = await contract.getHipIdByAddress(userAddress);

    if (hipId && hipId !== "") {
      console.log("✅ 등록 완료!");
      console.log(`\n📍 HIP ID: ${hipId}`);
      console.log(`👤 소유자: ${userAddress}\n`);

      // Get identity details
      const identity = await contract.getIdentity(hipId);
      console.log("📊 신원 정보:");
      console.log(`   HIP ID: ${identity[0]}`);
      console.log(`   IPFS Hash: ${identity[1]}`);
      console.log(`   Owner: ${identity[2]}`);
      console.log(`   Created: ${new Date(identity[3].toNumber() * 1000).toLocaleString('ko-KR')}`);
      console.log(`   Updated: ${new Date(identity[4].toNumber() * 1000).toLocaleString('ko-KR')}`);
      console.log(`   Verified: ${identity[5] ? '✅' : '❌'}`);
      console.log(`   Reputation: ${identity[6]} / 5`);
      console.log(`   Interactions: ${identity[7].toString()}`);

    } else {
      console.log("❌ 등록되지 않음");
    }
  } catch (error) {
    console.log("❌ 등록되지 않음");
    console.log(`오류: ${error.message}`);
  }
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
