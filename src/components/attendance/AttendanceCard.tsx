/**
 * AttendanceCard Component
 * 출석 체크 카드
 */

import { useEffect, useState } from 'react';
import { checkIn, getAttendanceStatus } from '@/services/attendanceApi';
import type { AttendanceStatus } from '@/types/attendance';

export function AttendanceCard() {
  const [status, setStatus] = useState<AttendanceStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [checking, setChecking] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    loadStatus();
  }, []);

  const loadStatus = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getAttendanceStatus();
      setStatus(data);
    } catch (err) {
      console.error('Failed to load attendance status:', err);
      setError('출석 현황을 불러올 수 없습니다');
    } finally {
      setLoading(false);
    }
  };

  const handleCheckIn = async () => {
    try {
      setChecking(true);
      setError(null);
      setSuccess(null);

      const record = await checkIn();
      setSuccess(`출석 완료! 연속 ${record.streakCount}일 | 보상 ${record.rewardPoints}P`);

      // 상태 새로고침
      await loadStatus();
    } catch (err: any) {
      setError(err.message || '출석 체크에 실패했습니다');
    } finally {
      setChecking(false);
    }
  };

  if (loading) {
    return (
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-indigo-900/90 via-purple-900/50 to-pink-900/90 backdrop-blur-xl border border-white/10 p-6">
        <div className="flex items-center justify-center h-32">
          <div className="animate-spin rounded-full h-8 w-8 border-4 border-indigo-500/30 border-t-indigo-500" />
        </div>
      </div>
    );
  }

  if (error && !status) {
    return (
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-indigo-900/90 via-purple-900/50 to-pink-900/90 backdrop-blur-xl border border-white/10 p-6">
        <p className="text-center text-slate-400">{error}</p>
        <button
          onClick={loadStatus}
          className="mt-3 w-full px-4 py-2 bg-indigo-500/20 hover:bg-indigo-500/30 rounded-lg transition-colors"
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-indigo-900/90 via-purple-900/50 to-pink-900/90 backdrop-blur-xl border border-white/10 shadow-2xl">
      {/* Animated background */}
      <div className="absolute inset-0 opacity-20">
        <div className="absolute top-5 right-5 w-20 h-20 bg-indigo-500/30 rounded-full blur-2xl animate-pulse" />
        <div className="absolute bottom-5 left-5 w-24 h-24 bg-pink-500/30 rounded-full blur-2xl animate-pulse" style={{ animationDelay: '1s' }} />
      </div>

      <div className="relative p-6">
        {/* Header */}
        <div className="flex items-center justify-between mb-5">
          <div>
            <h3 className="text-xl font-bold text-white flex items-center gap-2">
              <span className="text-2xl">📅</span>
              출석 체크
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              매일 출석하고 연속 기록을 쌓아보세요
            </p>
          </div>

          {/* Refresh button */}
          <button
            onClick={loadStatus}
            className="p-2 rounded-lg bg-white/5 hover:bg-white/10 border border-white/10 transition-all hover:scale-110 active:scale-95"
            title="새로고침"
          >
            <svg className="w-4 h-4 text-slate-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
          </button>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-3 gap-3 mb-5">
          <div className="bg-white/5 rounded-xl p-3 border border-white/10">
            <div className="text-2xl font-bold text-white">{status?.currentStreak || 0}</div>
            <div className="text-xs text-slate-400 mt-1">연속 출석</div>
          </div>
          <div className="bg-white/5 rounded-xl p-3 border border-white/10">
            <div className="text-2xl font-bold text-white">{status?.totalAttendanceDays || 0}</div>
            <div className="text-xs text-slate-400 mt-1">총 출석</div>
          </div>
          <div className="bg-white/5 rounded-xl p-3 border border-white/10">
            <div className="text-2xl font-bold text-white">{status?.maxStreak || 0}</div>
            <div className="text-xs text-slate-400 mt-1">최대 연속</div>
          </div>
        </div>

        {/* Check-in Button */}
        <button
          onClick={handleCheckIn}
          disabled={status?.checkedInToday || checking}
          className={`
            w-full py-3 rounded-xl font-semibold text-white transition-all duration-300
            ${status?.checkedInToday
              ? 'bg-green-500/30 cursor-not-allowed border-2 border-green-500/50'
              : 'bg-gradient-to-r from-indigo-500 to-pink-500 hover:from-indigo-600 hover:to-pink-600 active:scale-95 shadow-lg hover:shadow-xl'
            }
            ${checking ? 'opacity-50 cursor-wait' : ''}
          `}
        >
          {checking ? '출석 처리 중...' : status?.checkedInToday ? '✓ 오늘 출석 완료' : '출석 체크하기'}
        </button>

        {/* Messages */}
        {success && (
          <div className="mt-3 p-3 bg-green-500/20 border border-green-500/30 rounded-lg text-green-300 text-sm text-center">
            {success}
          </div>
        )}
        {error && (
          <div className="mt-3 p-3 bg-red-500/20 border border-red-500/30 rounded-lg text-red-300 text-sm text-center">
            {error}
          </div>
        )}

        {/* Streak info */}
        {status && status.currentStreak > 0 && (
          <div className="mt-4 text-center">
            <p className="text-xs text-slate-400">
              {status.currentStreak >= 7 ? '🔥 ' : ''}
              {status.currentStreak >= 30 ? '완벽한 출석! 계속 유지하세요!' :
               status.currentStreak >= 14 ? '대단해요! 2주 연속 출석!' :
               status.currentStreak >= 7 ? '일주일 연속! 잘하고 있어요!' :
               '계속 출석하면 보상이 증가합니다'}
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
