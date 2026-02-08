// Transfer ETH to user's MetaMask account
const hre = require("hardhat");

async function main() {
  const targetAddress = "0x78329E94d36dB46B52D1d1b2fD1cE6fB127b1E61";
  const amountInEther = "100"; // 100 ETH

  console.log("💰 ETH 전송 시작...");
  console.log(`📍 수신 주소: ${targetAddress}`);
  console.log(`💵 전송 금액: ${amountInEther} ETH\n`);

  // Get the first Hardhat account (has 10,000 ETH)
  const [sender] = await hre.ethers.getSigners();

  console.log(`📤 발신 계정: ${sender.address}`);

  // Check sender balance
  const senderBalance = await hre.ethers.provider.getBalance(sender.address);
  console.log(`💰 발신 계정 잔액: ${hre.ethers.formatEther(senderBalance)} ETH`);

  // Check receiver balance before
  const receiverBalanceBefore = await hre.ethers.provider.getBalance(targetAddress);
  console.log(`📥 수신 계정 잔액 (전): ${hre.ethers.formatEther(receiverBalanceBefore)} ETH\n`);

  // Send transaction
  console.log("⏳ 트랜잭션 전송 중...");
  const tx = await sender.sendTransaction({
    to: targetAddress,
    value: hre.ethers.parseEther(amountInEther)
  });

  console.log(`📝 트랜잭션 해시: ${tx.hash}`);
  console.log("⏳ 블록 확인 중...");

  await tx.wait();

  console.log("✅ 트랜잭션 완료!\n");

  // Check receiver balance after
  const receiverBalanceAfter = await hre.ethers.provider.getBalance(targetAddress);
  console.log(`📥 수신 계정 잔액 (후): ${hre.ethers.formatEther(receiverBalanceAfter)} ETH`);

  console.log("\n🎉 성공적으로 ETH를 전송했습니다!");
  console.log("\n📌 다음 단계:");
  console.log("1. MetaMask에서 Account 3 선택");
  console.log("2. 네트워크가 'Hardhat Local'인지 확인");
  console.log("3. 잔액에 100 ETH가 표시되는지 확인");
  console.log("4. http://localhost:5175/dashboard 에서 블록체인 연결 테스트!");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error("❌ 오류 발생:", error);
    process.exit(1);
  });
