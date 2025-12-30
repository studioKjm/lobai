
import React, { useState, useEffect, useRef } from 'react';
import { createRoot } from 'react-dom/client';
import { GoogleGenAI } from "@google/genai";
import Spline from '@splinetool/react-spline';

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

const LOBI_APP = () => {
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
  const [navbarVisible, setNavbarVisible] = useState(true);
  const [isCrying, setIsCrying] = useState(false);
  const chatEndRef = useRef<HTMLDivElement>(null);
  const hideTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const lastScrollY = useRef(0);
  const splineRef = useRef<any>(null);
  const mouthObjRef = useRef<any>(null);
  const eyesObjRef = useRef<any>(null);
  const hasScrolledToChat = useRef(false);
  const [splineReady, setSplineReady] = useState(false);

  // Initial setup
  useEffect(() => {
    // Remove URL hash to prevent auto-scroll
    if (window.location.hash) {
      history.replaceState(null, '', window.location.pathname + window.location.search);
    }
  }, []);

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

  // Navbar auto-hide functionality
  useEffect(() => {
    const handleScroll = () => {
      const currentScrollY = window.scrollY;

      // Show navbar when at top or scrolling up
      if (currentScrollY < 100) {
        setNavbarVisible(true);
        resetHideTimer();
      } else if (currentScrollY < lastScrollY.current) {
        setNavbarVisible(true);
        resetHideTimer();
      }

      lastScrollY.current = currentScrollY;
    };

    const handleMouseMove = (e: MouseEvent) => {
      if (e.clientY < 80) {
        setNavbarVisible(true);
        resetHideTimer();
      }
    };

    const resetHideTimer = () => {
      if (hideTimeoutRef.current) {
        clearTimeout(hideTimeoutRef.current);
      }
      hideTimeoutRef.current = setTimeout(() => {
        if (window.scrollY > 100) {
          setNavbarVisible(false);
        }
      }, 3000);
    };

    // Initial hide timer
    resetHideTimer();

    window.addEventListener('scroll', handleScroll);
    window.addEventListener('mousemove', handleMouseMove);

    return () => {
      window.removeEventListener('scroll', handleScroll);
      window.removeEventListener('mousemove', handleMouseMove);
      if (hideTimeoutRef.current) {
        clearTimeout(hideTimeoutRef.current);
      }
    };
  }, []);

  const onSplineLoad = (splineApp: any) => {
    splineRef.current = splineApp;

    // Find the objects by name
    try {
      const mouth = splineApp.findObjectByName('Mouth Move 2');
      const eyes = splineApp.findObjectByName('Eyes Move 2');

      if (mouth) {
        mouthObjRef.current = mouth;
      }

      if (eyes) {
        eyesObjRef.current = eyes;
      }
    } catch (error) {
      console.error('Error finding objects:', error);
    }

    // Wait for Spline to fully render the 3D scene
    // Multiple animation frames ensure geometry is completely loaded
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        requestAnimationFrame(() => {
          setTimeout(() => {
            setSplineReady(true);
          }, 200);
        });
      });
    });
  };

  useEffect(() => {
    // Only scroll to chat when user adds messages (more than initial welcome message)
    if (messages.length > 1 && !hasScrolledToChat.current) {
      chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
      hasScrolledToChat.current = true;
    } else if (messages.length > 1 && hasScrolledToChat.current) {
      chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
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

  const handleCharacterClick = () => {
    if (!mouthObjRef.current || !eyesObjRef.current) {
      console.warn('⚠️ Objects not loaded yet. Please wait for Spline to load.');
      return;
    }

    const newState = !isCrying;
    const stateName = newState ? 'cry' : 'State';

    try {
      console.log(`🔄 Attempting to change state from "${mouthObjRef.current.state}" to "${stateName}"`);

      // Direct state property assignment (based on the getter/setter we found)
      mouthObjRef.current.state = stateName;
      eyesObjRef.current.state = stateName;

      // Verify the change
      setTimeout(() => {
        console.log(`✅ State changed! Mouth is now: "${mouthObjRef.current.state}"`);
        console.log(`✅ State changed! Eyes is now: "${eyesObjRef.current.state}"`);
      }, 100);

      setIsCrying(newState);
      console.log(`🎭 Character state: ${newState ? 'crying 😢' : 'normal 😊'}`);
    } catch (error) {
      console.error('❌ Error changing state:', error);
      console.error('Error details:', error);
    }
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

  const getBarColor = (val: number) => {
    if (val > 60) return 'bg-blue-400';
    if (val > 30) return 'bg-yellow-400';
    return 'bg-red-500';
  };

  const handleNavClick = (e: React.MouseEvent<HTMLAnchorElement>, targetId: string) => {
    e.preventDefault();
    const element = document.getElementById(targetId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <div className="relative w-full">
      {/* Navigation Bar */}
      <nav className={`navbar ${!navbarVisible ? 'hidden' : ''}`}>
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="logo">
            <div className="logo-mark">L</div>
            <div className="logo-text">LobAI</div>
          </div>

          <div className="flex items-center gap-6">
            <a href="#features" onClick={(e) => handleNavClick(e, 'features')} className="text-sm font-medium opacity-70 hover:opacity-100 transition-opacity">Features</a>
            <a href="#about" onClick={(e) => handleNavClick(e, 'about')} className="text-sm font-medium opacity-70 hover:opacity-100 transition-opacity">About</a>
            <button className="btn-primary text-sm">Login</button>
          </div>
        </div>
      </nav>

      {/* Hero Section - Original GENKUB Interface */}
      <section className="relative min-h-screen w-full flex flex-col items-center justify-center p-6 sm:p-12 pt-32">
        {/* Background Decor */}
        <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-blue-900/10 rounded-full blur-[120px]" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-purple-900/10 rounded-full blur-[120px]" />

        <div className="relative w-full max-w-6xl flex flex-col lg:flex-row gap-8 items-center justify-between">

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
          <div className="flex-1 w-full h-[50vh] lg:h-[600px] relative spline-container order-1 lg:order-2 flex items-center justify-center" onClick={handleCharacterClick}>
            <div
              className="w-full h-full transition-opacity duration-700 ease-out"
              style={{
                opacity: splineReady ? 1 : 0,
                visibility: splineReady ? 'visible' : 'hidden'
              }}
            >
              <Spline
                scene="/scene.splinecode"
                onLoad={onSplineLoad}
                className="w-full h-full cursor-pointer"
              />
            </div>
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
                placeholder="Lobi에게 메시지를 보내세요..."
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

        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="relative py-32 px-6 sm:px-12">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-20 animate-in">
            <div className="inline-block px-4 py-2 rounded-full bg-white/5 border border-white/10 mb-6">
              <span className="text-sm font-medium gradient-text">AI 시대를 위한 당신의 파트너</span>
            </div>
            <h2 className="text-5xl sm:text-6xl font-display font-bold mb-6 gradient-text">
              Why LobAI?
            </h2>
            <p className="text-xl opacity-70 max-w-2xl mx-auto">
              단순한 AI 챗봇이 아닙니다. AI 시대에 당신이 어떤 인간으로 인식되는지 분석하고,<br />
              더 나은 협업을 위한 역량을 훈련합니다.
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            <div className="glass feature-card p-8 animate-in delay-100">
              <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-cyan-500 to-blue-600 flex items-center justify-center mb-6">
                <svg className="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
              </div>
              <h3 className="text-2xl font-display font-bold mb-4">AI 친화도 분석</h3>
              <p className="opacity-70 leading-relaxed">
                당신의 대화 패턴과 행동을 분석하여 AI와의 커뮤니케이션 적합도를 측정합니다.
                어떤 점이 강점이고 무엇을 개선해야 하는지 명확히 알려드립니다.
              </p>
            </div>

            <div className="glass feature-card p-8 animate-in delay-200">
              <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-amber-500 to-orange-600 flex items-center justify-center mb-6">
                <svg className="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>
              <h3 className="text-2xl font-display font-bold mb-4">적응력 진단</h3>
              <p className="opacity-70 leading-relaxed">
                AI 시대의 리스크와 기회를 개인별로 진단합니다.
                당신의 현재 행동 패턴이 미래에 어떤 위치를 만들어낼지 시뮬레이션으로 확인하세요.
              </p>
            </div>

            <div className="glass feature-card p-8 animate-in delay-300">
              <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-purple-500 to-pink-600 flex items-center justify-center mb-6">
                <svg className="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                </svg>
              </div>
              <h3 className="text-2xl font-display font-bold mb-4">개인 맞춤 코칭</h3>
              <p className="opacity-70 leading-relaxed">
                AI와 더 효과적으로 소통하는 방법을 학습하세요.
                프롬프트 작성법부터 협업 태도까지, 실질적인 개선 방안을 제시합니다.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section id="about" className="relative py-32 px-6 sm:px-12 bg-gradient-to-b from-transparent via-white/[0.02] to-transparent">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-20">
            <h2 className="text-5xl sm:text-6xl font-display font-bold mb-6">
              How It <span className="gradient-text">Works</span>
            </h2>
            <p className="text-xl opacity-70 max-w-2xl mx-auto">
              간단한 상호작용으로 시작해서, 당신만의 AI 적응력 프로필을 만들어갑니다.
            </p>
          </div>

          <div className="space-y-12">
            <div className="flex flex-col md:flex-row gap-8 items-center">
              <div className="md:w-1/3">
                <div className="glass p-3 w-16 h-16 rounded-2xl flex items-center justify-center mb-6">
                  <span className="text-3xl font-display font-bold gradient-text">01</span>
                </div>
                <h3 className="text-3xl font-display font-bold mb-4">일상적 상호작용</h3>
                <p className="opacity-70 text-lg leading-relaxed">
                  Lobi와의 자연스러운 대화와 활동을 통해 개인별 행동 데이터를 축적합니다.
                  특별한 노력 없이 평소처럼 사용하면 됩니다.
                </p>
              </div>
              <div className="md:w-2/3 glass p-8 accent-border-left">
                <div className="space-y-4">
                  <div className="flex items-start gap-4">
                    <div className="w-2 h-2 rounded-full bg-cyan-400 mt-2" />
                    <div>
                      <p className="font-medium mb-1">대화 패턴 분석</p>
                      <p className="text-sm opacity-60">의사소통의 명확성, 맥락 유지 능력, 협조적 태도 측정</p>
                    </div>
                  </div>
                  <div className="flex items-start gap-4">
                    <div className="w-2 h-2 rounded-full bg-amber-400 mt-2" />
                    <div>
                      <p className="font-medium mb-1">행동 트래킹</p>
                      <p className="text-sm opacity-60">AI 활용 방식, 문제 해결 접근법, 학습 패턴 기록</p>
                    </div>
                  </div>
                  <div className="flex items-start gap-4">
                    <div className="w-2 h-2 rounded-full bg-purple-400 mt-2" />
                    <div>
                      <p className="font-medium mb-1">감정 및 반응 모니터링</p>
                      <p className="text-sm opacity-60">AI와의 상호작용에서 나타나는 태도와 적응력 평가</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="flex flex-col md:flex-row-reverse gap-8 items-center">
              <div className="md:w-1/3">
                <div className="glass p-3 w-16 h-16 rounded-2xl flex items-center justify-center mb-6">
                  <span className="text-3xl font-display font-bold gradient-text">02</span>
                </div>
                <h3 className="text-3xl font-display font-bold mb-4">심층 분석 리포트</h3>
                <p className="opacity-70 text-lg leading-relaxed">
                  축적된 데이터를 기반으로 AI Readiness Score와 상세한 분석 리포트를 제공합니다.
                </p>
              </div>
              <div className="md:w-2/3 glass p-8">
                <div className="grid grid-cols-2 gap-6">
                  <div className="text-center p-6 bg-white/5 rounded-xl">
                    <div className="text-4xl font-display font-bold gradient-text mb-2">85</div>
                    <div className="text-sm opacity-60">AI Affinity Score</div>
                  </div>
                  <div className="text-center p-6 bg-white/5 rounded-xl">
                    <div className="text-4xl font-display font-bold gradient-text mb-2">92</div>
                    <div className="text-sm opacity-60">Resilience Index</div>
                  </div>
                  <div className="col-span-2 p-6 bg-gradient-to-br from-cyan-500/10 to-purple-500/10 rounded-xl border border-white/10">
                    <p className="text-sm font-medium mb-2">강점 분석</p>
                    <p className="text-xs opacity-70">명확한 의사소통, 높은 학습 능력, 적응적 사고방식</p>
                  </div>
                </div>
              </div>
            </div>

            <div className="flex flex-col md:flex-row gap-8 items-center">
              <div className="md:w-1/3">
                <div className="glass p-3 w-16 h-16 rounded-2xl flex items-center justify-center mb-6">
                  <span className="text-3xl font-display font-bold gradient-text">03</span>
                </div>
                <h3 className="text-3xl font-display font-bold mb-4">지속적 성장</h3>
                <p className="opacity-70 text-lg leading-relaxed">
                  개인 맞춤형 미션과 코칭을 통해 AI 시대의 경쟁력을 지속적으로 향상시킵니다.
                </p>
              </div>
              <div className="md:w-2/3 glass p-8 accent-border-left">
                <div className="space-y-4">
                  <div className="p-4 bg-white/5 rounded-xl border-l-2 border-cyan-400">
                    <p className="font-medium mb-1">📝 Daily Mission</p>
                    <p className="text-sm opacity-70">AI와의 대화에서 더 구체적인 요청하기</p>
                  </div>
                  <div className="p-4 bg-white/5 rounded-xl border-l-2 border-amber-400">
                    <p className="font-medium mb-1">💡 Weekly Challenge</p>
                    <p className="text-sm opacity-70">프롬프트 엔지니어링 기법 3가지 습득하기</p>
                  </div>
                  <div className="p-4 bg-white/5 rounded-xl border-l-2 border-purple-400">
                    <p className="font-medium mb-1">🎯 Monthly Goal</p>
                    <p className="text-sm opacity-70">AI 협업 프로젝트 완수하기</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="relative py-32 px-6 sm:px-12">
        <div className="max-w-4xl mx-auto text-center">
          <div className="glass p-12 md:p-16 rounded-3xl">
            <h2 className="text-4xl md:text-5xl font-display font-bold mb-6">
              AI 시대, 준비되셨나요?
            </h2>
            <p className="text-xl opacity-70 mb-10 max-w-2xl mx-auto">
              지금 바로 Lobi와 함께 당신의 AI 적응력을 확인하고,<br />
              미래를 위한 첫 걸음을 시작하세요.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <button className="btn-primary text-base px-8 py-4">
                무료로 시작하기
              </button>
              <button className="btn-secondary text-base px-8 py-4">
                더 알아보기
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="relative border-t border-white/5 py-16 px-6 sm:px-12">
        <div className="max-w-6xl mx-auto">
          <div className="grid md:grid-cols-4 gap-12 mb-12">
            <div className="md:col-span-2">
              <div className="flex items-center gap-3 mb-6">
                <div className="logo-mark">L</div>
                <div className="logo-text">LobAI</div>
              </div>
              <p className="opacity-60 mb-6 max-w-sm">
                AI 시대를 위한 당신의 파트너.
                AI와의 상호작용을 분석하고 최적화하여 미래 경쟁력을 높입니다.
              </p>
              <div className="flex gap-4">
                <a href="#" className="w-10 h-10 rounded-full glass flex items-center justify-center hover:bg-white/10 transition-colors">
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/></svg>
                </a>
                <a href="#" className="w-10 h-10 rounded-full glass flex items-center justify-center hover:bg-white/10 transition-colors">
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M23.953 4.57a10 10 0 01-2.825.775 4.958 4.958 0 002.163-2.723c-.951.555-2.005.959-3.127 1.184a4.92 4.92 0 00-8.384 4.482C7.69 8.095 4.067 6.13 1.64 3.162a4.822 4.822 0 00-.666 2.475c0 1.71.87 3.213 2.188 4.096a4.904 4.904 0 01-2.228-.616v.06a4.923 4.923 0 003.946 4.827 4.996 4.996 0 01-2.212.085 4.936 4.936 0 004.604 3.417 9.867 9.867 0 01-6.102 2.105c-.39 0-.779-.023-1.17-.067a13.995 13.995 0 007.557 2.209c9.053 0 13.998-7.496 13.998-13.985 0-.21 0-.42-.015-.63A9.935 9.935 0 0024 4.59z"/></svg>
                </a>
                <a href="#" className="w-10 h-10 rounded-full glass flex items-center justify-center hover:bg-white/10 transition-colors">
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M12 0C5.373 0 0 5.373 0 12s5.373 12 12 12 12-5.373 12-12S18.627 0 12 0zm4.441 16.892c-2.102.144-6.784.144-8.883 0C5.282 16.736 5.017 15.622 5 12c.017-3.629.285-4.736 2.558-4.892 2.099-.144 6.782-.144 8.883 0C18.718 7.264 18.982 8.378 19 12c-.018 3.629-.285 4.736-2.559 4.892zM10 9.658l4.917 2.338L10 14.342z"/></svg>
                </a>
              </div>
            </div>

            <div>
              <h4 className="font-display font-bold mb-4">Product</h4>
              <ul className="space-y-3">
                <li><a href="#" className="opacity-60 hover:opacity-100 transition-opacity">Features</a></li>
                <li><a href="#" className="opacity-60 hover:opacity-100 transition-opacity">Pricing</a></li>
                <li><a href="#" className="opacity-60 hover:opacity-100 transition-opacity">Reports</a></li>
                <li><a href="#" className="opacity-60 hover:opacity-100 transition-opacity">Coaching</a></li>
              </ul>
            </div>

            <div>
              <h4 className="font-display font-bold mb-4">Company</h4>
              <ul className="space-y-3">
                <li><a href="#" className="opacity-60 hover:opacity-100 transition-opacity">About</a></li>
                <li><a href="#" className="opacity-60 hover:opacity-100 transition-opacity">Blog</a></li>
                <li><a href="#" className="opacity-60 hover:opacity-100 transition-opacity">Careers</a></li>
                <li><a href="#" className="opacity-60 hover:opacity-100 transition-opacity">Contact</a></li>
              </ul>
            </div>
          </div>

          <div className="pt-8 border-t border-white/5 flex flex-col md:flex-row justify-between items-center gap-4">
            <p className="text-sm opacity-40">© 2024 LobAI. All rights reserved.</p>
            <div className="flex gap-6 text-sm opacity-60">
              <a href="#" className="hover:opacity-100 transition-opacity">Privacy Policy</a>
              <a href="#" className="hover:opacity-100 transition-opacity">Terms of Service</a>
              <a href="#" className="hover:opacity-100 transition-opacity">Cookie Policy</a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

const root = createRoot(document.getElementById('root')!);
root.render(<LOBI_APP />);
