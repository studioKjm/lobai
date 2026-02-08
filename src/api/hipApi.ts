// HIP API Client (Mock for Phase 1)
// Will be replaced with real API client after Backend (Session 1) is ready

import type { HIPProfile, ReanalyzeResponse, RankingResponse } from '@/types/hip';

// Simulated network delay
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

// Mock data generator
const generateMockProfile = (): HIPProfile => ({
  hipId: "HIP-01-A7F2E9C4-B3A1",
  userId: 1,
  overallHipScore: 78.5,
  identityLevel: 7,
  reputationTier: "Distinguished",
  coreScores: {
    cognitiveFlexibility: 82.0,
    collaborationPattern: 85.0,
    informationProcessing: 75.0,
    emotionalIntelligence: 78.0,
    creativity: 70.0,
    ethicalAlignment: 81.0
  },
  createdAt: "2026-02-01T10:00:00Z",
  lastUpdatedAt: new Date().toISOString(),
  totalInteractions: 142,
  verificationStatus: "VERIFIED"
});

// Mock HIP API
export const mockHIPApi = {
  /**
   * GET /api/hip/me
   * Get current user's HIP profile
   */
  getMyProfile: async (): Promise<HIPProfile> => {
    await delay(800); // Simulate network delay
    return generateMockProfile();
  },

  /**
   * POST /api/hip/me/reanalyze
   * Reanalyze HIP based on AffinityScore
   */
  reanalyze: async (forceUpdate = false): Promise<ReanalyzeResponse> => {
    await delay(2000); // Simulate AI analysis time

    const currentProfile = generateMockProfile();

    // Simulate score improvements
    const improvements = {
      cognitiveFlexibility: Math.random() * 5,
      collaborationPattern: Math.random() * 5,
      informationProcessing: Math.random() * 5,
      emotionalIntelligence: Math.random() * 5,
      creativity: Math.random() * 5,
      ethicalAlignment: Math.random() * 5
    };

    const updatedScores = Object.keys(currentProfile.coreScores).reduce((acc, key) => {
      const scoreKey = key as keyof typeof currentProfile.coreScores;
      const oldValue = currentProfile.coreScores[scoreKey];
      const change = improvements[scoreKey];
      const newValue = Math.min(100, oldValue + change);

      acc[key] = {
        old: oldValue,
        new: parseFloat(newValue.toFixed(1)),
        change: parseFloat(change.toFixed(1))
      };

      return acc;
    }, {} as Record<string, { old: number; new: number; change: number }>);

    const previousScore = currentProfile.overallHipScore;
    const newScore = Object.values(updatedScores).reduce((sum, s) => sum + s.new, 0) / 6;
    const scoreChange = newScore - previousScore;

    const previousLevel = currentProfile.identityLevel;
    const newLevel = Math.min(10, Math.floor(newScore / 10) + 1);

    return {
      hipId: currentProfile.hipId,
      previousScore,
      newScore: parseFloat(newScore.toFixed(1)),
      scoreChange: parseFloat(scoreChange.toFixed(1)),
      previousLevel,
      newLevel,
      levelChanged: newLevel !== previousLevel,
      updatedScores,
      message: "HIP profile updated successfully",
      updatedAt: new Date().toISOString()
    };
  },

  /**
   * GET /api/hip/ranking
   * Get HIP ranking (public API)
   */
  getRanking: async (limit = 10, offset = 0): Promise<RankingResponse> => {
    await delay(600);

    // Generate mock ranking data
    const mockRankings = Array.from({ length: 100 }, (_, i) => ({
      rank: i + 1,
      hipId: `HIP-01-${Math.random().toString(36).substring(2, 10).toUpperCase()}-${Math.random().toString(36).substring(2, 6).toUpperCase()}`,
      overallHipScore: parseFloat((100 - i * 0.5 - Math.random() * 2).toFixed(1)),
      identityLevel: Math.max(1, Math.min(10, Math.floor((100 - i * 0.5) / 10))),
      reputationTier: i < 10 ? "Legendary" : i < 30 ? "Exemplary" : i < 60 ? "Distinguished" : "Established",
      username: i % 3 === 0 ? `User_${i + 1}` : undefined
    })) as RankingResponse['rankings'];

    return {
      rankings: mockRankings.slice(offset, offset + limit),
      total: 100,
      limit,
      offset
    };
  }
};

// Real API client
import api from '@/lib/api';

// Backend response types (without ApiResponse wrapper)
interface BackendProfileResponse {
  hipId: string;
  userId: number;
  identityLevel: number;
  identityLevelName: string;
  reputationLevel: number;
  overallScore: number;
  aiTrustScore: number;
  dataQualityScore: number;
  totalInteractions: number;
  verificationStatus: string;
  lastVerifiedAt: string | null;
  createdAt: string;
  updatedAt: string;
  coreScores: {
    cognitiveFlexibility: number;
    collaborationPattern: number;
    informationProcessing: number;
    emotionalIntelligence: number;
    creativity: number;
    ethicalAlignment: number;
  };
}

