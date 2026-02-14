import { create } from 'zustand';
import api, { getErrorMessage, ApiResponse } from '@/lib/api';
import type { Stats, Message, Persona, ActionType, ScheduleEvent, CreateScheduleRequest } from '@/types';
import { scheduleApi } from '@/lib/scheduleApi';
import { streamMessage } from '@/lib/streamApi';
import toast from 'react-hot-toast';

interface ChatState {
  // State
  stats: Stats;
  messages: Message[];
  personas: Persona[];
  currentPersona: Persona | null;
  isTyping: boolean;
  isLoading: boolean;
  isStreaming: boolean;
  streamingContent: string;
  error: string | null;
  schedules: ScheduleEvent[];
  isScheduleLoading: boolean;

  // Actions
  loadStats: () => Promise<void>;
  updateStats: (action: ActionType) => Promise<void>;
  setStatValue: (statName: keyof Stats, value: number) => void;
  syncStatsToBackend: () => Promise<void>;
  sleepTick: () => Promise<void>;
  loadMessages: () => Promise<void>;
  sendMessage: (content: string) => Promise<void>;
  sendMessageStream: (content: string) => Promise<void>;
  sendMessageWithFile: (content: string, file: File) => Promise<void>;
  loadPersonas: () => Promise<void>;
  changePersona: (personaId: number) => Promise<void>;
  clearError: () => void;
  resetMessages: () => void;
  clearMessageHistory: () => Promise<void>;
  loadTodaysSchedules: () => Promise<void>;
  loadSchedulesByRange: (start: string, end: string) => Promise<void>;
  addSchedule: (data: CreateScheduleRequest) => Promise<void>;
  updateSchedule: (id: number, data: Partial<CreateScheduleRequest>) => Promise<void>;
  deleteSchedule: (id: number) => Promise<void>;
}

