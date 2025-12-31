import React, { useState, useEffect } from 'react';
import { Navbar } from '@/components/common/Navbar';
import { StatsPanel } from '@/components/chat/StatsPanel';
import { ActionButtons } from '@/components/chat/ActionButtons';
import { PersonaSelector } from '@/components/chat/PersonaSelector';
import { SplineCharacter } from '@/components/chat/SplineCharacter';
import { ChatInterface } from '@/components/chat/ChatInterface';
import { useSplineLoader } from '@/hooks/useSplineLoader';
import { useChatStore } from '@/stores/chatStore';
import type { ActionType } from '@/types';

export const ChatPage: React.FC = () => {
  // Chat Store
  const {
    stats,
    messages,
    isTyping,
    loadStats,
    loadMessages,
    loadPersonas,
    updateStats,
    sendMessage
  } = useChatStore();

  // Local UI state
  const [inputValue, setInputValue] = useState('');

  // Hooks
  const { splineReady, onSplineLoad, handleCharacterClick } = useSplineLoader();

  // Load initial data from backend
  useEffect(() => {
    loadStats();
    loadMessages();
    loadPersonas();
  }, [loadStats, loadMessages, loadPersonas]);

  // Prevent auto-scroll on mount
  useEffect(() => {
    if (window.location.hash) {
      history.replaceState(null, '', window.location.pathname + window.location.search);
    }
  }, []);

  // Handle action buttons (Feed, Play, Sleep)
  const handleAction = (type: ActionType) => {
    updateStats(type);
  };

  // Handle message send
  const handleSendMessage = async () => {
    if (!inputValue.trim() || isTyping) return;

    const content = inputValue;
    setInputValue('');
    await sendMessage(content);
  };

  return (
    <div className="relative w-full">
      <Navbar />

      <section className="relative min-h-screen w-full flex flex-col items-center justify-center p-6 sm:p-12 pt-32">
        {/* Background Decor */}
        <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-blue-900/10 rounded-full blur-[120px]" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-purple-900/10 rounded-full blur-[120px]" />

        <div className="relative w-full max-w-6xl flex flex-col lg:flex-row gap-8 items-center justify-between">

          {/* Left: Stats & Actions */}
          <div className="w-full lg:w-1/4 flex flex-col gap-6 order-2 lg:order-1">
            <StatsPanel stats={stats} />
            <ActionButtons onAction={handleAction} />
            <PersonaSelector />
          </div>

          {/* Center: 3D Character */}
          <SplineCharacter
            onLoad={onSplineLoad}
            onClick={handleCharacterClick}
            splineReady={splineReady}
          />

          {/* Right: Chat */}
          <ChatInterface
            messages={messages}
            inputValue={inputValue}
            isTyping={isTyping}
            onInputChange={setInputValue}
            onSendMessage={handleSendMessage}
          />
        </div>
      </section>
    </div>
  );
};
