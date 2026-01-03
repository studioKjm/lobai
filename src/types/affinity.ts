/**
 * Affinity Score Types
 *
 * TypeScript definitions for the affinity scoring system
 */

export interface AffinityScore {
  userId: number;
  overallScore: number; // 0-100
  level: number; // 1-5
  levelName: string;
  avgSentimentScore: number; // -1.00 to 1.00
  avgClarityScore: number; // 0.00 to 1.00
  avgContextScore: number; // 0.00 to 1.00
  avgUsageScore: number; // 0.00 to 1.00
  totalMessages: number;
  analyzedMessages: number;
  progress: {
    currentLevelMin: number;
    currentLevelMax: number;
    progressPercentage: number;
  };
  updatedAt: string;
}

export interface ScoreBarProps {
  label: string;
  value: number;
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
