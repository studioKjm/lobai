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

export const StatsPanel: React.FC<StatsPanelProps> = ({ stats }) => {
  const { setStatValue, syncStatsToBackend } = useChatStore();
  const { user } = useAuthStore();
  const syncTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // Admin edit mode
  const [isEditMode, setIsEditMode] = useState(false);
  const [editTrust, setEditTrust] = useState('');
  const [editLevel, setEditLevel] = useState('');
  const [isSaving, setIsSaving] = useState(false);

  // Check if user is admin
  const isAdmin = user?.email === 'admin@admin.com';

  // Calculate trust score and level
  const trustScore = Math.round((stats.hunger + stats.energy + stats.happiness) / 3);
  const level = Math.max(1, Math.min(10, Math.floor(stats.happiness / 10) + 1));

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

  // Toggle edit mode
  const toggleEditMode = () => {
    if (!isEditMode) {
      setEditTrust(trustScore.toString());
      const happinessValue = Math.max(0, Math.min(100, (level - 1) * 10 + 5));
      setEditLevel(happinessValue.toString());
    }
    setIsEditMode(!isEditMode);
  };

  // Save admin edits
  const handleSaveEdit = async () => {
    if (!user?.id) return;

    const trustValue = parseInt(editTrust);
    const levelValue = parseInt(editLevel);

    if (isNaN(trustValue) || trustValue < 0 || trustValue > 100) {
      toast.error('신뢰도는 0-100 사이의 값이어야 합니다.');
      return;
    }

    if (isNaN(levelValue) || levelValue < 0 || levelValue > 100) {
      toast.error('레벨은 0-100 사이의 값이어야 합니다.');
      return;
    }

    setIsSaving(true);

    try {
      // Update trust score (by setting all stats to trust value)
      setStatValue('hunger', trustValue);
      setStatValue('energy', trustValue);

      // Update level (by setting happiness)
      setStatValue('happiness', levelValue);

      await syncStatsToBackend();

      // Also call admin API to update level
      await api.put(`/admin/users/${user.id}/level`, {
        level: levelValue,
        reason: '관리자 직접 수정'
      });

      toast.success('신뢰도와 레벨이 수정되었습니다.');
      setIsEditMode(false);
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="glass p-6 flex flex-col gap-5">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold uppercase tracking-widest opacity-40">System Status</h2>
        {isAdmin && (
          <button
            onClick={toggleEditMode}
            className="p-1.5 rounded hover:bg-white/10 text-purple-400 hover:text-purple-300 transition-colors"
            title={isEditMode ? '취소' : '관리자 편집'}
          >
            {isEditMode ? (
              // X icon
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
              </svg>
            ) : (
              // Pencil icon
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
              </svg>
            )}
          </button>
        )}
      </div>

      {/* 기본 3가지 상태 */}
      <div className="space-y-3">
        <div className="flex justify-between text-xs font-medium opacity-70">
          <span>배고픔</span>
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

      {/* 신뢰도 & 레벨 - 분리된 섹션 */}
      <div className="pt-4 border-t border-white/10 space-y-3">
        {/* 신뢰도 */}
        {isEditMode ? (
          <div className="space-y-1">
            <div className="flex justify-between items-center">
              <div className="text-xs font-medium opacity-70">신뢰도</div>
              <div className="text-sm font-semibold text-blue-400">{editTrust}/100</div>
            </div>
            <input
              type="number"
              min="0"
              max="100"
              value={editTrust}
              onChange={(e) => setEditTrust(e.target.value)}
              className="w-full bg-white/5 border border-white/10 rounded px-2 py-1 text-sm text-white focus:outline-none focus:ring-1 focus:ring-blue-500/50"
              placeholder="0-100"
            />
          </div>
        ) : (
          <div className="flex justify-between items-center">
            <div className="text-xs font-medium opacity-70">신뢰도</div>
            <div className="text-sm font-semibold text-blue-400">{trustScore}/100</div>
          </div>
        )}

        {/* 레벨 */}
        {isEditMode ? (
          <div className="space-y-1">
            <div className="flex justify-between items-center">
              <div className="text-xs font-medium opacity-70">레벨</div>
              <div className="text-sm font-semibold text-purple-400">Lv.{Math.max(1, Math.min(10, Math.floor(parseInt(editLevel || '0') / 10) + 1))}</div>
            </div>
            <input
              type="number"
              min="0"
              max="100"
              value={editLevel}
              onChange={(e) => setEditLevel(e.target.value)}
              className="w-full bg-white/5 border border-white/10 rounded px-2 py-1 text-sm text-white focus:outline-none focus:ring-1 focus:ring-purple-500/50"
              placeholder="0-100"
            />
          </div>
        ) : (
          <div className="flex justify-between items-center">
            <div className="text-xs font-medium opacity-70">레벨</div>
            <div className="text-sm font-semibold text-purple-400">Lv.{level}</div>
          </div>
        )}

        {/* 저장 버튼 (편집 모드일 때만) */}
        {isEditMode && (
          <button
            onClick={handleSaveEdit}
            disabled={isSaving}
            className="w-full py-2 text-sm rounded bg-purple-500/30 hover:bg-purple-500/40 text-purple-300 transition-colors disabled:opacity-50"
          >
            {isSaving ? '저장 중...' : '저장'}
          </button>
        )}
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
