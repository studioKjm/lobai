import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Navbar } from '@/components/common/Navbar';
import { Footer } from '@/components/common/Footer';
import { StatsOverviewCard } from '@/components/admin/StatsOverviewCard';
import { ActiveUsersChartCard } from '@/components/admin/ActiveUsersChartCard';
import { MessageStatsCard } from '@/components/admin/MessageStatsCard';
import { PersonaStatsCard } from '@/components/admin/PersonaStatsCard';
import { getErrorMessage } from '@/lib/api';
import {
  getStatsOverview,
  getActiveUsersChart,
  getMessageStats,
  getPersonaStats,
} from '@/services/adminApi';
import type {
  StatsOverview,
  ActiveUsersChart,
  MessageStats,
  PersonaStats,
} from '@/types';

export function AdminPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [overview, setOverview] = useState<StatsOverview | null>(null);
  const [activeUsers, setActiveUsers] = useState<ActiveUsersChart | null>(null);
  const [messageStats, setMessageStats] = useState<MessageStats | null>(null);
  const [personaStats, setPersonaStats] = useState<PersonaStats | null>(null);

  useEffect(() => {
    const fetchAllStats = async () => {
      try {
        setLoading(true);

        // Fetch all stats in parallel
        const [overviewData, activeUsersData, messageStatsData, personaStatsData] =
          await Promise.all([
            getStatsOverview(),
            getActiveUsersChart(),
            getMessageStats(),
            getPersonaStats(),
          ]);

        setOverview(overviewData);
        setActiveUsers(activeUsersData);
        setMessageStats(messageStatsData);
        setPersonaStats(personaStatsData);
      } catch (error: any) {
        const errorMessage = getErrorMessage(error);
        toast.error(errorMessage);

        // If 403 Forbidden, show message and redirect
        if (error?.response?.status === 403) {
          toast.error('관리자 권한이 필요합니다. 다시 로그인해주세요.');
          setTimeout(() => navigate('/'), 2000);
        }
      } finally {
        setLoading(false);
      }
    };

    fetchAllStats();
  }, [navigate]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-purple-900 to-violet-800">
        <div className="text-white text-xl">통계 데이터 로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-purple-900 to-violet-800">
      <Navbar />

      <main className="container mx-auto px-4 py-8 mt-20">
        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-white mb-2">관리자 대시보드</h1>
          <p className="text-gray-300">LobAI 플랫폼 통계 및 사용자 분석</p>
        </div>

        {/* Stats Grid */}
        <div className="space-y-6">
          {/* Overview Stats */}
          {overview && <StatsOverviewCard data={overview} />}

          {/* Charts Row */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {activeUsers && <ActiveUsersChartCard data={activeUsers} />}
            {messageStats && <MessageStatsCard data={messageStats} />}
          </div>

          {/* Persona Stats */}
          {personaStats && <PersonaStatsCard data={personaStats} />}
        </div>
      </main>

      <Footer />
    </div>
  );
}
