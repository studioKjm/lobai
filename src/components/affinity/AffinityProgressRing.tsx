/**
 * AffinityProgressRing Component
 *
 * Circular progress indicator with celestial orbit aesthetic
 */

import { useEffect, useState } from 'react';

interface AffinityProgressRingProps {
  score: number;
  level: number;
  levelName: string;
}

export function AffinityProgressRing({ score, level, levelName }: AffinityProgressRingProps) {
  const [animated, setAnimated] = useState(false);
  const circumference = 2 * Math.PI * 58; // radius = 58
  const strokeDashoffset = circumference - (score / 100) * circumference;

  useEffect(() => {
    const timer = setTimeout(() => setAnimated(true), 100);
    return () => clearTimeout(timer);
  }, []);

  // Dynamic gradient based on level
  const gradientColors = {
    1: ['#64748b', '#475569'], // slate
    2: ['#3b82f6', '#06b6d4'], // blue to cyan
    3: ['#a855f7', '#ec4899'], // purple to pink
    4: ['#8b5cf6', '#d946ef'], // violet to fuchsia
    5: ['#f59e0b', '#f43f5e'], // amber to rose
  };

  const [colorStart, colorEnd] = gradientColors[level as keyof typeof gradientColors] || gradientColors[3];

  return (
    <div className="relative flex items-center justify-center">
      {/* Outer glow */}
      <div
        className="absolute inset-0 rounded-full opacity-50 blur-2xl transition-opacity duration-1000"
        style={{
          background: `radial-gradient(circle, ${colorStart}40 0%, transparent 70%)`,
          animation: 'pulse 3s ease-in-out infinite',
        }}
      />

      {/* SVG Ring */}
      <svg className="transform -rotate-90" width="140" height="140">
        <defs>
          {/* Gradient definition */}
          <linearGradient id="progressGradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor={colorStart} />
            <stop offset="100%" stopColor={colorEnd} />
          </linearGradient>

          {/* Glow filter */}
          <filter id="glow">
            <feGaussianBlur stdDeviation="3" result="coloredBlur" />
            <feMerge>
              <feMergeNode in="coloredBlur" />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>
        </defs>

        {/* Background circle */}
        <circle
          cx="70"
          cy="70"
          r="58"
          fill="none"
          stroke="rgba(255, 255, 255, 0.05)"
          strokeWidth="6"
        />

        {/* Progress circle */}
        <circle
          cx="70"
          cy="70"
          r="58"
          fill="none"
          stroke="url(#progressGradient)"
          strokeWidth="6"
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={animated ? strokeDashoffset : circumference}
          filter="url(#glow)"
          className="transition-all duration-1500 ease-out"
        />

        {/* Orbiting particle */}
        {animated && (
          <circle
            cx="70"
            cy="12"
            r="3"
            fill={colorEnd}
            filter="url(#glow)"
            className="origin-center"
            style={{
              animation: 'orbit 4s linear infinite',
              transformOrigin: '70px 70px',
            }}
          />
        )}
      </svg>

      {/* Center content */}
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        {/* Score */}
        <div className="text-5xl font-bold bg-gradient-to-br from-white to-white/80 bg-clip-text text-transparent tabular-nums">
          {Math.round(score)}
        </div>

        {/* Level badge */}
        <div
          className="mt-1 px-3 py-1 rounded-full text-xs font-semibold backdrop-blur-sm"
          style={{
            background: `linear-gradient(135deg, ${colorStart}30, ${colorEnd}30)`,
            border: `1px solid ${colorStart}60`,
            color: colorEnd,
          }}
        >
          Lv.{level} {levelName}
        </div>
      </div>

      <style jsx>{`
        @keyframes orbit {
          from {
            transform: rotate(0deg) translateX(58px) rotate(0deg);
          }
          to {
            transform: rotate(360deg) translateX(58px) rotate(-360deg);
          }
        }

        @keyframes pulse {
          0%,
          100% {
            opacity: 0.5;
          }
          50% {
            opacity: 0.8;
          }
        }
      `}</style>
    </div>
  );
}
