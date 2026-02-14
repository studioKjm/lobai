import React, { Fragment, useState, useEffect } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { useChatStore } from '@/stores/chatStore';
import { ScheduleEvent } from '@/types';
import toast from 'react-hot-toast';

interface ScheduleFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  schedule?: ScheduleEvent | null; // null이면 생성, 있으면 수정
  defaultDate?: string; // 기본 날짜 (YYYY-MM-DD)
}

export const ScheduleFormModal: React.FC<ScheduleFormModalProps> = ({
  isOpen,
  onClose,
  schedule,
  defaultDate
}) => {
  const { addSchedule, updateSchedule } = useChatStore();
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Form state
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [date, setDate] = useState('');
  const [startTime, setStartTime] = useState('09:00');
  const [endTime, setEndTime] = useState('10:00');
  const [type, setType] = useState<'REMINDER' | 'INTERACTION' | 'EVENT'>('EVENT');

  // Initialize form
  useEffect(() => {
    if (schedule) {
      // 수정 모드
      setTitle(schedule.title);
      setDescription(schedule.description || '');
      const start = new Date(schedule.startTime);
      setDate(start.toISOString().split('T')[0]);
      setStartTime(start.toTimeString().slice(0, 5));
      const end = new Date(schedule.endTime);
      setEndTime(end.toTimeString().slice(0, 5));
      setType(schedule.type);
    } else {
      // 생성 모드
      setTitle('');
      setDescription('');
      setDate(defaultDate || new Date().toISOString().split('T')[0]);
      setStartTime('09:00');
      setEndTime('10:00');
      setType('EVENT');
    }
  }, [schedule, defaultDate, isOpen]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      const startDateTime = `${date}T${startTime}:00`;
      const endDateTime = `${date}T${endTime}:00`;

      if (schedule) {
        // 수정
        await updateSchedule(schedule.id, {
          title,
          description: description || undefined,
          startTime: startDateTime,
          endTime: endDateTime,
          type
        });
      } else {
        // 생성
        await addSchedule({
          title,
          description: description || undefined,
          startTime: startDateTime,
          endTime: endDateTime,
          type
        });
      }

      onClose();
    } catch (error) {
      console.error('Failed to save schedule:', error);
    } finally {
      setIsSubmitting(false);
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
              <Dialog.Panel className="w-full max-w-md transform overflow-hidden rounded-2xl glass p-6 text-left align-middle shadow-xl transition-all border border-white/10">
                <Dialog.Title
                  as="h3"
                  className="text-lg font-semibold leading-6 text-white mb-4"
                >
                  {schedule ? '일정 수정' : '일정 추가'}
                </Dialog.Title>

                <form onSubmit={handleSubmit} className="space-y-4">
                  {/* 제목 */}
                  <div>
                    <label className="block text-sm font-medium text-white/70 mb-2">
                      제목 *
                    </label>
                    <input
                      type="text"
                      value={title}
                      onChange={(e) => setTitle(e.target.value)}
                      required
                      placeholder="일정 제목"
                      className="w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-white placeholder:text-white/30 focus:outline-none focus:ring-2 focus:ring-blue-500/50"
                    />
                  </div>

                  {/* 설명 */}
                  <div>
                    <label className="block text-sm font-medium text-white/70 mb-2">
                      설명
                    </label>
                    <textarea
                      value={description}
                      onChange={(e) => setDescription(e.target.value)}
                      placeholder="일정 설명 (선택사항)"
                      rows={2}
                      className="w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-white placeholder:text-white/30 focus:outline-none focus:ring-2 focus:ring-blue-500/50 resize-none"
                    />
                  </div>

                  {/* 날짜 */}
                  <div>
                    <label className="block text-sm font-medium text-white/70 mb-2">
                      날짜 *
                    </label>
                    <input
                      type="date"
                      value={date}
                      onChange={(e) => setDate(e.target.value)}
                      required
                      className="w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50"
                    />
                  </div>

                  {/* 시간 */}
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-white/70 mb-2">
                        시작 시간 *
                      </label>
                      <input
                        type="time"
                        value={startTime}
                        onChange={(e) => setStartTime(e.target.value)}
                        required
                        className="w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-white/70 mb-2">
                        종료 시간 *
                      </label>
                      <input
                        type="time"
                        value={endTime}
                        onChange={(e) => setEndTime(e.target.value)}
                        required
                        className="w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50"
                      />
                    </div>
                  </div>

                  {/* 유형 */}
                  <div>
                    <label className="block text-sm font-medium text-white/70 mb-2">
                      유형
                    </label>
                    <select
                      value={type}
                      onChange={(e) => setType(e.target.value as any)}
                      className="w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50"
                    >
                      <option value="EVENT">일반 이벤트</option>
                      <option value="REMINDER">알림/리마인더</option>
                      <option value="INTERACTION">AI 상호작용</option>
                    </select>
                  </div>

                  {/* 버튼 */}
                  <div className="flex gap-3 mt-6">
                    <button
                      type="button"
                      onClick={onClose}
                      className="flex-1 px-4 py-2 text-sm font-medium text-white/60 hover:text-white bg-white/5 hover:bg-white/10 rounded-lg transition-colors"
                    >
                      취소
                    </button>
                    <button
                      type="submit"
                      disabled={isSubmitting}
                      className="flex-1 px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:bg-blue-600/50 rounded-lg transition-colors"
                    >
                      {isSubmitting ? '저장 중...' : (schedule ? '수정' : '추가')}
                    </button>
                  </div>
                </form>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition>
  );
};
