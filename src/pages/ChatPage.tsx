import React, { useState, useEffect } from 'react';
import { GoogleGenAI } from "@google/genai";
import { Navbar } from '@/components/common/Navbar';
import { StatsPanel } from '@/components/chat/StatsPanel';
import { ActionButtons } from '@/components/chat/ActionButtons';
import { SplineCharacter } from '@/components/chat/SplineCharacter';
import { ChatInterface } from '@/components/chat/ChatInterface';
import { useStatsDecay } from '@/hooks/useStatsDecay';
import { useSplineLoader } from '@/hooks/useSplineLoader';
import type { Stats, Message, ActionType } from '@/types';

export const ChatPage: React.FC = () => {
  // State
  const [stats, setStats] = useState<Stats>({
    hunger: 80,
    energy: 90,
    happiness: 70
  });
  const [messages, setMessages] = useState<Message[]>([
    { role: 'bot', text: '안녕하세요! 저는 당신의 AI 동반자 Lobi입니다. 오늘도 멋진 하루 보내세요!' }
  ]);
  const [inputValue, setInputValue] = useState('');
  const [isTyping, setIsTyping] = useState(false);

  // Hooks
  useStatsDecay(setStats);
  const { splineReady, onSplineLoad, handleCharacterClick } = useSplineLoader();

  // Prevent auto-scroll on mount
  useEffect(() => {
    if (window.location.hash) {
      history.replaceState(null, '', window.location.pathname + window.location.search);
    }
  }, []);

  // Handle action buttons (Feed, Play, Sleep)
  const handleAction = (type: ActionType) => {
    setStats(prev => ({
      ...prev,
      [type]: Math.min(100, prev[type] + 15),
      happiness: Math.min(100, prev.happiness + 5)
    }));

    const responses: Record<string, string> = {
      hunger: "냠냠! 에너지가 충전되고 있어요.",
      energy: "꿀잠 자고 일어났어요. 몸이 가볍네요!",
      happiness: "와! 같이 노니까 정말 즐거워요!"
    };

    setMessages(prev => [...prev, {
      role: 'bot',
      text: responses[type] || "기분이 좋아졌어요!"
    }]);
  };

  // Send message to Gemini AI
  const sendMessage = async () => {
    if (!inputValue.trim() || isTyping) return;

    const userText = inputValue;
    setMessages(prev => [...prev, { role: 'user', text: userText }]);
    setInputValue('');
    setIsTyping(true);

    try {
      const ai = new GoogleGenAI({ apiKey: process.env.API_KEY || '' });
      const response = await ai.models.generateContent({
        model: 'gemini-3-flash-preview',
        contents: userText,
        config: {
          systemInstruction: `당신은 이름이 'Lobi'인 작고 귀여운 하이테크 로봇 타마고치입니다.
          현재 당신의 상태는 다음과 같습니다: 배고픔 ${Math.round(stats.hunger)}%, 에너지 ${Math.round(stats.energy)}%, 행복도 ${Math.round(stats.happiness)}%.
          상태에 맞게 반응하세요.
          말투는 아주 친절하고 약간 기계적인 느낌이 섞인 귀여운 말투(예: ~입니다, ~해요, ! 삐빅)를 사용하세요.
          답변은 아주 짧고 간결하게 한두 문장으로 하세요.`,
          temperature: 0.8,
        }
      });

      const botText = response.text || "삐빅... 통신 오류가 발생했습니다.";
      setMessages(prev => [...prev, { role: 'bot', text: botText }]);
      setStats(prev => ({ ...prev, happiness: Math.min(100, prev.happiness + 2) }));
    } catch (error) {
      console.error(error);
      setMessages(prev => [...prev, { role: 'bot', text: "삐비빅... 지금은 대화가 조금 어려워요." }]);
    } finally {
      setIsTyping(false);
    }
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
            onSendMessage={sendMessage}
          />
        </div>
      </section>
    </div>
  );
};
