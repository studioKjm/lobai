import React, { useState, useEffect, useRef } from 'react';
import { Navbar } from '@/components/common/Navbar';
import { StatsPanel } from '@/components/chat/StatsPanel';
import { ActionButtons } from '@/components/chat/ActionButtons';
import { SidebarSchedule } from '@/components/schedule/SidebarSchedule';
import { SplineCharacter } from '@/components/chat/SplineCharacter';
import { ChatInterface } from '@/components/chat/ChatInterface';
import { SleepOverlay } from '@/components/chat/SleepOverlay';
import { PlayOverlay } from '@/components/chat/PlayOverlay';
import { AttendanceCard } from '@/components/attendance/AttendanceCard';
import { ChatDashboardSection } from '@/components/dashboard/ChatDashboardSection';
import { LobCoinBalance } from '@/components/chat/LobCoinBalance';
import { UserLevelBadge } from '@/components/chat/UserLevelBadge';
import { useSplineLoader } from '@/hooks/useSplineLoader';
import { useChatStore } from '@/stores/chatStore';
import type { ActionType } from '@/types';

export const ChatPage: React.FC = () => {
  // Chat Store
  const {
    stats,
    messages,
    isTyping,
    isStreaming,
    loadStats,
    loadMessages,
    loadPersonas,
    updateStats,
    sendMessage,
    sendMessageStream,
    sleepTick,
    clearMessageHistory
  } = useChatStore();

  // Local UI state
  const [inputValue, setInputValue] = useState('');
  const [isPlaying, setIsPlaying] = useState(false);
  const [isReacting, setIsReacting] = useState(false);
  const [playButtonPulse, setPlayButtonPulse] = useState(false);
  const [isChatExpanded, setIsChatExpanded] = useState(false);
  const sleepTimerRef = useRef<NodeJS.Timeout | null>(null);
  const sleepTickIntervalRef = useRef<NodeJS.Timeout | null>(null);
  const playTimerRef = useRef<NodeJS.Timeout | null>(null);
  const reactionTimerRef = useRef<NodeJS.Timeout | null>(null);
  const playPulseCounterRef = useRef(0);

  // Hooks
  const { splineReady, onSplineLoad, isSleeping, startSleep, endSleep, resetCamera, setSplineState, characterState } = useSplineLoader();

  // Auto-update character expression based on stats
  useEffect(() => {
    if (!splineReady || isSleeping || isPlaying || isReacting) return;

    const isAnySadStat = stats.hunger <= 30 || stats.energy <= 30 || stats.happiness <= 30;
    const targetState = isAnySadStat ? 'cry' : 'State';

    if (characterState !== targetState) {
      setSplineState(targetState);
    }
  }, [stats.hunger, stats.energy, stats.happiness, splineReady, isSleeping, isPlaying, isReacting, characterState, setSplineState]);

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

  // Cleanup timers on unmount
  useEffect(() => {
    return () => {
      if (sleepTimerRef.current) {
        clearTimeout(sleepTimerRef.current);
      }
      if (sleepTickIntervalRef.current) {
        clearInterval(sleepTickIntervalRef.current);
      }
      if (playTimerRef.current) {
        clearTimeout(playTimerRef.current);
      }
      if (reactionTimerRef.current) {
        clearTimeout(reactionTimerRef.current);
      }
    };
  }, []);

  // Sleep tick interval - update stats every second while sleeping
  useEffect(() => {
    if (isSleeping) {
      // Start interval when sleeping begins
      sleepTickIntervalRef.current = setInterval(() => {
        sleepTick();
      }, 1000);
    } else {
      // Clear interval when sleeping ends
      if (sleepTickIntervalRef.current) {
        clearInterval(sleepTickIntervalRef.current);
        sleepTickIntervalRef.current = null;
      }
    }

    return () => {
      if (sleepTickIntervalRef.current) {
        clearInterval(sleepTickIntervalRef.current);
      }
    };
  }, [isSleeping, sleepTick]);

  // Trigger play effect
  const triggerPlayEffect = () => {
    // Show heart particles
    setIsPlaying(true);

    // Clear any existing timer
    if (playTimerRef.current) {
      clearTimeout(playTimerRef.current);
    }

    // Hide hearts after animation completes
    playTimerRef.current = setTimeout(() => {
      setIsPlaying(false);
      playTimerRef.current = null;
    }, 1500);

    // Trigger button pulse animation
    playPulseCounterRef.current += 1;
    setPlayButtonPulse(true);
    setTimeout(() => {
      setPlayButtonPulse(false);
    }, 50);
  };

  // Trigger happy reaction (smile) for feed/play actions
  const triggerHappyReaction = () => {
    // Clear any existing reaction timer
    if (reactionTimerRef.current) {
      clearTimeout(reactionTimerRef.current);
    }

    // Set reacting state and show smile
    setIsReacting(true);
    setSplineState('State'); // 'State' is the happy/normal expression

    // Return to stats-based expression after 1.5 seconds
    reactionTimerRef.current = setTimeout(() => {
      setIsReacting(false);
      reactionTimerRef.current = null;
    }, 1500);
  };

  // Handle action buttons (Feed, Play, Sleep)
  const handleAction = (type: ActionType) => {
    updateStats(type);

    // Trigger play effect when Play button is clicked
    if (type === 'play') {
      triggerPlayEffect();
      triggerHappyReaction();
    }

    // Trigger happy reaction when Feed button is clicked
    if (type === 'feed') {
      triggerHappyReaction();
    }

    // Trigger sleep expression when Sleep button is clicked
    if (type === 'sleep') {
      startSleep();
      // Clear any existing timer
      if (sleepTimerRef.current) {
        clearTimeout(sleepTimerRef.current);
      }
      // End sleep after 15 seconds (click character to stop early)
      sleepTimerRef.current = setTimeout(() => {
        endSleep();
        sleepTimerRef.current = null;
      }, 15000);
    }
  };

  // Handle wake up - stop sleep
  const handleWake = () => {
    if (sleepTimerRef.current) {
      clearTimeout(sleepTimerRef.current);
      sleepTimerRef.current = null;
    }
    endSleep();
  };

  // Handle character click - play action (or wake if sleeping)
  const handleCharacterClick = () => {
    if (isSleeping) {
      handleWake();
    } else {
      // Trigger play action when character is clicked
      handleAction('play');
    }
  };

  // Handle message send (streaming with fallback)
  const handleSendMessage = async () => {
    if (!inputValue.trim() || isTyping || isStreaming) return;

    const content = inputValue;
    setInputValue('');

    try {
      await sendMessageStream(content);
    } catch {
      // Fallback to non-streaming
      await sendMessage(content);
    }
  };

  return (
    <div className="relative w-full">
      <Navbar />

      <section className="relative min-h-screen w-full flex flex-col items-center px-6 sm:px-12 pb-6 sm:pb-12 pt-36">
        {/* Background Decor */}
        <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-blue-900/10 rounded-full blur-[120px]" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-purple-900/10 rounded-full blur-[120px]" />

        <div className="relative w-full max-w-6xl flex flex-col lg:flex-row gap-8 items-center justify-between">

          {/* Left: Stats & Actions */}
          <div className="w-full lg:w-1/4 flex flex-col gap-6 order-2 lg:order-1">
            <StatsPanel stats={stats} />
            <ActionButtons
              onAction={handleAction}
              isSleeping={isSleeping}
              onWake={handleWake}
              playButtonPulse={playButtonPulse}
            />
            <SidebarSchedule />
          </div>

          {/* Center: 3D Character with Overlays */}
          <div className="flex-1 w-full h-[50vh] lg:h-[600px] relative order-1 lg:order-2 overflow-hidden">
            <SplineCharacter
              onLoad={onSplineLoad}
              onClick={handleCharacterClick}
              splineReady={splineReady}
              isSleeping={isSleeping}
              onResetCamera={resetCamera}
            />
            {/* Sleep zzz overlay */}
            <SleepOverlay isSleeping={isSleeping} />
            {/* Play hearts overlay */}
            <PlayOverlay isPlaying={isPlaying} />
          </div>

          {/* Right: Chat with LobCoin */}
          <div className="w-full lg:w-1/3 flex flex-col gap-3 order-3">
            {/* Human Level Badge */}
            <UserLevelBadge />

            {/* LobCoin Balance */}
            <LobCoinBalance />

            {/* Chat Interface */}
            <ChatInterface
              messages={messages}
              inputValue={inputValue}
              isTyping={isTyping}
              onInputChange={setInputValue}
              onSendMessage={handleSendMessage}
              onClearHistory={clearMessageHistory}
              isExpanded={isChatExpanded}
              onToggleExpand={() => setIsChatExpanded(!isChatExpanded)}
            />
          </div>
        </div>

        {/* Dashboard Section */}
        <div className="relative w-full max-w-6xl mt-12">
          <ChatDashboardSection />
        </div>

        {/* Attendance Card */}
        <div className="relative w-full max-w-6xl mt-8">
          <div className="grid lg:grid-cols-2 gap-8 mb-8">
            <AttendanceCard />
            <div className="lg:col-span-1" />
          </div>
        </div>
      </section>
    </div>
  );
};
