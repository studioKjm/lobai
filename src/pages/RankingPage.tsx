import { useState, useEffect } from 'react';
import { hipApi } from '@/api/hipApi';
import type { RankingEntry } from '@/types/hip';
import toast from 'react-hot-toast';

export default function RankingPage() {
  const [rankings, setRankings] = useState<RankingEntry[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const limit = 20;

  useEffect(() => {
    loadRankings();
  }, [page]);

  const loadRankings = async () => {
    setIsLoading(true);
    try {
      const data = await hipApi.getRanking(limit, page * limit);
      setRankings(data.rankings);
      setTotal(data.total);
    } catch (error) {
      console.error('Failed to load rankings:', error);
      toast.error('Failed to load HIP rankings');
    } finally {
      setIsLoading(false);
    }
  };

  const getTierColor = (tier: string) => {
    switch (tier) {
      case 'Legendary': return 'text-yellow-500 bg-yellow-500/10 border-yellow-500/30';
      case 'Exemplary': return 'text-purple-500 bg-purple-500/10 border-purple-500/30';
      case 'Distinguished': return 'text-blue-500 bg-blue-500/10 border-blue-500/30';
      case 'Established': return 'text-green-500 bg-green-500/10 border-green-500/30';
      default: return 'text-gray-500 bg-gray-500/10 border-gray-500/30';
    }
  };

  const getRankBadge = (rank: number) => {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return rank;
  };

  if (isLoading && rankings.length === 0) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-16 w-16 border-4 border-indigo-600 border-t-transparent"></div>
          <p className="mt-4 text-gray-600 dark:text-gray-400">Loading rankings...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">
      <div className="container mx-auto px-4 py-12">
        {/* Header */}
        <div className="text-center mb-12">
          <h1 className="text-5xl font-bold bg-gradient-to-r from-yellow-500 via-purple-600 to-pink-600 bg-clip-text text-transparent mb-4">
            🏆 HIP Leaderboard
          </h1>
          <p className="text-xl text-gray-600 dark:text-gray-400">
            Top Human Identity Profiles ({total} total)
          </p>
        </div>

        {/* Rankings Table */}
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-xl overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gradient-to-r from-indigo-600 to-purple-600 text-white">
                <tr>
                  <th className="px-6 py-4 text-left text-sm font-semibold">Rank</th>
                  <th className="px-6 py-4 text-left text-sm font-semibold">HIP ID</th>
                  <th className="px-6 py-4 text-left text-sm font-semibold">Username</th>
                  <th className="px-6 py-4 text-center text-sm font-semibold">Score</th>
                  <th className="px-6 py-4 text-center text-sm font-semibold">Level</th>
                  <th className="px-6 py-4 text-center text-sm font-semibold">Tier</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                {rankings.map((entry) => (
                  <tr
                    key={entry.hipId}
                    className="hover:bg-gray-50 dark:hover:bg-gray-700 transition cursor-pointer"
                    onClick={() => window.location.href = `/hip/${entry.hipId}`}
                  >
                    <td className="px-6 py-4 text-2xl font-bold text-gray-900 dark:text-white">
                      {getRankBadge(entry.rank)}
                    </td>
                    <td className="px-6 py-4 font-mono text-sm text-indigo-600 dark:text-indigo-400">
                      {entry.hipId}
                    </td>
                    <td className="px-6 py-4 text-gray-900 dark:text-white">
                      {entry.username || '(Anonymous)'}
                    </td>
                    <td className="px-6 py-4 text-center">
                      <span className="text-2xl font-bold text-purple-600 dark:text-purple-400">
                        {entry.overallHipScore.toFixed(1)}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-center">
                      <span className="inline-flex items-center justify-center w-10 h-10 rounded-full bg-gradient-to-br from-indigo-500 to-purple-500 text-white font-bold">
                        {entry.identityLevel}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-center">
                      <span className={`inline-block px-3 py-1 rounded-full text-sm font-semibold border ${getTierColor(entry.reputationTier)}`}>
                        {entry.reputationTier}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Pagination */}
        <div className="mt-8 flex justify-center gap-4">
          <button
            onClick={() => setPage(Math.max(0, page - 1))}
            disabled={page === 0}
            className="px-6 py-3 bg-white dark:bg-gray-800 text-indigo-600 dark:text-indigo-400 border-2 border-indigo-600 dark:border-indigo-400 rounded-lg font-semibold disabled:opacity-50 disabled:cursor-not-allowed hover:bg-indigo-50 dark:hover:bg-gray-700 transition"
          >
            ← Previous
          </button>
          <span className="flex items-center px-4 text-gray-700 dark:text-gray-300 font-semibold">
            Page {page + 1} of {Math.ceil(total / limit)}
          </span>
          <button
            onClick={() => setPage(page + 1)}
            disabled={(page + 1) * limit >= total}
            className="px-6 py-3 bg-white dark:bg-gray-800 text-indigo-600 dark:text-indigo-400 border-2 border-indigo-600 dark:border-indigo-400 rounded-lg font-semibold disabled:opacity-50 disabled:cursor-not-allowed hover:bg-indigo-50 dark:hover:bg-gray-700 transition"
          >
            Next →
          </button>
        </div>

        {/* Back Button */}
        <div className="mt-8 text-center">
          <button
            onClick={() => window.location.href = '/dashboard'}
            className="px-6 py-3 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition"
          >
            ← Back to Dashboard
          </button>
        </div>
      </div>
    </div>
  );
}
