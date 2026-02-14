import api from './api';

export enum TrainingType {
  CRITICAL_THINKING = 'CRITICAL_THINKING',
  PROBLEM_SOLVING = 'PROBLEM_SOLVING',
  CREATIVE_THINKING = 'CREATIVE_THINKING',
  SELF_REFLECTION = 'SELF_REFLECTION',
  REASONING = 'REASONING',
  MEMORY = 'MEMORY',
}

export enum SessionStatus {
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  ABANDONED = 'ABANDONED',
}

export interface TrainingSession {
  id: number;
  trainingType: TrainingType;
  difficultyLevel: number;
  problemText: string;
  userAnswer?: string;
  correctAnswer?: string;  // 정답 (문제 해결 유형에만 제공)
  score?: number;
  evaluation?: string;
  feedback?: string;
  timeTakenSeconds?: number;
  hintsUsed: number;
  lobcoinEarned: number;
  lobcoinSpent: number;
  status: SessionStatus;
  completedAt?: string;
  createdAt: string;
}

export interface TrainingStatistics {
  trainingType: TrainingType;
  totalSessions: number;
  completedSessions: number;
  avgScore: number;
  bestScore: number;
  totalTimeSeconds: number;
  totalLobcoinEarned: number;
  totalLobcoinSpent: number;
  currentStreak: number;
  lastTrainingDate?: string;
}

/**
 * 훈련 세션 시작
 */
export const startTraining = async (
  trainingType: TrainingType,
  difficultyLevel: number
): Promise<TrainingSession> => {
  const response = await api.post('/training/start', {
    trainingType,
    difficultyLevel,
  });
  return response.data;
};

/**
 * 힌트 요청
 */
export const requestHint = async (sessionId: number): Promise<{ hint: string; cost: number }> => {
  const response = await api.post('/training/hint', { sessionId });
  return response.data;
};

/**
 * 답변 제출
 */
export const submitAnswer = async (
  sessionId: number,
  answer: string,
  timeTakenSeconds: number
): Promise<TrainingSession> => {
  const response = await api.post('/training/submit', {
    sessionId,
    answer,
    timeTakenSeconds,
  });
  return response.data;
};

/**
 * 훈련 세션 중단
 */
export const abandonSession = async (sessionId: number): Promise<void> => {
  await api.post(`/training/${sessionId}/abandon`);
};

/**
 * 훈련 기록 조회
 */
export const getTrainingHistory = async (type?: TrainingType): Promise<TrainingSession[]> => {
  const params = type ? { type } : {};
  const response = await api.get('/training/history', { params });
  return response.data;
};

/**
 * 훈련 통계 조회
 */
export const getTrainingStatistics = async (): Promise<TrainingStatistics[]> => {
  const response = await api.get('/training/statistics');
  return response.data;
};

/**
 * 훈련 세션 상세 조회
 */
export const getTrainingSession = async (sessionId: number): Promise<TrainingSession> => {
  const response = await api.get(`/training/${sessionId}`);
  return response.data;
};

/**
 * 훈련 유형 한글 이름
 */
export const getTrainingTypeName = (type: TrainingType): string => {
  switch (type) {
    case TrainingType.CRITICAL_THINKING:
      return '비판적 사고';
    case TrainingType.PROBLEM_SOLVING:
      return '문제 해결';
    case TrainingType.CREATIVE_THINKING:
      return '창의적 사고';
    case TrainingType.SELF_REFLECTION:
      return '자기 성찰';
    case TrainingType.REASONING:
      return '추리문제';
    case TrainingType.MEMORY:
      return '암기력';
    default:
      return type;
  }
};

/**
 * 훈련 유형 설명
 */
export const getTrainingTypeDescription = (type: TrainingType): string => {
  switch (type) {
    case TrainingType.CRITICAL_THINKING:
      return 'AI의 답변이나 정보의 오류를 찾아내는 훈련';
    case TrainingType.PROBLEM_SOLVING:
      return 'AI 없이 논리적으로 해결해야 하는 문제';
    case TrainingType.CREATIVE_THINKING:
      return 'AI가 생성하기 어려운 독창적인 아이디어';
    case TrainingType.SELF_REFLECTION:
      return 'AI 의존도를 인식하고 개선 방법 찾기';
    case TrainingType.REASONING:
      return '단서를 분석하고 논리적으로 추론하는 훈련';
    case TrainingType.MEMORY:
      return '정보를 기억하고 정확하게 재생하는 훈련';
    default:
      return '';
  }
};

/**
 * 훈련 유형 이모지
 */
export const getTrainingTypeIcon = (type: TrainingType): string => {
  switch (type) {
    case TrainingType.CRITICAL_THINKING:
      return '🔍';
    case TrainingType.PROBLEM_SOLVING:
      return '🧩';
    case TrainingType.CREATIVE_THINKING:
      return '💡';
    case TrainingType.SELF_REFLECTION:
      return '🪞';
    case TrainingType.REASONING:
      return '🕵️';
    case TrainingType.MEMORY:
      return '🧠';
    default:
      return '📝';
  }
};
