import React from 'react';
import { useChatStore } from '@/stores/chatStore';

export const UserLevelCard: React.FC = () => {
  const { stats, messages } = useChatStore();

  // Calculate level based on happiness (0-100 -> Level 1-10)
  const level = Math.floor(stats.happiness / 10) + 1;
  const currentLevelMin = (level - 1) * 10;
  const nextLevelMin = level * 10;
  const progressInLevel = stats.happiness - currentLevelMin;
  const progressPercentage = (progressInLevel / 10) * 100;

  // Count user messages (activity)
  const userMessageCount = messages.filter(msg => msg.role === 'user').length;

  // Calculate activity days (placeholder - would need backend support)
  const activityDays = 1; // Placeholder

  return (
    <div className="glass p-6">
      <h3 className="text-lg font-semibold mb-4 text-white/90">사용자 레벨</h3>

      {/* Level Display */}
      <div className="flex items-center justify-center mb-6">
        <div className="relative">
          <div className="w-24 h-24 rounded-full bg-gradient-to-br from-cyan-500 to-purple-600 flex items-center justify-center">
            <div className="w-20 h-20 rounded-full bg-gray-900 flex items-center justify-center">
              <span className="text-2xl font-bold text-white">Lv.{level}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Progress Bar */}
      <div className="mb-6">
        <div className="flex justify-between text-xs text-white/60 mb-2">
          <span>Lv.{level}</span>
          <span>Lv.{level + 1}</span>
        </div>
        <div className="w-full h-2 bg-white/10 rounded-full overflow-hidden">
          <div
            className="h-full bg-gradient-to-r from-cyan-500 to-purple-600 transition-all duration-300"
            style={{ width: `${progressPercentage}%` }}
          />
        </div>
        <p className="text-xs text-white/50 mt-2 text-center">
          다음 레벨까지 {nextLevelMin - stats.happiness}XP
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 gap-4">
        <div className="bg-white/5 rounded-lg p-3">
          <p className="text-xs text-white/60 mb-1">총 메시지</p>
          <p className="text-xl font-bold text-cyan-400">{userMessageCount}</p>
        </div>
        <div className="bg-white/5 rounded-lg p-3">
          <p className="text-xs text-white/60 mb-1">활동 일수</p>
          <p className="text-xl font-bold text-purple-400">{activityDays}</p>
        </div>
      </div>
    </div>
  );
};
