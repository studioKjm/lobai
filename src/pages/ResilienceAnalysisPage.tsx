import React from 'react';
import { ResilienceReportCard } from '@/components/resilience/ResilienceReportCard';
import { Navbar } from '@/components/common/Navbar';

export const ResilienceAnalysisPage: React.FC = () => {
  return (
    <div className="relative w-full">
      <Navbar />
      <section className="relative min-h-screen w-full px-6 sm:px-12 pb-12 pt-36">
        <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-purple-900/10 rounded-full blur-[120px]" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-violet-900/10 rounded-full blur-[120px]" />

        <div className="relative w-full max-w-6xl mx-auto">
          <h1 className="text-4xl font-bold mb-8 text-white">AI 적응력 분석</h1>
          <ResilienceReportCard />
        </div>
      </section>
    </div>
  );
};
