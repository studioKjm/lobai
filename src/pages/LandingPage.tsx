import React from 'react';
import { Navbar } from '@/components/common/Navbar';
import { Footer } from '@/components/common/Footer';
import { Features } from '@/components/landing/Features';
import { HowItWorks } from '@/components/landing/HowItWorks';
import { CTA } from '@/components/landing/CTA';
import { Link } from 'react-router-dom';

export const LandingPage: React.FC = () => {
  return (
    <div className="relative w-full">
      <Navbar />

      {/* 간소화된 Hero Section - 마케팅용 */}
      <section className="relative min-h-screen w-full flex flex-col items-center justify-center p-6 sm:p-12 pt-32">
        {/* Background Decor */}
        <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-blue-900/10 rounded-full blur-[120px]" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-purple-900/10 rounded-full blur-[120px]" />

        <div className="text-center max-w-4xl relative z-10">
          <h1 className="text-6xl sm:text-7xl font-display font-bold mb-6 gradient-text">
            AI 시대의 당신,<br />준비되셨나요?
          </h1>
          <p className="text-xl opacity-70 mb-10">
            LobAI와 함께 AI 적응력을 진단하고,<br />
            미래를 위한 경쟁력을 키우세요.
          </p>
          <Link to="/chat" className="btn-primary text-lg px-10 py-5 inline-block">
            시작하기
          </Link>
        </div>
      </section>

      <Features />
      <HowItWorks />
      <CTA />
      <Footer />
    </div>
  );
};
