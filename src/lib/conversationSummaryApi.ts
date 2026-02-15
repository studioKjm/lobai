import api, { ApiResponse } from './api';
import { DailySummaryItem, DailySummaryDetail } from '@/types';

export const conversationSummaryApi = {
  /**
   * 일일 요약 목록 조회
   */
  getDailySummaries: async (limit: number = 30): Promise<DailySummaryItem[]> => {
    const response = await api.get<ApiResponse<DailySummaryItem[]>>(
      '/conversation-summaries/daily',
      { params: { limit } }
    );
    return response.data.data || [];
  },

  /**
   * 특정 날짜 일일 요약 + 원본 메시지 조회
   */
  getDailySummaryDetail: async (date: string): Promise<DailySummaryDetail | null> => {
    const response = await api.get<ApiResponse<DailySummaryDetail>>(
      `/conversation-summaries/daily/${date}`
    );
    return response.data.data;
  },
};
