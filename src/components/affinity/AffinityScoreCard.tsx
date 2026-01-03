/**
 * AffinityScoreCard Component
 *
 * Main affinity score display with cosmic connection aesthetic
 */

import { useEffect, useState } from 'react';
import { AffinityProgressRing } from './AffinityProgressRing';
import { ScoreBar } from './ScoreBar';
import { getAffinityScore } from '@/services/affinityApi';
import type { AffinityScore } from '@/types/affinity';

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

  // Normalize sentiment score from -1~1 to 0~1 for display
  const sentimentNormalized = (affinityScore.avgSentimentScore + 1) / 2;

  return (
    <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-slate-900/90 via-purple-900/50 to-violet-900/90 backdrop-blur-xl border border-white/10 shadow-2xl">
      {/* Animated background particles */}
      <div className="absolute inset-0 opacity-30">
        <div className="absolute top-10 left-10 w-32 h-32 bg-purple-500/20 rounded-full blur-3xl animate-pulse" />
        <div className="absolute bottom-10 right-10 w-40 h-40 bg-cyan-500/20 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '1s' }} />
        <div className="absolute top-1/2 left-1/2 w-24 h-24 bg-pink-500/20 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '2s' }} />
      </div>

      <div className="relative p-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-2xl font-bold text-white mb-1">친밀도 분석</h2>
            <p className="text-sm text-slate-400">
              총 {affinityScore.totalMessages}개의 대화 분석 완료
            </p>
          </div>

          {/* Refresh button */}
          <button
            onClick={loadAffinityScore}
            className="p-2 rounded-lg bg-white/5 hover:bg-white/10 border border-white/10 transition-all hover:scale-110 active:scale-95"
            title="새로고침"
          >
            <svg
              className="w-5 h-5 text-slate-300"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
              />
            </svg>
          </button>
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
                  style={{
                    width: `${affinityScore.progress.progressPercentage}%`,
                  }}
                />
              </div>
              <div className="flex items-center justify-between text-xs text-slate-500 mt-1">
                <span>{affinityScore.progress.currentLevelMin}</span>
                <span>{affinityScore.progress.currentLevelMax}</span>
              </div>
            </div>
          </div>

          {/* Right: Individual Scores */}
          <div className="space-y-5">
            <div className="mb-4">
              <h3 className="text-sm font-semibold text-slate-300 uppercase tracking-wider mb-1">
                상세 점수
              </h3>
              <p className="text-xs text-slate-500">
                AI와의 소통 품질을 4가지 지표로 분석합니다
              </p>
            </div>

            <ScoreBar
              label="감정 점수"
              value={sentimentNormalized}
              maxValue={1}
              color="#3b82f6"
              delay={200}
            />

            <ScoreBar
              label="명확성 점수"
              value={affinityScore.avgClarityScore}
              maxValue={1}
              color="#8b5cf6"
              delay={400}
            />

            <ScoreBar
              label="맥락 유지 점수"
              value={affinityScore.avgContextScore}
              maxValue={1}
              color="#ec4899"
              delay={600}
            />

            <ScoreBar
              label="활용 태도 점수"
              value={affinityScore.avgUsageScore}
              maxValue={1}
              color="#06b6d4"
              delay={800}
            />
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
              {new Date(affinityScore.updatedAt).toLocaleString('ko-KR', {
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
              })}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}
