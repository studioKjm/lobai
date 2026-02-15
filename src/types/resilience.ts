/**
 * AI Resilience Report Types (Phase 2 - 친밀도 연동)
 */

export interface ResilienceReport {
  id: number;
  reportType: 'basic' | 'premium';
  reportVersion: string;

  readinessScore: number;
  readinessLevel: number;
  readinessLevelName: string;

  communicationScore: number;
  automationRiskScore: number;
  collaborationScore: number;
  misuseRiskScore: number;

  strengths: string[];
  weaknesses: string[];
  simulationResult: string;
  detailedFeedback: string;

  // 친밀도 연동 필드 (Phase 2)
  avgEngagementDepth?: number;
  avgSelfDisclosure?: number;
  avgReciprocity?: number;
  affinityScoreAtReport?: number;
  affinityLevelAtReport?: number;
  recommendations?: string[];

  analyzedMessageCount: number;
  analyzedPeriodDays: number;

  isUnlocked: boolean;
  createdAt: string;
}

export interface ResilienceReportSummary {
  id: number;
  reportType: 'basic' | 'premium';
  readinessScore: number;
  readinessLevel: number;
  readinessLevelName: string;
  isUnlocked: boolean;
  createdAt: string;
}
