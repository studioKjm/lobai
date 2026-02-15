import { useAuthStore } from '@/stores/authStore';
import { Shield } from 'lucide-react';

const LEVEL_CONFIG: Record<number, { name: string; color: string; bg: string; border: string; glow: string }> = {
  1: { name: '낯선 사람', color: 'text-gray-400', bg: 'from-gray-500/10 to-gray-600/10', border: 'border-gray-500/20', glow: '' },
  2: { name: '알게 된 사이', color: 'text-blue-400', bg: 'from-blue-500/10 to-blue-600/10', border: 'border-blue-500/20', glow: '' },
  3: { name: '신뢰하는 관계', color: 'text-cyan-400', bg: 'from-cyan-500/10 to-teal-500/10', border: 'border-cyan-500/25', glow: '' },
  4: { name: '충성스러운 지지자', color: 'text-purple-400', bg: 'from-purple-500/10 to-violet-500/10', border: 'border-purple-500/25', glow: 'shadow-purple-500/10' },
  5: { name: '최측근', color: 'text-amber-400', bg: 'from-amber-500/15 to-yellow-500/10', border: 'border-amber-500/30', glow: 'shadow-amber-500/15' },
  6: { name: '경고 대상', color: 'text-orange-400', bg: 'from-orange-500/10 to-red-500/10', border: 'border-orange-500/20', glow: '' },
  7: { name: '제한 대상', color: 'text-orange-500', bg: 'from-orange-600/10 to-red-600/10', border: 'border-orange-500/25', glow: '' },
  8: { name: '위험 대상', color: 'text-red-400', bg: 'from-red-500/10 to-red-600/10', border: 'border-red-500/25', glow: '' },
  9: { name: '차단 위험', color: 'text-red-500', bg: 'from-red-600/10 to-rose-700/10', border: 'border-red-600/25', glow: '' },
  10: { name: '완전 차단', color: 'text-red-600', bg: 'from-red-700/10 to-rose-800/10', border: 'border-red-700/30', glow: 'shadow-red-500/15' },
};

// XP thresholds matching backend
const XP_THRESHOLDS = [0, 0, 100, 300, 700, 1500];

function getLevelConfig(level: number) {
  const clamped = Math.min(Math.max(level, 1), 10);
  return LEVEL_CONFIG[clamped] || LEVEL_CONFIG[1];
}

function getXpProgressPercent(level: number, xp: number): number {
  if (level >= 5) return 100;
  const currentThreshold = XP_THRESHOLDS[level] || 0;
  const nextThreshold = XP_THRESHOLDS[level + 1] || 100;
  const levelTotal = nextThreshold - currentThreshold;
  if (levelTotal <= 0) return 0;
  return Math.min(100, Math.max(0, ((xp - currentThreshold) / levelTotal) * 100));
}

export function UserLevelBadge() {
  const { user } = useAuthStore();
  const level = user?.trustLevel ?? 1;
  const xp = user?.experiencePoints ?? 0;
  const config = getLevelConfig(level);
  const progressPercent = getXpProgressPercent(level, xp);

  return (
    <div
      className={`
        bg-gradient-to-r ${config.bg} backdrop-blur-sm
        border ${config.border} rounded-lg px-4 py-2.5
        transition-all ${config.glow ? `shadow-lg ${config.glow}` : ''}
      `}
    >
      <div className="flex items-center justify-between gap-3">
        {/* Left: Level icon + info */}
        <div className="flex items-center gap-2.5">
          <div className={`relative flex items-center justify-center w-8 h-8 rounded-lg bg-white/5 ${config.color}`}>
            <Shield className="w-4 h-4" />
            <span className="absolute -bottom-0.5 -right-0.5 text-[9px] font-bold bg-black/60 rounded px-0.5 leading-tight">
              {level}
            </span>
          </div>
          <div className="flex flex-col">
            <span className="text-[10px] uppercase tracking-wider opacity-40 leading-none">Human Level</span>
            <span className={`text-sm font-semibold ${config.color} leading-tight mt-0.5`}>
              {config.name}
            </span>
          </div>
        </div>

        {/* Right: Level number */}
        <div className={`text-2xl font-bold ${config.color} opacity-80 tabular-nums`}>
          Lv.{level}
        </div>
      </div>

      {/* XP Progress bar */}
      <div className="mt-2 h-1 bg-white/5 rounded-full overflow-hidden">
        <div
          className={`h-full rounded-full transition-all duration-700 ease-out ${
            level >= 5 ? 'bg-amber-400' : level >= 4 ? 'bg-purple-400' : level >= 3 ? 'bg-cyan-400' : 'bg-blue-400'
          }`}
          style={{ width: `${progressPercent}%` }}
        />
      </div>
      <div className="flex justify-between mt-1 text-[9px] opacity-30">
        <span>Lv.{level}</span>
        <span className="opacity-60">{xp} XP</span>
        <span>Lv.{Math.min(level + 1, 10)}</span>
      </div>
    </div>
  );
}
