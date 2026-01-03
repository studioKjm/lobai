/**
 * AI Resilience Analysis API Service
 */

import api from '@/lib/api';
import type { ApiResponse } from '@/lib/api';
import type { ResilienceReport, ResilienceReportSummary } from '@/types/resilience';

/**
 * AI 적응력 리포트 생성
 */
export const generateReport = async (type: 'basic' | 'premium' = 'basic'): Promise<ResilienceReport> => {
  const response = await api.post<ApiResponse<ResilienceReport>>(`/resilience/generate?type=${type}`);
  if (!response.data.success) {
    throw new Error(response.data.message);
  }
  return response.data.data;
};

/**
 * 최근 리포트 조회
 */
export const getLatestReport = async (): Promise<ResilienceReport | null> => {
  try {
    const response = await api.get<ApiResponse<ResilienceReport>>('/resilience/latest');
    if (!response.data.success) {
      return null;
    }
    return response.data.data;
  } catch {
    return null;
  }
};

/**
 * 모든 리포트 목록 조회
 */
export const getAllReports = async (): Promise<ResilienceReportSummary[]> => {
  const response = await api.get<ApiResponse<ResilienceReportSummary[]>>('/resilience/reports');
  return response.data.data;
};

/**
 * 리포트 잠금 해제
 */
export const unlockReport = async (reportId: number): Promise<ResilienceReport> => {
  const response = await api.post<ApiResponse<ResilienceReport>>(`/resilience/reports/${reportId}/unlock`);
  return response.data.data;
};
