import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';
import type { PersonaStats } from '@/types';

interface PersonaStatsCardProps {
  data: PersonaStats;
}

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];

export function PersonaStatsCard({ data }: PersonaStatsCardProps) {
  // Prepare chart data
  const chartData = data.personaUsage.map((persona) => ({
    name: persona.displayName,
    value: persona.messageCount,
  }));

  return (
    <div className="bg-white/10 backdrop-blur-md rounded-2xl p-6 border border-white/20">
      <h3 className="text-white text-lg font-semibold mb-6 flex items-center gap-2">
        <span>🎭</span>
        페르소나별 사용 통계
      </h3>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Pie Chart */}
        <div>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={chartData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {chartData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{
                  backgroundColor: 'rgba(0,0,0,0.8)',
                  border: '1px solid rgba(255,255,255,0.2)',
                  borderRadius: '8px',
                  color: '#fff',
                }}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>

        {/* Table */}
        <div className="space-y-3">
          {data.personaUsage.map((persona, index) => (
            <div
              key={persona.personaId}
              className="bg-white/5 rounded-lg p-4 border border-white/10 hover:border-white/20 transition-all"
            >
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-3">
                  <span className="text-2xl">{persona.iconEmoji}</span>
                  <div>
                    <p className="text-white font-semibold">{persona.displayName}</p>
                    <p className="text-gray-400 text-xs">{persona.personaName}</p>
                  </div>
                </div>
                <div
                  className="w-3 h-3 rounded-full"
                  style={{ backgroundColor: COLORS[index % COLORS.length] }}
                />
              </div>

              <div className="grid grid-cols-3 gap-2 text-sm">
                <div>
                  <p className="text-gray-400 text-xs">메시지</p>
                  <p className="text-white font-semibold">
                    {persona.messageCount.toLocaleString()}
                  </p>
                </div>
                <div>
                  <p className="text-gray-400 text-xs">사용자</p>
                  <p className="text-white font-semibold">
                    {persona.currentUserCount.toLocaleString()}
                  </p>
                </div>
                <div>
                  <p className="text-gray-400 text-xs">비율</p>
                  <p className="text-white font-semibold">
                    {persona.usagePercentage.toFixed(1)}%
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
