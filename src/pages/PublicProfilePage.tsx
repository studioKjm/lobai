import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { hipApi } from '@/api/hipApi';
import { ScoreChart, IdentityCard } from '@/components/hip';
import type { HIPProfile } from '@/types/hip';
import toast from 'react-hot-toast';

export default function PublicProfilePage() {
  const { hipId } = useParams<{ hipId: string }>();
  const navigate = useNavigate();
  const [profile, setProfile] = useState<HIPProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    if (hipId) {
      loadProfile(hipId);
    }
  }, [hipId]);

  const loadProfile = async (id: string) => {
    setIsLoading(true);
    setNotFound(false);
    try {
      const data = await hipApi.getProfileByHipId(id);
      setProfile(data);
    } catch (error: any) {
      console.error('Failed to load HIP profile:', error);
      if (error.response?.status === 404) {
        setNotFound(true);
      } else {
        toast.error('Failed to load HIP profile');
      }
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-16 w-16 border-4 border-indigo-600 border-t-transparent"></div>
          <p className="mt-4 text-gray-600 dark:text-gray-400">Loading HIP profile...</p>
        </div>
      </div>
    );
  }

  if (notFound || !profile) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 flex items-center justify-center">
        <div className="text-center">
          <div className="text-8xl mb-6">🔍</div>
          <h2 className="text-3xl font-bold text-gray-900 dark:text-white mb-4">
            HIP Profile Not Found
          </h2>
          <p className="text-gray-600 dark:text-gray-400 mb-8">
            The HIP ID "{hipId}" does not exist or is private.
          </p>
          <div className="flex gap-4 justify-center">
            <button
              onClick={() => navigate('/dashboard')}
              className="px-6 py-3 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
            >
              Go to Dashboard
            </button>
            <button
              onClick={() => navigate('/dashboard/ranking')}
              className="px-6 py-3 bg-white dark:bg-gray-800 text-indigo-600 dark:text-indigo-400 border-2 border-indigo-600 dark:border-indigo-400 rounded-lg hover:bg-indigo-50 dark:hover:bg-gray-700 transition"
            >
              View Rankings
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">
      <div className="container mx-auto px-4 py-12">
        {/* Header */}
        <div className="text-center mb-12">
          <div className="inline-block px-4 py-2 bg-indigo-100 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400 rounded-full text-sm font-semibold mb-4">
            Public Profile
          </div>
          <h1 className="text-5xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent mb-4">
            Human Identity Protocol
          </h1>
          <p className="text-xl text-gray-600 dark:text-gray-400">
            Verified AI-evaluated identity
          </p>
        </div>

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Identity Card */}
          <IdentityCard profile={profile} />

          {/* Score Chart */}
          <ScoreChart coreScores={profile.coreScores} />
        </div>

        {/* Verification Badge */}
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-8 mb-8">
          <div className="flex items-center justify-center gap-4">
            <div className="text-5xl">
              {profile.verificationStatus === 'VERIFIED' ? '✅' : '⏳'}
            </div>
            <div>
              <h3 className="text-xl font-bold text-gray-900 dark:text-white">
                {profile.verificationStatus === 'VERIFIED' ? 'Verified Profile' : 'Pending Verification'}
              </h3>
              <p className="text-gray-600 dark:text-gray-400">
                {profile.verificationStatus === 'VERIFIED'
                  ? 'This HIP profile has been cryptographically verified and is authentic.'
                  : 'This profile is awaiting verification.'}
              </p>
            </div>
          </div>
        </div>

        {/* Info Section */}
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-8">
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">
            About This Profile
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="text-center p-4 bg-indigo-50 dark:bg-indigo-900/20 rounded-lg">
              <div className="text-3xl font-bold text-indigo-600 dark:text-indigo-400 mb-2">
                {profile.totalInteractions}
              </div>
              <div className="text-sm text-gray-600 dark:text-gray-400">
                Total Interactions
              </div>
            </div>
            <div className="text-center p-4 bg-purple-50 dark:bg-purple-900/20 rounded-lg">
              <div className="text-3xl font-bold text-purple-600 dark:text-purple-400 mb-2">
                Level {profile.identityLevel}
              </div>
              <div className="text-sm text-gray-600 dark:text-gray-400">
                Identity Level
              </div>
            </div>
            <div className="text-center p-4 bg-pink-50 dark:bg-pink-900/20 rounded-lg">
              <div className="text-3xl font-bold text-pink-600 dark:text-pink-400 mb-2">
                {profile.reputationTier}
              </div>
              <div className="text-sm text-gray-600 dark:text-gray-400">
                Reputation Tier
              </div>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="mt-8 flex flex-col sm:flex-row gap-4 justify-center">
          <button
            onClick={() => navigate('/dashboard/ranking')}
            className="px-8 py-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-xl font-semibold text-lg shadow-lg hover:shadow-xl transition-all hover:scale-105"
          >
            🏆 View Full Rankings
          </button>
          <button
            onClick={() => navigate('/dashboard')}
            className="px-8 py-4 bg-white dark:bg-gray-800 text-indigo-600 dark:text-indigo-400 border-2 border-indigo-600 dark:border-indigo-400 rounded-xl font-semibold text-lg shadow-lg hover:shadow-xl transition-all hover:scale-105"
          >
            📊 View My Dashboard
          </button>
        </div>
      </div>
    </div>
  );
}
