import React, { useState, useEffect } from 'react';
import { DailySummaryItem } from '@/types';
import { conversationSummaryApi } from '@/lib/conversationSummaryApi';
import { ConversationHistoryModal } from './ConversationHistoryModal';

const INITIAL_DISPLAY = 5;

export const ConversationHistoryCard: React.FC = () => {
  const [summaries, setSummaries] = useState<DailySummaryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);

  useEffect(() => {
    conversationSummaryApi
      .getDailySummaries(10)
      .then(setSummaries)
      .catch(() => setSummaries([]))
      .finally(() => setLoading(false));
  }, []);

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr + 'T00:00:00');
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const target = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    const diffDays = Math.floor((today.getTime() - target.getTime()) / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return '오늘';
    if (diffDays === 1) return '어제';
    if (diffDays < 7) return `${diffDays}일 전`;

    return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
  };

  const handleOpenDetail = (date: string) => {
    setSelectedDate(date);
    setModalOpen(true);
  };

  return (
    <>
      <div className="glass p-6">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold text-white/90">대화 히스토리</h3>
          <span className="text-xs text-white/40">
            AI 일일 요약
          </span>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-8">
            <div className="w-5 h-5 border-2 border-cyan-400/30 border-t-cyan-400 rounded-full animate-spin" />
          </div>
        ) : summaries.length === 0 ? (
          <p className="text-sm text-white/40 text-center py-8">
            아직 대화 요약이 없습니다
          </p>
        ) : (
          <>
            <div className="space-y-2">
              {summaries.slice(0, INITIAL_DISPLAY).map((item) => (
                <button
                  key={item.date}
                  onClick={() => handleOpenDetail(item.date)}
                  className="w-full bg-white/5 rounded-lg p-3 border border-white/10 hover:border-cyan-400/30 hover:bg-white/[0.07] transition-all text-left group"
                >
                  <div className="flex items-center justify-between mb-1.5">
                    <span className="text-xs text-cyan-400/80 font-semibold">
                      {formatDate(item.date)}
                    </span>
                    <span className="text-xs text-white/30">
                      {item.messageCount}개 메시지
                    </span>
                  </div>
                  <p className="text-sm text-white/70 line-clamp-2 mb-1.5">
                    {item.summaryPreview}
                  </p>
                  {item.keyFactsPreview && (
                    <div className="flex flex-wrap gap-1">
                      {item.keyFactsPreview.split(', ').map((tag, idx) => (
                        <span
                          key={idx}
                          className="text-[10px] px-1.5 py-0.5 rounded-full bg-white/5 text-white/40 border border-white/5"
                        >
                          {tag}
                        </span>
                      ))}
                    </div>
                  )}
                </button>
              ))}
            </div>

            {summaries.length > INITIAL_DISPLAY && (
              <div className="mt-4 pt-4 border-t border-white/10">
                <button
                  onClick={() => handleOpenDetail(summaries[0].date)}
                  className="text-xs text-cyan-400 hover:text-cyan-300 transition-colors w-full text-center"
                >
                  전체 보기 ({summaries.length}일)
                </button>
              </div>
            )}
          </>
        )}
      </div>

      <ConversationHistoryModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        date={selectedDate}
      />
    </>
  );
};
