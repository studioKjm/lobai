import React, { useState, useEffect } from 'react';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { useChatStore } from '@/stores/chatStore';
import type { EventInput } from '@fullcalendar/core';

type ViewMode = 'day' | 'week' | 'month';

export const ScheduleCalendar: React.FC = () => {
  const [viewMode, setViewMode] = useState<ViewMode>('month');
  const { schedules, loadSchedulesByRange } = useChatStore();

  useEffect(() => {
    // Load schedules for current month
    const now = new Date();
    const start = new Date(now.getFullYear(), now.getMonth(), 1);
    const end = new Date(now.getFullYear(), now.getMonth() + 1, 0);

    loadSchedulesByRange(start.toISOString(), end.toISOString());
  }, [loadSchedulesByRange]);

  const events: EventInput[] = schedules.map(s => ({
    id: String(s.id),
    title: s.title,
    start: s.startTime,
    end: s.endTime,
    backgroundColor:
      s.type === 'REMINDER' ? '#eab308' :
      s.type === 'INTERACTION' ? '#06b6d4' :
      '#a855f7',
    borderColor:
      s.type === 'REMINDER' ? '#ca8a04' :
      s.type === 'INTERACTION' ? '#0891b2' :
      '#9333ea'
  }));

  const getCalendarView = () => {
    switch (viewMode) {
      case 'day':
        return 'timeGridDay';
      case 'week':
        return 'timeGridWeek';
      case 'month':
      default:
        return 'dayGridMonth';
    }
  };

  return (
    <div className="glass p-6 rounded-2xl">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-xl font-semibold gradient-text">일정 관리</h3>

        <div className="flex gap-2">
          {(['day', 'week', 'month'] as ViewMode[]).map(mode => (
            <button
              key={mode}
              onClick={() => setViewMode(mode)}
              className={`px-4 py-2 rounded-lg text-sm transition-all ${
                viewMode === mode
                  ? 'bg-blue-500/20 border-2 border-blue-500/50 text-blue-400'
                  : 'bg-white/5 border border-white/10 hover:bg-white/10'
              }`}
            >
              {mode === 'day' ? '일간' : mode === 'week' ? '주간' : '월간'}
            </button>
          ))}
        </div>
      </div>

      <div className="fullcalendar-wrapper">
        <FullCalendar
          plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
          initialView={getCalendarView()}
          events={events}
          headerToolbar={{
            left: 'prev,next today',
            center: 'title',
            right: ''
          }}
          locale="ko"
          height="auto"
          eventTimeFormat={{
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
          }}
          slotLabelFormat={{
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
          }}
        />
      </div>

      <style>{`
        .fullcalendar-wrapper .fc {
          --fc-border-color: rgba(255, 255, 255, 0.1);
          --fc-button-bg-color: rgba(59, 130, 246, 0.2);
          --fc-button-border-color: rgba(59, 130, 246, 0.3);
          --fc-button-hover-bg-color: rgba(59, 130, 246, 0.3);
          --fc-button-hover-border-color: rgba(59, 130, 246, 0.4);
          --fc-button-active-bg-color: rgba(59, 130, 246, 0.4);
          --fc-button-active-border-color: rgba(59, 130, 246, 0.5);
          --fc-today-bg-color: rgba(59, 130, 246, 0.1);
        }

        .fullcalendar-wrapper .fc-theme-standard td,
        .fullcalendar-wrapper .fc-theme-standard th {
          border-color: rgba(255, 255, 255, 0.1);
        }

        .fullcalendar-wrapper .fc-col-header-cell {
          background-color: rgba(255, 255, 255, 0.05);
          font-weight: 600;
        }

        .fullcalendar-wrapper .fc-daygrid-day-number,
        .fullcalendar-wrapper .fc-col-header-cell-cushion {
          color: rgba(255, 255, 255, 0.8);
        }

        .fullcalendar-wrapper .fc-event {
          font-size: 0.875rem;
          padding: 2px 4px;
          border-radius: 4px;
        }

        .fullcalendar-wrapper .fc-daygrid-day.fc-day-today {
          background-color: rgba(59, 130, 246, 0.1) !important;
        }
      `}</style>
    </div>
  );
};
