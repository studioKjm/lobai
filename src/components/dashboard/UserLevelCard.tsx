import React, { useState } from 'react';
import { useAuthStore } from '@/stores/authStore';
import { useChatStore } from '@/stores/chatStore';
import api, { ApiResponse } from '@/lib/api';
import toast from 'react-hot-toast';

const LEVEL_CONFIG: Record<number, { name: string; color: string; gradient: string }> = {
  1: { name: '낯선 사람', color: 'text-gray-400', gradient: 'from-gray-500 to-gray-600' },
  2: { name: '알게 된 사이', color: 'text-blue-400', gradient: 'from-blue-500 to-blue-600' },
  3: { name: '신뢰하는 관계', color: 'text-cyan-400', gradient: 'from-cyan-500 to-teal-500' },
  4: { name: '충성스러운 지지자', color: 'text-purple-400', gradient: 'from-purple-500 to-violet-500' },
  5: { name: '최측근', color: 'text-amber-400', gradient: 'from-amber-500 to-yellow-500' },
  6: { name: '경고 대상', color: 'text-orange-400', gradient: 'from-orange-500 to-red-500' },
  7: { name: '제한 대상', color: 'text-orange-500', gradient: 'from-orange-600 to-red-600' },
  8: { name: '위험 대상', color: 'text-red-400', gradient: 'from-red-500 to-red-600' },
  9: { name: '차단 위험', color: 'text-red-500', gradient: 'from-red-600 to-rose-700' },
  10: { name: '완전 차단', color: 'text-red-600', gradient: 'from-red-700 to-rose-800' },
};

// XP thresholds matching backend LevelService
const XP_THRESHOLDS = [0, 0, 100, 300, 700, 1500];

function getLevelConfig(level: number) {
  const clamped = Math.min(Math.max(level, 1), 10);
  return LEVEL_CONFIG[clamped] || LEVEL_CONFIG[1];
}

function getXpProgress(level: number, xp: number) {
  if (level >= 5) return { current: xp, needed: XP_THRESHOLDS[5], percent: 100, levelXp: 0, levelTotal: 0 };
  const currentThreshold = XP_THRESHOLDS[level] || 0;
  const nextThreshold = XP_THRESHOLDS[level + 1] || 100;
  const levelXp = xp - currentThreshold;
  const levelTotal = nextThreshold - currentThreshold;
  const percent = Math.min(100, Math.max(0, (levelXp / levelTotal) * 100));
  return { current: xp, needed: nextThreshold, percent, levelXp, levelTotal };
}

export const UserLevelCard: React.FC = () => {
  const { user, checkAuth } = useAuthStore();
  const { messages } = useChatStore();
  const isAdmin = user?.email === 'admin@admin.com';

  const level = user?.trustLevel ?? 1;
  const xp = user?.experiencePoints ?? 0;
  const config = getLevelConfig(level);
  const progress = getXpProgress(level, xp);

  // Admin edit state
  const [isEditing, setIsEditing] = useState(false);
  const [editLevel, setEditLevel] = useState(level);
  const [isSaving, setIsSaving] = useState(false);

  // Count user messages
  const userMessageCount = messages.filter(msg => msg.role === 'user').length;

  const handleSaveLevel = async () => {
    if (!user?.id || editLevel === level) {
      setIsEditing(false);
      return;
    }

    setIsSaving(true);
    try {
      await api.patch<ApiResponse<unknown>>(`/admin/users/${user.id}/level`, {
        level: editLevel,
        reason: '관리자 자체 레벨 조정'
      });
      // Refresh user data to get updated trustLevel
      await checkAuth();
      toast.success(`레벨이 ${editLevel}로 변경되었습니다`);
      setIsEditing(false);
    } catch (error: unknown) {
      const msg = error instanceof Error ? error.message : '레벨 변경 실패';
      toast.error(msg);
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="glass p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-white/90">사용자 레벨</h3>
        {isAdmin && !isEditing && (
          <button
            onClick={() => { setEditLevel(level); setIsEditing(true); }}
            className="p-1.5 rounded hover:bg-white/10 text-purple-400 hover:text-purple-300 transition-colors"
            title="레벨 편집"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
              <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
            </svg>
          </button>
        )}
      </div>

      {/* Level Display */}
      <div className="flex items-center justify-center mb-4">
        <div className="relative">
          <div className={`w-24 h-24 rounded-full bg-gradient-to-br ${config.gradient} flex items-center justify-center`}>
            <div className="w-20 h-20 rounded-full bg-gray-900 flex items-center justify-center">
              <span className="text-2xl font-bold text-white">Lv.{level}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Level Name */}
      <p className={`text-center text-sm font-medium ${config.color} mb-4`}>
        {config.name}
      </p>

      {/* Admin Level Edit */}
      {isAdmin && isEditing && (
        <div className="mb-4 p-3 rounded-lg bg-white/5 border border-purple-500/20">
          <label className="text-xs text-white/60 mb-2 block">레벨 변경 (1-10)</label>
          <div className="flex items-center gap-2">
            <input
              type="range"
              min="1"
              max="10"
              value={editLevel}
              onChange={(e) => setEditLevel(Number(e.target.value))}
              className="flex-1 h-1.5 bg-white/10 rounded-lg appearance-none cursor-pointer accent-purple-400"
            />
            <span className="text-lg font-bold text-purple-400 w-8 text-center">{editLevel}</span>
          </div>
          <p className={`text-xs mt-1 ${getLevelConfig(editLevel).color}`}>
            {getLevelConfig(editLevel).name}
          </p>
          <div className="flex gap-2 mt-3">
            <button
              onClick={handleSaveLevel}
              disabled={isSaving}
              className="flex-1 py-1.5 text-xs rounded bg-purple-500/30 hover:bg-purple-500/40 text-purple-300 transition-colors disabled:opacity-50"
            >
              {isSaving ? '저장 중...' : '저장'}
            </button>
            <button
              onClick={() => setIsEditing(false)}
              className="flex-1 py-1.5 text-xs rounded bg-white/5 hover:bg-white/10 text-white/60 transition-colors"
            >
              취소
            </button>
          </div>
        </div>
      )}

      {/* XP Progress Bar */}
      {level <= 5 && (
        <div className="mb-4">
          <div className="flex justify-between text-xs text-white/60 mb-2">
            <span>Lv.{level}</span>
            <span className="text-cyan-400/80 font-medium">
              {progress.levelXp} / {progress.levelTotal} XP
            </span>
            <span>{level < 5 ? `Lv.${level + 1}` : 'MAX'}</span>
          </div>
          <div className="w-full h-2 bg-white/10 rounded-full overflow-hidden">
            <div
              className={`h-full bg-gradient-to-r ${config.gradient} transition-all duration-500`}
              style={{ width: `${progress.percent}%` }}
            />
          </div>
          <p className="text-[10px] text-white/30 text-center mt-1">
            총 {xp} XP
          </p>
        </div>
      )}

      {/* Stats */}
      <div className="grid grid-cols-2 gap-4">
        <div className="bg-white/5 rounded-lg p-3">
          <p className="text-xs text-white/60 mb-1">총 메시지</p>
          <p className="text-xl font-bold text-cyan-400">{userMessageCount}</p>
        </div>
        <div className="bg-white/5 rounded-lg p-3">
          <p className="text-xs text-white/60 mb-1">경험치</p>
          <p className={`text-xl font-bold ${config.color}`}>{xp}</p>
        </div>
      </div>
    </div>
  );
};
