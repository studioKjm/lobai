/**
 * ResilienceReportCard Component
 * AI 적응력 리포트 카드 (Phase 2 - 친밀도 연동 + 추천)
 */

import { useEffect, useState } from 'react';
import { generateReport, getLatestReport } from '@/services/resilienceApi';
import type { ResilienceReport } from '@/types/resilience';

export function ResilienceReportCard() {
  const [report, setReport] = useState<ResilienceReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadReport();
  }, []);

  const loadReport = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getLatestReport();
      setReport(data);
    } catch (err) {
      console.error('Failed to load resilience report:', err);
      setError('리포트를 불러올 수 없습니다');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerate = async () => {
    try {
      setGenerating(true);
      setError(null);
      const newReport = await generateReport('basic');
      setReport(newReport);
    } catch (err: any) {
      setError(err.message || '리포트 생성에 실패했습니다');
    } finally {
      setGenerating(false);
    }
  };

  if (loading) {
    return (
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-slate-900/90 via-cyan-900/50 to-blue-900/90 backdrop-blur-xl border border-white/10 p-8">
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-4 border-cyan-500/30 border-t-cyan-500" />
        </div>
      </div>
    );
  }

  if (!report) {
    return (
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-slate-900/90 via-cyan-900/50 to-blue-900/90 backdrop-blur-xl border border-white/10 shadow-2xl">
        <div className="absolute inset-0 opacity-20">
          <div className="absolute top-10 left-10 w-32 h-32 bg-cyan-500/20 rounded-full blur-3xl animate-pulse" />
          <div className="absolute bottom-10 right-10 w-40 h-40 bg-blue-500/20 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '1s' }} />
        </div>

        <div className="relative p-8 text-center">
          <div className="text-6xl mb-4">🤖</div>
          <h3 className="text-2xl font-bold text-white mb-2">AI 적응력 분석</h3>
          <p className="text-slate-400 mb-6">
            당신의 AI 소통 능력을 분석하고<br />
            AI 시대에 필요한 역량을 진단합니다
          </p>

          {error && (
            <div className="mb-4 p-3 bg-red-500/20 border border-red-500/30 rounded-lg text-red-300 text-sm">
              {error}
            </div>
          )}

          <button
            onClick={handleGenerate}
            disabled={generating}
            className={`
              px-8 py-4 rounded-xl font-semibold text-white transition-all duration-300
              bg-gradient-to-r from-cyan-500 to-blue-500 hover:from-cyan-600 hover:to-blue-600
              active:scale-95 shadow-lg hover:shadow-xl
              ${generating ? 'opacity-50 cursor-wait' : ''}
            `}
          >
            {generating ? '분석 중...' : '리포트 생성하기'}
          </button>

          <p className="text-xs text-slate-500 mt-4">
            최소 10개 이상의 대화 기록이 필요합니다
          </p>
        </div>
      </div>
    );
  }

  const getLevelColor = (level: number) => {
    switch (level) {
      case 1: return { from: '#64748b', to: '#475569' };
      case 2: return { from: '#3b82f6', to: '#06b6d4' };
      case 3: return { from: '#a855f7', to: '#ec4899' };
      case 4: return { from: '#8b5cf6', to: '#d946ef' };
      case 5: return { from: '#f59e0b', to: '#f43f5e' };
      default: return { from: '#a855f7', to: '#ec4899' };
    }
  };

  const colors = getLevelColor(report.readinessLevel);

  return (
    <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-slate-900/90 via-cyan-900/50 to-blue-900/90 backdrop-blur-xl border border-white/10 shadow-2xl">
      {/* Animated background */}
      <div className="absolute inset-0 opacity-20">
        <div className="absolute top-10 left-10 w-32 h-32 bg-cyan-500/20 rounded-full blur-3xl animate-pulse" />
        <div className="absolute bottom-10 right-10 w-40 h-40 bg-blue-500/20 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '1s' }} />
        <div className="absolute top-1/2 left-1/2 w-24 h-24 bg-purple-500/20 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '2s' }} />
      </div>

      <div className="relative p-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-2xl font-bold text-white mb-1 flex items-center gap-2">
              <span className="text-3xl">🧠</span>
              AI 적응력 분석
            </h2>
            <p className="text-sm text-slate-400">
              {report.analyzedMessageCount}개의 대화 분석 | {new Date(report.createdAt).toLocaleDateString('ko-KR')}
            </p>
          </div>

          <button
            onClick={handleGenerate}
            disabled={generating}
            className="px-4 py-2 rounded-lg bg-cyan-500/20 hover:bg-cyan-500/30 border border-cyan-500/30 transition-all text-sm font-medium text-cyan-300"
          >
            {generating ? '분석 중...' : '재분석'}
          </button>
        </div>

        {/* Main Score */}
        <div className="text-center mb-8">
          <div className="inline-block relative">
            <div
              className="absolute inset-0 rounded-full opacity-30 blur-xl"
              style={{ background: `radial-gradient(circle, ${colors.from}40 0%, transparent 70%)` }}
            />
            <div
              className="relative inline-flex items-center justify-center w-32 h-32 rounded-full border-4"
              style={{
                borderColor: colors.from,
                background: `linear-gradient(135deg, ${colors.from}20, ${colors.to}20)`,
              }}
            >
              <div>
                <div className="text-4xl font-bold text-white">{Math.round(report.readinessScore)}</div>
                <div className="text-xs text-slate-300">/ 100</div>
              </div>
            </div>
          </div>
          <div
            className="mt-3 inline-block px-4 py-2 rounded-full text-sm font-semibold"
            style={{
              background: `linear-gradient(135deg, ${colors.from}30, ${colors.to}30)`,
              border: `1px solid ${colors.from}60`,
              color: colors.to,
            }}
          >
            Lv.{report.readinessLevel} {report.readinessLevelName}
          </div>
        </div>

        {/* Affinity Integration Section */}
        {report.affinityScoreAtReport != null && (
          <div className="mb-6 p-4 bg-purple-500/10 border border-purple-500/20 rounded-xl">
            <h4 className="text-sm font-semibold text-purple-300 mb-3 flex items-center gap-1">
              <span>💜</span> 친밀도 연동
            </h4>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
              <MiniStat label="친밀도" value={`${Math.round(report.affinityScoreAtReport)}`} sub={`Lv.${report.affinityLevelAtReport}`} color="#a855f7" />
              {report.avgEngagementDepth != null && (
                <MiniStat label="참여 깊이" value={report.avgEngagementDepth.toFixed(2)} color="#10b981" />
              )}
              {report.avgSelfDisclosure != null && (
                <MiniStat label="자기 개방도" value={report.avgSelfDisclosure.toFixed(2)} color="#f59e0b" />
              )}
              {report.avgReciprocity != null && (
                <MiniStat label="상호작용" value={report.avgReciprocity.toFixed(2)} color="#ef4444" />
              )}
            </div>
          </div>
        )}

        {/* 4 Scores Grid */}
        <div className="grid md:grid-cols-2 gap-4 mb-6">
          <ScoreItem label="AI 친화 커뮤니케이션" score={report.communicationScore} color="#3b82f6" />
          <ScoreItem label="AI 협업 적합도" score={report.collaborationScore} color="#8b5cf6" />
          <ScoreItem label="자동화 위험도" score={report.automationRiskScore} color="#f59e0b" inverted />
          <ScoreItem label="AI 오용 가능성" score={report.misuseRiskScore} color="#ef4444" inverted />
        </div>

        {/* Strengths & Weaknesses */}
        {(report.strengths?.length > 0 || report.weaknesses?.length > 0) && (
          <div className="grid md:grid-cols-2 gap-4 mb-6">
            {report.strengths && report.strengths.length > 0 && (
              <div className="bg-green-500/10 border border-green-500/20 rounded-xl p-4">
                <h4 className="text-sm font-semibold text-green-400 mb-2 flex items-center gap-1">
                  <span>💪</span> 강점
                </h4>
                <ul className="space-y-1">
                  {report.strengths.map((strength, idx) => (
                    <li key={idx} className="text-xs text-slate-300 flex items-start gap-2">
                      <span className="text-green-400 mt-0.5">•</span>
                      <span>{strength}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}
            {report.weaknesses && report.weaknesses.length > 0 && (
              <div className="bg-orange-500/10 border border-orange-500/20 rounded-xl p-4">
                <h4 className="text-sm font-semibold text-orange-400 mb-2 flex items-center gap-1">
                  <span>🎯</span> 개선점
                </h4>
                <ul className="space-y-1">
                  {report.weaknesses.map((weakness, idx) => (
                    <li key={idx} className="text-xs text-slate-300 flex items-start gap-2">
                      <span className="text-orange-400 mt-0.5">•</span>
                      <span>{weakness}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        )}

        {/* Recommendations Section */}
        {report.recommendations && report.recommendations.length > 0 && (
          <div className="mb-6 bg-cyan-500/10 border border-cyan-500/20 rounded-xl p-4">
            <h4 className="text-sm font-semibold text-cyan-400 mb-3 flex items-center gap-1">
              <span>📋</span> 추천 행동
            </h4>
            <ul className="space-y-2">
              {report.recommendations.map((rec, idx) => (
                <li key={idx} className="flex items-start gap-3 text-xs text-slate-300">
                  <span className="flex-shrink-0 w-5 h-5 mt-0.5 rounded border border-cyan-500/30 bg-cyan-500/10 flex items-center justify-center text-cyan-400 text-[10px]">
                    {idx + 1}
                  </span>
                  <span>{rec}</span>
                </li>
              ))}
            </ul>
          </div>
        )}

        {/* Detailed Feedback */}
        {report.detailedFeedback && (
          <div className="bg-white/5 border border-white/10 rounded-xl p-4">
            <h4 className="text-sm font-semibold text-slate-300 mb-2">상세 피드백</h4>
            <p className="text-xs text-slate-400 leading-relaxed">{report.detailedFeedback}</p>
          </div>
        )}

        {error && (
          <div className="mt-4 p-3 bg-red-500/20 border border-red-500/30 rounded-lg text-red-300 text-sm text-center">
            {error}
          </div>
        )}
      </div>
    </div>
  );
}

interface ScoreItemProps {
  label: string;
  score: number;
  color: string;
  inverted?: boolean;
}

function ScoreItem({ label, score, color, inverted }: ScoreItemProps) {
  const displayScore = inverted ? 100 - score : score;
  const percentage = Math.round(displayScore);

  return (
    <div className="bg-white/5 border border-white/10 rounded-xl p-4">
      <div className="flex items-center justify-between mb-2">
        <span className="text-xs text-slate-300 font-medium">{label}</span>
        <span className="text-sm font-bold text-white">{percentage}</span>
      </div>
      <div className="h-2 bg-white/10 rounded-full overflow-hidden">
        <div
          className="h-full rounded-full transition-all duration-1000 ease-out"
          style={{ width: `${percentage}%`, backgroundColor: color }}
        />
      </div>
      {inverted && (
        <p className="text-xs text-slate-500 mt-1">낮을수록 안전</p>
      )}
    </div>
  );
}

interface MiniStatProps {
  label: string;
  value: string;
  sub?: string;
  color: string;
}

function MiniStat({ label, value, sub, color }: MiniStatProps) {
  return (
    <div className="text-center p-2 rounded-lg bg-white/5">
      <div className="text-lg font-bold" style={{ color }}>{value}</div>
      {sub && <div className="text-[10px] text-slate-400">{sub}</div>}
      <div className="text-[10px] text-slate-500 mt-0.5">{label}</div>
    </div>
  );
}
