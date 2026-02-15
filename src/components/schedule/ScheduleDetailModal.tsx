import React, { Fragment } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { useChatStore } from '@/stores/chatStore';
import { ScheduleEvent } from '@/types';
import { format } from 'date-fns';
import toast from 'react-hot-toast';

interface ScheduleDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  schedule: ScheduleEvent | null;
}

const typeLabels: Record<string, string> = {
  REMINDER: '알림/리마인더',
  INTERACTION: 'AI 상호작용',
  EVENT: '일반 이벤트'
};

const typeIcons: Record<string, string> = {
  REMINDER: '🔔',
  INTERACTION: '🤖',
  EVENT: '📅'
};

export const ScheduleDetailModal: React.FC<ScheduleDetailModalProps> = ({
  isOpen,
  onClose,
  schedule
}) => {
  const { updateSchedule, deleteSchedule } = useChatStore();
  const [isProcessing, setIsProcessing] = React.useState(false);

  if (!schedule) return null;

  const startDate = new Date(schedule.startTime);
  const endDate = new Date(schedule.endTime);

  const handleComplete = async () => {
    setIsProcessing(true);
    try {
      await updateSchedule(schedule.id, { isCompleted: true } as any);
      toast.success('일정이 완료 처리되었습니다!');
      onClose();
    } catch (error) {
      console.error('Failed to complete schedule:', error);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleDelete = async () => {
    setIsProcessing(true);
    try {
      await deleteSchedule(schedule.id);
      onClose();
    } catch (error) {
      console.error('Failed to delete schedule:', error);
    } finally {
      setIsProcessing(false);
    }
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
              <Dialog.Panel className="w-full max-w-sm transform overflow-hidden rounded-2xl glass p-6 text-left align-middle shadow-xl transition-all border border-white/10">
                {/* Header */}
                <div className="flex items-center justify-between mb-4">
                  <Dialog.Title
                    as="h3"
                    className={`text-lg font-semibold leading-6 flex items-center gap-2 ${
                      schedule.isCompleted ? 'text-white/50 line-through' : 'text-white'
                    }`}
                  >
                    <span>{typeIcons[schedule.type]}</span>
                    <span>{schedule.title}</span>
                  </Dialog.Title>
                  <button
                    onClick={onClose}
                    className="text-white/40 hover:text-white transition-colors text-xl leading-none"
                  >
                    &times;
                  </button>
                </div>

                {/* Details */}
                <div className="space-y-3 mb-6">
                  {/* Time */}
                  <div className="flex items-center gap-3 text-sm text-white/70">
                    <span className="text-base">🕐</span>
                    <span>{format(startDate, 'HH:mm')} ~ {format(endDate, 'HH:mm')}</span>
                  </div>

                  {/* Description */}
                  {schedule.description && (
                    <div className="flex items-start gap-3 text-sm text-white/70">
                      <span className="text-base">📍</span>
                      <span>{schedule.description}</span>
                    </div>
                  )}

                  {/* Type */}
                  <div className="flex items-center gap-3 text-sm text-white/70">
                    <span className="text-base">📋</span>
                    <span>유형: {typeLabels[schedule.type]}</span>
                  </div>

                  {/* Status */}
                  <div className="flex items-center gap-3 text-sm">
                    <span className="text-base">{schedule.isCompleted ? '✅' : '⏳'}</span>
                    <span className={schedule.isCompleted ? 'text-green-400' : 'text-yellow-400'}>
                      상태: {schedule.isCompleted ? '완료' : '미완료'}
                    </span>
                  </div>
                </div>

                {/* Actions */}
                {!schedule.isCompleted && (
                  <div className="flex gap-3">
                    <button
                      onClick={handleComplete}
                      disabled={isProcessing}
                      className="flex-1 px-4 py-2 text-sm font-medium text-white bg-green-600 hover:bg-green-700 disabled:bg-green-600/50 rounded-lg transition-colors"
                    >
                      {isProcessing ? '처리 중...' : '완료 처리'}
                    </button>
                    <button
                      onClick={handleDelete}
                      disabled={isProcessing}
                      className="flex-1 px-4 py-2 text-sm font-medium text-white bg-red-600/80 hover:bg-red-600 disabled:bg-red-600/40 rounded-lg transition-colors"
                    >
                      {isProcessing ? '처리 중...' : '삭제'}
                    </button>
                  </div>
                )}

                {schedule.isCompleted && (
                  <div className="flex gap-3">
                    <button
                      onClick={handleDelete}
                      disabled={isProcessing}
                      className="flex-1 px-4 py-2 text-sm font-medium text-white/60 hover:text-white bg-white/5 hover:bg-white/10 rounded-lg transition-colors"
                    >
                      {isProcessing ? '처리 중...' : '삭제'}
                    </button>
                  </div>
                )}
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition>
  );
};
