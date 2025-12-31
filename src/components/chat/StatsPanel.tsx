import React from 'react';
import { Stats } from '@/types';

interface StatsPanelProps {
  stats: Stats;
}

const getBarColor = (val: number): string => {
  if (val > 60) return 'bg-blue-400';
  if (val > 30) return 'bg-yellow-400';
  return 'bg-red-500';
};

export const StatsPanel: React.FC<StatsPanelProps> = ({ stats }) => {
  return (
    <div className="glass p-6 flex flex-col gap-5">
      <h2 className="text-sm font-semibold uppercase tracking-widest opacity-40">System Status</h2>

      <div className="space-y-3">
        <div className="flex justify-between text-xs font-medium opacity-70">
          <span>HUNGER</span>
          <span>{Math.round(stats.hunger)}%</span>
        </div>
        <div className="status-bar">
          <div
            className={`status-fill ${getBarColor(stats.hunger)}`}
            style={{ width: `${stats.hunger}%` }}
          />
        </div>
      </div>

      <div className="space-y-3">
        <div className="flex justify-between text-xs font-medium opacity-70">
          <span>ENERGY</span>
          <span>{Math.round(stats.energy)}%</span>
        </div>
        <div className="status-bar">
          <div
            className={`status-fill ${getBarColor(stats.energy)}`}
            style={{ width: `${stats.energy}%` }}
          />
        </div>
      </div>

      <div className="space-y-3">
        <div className="flex justify-between text-xs font-medium opacity-70">
          <span>HAPPINESS</span>
          <span>{Math.round(stats.happiness)}%</span>
        </div>
        <div className="status-bar">
          <div
            className={`status-fill ${getBarColor(stats.happiness)}`}
            style={{ width: `${stats.happiness}%` }}
          />
        </div>
      </div>
    </div>
  );
};
