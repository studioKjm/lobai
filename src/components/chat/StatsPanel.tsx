import React, { useCallback, useRef } from 'react';
import { Stats } from '@/types';
import { useChatStore } from '@/stores/chatStore';

interface StatsPanelProps {
  stats: Stats;
}

const getBarColor = (val: number): string => {
  if (val > 60) return 'bg-blue-400';
  if (val > 30) return 'bg-yellow-400';
  return 'bg-red-500';
};

export const StatsPanel: React.FC<StatsPanelProps> = ({ stats }) => {
  const { setStatValue, syncStatsToBackend } = useChatStore();
  const syncTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // Debounced sync to backend
  const handleStatChange = useCallback((statName: keyof Stats, value: number) => {
    setStatValue(statName, value);

    // Clear previous timeout
    if (syncTimeoutRef.current) {
      clearTimeout(syncTimeoutRef.current);
    }

    // Sync to backend after 300ms of no changes
    syncTimeoutRef.current = setTimeout(() => {
      syncStatsToBackend();
    }, 300);
  }, [setStatValue, syncStatsToBackend]);

  const handleMaxAll = useCallback(() => {
    setStatValue('hunger', 100);
    setStatValue('energy', 100);
    setStatValue('happiness', 100);

    // Immediate sync for button clicks
    setTimeout(() => syncStatsToBackend(), 50);
  }, [setStatValue, syncStatsToBackend]);

  const handleMinAll = useCallback(() => {
    setStatValue('hunger', 0);
    setStatValue('energy', 0);
    setStatValue('happiness', 0);

    // Immediate sync for button clicks
    setTimeout(() => syncStatsToBackend(), 50);
  }, [setStatValue, syncStatsToBackend]);

  return (
    <div className="glass p-6 flex flex-col gap-5">
      <h2 className="text-sm font-semibold uppercase tracking-widest opacity-40">System Status</h2>

      <div className="space-y-3">
        <div className="flex justify-between text-xs font-medium opacity-70">
          <span>포만감</span>
          <span>{Math.round(stats.hunger)}%</span>
        </div>
        <div className="status-bar">
          <div
            className={`status-fill ${getBarColor(stats.hunger)}`}
            style={{ width: `${stats.hunger}%` }}
          />
        </div>
        <input
          type="range"
          min="0"
          max="100"
          value={stats.hunger}
          onChange={(e) => handleStatChange('hunger', Number(e.target.value))}
          className="w-full h-1 bg-white/10 rounded-lg appearance-none cursor-pointer accent-blue-400"
        />
      </div>

      <div className="space-y-3">
        <div className="flex justify-between text-xs font-medium opacity-70">
          <span>에너지</span>
          <span>{Math.round(stats.energy)}%</span>
        </div>
        <div className="status-bar">
          <div
            className={`status-fill ${getBarColor(stats.energy)}`}
            style={{ width: `${stats.energy}%` }}
          />
        </div>
        <input
          type="range"
          min="0"
          max="100"
          value={stats.energy}
          onChange={(e) => handleStatChange('energy', Number(e.target.value))}
          className="w-full h-1 bg-white/10 rounded-lg appearance-none cursor-pointer accent-blue-400"
        />
      </div>

      <div className="space-y-3">
        <div className="flex justify-between text-xs font-medium opacity-70">
          <span>행복도</span>
          <span>{Math.round(stats.happiness)}%</span>
        </div>
        <div className="status-bar">
          <div
            className={`status-fill ${getBarColor(stats.happiness)}`}
            style={{ width: `${stats.happiness}%` }}
          />
        </div>
        <input
          type="range"
          min="0"
          max="100"
          value={stats.happiness}
          onChange={(e) => handleStatChange('happiness', Number(e.target.value))}
          className="w-full h-1 bg-white/10 rounded-lg appearance-none cursor-pointer accent-blue-400"
        />
      </div>

      <div className="pt-2 border-t border-white/10 flex gap-2">
        <button
          onClick={handleMaxAll}
          className="flex-1 text-xs py-1.5 rounded bg-green-500/20 hover:bg-green-500/30 text-green-400 transition-colors"
        >
          MAX ALL
        </button>
        <button
          onClick={handleMinAll}
          className="flex-1 text-xs py-1.5 rounded bg-red-500/20 hover:bg-red-500/30 text-red-400 transition-colors"
        >
          MIN ALL
        </button>
      </div>
    </div>
  );
};
