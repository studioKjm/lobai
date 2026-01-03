import { create } from 'zustand';
import api, { getErrorMessage, ApiResponse } from '@/lib/api';
import type { Stats, Message, Persona, ActionType } from '@/types';
import toast from 'react-hot-toast';

interface ChatState {
  // State
  stats: Stats;
  messages: Message[];
  personas: Persona[];
  currentPersona: Persona | null;
  isTyping: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  loadStats: () => Promise<void>;
  updateStats: (action: ActionType) => Promise<void>;
  loadMessages: () => Promise<void>;
  sendMessage: (content: string) => Promise<void>;
  loadPersonas: () => Promise<void>;
  changePersona: (personaId: number) => Promise<void>;
  clearError: () => void;
  resetMessages: () => void;
}

export const useChatStore = create<ChatState>()((set, get) => ({
  // Initial state
  stats: {
    hunger: 80,
    energy: 90,
    happiness: 70
  },
  messages: [],
  personas: [],
  currentPersona: null,
  isTyping: false,
  isLoading: false,
  error: null,

  // Load stats from backend
  loadStats: async () => {
    try {
      const response = await api.get<ApiResponse<Stats>>('/stats');
      const statsData = response.data.data;

      set({
        stats: {
          hunger: statsData.hunger,
          energy: statsData.energy,
          happiness: statsData.happiness
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

      set({
        stats: {
          hunger: updatedStats.hunger,
          energy: updatedStats.energy,
          happiness: updatedStats.happiness
        },
        isLoading: false
      });

      // Add bot response for action
      const responses: Record<ActionType, string> = {
        hunger: "냠냠! 에너지가 충전되고 있어요.",
        energy: "꿀잠 자고 일어났어요. 몸이 가볍네요!",
        happiness: "와! 같이 노니까 정말 즐거워요!"
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

  // Load available personas from backend
  loadPersonas: async () => {
    try {
      const response = await api.get<ApiResponse<Persona[]>>('/personas');
      const personasData = response.data.data;

      set({
        personas: personasData,
        currentPersona: personasData[0] || null  // Default to first persona
      });
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      console.error('Failed to load personas:', errorMessage);

      // Set default persona if backend fails
      set({
        personas: [{
          id: 1,
          name: '친구',
          nameEn: 'friend',
          displayName: '친구모드',
          systemInstruction: '친구처럼 대화하세요',
          iconEmoji: '👥'
        }],
        currentPersona: {
          id: 1,
          name: '친구',
          nameEn: 'friend',
          displayName: '친구모드',
          systemInstruction: '친구처럼 대화하세요',
          iconEmoji: '👥'
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
  }
}));
