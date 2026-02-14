import React from 'react';
import { Menu, MenuButton, MenuItems, MenuItem } from '@headlessui/react';
import { useChatStore } from '@/stores/chatStore';

export const PersonaDropdown: React.FC = () => {
  const { personas, currentPersona, changePersona, isLoading } = useChatStore();

  if (personas.length === 0) return null;

  return (
    <Menu as="div" className="relative glass p-4 rounded-2xl">
      {/* Trigger Button */}
      <MenuButton className="w-full flex items-center justify-between gap-3 text-left hover:bg-white/5 transition-all rounded-lg p-2">
        <div className="flex items-center gap-2">
          <span className="text-xl">{currentPersona?.iconEmoji}</span>
          <div className="flex-1">
            <div className="text-sm font-medium">{currentPersona?.displayName}</div>
            <div className="text-xs opacity-60">{currentPersona?.name}</div>
          </div>
        </div>
        <svg
          className="w-4 h-4 opacity-60"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M19 9l-7 7-7-7"
          />
        </svg>
      </MenuButton>

      {/* Dropdown Menu */}
      <MenuItems className="absolute left-0 right-0 mt-2 glass rounded-xl overflow-hidden border border-white/10 z-10 shadow-2xl">
        {personas.map((persona) => {
          const isLobbyMaster = persona.nameEn === 'lobby_master';
          const isDisabled = !isLobbyMaster;
          const isCurrent = persona.id === currentPersona?.id;

          return (
            <MenuItem key={persona.id} disabled={isDisabled}>
              {({ focus }) => (
                <button
                  onClick={() => !isDisabled && changePersona(persona.id)}
                  disabled={isLoading || isDisabled}
                  className={`
                    w-full flex items-center gap-3 px-4 py-3 transition-all
                    ${focus && !isDisabled ? 'bg-white/10' : ''}
                    ${isCurrent ? 'bg-blue-500/20 border-l-4 border-blue-500' : ''}
                    ${isDisabled ? 'opacity-40 cursor-not-allowed' : 'cursor-pointer'}
                    ${isLoading ? 'pointer-events-none' : ''}
                  `}
                >
                  <span className="text-xl">{persona.iconEmoji}</span>
                  <div className="flex-1 text-left">
                    <div className="text-sm font-medium">{persona.displayName}</div>
                    <div className="text-xs opacity-60">{persona.name}</div>
                  </div>
                  {isDisabled && (
                    <span className="text-xs opacity-60 bg-white/5 px-2 py-1 rounded">
                      준비 중
                    </span>
                  )}
                  {isCurrent && !isDisabled && (
                    <div className="w-2 h-2 bg-blue-500 rounded-full animate-pulse" />
                  )}
                </button>
              )}
            </MenuItem>
          );
        })}
      </MenuItems>
    </Menu>
  );
};
