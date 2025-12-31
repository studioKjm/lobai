import { Fragment, useState } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { LoginForm } from './LoginForm';
import { RegisterForm } from './RegisterForm';

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialMode?: 'login' | 'register';
}

export function AuthModal({ isOpen, onClose, initialMode = 'login' }: AuthModalProps) {
  const [mode, setMode] = useState<'login' | 'register'>(initialMode);

  return (
    <Transition appear show={isOpen} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={onClose}>
        {/* Backdrop */}
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-black/50 backdrop-blur-sm" />
        </Transition.Child>

        {/* Modal container */}
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
              <Dialog.Panel className="glass w-full max-w-md p-8 relative">
                {/* Close button */}
                <button
                  onClick={onClose}
                  className="absolute top-4 right-4 p-2 hover:bg-white/10 rounded-lg transition-colors"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>

                {/* Title */}
                <Dialog.Title className="text-2xl font-display font-bold mb-6 gradient-text">
                  {mode === 'login' ? '로그인' : '회원가입'}
                </Dialog.Title>

                {/* Tab buttons */}
                <div className="flex gap-2 mb-6">
                  <button
                    onClick={() => setMode('login')}
                    className={`flex-1 py-2 px-4 rounded-lg font-medium transition-all ${
                      mode === 'login'
                        ? 'bg-gradient-to-r from-cyan-500 to-blue-600 text-white'
                        : 'bg-white/5 text-white/60 hover:bg-white/10'
                    }`}
                  >
                    로그인
                  </button>
                  <button
                    onClick={() => setMode('register')}
                    className={`flex-1 py-2 px-4 rounded-lg font-medium transition-all ${
                      mode === 'register'
                        ? 'bg-gradient-to-r from-cyan-500 to-blue-600 text-white'
                        : 'bg-white/5 text-white/60 hover:bg-white/10'
                    }`}
                  >
                    회원가입
                  </button>
                </div>

                {/* Form */}
                {mode === 'login' ? (
                  <LoginForm onSuccess={onClose} />
                ) : (
                  <RegisterForm onSuccess={onClose} />
                )}
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition>
  );
}
