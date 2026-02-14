import React from 'react';
import { useChatStore } from '@/stores/chatStore';

export const ChatHistoryCard: React.FC = () => {
  const { messages, clearMessageHistory } = useChatStore();

  // Group messages by date (simplified - all today for now)
  const today = new Date().toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });

  const recentMessages = messages.slice(-5); // Last 5 messages

  return (
    <div className="glass p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-white/90">최근 대화</h3>
        <button
          onClick={clearMessageHistory}
          className="text-xs text-white/40 hover:text-red-400 transition-colors"
        >
          전체 삭제
        </button>
      </div>

      {/* Date Header */}
      <div className="mb-4">
        <p className="text-xs text-white/40 font-semibold uppercase tracking-wider">{today}</p>
      </div>

      {/* Message List */}
      <div className="space-y-3">
        {recentMessages.length === 0 ? (
          <p className="text-sm text-white/40 text-center py-8">
            아직 대화 내역이 없습니다
          </p>
        ) : (
          recentMessages.map((msg, idx) => (
            <div
              key={idx}
              className="bg-white/5 rounded-lg p-3 border border-white/10 hover:border-white/20 transition-colors"
            >
              <div className="flex items-center gap-2 mb-1">
                <div className={`w-2 h-2 rounded-full ${
                  msg.role === 'user' ? 'bg-cyan-400' : 'bg-purple-400'
                }`} />
                <span className="text-xs text-white/60 font-semibold">
                  {msg.role === 'user' ? '나' : 'Lobi'}
                </span>
              </div>
              <p className="text-sm text-white/80 line-clamp-2">
                {msg.content}
              </p>
            </div>
          ))
        )}
      </div>

      {/* View All Link */}
      {messages.length > 5 && (
        <div className="mt-4 pt-4 border-t border-white/10">
          <button className="text-xs text-cyan-400 hover:text-cyan-300 transition-colors w-full text-center">
            전체 보기 ({messages.length}개)
          </button>
        </div>
      )}
    </div>
  );
};
