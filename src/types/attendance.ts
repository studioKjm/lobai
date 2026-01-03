/**
 * Attendance Types
 */

export interface AttendanceRecord {
  id: number;
  checkInDate: string;
  checkInTime: string;
  streakCount: number;
  rewardPoints: number;
}

export interface AttendanceStatus {
  checkedInToday: boolean;
  currentStreak: number;
  totalAttendanceDays: number;
  maxStreak: number;
}
