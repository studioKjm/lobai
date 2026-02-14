import React, { useState } from 'react';

interface ScheduleItem {
  id: number;
  title: string;
  time: string;
  type: 'reminder' | 'interaction' | 'event';
}

export const ScheduleCard: React.FC = () => {
  const [schedules, setSchedules] = useState<ScheduleItem[]>([
    {
      id: 1,
      title: '일일 AI 상호작용',
      time: '오후 3:00',
      type: 'interaction'
    }
  ]);

  const [isAdding, setIsAdding] = useState(false);

  const getTypeIcon = (type: ScheduleItem['type']) => {
    switch (type) {
      case 'reminder':
        return '🔔';
      case 'interaction':
        return '🤖';
      case 'event':
        return '📅';
    }
  };

  const getTypeBgColor = (type: ScheduleItem['type']) => {
    switch (type) {
      case 'reminder':
        return 'bg-yellow-500/20 border-yellow-500/30';
      case 'interaction':
        return 'bg-cyan-500/20 border-cyan-500/30';
      case 'event':
        return 'bg-purple-500/20 border-purple-500/30';
    }
  };

  return (
    <div className="glass p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-white/90">일정</h3>
        <button
          onClick={() => setIsAdding(!isAdding)}
          className="text-xs text-cyan-400 hover:text-cyan-300 transition-colors"
        >
          + 추가
        </button>
      </div>

      {/* Add Schedule Form (placeholder) */}
      {isAdding && (
        <div className="mb-4 p-4 bg-white/5 rounded-lg border border-white/10">
          <p className="text-sm text-white/60 text-center">
            일정 추가 기능 (개발 예정)
          </p>
        </div>
      )}

      {/* Schedule List */}
      <div className="space-y-3">
        {schedules.length === 0 ? (
          <p className="text-sm text-white/40 text-center py-8">
            예정된 일정이 없습니다
          </p>
        ) : (
          schedules.map((schedule) => (
            <div
              key={schedule.id}
              className={`rounded-lg p-4 border ${getTypeBgColor(schedule.type)} transition-all hover:scale-[1.02]`}
            >
              <div className="flex items-start gap-3">
                <span className="text-2xl">{getTypeIcon(schedule.type)}</span>
                <div className="flex-1">
                  <h4 className="text-sm font-semibold text-white/90 mb-1">
                    {schedule.title}
                  </h4>
                  <p className="text-xs text-white/60">
                    ⏰ {schedule.time}
                  </p>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Quick Actions */}
      <div className="mt-4 pt-4 border-t border-white/10">
        <button className="w-full text-xs text-white/60 hover:text-white/90 transition-colors py-2 bg-white/5 rounded-lg">
          리마인더 설정
        </button>
      </div>
    </div>
  );
};
