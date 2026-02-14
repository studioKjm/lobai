import React, { useEffect, useState } from 'react';
import { useChatStore } from '@/stores/chatStore';
import { ScheduleFormModal } from './ScheduleFormModal';
import { ScheduleEvent } from '@/types';
import { format, startOfWeek, addDays, isSameDay, parseISO } from 'date-fns';
import { ko } from 'date-fns/locale';

const typeIcons = {
  REMINDER: '🔔',
  INTERACTION: '🤖',
  EVENT: '📅'
};

const typeColors = {
  REMINDER: 'bg-yellow-500/20 border-yellow-500/30',
  INTERACTION: 'bg-cyan-500/20 border-cyan-500/30',
  EVENT: 'bg-purple-500/20 border-purple-500/30'
};

export const WeeklyScheduleView: React.FC = () => {
  const { schedules, loadSchedulesByRange, deleteSchedule, isScheduleLoading } = useChatStore();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedSchedule, setSelectedSchedule] = useState<ScheduleEvent | null>(null);
  const [selectedDate, setSelectedDate] = useState<string>('');

  // 이번 주 날짜 계산 (월~일)
  const weekStart = startOfWeek(new Date(), { weekStartsOn: 1 }); // 월요일 시작
  const weekDays = Array.from({ length: 7 }, (_, i) => addDays(weekStart, i));

  useEffect(() => {
    // 이번 주 일정 로드
    const start = weekDays[0].toISOString();
    const end = addDays(weekDays[6], 1).toISOString(); // 일요일 끝까지
    loadSchedulesByRange(start, end);
  }, [loadSchedulesByRange]);

  // 특정 날짜의 일정 필터링
  const getSchedulesForDay = (date: Date): ScheduleEvent[] => {
    return schedules.filter(schedule => {
      const scheduleDate = parseISO(schedule.startTime);
      return isSameDay(scheduleDate, date);
    }).sort((a, b) => {
      return new Date(a.startTime).getTime() - new Date(b.startTime).getTime();
    });
  };

  // 일정 추가 버튼 클릭
  const handleAddSchedule = (date: Date) => {
    setSelectedDate(format(date, 'yyyy-MM-dd'));
    setSelectedSchedule(null);
    setIsModalOpen(true);
  };

  // 일정 수정 버튼 클릭
  const handleEditSchedule = (schedule: ScheduleEvent) => {
    setSelectedSchedule(schedule);
    setIsModalOpen(true);
  };

  // 일정 삭제
  const handleDeleteSchedule = async (id: number) => {
    if (confirm('이 일정을 삭제하시겠습니까?')) {
      await deleteSchedule(id);
    }
  };

  if (isScheduleLoading) {
    return (
      <div className="glass p-6 rounded-2xl">
        <h3 className="text-xl font-semibold gradient-text mb-4">이번 주 일정</h3>
        <div className="text-center text-white/40 py-8">로딩 중...</div>
      </div>
    );
  }

  return (
    <>
      <div className="glass p-6 rounded-2xl">
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-xl font-semibold gradient-text">이번 주 일정</h3>
          <button
            onClick={() => handleAddSchedule(new Date())}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors flex items-center gap-2"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            일정 추가
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-7 gap-4">
          {weekDays.map((day, index) => {
            const daySchedules = getSchedulesForDay(day);
            const isToday = isSameDay(day, new Date());

            return (
              <div
                key={index}
                className={`border rounded-xl p-4 ${
                  isToday
                    ? 'border-blue-500/50 bg-blue-500/5'
                    : 'border-white/10 bg-white/5'
                }`}
              >
                {/* 날짜 헤더 */}
                <div className="text-center mb-3">
                  <div className="text-xs text-white/50">
                    {format(day, 'E', { locale: ko })}
                  </div>
                  <div className={`text-lg font-semibold ${
                    isToday ? 'text-blue-400' : 'text-white'
                  }`}>
                    {format(day, 'd')}
                  </div>
                </div>

                {/* 일정 추가 버튼 */}
                <button
                  onClick={() => handleAddSchedule(day)}
                  className="w-full mb-2 px-2 py-1 text-xs text-white/40 hover:text-white hover:bg-white/5 rounded transition-colors"
                >
                  + 추가
                </button>

                {/* 일정 목록 */}
                <div className="space-y-2 max-h-[300px] overflow-y-auto">
                  {daySchedules.length === 0 ? (
                    <div className="text-xs text-white/30 text-center py-4">
                      일정 없음
                    </div>
                  ) : (
                    daySchedules.map(schedule => (
                      <div
                        key={schedule.id}
                        className={`p-2 rounded-lg border ${typeColors[schedule.type]} group relative`}
                      >
                        <div className="flex items-start gap-2">
                          <span className="text-sm">{typeIcons[schedule.type]}</span>
                          <div className="flex-1 min-w-0">
                            <div className="text-xs font-medium text-white truncate">
                              {schedule.title}
                            </div>
                            <div className="text-xs text-white/50 mt-1">
                              {format(parseISO(schedule.startTime), 'HH:mm')}
                            </div>
                          </div>
                        </div>

                        {/* 수정/삭제 버튼 (호버 시 표시) */}
                        <div className="absolute top-1 right-1 opacity-0 group-hover:opacity-100 transition-opacity flex gap-1">
                          <button
                            onClick={() => handleEditSchedule(schedule)}
                            className="p-1 bg-blue-600 hover:bg-blue-700 rounded text-white"
                            title="수정"
                          >
                            <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                            </svg>
                          </button>
                          <button
                            onClick={() => handleDeleteSchedule(schedule.id)}
                            className="p-1 bg-red-600 hover:bg-red-700 rounded text-white"
                            title="삭제"
                          >
                            <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                            </svg>
                          </button>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* 일정 추가/수정 모달 */}
      <ScheduleFormModal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setSelectedSchedule(null);
          setSelectedDate('');
        }}
        schedule={selectedSchedule}
        defaultDate={selectedDate}
      />
    </>
  );
};
