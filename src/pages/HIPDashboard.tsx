// HIP Dashboard Page
import { useState, useEffect } from 'react';
import { Navbar } from '@/components/common/Navbar';
import { ScoreChart, IdentityCard, BlockchainSection } from '@/components/hip';
import { hipApi } from '@/api/hipApi';
import type { HIPProfile, ReanalyzeResponse } from '@/types/hip';
import toast from 'react-hot-toast';

export default function HIPDashboard() {
  const [profile, setProfile] = useState<HIPProfile | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isReanalyzing, setIsReanalyzing] = useState(false);

  // Load profile on mount
  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    setIsLoading(true);
    try {
      const data = await hipApi.getMyProfile();
      setProfile(data);
    } catch (error) {
      console.error('Failed to load HIP profile:', error);
      toast.error('HIP 프로필을 불러오는데 실패했습니다');
    } finally {
      setIsLoading(false);
    }
  };

  const handleReanalyze = async () => {
    if (isReanalyzing) return;

    setIsReanalyzing(true);
    const reanalyzeToast = toast.loading('HIP 프로필을 재분석 중입니다...');

    try {
      const result: ReanalyzeResponse = await hipApi.reanalyze();

      // Update profile with new scores
      if (profile) {
        const updatedCoreScores = Object.keys(profile.coreScores).reduce((acc, key) => {
          const scoreKey = key as keyof typeof profile.coreScores;
          const newValue = result.updatedScores[key]?.new || profile.coreScores[scoreKey];
          acc[scoreKey] = newValue;
          return acc;
        }, {} as typeof profile.coreScores);

        setProfile({
          ...profile,
          overallHipScore: result.newScore,
          identityLevel: result.newLevel,
          coreScores: updatedCoreScores,
          lastUpdatedAt: result.updatedAt
        });
      }

      toast.success(
        `${result.message}\nScore: ${result.previousScore.toFixed(1)} → ${result.newScore.toFixed(1)} (${result.scoreChange > 0 ? '+' : ''}${result.scoreChange.toFixed(1)})`,
        { id: reanalyzeToast, duration: 5000 }
      );

      if (result.levelChanged) {
        toast.success(
          `🎉 Level Up! ${result.previousLevel} → ${result.newLevel}`,
          { duration: 5000 }
        );
      }
    } catch (error) {
      console.error('Failed to reanalyze:', error);
      toast.error('프로필 재분석에 실패했습니다', { id: reanalyzeToast });
    } finally {
      setIsReanalyzing(false);
    }
  };

  const handleShare = () => {
    if (!profile) return;

    const shareText = `내 HIP 점수를 확인해보세요: ${profile.overallHipScore.toFixed(1)}점 (레벨 ${profile.identityLevel} - ${profile.reputationTier})!\n\nHIP ID: ${profile.hipId}`;
    const shareUrl = `${window.location.origin}/hip/${profile.hipId}`;

    if (navigator.share) {
      navigator.share({
        title: '나의 인간 신원 프로토콜 점수',
        text: shareText,
        url: shareUrl
      }).catch(() => {
        // Fallback to clipboard
        copyToClipboard(shareText);
      });
    } else {
      copyToClipboard(shareText);
    }
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text).then(() => {
      toast.success('클립보드에 복사되었습니다!');
    }).catch(() => {
      toast.error('복사에 실패했습니다');
    });
  };

  if (isLoading) {
    return (
      <>
        <Navbar />
        <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 flex items-center justify-center pt-20">
          <div className="text-center">
            <div className="inline-block animate-spin rounded-full h-16 w-16 border-4 border-indigo-600 border-t-transparent"></div>
            <p className="mt-4 text-gray-600 dark:text-gray-400">HIP 프로필을 불러오는 중...</p>
          </div>
        </div>
      </>
    );
  }

  if (!profile) {
    return (
      <>
        <Navbar />
        <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 flex items-center justify-center pt-20">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">
              HIP 프로필을 찾을 수 없습니다
            </h2>
            <p className="text-gray-600 dark:text-gray-400 mb-6">
              아직 인간 신원 프로토콜 프로필이 없습니다.
            </p>
            <button
              onClick={loadProfile}
              className="px-6 py-3 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
            >
              HIP 프로필 생성하기
            </button>
          </div>
        </div>
      </>
    );
  }

  return (
    <>
      <Navbar />
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 pt-20">
        <div className="container mx-auto px-4 py-12">
        {/* Header */}
        <div className="text-center mb-12">
          <h1 className="text-5xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent mb-4">
            인간 신원 프로토콜
          </h1>
          <p className="text-xl text-gray-600 dark:text-gray-400">
            AI가 평가한 당신의 신원 프로필
          </p>
        </div>

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Identity Card */}
          <IdentityCard profile={profile} />

          {/* Score Chart */}
          <ScoreChart coreScores={profile.coreScores} />
        </div>

        {/* Blockchain Section */}
        <BlockchainSection profile={profile} className="mb-8" />

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
          <button
            onClick={handleReanalyze}
            disabled={isReanalyzing}
            className={`px-8 py-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-xl font-semibold text-lg shadow-lg hover:shadow-xl transition-all ${
              isReanalyzing ? 'opacity-50 cursor-not-allowed' : 'hover:scale-105'
            }`}
          >
            {isReanalyzing ? (
              <span className="flex items-center gap-2">
                <div className="inline-block animate-spin rounded-full h-5 w-5 border-2 border-white border-t-transparent"></div>
                재분석 중...
              </span>
            ) : (
              '🔄 HIP 재분석'
            )}
          </button>

          <button
            onClick={handleShare}
            className="px-8 py-4 bg-white dark:bg-gray-800 text-indigo-600 dark:text-indigo-400 border-2 border-indigo-600 dark:border-indigo-400 rounded-xl font-semibold text-lg shadow-lg hover:shadow-xl transition-all hover:scale-105"
          >
            📤 프로필 공유
          </button>

          <button
            onClick={() => window.location.href = '/dashboard/ranking'}
            className="px-8 py-4 bg-white dark:bg-gray-800 text-purple-600 dark:text-purple-400 border-2 border-purple-600 dark:border-purple-400 rounded-xl font-semibold text-lg shadow-lg hover:shadow-xl transition-all hover:scale-105"
          >
            🏆 랭킹 보기
          </button>
        </div>

        {/* Info Section */}
        <div className="mt-12 bg-white dark:bg-gray-800 rounded-xl shadow-lg p-8">
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">
            HIP이란 무엇인가요?
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-gray-700 dark:text-gray-300">
            <div>
              <h3 className="font-semibold text-indigo-600 dark:text-indigo-400 mb-2">
                🆔 인간 신원 프로토콜
              </h3>
              <p className="text-sm">
                AI 시스템이 상호작용 패턴을 기반으로 개별 인간을 인식하고 평가하는 표준화된 프로토콜입니다.
              </p>
            </div>
            <div>
              <h3 className="font-semibold text-purple-600 dark:text-purple-400 mb-2">
                📊 6가지 핵심 점수
              </h3>
              <p className="text-sm">
                당신의 정체성은 6가지 차원에서 측정됩니다: 인지적 유연성, 협업 패턴, 정보 처리, 감정 지능, 창의성, 윤리적 정렬.
              </p>
            </div>
            <div>
              <h3 className="font-semibold text-pink-600 dark:text-pink-400 mb-2">
                🔐 검증된 신원
              </h3>
              <p className="text-sm">
                당신의 HIP ID는 암호화되어 보호되며 여러 AI 시스템에서 검증될 수 있습니다.
              </p>
            </div>
            <div>
              <h3 className="font-semibold text-orange-600 dark:text-orange-400 mb-2">
                🌐 크로스 AI 신원
              </h3>
              <p className="text-sm">
                여러 AI 모델(GPT, Claude, Gemini)이 당신을 어떻게 인식하는지 비교해보세요. (Phase 2에서 제공 예정)
              </p>
            </div>
          </div>
        </div>
        </div>
      </div>
    </>
  );
}
