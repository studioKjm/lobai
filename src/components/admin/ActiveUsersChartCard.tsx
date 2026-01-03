import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import type { ActiveUsersChart } from '@/types';

interface ActiveUsersChartCardProps {
  data: ActiveUsersChart;
}

export function ActiveUsersChartCard({ data }: ActiveUsersChartCardProps) {
  // Combine both datasets for the chart
  const chartData = data.dailyActiveUsers.map((point, index) => ({
    date: point.date.slice(5), // Show only MM-DD
    activeUsers: point.count,
    newUsers: data.newUserSignups[index]?.count || 0,
  }));

  return (
    <div className="bg-white/10 backdrop-blur-md rounded-2xl p-6 border border-white/20">
      <h3 className="text-white text-lg font-semibold mb-4 flex items-center gap-2">
        <span>📈</span>
        활성 사용자 추이 (최근 30일)
      </h3>

      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
          <XAxis
            dataKey="date"
            stroke="rgba(255,255,255,0.5)"
            tick={{ fill: 'rgba(255,255,255,0.7)', fontSize: 12 }}
          />
          <YAxis
            stroke="rgba(255,255,255,0.5)"
            tick={{ fill: 'rgba(255,255,255,0.7)', fontSize: 12 }}
          />
          <Tooltip
            contentStyle={{
              backgroundColor: 'rgba(0,0,0,0.8)',
              border: '1px solid rgba(255,255,255,0.2)',
              borderRadius: '8px',
              color: '#fff',
            }}
          />
          <Legend
            wrapperStyle={{ color: '#fff' }}
            iconType="line"
          />
          <Line
            type="monotone"
            dataKey="activeUsers"
            stroke="#3b82f6"
            strokeWidth={2}
            name="일일 활성 사용자"
            dot={{ fill: '#3b82f6', r: 3 }}
            activeDot={{ r: 5 }}
          />
          <Line
            type="monotone"
            dataKey="newUsers"
            stroke="#10b981"
            strokeWidth={2}
            name="신규 가입자"
            dot={{ fill: '#10b981', r: 3 }}
            activeDot={{ r: 5 }}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
