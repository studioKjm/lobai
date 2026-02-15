/**
 * Affinity Score API Service (Phase 2 - 7차원 고도화)
 */

import api from '@/lib/api';
import type { ApiResponse } from '@/lib/api';
import type { AffinityScore, AffinityTrendPoint } from '@/types/affinity';

/**
 * Get user's affinity score
 */
export const getAffinityScore = async (): Promise<AffinityScore> => {
  const response = await api.get<ApiResponse<AffinityScore>>('/affinity/score');
  return response.data.data;
};

/**
 * Recalculate affinity score (강제 재계산)
 */
export const recalculateAffinityScore = async (): Promise<AffinityScore> => {
  const response = await api.post<ApiResponse<AffinityScore>>('/affinity/recalculate');
  return response.data.data;
};

/**
 * Get affinity trend data (daily snapshots)
 */
export const getAffinityTrend = async (days: number = 30): Promise<AffinityTrendPoint[]> => {
  const response = await api.get<ApiResponse<AffinityTrendPoint[]>>(`/affinity/trend?days=${days}`);
  return response.data.data;
};
