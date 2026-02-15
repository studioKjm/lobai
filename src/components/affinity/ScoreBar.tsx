/**
 * ScoreBar Component
 *
 * Individual score bar with cosmic energy stream aesthetic
 */

import { useEffect, useState } from 'react';
import type { ScoreBarProps } from '@/types/affinity';

export function ScoreBar({ label, value, maxValue = 1, color, delay = 0 }: ScoreBarProps) {
  const [animated, setAnimated] = useState(false);
  const safeValue = value ?? 0; // null/undefined 방어
  const percentage = (safeValue / maxValue) * 100;

  useEffect(() => {
    const timer = setTimeout(() => setAnimated(true), delay);
    return () => clearTimeout(timer);
  }, [delay]);

  return (
    <div className="group relative">
      {/* Label */}
      <div className="flex items-center justify-between mb-2">
        <span className="text-sm font-medium text-slate-300 group-hover:text-white transition-colors">
          {label}
        </span>
        <span className="text-sm font-bold text-white tabular-nums">
          {safeValue.toFixed(2)}
        </span>
      </div>

      {/* Track */}
      <div className="relative h-2 bg-white/5 rounded-full overflow-hidden backdrop-blur-sm">
        {/* Background glow */}
        <div
          className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-500"
          style={{
            background: `radial-gradient(ellipse at left, ${color}40 0%, transparent 60%)`,
          }}
        />

        {/* Animated fill */}
        <div
          className="absolute inset-y-0 left-0 rounded-full transition-all duration-1000 ease-out"
          style={{
            width: animated ? `${percentage}%` : '0%',
            background: `linear-gradient(90deg, ${color}, ${color}dd)`,
            boxShadow: `0 0 20px ${color}60, inset 0 1px 0 ${color}40`,
          }}
        >
          {/* Shimmer effect */}
          <div
            className="absolute inset-0 opacity-50"
            style={{
              background:
                'linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.3) 50%, transparent 100%)',
              animation: 'shimmer 2s infinite',
            }}
          />
        </div>

        {/* Particles */}
        {animated && (
          <>
            <div
              className="absolute top-1/2 -translate-y-1/2 w-1 h-1 rounded-full animate-pulse"
              style={{
                left: `${Math.min(percentage, 95)}%`,
                background: color,
                boxShadow: `0 0 8px ${color}`,
                animation: 'pulse 2s ease-in-out infinite',
              }}
            />
          </>
        )}
      </div>

      <style jsx>{`
        @keyframes shimmer {
          0% {
            transform: translateX(-100%);
          }
          100% {
            transform: translateX(200%);
          }
        }
      `}</style>
    </div>
  );
}
