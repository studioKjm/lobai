import React from 'react';
import { useChatStore } from '@/stores/chatStore';

export const PersonaSelector: React.FC = () => {
  const { personas, currentPersona, changePersona, isLoading } = useChatStore();

  if (personas.length === 0) return null;

  return (
    <div className="glass p-6 rounded-2xl">
      <h3 className="text-lg font-semibold mb-4 gradient-text">페르소나 선택</h3>

      <div className="grid grid-cols-1 gap-3">
        {personas.map(persona => (
          <button
            key={persona.id}
            onClick={() => changePersona(persona.id)}
            disabled={isLoading || currentPersona?.id === persona.id}
            className={`
              flex items-center gap-3 p-3 rounded-xl transition-all
              ${currentPersona?.id === persona.id
                ? 'bg-blue-500/20 border-2 border-blue-500/50'
                : 'bg-white/5 border border-white/10 hover:bg-white/10'
              }
              ${isLoading ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}
            `}
          >
            <span className="text-2xl">{persona.iconEmoji}</span>
            <div className="flex-1 text-left">
              <div className="font-medium text-sm">{persona.displayName}</div>
              <div className="text-xs opacity-60">{persona.name}</div>
            </div>
            {currentPersona?.id === persona.id && (
              <div className="w-2 h-2 bg-blue-500 rounded-full animate-pulse" />
            )}
          </button>
        ))}
      </div>
    </div>
  );
};
