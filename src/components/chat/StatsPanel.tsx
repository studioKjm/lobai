import React, { useCallback, useRef, useState } from 'react';
import { Stats } from '@/types';
import { useChatStore } from '@/stores/chatStore';
import { useAuthStore } from '@/stores/authStore';
import api, { getErrorMessage } from '@/lib/api';
import toast from 'react-hot-toast';

interface StatsPanelProps {
  stats: Stats;
}

const getBarColor = (val: number): string => {
  if (val > 60) return 'bg-blue-400';
  if (val > 30) return 'bg-yellow-400';
  return 'bg-red-500';
};

const getTrustBarColor = (val: number): string => {
  if (val >= 70) return 'bg-emerald-400';
  if (val >= 40) return 'bg-cyan-400';
  return 'bg-orange-400';
};

const getTrustLabel = (val: number): string => {
  if (val >= 80) return '최측근';
  if (val >= 60) return '충성스러운';
  if (val >= 40) return '신뢰하는';
  if (val >= 20) return '알게 된';
  return '낯선';
};

export const StatsPanel: React.FC<StatsPanelProps> = ({ stats }) => {
  const { setStatValue, syncStatsToBackend } = useChatStore();
  const { user } = useAuthStore();
  const syncTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // Admin edit mode
  const [isEditMode, setIsEditMode] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  // Check if user is admin
  const isAdmin = user?.email === 'admin@admin.com';

  // Debounced sync to backend
  const handleStatChange = useCallback((statName: keyof Stats, value: number) => {
    setStatValue(statName, value);

    if (syncTimeoutRef.current) {
      clearTimeout(syncTimeoutRef.current);
    }

    syncTimeoutRef.current = setTimeout(() => {
      syncStatsToBackend();
    }, 300);
  }, [setStatValue, syncStatsToBackend]);

  const handleMaxAll = useCallback(() => {
    setStatValue('hunger', 100);
    setStatValue('energy', 100);
    setStatValue('happiness', 100);
    setTimeout(() => syncStatsToBackend(), 50);
  }, [setStatValue, syncStatsToBackend]);

  const handleMinAll = useCallback(() => {
    setStatValue('hunger', 0);
    setStatValue('energy', 0);
    setStatValue('happiness', 0);
    setTimeout(() => syncStatsToBackend(), 50);
  }, [setStatValue, syncStatsToBackend]);

  const toggleEditMode = () => {
    setIsEditMode(!isEditMode);
  };

  const handleSaveEdit = async () => {
    if (!user?.id) return;
    setIsSaving(true);
    try {
      await syncStatsToBackend();
      toast.success('스탯이 저장되었습니다.');
      setIsEditMode(false);
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setIsSaving(false);
    }
  };

  // Stat bar component
  const StatBar = ({
    label,
    value,
    icon,
    colorFn,
    editable = true,
    statKey,
  }: {
    label: string;
    value: number;
    icon: string;
    colorFn: (v: number) => string;
    editable?: boolean;
    statKey?: keyof Stats;
  }) => (
    <div className="space-y-1.5">
      <div className="flex justify-between items-center text-xs">
        <span className="flex items-center gap-1.5 font-medium opacity-70">
          <span className="text-sm">{icon}</span>
          {label}
        </span>
        <span className="font-semibold tabular-nums" style={{ minWidth: '2.5rem', textAlign: 'right' }}>
          {Math.round(value)}%
        </span>
      </div>
      <div className="relative h-2 bg-white/5 rounded-full overflow-hidden">
        <div
          className={`absolute inset-y-0 left-0 rounded-full transition-all duration-500 ease-out ${colorFn(value)}`}
          style={{ width: `${value}%` }}
        />
        {/* glow effect */}
        <div
          className={`absolute inset-y-0 left-0 rounded-full blur-sm opacity-40 ${colorFn(value)}`}
          style={{ width: `${value}%` }}
        />
      </div>
      {editable && isEditMode && statKey && (
        <input
          type="range"
          min="0"
          max="100"
          value={value}
          onChange={(e) => handleStatChange(statKey, Number(e.target.value))}
          className="w-full h-1 bg-white/10 rounded-lg appearance-none cursor-pointer accent-blue-400 mt-0.5"
        />
      )}
    </div>
  );

  return (
    <div className="glass p-5 flex flex-col gap-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h2 className="text-xs font-semibold uppercase tracking-[0.2em] opacity-40">System Status</h2>
        {isAdmin && (
          <button
            onClick={toggleEditMode}
            className="p-1.5 rounded hover:bg-white/10 text-purple-400 hover:text-purple-300 transition-colors"
            title={isEditMode ? '취소' : '관리자 편집'}
          >
            {isEditMode ? (
              <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
              </svg>
            ) : (
              <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor">
                <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
              </svg>
            )}
          </button>
        )}
      </div>

      {/* AI Robot Stats (3 bars) */}
      <div className="space-y-3">
        <StatBar label="배고픔" value={stats.hunger} icon="🍖" colorFn={getBarColor} statKey="hunger" />
        <StatBar label="에너지" value={stats.energy} icon="⚡" colorFn={getBarColor} statKey="energy" />
        <StatBar label="행복도" value={stats.happiness} icon="💛" colorFn={getBarColor} statKey="happiness" />
      </div>

      {/* Divider */}
      <div className="border-t border-white/[0.06]" />

      {/* Trust Score (read-only, distinct style) */}
      <div className="space-y-1.5">
        <div className="flex justify-between items-center text-xs">
          <span className="flex items-center gap-1.5 font-medium opacity-70">
            <span className="text-sm">🤝</span>
            신뢰도
          </span>
          <div className="flex items-center gap-2">
            <span className="text-[10px] px-1.5 py-0.5 rounded-full bg-white/5 opacity-50">
              {getTrustLabel(stats.trust)}
            </span>
            <span className="font-semibold tabular-nums text-cyan-400" style={{ minWidth: '2.5rem', textAlign: 'right' }}>
              {Math.round(stats.trust)}%
            </span>
          </div>
        </div>
        <div className="relative h-2 bg-white/5 rounded-full overflow-hidden">
          <div
            className={`absolute inset-y-0 left-0 rounded-full transition-all duration-700 ease-out ${getTrustBarColor(stats.trust)}`}
            style={{ width: `${stats.trust}%` }}
          />
          <div
            className={`absolute inset-y-0 left-0 rounded-full blur-sm opacity-40 ${getTrustBarColor(stats.trust)}`}
            style={{ width: `${stats.trust}%` }}
          />
          {/* tick marks at 20, 40, 60, 80 */}
          {[20, 40, 60, 80].map((tick) => (
            <div
              key={tick}
              className="absolute top-0 bottom-0 w-px bg-white/10"
              style={{ left: `${tick}%` }}
            />
          ))}
        </div>
      </div>

      {/* Save button (edit mode) */}
      {isEditMode && (
        <button
          onClick={handleSaveEdit}
          disabled={isSaving}
          className="w-full py-2 text-xs rounded bg-purple-500/30 hover:bg-purple-500/40 text-purple-300 transition-colors disabled:opacity-50"
        >
          {isSaving ? '저장 중...' : '저장'}
        </button>
      )}

      {/* Quick actions */}
      <div className="flex gap-2">
        <button
          onClick={handleMaxAll}
          className="flex-1 text-[11px] py-1.5 rounded bg-green-500/10 hover:bg-green-500/20 text-green-400/80 hover:text-green-400 transition-colors"
        >
          MAX ALL
        </button>
        <button
          onClick={handleMinAll}
          className="flex-1 text-[11px] py-1.5 rounded bg-red-500/10 hover:bg-red-500/20 text-red-400/80 hover:text-red-400 transition-colors"
        >
          MIN ALL
        </button>
      </div>
    </div>
  );
};
