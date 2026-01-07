import type { StatsOverview } from '@/types';

interface StatsOverviewCardProps {
  data: StatsOverview;
}

export function StatsOverviewCard({ data }: StatsOverviewCardProps) {
  const statsCards = [
    {
      title: '총 사용자',
      value: data.totalUsers.toLocaleString(),
      subtitle: `오늘 활성: ${data.activeUsersToday}명`,
      icon: '👥',
      color: 'from-blue-500 to-cyan-500',
    },
    {
      title: '신규 사용자',
      value: data.newUsersThisMonth.toLocaleString(),
      subtitle: `이번 주: ${data.newUsersThisWeek}명`,
      icon: '✨',
      color: 'from-green-500 to-emerald-500',
    },
    {
      title: '총 메시지',
      value: data.totalMessages.toLocaleString(),
      subtitle: `오늘: ${data.messagesToday}개`,
      icon: '💬',
      color: 'from-purple-500 to-pink-500',
    },
    {
      title: '평균 메시지/사용자',
      value: data.avgMessagesPerUser.toFixed(1),
      subtitle: `이번 달: ${data.messagesThisMonth}개`,
      icon: '📊',
      color: 'from-orange-500 to-red-500',
    },
  ];

  const robotStats = [
    {
      label: '평균 포만감',
      value: data.avgHunger.toFixed(1),
      color: 'text-yellow-400',
    },
    {
      label: '평균 에너지',
      value: data.avgEnergy.toFixed(1),
      color: 'text-green-400',
    },
    {
      label: '평균 행복도',
      value: data.avgHappiness.toFixed(1),
      color: 'text-pink-400',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Main Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {statsCards.map((stat, index) => (
          <div
            key={index}
            className="bg-white/10 backdrop-blur-md rounded-2xl p-6 border border-white/20 hover:border-white/40 transition-all"
          >
            <div className="flex items-center justify-between mb-3">
              <div className={`text-4xl bg-gradient-to-br ${stat.color} p-3 rounded-xl`}>
                {stat.icon}
              </div>
            </div>
            <div>
              <p className="text-gray-300 text-sm mb-1">{stat.title}</p>
              <p className="text-white text-3xl font-bold mb-1">{stat.value}</p>
              <p className="text-gray-400 text-xs">{stat.subtitle}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Robot Stats & Popular Persona */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {/* Robot Average Stats */}
        <div className="bg-white/10 backdrop-blur-md rounded-2xl p-6 border border-white/20">
          <h3 className="text-white text-lg font-semibold mb-4 flex items-center gap-2">
            <span>🤖</span>
            로봇 평균 스탯
          </h3>
          <div className="grid grid-cols-3 gap-4">
            {robotStats.map((stat, index) => (
              <div key={index} className="text-center">
                <p className="text-gray-400 text-sm mb-1">{stat.label}</p>
                <p className={`text-2xl font-bold ${stat.color}`}>{stat.value}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Most Popular Persona */}
        <div className="bg-white/10 backdrop-blur-md rounded-2xl p-6 border border-white/20">
          <h3 className="text-white text-lg font-semibold mb-4 flex items-center gap-2">
            <span>🏆</span>
            가장 인기있는 페르소나
          </h3>
          <div className="text-center">
            <p className="text-white text-3xl font-bold mb-2">
              {data.mostPopularPersona}
            </p>
            <p className="text-gray-400">
              총 {data.mostPopularPersonaCount.toLocaleString()}개의 메시지
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