export const useChatStore = create<ChatState>()((set, get) => ({
  // Initial state (will be overwritten by backend)
  stats: {
    hunger: 50,
    energy: 50,
    happiness: 50,
    trust: 50
  },
  messages: [],
  personas: [],
  currentPersona: null,
  isTyping: false,
  isLoading: false,
  isStreaming: false,
  streamingContent: '',
  error: null,
  schedules: [],
  isScheduleLoading: false,

  // Load stats from backend
  loadStats: async () => {
    try {
      const response = await api.get<ApiResponse<Stats>>('/stats');
      const statsData = response.data.data;

      set({
        stats: {
          hunger: statsData.hunger,
          energy: statsData.energy,
          happiness: statsData.happiness,
          trust: statsData.trust ?? 50
        }
      });
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      console.error('Failed to load stats:', errorMessage);
      // Don't show toast for stats loading failure (use default values)
    }
  },

  // Update stats (Feed, Play, Sleep actions)
  updateStats: async (action: ActionType) => {
    set({ isLoading: true, error: null });

    try {
      const response = await api.put<ApiResponse<Stats>>('/stats', { action });
      const updatedStats = response.data.data;

      set(state => ({
        stats: {
          hunger: updatedStats.hunger,
          energy: updatedStats.energy,
          happiness: updatedStats.happiness,
          trust: updatedStats.trust ?? state.stats.trust
        },
        isLoading: false
      }));

      // Add bot response for action
      const responses: Record<ActionType, string> = {
        feed: "냠냠! 에너지가 충전되고 있어요.",
        sleep: "꿀잠 자고 일어났어요. 몸이 가볍네요!",
        play: "와! 같이 노니까 정말 즐거워요!"
      };

      set(state => ({
        messages: [...state.messages, {
          role: 'bot' as const,
          content: responses[action]
        }]
      }));

      toast.success('상태가 업데이트되었습니다!');
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      set({ isLoading: false, error: errorMessage });
      toast.error(errorMessage);
    }
  },

  // Set individual stat value (for UI controls)
  setStatValue: (statName: keyof Stats, value: number) => {
    const clampedValue = Math.max(0, Math.min(100, value));
    set(state => ({
      stats: {
        ...state.stats,
        [statName]: clampedValue
      }
    }));
  },

  // Sync current stats to backend
  syncStatsToBackend: async () => {
    const { stats } = get();
    try {
      await api.patch<ApiResponse<Stats>>('/stats', {
        hunger: stats.hunger,
        energy: stats.energy,
        happiness: stats.happiness
      });
    } catch (error) {
      console.error('Failed to sync stats to backend:', getErrorMessage(error));
    }
  },

  // Sleep tick - called every second during sleep
  // Increases energy (+2) and decreases hunger (-1)
  sleepTick: async () => {
    const { stats } = get();
    const newEnergy = Math.min(100, stats.energy + 2);
    const newHunger = Math.max(0, stats.hunger - 1);

    // Update local state immediately
    set({
      stats: {
        ...stats,
        energy: newEnergy,
        hunger: newHunger
      }
    });

    // Sync to backend
    try {
      await api.patch<ApiResponse<Stats>>('/stats', {
        hunger: newHunger,
        energy: newEnergy,
        happiness: stats.happiness
      });
    } catch (error) {
      console.error('Failed to sync sleep tick to backend:', getErrorMessage(error));
    }
  },

  // Load message history from backend
  loadMessages: async () => {
    set({ isLoading: true, error: null });

    try {
      const response = await api.get<ApiResponse<Message[]>>('/messages');
      const messagesData = response.data.data;

      // Add welcome message if no messages exist
      if (messagesData.length === 0) {
        set({
          messages: [{
            role: 'bot' as const,
            content: '안녕하세요! 저는 당신의 AI 동반자 Lobi입니다. 오늘도 멋진 하루 보내세요!'
          }],
          isLoading: false
        });
      } else {
        set({
          messages: messagesData,
          isLoading: false
        });
      }
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      set({ isLoading: false, error: errorMessage });

      // Show welcome message on error
      set({
        messages: [{
          role: 'bot' as const,
          content: '안녕하세요! 저는 당신의 AI 동반자 Lobi입니다. 오늘도 멋진 하루 보내세요!'
        }]
      });
    }
  },

  // Send message to backend (which calls Gemini)
  sendMessage: async (content: string) => {
    if (!content.trim() || get().isTyping) return;

    const { currentPersona } = get();

    // Add user message immediately
    set(state => ({
      messages: [...state.messages, { role: 'user' as const, content: content }],
      isTyping: true,
      error: null
    }));

    try {
      const response = await api.post<ApiResponse<{
        userMessage: Message;
        botMessage: Message;
        statsUpdate?: Partial<Stats>;
      }>>('/messages', {
        content,
        personaId: currentPersona?.id || 1  // Default to persona 1 if not set
      });

      const { botMessage, statsUpdate } = response.data.data;

      // Add bot message
      set(state => ({
        messages: [...state.messages, botMessage],
        isTyping: false
      }));

      // Update stats if provided
      if (statsUpdate) {
        set(state => ({
          stats: {
            ...state.stats,
            ...statsUpdate
          }
        }));
      }

      // Auto-reload schedules after message (in case schedule was created)
      // Check if message contains schedule-related keywords
      const scheduleKeywords = ['일정', '등록', '추가', '스케줄'];
      const isScheduleRelated = scheduleKeywords.some(keyword =>
        content.includes(keyword) || botMessage.content.includes(keyword)
      );

      if (isScheduleRelated) {
        // Reload schedules after a short delay
        setTimeout(() => {
          get().loadTodaysSchedules();
        }, 500);
      }
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      set({
        isTyping: false,
        error: errorMessage
      });

      // Add error message
      set(state => ({
        messages: [...state.messages, {
          role: 'bot' as const,
          content: '삐비빅... 지금은 대화가 조금 어려워요.'
        }]
      }));

      toast.error(errorMessage);
    }
  },

  // Send message with SSE streaming
  sendMessageStream: async (content: string) => {
    if (!content.trim() || get().isTyping || get().isStreaming) return;

    const { currentPersona } = get();

    // Add user message immediately
    set(state => ({
      messages: [...state.messages, { role: 'user' as const, content }],
      isStreaming: true,
      streamingContent: '',
      error: null
    }));

    streamMessage(
      content,
      currentPersona?.id || 1,
      // onChunk
      (text: string) => {
        set(state => ({
          streamingContent: state.streamingContent + text
        }));
      },
      // onDone
      () => {
        const { streamingContent } = get();
        set(state => ({
          messages: [...state.messages, {
            role: 'bot' as const,
            content: streamingContent
          }],
          isStreaming: false,
          streamingContent: ''
        }));

        // Reload stats after streaming completes
        get().loadStats();

        // Check for schedule-related content
        const scheduleKeywords = ['일정', '등록', '추가', '스케줄'];
        const isScheduleRelated = scheduleKeywords.some(keyword =>
          content.includes(keyword) || get().streamingContent.includes(keyword)
        );
        if (isScheduleRelated) {
          setTimeout(() => get().loadTodaysSchedules(), 500);
        }
      },
      // onError
      (error: Error) => {
        set(state => ({
          isStreaming: false,
          streamingContent: '',
          error: error.message,
          messages: [...state.messages, {
            role: 'bot' as const,
            content: '삐비빅... 지금은 대화가 조금 어려워요.'
          }]
        }));
        toast.error('스트리밍 중 오류가 발생했습니다');
      }
    );
  },

  // Send message with file attachment
  sendMessageWithFile: async (content: string, file: File) => {
    if (!content.trim() || get().isTyping) return;

    const { currentPersona } = get();

    // Add user message immediately
    set(state => ({
      messages: [...state.messages, {
        role: 'user' as const,
        content: content,
        attachmentName: file.name,
        attachmentType: file.type
      }],
      isTyping: true,
      error: null
    }));

    try {
      // Create FormData for multipart/form-data request
      const formData = new FormData();
      formData.append('content', content);
      formData.append('file', file);
      if (currentPersona?.id) {
        formData.append('personaId', String(currentPersona.id));
      }

      const response = await api.post<ApiResponse<{
        userMessage: Message;
        botMessage: Message;
        statsUpdate?: Partial<Stats>;
      }>>('/messages/with-file', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        }
      });

      const { botMessage, statsUpdate } = response.data.data;

      // Add bot message
      set(state => ({
        messages: [...state.messages, botMessage],
        isTyping: false
      }));

      // Update stats if provided
      if (statsUpdate) {
        set(state => ({
          stats: {
            ...state.stats,
            ...statsUpdate
          }
        }));
      }

      toast.success('파일이 전송되었습니다');
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      set({
        isTyping: false,
        error: errorMessage
      });

      // Add error message
      set(state => ({
        messages: [...state.messages, {
          role: 'bot' as const,
          content: '파일 전송에 실패했습니다. 다시 시도해 주세요.'
        }]
      }));

      toast.error(errorMessage);
    }
  },

  // Load available personas from backend
  loadPersonas: async () => {
    try {
      const response = await api.get<ApiResponse<Persona[]>>('/personas');
      const personasData = response.data.data;

      // Find lobby_master persona and set it as default
      const lobbyMaster = personasData.find(p => p.nameEn === 'lobby_master');
      const defaultPersona = lobbyMaster || personasData[0] || null;

      set({
        personas: personasData,
        currentPersona: defaultPersona
      });
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      console.error('Failed to load personas:', errorMessage);

      // Set default lobby_master persona if backend fails
      set({
        personas: [{
          id: 1,
          name: '로비 마스터',
          nameEn: 'lobby_master',
          displayName: '로비모드',
          systemInstruction: '로비 마스터로 대화하세요',
          iconEmoji: '🏛️'
        }],
        currentPersona: {
          id: 1,
          name: '로비 마스터',
          nameEn: 'lobby_master',
          displayName: '로비모드',
          systemInstruction: '로비 마스터로 대화하세요',
          iconEmoji: '🏛️'
        }
      });
    }
  },

  // Change current persona
  changePersona: async (personaId: number) => {
    set({ isLoading: true, error: null });

    try {
      await api.put<ApiResponse<void>>('/personas/current', { personaId });

      const persona = get().personas.find(p => p.id === personaId);
      if (persona) {
        set({
          currentPersona: persona,
          isLoading: false
        });
        toast.success(`${persona.displayName}로 전환했습니다!`);
      }
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      set({ isLoading: false, error: errorMessage });
      toast.error(errorMessage);
    }
  },

  // Clear error
  clearError: () => {
    set({ error: null });
  },

  // Reset messages (for testing)
  resetMessages: () => {
    set({
      messages: [{
        role: 'bot' as const,
        content: '안녕하세요! 저는 당신의 AI 동반자 Lobi입니다. 오늘도 멋진 하루 보내세요!'
      }]
    });
  },

  // Clear message history (backend + frontend)
  clearMessageHistory: async () => {
    set({ isLoading: true, error: null });

    try {
      await api.delete<ApiResponse<void>>('/messages');

      set({
        messages: [{
          role: 'bot' as const,
          content: '안녕하세요! 저는 당신의 AI 동반자 Lobi입니다. 대화 기록이 초기화되었습니다.'
        }],
        isLoading: false
      });

      toast.success('대화 기록이 초기화되었습니다!');
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      set({ isLoading: false, error: errorMessage });
      toast.error(errorMessage);
    }
  },

  // Load today's schedules
  loadTodaysSchedules: async () => {
    set({ isScheduleLoading: true });
    try {
      const schedules = await scheduleApi.getTodaysSchedules();
      set({ schedules, isScheduleLoading: false });
    } catch (error) {
      console.error('Failed to load schedules:', getErrorMessage(error));
      set({ isScheduleLoading: false });
    }
  },

  // Load schedules by date range
  loadSchedulesByRange: async (start: string, end: string) => {
    set({ isScheduleLoading: true });
    try {
      const schedules = await scheduleApi.getSchedulesByRange(start, end);
      set({ schedules, isScheduleLoading: false });
    } catch (error) {
      console.error('Failed to load schedules:', getErrorMessage(error));
      set({ isScheduleLoading: false });
    }
  },

  // Add schedule
  addSchedule: async (data: CreateScheduleRequest) => {
    try {
      const newSchedule = await scheduleApi.createSchedule(data);
      set(state => ({ schedules: [...state.schedules, newSchedule] }));
      toast.success('일정이 추가되었습니다');
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      toast.error(errorMessage);
      throw error;
    }
  },

  // Update schedule
  updateSchedule: async (id: number, data: Partial<CreateScheduleRequest>) => {
    try {
      const updated = await scheduleApi.updateSchedule(id, data);
      set(state => ({
        schedules: state.schedules.map(s => s.id === id ? updated : s)
      }));
      toast.success('일정이 수정되었습니다');
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      toast.error(errorMessage);
      throw error;
    }
  },

  // Delete schedule
  deleteSchedule: async (id: number) => {
    try {
      await scheduleApi.deleteSchedule(id);
      set(state => ({
        schedules: state.schedules.filter(s => s.id !== id)
      }));
      toast.success('일정이 삭제되었습니다');
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      toast.error(errorMessage);
      throw error;
    }
  }
}));
