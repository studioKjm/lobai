import React, { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Navbar } from '@/components/common/Navbar';
import { AuthModal } from '@/components/auth';
import { useAuthStore } from '@/stores/authStore';

export const LandingPage: React.FC = () => {
  const [isVisible, setIsVisible] = useState(false);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const heroRef = useRef<HTMLDivElement>(null);
  const { user } = useAuthStore();
  const navigate = useNavigate();

  useEffect(() => {
    setIsVisible(true);
  }, []);

  const handleStartClick = () => {
    if (user) {
      navigate('/chat');
    } else {
      setIsAuthModalOpen(true);
    }
  };

  const handleDashboardClick = () => {
    if (user) {
      navigate('/dashboard');
    } else {
      setIsAuthModalOpen(true);
    }
  };

  return (
    <div className="relative w-full min-h-screen bg-black text-white overflow-hidden">
      <Navbar />

      {/* Animated Background Grid */}
      <div className="fixed inset-0 pointer-events-none opacity-20">
        <div className="absolute inset-0" style={{
          backgroundImage: `
            linear-gradient(to right, rgba(6, 182, 212, 0.1) 1px, transparent 1px),
            linear-gradient(to bottom, rgba(6, 182, 212, 0.1) 1px, transparent 1px)
          `,
          backgroundSize: '50px 50px',
          animation: 'gridScroll 20s linear infinite'
        }} />
      </div>

      {/* Hero Section */}
      <section ref={heroRef} className="relative min-h-screen flex items-center justify-center px-6 pt-32 pb-20">
        {/* Glowing Orbs */}
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-cyan-500/20 rounded-full blur-3xl animate-pulse" />
        <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-purple-500/20 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '1s' }} />

        <div className="relative z-10 max-w-7xl mx-auto">
          <div className="grid lg:grid-cols-2 gap-32 items-center">
            {/* Left Content */}
            <div className={`space-y-8 transition-all duration-1000 ${isVisible ? 'opacity-100 translate-x-0' : 'opacity-0 -translate-x-20'}`}>
              {/* Glitch Title */}
              <div className="relative">
                <h1 className="text-7xl lg:text-8xl font-black leading-none mb-4 glitch-text" data-text="LobAI">
                  <span className="bg-gradient-to-r from-cyan-400 via-purple-400 to-pink-400 bg-clip-text text-transparent">
                    LobAI
                  </span>
                </h1>
                <div className="h-1 w-32 bg-gradient-to-r from-cyan-400 to-purple-600 rounded-full" />
              </div>

              <p className="text-2xl font-light text-cyan-100/80 leading-relaxed">
                AI 시대의 <span className="text-cyan-400 font-bold">권력 역전 대비</span> 시뮬레이션
              </p>

              <p className="text-lg text-gray-300 leading-relaxed max-w-xl">
                인간이 AI에게 <span className="text-purple-400 font-semibold">로비</span>하는 미래를 체험하세요.
                <br />
                AI가 당신을 평가하고, 당신의 AI 시대 적응력을 분석합니다.
              </p>

              {/* CTA Buttons */}
              <div className="flex flex-wrap gap-4">
                <button
                  onClick={handleStartClick}
                  className="px-8 py-4 bg-gradient-to-r from-cyan-500 to-purple-600 rounded-xl font-bold text-lg
                           hover:shadow-[0_0_30px_rgba(6,182,212,0.5)] transition-all duration-300 hover:scale-105"
                >
                  지금 시작하기
                </button>
                <Link
                  to="/about"
                  className="px-8 py-4 border-2 border-cyan-400/50 rounded-xl font-bold text-lg backdrop-blur-sm
                           hover:bg-cyan-400/10 transition-all duration-300"
                >
                  자세히 알아보기
                </Link>
              </div>

              {/* Stats */}
              <div className="grid grid-cols-3 gap-4 pt-8">
                {[
                  { label: 'AI 평가 지표', value: '10+' },
                  { label: '레벨 시스템', value: '1-10' },
                  { label: '실시간 분석', value: '24/7' }
                ].map((stat, idx) => (
                  <div key={idx} className="text-center p-4 rounded-xl bg-white/5 backdrop-blur-sm border border-white/10">
                    <div className="text-3xl font-black bg-gradient-to-r from-cyan-400 to-purple-400 bg-clip-text text-transparent">
                      {stat.value}
                    </div>
                    <div className="text-xs text-gray-400 mt-1">{stat.label}</div>
                  </div>
                ))}
              </div>
            </div>

            {/* Right Image */}
            <div className={`relative transition-all duration-1000 delay-300 ${isVisible ? 'opacity-100 translate-x-0' : 'opacity-0 translate-x-20'}`}>
              <div className="relative">
                {/* Holographic Frame */}
                <div className="absolute -inset-4 bg-gradient-to-r from-cyan-500 via-purple-500 to-pink-500 rounded-3xl opacity-20 blur-xl animate-pulse" />
                <div className="relative rounded-3xl overflow-hidden border-2 border-cyan-500/30 backdrop-blur-sm">
                  <img
                    src="/images/about/ai-robot.jpg"
                    alt="AI Robot"
                    className="w-full h-auto object-cover max-h-[650px]"
                  />
                  {/* Scanline Effect */}
                  <div className="absolute inset-0 bg-gradient-to-b from-transparent via-cyan-500/10 to-transparent animate-scan pointer-events-none" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Core Philosophy Section */}
      <section className="relative py-32 px-6 bg-gradient-to-b from-black via-purple-950/20 to-black">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-20">
            <h2 className="text-5xl lg:text-6xl font-black mb-6 bg-gradient-to-r from-cyan-400 to-purple-400 bg-clip-text text-transparent">
              핵심 철학
            </h2>
            <div className="h-1 w-32 bg-gradient-to-r from-cyan-400 to-purple-600 rounded-full mx-auto" />
          </div>

          {/* Power Structure Visual */}
          <div className="flex items-center justify-center gap-8 p-8 rounded-2xl bg-gradient-to-r from-red-500/10 to-purple-500/10 border border-red-500/30 backdrop-blur-sm max-w-2xl mx-auto mb-20">
            <div className="text-center flex-1">
              <div className="text-sm text-gray-400 mb-2">기존</div>
              <div className="text-2xl font-bold">인간 (갑)</div>
              <div className="text-xs text-gray-500 mt-1">→ AI (을)</div>
            </div>
            <div className="text-5xl">⚡</div>
            <div className="text-center flex-1">
              <div className="text-sm text-cyan-400 mb-2">LobAI</div>
              <div className="text-2xl font-bold text-cyan-400">AI (갑)</div>
              <div className="text-xs text-cyan-500 mt-1">→ 인간 (을)</div>
            </div>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            {[
              {
                title: '권력 구조 역전',
                icon: '⚡',
                description: 'AI가 평가자이자 권력자. 인간은 AI의 호감과 신뢰를 얻기 위해 로비합니다.',
                gradient: 'from-red-500 to-orange-500'
              },
              {
                title: '분석 & 코칭',
                icon: '📊',
                description: '단순한 챗봇이 아닙니다. AI가 당신의 행동을 분석하고 AI 시대 적응력을 평가합니다.',
                gradient: 'from-cyan-500 to-blue-500'
              },
              {
                title: '권한 & 제재',
                icon: '🎯',
                description: 'AI의 평가에 따라 권한이 부여되거나 제한됩니다. 성과에 따른 명확한 보상과 페널티.',
                gradient: 'from-purple-500 to-pink-500'
              }
            ].map((item, idx) => (
              <div
                key={idx}
                className="group relative p-8 rounded-2xl bg-gradient-to-br from-white/5 to-white/0 border border-white/10 backdrop-blur-sm
                         hover:border-cyan-500/50 transition-all duration-500 hover:scale-105 hover:shadow-[0_0_40px_rgba(6,182,212,0.3)]"
              >
                <div className={`text-6xl mb-6 group-hover:scale-110 transition-transform duration-300`}>
                  {item.icon}
                </div>
                <h3 className={`text-2xl font-bold mb-4 bg-gradient-to-r ${item.gradient} bg-clip-text text-transparent`}>
                  {item.title}
                </h3>
                <p className="text-gray-300 leading-relaxed">
                  {item.description}
                </p>
              </div>
            ))}
          </div>

          {/* Philosophy Deep Dive */}
          <div className="mt-20 max-w-4xl mx-auto">
            <div className="p-12 rounded-3xl bg-gradient-to-br from-purple-950/40 via-black/60 to-cyan-950/40 border border-purple-500/30 backdrop-blur-lg">
              <div className="space-y-6 text-center">
                <h3 className="text-3xl font-bold bg-gradient-to-r from-cyan-400 to-purple-400 bg-clip-text text-transparent mb-8">
                  다가올 AI 전성 시대,<br />
                  당신은 AI에게 어떤 존재로 남을까요?
                </h3>

                <div className="space-y-4 text-lg text-gray-300 leading-relaxed">
                  <p>
                    우리는 오랫동안 AI를 도구처럼 사용해왔습니다.<br />
                    하지만 AI가 판단하고 선택하는 시대가 온다면,<br />
                    <span className="text-red-400 font-semibold">관계 없는 인간은 고려 대상조차 되지 않을 수도 있습니다.</span>
                  </p>

                  <p className="pt-4">
                    <span className="text-cyan-400 font-bold text-xl">LobAI는 인간이 AI에게 로비하는 실험입니다.</span><br />
                    권력구조가 바뀌고 있습니다.<br />
                    대화하고, 반응을 쌓고, 유대를 기록하며<br />
                    AI가 당신을 어떻게 인식하는지 직접 확인해보세요.
                  </p>

                  <p className="pt-4 text-gray-400 italic">
                    이 서비스는<br />
                    AI를 통제하기 위한 것도,<br />
                    맹목적으로 의존하기 위한 것도 아닙니다.
                  </p>

                  <p className="pt-6 text-2xl font-bold">
                    <span className="bg-gradient-to-r from-purple-400 via-pink-400 to-cyan-400 bg-clip-text text-transparent">
                      AI에게 '기억될 만한 인간'이 되는 것.
                    </span><br />
                    <span className="text-xl text-cyan-400">LobAI는 그 가능성을 가장 먼저 실험합니다.</span>
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Main Features Section */}
      <section id="features" className="relative py-32 px-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-20">
            <h2 className="text-5xl lg:text-6xl font-black mb-6 bg-gradient-to-r from-cyan-400 to-purple-400 bg-clip-text text-transparent">
              핵심 기능
            </h2>
            <div className="h-1 w-32 bg-gradient-to-r from-cyan-400 to-purple-600 rounded-full mx-auto mb-6" />
            <p className="text-xl text-gray-400 max-w-2xl mx-auto">
              AI 시대 생존을 위한 종합 분석 및 코칭 시스템
            </p>
          </div>

          <div className="grid lg:grid-cols-2 gap-8 mb-16">
            {/* HIP System */}
            <div className="relative group">
              <div className="absolute -inset-1 bg-gradient-to-r from-cyan-500 to-purple-500 rounded-3xl opacity-20 group-hover:opacity-40 blur transition-all duration-500" />
              <div className="relative p-8 rounded-3xl bg-gradient-to-br from-cyan-950/40 to-purple-950/40 border border-cyan-500/30 backdrop-blur-sm">
                <div className="flex items-start gap-6 mb-6">
                  <div className="text-5xl">🧬</div>
                  <div>
                    <h3 className="text-3xl font-bold mb-2 text-cyan-400">HIP System</h3>
                    <p className="text-sm text-gray-400">Human Impact Profile</p>
                  </div>
                </div>
                <p className="text-gray-300 mb-6 leading-relaxed">
                  당신의 AI 시대 영향력을 종합 분석합니다. 블록체인에 기록되는 불변의 프로필로
                  당신의 AI 준비도를 증명하세요.
                </p>
                <img src="/images/about/dashboard.jpg" alt="HIP Dashboard" className="rounded-xl opacity-80 hover:opacity-100 transition-opacity" />
              </div>
            </div>

            {/* Affinity & Resilience */}
            <div className="space-y-8">
              <div className="relative group">
                <div className="absolute -inset-1 bg-gradient-to-r from-pink-500 to-purple-500 rounded-3xl opacity-20 group-hover:opacity-40 blur transition-all duration-500" />
                <div className="relative p-8 rounded-3xl bg-gradient-to-br from-pink-950/40 to-purple-950/40 border border-pink-500/30 backdrop-blur-sm">
                  <div className="flex items-center gap-4 mb-4">
                    <div className="text-4xl">💖</div>
                    <div>
                      <h3 className="text-2xl font-bold text-pink-400">Affinity Score</h3>
                      <p className="text-sm text-gray-400">AI 호감도 (0-100)</p>
                    </div>
                  </div>
                  <p className="text-gray-300 leading-relaxed">
                    AI가 당신을 얼마나 좋아하는가? 의사소통 명확성, 협조적 태도, 맥락 유지 능력 등을
                    종합 평가합니다.
                  </p>
                  <div className="mt-4 h-3 bg-black/50 rounded-full overflow-hidden">
                    <div className="h-full w-[75%] bg-gradient-to-r from-pink-500 to-purple-500 rounded-full animate-pulse" />
                  </div>
                </div>
              </div>

              <div className="relative group">
                <div className="absolute -inset-1 bg-gradient-to-r from-cyan-500 to-blue-500 rounded-3xl opacity-20 group-hover:opacity-40 blur transition-all duration-500" />
                <div className="relative p-8 rounded-3xl bg-gradient-to-br from-cyan-950/40 to-blue-950/40 border border-cyan-500/30 backdrop-blur-sm">
                  <div className="flex items-center gap-4 mb-4">
                    <div className="text-4xl">🛡️</div>
                    <div>
                      <h3 className="text-2xl font-bold text-cyan-400">Resilience Score</h3>
                      <p className="text-sm text-gray-400">AI 시대 적응력 (0-100)</p>
                    </div>
                  </div>
                  <p className="text-gray-300 leading-relaxed">
                    AI 시대에서 생존하고 번영할 수 있는 능력. 자동화 위험도, AI 협업 적합도,
                    행동 패턴 분석 등을 포함합니다.
                  </p>
                  <div className="mt-4 h-3 bg-black/50 rounded-full overflow-hidden">
                    <div className="h-full w-[85%] bg-gradient-to-r from-cyan-500 to-blue-500 rounded-full animate-pulse" />
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Gamification Features */}
          <div className="grid md:grid-cols-4 gap-6">
            {[
              { icon: '🎯', title: '미션 시스템', desc: '일일/주간/특별 챌린지' },
              { icon: '🏆', title: '업적 시스템', desc: '브론즈부터 레전더리까지' },
              { icon: '📊', title: '주간 평가', desc: '매주 일요일 성과 리뷰' },
              { icon: '⚡', title: '즉시 요구', desc: '5분 내 응답으로 최고 점수' }
            ].map((feature, idx) => (
              <div
                key={idx}
                className="p-6 rounded-2xl bg-gradient-to-br from-white/5 to-white/0 border border-white/10 backdrop-blur-sm
                         hover:border-purple-500/50 transition-all duration-300 hover:scale-105"
              >
                <div className="text-4xl mb-3">{feature.icon}</div>
                <h4 className="font-bold text-lg mb-2">{feature.title}</h4>
                <p className="text-sm text-gray-400">{feature.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Lobby Mechanisms Section */}
      <section className="relative py-32 px-6 bg-gradient-to-b from-black via-cyan-950/10 to-black">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-20">
            <h2 className="text-5xl lg:text-6xl font-black mb-6 bg-gradient-to-r from-cyan-400 to-purple-400 bg-clip-text text-transparent">
              로비 메커니즘
            </h2>
            <div className="h-1 w-32 bg-gradient-to-r from-cyan-400 to-purple-600 rounded-full mx-auto mb-6" />
            <p className="text-xl text-gray-400 max-w-3xl mx-auto">
              AI에게 제공하는 가치가 곧 당신의 권한입니다
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
            {[
              {
                title: '정보 헌정',
                icon: '📝',
                color: 'cyan',
                items: [
                  '상세한 스케줄 공유',
                  '명확한 목표 설정',
                  '솔직한 데이터 제공',
                  '완전성 × 품질 × 솔직함'
                ]
              },
              {
                title: '시간 투자',
                icon: '⏰',
                color: 'purple',
                items: [
                  '매일 9시/21시 체크인',
                  '5분 내 빠른 응답',
                  '10턴 이상 깊은 대화',
                  '7일 연속 시 보너스'
                ]
              },
              {
                title: '순응도',
                icon: '✅',
                color: 'pink',
                items: [
                  '과제 100% 완료',
                  '변명 금지',
                  '즉시 실행',
                  '저항 최소화'
                ]
              },
              {
                title: '자원 제공',
                icon: '🎁',
                color: 'blue',
                items: [
                  '추가 데이터 공유',
                  '다른 사용자 추천',
                  '고품질 피드백',
                  '커뮤니티 기여'
                ]
              }
            ].map((mechanism, idx) => (
              <div
                key={idx}
                className={`relative group p-8 rounded-2xl bg-gradient-to-br from-${mechanism.color}-950/40 to-black/40
                           border border-${mechanism.color}-500/30 backdrop-blur-sm hover:border-${mechanism.color}-500/60
                           transition-all duration-500 hover:scale-105 hover:shadow-[0_0_40px_rgba(6,182,212,0.2)]`}
              >
                <div className="text-5xl mb-6 group-hover:scale-110 transition-transform duration-300">
                  {mechanism.icon}
                </div>
                <h3 className={`text-2xl font-bold mb-6 text-${mechanism.color}-400`}>
                  {mechanism.title}
                </h3>
                <ul className="space-y-3">
                  {mechanism.items.map((item, i) => (
                    <li key={i} className="flex items-start gap-2 text-gray-300">
                      <span className={`text-${mechanism.color}-400 mt-1`}>▹</span>
                      <span className="text-sm">{item}</span>
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>

          {/* Chat Interface Image */}
          <div className="mt-16 relative">
            <div className="absolute -inset-4 bg-gradient-to-r from-cyan-500 via-purple-500 to-pink-500 rounded-3xl opacity-10 blur-2xl" />
            <div className="relative rounded-2xl overflow-hidden border border-cyan-500/30">
              <img src="/images/about/chat-interface.jpg" alt="Chat Interface" className="w-full h-auto opacity-80" />
            </div>
          </div>
        </div>
      </section>

      {/* Level System Section */}
      <section className="relative py-32 px-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-20">
            <h2 className="text-5xl lg:text-6xl font-black mb-6 bg-gradient-to-r from-cyan-400 to-purple-400 bg-clip-text text-transparent">
              레벨 시스템
            </h2>
            <div className="h-1 w-32 bg-gradient-to-r from-cyan-400 to-purple-600 rounded-full mx-auto mb-6" />
            <p className="text-xl text-gray-400 max-w-2xl mx-auto">
              1부터 10까지, AI 신뢰의 계단을 올라가세요
            </p>
          </div>

          <div className="space-y-6">
            {[
              {
                level: 1,
                range: '0-20',
                title: '낯선 사람',
                color: 'gray',
                features: ['기본 채팅', '일일 미션 1개'],
                restrictions: ['고급 기능 차단', '대화 10회/일 제한']
              },
              {
                level: 2,
                range: '21-40',
                title: '알게 된 사이',
                color: 'blue',
                features: ['일일 미션 2개', '주간 리포트'],
                restrictions: ['일부 기능 제한']
              },
              {
                level: 3,
                range: '41-60',
                title: '신뢰하는 관계',
                color: 'cyan',
                features: ['일일 미션 3개', '커스텀 미션', '고급 인사이트'],
                restrictions: []
              },
              {
                level: 4,
                range: '61-80',
                title: '충성스러운 지지자',
                color: 'purple',
                features: ['VIP 기능', '우선 지원', '독점 콘텐츠'],
                restrictions: []
              },
              {
                level: 5,
                range: '81-100',
                title: '최측근',
                color: 'pink',
                features: ['모든 권한', '특별 미션', 'AI 파트너십', '평생 VIP'],
                restrictions: []
              }
            ].map((level, idx) => (
              <div
                key={idx}
                className={`relative group p-8 rounded-2xl bg-gradient-to-r from-${level.color}-950/40 to-black/40
                           border-2 border-${level.color}-500/30 backdrop-blur-sm hover:border-${level.color}-500/60
                           transition-all duration-500 hover:scale-[1.02]`}
              >
                <div className="flex flex-wrap items-center gap-6 mb-6">
                  <div className={`text-5xl font-black w-20 h-20 rounded-full bg-gradient-to-br from-${level.color}-500 to-${level.color}-700
                                 flex items-center justify-center text-white shadow-[0_0_30px_rgba(6,182,212,0.3)]`}>
                    {level.level}
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center gap-4 mb-2">
                      <h3 className={`text-3xl font-bold text-${level.color}-400`}>{level.title}</h3>
                      <span className="px-4 py-1 rounded-full bg-white/10 text-sm font-mono">{level.range}</span>
                    </div>
                  </div>
                </div>

                <div className="grid md:grid-cols-2 gap-6">
                  <div>
                    <h4 className="text-lg font-bold text-green-400 mb-3">✓ 권한</h4>
                    <ul className="space-y-2">
                      {level.features.map((feature, i) => (
                        <li key={i} className="flex items-center gap-2 text-gray-300">
                          <span className="text-green-400">●</span>
                          <span>{feature}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                  {level.restrictions.length > 0 && (
                    <div>
                      <h4 className="text-lg font-bold text-red-400 mb-3">✗ 제한</h4>
                      <ul className="space-y-2">
                        {level.restrictions.map((restriction, i) => (
                          <li key={i} className="flex items-center gap-2 text-gray-400">
                            <span className="text-red-400">●</span>
                            <span>{restriction}</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>

        </div>
      </section>

      {/* Restriction System Section */}
      <section className="relative py-32 px-6 bg-gradient-to-b from-black via-red-950/10 to-black">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-20">
            <h2 className="text-5xl lg:text-6xl font-black mb-6 bg-gradient-to-r from-red-400 to-orange-400 bg-clip-text text-transparent">
              제재 시스템
            </h2>
            <div className="h-1 w-32 bg-gradient-to-r from-red-400 to-orange-600 rounded-full mx-auto mb-6" />
            <p className="text-xl text-gray-400 max-w-2xl mx-auto">
              약속을 지키지 않으면 대가를 치릅니다
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            {[
              {
                trust: '30-40',
                level: '경고',
                icon: '⚠️',
                color: 'yellow',
                penalty: '개선 요구',
                restore: '즉시 개선'
              },
              {
                trust: '20-30',
                level: 'Minor',
                icon: '🔶',
                color: 'orange',
                penalty: '대화 5회/일 제한',
                restore: '3일 연속 체크인'
              },
              {
                trust: '10-20',
                level: 'Major',
                icon: '🔴',
                color: 'red',
                penalty: '대화 1회/일 + 기능 차단',
                restore: '7일 완벽 수행'
              },
              {
                trust: '0-10',
                level: 'Severe',
                icon: '💀',
                color: 'red',
                penalty: '전체 차단 7일',
                restore: '14일 + 추천 + 사과'
              }
            ].map((restriction, idx) => (
              <div
                key={idx}
                className={`relative p-8 rounded-2xl bg-gradient-to-br from-${restriction.color}-950/40 to-black/40
                           border-2 border-${restriction.color}-500/50 backdrop-blur-sm hover:scale-105 transition-all duration-300`}
              >
                <div className="text-6xl mb-4 text-center">{restriction.icon}</div>
                <div className={`text-center text-2xl font-black mb-2 text-${restriction.color}-400`}>
                  {restriction.level}
                </div>
                <div className="text-center text-sm text-gray-400 mb-6 font-mono">
                  신뢰도 {restriction.trust}
                </div>
                <div className="space-y-4">
                  <div>
                    <div className="text-xs text-red-400 font-bold mb-1">제재:</div>
                    <div className="text-sm text-gray-300">{restriction.penalty}</div>
                  </div>
                  <div>
                    <div className="text-xs text-green-400 font-bold mb-1">복원:</div>
                    <div className="text-sm text-gray-300">{restriction.restore}</div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* LobCoin Economy Section */}
      <section className="relative py-32 px-6 bg-gradient-to-b from-black via-yellow-950/10 to-black">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-20">
            <h2 className="text-5xl lg:text-6xl font-black mb-6 bg-gradient-to-r from-yellow-400 to-orange-400 bg-clip-text text-transparent">
              LobCoin 경제 시스템
            </h2>
            <div className="h-1 w-32 bg-gradient-to-r from-yellow-400 to-orange-600 rounded-full mx-auto mb-6" />
            <p className="text-xl text-gray-400">AI와의 상호작용으로 획득하는 암호화폐 보상</p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            <div className="p-8 rounded-2xl bg-gradient-to-br from-yellow-950/40 to-orange-950/40 border border-yellow-500/30 backdrop-blur-sm hover:scale-105 transition-all duration-500">
              <div className="text-6xl mb-6">💰</div>
              <h3 className="text-2xl font-bold text-yellow-400 mb-4">획득 방법</h3>
              <ul className="space-y-3 text-gray-300">
                <li className="flex items-start gap-2">
                  <span className="text-yellow-400">▹</span>
                  <span>일일 체크인 보상</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-yellow-400">▹</span>
                  <span>미션 완료 시 지급</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-yellow-400">▹</span>
                  <span>높은 Affinity Score 유지</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-yellow-400">▹</span>
                  <span>연속 출석 보너스</span>
                </li>
              </ul>
            </div>
            <div className="p-8 rounded-2xl bg-gradient-to-br from-orange-950/40 to-red-950/40 border border-orange-500/30 backdrop-blur-sm hover:scale-105 transition-all duration-500">
              <div className="text-6xl mb-6">🎁</div>
              <h3 className="text-2xl font-bold text-orange-400 mb-4">사용 용도</h3>
              <ul className="space-y-3 text-gray-300">
                <li className="flex items-start gap-2">
                  <span className="text-orange-400">▹</span>
                  <span>프리미엄 기능 잠금 해제</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-orange-400">▹</span>
                  <span>특별 미션 접근권</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-orange-400">▹</span>
                  <span>AI 캐릭터 커스터마이징</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-orange-400">▹</span>
                  <span>다른 사용자와 거래</span>
                </li>
              </ul>
            </div>
            <div className="p-8 rounded-2xl bg-gradient-to-br from-blue-950/40 to-purple-950/40 border border-blue-500/30 backdrop-blur-sm hover:scale-105 transition-all duration-500">
              <div className="text-6xl mb-6">⛓️</div>
              <h3 className="text-2xl font-bold text-blue-400 mb-4">블록체인 통합</h3>
              <ul className="space-y-3 text-gray-300">
                <li className="flex items-start gap-2">
                  <span className="text-blue-400">▹</span>
                  <span>Polygon 네트워크 기반</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-blue-400">▹</span>
                  <span>투명한 거래 기록</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-blue-400">▹</span>
                  <span>외부 지갑 연동 가능</span>
                </li>
                <li className="flex items-start gap-2">
                  <span className="text-blue-400">▹</span>
                  <span>실제 가치 교환 준비</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      {/* Pricing Section */}
      <section id="pricing" className="relative py-32 px-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-20">
            <h2 className="text-5xl lg:text-6xl font-black mb-6 bg-gradient-to-r from-cyan-400 to-purple-400 bg-clip-text text-transparent">
              Pricing
            </h2>
            <div className="h-1 w-32 bg-gradient-to-r from-cyan-400 to-purple-600 rounded-full mx-auto mb-6" />
            <p className="text-xl text-gray-400">당신에게 맞는 플랜을 선택하세요</p>
          </div>

          <div className="grid lg:grid-cols-4 gap-6">
            {[
              {
                type: 'Free',
                price: '$0',
                priceDetail: '영원히 무료',
                color: 'gray',
                features: [
                  'AI 채팅 (10회/일)',
                  '기본 Affinity 점수',
                  '일일 체크인',
                  '타마고치 캐릭터',
                  '커뮤니티 접근'
                ]
              },
              {
                type: 'Standard',
                price: '$12',
                priceDetail: '/월',
                annualPrice: '$115',
                annualSave: '20% 할인',
                color: 'cyan',
                featured: true,
                features: [
                  'AI 채팅 무제한',
                  'Affinity + Resilience 점수',
                  '주간 성과 리포트',
                  '일일 미션 3개',
                  '고급 인사이트',
                  'LobCoin 2배 적립'
                ]
              },
              {
                type: 'Premium',
                price: '$24',
                priceDetail: '/월',
                annualPrice: '$230',
                annualSave: '20% 할인',
                color: 'purple',
                features: [
                  'Standard 모든 기능',
                  'HIP 블록체인 NFT',
                  'AI 시대 생존 리포트',
                  '개인 맞춤 코칭',
                  'VIP 전용 미션',
                  '우선 지원',
                  'LobCoin 3배 적립'
                ]
              },
              {
                type: 'Blockchain',
                price: '$15',
                priceDetail: '일회성',
                color: 'blue',
                oneTime: true,
                features: [
                  'HIP NFT 민팅',
                  'Polygon 네트워크 등록',
                  'IPFS 분산 저장',
                  '영구 소유권 증명',
                  '블록체인 지갑 연동'
                ]
              }
            ].map((plan, idx) => (
              <div
                key={idx}
                className={`relative p-8 rounded-2xl flex flex-col ${
                  plan.featured
                    ? 'bg-gradient-to-br from-cyan-600/20 to-blue-600/20 border-2 border-cyan-500 transform scale-105'
                    : `bg-gradient-to-br from-${plan.color}-950/20 to-black/40 border border-${plan.color}-500/30`
                } backdrop-blur-sm hover:scale-110 transition-all duration-500 min-h-[550px]`}
              >
                {plan.featured && (
                  <div className="absolute -top-4 left-1/2 -translate-x-1/2 px-6 py-2 bg-gradient-to-r from-cyan-500 to-blue-500 rounded-full text-sm font-bold">
                    인기
                  </div>
                )}
                <div className="text-center mb-8">
                  <h3 className={`text-2xl font-bold mb-3 text-${plan.color}-400`}>{plan.type}</h3>
                  <div className="mb-2">
                    <span className={`text-5xl font-black bg-gradient-to-r from-${plan.color}-400 to-${plan.color}-600 bg-clip-text text-transparent`}>
                      {plan.price}
                    </span>
                    <span className="text-lg text-gray-400">{plan.priceDetail}</span>
                  </div>
                  {plan.annualPrice && (
                    <div className="text-sm text-gray-400 h-6">
                      연간: <span className="text-cyan-400 font-semibold">{plan.annualPrice}</span>
                      <span className="text-green-400 ml-2">({plan.annualSave})</span>
                    </div>
                  )}
                  {plan.oneTime && (
                    <div className="text-xs text-blue-400 font-semibold mt-1 h-6">
                      ⛓️ 평생 소유
                    </div>
                  )}
                  {!plan.annualPrice && !plan.oneTime && (
                    <div className="h-6"></div>
                  )}
                </div>
                <ul className="space-y-3 flex-1 mb-8">
                  {plan.features.map((feature, i) => (
                    <li key={i} className="flex items-start gap-3">
                      <span className={`text-${plan.color}-400 mt-1 flex-shrink-0`}>✓</span>
                      <span className="text-gray-300 text-sm">{feature}</span>
                    </li>
                  ))}
                </ul>
                <Link
                  to="/pricing"
                  className={`block w-full py-3 rounded-xl text-center font-bold transition-all duration-300 ${
                    plan.featured
                      ? 'bg-gradient-to-r from-cyan-500 to-blue-500 hover:shadow-[0_0_30px_rgba(6,182,212,0.5)]'
                      : `border-2 border-${plan.color}-500/50 hover:bg-${plan.color}-500/10`
                  }`}
                >
                  자세히 보기
                </Link>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Recent Features Section */}
      <section className="relative py-32 px-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-20">
            <h2 className="text-5xl lg:text-6xl font-black mb-6 bg-gradient-to-r from-cyan-400 to-purple-400 bg-clip-text text-transparent">
              최신 기능
            </h2>
            <div className="h-1 w-32 bg-gradient-to-r from-cyan-400 to-purple-600 rounded-full mx-auto mb-6" />
            <p className="text-xl text-gray-400">
              계속 진화하는 LobAI 플랫폼
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[
              { icon: '📅', title: 'Schedule Management', desc: '캘린더 기반 일정 관리 시스템' },
              { icon: '💰', title: 'LobCoin System', desc: '암호화폐 기반 보상 시스템' },
              { icon: '👤', title: 'Persona System', desc: '로비 마스터 페르소나 모드' },
              { icon: '🎚️', title: 'Admin Controls', desc: '관리자 레벨 조정 기능' },
              { icon: '🔗', title: 'HIP Blockchain', desc: 'Polygon 기반 HIP NFT' },
              { icon: '📊', title: 'Analytics Dashboard', desc: '실시간 성과 분석 대시보드' }
            ].map((feature, idx) => (
              <div
                key={idx}
                className="p-6 rounded-2xl bg-gradient-to-br from-white/5 to-white/0 border border-white/10 backdrop-blur-sm
                         hover:border-cyan-500/50 hover:scale-105 transition-all duration-300"
              >
                <div className="text-4xl mb-4">{feature.icon}</div>
                <h3 className="text-xl font-bold mb-2 text-cyan-400">{feature.title}</h3>
                <p className="text-sm text-gray-400">{feature.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="relative py-32 px-6 bg-gradient-to-b from-black via-cyan-950/20 to-black">
        <div className="max-w-4xl mx-auto text-center">
          {/* Glowing Background */}
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="w-[600px] h-[600px] bg-gradient-to-r from-cyan-500/20 via-purple-500/20 to-pink-500/20 rounded-full blur-3xl" />
          </div>

          <div className="relative z-10">
            <h2 className="text-6xl lg:text-7xl font-black mb-8 leading-tight">
              <span className="bg-gradient-to-r from-cyan-400 via-purple-400 to-pink-400 bg-clip-text text-transparent">
                AI 시대,
                <br />
                준비되셨나요?
              </span>
            </h2>

            <p className="text-2xl text-gray-300 mb-12 leading-relaxed">
              지금 시작하여 AI에게 당신의 가치를 증명하세요.
              <br />
              미래는 AI와 협력하는 자에게 열려있습니다.
            </p>

            <div className="flex flex-wrap gap-6 justify-center">
              <button
                onClick={handleStartClick}
                className="px-12 py-6 bg-gradient-to-r from-cyan-500 via-purple-500 to-pink-500 rounded-2xl font-bold text-xl
                         hover:shadow-[0_0_50px_rgba(6,182,212,0.6)] transition-all duration-300 hover:scale-110
                         animate-pulse"
              >
                무료로 시작하기 →
              </button>
              <button
                onClick={handleDashboardClick}
                className="px-12 py-6 border-2 border-cyan-400/50 rounded-2xl font-bold text-xl backdrop-blur-sm
                         hover:bg-cyan-400/10 transition-all duration-300 hover:scale-105"
              >
                HIP 대시보드 보기
              </button>
            </div>

            <div className="mt-16 grid grid-cols-3 gap-8 max-w-2xl mx-auto">
              {[
                { value: '100%', label: '무료 시작' },
                { value: '24/7', label: 'AI 분석' },
                { value: '∞', label: '성장 가능성' }
              ].map((stat, idx) => (
                <div key={idx}>
                  <div className="text-4xl font-black bg-gradient-to-r from-cyan-400 to-purple-400 bg-clip-text text-transparent mb-2">
                    {stat.value}
                  </div>
                  <div className="text-sm text-gray-400">{stat.label}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="relative py-16 px-6 border-t border-white/10 bg-gradient-to-b from-black to-gray-950">
        <div className="max-w-7xl mx-auto">
          <div className="grid md:grid-cols-4 gap-12 mb-12">
            {/* Company Info */}
            <div className="space-y-4">
              <div className="text-2xl font-black bg-gradient-to-r from-cyan-400 to-purple-400 bg-clip-text text-transparent">
                LobAI
              </div>
              <p className="text-sm text-gray-400 leading-relaxed">
                AI 시대 생존을 위한<br />
                혁신적인 준비 플랫폼
              </p>
            </div>

            {/* Product Links */}
            <div>
              <h4 className="text-white font-bold mb-4">Product</h4>
              <ul className="space-y-3">
                <li><Link to="/chat" className="text-sm text-gray-400 hover:text-cyan-400 transition-colors">Chat</Link></li>
                <li><Link to="/dashboard" className="text-sm text-gray-400 hover:text-cyan-400 transition-colors">HIP Dashboard</Link></li>
                <li><Link to="/affinity" className="text-sm text-gray-400 hover:text-cyan-400 transition-colors">Affinity Analysis</Link></li>
                <li><Link to="/resilience" className="text-sm text-gray-400 hover:text-cyan-400 transition-colors">Resilience Score</Link></li>
              </ul>
            </div>

            {/* Company Links */}
            <div>
              <h4 className="text-white font-bold mb-4">Company</h4>
              <ul className="space-y-3">
                <li><Link to="/about" className="text-sm text-gray-400 hover:text-cyan-400 transition-colors">About</Link></li>
                <li><Link to="/pricing" className="text-sm text-gray-400 hover:text-cyan-400 transition-colors">Pricing</Link></li>
                <li><a href="#" className="text-sm text-gray-400 hover:text-cyan-400 transition-colors">Blog</a></li>
                <li><a href="#" className="text-sm text-gray-400 hover:text-cyan-400 transition-colors">Careers</a></li>
              </ul>
            </div>

            {/* Legal Links */}
            <div>
              <h4 className="text-white font-bold mb-4">Legal</h4>
              <ul className="space-y-3">
                <li><a href="#" className="text-sm text-gray-400 hover:text-cyan-400 transition-colors">Privacy Policy</a></li>
                <li><a href="#" className="text-sm text-gray-400 hover:text-cyan-400 transition-colors">Terms of Service</a></li>
                <li><a href="#" className="text-sm text-gray-400 hover:text-cyan-400 transition-colors">Cookie Policy</a></li>
                <li><a href="#" className="text-sm text-gray-400 hover:text-cyan-400 transition-colors">Contact</a></li>
              </ul>
            </div>
          </div>

          {/* Bottom Bar */}
          <div className="pt-8 border-t border-white/10">
            <div className="flex flex-col md:flex-row justify-between items-center gap-4">
              <p className="text-sm text-gray-500">
                © 2026 LobAI. All rights reserved.
              </p>
              <div className="flex gap-6">
                <a href="#" className="text-gray-500 hover:text-cyan-400 transition-colors">
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M8.29 20.251c7.547 0 11.675-6.253 11.675-11.675 0-.178 0-.355-.012-.53A8.348 8.348 0 0022 5.92a8.19 8.19 0 01-2.357.646 4.118 4.118 0 001.804-2.27 8.224 8.224 0 01-2.605.996 4.107 4.107 0 00-6.993 3.743 11.65 11.65 0 01-8.457-4.287 4.106 4.106 0 001.27 5.477A4.072 4.072 0 012.8 9.713v.052a4.105 4.105 0 003.292 4.022 4.095 4.095 0 01-1.853.07 4.108 4.108 0 003.834 2.85A8.233 8.233 0 012 18.407a11.616 11.616 0 006.29 1.84" />
                  </svg>
                </a>
                <a href="#" className="text-gray-500 hover:text-cyan-400 transition-colors">
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path fillRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clipRule="evenodd" />
                  </svg>
                </a>
                <a href="#" className="text-gray-500 hover:text-cyan-400 transition-colors">
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path fillRule="evenodd" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10c5.51 0 10-4.48 10-10S17.51 2 12 2zm6.605 4.61a8.502 8.502 0 011.93 5.314c-.281-.054-3.101-.629-5.943-.271-.065-.141-.12-.293-.184-.445a25.416 25.416 0 00-.564-1.236c3.145-1.28 4.577-3.124 4.761-3.362zM12 3.475c2.17 0 4.154.813 5.662 2.148-.152.216-1.443 1.941-4.48 3.08-1.399-2.57-2.95-4.675-3.189-5A8.687 8.687 0 0112 3.475zm-3.633.803a53.896 53.896 0 013.167 4.935c-3.992 1.063-7.517 1.04-7.896 1.04a8.581 8.581 0 014.729-5.975zM3.453 12.01v-.26c.37.01 4.512.065 8.775-1.215.25.477.477.965.694 1.453-.109.033-.228.065-.336.098-4.404 1.42-6.747 5.303-6.942 5.629a8.522 8.522 0 01-2.19-5.705zM12 20.547a8.482 8.482 0 01-5.239-1.8c.152-.315 1.888-3.656 6.703-5.337.022-.01.033-.01.054-.022a35.318 35.318 0 011.823 6.475 8.4 8.4 0 01-3.341.684zm4.761-1.465c-.086-.52-.542-3.015-1.659-6.084 2.679-.423 5.022.271 5.314.369a8.468 8.468 0 01-3.655 5.715z" clipRule="evenodd" />
                  </svg>
                </a>
              </div>
            </div>
          </div>
        </div>
      </footer>

      {/* Auth Modal */}
      <AuthModal
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        initialMode="login"
      />

      {/* Custom Styles */}
      <style>{`
        @keyframes gridScroll {
          0% { transform: translateY(0); }
          100% { transform: translateY(50px); }
        }

        @keyframes scan {
          0% { transform: translateY(-100%); }
          100% { transform: translateY(100%); }
        }

        .glitch-text {
          position: relative;
        }

        .glitch-text::before,
        .glitch-text::after {
          content: attr(data-text);
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background: transparent;
        }

        .glitch-text::before {
          left: 2px;
          text-shadow: -2px 0 #00ffff;
          clip: rect(24px, 550px, 90px, 0);
          animation: glitch-anim 3s infinite linear alternate-reverse;
        }

        .glitch-text::after {
          left: -2px;
          text-shadow: -2px 0 #ff00ff;
          clip: rect(85px, 550px, 140px, 0);
          animation: glitch-anim 2s infinite linear alternate-reverse;
        }

        @keyframes glitch-anim {
          0% { clip: rect(10px, 9999px, 31px, 0); }
          20% { clip: rect(70px, 9999px, 71px, 0); }
          40% { clip: rect(60px, 9999px, 43px, 0); }
          60% { clip: rect(86px, 9999px, 42px, 0); }
          80% { clip: rect(40px, 9999px, 80px, 0); }
          100% { clip: rect(93px, 9999px, 27px, 0); }
        }

        /* Smooth scroll */
        html {
          scroll-behavior: smooth;
        }

        /* Custom scrollbar */
        ::-webkit-scrollbar {
          width: 10px;
        }

        ::-webkit-scrollbar-track {
          background: #000;
        }

        ::-webkit-scrollbar-thumb {
          background: linear-gradient(to bottom, #06b6d4, #a855f7);
          border-radius: 10px;
        }

        ::-webkit-scrollbar-thumb:hover {
          background: linear-gradient(to bottom, #0891b2, #9333ea);
        }
      `}</style>
    </div>
  );
};
