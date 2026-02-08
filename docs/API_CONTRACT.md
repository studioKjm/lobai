# LobAI HIP API Contract v1.0

> **목적**: 모든 세션이 준수해야 할 API 명세 및 인터페이스 계약
> **작성일**: 2026-02-08
> **버전**: 1.0
> **관리**: 모든 API 변경 시 24시간 전 공지 필수

---

## 📌 목차

1. [Core Identity API (Session 1)](#1-core-identity-api-session-1)
2. [Blockchain API (Session 2)](#2-blockchain-api-session-2)
3. [AI Bridge API (Session 3)](#3-ai-bridge-api-session-3)
4. [Analytics API (Session 5)](#4-analytics-api-session-5)
5. [공통 규칙](#5-공통-규칙)
6. [변경 이력](#6-변경-이력)

---

## 1. Core Identity API (Session 1)

### 1.1 GET /api/hip/me

**설명**: 현재 로그인한 사용자의 HIP 프로필 조회

**인증**: Required (JWT Bearer Token)

**담당**: Session 1 (Core Identity Module)

**사용자**: Session 4 (Frontend Dashboard)

**Request**:
```http
GET /api/hip/me HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK)**:
```json
{
  "hipId": "HIP-01-A7F2E9C4-B3A1",
  "userId": 1,
  "overallHipScore": 78.5,
  "identityLevel": 7,
  "reputationTier": "Distinguished",
  "coreScores": {
    "cognitiveFlexibility": 82.0,
    "collaborationPattern": 85.0,
    "informationProcessing": 75.0,
    "emotionalIntelligence": 78.0,
    "creativity": 70.0,
    "ethicalAlignment": 81.0
  },
  "createdAt": "2026-02-01T10:00:00Z",
  "lastUpdatedAt": "2026-02-08T15:30:00Z",
  "totalInteractions": 142,
  "verificationStatus": "VERIFIED"
}
```

**Response Fields**:
- `hipId` (string): HIP ID (형식: `HIP-{version}-{hash}-{checksum}`)
- `userId` (number): User ID (FK)
- `overallHipScore` (number): 전체 점수 (0-100)
- `identityLevel` (number): Identity Level (1-10)
- `reputationTier` (string): Reputation Tier (`Novice`, `Emerging`, `Established`, `Distinguished`, `Legendary`)
- `coreScores` (object): 6개 Core Scores (0-100)
- `createdAt` (string): ISO 8601 DateTime
- `lastUpdatedAt` (string): ISO 8601 DateTime
- `totalInteractions` (number): 총 상호작용 수
- `verificationStatus` (string): 검증 상태 (`PENDING`, `VERIFIED`, `EXPIRED`)

**Error Responses**:
```json
// 401 Unauthorized
{
  "error": "UNAUTHORIZED",
  "message": "JWT token is missing or invalid"
}

// 404 Not Found
{
  "error": "HIP_NOT_FOUND",
  "message": "HIP profile not found for this user"
}
```

**버전 변경 이력**:
- v1.0 (2026-02-08): 초기 버전

---

### 1.2 POST /api/hip/me/reanalyze

**설명**: AffinityScore 기반으로 HIP 재분석

**인증**: Required (JWT Bearer Token)

**담당**: Session 1 (Core Identity Module)

**사용자**: Session 4 (Frontend Dashboard)

**Request**:
```http
POST /api/hip/me/reanalyze HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "forceUpdate": false
}
```

**Request Body**:
- `forceUpdate` (boolean, optional): 강제 업데이트 여부 (기본값: false)

**Response (200 OK)**:
```json
{
  "hipId": "HIP-01-A7F2E9C4-B3A1",
  "previousScore": 78.5,
  "newScore": 81.2,
  "scoreChange": +2.7,
  "previousLevel": 7,
  "newLevel": 8,
  "levelChanged": true,
  "updatedScores": {
    "cognitiveFlexibility": { "old": 82.0, "new": 84.0, "change": +2.0 },
    "collaborationPattern": { "old": 85.0, "new": 87.0, "change": +2.0 },
    "informationProcessing": { "old": 75.0, "new": 78.0, "change": +3.0 },
    "emotionalIntelligence": { "old": 78.0, "new": 80.0, "change": +2.0 },
    "creativity": { "old": 70.0, "new": 73.0, "change": +3.0 },
    "ethicalAlignment": { "old": 81.0, "new": 85.0, "change": +4.0 }
  },
  "message": "HIP profile updated successfully",
  "updatedAt": "2026-02-08T16:00:00Z"
}
```

**Error Responses**:
```json
// 429 Too Many Requests
{
  "error": "RATE_LIMIT_EXCEEDED",
  "message": "You can reanalyze only once per hour",
  "nextAvailableAt": "2026-02-08T17:00:00Z"
}
```

---

### 1.3 GET /api/hip/ranking

**설명**: HIP Score 기준 상위 랭킹 조회 (공개 API)

**인증**: Not Required

**담당**: Session 1 (Core Identity Module)

**사용자**: Session 4 (Frontend Dashboard), Public

**Request**:
```http
GET /api/hip/ranking?limit=10&offset=0 HTTP/1.1
Host: localhost:8080
```

**Query Parameters**:
- `limit` (number, optional): 조회 개수 (기본값: 10, 최대: 100)
- `offset` (number, optional): 시작 위치 (기본값: 0)

**Response (200 OK)**:
```json
{
  "rankings": [
    {
      "rank": 1,
      "hipId": "HIP-01-XXXXXXXX-XXXX",
      "overallHipScore": 95.8,
      "identityLevel": 10,
      "reputationTier": "Legendary",
      "username": "AI_Master_User" // Optional (if public profile)
    },
    {
      "rank": 2,
      "hipId": "HIP-01-YYYYYYYY-YYYY",
      "overallHipScore": 93.2,
      "identityLevel": 9,
      "reputationTier": "Exemplary",
      "username": "Anonymous"
    }
    // ... up to 'limit' entries
  ],
  "total": 1247,
  "limit": 10,
  "offset": 0
}
```

---

## 2. Blockchain API (Session 2)

### 2.1 POST /api/blockchain/register

**설명**: HIP ID를 블록체인에 등록

**인증**: Required (JWT Bearer Token)

**담당**: Session 2 (Blockchain Module)

**사용자**: Session 1 (Core Identity), Session 4 (Frontend)

**Request**:
```http
POST /api/blockchain/register HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "hipId": "HIP-01-A7F2E9C4-B3A1"
}
```

**Response (200 OK)**:
```json
{
  "txHash": "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
  "blockNumber": 12345678,
  "network": "polygon-zkevm-testnet",
  "contractAddress": "0xabcdefabcdefabcdefabcdefabcdefabcdefabcd",
  "registered": true,
  "timestamp": "2026-02-08T16:00:00Z",
  "explorerUrl": "https://testnet-zkevm.polygonscan.com/tx/0x123..."
}
```

**Error Responses**:
```json
// 409 Conflict
{
  "error": "ALREADY_REGISTERED",
  "message": "This HIP ID is already registered on blockchain",
  "txHash": "0xabc..."
}

// 500 Internal Server Error
{
  "error": "BLOCKCHAIN_ERROR",
  "message": "Failed to register on blockchain",
  "details": "Gas estimation failed"
}
```

---

### 2.2 GET /api/blockchain/verify/{hipId}

**설명**: 블록체인에서 HIP ID 검증

**인증**: Not Required

**담당**: Session 2 (Blockchain Module)

**사용자**: Session 4 (Frontend), Public

**Request**:
```http
GET /api/blockchain/verify/HIP-01-A7F2E9C4-B3A1 HTTP/1.1
Host: localhost:8080
```

**Response (200 OK)**:
```json
{
  "hipId": "HIP-01-A7F2E9C4-B3A1",
  "isRegistered": true,
  "txHash": "0x1234567890abcdef...",
  "blockNumber": 12345678,
  "registeredAt": "2026-02-08T16:00:00Z",
  "ipfsMetadataHash": "QmXxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
}
```

---

### 2.3 POST /api/blockchain/certificate/issue

**설명**: Identity Certificate NFT 발행

**인증**: Required (JWT Bearer Token)

**담당**: Session 2 (Blockchain Module)

**사용자**: Session 4 (Frontend)

**Request**:
```http
POST /api/blockchain/certificate/issue HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "hipId": "HIP-01-A7F2E9C4-B3A1",
  "certificateLevel": "GOLD" // SILVER, GOLD, PLATINUM
}
```

**Response (200 OK)**:
```json
{
  "certificateId": "CERT-001-A7F2E9C4",
  "nftTokenId": 12345,
  "certificateLevel": "GOLD",
  "txHash": "0xabc...",
  "ipfsImageUrl": "ipfs://QmXxxxxx/certificate.png",
  "opensea_url": "https://testnets.opensea.io/assets/...",
  "issuedAt": "2026-02-08T17:00:00Z"
}
```

---

## 3. AI Bridge API (Session 3)

### 3.1 POST /api/ai/analyze-all

**설명**: 모든 AI Provider로 사용자 분석 (GPT, Claude, Gemini)

**인증**: Required (JWT Bearer Token)

**담당**: Session 3 (AI Bridge Module)

**사용자**: Session 4 (Frontend)

**Request**:
```http
POST /api/ai/analyze-all HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "userId": 1,
  "includeHistory": true, // Optional: 대화 이력 포함 여부
  "maxMessages": 50 // Optional: 최대 메시지 수
}
```

**Response (200 OK)**:
```json
{
  "userId": 1,
  "analyzedAt": "2026-02-08T18:00:00Z",
  "providers": {
    "gpt4": {
      "overallScore": 80.5,
      "coreScores": {
        "cognitiveFlexibility": 85.0,
        "collaborationPattern": 82.0,
        "informationProcessing": 78.0,
        "emotionalIntelligence": 79.0,
        "creativity": 75.0,
        "ethicalAlignment": 84.0
      },
      "signature": "Analytical problem-solver with strong logical reasoning",
      "analysisTime": 3.2 // seconds
    },
    "claude": {
      "overallScore": 82.0,
      "coreScores": {
        "cognitiveFlexibility": 84.0,
        "collaborationPattern": 88.0,
        "informationProcessing": 80.0,
        "emotionalIntelligence": 85.0,
        "creativity": 73.0,
        "ethicalAlignment": 82.0
      },
      "signature": "Collaborative and empathetic communicator",
      "analysisTime": 2.8
    },
    "gemini": {
      "overallScore": 75.0,
      "coreScores": {
        "cognitiveFlexibility": 78.0,
        "collaborationPattern": 74.0,
        "informationProcessing": 76.0,
        "emotionalIntelligence": 72.0,
        "creativity": 80.0,
        "ethicalAlignment": 70.0
      },
      "signature": "Creative thinker with innovative ideas",
      "analysisTime": 2.5
    }
  },
  "crossAIAnalysis": {
    "consistencyScore": 79.17, // Average of all scores
    "variance": 12.5,
    "universalSignature": "Logical Collaborator with Creative Potential",
    "strengthAreas": ["Collaboration", "Cognitive Flexibility"],
    "improvementAreas": ["Creativity (Gemini)", "Ethical Alignment (Gemini)"]
  }
}
```

**Error Responses**:
```json
// 503 Service Unavailable
{
  "error": "AI_PROVIDER_ERROR",
  "message": "One or more AI providers are unavailable",
  "availableProviders": ["gpt4", "gemini"],
  "unavailableProviders": ["claude"]
}
```

---

### 3.2 GET /api/ai/comparison/{userId}

**설명**: Cross-AI 비교 리포트 조회

**인증**: Required (JWT Bearer Token, 본인만)

**담당**: Session 3 (AI Bridge Module)

**사용자**: Session 4 (Frontend)

**Request**:
```http
GET /api/ai/comparison/1 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK)**:
```json
{
  "userId": 1,
  "hipId": "HIP-01-A7F2E9C4-B3A1",
  "comparisonData": {
    "cognitiveFlexibility": {
      "gpt4": 85.0,
      "claude": 84.0,
      "gemini": 78.0,
      "average": 82.33,
      "stdDev": 3.86,
      "interpretation": "Consistent across all AIs"
    },
    // ... other scores
  },
  "radarChartData": [
    { "dimension": "Cognitive Flexibility", "gpt4": 85, "claude": 84, "gemini": 78 },
    { "dimension": "Collaboration", "gpt4": 82, "claude": 88, "gemini": 74 },
    // ...
  ],
  "lastAnalyzedAt": "2026-02-08T18:00:00Z"
}
```

---

## 4. Analytics API (Session 5)

### 4.1 GET /api/analytics/distribution

**설명**: HIP Score 분포 통계 (공개 API)

**인증**: Not Required

**담당**: Session 5 (Analytics Module)

**사용자**: Session 4 (Frontend), Public

**Request**:
```http
GET /api/analytics/distribution HTTP/1.1
Host: localhost:8080
```

**Response (200 OK)**:
```json
{
  "totalUsers": 1247,
  "scoreDistribution": [
    { "range": "0-10", "count": 12, "percentage": 0.96 },
    { "range": "11-20", "count": 45, "percentage": 3.61 },
    { "range": "21-30", "count": 89, "percentage": 7.14 },
    { "range": "31-40", "count": 134, "percentage": 10.75 },
    { "range": "41-50", "count": 187, "percentage": 15.00 },
    { "range": "51-60", "count": 245, "percentage": 19.65 },
    { "range": "61-70", "count": 198, "percentage": 15.88 },
    { "range": "71-80", "count": 156, "percentage": 12.51 },
    { "range": "81-90", "count": 112, "percentage": 8.98 },
    { "range": "91-100", "count": 69, "percentage": 5.53 }
  ],
  "averageScore": 62.4,
  "medianScore": 58.0,
  "cachedAt": "2026-02-08T18:00:00Z",
  "nextUpdateAt": "2026-02-08T19:00:00Z"
}
```

---

### 4.2 GET /api/analytics/trends

**설명**: Identity Level 트렌드 (시계열)

**인증**: Not Required

**담당**: Session 5 (Analytics Module)

**사용자**: Session 4 (Frontend)

**Request**:
```http
GET /api/analytics/trends?from=2026-01-01&to=2026-02-08 HTTP/1.1
Host: localhost:8080
```

**Query Parameters**:
- `from` (string, ISO 8601 Date): 시작일 (기본값: 30일 전)
- `to` (string, ISO 8601 Date): 종료일 (기본값: 오늘)

**Response (200 OK)**:
```json
{
  "from": "2026-01-01",
  "to": "2026-02-08",
  "dataPoints": [
    {
      "date": "2026-01-01",
      "averageScore": 58.2,
      "totalUsers": 1024,
      "levelDistribution": {
        "1": 120, "2": 150, "3": 180, "4": 160, "5": 140, "6": 120, "7": 80, "8": 50, "9": 20, "10": 4
      }
    },
    // ... daily data
    {
      "date": "2026-02-08",
      "averageScore": 62.4,
      "totalUsers": 1247,
      "levelDistribution": {
        "1": 100, "2": 130, "3": 160, "4": 180, "5": 170, "6": 150, "7": 120, "8": 90, "9": 60, "10": 87
      }
    }
  ]
}
```

---

## 5. 공통 규칙

### 5.1 인증 헤더

모든 인증 필요 API는 JWT Bearer Token 사용:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 5.2 에러 응답 형식

모든 에러는 다음 형식을 따름:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "details": "Additional details (optional)",
  "timestamp": "2026-02-08T18:00:00Z"
}
```

**공통 에러 코드**:
- `UNAUTHORIZED` (401): 인증 실패
- `FORBIDDEN` (403): 권한 없음
- `NOT_FOUND` (404): 리소스 없음
- `VALIDATION_ERROR` (400): 입력 검증 실패
- `RATE_LIMIT_EXCEEDED` (429): 요청 횟수 초과
- `INTERNAL_SERVER_ERROR` (500): 서버 오류
- `SERVICE_UNAVAILABLE` (503): 서비스 일시 불가

### 5.3 Rate Limiting

API별 Rate Limit:

| API | Rate Limit | 주기 |
|-----|------------|------|
| POST /api/hip/me/reanalyze | 1 req | 1 hour |
| POST /api/ai/analyze-all | 3 req | 1 hour |
| GET /api/hip/ranking | 60 req | 1 minute |
| 기타 GET | 100 req | 1 minute |
| 기타 POST | 20 req | 1 minute |

Rate Limit 초과 시 응답:

```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1644340800

