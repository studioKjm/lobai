// HIP Score Radar Chart Component
import { Radar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer, Legend, Tooltip } from 'recharts';
import type { CoreScores } from '@/types/hip';

interface ScoreChartProps {
  coreScores: CoreScores;
  className?: string;
}

export function ScoreChart({ coreScores, className = '' }: ScoreChartProps) {
  // Transform scores into chart data
  const data = [
    {
      dimension: '인지적\n유연성',
      score: coreScores.cognitiveFlexibility,
      fullMark: 100
    },
    {
      dimension: '협업\n패턴',
      score: coreScores.collaborationPattern,
      fullMark: 100
    },
    {
      dimension: '정보\n처리',
      score: coreScores.informationProcessing,
      fullMark: 100
    },
    {
      dimension: '감정\n지능',
      score: coreScores.emotionalIntelligence,
      fullMark: 100
    },
    {
      dimension: '창의성',
      score: coreScores.creativity,
      fullMark: 100
    },
    {
      dimension: '윤리적\n정렬',
      score: coreScores.ethicalAlignment,
      fullMark: 100
    }
  ];

  return (
    <div className={`bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6 ${className}`}>
      <h3 className="text-xl font-bold mb-4 text-gray-900 dark:text-white">
        핵심 신원 점수
      </h3>

      <ResponsiveContainer width="100%" height={400}>
        <RadarChart data={data}>
          <PolarGrid stroke="#e5e7eb" />
          <PolarAngleAxis
            dataKey="dimension"
            tick={{ fill: '#6b7280', fontSize: 12 }}
            style={{ whiteSpace: 'pre-line', textAnchor: 'middle' }}
          />
          <PolarRadiusAxis
            angle={90}
            domain={[0, 100]}
            tick={{ fill: '#9ca3af' }}
          />
          <Radar
            name="내 점수"
            dataKey="score"
            stroke="#6366f1"
            fill="#6366f1"
            fillOpacity={0.6}
          />
          <Tooltip
            contentStyle={{
              backgroundColor: '#1f2937',
              border: 'none',
              borderRadius: '8px',
              color: '#fff'
            }}
          />
          <Legend />
        </RadarChart>
      </ResponsiveContainer>

      {/* Score Breakdown */}
      <div className="mt-6 grid grid-cols-2 gap-4">
        {Object.entries(coreScores).map(([key, value]) => {
          const labelMap: Record<string, string> = {
            cognitiveFlexibility: '인지적 유연성',
            collaborationPattern: '협업 패턴',
            informationProcessing: '정보 처리',
            emotionalIntelligence: '감정 지능',
            creativity: '창의성',
            ethicalAlignment: '윤리적 정렬'
          };

          return (
            <div key={key} className="flex items-center justify-between">
              <span className="text-sm text-gray-600 dark:text-gray-400">
                {labelMap[key] || key}
              </span>
              <span className="text-lg font-semibold text-indigo-600 dark:text-indigo-400">
                {value.toFixed(1)}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
