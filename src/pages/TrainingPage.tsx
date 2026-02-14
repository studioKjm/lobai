import React, { useState, useEffect } from 'react';
import { Navbar } from '@/components/common/Navbar';
import toast from 'react-hot-toast';
import {
  TrainingType,
  SessionStatus,
  TrainingSession,
  TrainingStatistics,
  startTraining,
  requestHint,
  submitAnswer,
  getTrainingHistory,
  getTrainingStatistics,
  getTrainingTypeName,
  getTrainingTypeDescription,
  getTrainingTypeIcon,
} from '@/lib/trainingApi';
import MemoryTrainingInterface from '@/components/training/MemoryTrainingInterface';

export const TrainingPage: React.FC = () => {
  // State
  const [currentSession, setCurrentSession] = useState<TrainingSession | null>(null);
  const [selectedType, setSelectedType] = useState<TrainingType | null>(null);
  const [difficultyLevel, setDifficultyLevel] = useState<number>(1);
  const [answer, setAnswer] = useState<string>('');
  const [startTime, setStartTime] = useState<number | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showResult, setShowResult] = useState(false);
  const [history, setHistory] = useState<TrainingSession[]>([]);
  const [statistics, setStatistics] = useState<TrainingStatistics[]>([]);
  const [isLoadingHint, setIsLoadingHint] = useState(false);
  const [hints, setHints] = useState<string[]>([]);

  // Load history and statistics
  useEffect(() => {
    loadHistory();
    loadStatistics();
  }, []);

  const loadHistory = async () => {
    try {
      const data = await getTrainingHistory();
      setHistory(data);
    } catch (error) {
      console.error('Failed to load history:', error);
    }
  };

  const loadStatistics = async () => {
    try {
      const data = await getTrainingStatistics();
      setStatistics(data);
    } catch (error) {
      console.error('Failed to load statistics:', error);
    }
  };

  // Start training
  const handleStart = async () => {
    if (!selectedType) {
      toast.error('훈련 유형을 선택하세요');
      return;
    }

    try {
      const session = await startTraining(selectedType, difficultyLevel);
      setCurrentSession(session);
      setStartTime(Date.now());
      setAnswer('');
      setHints([]);
      setShowResult(false);
      toast.success('훈련이 시작되었습니다!');
    } catch (error: any) {
      toast.error(error.response?.data?.message || '훈련 시작에 실패했습니다');
    }
  };

  // Request hint
  const handleHint = async () => {
    if (!currentSession) return;

    setIsLoadingHint(true);
    try {
      const { hint, cost } = await requestHint(currentSession.id);
      setHints([...hints, hint]);
      toast.success(`힌트를 받았습니다! (${cost} LobCoin 사용)`);
    } catch (error: any) {
      toast.error(error.response?.data?.message || '힌트를 받을 수 없습니다');
    } finally {
      setIsLoadingHint(false);
    }
  };

  // Submit answer
  const handleSubmit = async () => {
    if (!currentSession || !answer.trim()) {
      toast.error('답변을 입력하세요');
      return;
    }

    setIsSubmitting(true);
    try {
      const timeTaken = startTime ? Math.floor((Date.now() - startTime) / 1000) : 0;
      const result = await submitAnswer(currentSession.id, answer, timeTaken);
      setCurrentSession(result);
      setShowResult(true);
      toast.success('답변이 제출되었습니다!');

      // Reload history and statistics
      loadHistory();
      loadStatistics();
    } catch (error: any) {
      toast.error(error.response?.data?.message || '답변 제출에 실패했습니다');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Reset to selection
  const handleReset = () => {
    setCurrentSession(null);
    setSelectedType(null);
    setDifficultyLevel(1);
    setAnswer('');
    setHints([]);
    setShowResult(false);
  };

  return (
    <div className="relative w-full">
      <Navbar />

      <section className="relative min-h-screen w-full px-6 sm:px-12 pb-12 pt-36">
        {/* Background Decor */}
        <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-purple-900/10 rounded-full blur-[120px]" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-blue-900/10 rounded-full blur-[120px]" />

        <div className="relative w-full max-w-6xl mx-auto">
          <h1 className="text-4xl font-bold mb-4 text-center">
            🧠 AI 독립성 훈련
          </h1>
          <p className="text-center opacity-70 mb-12">
            Lobi와 함께 AI에 의존하지 않는 독립적 사고 능력을 키워보세요
          </p>

          {/* Main Content */}
          {!currentSession ? (
            // Selection Mode
            <div className="grid lg:grid-cols-2 gap-8">
              {/* Training Type Selection */}
              <div className="glass p-8">
                <h2 className="text-2xl font-semibold mb-6">훈련 유형 선택</h2>
                <div className="space-y-4">
                  {Object.values(TrainingType).map((type) => (
                    <button
                      key={type}
                      onClick={() => setSelectedType(type)}
                      className={`w-full p-4 rounded-lg text-left transition-all ${
                        selectedType === type
                          ? 'bg-purple-500/30 border-2 border-purple-400'
                          : 'bg-white/5 border-2 border-transparent hover:bg-white/10'
                      }`}
                    >
                      <div className="flex items-center gap-3 mb-2">
                        <span className="text-3xl">{getTrainingTypeIcon(type)}</span>
                        <span className="text-lg font-semibold">
                          {getTrainingTypeName(type)}
                        </span>
                      </div>
                      <p className="text-sm opacity-70 ml-12">
                        {getTrainingTypeDescription(type)}
                      </p>
                    </button>
                  ))}
                </div>

                {/* Difficulty Selection */}
                {selectedType && (
                  <div className="mt-8">
                    <h3 className="text-lg font-semibold mb-4">난이도 선택</h3>
                    <div className="flex items-center gap-4">
                      <input
                        type="range"
                        min="1"
                        max="10"
                        value={difficultyLevel}
                        onChange={(e) => setDifficultyLevel(Number(e.target.value))}
                        className="flex-1 h-2 bg-white/10 rounded-lg appearance-none cursor-pointer accent-purple-400"
                      />
                      <span className="text-2xl font-bold text-purple-400 w-12 text-center">
                        {difficultyLevel}
                      </span>
                    </div>
                    <p className="text-sm opacity-70 mt-2">
                      {difficultyLevel <= 3 && '초급 - 기본적인 사고 능력'}
                      {difficultyLevel > 3 && difficultyLevel <= 6 && '중급 - 논리적 사고 필요'}
                      {difficultyLevel > 6 && difficultyLevel <= 8 && '고급 - 심화 분석 필요'}
                      {difficultyLevel > 8 && '최고급 - 창의적 해결책 필요'}
                    </p>

                    <button
                      onClick={handleStart}
                      className="w-full mt-6 py-3 rounded-lg bg-purple-500/30 hover:bg-purple-500/40 text-purple-300 font-semibold transition-colors"
                    >
                      훈련 시작하기
                    </button>
                  </div>
                )}
              </div>

              {/* Statistics */}
              <div className="glass p-8">
                <h2 className="text-2xl font-semibold mb-6">훈련 통계</h2>
                {statistics.length > 0 ? (
                  <div className="space-y-4">
                    {statistics.map((stat) => (
                      <div key={stat.trainingType} className="p-4 bg-white/5 rounded-lg">
                        <div className="flex items-center gap-2 mb-2">
                          <span className="text-2xl">{getTrainingTypeIcon(stat.trainingType)}</span>
                          <span className="font-semibold">{getTrainingTypeName(stat.trainingType)}</span>
                        </div>
                        <div className="grid grid-cols-2 gap-2 text-sm">
                          <div>
                            <span className="opacity-70">완료: </span>
                            <span className="font-semibold">{stat.completedSessions}</span>
                          </div>
                          <div>
                            <span className="opacity-70">평균: </span>
                            <span className="font-semibold">{stat.avgScore.toFixed(1)}점</span>
                          </div>
                          <div>
                            <span className="opacity-70">최고: </span>
                            <span className="font-semibold text-yellow-400">{stat.bestScore}점</span>
                          </div>
                          <div>
                            <span className="opacity-70">획득: </span>
                            <span className="font-semibold text-purple-400">{stat.totalLobcoinEarned}💰</span>
                          </div>
                        </div>
                        {stat.currentStreak > 0 && (
                          <div className="mt-2 text-sm">
                            <span className="opacity-70">연속 훈련: </span>
                            <span className="font-semibold text-green-400">{stat.currentStreak}일 🔥</span>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-center opacity-50 py-8">아직 훈련 기록이 없습니다</p>
                )}
              </div>
            </div>
          ) : showResult ? (
            // Result Mode
            <div className="glass p-8 max-w-3xl mx-auto">
              <div className="text-center mb-8">
                <div className="text-6xl mb-4">
                  {currentSession.score && currentSession.score >= 80 ? '🎉' : currentSession.score && currentSession.score >= 60 ? '😊' : '💪'}
                </div>
                <h2 className="text-3xl font-bold mb-2">훈련 완료!</h2>
                <div className="text-5xl font-bold text-purple-400 mb-4">
                  {currentSession.score}점
                </div>
                <div className="flex items-center justify-center gap-4 text-sm">
                  <span className="opacity-70">획득 LobCoin:</span>
                  <span className="text-2xl font-semibold text-yellow-400">
                    +{currentSession.lobcoinEarned} 💰
                  </span>
                </div>
              </div>

              {/* Evaluation */}
              {currentSession.evaluation && (
                <div className="mb-6 p-4 bg-white/5 rounded-lg">
                  <h3 className="text-lg font-semibold mb-2">평가</h3>
                  <p className="opacity-80">{currentSession.evaluation}</p>
                </div>
              )}

              {/* Feedback */}
              {currentSession.feedback && (
                <div className="mb-6 p-4 bg-purple-500/10 rounded-lg border border-purple-500/30">
                  <h3 className="text-lg font-semibold mb-2 text-purple-300">피드백</h3>
                  <p className="opacity-80 whitespace-pre-wrap">{currentSession.feedback}</p>
                </div>
              )}

              {/* Correct Answer (문제 해결 유형만) */}
              {currentSession.correctAnswer && (
                <div className="mb-6 p-4 bg-green-500/10 rounded-lg border border-green-500/30">
                  <h3 className="text-lg font-semibold mb-2 text-green-300">💡 정답</h3>
                  <p className="opacity-80 whitespace-pre-wrap">{currentSession.correctAnswer}</p>
                </div>
              )}

              {/* Your Answer */}
              <div className="mb-6 p-4 bg-white/5 rounded-lg">
                <h3 className="text-lg font-semibold mb-2">제출한 답변</h3>
                <p className="opacity-80 whitespace-pre-wrap">{currentSession.userAnswer}</p>
              </div>

              <button
                onClick={handleReset}
                className="w-full py-3 rounded-lg bg-purple-500/30 hover:bg-purple-500/40 text-purple-300 font-semibold transition-colors"
              >
                다시 훈련하기
              </button>
            </div>
          ) : currentSession.trainingType === TrainingType.MEMORY ? (
            // Memory Training Mode (Interactive)
            <div className="glass p-8 max-w-4xl mx-auto">
              <div className="mb-6 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <span className="text-3xl">{getTrainingTypeIcon(currentSession.trainingType)}</span>
                  <div>
                    <h2 className="text-xl font-semibold">{getTrainingTypeName(currentSession.trainingType)}</h2>
                    <p className="text-sm opacity-70">난이도: {currentSession.difficultyLevel}/10</p>
                  </div>
                </div>
                <button
                  onClick={handleReset}
                  className="text-sm px-4 py-2 rounded bg-white/5 hover:bg-white/10 transition-colors"
                >
                  포기하기
                </button>
              </div>
              <MemoryTrainingInterface
                session={currentSession}
                onComplete={() => {
                  loadHistory();
                  loadStatistics();
                  // Reload session to get results
                  submitAnswer(currentSession.id, '', 0)
                    .then((result) => {
                      setCurrentSession(result);
                      setShowResult(true);
                      toast.success('훈련이 완료되었습니다!');
                    })
                    .catch(() => {
                      // If submission fails, just show result anyway
                      setShowResult(true);
                    });
                }}
              />
            </div>
          ) : (
            // Standard Training Mode
            <div className="glass p-8 max-w-3xl mx-auto">
              <div className="mb-6">
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <span className="text-3xl">{getTrainingTypeIcon(currentSession.trainingType)}</span>
                    <div>
                      <h2 className="text-xl font-semibold">{getTrainingTypeName(currentSession.trainingType)}</h2>
                      <p className="text-sm opacity-70">난이도: {currentSession.difficultyLevel}/10</p>
                    </div>
                  </div>
                  <div className="text-right text-sm">
                    <div className="opacity-70">힌트 사용:</div>
                    <div className="font-semibold">{currentSession.hintsUsed}회</div>
                  </div>
                </div>
              </div>

              {/* Problem */}
              <div className="mb-6 p-6 bg-white/5 rounded-lg border-2 border-purple-500/30">
                <h3 className="text-lg font-semibold mb-3 text-purple-300">문제</h3>
                <p className="text-lg leading-relaxed whitespace-pre-wrap">
                  {currentSession.problemText}
                </p>
              </div>

              {/* Hints */}
              {hints.length > 0 && (
                <div className="mb-6 space-y-2">
                  {hints.map((hint, index) => (
                    <div key={index} className="p-4 bg-yellow-500/10 rounded-lg border border-yellow-500/30">
                      <div className="flex items-start gap-2">
                        <span className="text-yellow-400">💡</span>
                        <div>
                          <div className="text-sm font-semibold text-yellow-300 mb-1">
                            힌트 {index + 1}
                          </div>
                          <p className="text-sm opacity-80">{hint}</p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* Answer Input */}
              <div className="mb-6">
                <div className="flex items-center justify-between mb-2">
                  <label className="text-lg font-semibold">답변</label>
                  <button
                    onClick={handleHint}
                    disabled={isLoadingHint}
                    className="text-sm px-4 py-1 rounded bg-yellow-500/20 hover:bg-yellow-500/30 text-yellow-300 transition-colors disabled:opacity-50"
                  >
                    {isLoadingHint ? '로딩 중...' : '💡 힌트 받기 (5 LobCoin)'}
                  </button>
                </div>
                <textarea
                  value={answer}
                  onChange={(e) => setAnswer(e.target.value)}
                  placeholder="답변을 입력하세요... (최소 20자 이상 권장)"
                  className="w-full h-48 p-4 bg-white/5 border border-white/10 rounded-lg text-white placeholder-white/30 focus:outline-none focus:ring-2 focus:ring-purple-500/50 resize-none"
                />
                <p className="text-sm opacity-50 mt-2">
                  {answer.length}자 입력됨
                </p>
              </div>

              {/* Submit Button */}
              <div className="flex gap-4">
                <button
                  onClick={handleReset}
                  className="flex-1 py-3 rounded-lg bg-white/5 hover:bg-white/10 transition-colors"
                >
                  포기하기
                </button>
                <button
                  onClick={handleSubmit}
                  disabled={isSubmitting || !answer.trim()}
                  className="flex-1 py-3 rounded-lg bg-purple-500/30 hover:bg-purple-500/40 text-purple-300 font-semibold transition-colors disabled:opacity-50"
                >
                  {isSubmitting ? '제출 중...' : '답변 제출'}
                </button>
              </div>
            </div>
          )}

          {/* Recent History */}
          {history.length > 0 && !currentSession && (
            <div className="mt-12 glass p-8">
              <h2 className="text-2xl font-semibold mb-6">최근 훈련 기록</h2>
              <div className="space-y-3">
                {history.slice(0, 5).map((session) => (
                  <div key={session.id} className="p-4 bg-white/5 rounded-lg flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <span className="text-2xl">{getTrainingTypeIcon(session.trainingType)}</span>
                      <div>
                        <div className="font-semibold">{getTrainingTypeName(session.trainingType)}</div>
                        <div className="text-sm opacity-70">
                          난이도 {session.difficultyLevel} | {new Date(session.createdAt).toLocaleDateString()}
                        </div>
                      </div>
                    </div>
                    <div className="text-right">
                      {session.status === SessionStatus.COMPLETED ? (
                        <>
                          <div className="text-2xl font-bold text-purple-400">{session.score}점</div>
                          <div className="text-sm text-yellow-400">+{session.lobcoinEarned}💰</div>
                        </>
                      ) : (
                        <div className="text-sm opacity-50">중단됨</div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </section>
    </div>
  );
};
