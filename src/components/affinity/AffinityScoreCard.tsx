/**
 * AffinityScoreCard Component
 *
 * 7-dimension affinity score display with cosmic connection aesthetic (Phase 2)
 */

import { useEffect, useState } from 'react';
import { AffinityProgressRing } from './AffinityProgressRing';
import { ScoreBar } from './ScoreBar';
import { getAffinityScore, recalculateAffinityScore } from '@/services/affinityApi';
import type { AffinityScore } from '@/types/affinity';
import { STAGE_ICONS } from '@/types/affinity';
import toast from 'react-hot-toast';

export function AffinityScoreCard() {
  const [affinityScore, setAffinityScore] = useState<AffinityScore | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadAffinityScore();
  }, []);

  const loadAffinityScore = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getAffinityScore();
      setAffinityScore(data);
    } catch (err) {
      console.error('Failed to load affinity score:', err);
      setError('친밀도 점수를 불러올 수 없습니다');
    } finally {
      setLoading(false);
    }
  };

  const handleRecalculate = async () => {
    try {
      setLoading(true);
      setError(null);
      toast.loading('점수를 재계산하는 중...', { id: 'recalc' });
      const data = await recalculateAffinityScore();
      setAffinityScore(data);
      toast.success('점수가 업데이트되었습니다!', { id: 'recalc' });
    } catch (err) {
      console.error('Failed to recalculate affinity score:', err);
      setError('점수 재계산에 실패했습니다');
      toast.error('재계산 실패', { id: 'recalc' });
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-slate-900/90 via-purple-900/50 to-violet-900/90 backdrop-blur-xl border border-white/10 p-8">
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-4 border-purple-500/30 border-t-purple-500" />
        </div>
      </div>
    );
  }

  if (error || !affinityScore) {
    return (
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-slate-900/90 via-purple-900/50 to-violet-900/90 backdrop-blur-xl border border-white/10 p-8">
        <div className="text-center text-slate-400">
          <p>{error || '데이터를 불러올 수 없습니다'}</p>
          <button
            onClick={loadAffinityScore}
            className="mt-4 px-4 py-2 bg-purple-500/20 hover:bg-purple-500/30 rounded-lg transition-colors"
          >
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  const sentimentNormalized = ((affinityScore.avgSentimentScore ?? 0) + 1) / 2;
  const isCollecting = affinityScore.dataThresholdStatus === 'COLLECTING';
  const isInitial = affinityScore.dataThresholdStatus === 'INITIAL';
  const stageIcon = STAGE_ICONS[affinityScore.relationshipStage] || '🌱';

  return (
    <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-slate-900/90 via-purple-900/50 to-violet-900/90 backdrop-blur-xl border border-white/10 shadow-2xl">
      {/* Animated background particles */}
      <div className="absolute inset-0 opacity-30">
        <div className="absolute top-10 left-10 w-32 h-32 bg-purple-500/20 rounded-full blur-3xl animate-pulse" />
        <div className="absolute bottom-10 right-10 w-40 h-40 bg-cyan-500/20 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '1s' }} />
        <div className="absolute top-1/2 left-1/2 w-24 h-24 bg-pink-500/20 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '2s' }} />
      </div>

      <div className="relative p-8">
        {/* Data Threshold Banner */}
        {(isCollecting || isInitial) && (
          <div className={`mb-4 px-4 py-3 rounded-xl border ${
            isCollecting
              ? 'bg-amber-500/10 border-amber-500/20'
              : 'bg-blue-500/10 border-blue-500/20'
          }`}>
            <div className="flex items-center gap-2 mb-2">
              <span className="text-sm">{isCollecting ? '📊' : '🔍'}</span>
              <span className={`text-xs font-semibold ${
                isCollecting ? 'text-amber-400' : 'text-blue-400'
              }`}>
                {affinityScore.dataThresholdMessage}
              </span>
            </div>
            {isCollecting && (
              <div className="h-1.5 bg-white/10 rounded-full overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-amber-500 to-orange-500 rounded-full transition-all duration-1000"
                  style={{ width: `${Math.min(100, (affinityScore.totalMessages / 10) * 100)}%` }}
                />
              </div>
            )}
          </div>
        )}

        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-2xl font-bold text-white mb-1 flex items-center gap-2">
              친밀도 분석
              {/* Relationship Stage Badge */}
              <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-white/10 border border-white/10">
                <span>{stageIcon}</span>
                <span className="text-slate-300">{affinityScore.relationshipStageName}</span>
              </span>
            </h2>
            <p className="text-sm text-slate-400">
              총 {affinityScore.totalMessages}개의 대화 | {affinityScore.totalSessions}개의 세션 분석
            </p>
          </div>

          {/* Recalculate button */}
          <button
            onClick={handleRecalculate}
            disabled={loading}
            className="p-2 rounded-lg bg-purple-500/20 hover:bg-purple-500/30 border border-purple-500/30 transition-all hover:scale-110 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
            title="점수 재계산 (모든 메시지 분석)"
          >
            <svg className="w-5 h-5 text-purple-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
          </button>
        </div>

        {/* Badges Row */}
        <div className="flex flex-wrap gap-2 mb-6">
          {/* Novelty Discount */}
          {affinityScore.noveltyDiscountFactor < 1.0 && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-purple-500/15 border border-purple-500/20 text-purple-300">
              <span>✨</span> 초기 보정 적용 중 ({Math.round(affinityScore.noveltyDiscountFactor * 100)}%)
            </span>
          )}
          {/* Honorific Transition */}
          {affinityScore.honorificTransitionDetected && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-500/15 border border-emerald-500/20 text-emerald-300">
              <span>💬</span> 반말 전환 감지
            </span>
          )}
        </div>

        {/* Main content grid */}
        <div className="grid md:grid-cols-2 gap-8">
          {/* Left: Progress Ring */}
          <div className="flex flex-col items-center justify-center">
            <AffinityProgressRing
              score={affinityScore.overallScore}
              level={affinityScore.level}
              levelName={affinityScore.levelName}
            />

            {/* Level progress bar */}
            <div className="w-full mt-6 px-4">
              <div className="flex items-center justify-between text-xs text-slate-400 mb-2">
                <span>레벨 진행도</span>
                <span>{affinityScore.progress.progressPercentage.toFixed(0)}%</span>
              </div>
              <div className="h-1.5 bg-white/5 rounded-full overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-purple-500 to-pink-500 rounded-full transition-all duration-1000 ease-out"
                  style={{ width: `${affinityScore.progress.progressPercentage}%` }}
                />
              </div>
              <div className="flex items-center justify-between text-xs text-slate-500 mt-1">
                <span>{affinityScore.progress.currentLevelMin}</span>
                <span>{affinityScore.progress.currentLevelMax}</span>
              </div>
            </div>
          </div>

          {/* Right: All 7 Scores */}
          <div className="space-y-4">
            <div className="mb-3">
              <h3 className="text-sm font-semibold text-slate-300 uppercase tracking-wider mb-1">
                상세 점수 (7차원)
              </h3>
              <p className="text-xs text-slate-500">
                AI와의 소통 품질을 7가지 지표로 분석합니다
              </p>
            </div>

            {/* 기존 4차원 */}
            <ScoreBar label="감정 점수" value={sentimentNormalized} maxValue={1} color="#3b82f6" delay={200} />
            <ScoreBar label="명확성 점수" value={affinityScore.avgClarityScore} maxValue={1} color="#8b5cf6" delay={300} />
            <ScoreBar label="맥락 유지 점수" value={affinityScore.avgContextScore} maxValue={1} color="#ec4899" delay={400} />
            <ScoreBar label="활용 태도 점수" value={affinityScore.avgUsageScore} maxValue={1} color="#06b6d4" delay={500} />

            {/* 구분선 */}
            <div className="border-t border-white/5 pt-2">
              <p className="text-xs text-slate-500 mb-3">관계 깊이 지표</p>
            </div>

            {/* 새 3차원 */}
            <ScoreBar label="참여 깊이" value={affinityScore.avgEngagementDepth} maxValue={1} color="#10b981" delay={600} />
            <ScoreBar label="자기 개방도" value={affinityScore.avgSelfDisclosure} maxValue={1} color="#f59e0b" delay={700} />
            <ScoreBar label="상호작용 품질" value={affinityScore.avgReciprocity} maxValue={1} color="#ef4444" delay={800} />
          </div>
        </div>

        {/* Footer info */}
        <div className="mt-8 pt-6 border-t border-white/10">
          <div className="flex items-center justify-between text-xs text-slate-500">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
              <span>실시간 업데이트</span>
            </div>
            <span>
              마지막 업데이트:{' '}
              {affinityScore.updatedAt ? new Date(affinityScore.updatedAt).toLocaleString('ko-KR', {
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
              }) : '-'}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}
