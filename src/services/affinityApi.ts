/**
 * Affinity Score API Service
 *
 * API calls for fetching and managing affinity scores
 */

import api from '@/lib/api';
import type { ApiResponse } from '@/lib/api';
import type { AffinityScore } from '@/types/affinity';

/**
 * Get user's affinity score
 */
export const getAffinityScore = async (): Promise<AffinityScore> => {
  const response = await api.get<ApiResponse<AffinityScore>>('/affinity/score');
  return response.data.data;
};

/**
 * Recalculate affinity score
 */
export const recalculateAffinityScore = async (): Promise<AffinityScore> => {
  const response = await api.post<ApiResponse<AffinityScore>>('/affinity/recalculate');
  return response.data.data;
};
