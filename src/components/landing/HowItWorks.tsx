import React from 'react';

export const HowItWorks: React.FC = () => {
  return (
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
  );
};

