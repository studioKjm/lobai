import React from 'react';

export const Features: React.FC = () => {
  return (
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
  );
};

