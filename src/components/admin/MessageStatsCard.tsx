import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import type { MessageStats } from '@/types';

interface MessageStatsCardProps {
  data: MessageStats;
}

export function MessageStatsCard({ data }: MessageStatsCardProps) {
  // Prepare chart data
  const chartData = data.dailyMessages.map((point) => ({
    date: point.date.slice(5), // Show only MM-DD
    사용자: point.userMessages,
    봇: point.botMessages,
  }));

  return (
    <div className="bg-white/10 backdrop-blur-md rounded-2xl p-6 border border-white/20">
      <h3 className="text-white text-lg font-semibold mb-4 flex items-center gap-2">
        <span>💬</span>
        메시지 통계 (최근 30일)
      </h3>

      {/* Stats Summary */}
      <div className="grid grid-cols-2 gap-4 mb-6">
        <div className="text-center">
          <p className="text-gray-400 text-sm">일평균 메시지</p>
          <p className="text-white text-2xl font-bold">{data.avgMessagesPerDay.toFixed(1)}</p>
        </div>
        <div className="text-center">
          <p className="text-gray-400 text-sm">사용자당 평균</p>
          <p className="text-white text-2xl font-bold">{data.avgMessagesPerUser.toFixed(1)}</p>
        </div>
      </div>

      {/* Chart */}
      <ResponsiveContainer width="100%" height={250}>
        <BarChart data={chartData}>
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
          />
          <Bar dataKey="사용자" fill="#8b5cf6" />
          <Bar dataKey="봇" fill="#ec4899" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
