import React, { useState, useEffect } from 'react';
import { TrainingSession, submitAnswer } from '@/lib/trainingApi';
import { getErrorMessage } from '@/lib/api';

interface MemoryProblemData {
  type: 'number_sequence' | 'word_list' | 'color_pattern' | 'mixed_sequence';
  data: (string | number)[];
  display_time_per_item?: number; // 각 아이템 표시 시간 (ms)
  display_time_total?: number; // 전체 표시 시간 (ms)
  instruction: string;
}

interface MemoryTrainingInterfaceProps {
  session: TrainingSession;
  onComplete: () => void;
}

type Phase = 'instruction' | 'memorize' | 'wait' | 'answer' | 'submitting';

const MemoryTrainingInterface: React.FC<MemoryTrainingInterfaceProps> = ({
  session,
  onComplete,
}) => {
  const [phase, setPhase] = useState<Phase>('instruction');
  const [problemData, setProblemData] = useState<MemoryProblemData | null>(null);
  const [currentItemIndex, setCurrentItemIndex] = useState(0);
  const [userAnswer, setUserAnswer] = useState('');
  const [startTime] = useState(Date.now());
  const [error, setError] = useState('');

  // Parse problem data on mount
  useEffect(() => {
    try {
      const parsed = JSON.parse(session.problemText) as MemoryProblemData;
      setProblemData(parsed);
    } catch (err) {
      setError('문제 데이터를 불러올 수 없습니다.');
    }
  }, [session.problemText]);

  // Memorize phase: show items one by one
  useEffect(() => {
    if (phase !== 'memorize' || !problemData) return;

    const displayTime =
      problemData.display_time_per_item || problemData.display_time_total || 2000;
    const totalItems = problemData.data.length;

    if (currentItemIndex < totalItems) {
      const timer = setTimeout(() => {
        setCurrentItemIndex((prev) => prev + 1);
      }, displayTime);

      return () => clearTimeout(timer);
    } else {
      // All items shown, move to wait phase
      setPhase('wait');
      const waitTimer = setTimeout(() => {
        setPhase('answer');
      }, 1000);

      return () => clearTimeout(waitTimer);
    }
  }, [phase, currentItemIndex, problemData]);

  const startMemorization = () => {
    setPhase('memorize');
    setCurrentItemIndex(0);
  };

  const handleSubmit = async () => {
    if (!userAnswer.trim()) {
      setError('답변을 입력해주세요.');
      return;
    }

    setPhase('submitting');
    setError('');

    try {
      const timeTaken = Math.floor((Date.now() - startTime) / 1000);
      await submitAnswer(session.id, userAnswer.trim(), timeTaken);
      onComplete();
    } catch (err) {
      setError(getErrorMessage(err));
      setPhase('answer');
    }
  };

  if (!problemData) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="text-red-400">{error || '문제를 불러오는 중...'}</div>
        </div>
      </div>
    );
  }

  // Get item style based on type
  const getItemClassName = (type: string) => {
    const baseClass = 'text-6xl font-bold transition-all duration-500 transform';
    switch (type) {
      case 'number_sequence':
        return `${baseClass} text-blue-400`;
      case 'word_list':
        return `${baseClass} text-green-400`;
      case 'color_pattern':
        return `${baseClass} text-purple-400`;
      case 'mixed_sequence':
        return `${baseClass} text-yellow-400`;
      default:
        return baseClass;
    }
  };

  const getColorStyle = (colorName: string) => {
    const colorMap: Record<string, string> = {
      빨강: 'text-red-500',
      파랑: 'text-blue-500',
      노랑: 'text-yellow-500',
      초록: 'text-green-500',
      보라: 'text-purple-500',
      주황: 'text-orange-500',
      분홍: 'text-pink-500',
    };
    return colorMap[colorName] || 'text-white';
  };

  return (
    <div className="max-w-4xl mx-auto">
      {/* Instruction Phase */}
      {phase === 'instruction' && (
        <div className="text-center space-y-6 animate-fade-in">
          <div className="text-4xl mb-4">🧠</div>
          <h2 className="text-2xl font-bold mb-4">암기력 훈련</h2>
          <p className="text-xl mb-6 opacity-80">{problemData.instruction}</p>
          <div className="bg-purple-500/20 rounded-lg p-6 mb-6">
            <p className="text-lg mb-2">훈련 방법:</p>
            <ol className="text-left max-w-md mx-auto space-y-2 opacity-80">
              <li>1. 화면에 나타나는 정보를 집중해서 기억하세요</li>
              <li>2. 정보가 사라지면 기억한 내용을 입력하세요</li>
              <li>3. 순서와 내용이 정확해야 합니다</li>
            </ol>
          </div>
          <button
            onClick={startMemorization}
            className="px-8 py-4 bg-purple-600 hover:bg-purple-700 rounded-lg font-semibold text-lg transition-all transform hover:scale-105"
          >
            시작하기
          </button>
        </div>
      )}

      {/* Memorize Phase */}
      {phase === 'memorize' && (
        <div className="flex flex-col items-center justify-center min-h-[500px]">
          <div className="mb-8 text-sm opacity-60">
            {currentItemIndex + 1} / {problemData.data.length}
          </div>
          <div className="flex items-center justify-center h-64">
            {currentItemIndex < problemData.data.length && (
              <div
                className={`${getItemClassName(problemData.type)} ${
                  problemData.type === 'color_pattern'
                    ? getColorStyle(String(problemData.data[currentItemIndex]))
                    : ''
                } animate-scale-in`}
                key={currentItemIndex}
              >
                {problemData.data[currentItemIndex]}
              </div>
            )}
          </div>
          <div className="mt-8 text-sm opacity-40">집중하세요...</div>
        </div>
      )}

      {/* Wait Phase */}
      {phase === 'wait' && (
        <div className="flex flex-col items-center justify-center min-h-[500px] animate-fade-in">
          <div className="text-6xl mb-4 animate-bounce">⏳</div>
          <div className="text-xl opacity-60">기억하고 있나요?</div>
        </div>
      )}

      {/* Answer Phase */}
      {phase === 'answer' && (
        <div className="space-y-6 animate-fade-in">
          <div className="text-center mb-8">
            <h3 className="text-2xl font-bold mb-2">답변 입력</h3>
            <p className="opacity-60">
              기억한 내용을 순서대로 입력하세요 (쉼표로 구분)
            </p>
          </div>

          <div className="bg-gray-800/50 rounded-lg p-6">
            <textarea
              value={userAnswer}
              onChange={(e) => setUserAnswer(e.target.value)}
              placeholder="예: 7,3,9,1,5 또는 사과,바나나,오렌지"
              className="w-full h-32 px-4 py-3 bg-gray-900/50 border border-gray-700 rounded-lg focus:outline-none focus:border-purple-500 resize-none"
              autoFocus
            />
          </div>

          {error && (
            <div className="text-red-400 text-center animate-shake">{error}</div>
          )}

          <div className="flex justify-center gap-4">
            <button
              onClick={handleSubmit}
              disabled={phase === 'submitting'}
              className="px-8 py-3 bg-purple-600 hover:bg-purple-700 disabled:bg-gray-600 disabled:cursor-not-allowed rounded-lg font-semibold transition-all transform hover:scale-105"
            >
              {phase === 'submitting' ? '제출 중...' : '답변 제출'}
            </button>
          </div>
        </div>
      )}

      {/* Submitting Phase */}
      {phase === 'submitting' && (
        <div className="flex flex-col items-center justify-center min-h-[400px]">
          <div className="text-4xl mb-4 animate-spin">⏳</div>
          <div className="text-xl opacity-60">답변을 평가하는 중...</div>
        </div>
      )}

      <style>{`
        @keyframes fade-in {
          from {
            opacity: 0;
            transform: translateY(10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }

        @keyframes scale-in {
          0% {
            opacity: 0;
            transform: scale(0.5);
          }
          50% {
            transform: scale(1.1);
          }
          100% {
            opacity: 1;
            transform: scale(1);
          }
        }

        @keyframes shake {
          0%, 100% {
            transform: translateX(0);
          }
          25% {
            transform: translateX(-10px);
          }
          75% {
            transform: translateX(10px);
          }
        }

        .animate-fade-in {
          animation: fade-in 0.5s ease-out;
        }

        .animate-scale-in {
          animation: scale-in 0.6s ease-out;
        }

        .animate-shake {
          animation: shake 0.5s ease-in-out;
        }
      `}</style>
    </div>
  );
};

export default MemoryTrainingInterface;
