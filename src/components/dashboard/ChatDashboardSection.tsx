import React from 'react';
import { UserLevelCard } from './UserLevelCard';
import { ConversationHistoryCard } from './ConversationHistoryCard';
import { WeeklyScheduleView } from '@/components/schedule/WeeklyScheduleView';

export const ChatDashboardSection: React.FC = () => {
  return (
    <div>
      <h2 className="text-2xl font-bold mb-6 text-white/90">대시보드</h2>

      {/* Top 2-column grid */}
      <div className="grid lg:grid-cols-2 gap-6 mb-8">
        <UserLevelCard />
        <ConversationHistoryCard />
      </div>

      {/* Full-width weekly schedule */}
      <WeeklyScheduleView />
    </div>
  );
};
