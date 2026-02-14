import React, { Fragment } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { useChatStore } from '@/stores/chatStore';

interface PersonaModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const PersonaModal: React.FC<PersonaModalProps> = ({ isOpen, onClose }) => {
  const { personas, currentPersona, changePersona, isLoading } = useChatStore();

  const handleSelectPersona = async (personaId: number) => {
    await changePersona(personaId);
    onClose();
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
          <div className="fixed inset-0 bg-black/60 backdrop-blur-sm" />
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
                  대화 모드 선택
                </Dialog.Title>

                <div className="space-y-3">
                  {personas.map((persona) => {
                    const isLobbyMaster = persona.nameEn === 'lobby_master';
                    const isDisabled = !isLobbyMaster;
                    const isSelected = currentPersona?.id === persona.id;

                    return (
                      <button
                        key={persona.id}
                        onClick={() => !isDisabled && handleSelectPersona(persona.id)}
                        disabled={isDisabled || isLoading}
                        className={`
                          w-full flex items-center justify-between gap-3 p-4 rounded-xl
                          transition-all duration-200
                          ${isSelected
                            ? 'bg-blue-500/20 border-2 border-blue-500/50'
                            : 'bg-white/5 border border-white/10 hover:bg-white/10'
                          }
                          ${isDisabled
                            ? 'opacity-40 cursor-not-allowed'
                            : 'cursor-pointer'
                          }
                        `}
                      >
                        <div className="flex items-center gap-3">
                          <span className="text-2xl">{persona.iconEmoji}</span>
                          <div className="text-left">
                            <div className="text-sm font-medium text-white">
                              {persona.displayName}
                            </div>
                            <div className="text-xs text-white/50 mt-0.5">
                              {persona.name}
                            </div>
                          </div>
                        </div>

                        {isDisabled && (
                          <span className="text-xs text-white/40 bg-white/10 px-2 py-1 rounded">
                            준비 중
                          </span>
                        )}

                        {isSelected && !isDisabled && (
                          <svg className="w-5 h-5 text-blue-400" fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                          </svg>
                        )}
                      </button>
                    );
                  })}
                </div>

                <div className="mt-6 flex justify-end">
                  <button
                    type="button"
                    className="px-4 py-2 text-sm font-medium text-white/60 hover:text-white bg-white/5 hover:bg-white/10 rounded-lg transition-colors"
                    onClick={onClose}
                  >
                    닫기
                  </button>
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition>
  );
};
