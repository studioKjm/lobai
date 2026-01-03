import api, { ApiResponse } from '@/lib/api';
import type {
  StatsOverview,
  ActiveUsersChart,
  MessageStats,
  PersonaStats,
} from '@/types';

/**
 * Admin API Service
 * 관리자 전용 통계 API 호출 함수들
 */

/**
 * 전체 통계 개요 조회
 */
export const getStatsOverview = async (): Promise<StatsOverview> => {
  const response = await api.get<ApiResponse<StatsOverview>>('/admin/stats/overview');
  return response.data.data;
};

/**
 * 활성 사용자 차트 데이터 조회 (최근 30일)
 */
export const getActiveUsersChart = async (): Promise<ActiveUsersChart> => {
  const response = await api.get<ApiResponse<ActiveUsersChart>>('/admin/stats/users/active');
  return response.data.data;
};

/**
 * 메시지 통계 조회
 */
export const getMessageStats = async (): Promise<MessageStats> => {
  const response = await api.get<ApiResponse<MessageStats>>('/admin/stats/messages');
  return response.data.data;
};

/**
 * 페르소나별 사용 통계 조회
 */
export const getPersonaStats = async (): Promise<PersonaStats> => {
  const response = await api.get<ApiResponse<PersonaStats>>('/admin/stats/personas');
  return response.data.data;
};
