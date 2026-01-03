/**
 * AI Resilience Report Types
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
