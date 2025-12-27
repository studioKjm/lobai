
import React, { useState, useEffect, useRef } from 'react';
import { createRoot } from 'react-dom/client';
import { GoogleGenAI } from "@google/genai";

// Types
interface Stats {
  hunger: number;
  energy: number;
  happiness: number;
}

interface Message {
  role: 'user' | 'bot';
  text: string;
}

const GENKUB_APP = () => {
  const [stats, setStats] = useState<Stats>({
    hunger: 80,
    energy: 90,
    happiness: 70
  });
  const [messages, setMessages] = useState<Message[]>([
    { role: 'bot', text: '안녕하세요! 저는 당신의 AI 동반자 GENKUB입니다. 오늘도 멋진 하루 보내세요!' }
  ]);
  const [inputValue, setInputValue] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const chatEndRef = useRef<HTMLDivElement>(null);

  // Auto-decay stats over time
  useEffect(() => {
    const timer = setInterval(() => {
      setStats(prev => ({
        hunger: Math.max(0, prev.hunger - 0.5),
        energy: Math.max(0, prev.energy - 0.3),
        happiness: Math.max(0, prev.happiness - 0.4)
      }));
    }, 5000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleAction = (type: keyof Stats) => {
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
    
    setMessages(prev => [...prev, { role: 'bot', text: responses[type as string] || "기분이 좋아졌어요!" }]);
  };

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
          systemInstruction: `당신은 이름이 'GENKUB'인 작고 귀여운 하이테크 로봇 타마고치입니다.
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

  const getBarColor = (val: number) => {
    if (val > 60) return 'bg-blue-400';
    if (val > 30) return 'bg-yellow-400';
    return 'bg-red-500';
  };

  return (
    <div className="relative h-screen w-full flex flex-col items-center justify-center p-6 sm:p-12">
      {/* Background Decor */}
      <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-blue-900/10 rounded-full blur-[120px]" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-purple-900/10 rounded-full blur-[120px]" />

      <header className="absolute top-8 left-12 z-10">
        <h1 className="text-2xl font-outfit font-semibold tracking-tighter opacity-80">GENKUB v1.0</h1>
      </header>

      <main className="relative w-full max-w-6xl h-full flex flex-col lg:flex-row gap-8 items-center justify-between">
        
        {/* Left Side: Stats & Info */}
        <div className="w-full lg:w-1/4 flex flex-col gap-6 order-2 lg:order-1">
          <div className="glass p-6 flex flex-col gap-5">
            <h2 className="text-sm font-semibold uppercase tracking-widest opacity-40">System Status</h2>
            
            <div className="space-y-3">
              <div className="flex justify-between text-xs font-medium opacity-70">
                <span>HUNGER</span>
                <span>{Math.round(stats.hunger)}%</span>
              </div>
              <div className="status-bar"><div className={`status-fill ${getBarColor(stats.hunger)}`} style={{ width: `${stats.hunger}%` }} /></div>
            </div>

            <div className="space-y-3">
              <div className="flex justify-between text-xs font-medium opacity-70">
                <span>ENERGY</span>
                <span>{Math.round(stats.energy)}%</span>
              </div>
              <div className="status-bar"><div className={`status-fill ${getBarColor(stats.energy)}`} style={{ width: `${stats.energy}%` }} /></div>
            </div>

            <div className="space-y-3">
              <div className="flex justify-between text-xs font-medium opacity-70">
                <span>HAPPINESS</span>
                <span>{Math.round(stats.happiness)}%</span>
              </div>
              <div className="status-bar"><div className={`status-fill ${getBarColor(stats.happiness)}`} style={{ width: `${stats.happiness}%` }} /></div>
            </div>
          </div>

          <div className="flex flex-col gap-3">
             <button onClick={() => handleAction('hunger')} className="glass py-4 hover:bg-white/10 transition-all text-sm font-medium tracking-wide">FEED UNIT</button>
             <button onClick={() => handleAction('happiness')} className="glass py-4 hover:bg-white/10 transition-all text-sm font-medium tracking-wide">INITIATE PLAY</button>
             <button onClick={() => handleAction('energy')} className="glass py-4 hover:bg-white/10 transition-all text-sm font-medium tracking-wide">SLEEP MODE</button>
          </div>
        </div>

        {/* Center: 3D Model Stage */}
        <div className="flex-1 w-full h-[50vh] lg:h-full relative spline-container order-1 lg:order-2 flex items-center justify-center">
          <iframe 
            src='https://my.spline.design/genkubgreetingrobot-pkBX9j3rcGADQwxJfUloayrR/' 
            frameBorder='0' 
            width='100%' 
            height='100%'
            className="pointer-events-auto"
          />
          {/* Subtle overlay for styling integration */}
          <div className="absolute inset-0 pointer-events-none bg-gradient-to-t from-[#050505] via-transparent to-transparent opacity-40" />
        </div>

        {/* Right Side: Chat Interface */}
        <div className="w-full lg:w-1/3 h-[300px] lg:h-[500px] glass flex flex-col overflow-hidden order-3">
          <div className="p-4 border-b border-white/5 flex items-center gap-3">
            <div className="w-2 h-2 rounded-full bg-green-400 animate-pulse" />
            <span className="text-xs font-semibold uppercase tracking-widest opacity-60">Neural Interface</span>
          </div>
          
          <div className="flex-1 overflow-y-auto p-6 space-y-4">
            {messages.map((msg, idx) => (
              <div key={idx} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div className={`max-w-[85%] px-4 py-3 rounded-2xl text-sm leading-relaxed ${
                  msg.role === 'user' 
                  ? 'bg-blue-600 text-white rounded-tr-none' 
                  : 'bg-white/5 text-white/90 rounded-tl-none border border-white/5'
                }`}>
                  {msg.text}
                </div>
              </div>
            ))}
            {isTyping && (
              <div className="flex justify-start">
                <div className="bg-white/5 px-4 py-3 rounded-2xl rounded-tl-none border border-white/5">
                  <div className="flex gap-1">
                    <div className="w-1.5 h-1.5 bg-white/40 rounded-full animate-bounce" />
                    <div className="w-1.5 h-1.5 bg-white/40 rounded-full animate-bounce [animation-delay:0.2s]" />
                    <div className="w-1.5 h-1.5 bg-white/40 rounded-full animate-bounce [animation-delay:0.4s]" />
                  </div>
                </div>
              </div>
            )}
            <div ref={chatEndRef} />
          </div>

          <div className="p-4 bg-white/5 border-t border-white/5 flex gap-2">
            <input 
              type="text" 
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && sendMessage()}
              placeholder="GENKUB에게 메시지를 보내세요..."
              className="flex-1 bg-transparent border-none outline-none text-sm placeholder:opacity-30"
            />
            <button 
              onClick={sendMessage}
              className="p-2 hover:bg-white/10 rounded-xl transition-colors opacity-60 hover:opacity-100"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="m22 2-7 20-4-9-9-4Z"/><path d="M22 2 11 13"/></svg>
            </button>
          </div>
        </div>

      </main>

      <footer className="absolute bottom-8 w-full flex justify-center gap-8 text-[10px] font-medium tracking-[0.2em] opacity-30 uppercase">
        <span>© 2024 GENKUB LABS</span>
        <span>Neural Sync Active</span>
        <span>Bio-Metric Integrated</span>
      </footer>
    </div>
  );
};

const root = createRoot(document.getElementById('root')!);
root.render(<GENKUB_APP />);
