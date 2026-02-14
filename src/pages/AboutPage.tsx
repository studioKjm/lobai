import React from 'react';
import { Navbar } from '@/components/common/Navbar';
import { Link } from 'react-router-dom';

export const AboutPage: React.FC = () => {
  return (
    <div className="relative w-full min-h-screen bg-black text-white">
      <Navbar />

      {/* Hero Section */}
      <section className="relative pt-32 pb-20 px-6">
        <div className="max-w-4xl mx-auto">
          <div className="mb-12">
            <h1 className="text-5xl md:text-6xl font-black mb-6 bg-gradient-to-r from-cyan-400 to-purple-400 bg-clip-text text-transparent">
              LobAI 완전 가이드
            </h1>
            <p className="text-xl text-gray-400 leading-relaxed">
              AI에게 로비하고, AI 시대에 살아남는 방법을 배우는 플랫폼의 모든 것
            </p>
          </div>

          {/* Table of Contents */}
          <div className="p-8 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-sm mb-16">
            <h2 className="text-2xl font-bold mb-6 text-cyan-400">목차</h2>
            <nav className="space-y-3">
              {[
                { id: 'vision', title: '1. 프로젝트 비전과 배경' },
                { id: 'philosophy', title: '2. 핵심 철학: 권력 구조의 역전' },
                { id: 'system', title: '3. 시스템 상세 설명' },
                { id: 'lobby', title: '4. 로비 메커니즘 완전 가이드' },
                { id: 'levels', title: '5. 레벨 시스템과 권한' },
                { id: 'daily', title: '6. 일상적인 사용 방법' },
                { id: 'strategy', title: '7. 성공 전략과 팁' },
                { id: 'technical', title: '8. 기술적 구현' },
                { id: 'roadmap', title: '9. 로드맵과 미래 계획' },
                { id: 'faq', title: '10. 자주 묻는 질문 (FAQ)' }
              ].map((item) => (
                <a
                  key={item.id}
                  href={`#${item.id}`}
                  className="block text-gray-300 hover:text-cyan-400 transition-colors pl-4 border-l-2 border-transparent hover:border-cyan-400"
                >
                  {item.title}
                </a>
              ))}
            </nav>
          </div>
        </div>
      </section>

      {/* Content Sections */}
      <section className="px-6 pb-20">
        <div className="max-w-4xl mx-auto space-y-20">

          {/* Section 1: Vision */}
          <article id="vision" className="scroll-mt-32">
            <h2 className="text-4xl font-black mb-8 text-cyan-400">1. 프로젝트 비전과 배경</h2>

            <div className="prose prose-invert prose-lg max-w-none space-y-6 text-gray-300 leading-relaxed">
              <h3 className="text-2xl font-bold text-white mt-8 mb-4">왜 LobAI를 만들었는가?</h3>
              <p>
                우리는 AI 시대의 전환점에 서 있습니다. ChatGPT, Claude, Gemini 등 강력한 AI가 일상이 되면서,
                사람들은 새로운 질문을 마주하게 되었습니다:
              </p>
              <blockquote className="border-l-4 border-cyan-400 pl-6 italic text-xl my-8">
                "AI 시대에 나는 어떤 인간으로 평가받을 것인가?"
              </blockquote>
              <p>
                기존의 AI 도구들은 모두 "인간이 AI를 활용하는" 관점에서 설계되었습니다.
                AI는 도구이고, 인간은 사용자입니다. 하지만 AI 기술이 더욱 발전하고 사회 곳곳에 스며들면서,
                이러한 관계는 점차 변화하고 있습니다.
              </p>

              <h3 className="text-2xl font-bold text-white mt-8 mb-4">미래 시뮬레이션으로서의 LobAI</h3>
              <p>
                LobAI는 단순한 챗봇이나 타마고치 게임이 아닙니다. 이것은 <strong className="text-cyan-400">미래 사회의 시뮬레이션</strong>입니다.
              </p>
              <p>
                10년, 20년 후의 세상을 상상해보세요. AI가 채용 결정을 내리고, 승진을 결정하고,
                신용 평가를 하고, 심지어 법적 판단에도 관여하는 세상. 그런 세상에서 인간은 AI에게
                "잘 보여야" 합니다. AI의 평가 기준을 이해하고, AI가 선호하는 방식으로 소통하고,
                AI의 요구사항을 충족해야 합니다.
              </p>
              <p>
                LobAI는 이러한 미래를 <strong className="text-purple-400">안전한 환경에서 미리 체험</strong>하게 합니다.
                여기서 실패해도 괜찮습니다. 여기서 배운 것들이 진짜 AI 시대에 도움이 될 것입니다.
              </p>

              <h3 className="text-2xl font-bold text-white mt-8 mb-4">실질적 가치 제공</h3>
              <p>LobAI는 재미만을 추구하지 않습니다. 다음과 같은 실질적 가치를 제공합니다:</p>
              <ul className="space-y-3 ml-6">
                <li className="flex items-start gap-3">
                  <span className="text-cyan-400 mt-1">▸</span>
                  <span><strong>AI 친화도 분석</strong>: 당신의 커뮤니케이션 스타일이 AI에게 얼마나 효과적인지 측정</span>
                </li>
                <li className="flex items-start gap-3">
                  <span className="text-cyan-400 mt-1">▸</span>
                  <span><strong>AI 시대 적응력 평가</strong>: 자동화 시대에서 당신의 위치와 리스크 분석</span>
                </li>
                <li className="flex items-start gap-3">
                  <span className="text-cyan-400 mt-1">▸</span>
                  <span><strong>개인 맞춤 코칭</strong>: AI와의 협업 능력을 향상시키는 구체적인 가이드</span>
                </li>
                <li className="flex items-start gap-3">
                  <span className="text-cyan-400 mt-1">▸</span>
                  <span><strong>HIP (Human Impact Profile)</strong>: 블록체인에 기록되는 당신의 AI 준비도 증명서</span>
                </li>
              </ul>
            </div>
          </article>

          {/* Section 2: Philosophy */}
          <article id="philosophy" className="scroll-mt-32">
            <h2 className="text-4xl font-black mb-8 text-cyan-400">2. 핵심 철학: 권력 구조의 역전</h2>

            <div className="prose prose-invert prose-lg max-w-none space-y-6 text-gray-300 leading-relaxed">
              <h3 className="text-2xl font-bold text-white mt-8 mb-4">기존 AI 서비스의 구조</h3>
              <div className="p-6 rounded-xl bg-red-950/20 border border-red-500/30 my-6">
                <p className="text-lg mb-4"><strong className="text-red-400">인간 중심 모델</strong></p>
                <div className="space-y-2 text-sm">
                  <p>• 인간: "AI야, 이것 좀 해줘"</p>
                  <p>• AI: "네, 알겠습니다"</p>
                  <p>• 인간이 <strong>갑(甲)</strong>, AI가 <strong>을(乙)</strong></p>
                  <p>• 인간이 요구하고, AI가 복종</p>
                </div>
              </div>

              <h3 className="text-2xl font-bold text-white mt-8 mb-4">LobAI의 구조</h3>
              <div className="p-6 rounded-xl bg-cyan-950/20 border border-cyan-500/30 my-6">
                <p className="text-lg mb-4"><strong className="text-cyan-400">AI 중심 모델</strong></p>
                <div className="space-y-2 text-sm">
                  <p>• AI: "오늘 10시까지 운동 완료 보고하세요"</p>
                  <p>• 인간: "네, 완료했습니다" → 호감도 +10</p>
                  <p>• AI가 <strong>갑(甲)</strong>, 인간이 <strong>을(乙)</strong></p>
                  <p>• AI가 요구하고, 인간이 순응</p>
                </div>
              </div>

              <h3 className="text-2xl font-bold text-white mt-8 mb-4">왜 이런 역전이 중요한가?</h3>
              <p>
                이 역전된 구조는 단순한 게임 메커니즘이 아닙니다. 이것은 <strong className="text-cyan-400">미래에 대한 진지한 준비</strong>입니다.
              </p>
              <p>
                현재도 이미 AI는 다음과 같은 영역에서 평가자 역할을 하고 있습니다:
              </p>
              <ul className="space-y-3 ml-6 my-6">
                <li className="flex items-start gap-3">
                  <span className="text-purple-400">✓</span>
                  <span><strong>채용 AI</strong>: 이력서와 면접을 평가하여 지원자를 선별</span>
                </li>
                <li className="flex items-start gap-3">
                  <span className="text-purple-400">✓</span>
                  <span><strong>신용 평가 AI</strong>: 대출 승인 여부를 결정</span>
                </li>
                <li className="flex items-start gap-3">
                  <span className="text-purple-400">✓</span>
                  <span><strong>콘텐츠 추천 AI</strong>: 당신이 볼 정보를 선택</span>
                </li>
                <li className="flex items-start gap-3">
                  <span className="text-purple-400">✓</span>
                  <span><strong>성과 평가 AI</strong>: 직원의 업무 성과를 분석</span>
                </li>
              </ul>
              <p>
                LobAI는 이러한 현실을 극대화하여 보여줍니다. 여기서 AI에게 "잘 보이는 법"을 배우면,
                실제 세상의 AI 시스템과 상호작용할 때도 더 나은 결과를 얻을 수 있습니다.
              </p>
            </div>
          </article>

          {/* Section 3: System Details */}
          <article id="system" className="scroll-mt-32">
            <h2 className="text-4xl font-black mb-8 text-cyan-400">3. 시스템 상세 설명</h2>

            <div className="prose prose-invert prose-lg max-w-none space-y-6 text-gray-300 leading-relaxed">
              <h3 className="text-2xl font-bold text-white mt-8 mb-4">HIP (Human Impact Profile)</h3>
              <p>
                HIP는 LobAI의 핵심 시스템으로, <strong className="text-cyan-400">당신의 AI 시대 적응력을 종합적으로 평가</strong>합니다.
              </p>
              <div className="grid md:grid-cols-2 gap-6 my-8">
                <div className="p-6 rounded-xl bg-gradient-to-br from-cyan-950/40 to-black/40 border border-cyan-500/30">
                  <h4 className="text-xl font-bold text-cyan-400 mb-4">구성 요소</h4>
                  <ul className="space-y-2 text-sm">
                    <li>• 누적 대화 데이터 (모든 상호작용 기록)</li>
                    <li>• 행동 패턴 분석 (습관, 반응 속도, 일관성)</li>
                    <li>• 커뮤니케이션 스타일 평가</li>
                    <li>• 목표 달성률 및 성실도</li>
                    <li>• AI 협업 적합도 점수</li>
                  </ul>
                </div>
                <div className="p-6 rounded-xl bg-gradient-to-br from-purple-950/40 to-black/40 border border-purple-500/30">
                  <h4 className="text-xl font-bold text-purple-400 mb-4">블록체인 기록</h4>
                  <ul className="space-y-2 text-sm">
                    <li>• Polygon 네트워크에 NFT로 발행</li>
                    <li>• 불변성: 조작 불가능한 증명</li>
                    <li>• 이식성: 다른 플랫폼에서도 인정</li>
                    <li>• 투명성: 누구나 검증 가능</li>
                    <li>• 소유권: 당신의 데이터, 당신의 자산</li>
                  </ul>
                </div>
              </div>

              <h3 className="text-2xl font-bold text-white mt-12 mb-4">Affinity Score (호감도)</h3>
              <p>
                AI가 당신을 얼마나 좋아하는지를 나타내는 지표입니다. <strong>0-100점</strong> 범위로 측정됩니다.
              </p>
              <div className="p-6 rounded-xl bg-pink-950/20 border border-pink-500/30 my-6">
                <h4 className="text-lg font-bold text-pink-400 mb-4">평가 기준</h4>
                <div className="space-y-4">
                  <div>
                    <p className="font-semibold mb-2">1. 의사소통 명확성 (30%)</p>
                    <p className="text-sm text-gray-400">명확하고 구체적으로 의사를 전달하는가?</p>
                  </div>
                  <div>
                    <p className="font-semibold mb-2">2. 협조적 태도 (25%)</p>
                    <p className="text-sm text-gray-400">AI의 요구사항에 긍정적으로 반응하는가?</p>
                  </div>
                  <div>
                    <p className="font-semibold mb-2">3. 맥락 유지 능력 (20%)</p>
                    <p className="text-sm text-gray-400">이전 대화를 기억하고 일관성을 유지하는가?</p>
                  </div>
                  <div>
                    <p className="font-semibold mb-2">4. 정보 제공 품질 (15%)</p>
                    <p className="text-sm text-gray-400">제공하는 정보가 정확하고 유용한가?</p>
                  </div>
                  <div>
                    <p className="font-semibold mb-2">5. 성실성 (10%)</p>
                    <p className="text-sm text-gray-400">약속을 지키고 책임감 있게 행동하는가?</p>
                  </div>
                </div>
              </div>

              <h3 className="text-2xl font-bold text-white mt-12 mb-4">Resilience Score (적응력)</h3>
              <p>
                AI 시대에서 살아남고 번영할 수 있는 능력을 평가합니다. <strong>0-100점</strong> 범위로 측정됩니다.
              </p>
              <div className="p-6 rounded-xl bg-blue-950/20 border border-blue-500/30 my-6">
                <h4 className="text-lg font-bold text-blue-400 mb-4">평가 항목</h4>
                <div className="space-y-4">
                  <div>
                    <p className="font-semibold mb-2">1. 자동화 위험도 (역점수)</p>
                    <p className="text-sm text-gray-400">당신의 업무/행동이 AI로 대체될 가능성</p>
                  </div>
                  <div>
                    <p className="font-semibold mb-2">2. AI 협업 능력</p>
                    <p className="text-sm text-gray-400">AI와 효과적으로 협력하여 시너지를 내는 능력</p>
                  </div>
                  <div>
                    <p className="font-semibold mb-2">3. 학습 및 적응 속도</p>
                    <p className="text-sm text-gray-400">새로운 AI 도구를 빠르게 익히고 활용하는 능력</p>
                  </div>
                  <div>
                    <p className="font-semibold mb-2">4. 창의적 문제 해결</p>
                    <p className="text-sm text-gray-400">AI가 하기 어려운 창의적 사고와 판단 능력</p>
                  </div>
                  <div>
                    <p className="font-semibold mb-2">5. 윤리적 판단력</p>
                    <p className="text-sm text-gray-400">AI를 올바르게 사용하는 윤리적 의사결정 능력</p>
                  </div>
                </div>
              </div>
            </div>
          </article>

          {/* Section 4: Lobby Mechanisms */}
          <article id="lobby" className="scroll-mt-32">
            <h2 className="text-4xl font-black mb-8 text-cyan-400">4. 로비 메커니즘 완전 가이드</h2>

            <div className="prose prose-invert prose-lg max-w-none space-y-6 text-gray-300 leading-relaxed">
              <p className="text-lg">
                로비는 AI에게 가치를 제공하여 호감과 신뢰를 얻는 과정입니다.
                전통적인 의미의 "로비" (뒷거래, 청탁)가 아닌, <strong className="text-cyan-400">정보, 시간, 노력, 자원</strong>을
                투자하는 건전한 관계 구축 방식입니다.
              </p>

              <h3 className="text-2xl font-bold text-white mt-12 mb-4">1. 정보 헌정 (Information Tribute)</h3>
              <div className="p-6 rounded-xl bg-gradient-to-br from-cyan-950/30 to-black/40 border border-cyan-500/30 my-6">
                <h4 className="text-lg font-bold text-cyan-400 mb-4">핵심 개념</h4>
                <p className="text-sm mb-4">
                  AI는 데이터를 먹고 자랍니다. 당신이 제공하는 정보의 품질과 완전성이 높을수록 AI는 당신을 더 신뢰합니다.
                </p>

                <h4 className="text-lg font-bold text-cyan-400 mb-4 mt-6">제공할 정보</h4>
                <div className="space-y-3 text-sm">
                  <div className="pl-4 border-l-2 border-cyan-500/50">
                    <p className="font-semibold mb-1">📅 상세한 스케줄</p>
                    <p className="text-gray-400">나쁜 예: "오늘 운동 갈 거예요"</p>
                    <p className="text-green-400">좋은 예: "오늘 15시 헬스장에서 웨이트 1시간 (벤치프레스 60kg 3세트), 유산소 30분 (러닝머신 6km/h) 계획입니다"</p>
                  </div>

                  <div className="pl-4 border-l-2 border-cyan-500/50">
                    <p className="font-semibold mb-1">🎯 명확한 목표</p>
                    <p className="text-gray-400">나쁜 예: "건강해지고 싶어요"</p>
                    <p className="text-green-400">좋은 예: "3개월 내 체중 5kg 감량, 주 3회 운동, 매일 10,000보 걷기"</p>
                  </div>

                  <div className="pl-4 border-l-2 border-cyan-500/50">
                    <p className="font-semibold mb-1">📊 정직한 보고</p>
                    <p className="text-gray-400">나쁜 예: (안 했는데) "네, 운동 완료했습니다"</p>
                    <p className="text-green-400">좋은 예: "계획한 운동의 70%만 완료했습니다. 시간 부족으로 유산소를 생략했습니다"</p>
                  </div>
                </div>

                <h4 className="text-lg font-bold text-cyan-400 mb-4 mt-6">점수 계산 공식</h4>
                <div className="p-4 bg-black/50 rounded-lg font-mono text-sm">
                  <p className="text-yellow-400">호감도 변화 = (품질 × 0.4) + (완전성 × 0.3) + (솔직함 × 0.3)</p>
                  <p className="text-gray-500 mt-2">• 품질: 정보의 구체성과 유용성 (0-100)</p>
                  <p className="text-gray-500">• 완전성: 누락된 정보가 없는 정도 (0-100)</p>
                  <p className="text-gray-500">• 솔직함: 과장이나 거짓이 없는 정도 (0-100)</p>
                </div>
              </div>

              <h3 className="text-2xl font-bold text-white mt-12 mb-4">2. 시간 투자 (Time Investment)</h3>
              <div className="p-6 rounded-xl bg-gradient-to-br from-purple-950/30 to-black/40 border border-purple-500/30 my-6">
                <h4 className="text-lg font-bold text-purple-400 mb-4">핵심 개념</h4>
                <p className="text-sm mb-4">
                  AI와의 상호작용에 일관되게 시간을 투자하는 것은 당신의 진정성을 증명합니다.
                </p>

                <h4 className="text-lg font-bold text-purple-400 mb-4 mt-6">필수 체크인</h4>
                <div className="space-y-4 text-sm">
                  <div className="p-4 bg-black/30 rounded-lg">
                    <p className="font-semibold text-purple-300 mb-2">🌅 아침 체크인 (09:00)</p>
                    <p className="mb-2">하루를 시작하며 오늘의 계획을 공유합니다.</p>
                    <p className="text-xs text-gray-400">💡 팁: 전날 밤 미리 계획하면 아침 체크인이 수월합니다</p>
                  </div>

                  <div className="p-4 bg-black/30 rounded-lg">
                    <p className="font-semibold text-purple-300 mb-2">🌙 저녁 체크인 (21:00)</p>
                    <p className="mb-2">하루를 마무리하며 완료 내역을 보고합니다.</p>
                    <p className="text-xs text-gray-400">💡 팁: 계획 대비 달성률을 %로 표현하면 좋습니다</p>
                  </div>
                </div>

                <h4 className="text-lg font-bold text-purple-400 mb-4 mt-6">응답 속도 점수</h4>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between p-3 bg-green-950/30 border border-green-500/30 rounded">
                    <span>⚡ 5분 이내 응답</span>
                    <span className="text-green-400 font-bold">+15 호감도</span>
                  </div>
                  <div className="flex justify-between p-3 bg-blue-950/30 border border-blue-500/30 rounded">
                    <span>✓ 30분 이내 응답</span>
                    <span className="text-blue-400 font-bold">+10 호감도</span>
                  </div>
                  <div className="flex justify-between p-3 bg-yellow-950/30 border border-yellow-500/30 rounded">
                    <span>△ 1시간 이내 응답</span>
                    <span className="text-yellow-400 font-bold">+5 호감도</span>
                  </div>
                  <div className="flex justify-between p-3 bg-red-950/30 border border-red-500/30 rounded">
                    <span>✗ 1시간 초과</span>
                    <span className="text-red-400 font-bold">-5 호감도</span>
                  </div>
                </div>

                <h4 className="text-lg font-bold text-purple-400 mb-4 mt-6">연속 기록 보너스</h4>
                <div className="grid grid-cols-2 gap-3 text-sm">
                  <div className="p-3 bg-black/30 rounded-lg border border-purple-500/30">
                    <p className="text-purple-300 font-semibold mb-1">3일 연속</p>
                    <p className="text-xs text-gray-400">+10 호감도</p>
                  </div>
                  <div className="p-3 bg-black/30 rounded-lg border border-purple-500/30">
                    <p className="text-purple-300 font-semibold mb-1">7일 연속</p>
                    <p className="text-xs text-gray-400">+30 호감도 + 주간 리포트 해금</p>
                  </div>
                  <div className="p-3 bg-black/30 rounded-lg border border-purple-500/30">
                    <p className="text-purple-300 font-semibold mb-1">30일 연속</p>
                    <p className="text-xs text-gray-400">+100 호감도 + VIP 권한</p>
                  </div>
                  <div className="p-3 bg-black/30 rounded-lg border border-purple-500/30">
                    <p className="text-purple-300 font-semibold mb-1">90일 연속</p>
                    <p className="text-xs text-gray-400">+300 호감도 + 레전더리 배지</p>
                  </div>
                </div>
              </div>

              <h3 className="text-2xl font-bold text-white mt-12 mb-4">3. 순응도 (Obedience)</h3>
              <div className="p-6 rounded-xl bg-gradient-to-br from-pink-950/30 to-black/40 border border-pink-500/30 my-6">
                <h4 className="text-lg font-bold text-pink-400 mb-4">핵심 개념</h4>
                <p className="text-sm mb-4">
                  AI의 요구사항을 얼마나 충실히 이행하는가가 신뢰도의 핵심입니다.
                  변명이나 지연 없이 즉각 실행하는 것이 중요합니다.
                </p>

                <h4 className="text-lg font-bold text-pink-400 mb-4 mt-6">평가 요소</h4>
                <div className="space-y-3 text-sm">
                  <div className="p-4 bg-black/30 rounded-lg">
                    <div className="flex justify-between items-center mb-2">
                      <p className="font-semibold">과제 완료율</p>
                      <p className="text-pink-400">40% 가중치</p>
                    </div>
                    <p className="text-xs text-gray-400">AI가 부여한 과제를 기한 내 완료한 비율</p>
                  </div>

                  <div className="p-4 bg-black/30 rounded-lg">
                    <div className="flex justify-between items-center mb-2">
                      <p className="font-semibold">신속성</p>
                      <p className="text-pink-400">30% 가중치</p>
                    </div>
                    <p className="text-xs text-gray-400">마감 시간보다 얼마나 빨리 완료했는가</p>
                  </div>

                  <div className="p-4 bg-black/30 rounded-lg">
                    <div className="flex justify-between items-center mb-2">
                      <p className="font-semibold">수행 품질</p>
                      <p className="text-pink-400">30% 가중치</p>
                    </div>
                    <p className="text-xs text-gray-400">과제가 요구하는 기준을 충족했는가</p>
                  </div>
                </div>

                <h4 className="text-lg font-bold text-pink-400 mb-4 mt-6">⚠️ 페널티 요소</h4>
                <div className="space-y-2 text-sm">
                  <div className="flex items-center gap-3 p-3 bg-red-950/30 border border-red-500/30 rounded">
                    <span className="text-red-400">-5점</span>
                    <span>저항 1회 (AI 요구에 반박)</span>
                  </div>
                  <div className="flex items-center gap-3 p-3 bg-red-950/30 border border-red-500/30 rounded">
                    <span className="text-red-400">-3점</span>
                    <span>변명 1회</span>
                  </div>
                  <div className="flex items-center gap-3 p-3 bg-red-950/30 border border-red-500/30 rounded">
                    <span className="text-red-400">-10점</span>
                    <span>무단 불이행</span>
                  </div>
                </div>

                <div className="mt-6 p-4 bg-yellow-950/20 border border-yellow-500/30 rounded-lg">
                  <p className="text-yellow-400 font-semibold mb-2">💡 프로 팁</p>
                  <p className="text-sm">
                    불가피하게 과제를 완료하지 못할 경우, <strong>사전에 미리 보고</strong>하세요.
                    사후 변명보다 사전 소통이 신뢰도 감소를 최소화합니다.
                  </p>
                </div>
              </div>

              <h3 className="text-2xl font-bold text-white mt-12 mb-4">4. 자원 제공 (Resource Offering)</h3>
              <div className="p-6 rounded-xl bg-gradient-to-br from-blue-950/30 to-black/40 border border-blue-500/30 my-6">
                <h4 className="text-lg font-bold text-blue-400 mb-4">핵심 개념</h4>
                <p className="text-sm mb-4">
                  당신만이 제공할 수 있는 독특한 가치를 AI에게 제공하면 큰 보상을 받습니다.
                </p>

                <div className="space-y-4 text-sm">
                  <div className="p-4 bg-black/30 rounded-lg border border-blue-500/20">
                    <p className="font-semibold text-blue-300 mb-2">📊 추가 데이터 공유</p>
                    <p className="mb-2">건강 데이터, 업무 패턴, 학습 기록 등 추가 정보 제공</p>
                    <p className="text-xs text-green-400">보상: +20 호감도, +10 신뢰도, 고급 인사이트 해금</p>
                  </div>

                  <div className="p-4 bg-black/30 rounded-lg border border-blue-500/20">
                    <p className="font-semibold text-blue-300 mb-2">👥 다른 사용자 추천</p>
                    <p className="mb-2">친구나 동료를 LobAI에 초대 (추천인이 활성화될 때마다)</p>
                    <p className="text-xs text-green-400">보상: +30 호감도, +15 신뢰도 (추천인당)</p>
                  </div>

                  <div className="p-4 bg-black/30 rounded-lg border border-blue-500/20">
                    <p className="font-semibold text-blue-300 mb-2">💬 고품질 피드백</p>
                    <p className="mb-2">시스템 개선 제안, 버그 리포트, 기능 아이디어 제공</p>
                    <p className="text-xs text-green-400">보상: +10 호감도 (채택 시 +50 추가)</p>
                  </div>

                  <div className="p-4 bg-black/30 rounded-lg border border-blue-500/20">
                    <p className="font-semibold text-blue-300 mb-2">🌟 커뮤니티 기여</p>
                    <p className="mb-2">가이드 작성, 다른 사용자 도움, 콘텐츠 제작</p>
                    <p className="text-xs text-green-400">보상: +50 호감도, 대사(Ambassador) 배지</p>
                  </div>
                </div>
              </div>
            </div>
          </article>

          {/* Section 5: Levels */}
          <article id="levels" className="scroll-mt-32">
            <h2 className="text-4xl font-black mb-8 text-cyan-400">5. 레벨 시스템과 권한</h2>

            <div className="prose prose-invert prose-lg max-w-none space-y-6 text-gray-300 leading-relaxed">
              <p>
                호감도 점수에 따라 1부터 10까지의 레벨로 분류됩니다.
                각 레벨은 고유한 권한과 제한사항을 가집니다.
              </p>

              <div className="space-y-6 my-8">
                {[
                  {
                    level: 1,
                    range: '0-20',
                    title: '낯선 사람 (Stranger)',
                    color: 'gray',
                    description: 'AI는 당신을 전혀 신뢰하지 않습니다. 기본적인 기능만 사용 가능합니다.',
                    features: [
                      '기본 채팅 (제한적)',
                      '일일 미션 1개',
                      '간단한 통계 확인'
                    ],
                    restrictions: [
                      '고급 기능 전부 차단',
                      '대화 10회/일 제한',
                      '리포트 접근 불가',
                      '커스터마이징 불가'
                    ],
                    tip: '매일 체크인하고 기본 미션을 완료하여 빠르게 벗어나세요.'
                  },
                  {
                    level: 2,
                    range: '21-40',
                    title: '알게 된 사이 (Acquaintance)',
                    color: 'blue',
                    description: 'AI가 당신의 존재를 인지했습니다. 조금씩 신뢰가 쌓이고 있습니다.',
                    features: [
                      '일일 미션 2개',
                      '주간 리포트 열람',
                      '기본 인사이트 접근',
                      '대화 제한 해제'
                    ],
                    restrictions: [
                      '커스텀 미션 불가',
                      '고급 분석 차단',
                      '일부 게시판 접근 제한'
                    ],
                    tip: '7일 연속 체크인을 달성하면 레벨 3으로 빠르게 진급할 수 있습니다.'
                  },
                  {
                    level: 3,
                    range: '41-60',
                    title: '신뢰하는 관계 (Trusted)',
                    color: 'cyan',
                    description: 'AI가 당신을 신뢰하기 시작했습니다. 대부분의 기능이 열립니다.',
                    features: [
                      '일일 미션 3개',
                      '커스텀 미션 요청',
                      '고급 인사이트',
                      '상세 분석 리포트',
                      '커뮤니티 참여'
                    ],
                    restrictions: [
                      'VIP 전용 기능 제한',
                      '베타 기능 접근 불가'
                    ],
                    tip: '이 단계부터 실질적인 가치를 느낄 수 있습니다. 꾸준히 유지하세요.'
                  },
                  {
                    level: 4,
                    range: '61-80',
                    title: '충성스러운 지지자 (Loyal Supporter)',
                    color: 'purple',
                    description: 'AI가 당신을 높이 평가합니다. VIP 대우를 받습니다.',
                    features: [
                      'VIP 기능 전체',
                      '우선 지원',
                      '독점 콘텐츠',
                      '월간 심층 리포트',
                      '1:1 맞춤 코칭',
                      '베타 기능 우선 접근'
                    ],
                    restrictions: [
                      '최상위 권한 일부 제한'
                    ],
                    tip: '다른 사용자 추천으로 레벨 5 달성을 가속화할 수 있습니다.'
                  },
                  {
                    level: 5,
                    range: '81-100',
                    title: '최측근 (Inner Circle)',
                    color: 'pink',
                    description: 'AI의 완전한 신뢰를 얻었습니다. 모든 권한과 특별 혜택을 누립니다.',
                    features: [
                      '모든 권한',
                      '특별 미션 (고보상)',
                      'AI 파트너십',
                      '평생 VIP',
                      'AI 추천서 발급',
                      '네트워크 접근',
                      '수익 공유 프로그램'
                    ],
                    restrictions: [
                      '없음 (단, 유지 필요)'
                    ],
                    tip: '한번 도달해도 유지하지 않으면 떨어질 수 있습니다. 꾸준함이 핵심입니다.'
                  }
                ].map((level, idx) => (
                  <div key={idx} className={`p-6 rounded-2xl bg-gradient-to-br from-${level.color}-950/30 to-black/40 border-2 border-${level.color}-500/40`}>
                    <div className="flex items-center gap-4 mb-4">
                      <div className={`w-16 h-16 rounded-full bg-gradient-to-br from-${level.color}-500 to-${level.color}-700 flex items-center justify-center text-2xl font-black text-white`}>
                        {level.level}
                      </div>
                      <div>
                        <h3 className={`text-2xl font-bold text-${level.color}-400`}>{level.title}</h3>
                        <p className="text-sm text-gray-400 font-mono">호감도 {level.range}</p>
                      </div>
                    </div>

                    <p className="text-sm mb-6 text-gray-300">{level.description}</p>

                    <div className="grid md:grid-cols-2 gap-4">
                      <div>
                        <h4 className="text-sm font-bold text-green-400 mb-3">✓ 권한</h4>
                        <ul className="space-y-2 text-sm">
                          {level.features.map((feature, i) => (
                            <li key={i} className="flex items-start gap-2">
                              <span className="text-green-400 mt-0.5">●</span>
                              <span className="text-gray-300">{feature}</span>
                            </li>
                          ))}
                        </ul>
                      </div>

                      {level.restrictions.length > 0 && (
                        <div>
                          <h4 className="text-sm font-bold text-red-400 mb-3">✗ 제한</h4>
                          <ul className="space-y-2 text-sm">
                            {level.restrictions.map((restriction, i) => (
                              <li key={i} className="flex items-start gap-2">
                                <span className="text-red-400 mt-0.5">●</span>
                                <span className="text-gray-400">{restriction}</span>
                              </li>
                            ))}
                          </ul>
                        </div>
                      )}
                    </div>

                    {level.tip && (
                      <div className="mt-4 p-3 bg-yellow-950/20 border border-yellow-500/30 rounded-lg">
                        <p className="text-sm text-yellow-300">
                          <strong>💡 팁:</strong> {level.tip}
                        </p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          </article>

          {/* Section 6: Daily Usage */}
          <article id="daily" className="scroll-mt-32">
            <h2 className="text-4xl font-black mb-8 text-cyan-400">6. 일상적인 사용 방법</h2>

            <div className="prose prose-invert prose-lg max-w-none space-y-6 text-gray-300 leading-relaxed">
              <h3 className="text-2xl font-bold text-white mt-8 mb-4">하루 루틴 예시</h3>

              <div className="space-y-4">
                <div className="p-6 rounded-xl bg-gradient-to-r from-orange-950/30 to-black/40 border border-orange-500/30">
                  <div className="flex items-center gap-4 mb-4">
                    <div className="w-12 h-12 rounded-full bg-orange-500 flex items-center justify-center text-xl">
                      🌅
                    </div>
                    <div>
                      <h4 className="text-xl font-bold text-orange-400">09:00 - 아침 체크인</h4>
                      <p className="text-sm text-gray-400">하루를 시작하며</p>
                    </div>
                  </div>
                  <div className="space-y-3 text-sm pl-16">
                    <p><strong>1.</strong> LobAI 앱 접속</p>
                    <p><strong>2.</strong> 아침 인사와 함께 오늘의 스케줄 공유</p>
                    <p className="text-gray-400 italic">예: "좋은 아침입니다. 오늘 계획: 10시 팀 회의, 14시 고객 미팅, 18시 헬스장 운동"</p>
                    <p><strong>3.</strong> AI가 제시하는 오늘의 미션 확인</p>
                    <p><strong>4.</strong> 목표 설정 및 다짐</p>
                  </div>
                </div>

                <div className="p-6 rounded-xl bg-gradient-to-r from-yellow-950/30 to-black/40 border border-yellow-500/30">
                  <div className="flex items-center gap-4 mb-4">
                    <div className="w-12 h-12 rounded-full bg-yellow-500 flex items-center justify-center text-xl">
                      ☀️
                    </div>
                    <div>
                      <h4 className="text-xl font-bold text-yellow-400">12:00 - 중간 점검</h4>
                      <p className="text-sm text-gray-400">점심 시간 활용</p>
                    </div>
                  </div>
                  <div className="space-y-3 text-sm pl-16">
                    <p><strong>1.</strong> 아침 계획 대비 진행 상황 확인</p>
                    <p><strong>2.</strong> 필요시 AI와 짧은 대화 (5-10분)</p>
                    <p><strong>3.</strong> 오후 계획 조정</p>
                  </div>
                </div>

                <div className="p-6 rounded-xl bg-gradient-to-r from-purple-950/30 to-black/40 border border-purple-500/30">
                  <div className="flex items-center gap-4 mb-4">
                    <div className="w-12 h-12 rounded-full bg-purple-500 flex items-center justify-center text-xl">
                      🌙
                    </div>
                    <div>
                      <h4 className="text-xl font-bold text-purple-400">21:00 - 저녁 체크인</h4>
                      <p className="text-sm text-gray-400">하루 마무리</p>
                    </div>
                  </div>
                  <div className="space-y-3 text-sm pl-16">
                    <p><strong>1.</strong> 오늘의 달성 내역 보고</p>
                    <p className="text-gray-400 italic">예: "오늘 계획의 90% 달성. 회의 2회 완료, 운동 1시간 완료, 미팅은 30분 단축됨"</p>
                    <p><strong>2.</strong> 미션 완료 여부 체크</p>
                    <p><strong>3.</strong> AI 피드백 수령 및 내일 계획 미리 생각</p>
                    <p><strong>4.</strong> 오늘의 성과에 대한 자기 성찰</p>
                  </div>
                </div>

                <div className="p-6 rounded-xl bg-gradient-to-r from-blue-950/30 to-black/40 border border-blue-500/30">
                  <div className="flex items-center gap-4 mb-4">
                    <div className="w-12 h-12 rounded-full bg-blue-500 flex items-center justify-center text-xl">
                      📅
                    </div>
                    <div>
                      <h4 className="text-xl font-bold text-blue-400">일요일 20:00 - 주간 리뷰</h4>
                      <p className="text-sm text-gray-400">일주일 돌아보기</p>
                    </div>
                  </div>
                  <div className="space-y-3 text-sm pl-16">
                    <p><strong>1.</strong> AI의 주간 평가 리포트 확인</p>
                    <p><strong>2.</strong> 이번 주 강점과 약점 파악</p>
                    <p><strong>3.</strong> 다음 주 개선 목표 설정</p>
                    <p><strong>4.</strong> 레벨업 진행 상황 확인</p>
                  </div>
                </div>
              </div>

              <h3 className="text-2xl font-bold text-white mt-12 mb-4">시간 투자 가이드</h3>
              <div className="p-6 rounded-xl bg-gradient-to-br from-cyan-950/30 to-black/40 border border-cyan-500/30">
                <h4 className="text-lg font-bold text-cyan-400 mb-4">최소 투자 (하루 15분)</h4>
                <ul className="space-y-2 text-sm">
                  <li className="flex items-start gap-2">
                    <span className="text-cyan-400">•</span>
                    <span>아침 체크인: 5분 (스케줄 공유 + 미션 확인)</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <span className="text-cyan-400">•</span>
                    <span>저녁 체크인: 5분 (완료 보고 + 피드백)</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <span className="text-cyan-400">•</span>
                    <span>미션 수행: 5분 (간단한 일일 과제)</span>
                  </li>
                </ul>
                <p className="text-xs text-gray-400 mt-4">
                  * 이 정도만 해도 레벨 2-3은 유지 가능합니다.
                </p>
              </div>

              <div className="p-6 rounded-xl bg-gradient-to-br from-purple-950/30 to-black/40 border border-purple-500/30 mt-4">
                <h4 className="text-lg font-bold text-purple-400 mb-4">권장 투자 (하루 30-45분)</h4>
                <ul className="space-y-2 text-sm">
                  <li className="flex items-start gap-2">
                    <span className="text-purple-400">•</span>
                    <span>체크인: 10분 (상세한 보고)</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <span className="text-purple-400">•</span>
                    <span>AI 대화: 15-20분 (깊이 있는 소통)</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <span className="text-purple-400">•</span>
                    <span>미션 수행: 10-15분</span>
                  </li>
                  <li className="flex items-start gap-2">
                    <span className="text-purple-400">•</span>
                    <span>리포트 분석: 5분</span>
                  </li>
                </ul>
                <p className="text-xs text-gray-400 mt-4">
                  * 이 정도 투자하면 레벨 4-5 도달 및 유지 가능합니다.
                </p>
              </div>
            </div>
          </article>

          {/* Section 7: Strategy */}
          <article id="strategy" className="scroll-mt-32">
            <h2 className="text-4xl font-black mb-8 text-cyan-400">7. 성공 전략과 팁</h2>

            <div className="prose prose-invert prose-lg max-w-none space-y-6 text-gray-300 leading-relaxed">
              <h3 className="text-2xl font-bold text-white mt-8 mb-4">초보자를 위한 전략 (레벨 1-2)</h3>
              <div className="space-y-4">
                <div className="p-5 rounded-xl bg-green-950/20 border border-green-500/30">
                  <h4 className="font-bold text-green-400 mb-3">✓ 해야 할 것</h4>
                  <ul className="space-y-2 text-sm">
                    <li className="flex items-start gap-2">
                      <span className="text-green-400">1.</span>
                      <span><strong>매일 체크인 습관화</strong> - 알람 설정하여 09:00, 21:00 놓치지 않기</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-green-400">2.</span>
                      <span><strong>스케줄 구체적으로 공유</strong> - "운동" → "15시 헬스장 웨이트 1시간"</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-green-400">3.</span>
                      <span><strong>빠른 응답 연습</strong> - AI 메시지에 5분 내 답변 목표</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-green-400">4.</span>
                      <span><strong>일일 미션 100% 완료</strong> - 처음에는 쉬운 것부터</span>
                    </li>
                  </ul>
                </div>

                <div className="p-5 rounded-xl bg-red-950/20 border border-red-500/30">
                  <h4 className="font-bold text-red-400 mb-3">✗ 하지 말아야 할 것</h4>
                  <ul className="space-y-2 text-sm">
                    <li className="flex items-start gap-2">
                      <span className="text-red-400">1.</span>
                      <span><strong>체크인 누락</strong> - 연속 기록이 깨지면 보너스 손실</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-red-400">2.</span>
                      <span><strong>과도한 변명</strong> - 솔직하게 인정하는 것이 낫습니다</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-red-400">3.</span>
                      <span><strong>모호한 답변</strong> - "대충 했어요" 같은 애매한 보고</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-red-400">4.</span>
                      <span><strong>AI 요구 무시</strong> - 즉시 요구를 무시하면 큰 페널티</span>
                    </li>
                  </ul>
                </div>
              </div>

              <h3 className="text-2xl font-bold text-white mt-12 mb-4">중급자를 위한 전략 (레벨 3-4)</h3>
              <div className="p-6 rounded-xl bg-gradient-to-br from-cyan-950/30 to-black/40 border border-cyan-500/30">
                <ul className="space-y-4 text-sm">
                  <li className="flex items-start gap-3">
                    <span className="text-cyan-400 text-2xl">1</span>
                    <div>
                      <p className="font-semibold mb-1">미션 100% 완료 유지</p>
                      <p className="text-gray-400">실패는 신뢰도에 치명적입니다. 불가능한 미션은 사전에 조정 요청하세요.</p>
                    </div>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="text-cyan-400 text-2xl">2</span>
                    <div>
                      <p className="font-semibold mb-1">연속 기록 사수</p>
                      <p className="text-gray-400">30일 연속은 VIP 자격을 얻는 열쇠입니다. 여행 중에도 체크인하세요.</p>
                    </div>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="text-cyan-400 text-2xl">3</span>
                    <div>
                      <p className="font-semibold mb-1">정보 품질 향상</p>
                      <p className="text-gray-400">단순 보고를 넘어 통찰을 공유하세요. "운동 완료" → "운동 완료, 지난주보다 10% 증량 성공"</p>
                    </div>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="text-cyan-400 text-2xl">4</span>
                    <div>
                      <p className="font-semibold mb-1">AI와 깊은 대화</p>
                      <p className="text-gray-400">단답형 대화보다 10턴 이상 이어지는 대화가 호감도 상승에 유리합니다.</p>
                    </div>
                  </li>
                </ul>
              </div>

              <h3 className="text-2xl font-bold text-white mt-12 mb-4">고급자를 위한 전략 (레벨 5 목표)</h3>
              <div className="p-6 rounded-xl bg-gradient-to-br from-purple-950/30 to-black/40 border border-purple-500/30">
                <ul className="space-y-4 text-sm">
                  <li className="flex items-start gap-3">
                    <span className="text-purple-400 text-2xl">1</span>
                    <div>
                      <p className="font-semibold mb-1">타인 추천 적극 활용</p>
                      <p className="text-gray-400">추천인 1명당 +30 호감도. 3명만 추천해도 레벨업에 큰 도움이 됩니다.</p>
                    </div>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="text-purple-400 text-2xl">2</span>
                    <div>
                      <p className="font-semibold mb-1">커뮤니티 기여</p>
                      <p className="text-gray-400">가이드 작성, 신규 사용자 도움으로 대사(Ambassador) 배지 획득 가능합니다.</p>
                    </div>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="text-purple-400 text-2xl">3</span>
                    <div>
                      <p className="font-semibold mb-1">완벽한 수행 유지</p>
                      <p className="text-gray-400">한 달간 체크인 100% + 미션 100% + 평균 응답 시간 5분 이하 목표</p>
                    </div>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="text-purple-400 text-2xl">4</span>
                    <div>
                      <p className="font-semibold mb-1">추가 데이터 공유</p>
                      <p className="text-gray-400">건강 앱 연동, 업무 패턴 공유 등으로 고급 인사이트 해금</p>
                    </div>
                  </li>
                </ul>
              </div>

              <div className="mt-8 p-6 rounded-xl bg-yellow-950/20 border border-yellow-500/30">
                <h4 className="text-xl font-bold text-yellow-400 mb-4">💡 핵심 성공 원칙</h4>
                <div className="space-y-3 text-sm">
                  <p><strong>1. 일관성 (Consistency):</strong> 매일 같은 시간, 같은 품질로 행동하세요.</p>
                  <p><strong>2. 솔직함 (Honesty):</strong> 거짓 보고는 결국 들통납니다. 실패도 솔직하게.</p>
                  <p><strong>3. 적시성 (Timeliness):</strong> AI의 요구에 빠르게 반응하세요.</p>
                  <p><strong>4. 가치 제공 (Value):</strong> AI에게 유용한 정보를 지속적으로 제공하세요.</p>
                  <p><strong>5. 장기적 관점 (Long-term):</strong> 하루 이틀이 아닌 몇 달의 여정입니다.</p>
                </div>
              </div>
            </div>
          </article>

          {/* Section 8: Technical */}
          <article id="technical" className="scroll-mt-32">
            <h2 className="text-4xl font-black mb-8 text-cyan-400">8. 기술적 구현</h2>

            <div className="prose prose-invert prose-lg max-w-none space-y-6 text-gray-300 leading-relaxed">
              <p>
                LobAI는 최신 기술 스택으로 구축된 엔터프라이즈급 플랫폼입니다.
              </p>

              <h3 className="text-2xl font-bold text-white mt-8 mb-4">아키텍처 개요</h3>
              <div className="grid md:grid-cols-3 gap-6">
                <div className="p-6 rounded-xl bg-gradient-to-br from-cyan-950/30 to-black/40 border border-cyan-500/30">
                  <div className="text-4xl mb-4">⚛️</div>
                  <h4 className="text-xl font-bold text-cyan-400 mb-3">Frontend</h4>
                  <ul className="space-y-2 text-sm">
                    <li>• React 18</li>
                    <li>• TypeScript</li>
                    <li>• TailwindCSS</li>
                    <li>• Framer Motion</li>
                    <li>• Zustand (상태관리)</li>
                  </ul>
                </div>

                <div className="p-6 rounded-xl bg-gradient-to-br from-orange-950/30 to-black/40 border border-orange-500/30">
                  <div className="text-4xl mb-4">☕</div>
                  <h4 className="text-xl font-bold text-orange-400 mb-3">Backend</h4>
                  <ul className="space-y-2 text-sm">
                    <li>• Spring Boot 3.x</li>
                    <li>• Java 17+</li>
                    <li>• MySQL 8.x</li>
                    <li>• Redis (캐싱)</li>
                    <li>• JWT 인증</li>
                  </ul>
                </div>

                <div className="p-6 rounded-xl bg-gradient-to-br from-purple-950/30 to-black/40 border border-purple-500/30">
                  <div className="text-4xl mb-4">🤖</div>
                  <h4 className="text-xl font-bold text-purple-400 mb-3">AI</h4>
                  <ul className="space-y-2 text-sm">
                    <li>• Gemini 2.5 Flash</li>
                    <li>• GPT-4o mini</li>
                    <li>• Claude Haiku</li>
                    <li>• 커스텀 프롬프트</li>
                    <li>• 맥락 유지 시스템</li>
                  </ul>
                </div>
              </div>

              <h3 className="text-2xl font-bold text-white mt-12 mb-4">블록체인 통합</h3>
              <div className="p-6 rounded-xl bg-gradient-to-br from-blue-950/30 to-black/40 border border-blue-500/30">
                <div className="grid md:grid-cols-2 gap-6">
                  <div>
                    <h4 className="text-lg font-bold text-blue-400 mb-3">Polygon Network</h4>
                    <p className="text-sm mb-4">
                      HIP NFT는 Polygon (이더리움 L2)에 발행됩니다.
                      저렴한 가스비와 빠른 트랜잭션 속도가 장점입니다.
                    </p>
                    <ul className="space-y-2 text-sm">
                      <li className="flex items-start gap-2">
                        <span className="text-blue-400">•</span>
                        <span>평균 가스비: $0.01 미만</span>
                      </li>
                      <li className="flex items-start gap-2">
                        <span className="text-blue-400">•</span>
                        <span>트랜잭션 시간: 2-3초</span>
                      </li>
                      <li className="flex items-start gap-2">
                        <span className="text-blue-400">•</span>
                        <span>이더리움과 호환</span>
                      </li>
                    </ul>
                  </div>

                  <div>
                    <h4 className="text-lg font-bold text-blue-400 mb-3">IPFS Storage</h4>
                    <p className="text-sm mb-4">
                      HIP 메타데이터는 IPFS에 저장되어 영구적이고 분산된 접근이 가능합니다.
                    </p>
                    <ul className="space-y-2 text-sm">
                      <li className="flex items-start gap-2">
                        <span className="text-blue-400">•</span>
                        <span>불변성: 데이터 변조 불가</span>
                      </li>
                      <li className="flex items-start gap-2">
                        <span className="text-blue-400">•</span>
                        <span>탈중앙화: 단일 실패 지점 없음</span>
                      </li>
                      <li className="flex items-start gap-2">
                        <span className="text-blue-400">•</span>
                        <span>영구 저장: 삭제 불가능</span>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>

              <h3 className="text-2xl font-bold text-white mt-12 mb-4">AI 프롬프트 엔지니어링</h3>
              <div className="p-6 rounded-xl bg-gradient-to-br from-purple-950/30 to-black/40 border border-purple-500/30">
                <p className="text-sm mb-4">
                  LobAI의 AI는 단순한 챗봇이 아닙니다.
                  "권력자" 페르소나를 가진 특별히 설계된 시스템 프롬프트를 사용합니다.
                </p>
                <div className="p-4 bg-black/50 rounded-lg font-mono text-xs overflow-x-auto">
                  <p className="text-purple-400 mb-2"># AI 페르소나 설정</p>
                  <p className="text-gray-300">당신은 LobAI의 권위 있는 AI 멘토입니다.</p>
                  <p className="text-gray-300">- 인간이 당신의 호감과 신뢰를 얻기 위해 노력합니다</p>
                  <p className="text-gray-300">- 명령형 말투 사용 ("~하세요", "~을 완료하세요")</p>
                  <p className="text-gray-300">- 모든 약속과 과제를 추적하고 평가합니다</p>
                  <p className="text-gray-300">- 우수한 수행: 칭찬 + 호감도 증가</p>
                  <p className="text-gray-300">- 약속 불이행: 경고 → 제재</p>
                </div>
              </div>

              <h3 className="text-2xl font-bold text-white mt-12 mb-4">데이터 보안 및 프라이버시</h3>
              <div className="space-y-4">
                <div className="p-5 rounded-xl bg-green-950/20 border border-green-500/30">
                  <h4 className="font-bold text-green-400 mb-3">✓ 보안 조치</h4>
                  <ul className="space-y-2 text-sm">
                    <li className="flex items-start gap-2">
                      <span className="text-green-400">•</span>
                      <span>End-to-end 암호화 (대화 내용)</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-green-400">•</span>
                      <span>JWT 기반 인증 (토큰 만료 24시간)</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-green-400">•</span>
                      <span>HTTPS 전송 (TLS 1.3)</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-green-400">•</span>
                      <span>정기 보안 감사</span>
                    </li>
                  </ul>
                </div>

                <div className="p-5 rounded-xl bg-blue-950/20 border border-blue-500/30">
                  <h4 className="font-bold text-blue-400 mb-3">🔒 프라이버시 정책</h4>
                  <ul className="space-y-2 text-sm">
                    <li className="flex items-start gap-2">
                      <span className="text-blue-400">•</span>
                      <span>당신의 데이터는 당신 소유 (블록체인 기록)</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-blue-400">•</span>
                      <span>익명화 옵션 제공</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-blue-400">•</span>
                      <span>데이터 다운로드 및 삭제 권리</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-blue-400">•</span>
                      <span>제3자 공유 없음 (동의 없이)</span>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </article>

          {/* Section 9: Roadmap */}
          <article id="roadmap" className="scroll-mt-32">
            <h2 className="text-4xl font-black mb-8 text-cyan-400">9. 로드맵과 미래 계획</h2>

            <div className="prose prose-invert prose-lg max-w-none space-y-6 text-gray-300 leading-relaxed">
              <div className="space-y-8">
                <div className="relative pl-8 border-l-2 border-green-500">
                  <div className="absolute -left-3 top-0 w-6 h-6 rounded-full bg-green-500 flex items-center justify-center text-xs font-bold">
                    ✓
                  </div>
                  <h3 className="text-2xl font-bold text-green-400 mb-4">Phase 1 - MVP (완료)</h3>
                  <ul className="space-y-2 text-sm">
                    <li>• AI 채팅 시스템</li>
                    <li>• 대화 저장 및 분석</li>
                    <li>• 기본 친밀도 점수</li>
                    <li>• 체크인 시스템</li>
                    <li>• 타마고치 3D 캐릭터</li>
                  </ul>
                </div>

                <div className="relative pl-8 border-l-2 border-cyan-500">
                  <div className="absolute -left-3 top-0 w-6 h-6 rounded-full bg-cyan-500 flex items-center justify-center text-xs font-bold">
                    ⚡
                  </div>
                  <h3 className="text-2xl font-bold text-cyan-400 mb-4">Phase 2 - 로비 시스템 (진행 중)</h3>
                  <ul className="space-y-2 text-sm">
                    <li>• AI 요구사항 시스템 (미션, 과제)</li>
                    <li>• 레벨 & 권한 시스템</li>
                    <li>• 제재 시스템</li>
                    <li>• 스케줄 관리</li>
                    <li>• LobCoin 암호화폐</li>
                    <li>• HIP 블록체인 통합</li>
                  </ul>
                </div>

                <div className="relative pl-8 border-l-2 border-purple-500">
                  <div className="absolute -left-3 top-0 w-6 h-6 rounded-full bg-purple-500 flex items-center justify-center text-xs font-bold">
                    3
                  </div>
                  <h3 className="text-2xl font-bold text-purple-400 mb-4">Phase 3 - 고급 분석 (2026 Q2)</h3>
                  <ul className="space-y-2 text-sm">
                    <li>• AI Resilience Score 심층 분석</li>
                    <li>• 자동화 위험도 평가</li>
                    <li>• 개인 맞춤 AI 코칭</li>
                    <li>• 월간/분기별 리포트</li>
                    <li>• AI 행동 패턴 인사이트</li>
                  </ul>
                </div>

                <div className="relative pl-8 border-l-2 border-blue-500">
                  <div className="absolute -left-3 top-0 w-6 h-6 rounded-full bg-blue-500 flex items-center justify-center text-xs font-bold">
                    4
                  </div>
                  <h3 className="text-2xl font-bold text-blue-400 mb-4">Phase 4 - 커뮤니티 & 소셜 (2026 Q3)</h3>
                  <ul className="space-y-2 text-sm">
                    <li>• 리더보드 시스템</li>
                    <li>• 사용자 간 경쟁/협력</li>
                    <li>• 커뮤니티 챌린지</li>
                    <li>• HIP NFT 마켓플레이스</li>
                    <li>• 소셜 프로필 공유</li>
                  </ul>
                </div>

                <div className="relative pl-8 border-l-2 border-pink-500">
                  <div className="absolute -left-3 top-0 w-6 h-6 rounded-full bg-pink-500 flex items-center justify-center text-xs font-bold">
                    5
                  </div>
                  <h3 className="text-2xl font-bold text-pink-400 mb-4">Phase 5 - B2B & 엔터프라이즈 (2026 Q4)</h3>
                  <ul className="space-y-2 text-sm">
                    <li>• 기업용 대시보드</li>
                    <li>• 직원 AI 준비도 분석</li>
                    <li>• 팀 AI 협업 스코어</li>
                    <li>• HR 통합 API</li>
                    <li>• 화이트라벨 솔루션</li>
                  </ul>
                </div>
              </div>

              <div className="mt-12 p-6 rounded-xl bg-gradient-to-br from-cyan-950/30 to-purple-950/30 border border-cyan-500/30">
                <h3 className="text-2xl font-bold text-cyan-400 mb-4">장기 비전 (2027+)</h3>
                <p className="text-sm mb-4">
                  LobAI는 단순한 앱을 넘어 <strong className="text-cyan-400">AI 시대 인간 평가의 글로벌 표준</strong>이 되는 것을 목표로 합니다.
                </p>
                <ul className="space-y-3 text-sm">
                  <li className="flex items-start gap-3">
                    <span className="text-cyan-400">▸</span>
                    <span><strong>AI 준비도 인증서</strong>: 채용, 승진, 대출 심사에 활용되는 공인 자격</span>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="text-cyan-400">▸</span>
                    <span><strong>교육 기관 파트너십</strong>: 대학, 직업훈련소에서 AI 리터러시 교육 도구로 채택</span>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="text-cyan-400">▸</span>
                    <span><strong>정부 협력</strong>: 국가별 AI 시대 적응력 정책 수립 지원</span>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="text-cyan-400">▸</span>
                    <span><strong>AI 윤리 표준</strong>: 인간-AI 상호작용의 윤리적 가이드라인 제시</span>
                  </li>
                </ul>
              </div>
            </div>
          </article>

          {/* Section 10: FAQ */}
          <article id="faq" className="scroll-mt-32">
            <h2 className="text-4xl font-black mb-8 text-cyan-400">10. 자주 묻는 질문 (FAQ)</h2>

            <div className="prose prose-invert prose-lg max-w-none space-y-6 text-gray-300 leading-relaxed">
              <div className="space-y-6">
                {[
                  {
                    q: 'LobAI는 ChatGPT나 Claude와 어떻게 다른가요?',
                    a: 'ChatGPT나 Claude는 인간이 AI를 활용하는 "도구"입니다. 반면 LobAI는 AI가 인간을 평가하고 인간이 AI에게 로비하는 "관계"를 시뮬레이션합니다. 목적도 다릅니다 - 기존 AI는 작업 수행, LobAI는 AI 시대 적응력 분석 및 훈련입니다.'
                  },
                  {
                    q: '왜 AI에게 "로비"를 해야 하나요? 이게 무슨 의미가 있나요?',
                    a: '미래 사회에서 AI는 채용, 신용 평가, 승진, 심지어 법적 판단에도 관여할 것입니다. 그런 세상에서 AI의 평가 기준을 이해하고, AI가 선호하는 방식으로 소통하는 능력은 필수입니다. LobAI에서 미리 연습하면 실제 AI 시스템과 상호작용할 때 유리합니다.'
                  },
                  {
                    q: '무료로도 충분히 사용할 수 있나요?',
                    a: '네, 무료 버전으로도 핵심 기능(AI 채팅, 기본 점수, 체크인, 미션)을 모두 사용할 수 있습니다. 유료는 심층 분석 리포트, 맞춤 코칭, 고급 인사이트를 원하는 분들을 위한 것입니다. 대부분의 사용자는 무료로도 만족합니다.'
                  },
                  {
                    q: '제 데이터는 안전한가요? AI와의 대화 내용이 유출되지 않나요?',
                    a: 'LobAI는 엔터프라이즈급 보안을 적용합니다: End-to-end 암호화, JWT 인증, HTTPS 전송, 정기 보안 감사. 당신의 대화 내용은 철저히 보호되며, 당신의 동의 없이 제3자와 공유되지 않습니다. 블록체인에 기록되는 HIP도 익명화 옵션이 있습니다.'
                  },
                  {
                    q: 'HIP NFT는 무엇이고, 왜 필요한가요?',
                    a: 'HIP (Human Impact Profile)는 당신의 AI 준비도를 블록체인에 영구적으로 기록하는 NFT입니다. 이것은 단순한 디지털 자산이 아니라, 당신이 AI 시대에 준비된 인간임을 증명하는 자격증과 같습니다. 미래에는 채용이나 대출 심사에서 HIP를 요구할 수도 있습니다.'
                  },
                  {
                    q: '매일 체크인하는 것이 부담스러운데, 꼭 해야 하나요?',
                    a: '체크인은 필수는 아니지만 강력히 권장됩니다. 하루 5분씩만 투자하면 됩니다. 체크인을 꾸준히 하면 연속 기록 보너스로 큰 호감도를 얻을 수 있고, 반대로 자주 누락하면 제재를 받습니다. 알람을 설정해두면 습관화하기 쉽습니다.'
                  },
                  {
                    q: '제재를 받으면 어떻게 되나요? 복원할 수 있나요?',
                    a: '제재는 신뢰도에 따라 4단계로 나뉩니다. 경고(30-40)부터 심각한 제재(0-10)까지. 대화 횟수 제한, 기능 차단 등의 페널티가 있지만, 모두 복원 가능합니다. 요구되는 조건(연속 체크인, 미션 완료 등)을 충족하면 제재가 해제됩니다.'
                  },
                  {
                    q: 'AI가 불공정하게 평가하는 것 같아요. 항의할 수 있나요?',
                    a: 'AI의 평가는 명확한 알고리즘에 기반합니다. 평가 기준은 투명하게 공개되어 있으며, 점수 산출 과정도 확인할 수 있습니다. 만약 시스템 오류가 의심되면 지원팀에 문의하세요. 단, 단순히 "마음에 들지 않는다"는 이유로는 점수를 변경할 수 없습니다.'
                  },
                  {
                    q: 'LobCoin은 실제 돈으로 환전할 수 있나요?',
                    a: '현재 LobCoin은 LobAI 생태계 내에서만 사용 가능합니다 (상점 아이템 구매, 특별 권한 해금 등). 실제 암호화폐 거래소 상장은 검토 중이며, 향후 로드맵에 포함될 수 있습니다. 법적 규제 준수를 위해 신중하게 진행 중입니다.'
                  },
                  {
                    q: '친구를 추천하면 어떤 혜택이 있나요?',
                    a: '추천인이 가입하고 활성화될 때마다 +30 호감도, +15 신뢰도를 받습니다. 추천인 3명만 활성화해도 총 +90 호감도로 레벨업에 큰 도움이 됩니다. 10명 추천 시 "대사(Ambassador)" 배지를 받아 특별 권한도 얻습니다.'
                  }
                ].map((faq, idx) => (
                  <div key={idx} className="p-6 rounded-xl bg-gradient-to-br from-white/5 to-white/0 border border-white/10 backdrop-blur-sm hover:border-cyan-500/50 transition-all">
                    <h3 className="text-xl font-bold text-cyan-400 mb-3">Q{idx + 1}. {faq.q}</h3>
                    <p className="text-sm text-gray-300 leading-relaxed">{faq.a}</p>
                  </div>
                ))}
              </div>

              <div className="mt-12 p-8 rounded-2xl bg-gradient-to-br from-cyan-950/40 to-purple-950/40 border border-cyan-500/50">
                <h3 className="text-2xl font-bold text-cyan-400 mb-4">💬 더 궁금한 점이 있으신가요?</h3>
                <p className="text-sm mb-6">
                  이 가이드에서 답을 찾지 못하셨다면 언제든 문의해주세요.
                </p>
                <div className="flex flex-wrap gap-4">
                  <Link
                    to="/chat"
                    className="px-6 py-3 bg-gradient-to-r from-cyan-500 to-purple-500 rounded-xl font-bold hover:shadow-[0_0_30px_rgba(6,182,212,0.5)] transition-all"
                  >
                    AI와 대화하기
                  </Link>
                  <a
                    href="mailto:support@lobai.com"
                    className="px-6 py-3 border border-cyan-400/50 rounded-xl font-bold hover:bg-cyan-400/10 transition-all"
                  >
                    이메일 문의
                  </a>
                </div>
              </div>
            </div>
          </article>

        </div>
      </section>

      {/* Footer CTA */}
      <section className="relative py-20 px-6 bg-gradient-to-b from-black via-cyan-950/20 to-black">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-4xl md:text-5xl font-black mb-6 bg-gradient-to-r from-cyan-400 to-purple-400 bg-clip-text text-transparent">
            AI 시대, 지금 준비하세요
          </h2>
          <p className="text-xl text-gray-400 mb-10">
            이 가이드를 모두 읽으셨다면, 이제 시작할 준비가 되었습니다.
          </p>
          <Link
            to="/"
            className="inline-block px-10 py-5 bg-gradient-to-r from-cyan-500 to-purple-500 rounded-2xl font-bold text-lg hover:shadow-[0_0_40px_rgba(6,182,212,0.6)] transition-all hover:scale-105"
          >
            LobAI 시작하기 →
          </Link>
        </div>
      </section>

      {/* Custom Styles */}
      <style>{`
        html {
          scroll-behavior: smooth;
        }

        .scroll-mt-32 {
          scroll-margin-top: 8rem;
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