interface BackendRankingResponse {
  ranking: Array<{
    hipId: string;
    identityLevel: number;
    identityLevelName: string;
    reputationLevel: number;
    overallScore: number;
    verificationStatus: string;
  }>;
  total: number;
}

// Helper function to convert backend response to frontend type
const mapToHIPProfile = (data: BackendProfileResponse): HIPProfile => ({
  hipId: data.hipId,
  userId: data.userId,
  overallHipScore: data.overallScore,
  identityLevel: data.identityLevel,
  reputationTier: data.identityLevelName as any, // Map identity level name to tier
  coreScores: data.coreScores,
  createdAt: data.createdAt,
  lastUpdatedAt: data.updatedAt,
  totalInteractions: data.totalInteractions,
  verificationStatus: data.verificationStatus as any
});

export const realHIPApi = {
  /**
   * GET /api/hip/me
   * Get current user's HIP profile
   */
  getMyProfile: async (): Promise<HIPProfile> => {
    const response = await api.get<BackendProfileResponse>('/hip/me');
    return mapToHIPProfile(response.data);
  },

  /**
   * POST /api/hip/me/reanalyze
   * Reanalyze HIP based on AffinityScore
   */
  reanalyze: async (forceUpdate = false): Promise<ReanalyzeResponse> => {
    const previousProfile = await realHIPApi.getMyProfile();
    const previousScore = previousProfile.overallHipScore;
    const previousLevel = previousProfile.identityLevel;

    const response = await api.post<BackendProfileResponse & { message: string }>('/hip/me/reanalyze');
    const updatedProfile = mapToHIPProfile(response.data);

    const newScore = updatedProfile.overallHipScore;
    const newLevel = updatedProfile.identityLevel;

    // Build score changes
    const updatedScores: Record<string, { old: number; new: number; change: number }> = {};
    Object.keys(updatedProfile.coreScores).forEach((key) => {
      const scoreKey = key as keyof typeof updatedProfile.coreScores;
      const newValue = updatedProfile.coreScores[scoreKey];
      const oldValue = previousProfile.coreScores[scoreKey];
      updatedScores[key] = {
        old: oldValue,
        new: newValue,
        change: newValue - oldValue
      };
    });

    return {
      hipId: updatedProfile.hipId,
      previousScore,
      newScore,
      scoreChange: newScore - previousScore,
      previousLevel,
      newLevel,
      levelChanged: newLevel !== previousLevel,
      updatedScores,
      message: response.data.message || 'Profile reanalyzed successfully',
      updatedAt: updatedProfile.lastUpdatedAt
    };
  },

  /**
   * GET /api/hip/ranking
   * Get HIP ranking (public API)
   */
  getRanking: async (limit = 10, offset = 0): Promise<RankingResponse> => {
    const response = await api.get<BackendRankingResponse>('/hip/ranking', {
      params: { limit }
    });

    const rankings: RankingResponse['rankings'] = response.data.ranking.map((entry, index) => ({
      rank: offset + index + 1,
      hipId: entry.hipId,
      overallHipScore: entry.overallScore,
      identityLevel: entry.identityLevel,
      reputationTier: entry.identityLevelName as any,
      username: undefined // Backend doesn't include username in ranking
    }));

    return {
      rankings,
      total: response.data.total,
      limit,
      offset
    };
  },

  /**
   * GET /api/hip/{hipId}
   * Get HIP profile by HIP ID (public API)
   */
  getProfileByHipId: async (hipId: string): Promise<HIPProfile> => {
    const response = await api.get<any>(`/hip/${hipId}`);

    // Public profile has limited fields
    return {
      hipId: response.data.hipId,
      userId: 0, // Not included in public profile
      overallHipScore: response.data.overallScore,
      identityLevel: response.data.identityLevel,
      reputationTier: response.data.identityLevelName as any,
      coreScores: {
        cognitiveFlexibility: 0,
        collaborationPattern: 0,
        informationProcessing: 0,
        emotionalIntelligence: 0,
        creativity: 0,
        ethicalAlignment: 0
      },
      createdAt: new Date().toISOString(),
      lastUpdatedAt: new Date().toISOString(),
      totalInteractions: 0,
      verificationStatus: response.data.verificationStatus as any
    };
  },

  /**
   * GET /api/hip/me/stats
   * Get my HIP statistics
   */
  getMyStats: async () => {
    const response = await api.get<any>('/hip/me/stats');
    return response.data;
  },

  /**
   * POST /api/hip/me/verify
   * Verify my HIP profile
   */
  verify: async () => {
    const response = await api.post<any>('/hip/me/verify');
    return response.data;
  }
};

// Export active API (switch between mock and real)
const USE_MOCK = import.meta.env.VITE_USE_MOCK !== 'false'; // Default to mock

export const hipApi = USE_MOCK ? mockHIPApi : realHIPApi;

// Named exports for flexibility
export { mockHIPApi as mock, realHIPApi as real };
