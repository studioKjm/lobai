import React, { useState } from 'react';
import { useChatStore } from '@/stores/chatStore';
import { ScheduleEvent } from '@/types';
import { format } from 'date-fns';
import { ScheduleDetailModal } from './ScheduleDetailModal';

const typeIcons: Record<string, string> = {
  REMINDER: '🔔',
  INTERACTION: '🤖',
  EVENT: '📅'
};

const typeColors: Record<string, string> = {
  REMINDER: 'bg-yellow-500/20 border-yellow-500/30',
  INTERACTION: 'bg-cyan-500/20 border-cyan-500/30',
  EVENT: 'bg-purple-500/20 border-purple-500/30'
};

export const SidebarSchedule: React.FC = () => {
  const { schedules, loadTodaysSchedules, isScheduleLoading } = useChatStore();
  const [selectedSchedule, setSelectedSchedule] = useState<ScheduleEvent | null>(null);

  if (isScheduleLoading) {
    return (
      <div className="glass p-4 rounded-2xl">
        <h3 className="text-sm font-semibold mb-3 opacity-60">오늘 일정</h3>
        <div className="text-center text-xs opacity-40 py-4">로딩 중...</div>
      </div>
    );
  }

  return (
    <>
      <div className="glass p-4 rounded-2xl">
        <h3 className="text-sm font-semibold mb-3 opacity-60">오늘 일정</h3>

        {schedules.length === 0 ? (
          <div className="text-center text-xs opacity-40 py-4">
            오늘 일정이 없습니다
          </div>
        ) : (
          <div className="space-y-2 max-h-[300px] overflow-y-auto">
            {schedules.slice(0, 5).map(schedule => {
              const startDate = new Date(schedule.startTime);
              const endDate = new Date(schedule.endTime);

              return (
                <div
                  key={schedule.id}
                  onClick={() => setSelectedSchedule(schedule)}
                  className={`p-2 rounded-lg border cursor-pointer transition-all hover:brightness-125 ${typeColors[schedule.type]} ${
                    schedule.isCompleted ? 'opacity-50' : ''
                  }`}
                >
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-sm">
                      {schedule.isCompleted ? '✅' : typeIcons[schedule.type]}
                    </span>
                    <span className={`text-xs font-medium truncate flex-1 ${
                      schedule.isCompleted ? 'line-through opacity-70' : ''
                    }`}>
                      {schedule.title}
                    </span>
                  </div>
                  <div className="text-xs opacity-60 pl-6">
                    {format(startDate, 'HH:mm')} ~ {format(endDate, 'HH:mm')}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      <ScheduleDetailModal
        isOpen={selectedSchedule !== null}
        onClose={() => {
          setSelectedSchedule(null);
          loadTodaysSchedules();
        }}
        schedule={selectedSchedule}
      />
    </>
  );
};
