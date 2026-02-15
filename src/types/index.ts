// Core Types for LobAI Application

export interface Stats {
  hunger: number;
  energy: number;
  happiness: number;
  trust: number;
}

export interface Message {
  role: 'user' | 'bot';
  content: string;
  attachmentUrl?: string;
  attachmentType?: string;
  attachmentName?: string;
  llmProvider?: string;
  llmModel?: string;
  messageType?: 'NORMAL' | 'PROACTIVE';
}

export interface Persona {
  id: number;
  name: string;
  nameEn: string;
  displayName: string;
  systemInstruction: string;
  iconEmoji: string;
  displayOrder?: number;
  isActive?: boolean;
}

export type ActionType = 'feed' | 'play' | 'sleep';

// User types (for future backend integration)
export interface User {
  id: number;
  email: string;
  username: string;
  currentHunger: number;
  currentEnergy: number;
  currentHappiness: number;
  currentPersonaId?: number;
  trustLevel?: number;
  experiencePoints?: number;
}

// Auth types (for future backend integration)
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
  userId: number;
  email: string;
  username: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  username: string;
}

// Admin Statistics Types
export interface StatsOverview {
  totalUsers: number;
  activeUsersToday: number;
  newUsersThisWeek: number;
  newUsersThisMonth: number;
  totalMessages: number;
  messagesToday: number;
  messagesThisWeek: number;
  messagesThisMonth: number;
  avgMessagesPerUser: number;
  avgHunger: number;
  avgEnergy: number;
  avgHappiness: number;
  mostPopularPersona: string;
  mostPopularPersonaCount: number;
}

export interface DataPoint {
  date: string;
  count: number;
}

export interface ActiveUsersChart {
  dailyActiveUsers: DataPoint[];
  newUserSignups: DataPoint[];
}

export interface DailyMessageCount {
  date: string;
  userMessages: number;
  botMessages: number;
  total: number;
}

export interface MessageStats {
  totalMessages: number;
  userMessages: number;
  botMessages: number;
  avgMessagesPerUser: number;
  avgMessagesPerDay: number;
  dailyMessages: DailyMessageCount[];
}

export interface PersonaUsageStat {
  personaId: number;
  personaName: string;
  displayName: string;
  iconEmoji: string;
  messageCount: number;
  currentUserCount: number;
  usagePercentage: number;
}

export interface PersonaStats {
  personaUsage: PersonaUsageStat[];
}

// Schedule Types
export interface ScheduleEvent {
  id: number;
  title: string;
  description?: string;
  startTime: string;  // ISO 8601
  endTime: string;
  type: 'REMINDER' | 'INTERACTION' | 'EVENT';
  isCompleted: boolean;
  timezone: string;
  notifyBeforeMinutes?: number;
  createdAt: string;
}

// Daily Conversation Summary Types
export interface DailySummaryItem {
  date: string;          // "2026-02-15"
  summaryPreview: string;
  messageCount: number;
  keyFactsPreview: string;
}

export interface DailySummaryDetail {
  date: string;
  summaryText: string;
  keyFacts: string;      // JSON string of key facts array
  messageCount: number;
  messages: SummaryMessageResponse[];
}

export interface SummaryMessageResponse {
  id: number;
  role: string;          // "user" or "bot"
  content: string;
  personaId?: number;
  personaName?: string;
  messageType: string;
  createdAt: string;
}

export interface CreateScheduleRequest {
  title: string;
  description?: string;
  startTime: string;
  endTime: string;
  type: 'REMINDER' | 'INTERACTION' | 'EVENT';
  timezone?: string;
  notifyBeforeMinutes?: number;
}
