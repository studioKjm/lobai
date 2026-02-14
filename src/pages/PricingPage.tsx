import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Navbar } from '@/components/common/Navbar';
import { AuthModal } from '@/components/auth';
import { useAuthStore } from '@/stores/authStore';

export const PricingPage: React.FC = () => {
  const [billingCycle, setBillingCycle] = useState<'monthly' | 'annual'>('monthly');
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const { user } = useAuthStore();
  const navigate = useNavigate();

  const handleSubscribe = (plan: string) => {
    if (!user) {
      setIsAuthModalOpen(true);
      return;
    }
    // TODO: Implement subscription logic
    console.log(`Subscribing to ${plan} plan`);
  };

  const plans = [
    {
      type: 'Free',
      monthlyPrice: 0,
      annualPrice: 0,
      color: 'gray',
      popular: false,
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
      monthlyPrice: 12,
      annualPrice: 115,
      color: 'cyan',
      popular: true,
      features: [
        'AI 채팅 무제한',
        'Affinity + Resilience 점수',
        '주간 성과 리포트',
        '일일 미션 3개',
        '고급 인사이트',
        'LobCoin 2배 적립',
        '히스토리 분석'
      ]
    },
    {
      type: 'Premium',
      monthlyPrice: 24,
      annualPrice: 230,
      color: 'purple',
      popular: false,
      features: [
        'Standard 모든 기능',
        'HIP 블록체인 NFT 포함',
        'AI 시대 생존 리포트',
        '개인 맞춤 코칭',
        'VIP 전용 미션',
        '우선 지원',
        'LobCoin 3배 적립',
        '월간 심층 분석'
      ]
    }
  ];

  const blockchainPlan = {
    type: 'Blockchain NFT',
    price: 15,
    color: 'blue',
    features: [
      'HIP NFT 민팅 (일회성)',
      'Polygon 네트워크 등록',
      'IPFS 분산 저장',
      '영구 소유권 증명',
      '블록체인 지갑 연동',
      '외부 마켓플레이스 거래 가능'
    ]
  };

  const faqs = [
    {
      question: '무료 플랜에서 프리미엄으로 언제든 업그레이드할 수 있나요?',
      answer: '네, 언제든지 업그레이드 가능합니다. 업그레이드 시 즉시 모든 프리미엄 기능에 접근할 수 있으며, 기존 데이터와 진행 상황은 모두 보존됩니다.'
    },
    {
      question: '연간 구독의 할인은 어떻게 적용되나요?',
      answer: '연간 구독 시 월간 요금 대비 약 20% 할인이 적용됩니다. Standard는 연간 $115 (월 $9.58), Premium은 연간 $230 (월 $19.17)으로 2개월 이상 절약할 수 있습니다.'
    },
    {
      question: 'Blockchain NFT는 무엇이며 왜 필요한가요?',
      answer: 'HIP(Human Impact Profile) NFT는 당신의 AI 준비도를 블록체인에 영구적으로 기록합니다. 이는 이력서나 포트폴리오처럼 AI 시대 적응력을 증명하는 디지털 자격증명이 됩니다. Premium 플랜에는 포함되어 있으며, 별도 구매도 가능합니다.'
    },
    {
      question: '환불 정책은 어떻게 되나요?',
      answer: '구독 후 14일 이내 전액 환불이 가능합니다. 단, 블록체인 NFT는 민팅 후 블록체인에 영구 기록되므로 환불이 불가능합니다.'
    },
    {
      question: 'LobCoin은 실제 돈으로 전환할 수 있나요?',
      answer: '현재 LobCoin은 플랫폼 내에서만 사용 가능한 포인트 시스템입니다. 향후 암호화폐 거래소 상장 및 실제 가치 교환을 준비 중이며, 로드맵에 따라 단계적으로 출시될 예정입니다.'
    }
  ];

  return (
    <div className="relative w-full min-h-screen bg-black text-white overflow-hidden">
      <Navbar />

      {/* Animated Background */}
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

      <div className="relative z-10 pt-32 pb-20 px-6">
        {/* Header */}
        <div className="max-w-5xl mx-auto text-center mb-20">
          <h1 className="text-6xl lg:text-7xl font-black mb-6 bg-gradient-to-r from-cyan-400 via-purple-400 to-pink-400 bg-clip-text text-transparent">
            Pricing
          </h1>
          <div className="h-1 w-32 bg-gradient-to-r from-cyan-400 to-purple-600 rounded-full mx-auto mb-6" />
          <p className="text-xl text-gray-300 mb-8">
            AI 시대를 준비하는 가장 스마트한 투자
          </p>

          {/* Billing Toggle */}
          <div className="inline-flex items-center gap-4 p-2 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-sm">
            <button
              onClick={() => setBillingCycle('monthly')}
              className={`px-6 py-3 rounded-xl font-semibold transition-all duration-300 ${
                billingCycle === 'monthly'
                  ? 'bg-gradient-to-r from-cyan-500 to-purple-500 text-white'
                  : 'text-gray-400 hover:text-white'
              }`}
            >
              월간 결제
            </button>
            <button
              onClick={() => setBillingCycle('annual')}
              className={`px-6 py-3 rounded-xl font-semibold transition-all duration-300 relative ${
                billingCycle === 'annual'
                  ? 'bg-gradient-to-r from-cyan-500 to-purple-500 text-white'
                  : 'text-gray-400 hover:text-white'
              }`}
            >
              연간 결제
              <span className="absolute -top-2 -right-2 px-2 py-1 bg-green-500 text-xs rounded-full">
                20% OFF
              </span>
            </button>
          </div>
        </div>

        {/* Pricing Cards */}
        <div className="max-w-7xl mx-auto">
          <div className="grid lg:grid-cols-3 gap-8 mb-16">
            {plans.map((plan, idx) => {
              const price = billingCycle === 'monthly' ? plan.monthlyPrice : plan.annualPrice;
              const displayPrice = billingCycle === 'annual' ? (price / 12).toFixed(2) : price;

              return (
                <div
                  key={idx}
                  className={`relative p-8 rounded-3xl ${
                    plan.popular
                      ? 'bg-gradient-to-br from-cyan-600/20 to-blue-600/20 border-2 border-cyan-500 transform scale-105'
                      : `bg-gradient-to-br from-${plan.color}-950/20 to-black/40 border border-${plan.color}-500/30`
                  } backdrop-blur-sm hover:scale-110 transition-all duration-500`}
                >
                  {plan.popular && (
                    <div className="absolute -top-4 left-1/2 -translate-x-1/2 px-6 py-2 bg-gradient-to-r from-cyan-500 to-blue-500 rounded-full text-sm font-bold">
                      가장 인기
                    </div>
                  )}

                  <div className="text-center mb-8">
                    <h3 className={`text-3xl font-bold mb-4 text-${plan.color}-400`}>{plan.type}</h3>
                    <div className="mb-2">
                      <span className={`text-6xl font-black bg-gradient-to-r from-${plan.color}-400 to-${plan.color}-600 bg-clip-text text-transparent`}>
                        ${displayPrice}
                      </span>
                      <span className="text-xl text-gray-400">/월</span>
                    </div>
                    {billingCycle === 'annual' && plan.monthlyPrice > 0 && (
                      <div className="text-sm text-gray-400">
                        연간 ${price} 청구
                        <span className="text-green-400 ml-2">({Math.round(((plan.monthlyPrice * 12 - price) / (plan.monthlyPrice * 12)) * 100)}% 할인)</span>
                      </div>
                    )}
                  </div>

                  <ul className="space-y-4 mb-8">
                    {plan.features.map((feature, i) => (
                      <li key={i} className="flex items-start gap-3">
                        <span className={`text-${plan.color}-400 mt-1 flex-shrink-0`}>✓</span>
                        <span className="text-gray-300">{feature}</span>
                      </li>
                    ))}
                  </ul>

                  <button
                    onClick={() => handleSubscribe(plan.type)}
                    className={`w-full py-4 rounded-xl font-bold text-lg transition-all duration-300 ${
                      plan.popular
                        ? 'bg-gradient-to-r from-cyan-500 to-blue-500 hover:shadow-[0_0_30px_rgba(6,182,212,0.5)] hover:scale-105'
                        : plan.monthlyPrice === 0
                        ? `border-2 border-${plan.color}-500/50 hover:bg-${plan.color}-500/10`
                        : `bg-gradient-to-r from-${plan.color}-600 to-${plan.color}-700 hover:shadow-[0_0_20px_rgba(139,92,246,0.3)] hover:scale-105`
                    }`}
                  >
                    {plan.monthlyPrice === 0 ? '무료로 시작하기' : '구독하기'}
                  </button>
                </div>
              );
            })}
          </div>

          {/* Blockchain NFT Card */}
          <div className="max-w-2xl mx-auto mb-20">
            <div className="relative p-8 rounded-3xl bg-gradient-to-br from-blue-950/40 to-cyan-950/40 border-2 border-blue-500/50 backdrop-blur-sm">
              <div className="flex items-center gap-6 mb-6">
                <div className="text-6xl">⛓️</div>
                <div>
                  <h3 className="text-3xl font-bold text-blue-400 mb-2">{blockchainPlan.type}</h3>
                  <p className="text-gray-400">AI 준비도를 블록체인에 영구 기록</p>
                </div>
              </div>

              <div className="flex items-center justify-between mb-6">
                <div>
                  <span className="text-5xl font-black bg-gradient-to-r from-blue-400 to-cyan-400 bg-clip-text text-transparent">
                    ${blockchainPlan.price}
                  </span>
                  <span className="text-gray-400 ml-2">일회성 구매</span>
                </div>
                <div className="text-sm text-blue-400 font-semibold">
                  ⚡ 평생 소유
                </div>
              </div>

              <div className="grid md:grid-cols-2 gap-4 mb-8">
                {blockchainPlan.features.map((feature, i) => (
                  <div key={i} className="flex items-start gap-3">
                    <span className="text-blue-400 mt-1 flex-shrink-0">✓</span>
                    <span className="text-gray-300 text-sm">{feature}</span>
                  </div>
                ))}
              </div>

              <button
                onClick={() => handleSubscribe('Blockchain')}
                className="w-full py-4 rounded-xl font-bold text-lg bg-gradient-to-r from-blue-500 to-cyan-500
                         hover:shadow-[0_0_30px_rgba(59,130,246,0.5)] transition-all duration-300 hover:scale-105"
              >
                NFT 구매하기
              </button>

              <p className="text-xs text-gray-500 text-center mt-4">
                * Premium 플랜 구독 시 블록체인 NFT가 포함됩니다
              </p>
            </div>
          </div>

          {/* FAQ Section */}
          <div className="max-w-4xl mx-auto">
            <div className="text-center mb-12">
              <h2 className="text-4xl font-black mb-6 bg-gradient-to-r from-cyan-400 to-purple-400 bg-clip-text text-transparent">
                자주 묻는 질문
              </h2>
              <div className="h-1 w-24 bg-gradient-to-r from-cyan-400 to-purple-600 rounded-full mx-auto" />
            </div>

            <div className="space-y-6">
              {faqs.map((faq, idx) => (
                <details
                  key={idx}
                  className="group p-6 rounded-2xl bg-gradient-to-br from-white/5 to-white/0 border border-white/10
                           backdrop-blur-sm hover:border-cyan-500/50 transition-all duration-300"
                >
                  <summary className="flex items-center justify-between cursor-pointer list-none">
                    <span className="text-lg font-semibold text-white group-hover:text-cyan-400 transition-colors">
                      {faq.question}
                    </span>
                    <span className="text-2xl text-cyan-400 group-open:rotate-180 transition-transform duration-300">
                      ↓
                    </span>
                  </summary>
                  <p className="mt-4 text-gray-300 leading-relaxed">
                    {faq.answer}
                  </p>
                </details>
              ))}
            </div>
          </div>

          {/* CTA Section */}
          <div className="text-center mt-20">
            <div className="p-12 rounded-3xl bg-gradient-to-br from-purple-950/40 via-black/60 to-cyan-950/40 border border-purple-500/30 backdrop-blur-lg max-w-3xl mx-auto">
              <h3 className="text-4xl font-bold mb-6 bg-gradient-to-r from-cyan-400 to-purple-400 bg-clip-text text-transparent">
                아직 고민 중이신가요?
              </h3>
              <p className="text-xl text-gray-300 mb-8">
                무료 플랜으로 시작해서 LobAI를 체험해보세요.<br />
                언제든지 업그레이드할 수 있습니다.
              </p>
              <Link
                to="/chat"
                className="inline-block px-12 py-4 bg-gradient-to-r from-cyan-500 to-purple-500 rounded-2xl font-bold text-lg
                         hover:shadow-[0_0_40px_rgba(6,182,212,0.5)] transition-all duration-300 hover:scale-105"
              >
                무료로 시작하기 →
              </Link>
            </div>
          </div>
        </div>
      </div>

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

        details summary::-webkit-details-marker {
          display: none;
        }

        html {
          scroll-behavior: smooth;
        }
      `}</style>
    </div>
  );
};
