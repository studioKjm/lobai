import api, { ApiResponse } from './api';
import { ScheduleEvent, CreateScheduleRequest } from '@/types';

export const scheduleApi = {
  /**
   * Get today's schedules
   */
  getTodaysSchedules: async (): Promise<ScheduleEvent[]> => {
    const response = await api.get<ApiResponse<ScheduleEvent[]>>('/schedules/today');
    return response.data.data;
  },

  /**
   * Get schedules by date range
   */
  getSchedulesByRange: async (start: string, end: string): Promise<ScheduleEvent[]> => {
    const response = await api.get<ApiResponse<ScheduleEvent[]>>('/schedules/range', {
      params: { start, end }
    });
    return response.data.data;
  },

  /**
   * Get upcoming schedules
   */
  getUpcomingSchedules: async (limit: number = 5): Promise<ScheduleEvent[]> => {
    const response = await api.get<ApiResponse<ScheduleEvent[]>>('/schedules/upcoming', {
      params: { limit }
    });
    return response.data.data;
  },

  /**
   * Create schedule
   */
  createSchedule: async (data: CreateScheduleRequest): Promise<ScheduleEvent> => {
    const response = await api.post<ApiResponse<ScheduleEvent>>('/schedules', data);
    return response.data.data;
  },

  /**
   * Update schedule
   */
  updateSchedule: async (id: number, data: Partial<CreateScheduleRequest> & { isCompleted?: boolean }): Promise<ScheduleEvent> => {
    const response = await api.put<ApiResponse<ScheduleEvent>>(`/schedules/${id}`, data);
    return response.data.data;
  },

  /**
   * Delete schedule
   */
  deleteSchedule: async (id: number): Promise<void> => {
    await api.delete(`/schedules/${id}`);
  }
};
