/**
 * Attendance API Service
 */

import api from '@/lib/api';
import type { ApiResponse } from '@/lib/api';
import type { AttendanceRecord, AttendanceStatus } from '@/types/attendance';

/**
 * 출석 체크
 */
export const checkIn = async (): Promise<AttendanceRecord> => {
  const response = await api.post<ApiResponse<AttendanceRecord>>('/attendance/check-in');
  if (!response.data.success) {
    throw new Error(response.data.message);
  }
  return response.data.data;
};

/**
 * 출석 현황 조회
 */
export const getAttendanceStatus = async (): Promise<AttendanceStatus> => {
  const response = await api.get<ApiResponse<AttendanceStatus>>('/attendance/status');
  return response.data.data;
};

/**
 * 최근 출석 기록 조회
 */
export const getRecentAttendance = async (days: number = 30): Promise<AttendanceRecord[]> => {
  const response = await api.get<ApiResponse<AttendanceRecord[]>>(`/attendance/recent?days=${days}`);
  return response.data.data;
};
