// HIP Identity Profile Card Component
import { formatDistanceToNow } from 'date-fns';
import type { HIPProfile } from '@/types/hip';

interface IdentityCardProps {
  profile: HIPProfile;
  className?: string;
}

// Badge component for verification status
function VerificationBadge({ status }: { status: HIPProfile['verificationStatus'] }) {
  const styles = {
    VERIFIED: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    PENDING: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
    EXPIRED: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
  };

  const statusNames = {
    VERIFIED: '검증됨',
    PENDING: '대기중',
    EXPIRED: '만료됨'
  };

  return (
    <span className={`px-3 py-1 rounded-full text-xs font-semibold ${styles[status]}`}>
      {statusNames[status]}
    </span>
  );
}

// Reputation tier badge
function ReputationBadge({ tier }: { tier: HIPProfile['reputationTier'] }) {
  const styles = {
    Novice: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200',
    Emerging: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
    Established: 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200',
    Distinguished: 'bg-pink-100 text-pink-800 dark:bg-pink-900 dark:text-pink-200',
    Legendary: 'bg-gradient-to-r from-yellow-400 to-orange-500 text-white font-bold'
  };

  const tierNames = {
    Novice: '초보자',
    Emerging: '신흥',
    Established: '확립됨',
    Distinguished: '탁월함',
    Legendary: '전설'
  };

  return (
    <span className={`px-4 py-2 rounded-full text-sm font-semibold ${styles[tier]}`}>
      {tierNames[tier]}
    </span>
  );
}

// Level progress bar
function LevelProgress({ level }: { level: number }) {
  const progress = (level / 10) * 100;

  return (
    <div className="w-full">
      <div className="flex justify-between items-center mb-2">
        <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
          신원 레벨
        </span>
        <span className="text-2xl font-bold text-indigo-600 dark:text-indigo-400">
          {level} / 10
        </span>
      </div>
      <div className="w-full bg-gray-200 rounded-full h-3 dark:bg-gray-700">
        <div
          className="bg-gradient-to-r from-indigo-500 to-purple-600 h-3 rounded-full transition-all duration-500"
          style={{ width: `${progress}%` }}
        />
      </div>
    </div>
  );
}

export function IdentityCard({ profile, className = '' }: IdentityCardProps) {
  const lastUpdated = formatDistanceToNow(new Date(profile.lastUpdatedAt), { addSuffix: true });

  return (
    <div className={`bg-gradient-to-br from-indigo-50 to-purple-50 dark:from-gray-800 dark:to-gray-900 rounded-2xl shadow-xl p-8 ${className}`}>
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-3xl font-bold text-gray-900 dark:text-white mb-1">
            {profile.hipId}
          </h2>
          <p className="text-sm text-gray-600 dark:text-gray-400">
            사용자 ID: {profile.userId}
          </p>
        </div>
        <VerificationBadge status={profile.verificationStatus} />
      </div>

      {/* Overall Score */}
      <div className="text-center py-8 mb-6 bg-white dark:bg-gray-800 rounded-xl shadow-sm">
        <p className="text-sm text-gray-600 dark:text-gray-400 mb-2">
          전체 HIP 점수
        </p>
        <div className="text-6xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
          {profile.overallHipScore.toFixed(1)}
        </div>
        <p className="text-xs text-gray-500 dark:text-gray-500 mt-2">
          100점 만점
        </p>
      </div>

      {/* Level Progress */}
      <div className="mb-6">
        <LevelProgress level={profile.identityLevel} />
      </div>

      {/* Reputation Tier */}
      <div className="flex justify-center mb-6">
        <ReputationBadge tier={profile.reputationTier} />
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-2 gap-4 pt-6 border-t border-gray-200 dark:border-gray-700">
        <div className="text-center">
          <p className="text-2xl font-bold text-indigo-600 dark:text-indigo-400">
            {profile.totalInteractions}
          </p>
          <p className="text-xs text-gray-600 dark:text-gray-400">
            총 상호작용
          </p>
        </div>
        <div className="text-center">
          <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
            {lastUpdated}
          </p>
          <p className="text-xs text-gray-600 dark:text-gray-400">
            마지막 업데이트
          </p>
        </div>
      </div>

      {/* Created Date */}
      <div className="mt-4 text-center">
        <p className="text-xs text-gray-500 dark:text-gray-500">
          생성일: {new Date(profile.createdAt).toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
          })}
        </p>
      </div>
    </div>
  );
}
