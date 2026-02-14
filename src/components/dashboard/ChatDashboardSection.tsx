import React from 'react';
import { UserLevelCard } from './UserLevelCard';
import { ChatHistoryCard } from './ChatHistoryCard';
import { WeeklyScheduleView } from '@/components/schedule/WeeklyScheduleView';
import { LobCoinShop } from '@/components/chat/LobCoinShop';

export const ChatDashboardSection: React.FC = () => {
  return (
    <div>
      <h2 className="text-2xl font-bold mb-6 text-white/90">대시보드</h2>

      {/* Top 2-column grid */}
      <div className="grid lg:grid-cols-2 gap-6 mb-8">
        <UserLevelCard />
        <ChatHistoryCard />
      </div>

      {/* Full-width weekly schedule */}
      <WeeklyScheduleView />

      {/* LobCoin Shop */}
      <div className="mt-8">
        <LobCoinShop />
      </div>
    </div>
  );
};
