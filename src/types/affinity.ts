/**
 * Affinity Score Types
 *
 * TypeScript definitions for the 7-dimension affinity scoring system (Phase 2)
 */

export interface AffinityScore {
  userId: number;
  overallScore: number; // 0-100
  level: number; // 1-5
  levelName: string;

  // 기존 4차원 점수 (null-safe)
  avgSentimentScore: number | null; // -1.00 to 1.00
  avgClarityScore: number | null; // 0.00 to 1.00
  avgContextScore: number | null; // 0.00 to 1.00
  avgUsageScore: number | null; // 0.00 to 1.00

  // 새로운 3차원 점수 (null-safe)
  avgEngagementDepth: number | null; // 0.00 to 1.00
  avgSelfDisclosure: number | null; // 0.00 to 1.00
  avgReciprocity: number | null; // 0.00 to 1.00

  // 관계 메타데이터
  relationshipStage: 'EARLY' | 'DEVELOPING' | 'MATURE';
  relationshipStageName: string;
  dataThresholdStatus: 'COLLECTING' | 'INITIAL' | 'FULL';
  dataThresholdMessage: string;
  noveltyDiscountFactor: number;
  honorificTransitionDetected: boolean;
  totalSessions: number;

  // 통계
  totalMessages: number;
  analyzedMessages: number;

  // 레벨 진행도
  progress: {
    currentLevelMin: number;
    currentLevelMax: number;
    progressPercentage: number;
  };
  updatedAt: string;
}

export interface AffinityTrendPoint {
  date: string;
  overallScore: number;
  level: number;
  avgSentimentScore: number;
  avgClarityScore: number;
  avgContextScore: number;
  avgUsageScore: number;
  avgEngagementDepth: number;
  avgSelfDisclosure: number;
  avgReciprocity: number;
  relationshipStage: string;
  dataThresholdStatus: string;
  totalMessages: number;
}

export interface ScoreBarProps {
  label: string;
  value: number | null | undefined; // null-safe
  maxValue?: number;
  color: string;
  delay?: number;
}

export const LEVEL_NAMES: Record<number, string> = {
  1: '낯선 사이',
  2: '알아가는 중',
  3: '친근한 사이',
  4: '가까운 친구',
  5: '최고의 파트너',
};

export const LEVEL_COLORS: Record<number, string> = {
  1: 'from-slate-500 to-slate-600',
  2: 'from-blue-500 to-cyan-500',
  3: 'from-purple-500 to-pink-500',
  4: 'from-violet-500 to-fuchsia-500',
  5: 'from-amber-400 to-rose-500',
};

export const STAGE_ICONS: Record<string, string> = {
  EARLY: '🌱',
  DEVELOPING: '🌿',
  MATURE: '🌳',
};
