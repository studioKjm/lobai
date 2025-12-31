import React from 'react';
import { ActionType } from '@/types';

interface ActionButtonsProps {
  onAction: (type: ActionType) => void;
}

export const ActionButtons: React.FC<ActionButtonsProps> = ({ onAction }) => {
  return (
    <div className="flex flex-col gap-3">
      <button
        onClick={() => onAction('hunger')}
        className="glass py-4 hover:bg-white/10 transition-all text-sm font-medium tracking-wide"
      >
        FEED UNIT
      </button>
      <button
        onClick={() => onAction('happiness')}
        className="glass py-4 hover:bg-white/10 transition-all text-sm font-medium tracking-wide"
      >
        INITIATE PLAY
      </button>
      <button
        onClick={() => onAction('energy')}
        className="glass py-4 hover:bg-white/10 transition-all text-sm font-medium tracking-wide"
      >
        SLEEP MODE
      </button>
    </div>
  );
};
