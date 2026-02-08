// HIP (Human Identity Protocol) Type Definitions
// Based on docs/API_CONTRACT.md

export type ReputationTier = 'Novice' | 'Emerging' | 'Established' | 'Distinguished' | 'Legendary';
export type VerificationStatus = 'PENDING' | 'VERIFIED' | 'EXPIRED';

export interface CoreScores {
  cognitiveFlexibility: number;
  collaborationPattern: number;
  informationProcessing: number;
  emotionalIntelligence: number;
  creativity: number;
  ethicalAlignment: number;
}

export interface HIPProfile {
  hipId: string;
  userId: number;
  overallHipScore: number;
  identityLevel: number;
  reputationTier: ReputationTier;
  coreScores: CoreScores;
  createdAt: string;
  lastUpdatedAt: string;
  totalInteractions: number;
  verificationStatus: VerificationStatus;
}

export interface ScoreChange {
  old: number;
  new: number;
  change: number;
}

export interface ReanalyzeResponse {
  hipId: string;
  previousScore: number;
  newScore: number;
  scoreChange: number;
  previousLevel: number;
  newLevel: number;
  levelChanged: boolean;
  updatedScores: Record<string, ScoreChange>;
  message: string;
  updatedAt: string;
}

export interface RankingEntry {
  rank: number;
  hipId: string;
  overallHipScore: number;
  identityLevel: number;
  reputationTier: ReputationTier;
  username?: string;
}

export interface RankingResponse {
  rankings: RankingEntry[];
  total: number;
  limit: number;
  offset: number;
}

export interface APIError {
  error: string;
  message: string;
  details?: string;
  timestamp: string;
}

// Blockchain Types (Phase 1.5)
export interface BlockchainRegistration {
  txHash: string;
  blockNumber: number;
  network: string;
  contractAddress: string;
  registered: boolean;
  timestamp: string;
  explorerUrl: string;
}

export interface CertificateData {
  certificateId: string;
  nftTokenId: number;
  certificateLevel: 'SILVER' | 'GOLD' | 'PLATINUM';
  txHash: string;
  ipfsImageUrl: string;
  opensea_url: string;
  issuedAt: string;
}

// AI Bridge Types (Phase 2)
export interface AIScoreResponse {
  overallScore: number;
  coreScores: CoreScores;
  signature: string;
  analysisTime: number;
}

export interface CrossAIAnalysis {
  consistencyScore: number;
  variance: number;
  universalSignature: string;
  strengthAreas: string[];
  improvementAreas: string[];
}

export interface AIAnalysisResponse {
  userId: number;
  analyzedAt: string;
  providers: Record<string, AIScoreResponse>;
  crossAIAnalysis: CrossAIAnalysis;
}
