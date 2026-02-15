import React, { Fragment, useState, useEffect } from 'react';
import { Dialog, Transition, Tab } from '@headlessui/react';
import { DailySummaryDetail } from '@/types';
import { conversationSummaryApi } from '@/lib/conversationSummaryApi';

interface ConversationHistoryModalProps {
  isOpen: boolean;
  onClose: () => void;
  date: string | null;
}

interface KeyFact {
  key: string;
  value: string;
  type: string;
}

export const ConversationHistoryModal: React.FC<ConversationHistoryModalProps> = ({
  isOpen,
  onClose,
  date,
}) => {
  const [detail, setDetail] = useState<DailySummaryDetail | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && date) {
      setLoading(true);
      conversationSummaryApi
        .getDailySummaryDetail(date)
        .then(setDetail)
        .catch(() => setDetail(null))
        .finally(() => setLoading(false));
    }
  }, [isOpen, date]);

  const parseKeyFacts = (): KeyFact[] => {
    if (!detail?.keyFacts) return [];
    try {
      return JSON.parse(detail.keyFacts);
    } catch {
      return [];
    }
  };

  const formatDate = (d: string) => {
    const dateObj = new Date(d + 'T00:00:00');
    return dateObj.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      weekday: 'long',
    });
  };

  const formatTime = (iso: string) => {
    const d = new Date(iso);
    return d.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <Transition appear show={isOpen} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={onClose}>
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-black/70 backdrop-blur-sm" />
        </Transition.Child>

        <div className="fixed inset-0 overflow-y-auto">
          <div className="flex min-h-full items-center justify-center p-4">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 scale-95"
              enterTo="opacity-100 scale-100"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 scale-100"
              leaveTo="opacity-0 scale-95"
            >
              <Dialog.Panel className="w-full max-w-lg transform overflow-hidden rounded-2xl glass p-6 text-left align-middle shadow-xl transition-all border border-white/10">
                {/* Header */}
                <div className="flex items-center justify-between mb-4">
                  <Dialog.Title as="h3" className="text-lg font-semibold text-white flex items-center gap-2">
                    <span className="text-base">📋</span>
                    {date && formatDate(date)}
                  </Dialog.Title>
                  <button
                    onClick={onClose}
                    className="text-white/40 hover:text-white transition-colors text-xl leading-none"
                  >
                    &times;
                  </button>
                </div>

                {loading ? (
                  <div className="flex items-center justify-center py-12">
                    <div className="w-6 h-6 border-2 border-cyan-400/30 border-t-cyan-400 rounded-full animate-spin" />
                  </div>
                ) : !detail ? (
                  <p className="text-sm text-white/40 text-center py-8">
                    해당 날짜의 요약을 찾을 수 없습니다
                  </p>
                ) : (
                  <Tab.Group>
                    <Tab.List className="flex gap-1 bg-white/5 rounded-lg p-1 mb-4">
                      <Tab className={({ selected }) =>
                        `flex-1 py-2 px-3 text-sm font-medium rounded-md transition-colors ${
                          selected
                            ? 'bg-cyan-500/20 text-cyan-400'
                            : 'text-white/50 hover:text-white/70'
                        }`
                      }>
                        AI 요약
                      </Tab>
                      <Tab className={({ selected }) =>
                        `flex-1 py-2 px-3 text-sm font-medium rounded-md transition-colors ${
                          selected
                            ? 'bg-cyan-500/20 text-cyan-400'
                            : 'text-white/50 hover:text-white/70'
                        }`
                      }>
                        원본 메시지 ({detail.messageCount})
                      </Tab>
                    </Tab.List>

                    <Tab.Panels>
                      {/* AI Summary Tab */}
                      <Tab.Panel>
                        <div className="space-y-4">
                          {/* Summary Text */}
                          <div className="bg-white/5 rounded-lg p-4 border border-white/10">
                            <h4 className="text-xs text-white/40 font-semibold uppercase tracking-wider mb-2">
                              요약
                            </h4>
                            <p className="text-sm text-white/80 leading-relaxed whitespace-pre-line">
                              {detail.summaryText}
                            </p>
                          </div>

                          {/* Key Facts */}
                          {parseKeyFacts().length > 0 && (
                            <div className="bg-white/5 rounded-lg p-4 border border-white/10">
                              <h4 className="text-xs text-white/40 font-semibold uppercase tracking-wider mb-3">
                                핵심 사실
                              </h4>
                              <div className="space-y-2">
                                {parseKeyFacts().map((fact, idx) => (
                                  <div key={idx} className="flex items-start gap-2">
                                    <span className={`text-xs px-1.5 py-0.5 rounded-full shrink-0 mt-0.5 ${
                                      fact.type === 'PROMISE'
                                        ? 'bg-amber-500/20 text-amber-400'
                                        : fact.type === 'PREFERENCE'
                                        ? 'bg-purple-500/20 text-purple-400'
                                        : 'bg-cyan-500/20 text-cyan-400'
                                    }`}>
                                      {fact.type === 'PROMISE' ? '약속' : fact.type === 'PREFERENCE' ? '선호' : '사실'}
                                    </span>
                                    <div>
                                      <span className="text-sm text-white/70 font-medium">{fact.key}</span>
                                      <span className="text-sm text-white/50"> - {fact.value}</span>
                                    </div>
                                  </div>
                                ))}
                              </div>
                            </div>
                          )}

                          <div className="text-xs text-white/30 text-center">
                            {detail.messageCount}개 메시지 기반
                          </div>
                        </div>
                      </Tab.Panel>

                      {/* Original Messages Tab */}
                      <Tab.Panel>
                        <div className="space-y-2 max-h-96 overflow-y-auto pr-1 custom-scrollbar">
                          {detail.messages.map((msg) => (
                            <div
                              key={msg.id}
                              className={`rounded-lg p-3 ${
                                msg.role === 'user'
                                  ? 'bg-cyan-500/10 border border-cyan-500/20 ml-6'
                                  : 'bg-purple-500/10 border border-purple-500/20 mr-6'
                              }`}
                            >
                              <div className="flex items-center gap-2 mb-1">
                                <div className={`w-2 h-2 rounded-full ${
                                  msg.role === 'user' ? 'bg-cyan-400' : 'bg-purple-400'
                                }`} />
                                <span className="text-xs text-white/50 font-semibold">
                                  {msg.role === 'user' ? '나' : (msg.personaName || 'Lobi')}
                                </span>
                                <span className="text-xs text-white/30 ml-auto">
                                  {formatTime(msg.createdAt)}
                                </span>
                              </div>
                              <p className="text-sm text-white/80 whitespace-pre-line">
                                {msg.content}
                              </p>
                            </div>
                          ))}
                        </div>
                      </Tab.Panel>
                    </Tab.Panels>
                  </Tab.Group>
                )}
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition>
  );
};
