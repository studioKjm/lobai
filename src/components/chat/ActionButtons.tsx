import React, { useEffect, useState } from 'react';
import { ActionType } from '@/types';

interface ActionButtonsProps {
  onAction: (type: ActionType) => void;
  isSleeping?: boolean;
  onWake?: () => void;
  playButtonPulse?: boolean;
}

export const ActionButtons: React.FC<ActionButtonsProps> = ({
  onAction,
  isSleeping = false,
  onWake,
  playButtonPulse = false
}) => {
  const [isPlayAnimating, setIsPlayAnimating] = useState(false);

  // Trigger animation when playButtonPulse changes to true
  useEffect(() => {
    if (playButtonPulse) {
      setIsPlayAnimating(true);
      const timer = setTimeout(() => {
        setIsPlayAnimating(false);
      }, 400);
      return () => clearTimeout(timer);
    }
  }, [playButtonPulse]);

  const handleSleepClick = () => {
    if (isSleeping && onWake) {
      onWake();
    } else {
      onAction('sleep');
    }
  };

  return (
    <div className="flex flex-col gap-3">
      <button
        onClick={() => onAction('feed')}
        disabled={isSleeping}
        className={`glass py-4 transition-all text-sm font-medium tracking-wide ${
          isSleeping
            ? 'opacity-50 cursor-not-allowed'
            : 'hover:bg-white/10 active:scale-[0.98]'
        }`}
      >
        먹이주기
      </button>
      <button
        onClick={() => onAction('play')}
        disabled={isSleeping}
        className={`glass py-4 transition-all text-sm font-medium tracking-wide ${
          isSleeping
            ? 'opacity-50 cursor-not-allowed'
            : 'hover:bg-white/10 active:scale-[0.98]'
        } ${isPlayAnimating ? 'animate-button-pulse bg-pink-500/20 text-pink-300' : ''}`}
      >
        놀아주기
      </button>
      <button
        onClick={handleSleepClick}
        className={`glass py-4 hover:bg-white/10 transition-all text-sm font-medium tracking-wide active:scale-[0.98] ${
          isSleeping ? 'bg-white/10 text-yellow-300' : ''
        }`}
      >
        {isSleeping ? '깨우기' : '잠자기'}
      </button>
    </div>
  );
};
