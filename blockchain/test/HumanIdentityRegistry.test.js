const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("HumanIdentityRegistry", function () {
  let registry;
  let owner;
  let user1;
  let user2;

  // 테스트용 HIP ID 및 IPFS 해시
  const HIP_ID_1 = "HIP-02-B4F8C3A9-7E2D-0x4f3a8b2c";
  const HIP_ID_2 = "HIP-02-C5G9D4B0-8F3E-0x5g4b9c3d";
  const IPFS_HASH_1 = "QmYwAPJzv5CZsnA625s3Xf2nemtYgPpHdWEz79ojWnPbdG";
  const IPFS_HASH_2 = "QmZ4tDuvesekSs4qM5ZBKpXiZGun7S2CYtEZRB3DYXkjGx";

  beforeEach(async function () {
    // 계정 설정
    [owner, user1, user2] = await ethers.getSigners();

    // Contract 배포
    const HumanIdentityRegistry = await ethers.getContractFactory(
      "HumanIdentityRegistry"
    );
    registry = await HumanIdentityRegistry.deploy();
    await registry.waitForDeployment();
  });

  describe("배포 테스트", function () {
    it("Contract owner가 올바르게 설정되어야 함", async function () {
      expect(await registry.owner()).to.equal(owner.address);
    });

    it("초기 신원 개수가 0이어야 함", async function () {
      expect(await registry.getTotalIdentities()).to.equal(0);
    });
  });

  describe("신원 등록 (registerIdentity)", function () {
    it("새로운 신원을 성공적으로 등록해야 함", async function () {
      const tx = await registry.connect(user1).registerIdentity(HIP_ID_1, IPFS_HASH_1);
      const receipt = await tx.wait();
      const block = await ethers.provider.getBlock(receipt.blockNumber);

      await expect(tx)
        .to.emit(registry, "IdentityRegistered")
        .withArgs(HIP_ID_1, user1.address, IPFS_HASH_1, block.timestamp);

      expect(await registry.getTotalIdentities()).to.equal(1);
    });

    it("등록된 신원 정보를 조회할 수 있어야 함", async function () {
      await registry.connect(user1).registerIdentity(HIP_ID_1, IPFS_HASH_1);

      const identity = await registry.getIdentity(HIP_ID_1);
      expect(identity[0]).to.equal(HIP_ID_1); // hipId
      expect(identity[1]).to.equal(IPFS_HASH_1); // ipfsHash
      expect(identity[2]).to.equal(user1.address); // owner
      expect(identity[5]).to.equal(false); // isVerified
      expect(identity[6]).to.equal(1); // reputationLevel (기본값)
      expect(identity[7]).to.equal(0); // totalInteractions
    });

    it("주소로 HIP ID를 조회할 수 있어야 함", async function () {
      await registry.connect(user1).registerIdentity(HIP_ID_1, IPFS_HASH_1);

      const hipId = await registry.getHipIdByAddress(user1.address);
      expect(hipId).to.equal(HIP_ID_1);
    });

    it("중복된 HIP ID 등록 시 실패해야 함", async function () {
      await registry.connect(user1).registerIdentity(HIP_ID_1, IPFS_HASH_1);

      await expect(
        registry.connect(user2).registerIdentity(HIP_ID_1, IPFS_HASH_2)
      ).to.be.revertedWith("HIP ID already registered");
    });

    it("동일 주소로 여러 신원 등록 시 실패해야 함", async function () {
      await registry.connect(user1).registerIdentity(HIP_ID_1, IPFS_HASH_1);

      await expect(
        registry.connect(user1).registerIdentity(HIP_ID_2, IPFS_HASH_2)
      ).to.be.revertedWith("Address already has a registered identity");
    });
  });

  describe("신원 업데이트 (updateIdentity)", function () {
    beforeEach(async function () {
      await registry.connect(user1).registerIdentity(HIP_ID_1, IPFS_HASH_1);
    });

    it("신원 소유자가 IPFS 해시를 업데이트할 수 있어야 함", async function () {
      const tx = await registry.connect(user1).updateIdentity(HIP_ID_1, IPFS_HASH_2);
      const receipt = await tx.wait();
      const block = await ethers.provider.getBlock(receipt.blockNumber);

      await expect(tx)
        .to.emit(registry, "IdentityUpdated")
        .withArgs(HIP_ID_1, IPFS_HASH_2, block.timestamp);

      const identity = await registry.getIdentity(HIP_ID_1);
      expect(identity[1]).to.equal(IPFS_HASH_2);
    });

    it("신원 소유자가 아닌 경우 업데이트 실패해야 함", async function () {
      await expect(
        registry.connect(user2).updateIdentity(HIP_ID_1, IPFS_HASH_2)
      ).to.be.revertedWith("Only identity owner can call this function");
    });

    it("존재하지 않는 신원 업데이트 시 실패해야 함", async function () {
      await expect(
        registry.connect(user1).updateIdentity(HIP_ID_2, IPFS_HASH_2)
      ).to.be.revertedWith("Identity does not exist");
    });
  });

  describe("신원 검증 (verifyIdentity)", function () {
    beforeEach(async function () {
      await registry.connect(user1).registerIdentity(HIP_ID_1, IPFS_HASH_1);
    });

    it("관리자가 신원을 검증할 수 있어야 함", async function () {
      const tx = await registry.connect(owner).verifyIdentity(HIP_ID_1, true);
      const receipt = await tx.wait();
      const block = await ethers.provider.getBlock(receipt.blockNumber);

      await expect(tx)
        .to.emit(registry, "IdentityVerified")
        .withArgs(HIP_ID_1, true, block.timestamp);

      expect(await registry.isIdentityVerified(HIP_ID_1)).to.equal(true);
    });

    it("관리자가 아닌 경우 검증 실패해야 함", async function () {
      await expect(
        registry.connect(user1).verifyIdentity(HIP_ID_1, true)
      ).to.be.revertedWith("Only contract owner can call this function");
    });
  });

  describe("평판 업데이트 (updateReputation)", function () {
    beforeEach(async function () {
      await registry.connect(user1).registerIdentity(HIP_ID_1, IPFS_HASH_1);
    });

    it("관리자가 평판 레벨을 업데이트할 수 있어야 함", async function () {
      const tx = await registry.connect(owner).updateReputation(HIP_ID_1, 3);
      const receipt = await tx.wait();
      const block = await ethers.provider.getBlock(receipt.blockNumber);

      await expect(tx)
        .to.emit(registry, "ReputationUpdated")
        .withArgs(HIP_ID_1, 1, 3, block.timestamp);

      const identity = await registry.getIdentity(HIP_ID_1);
      expect(identity[6]).to.equal(3); // reputationLevel
    });

    it("평판 레벨이 1-5 범위를 벗어나면 실패해야 함", async function () {
      await expect(
        registry.connect(owner).updateReputation(HIP_ID_1, 0)
      ).to.be.revertedWith("Reputation level must be between 1 and 5");

      await expect(
        registry.connect(owner).updateReputation(HIP_ID_1, 6)
      ).to.be.revertedWith("Reputation level must be between 1 and 5");
    });
  });

  describe("상호작용 카운트 (incrementInteractions)", function () {
    beforeEach(async function () {
      await registry.connect(user1).registerIdentity(HIP_ID_1, IPFS_HASH_1);
    });

    it("관리자가 상호작용 수를 증가시킬 수 있어야 함", async function () {
      await registry.connect(owner).incrementInteractions(HIP_ID_1);
      await registry.connect(owner).incrementInteractions(HIP_ID_1);
      await registry.connect(owner).incrementInteractions(HIP_ID_1);

      const identity = await registry.getIdentity(HIP_ID_1);
      expect(identity[7]).to.equal(3); // totalInteractions
    });
  });

  describe("페이지네이션 (getHipIdByIndex)", function () {
    it("인덱스로 HIP ID를 조회할 수 있어야 함", async function () {
      await registry.connect(user1).registerIdentity(HIP_ID_1, IPFS_HASH_1);
      await registry.connect(user2).registerIdentity(HIP_ID_2, IPFS_HASH_2);

      expect(await registry.getHipIdByIndex(0)).to.equal(HIP_ID_1);
      expect(await registry.getHipIdByIndex(1)).to.equal(HIP_ID_2);
      expect(await registry.getTotalIdentities()).to.equal(2);
    });

    it("범위를 벗어난 인덱스 조회 시 실패해야 함", async function () {
      await registry.connect(user1).registerIdentity(HIP_ID_1, IPFS_HASH_1);

      await expect(registry.getHipIdByIndex(1)).to.be.revertedWith(
        "Index out of bounds"
      );
    });
  });

  // 헬퍼 함수
  async function getBlockTimestamp() {
    const blockNumber = await ethers.provider.getBlockNumber();
    const block = await ethers.provider.getBlock(blockNumber);
    return block.timestamp;
  }
});