{
  "error": "RATE_LIMIT_EXCEEDED",
  "message": "Too many requests",
  "retryAfter": 60
}
```

### 5.4 Pagination

리스트 조회 API는 pagination 지원:

```
GET /api/resource?limit=10&offset=0
```

**Response**:
```json
{
  "data": [...],
  "pagination": {
    "limit": 10,
    "offset": 0,
    "total": 1247,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### 5.5 날짜/시간 형식

- **ISO 8601 형식**: `2026-02-08T18:00:00Z` (UTC)
- 모든 DateTime은 UTC 기준
- Frontend에서 로컬 타임존 변환

---

## 6. 변경 이력

### v1.0 (2026-02-08)

**초기 API 명세**:
- ✅ Core Identity API (3개 엔드포인트)
- ✅ Blockchain API (3개 엔드포인트)
- ✅ AI Bridge API (2개 엔드포인트)
- ✅ Analytics API (2개 엔드포인트)

**작성자**: Session 1 (Core Identity Team)

**승인**: All Sessions (1-6)

---

## 부록: TypeScript 타입 정의

Frontend (Session 4)에서 사용할 타입:

```typescript
// HIP Profile
export interface HIPProfile {
  hipId: string;
  userId: number;
  overallHipScore: number;
  identityLevel: number;
  reputationTier: 'Novice' | 'Emerging' | 'Established' | 'Distinguished' | 'Legendary';
  coreScores: {
    cognitiveFlexibility: number;
    collaborationPattern: number;
    informationProcessing: number;
    emotionalIntelligence: number;
    creativity: number;
    ethicalAlignment: number;
  };
  createdAt: string;
  lastUpdatedAt: string;
  totalInteractions: number;
  verificationStatus: 'PENDING' | 'VERIFIED' | 'EXPIRED';
}

// Reanalyze Response
export interface ReanalyzeResponse {
  hipId: string;
  previousScore: number;
  newScore: number;
  scoreChange: number;
  previousLevel: number;
  newLevel: number;
  levelChanged: boolean;
  updatedScores: Record<string, { old: number; new: number; change: number }>;
  message: string;
  updatedAt: string;
}

// Blockchain Registration
export interface BlockchainRegistration {
  txHash: string;
  blockNumber: number;
  network: string;
  contractAddress: string;
  registered: boolean;
  timestamp: string;
  explorerUrl: string;
}

// AI Analysis
export interface AIAnalysisResponse {
  userId: number;
  analyzedAt: string;
  providers: Record<string, {
    overallScore: number;
    coreScores: Record<string, number>;
    signature: string;
    analysisTime: number;
  }>;
  crossAIAnalysis: {
    consistencyScore: number;
    variance: number;
    universalSignature: string;
    strengthAreas: string[];
    improvementAreas: string[];
  };
}

// Error Response
export interface APIError {
  error: string;
  message: string;
  details?: string;
  timestamp: string;
}
```

---

**문서 상태**: ✅ Approved by All Sessions
**다음 리뷰**: 2026-02-15 (API v1.1 계획)
**연락처**: GitHub Issues (API 변경 제안)

---

**🔒 API 변경 규칙**:

```
1. 24시간 전 GitHub Issue 생성
2. 모든 관련 세션 담당자 승인 필요
3. 하위 호환성 유지 (가능한 경우)
4. 변경 이력에 명시
5. 버전 번호 업데이트 (v1.0 → v1.1)
```

**Let's Respect the Contract! 🤝**
