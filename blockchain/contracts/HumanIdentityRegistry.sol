// SPDX-License-Identifier: MIT
pragma solidity ^0.8.27;

/**
 * @title HumanIdentityRegistry
 * @notice Human Identity Protocol (HIP) - AI Evaluation Layer for Human Identity
 * @dev 블록체인 기반 인간 신원 등록 및 검증 시스템
 *
 * HIP는 단순한 "인간 검증"이 아닌, "AI가 인간을 어떻게 평가하는가"를 기록합니다.
 * - Humanity Protocol: "당신은 진짜 인간입니까?"
 * - HIP: "GPT-4는 당신을 어떻게 인식합니까?"
 */
contract HumanIdentityRegistry {

    /* ========== STRUCTS ========== */

    struct Identity {
        string hipId;               // HIP ID (e.g., HIP-02-B4F8C3A9-7E2D-0x4f3a8b2c)
        string ipfsHash;            // IPFS hash of encrypted identity data
        address owner;              // Ethereum address of the identity owner
        uint256 createdAt;          // Timestamp of identity creation
        uint256 updatedAt;          // Timestamp of last update
        bool isVerified;            // Verification status
        uint8 reputationLevel;      // Reputation level (1-5)
        uint256 totalInteractions;  // Total AI interactions count
    }

    /* ========== STATE VARIABLES ========== */

    // HIP ID → Identity 매핑
    mapping(string => Identity) private identities;

    // Ethereum Address → HIP ID 매핑 (역방향 조회)
    mapping(address => string) private addressToHipId;

    // 등록된 HIP ID 목록
    string[] private registeredHipIds;

    // Contract owner
    address public owner;

    /* ========== EVENTS ========== */

    event IdentityRegistered(
        string indexed hipId,
        address indexed owner,
        string ipfsHash,
        uint256 timestamp
    );

    event IdentityUpdated(
        string indexed hipId,
        string newIpfsHash,
        uint256 timestamp
    );

    event IdentityVerified(
        string indexed hipId,
        bool isVerified,
        uint256 timestamp
    );

    event ReputationUpdated(
        string indexed hipId,
        uint8 oldLevel,
        uint8 newLevel,
        uint256 timestamp
    );

    /* ========== MODIFIERS ========== */

    modifier onlyOwner() {
        require(msg.sender == owner, "Only contract owner can call this function");
        _;
    }

    modifier onlyIdentityOwner(string memory hipId) {
        require(
            identities[hipId].owner == msg.sender,
            "Only identity owner can call this function"
        );
        _;
    }

    modifier identityExists(string memory hipId) {
        require(
            bytes(identities[hipId].hipId).length > 0,
            "Identity does not exist"
        );
        _;
    }

    /* ========== CONSTRUCTOR ========== */

    constructor() {
        owner = msg.sender;
    }

    /* ========== PUBLIC FUNCTIONS ========== */

    /**
     * @notice 새로운 HIP 신원 등록
     * @param hipId HIP ID (예: HIP-02-B4F8C3A9-7E2D-0x4f3a8b2c)
     * @param ipfsHash 암호화된 신원 데이터의 IPFS 해시
     */
    function registerIdentity(string memory hipId, string memory ipfsHash)
        external
        returns (bool)
    {
        // HIP ID가 이미 등록되어 있는지 확인
        require(
            bytes(identities[hipId].hipId).length == 0,
            "HIP ID already registered"
        );

        // 동일한 주소로 이미 등록된 신원이 있는지 확인
        require(
            bytes(addressToHipId[msg.sender]).length == 0,
            "Address already has a registered identity"
        );

        // 신원 생성
        identities[hipId] = Identity({
            hipId: hipId,
            ipfsHash: ipfsHash,
            owner: msg.sender,
            createdAt: block.timestamp,
            updatedAt: block.timestamp,
            isVerified: false,
            reputationLevel: 1, // 기본 레벨 1
            totalInteractions: 0
        });

        // 역방향 매핑 설정
        addressToHipId[msg.sender] = hipId;

        // 목록에 추가
        registeredHipIds.push(hipId);

        emit IdentityRegistered(hipId, msg.sender, ipfsHash, block.timestamp);

        return true;
    }

    /**
     * @notice HIP 신원 데이터 업데이트 (IPFS 해시 갱신)
     * @param hipId 업데이트할 HIP ID
     * @param newIpfsHash 새로운 IPFS 해시
     */
    function updateIdentity(string memory hipId, string memory newIpfsHash)
        external
        identityExists(hipId)
        onlyIdentityOwner(hipId)
        returns (bool)
    {
        identities[hipId].ipfsHash = newIpfsHash;
        identities[hipId].updatedAt = block.timestamp;

        emit IdentityUpdated(hipId, newIpfsHash, block.timestamp);

        return true;
    }

    /**
     * @notice HIP 신원 검증 상태 변경 (관리자 전용)
     * @param hipId 검증할 HIP ID
     * @param verified 검증 여부
     */
    function verifyIdentity(string memory hipId, bool verified)
        external
        onlyOwner
        identityExists(hipId)
        returns (bool)
    {
        identities[hipId].isVerified = verified;
        identities[hipId].updatedAt = block.timestamp;

        emit IdentityVerified(hipId, verified, block.timestamp);

        return true;
    }

    /**
     * @notice 평판 레벨 업데이트 (관리자 전용)
     * @param hipId 업데이트할 HIP ID
     * @param newLevel 새로운 평판 레벨 (1-5)
     */
    function updateReputation(string memory hipId, uint8 newLevel)
        external
        onlyOwner
        identityExists(hipId)
        returns (bool)
    {
        require(newLevel >= 1 && newLevel <= 5, "Reputation level must be between 1 and 5");

        uint8 oldLevel = identities[hipId].reputationLevel;
        identities[hipId].reputationLevel = newLevel;
        identities[hipId].updatedAt = block.timestamp;

        emit ReputationUpdated(hipId, oldLevel, newLevel, block.timestamp);

        return true;
    }

    /**
     * @notice AI 상호작용 카운트 증가
     * @param hipId 업데이트할 HIP ID
     */
    function incrementInteractions(string memory hipId)
        external
        onlyOwner
        identityExists(hipId)
        returns (bool)
    {
        identities[hipId].totalInteractions++;
        identities[hipId].updatedAt = block.timestamp;

        return true;
    }

    /* ========== VIEW FUNCTIONS ========== */

    /**
     * @notice HIP 신원 정보 조회
     * @param hipId 조회할 HIP ID
     */
    function getIdentity(string memory hipId)
        external
        view
        identityExists(hipId)
        returns (
            string memory,  // hipId
            string memory,  // ipfsHash
            address,        // owner
            uint256,        // createdAt
            uint256,        // updatedAt
            bool,           // isVerified
            uint8,          // reputationLevel
            uint256         // totalInteractions
        )
    {
        Identity memory identity = identities[hipId];
        return (
            identity.hipId,
            identity.ipfsHash,
            identity.owner,
            identity.createdAt,
            identity.updatedAt,
            identity.isVerified,
            identity.reputationLevel,
            identity.totalInteractions
        );
    }

    /**
     * @notice 주소로 HIP ID 조회
     * @param addr 조회할 Ethereum 주소
     */
    function getHipIdByAddress(address addr)
        external
        view
        returns (string memory)
    {
        return addressToHipId[addr];
    }

    /**
     * @notice HIP 신원 검증 여부 확인
     * @param hipId 확인할 HIP ID
     */
    function isIdentityVerified(string memory hipId)
        external
        view
        identityExists(hipId)
        returns (bool)
    {
        return identities[hipId].isVerified;
    }

    /**
     * @notice 등록된 전체 HIP ID 수 조회
     */
    function getTotalIdentities()
        external
        view
        returns (uint256)
    {
        return registeredHipIds.length;
    }

    /**
     * @notice 인덱스로 HIP ID 조회 (페이지네이션용)
     * @param index 조회할 인덱스
     */
    function getHipIdByIndex(uint256 index)
        external
        view
        returns (string memory)
    {
        require(index < registeredHipIds.length, "Index out of bounds");
        return registeredHipIds[index];
    }
}
