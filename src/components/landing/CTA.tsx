import React from 'react';
import { Link } from 'react-router-dom';

export const CTA: React.FC = () => {
  return (
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
            <Link to="/chat" className="btn-primary text-base px-8 py-4">
              무료로 시작하기
            </Link>
            <button className="btn-secondary text-base px-8 py-4">
              더 알아보기
            </button>
          </div>
        </div>
      </div>
    </section>
  );
};

