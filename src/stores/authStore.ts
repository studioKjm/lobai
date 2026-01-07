import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import api, { getErrorMessage, ApiResponse } from '@/lib/api';
import type { AuthResponse, LoginRequest, RegisterRequest, User } from '@/types';
import toast from 'react-hot-toast';

interface AuthState {
  // State
  accessToken: string | null;
  user: User | null;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  clearError: () => void;
  checkAuth: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // Initial state
      accessToken: null,
      user: null,
      isLoading: false,
      error: null,

      // Login action
      login: async (credentials: LoginRequest) => {
        set({ isLoading: true, error: null });

        try {
          const response = await api.post<ApiResponse<AuthResponse>>('/auth/login', credentials);
          const { accessToken, refreshToken, userId, email, username } = response.data.data;

          // Save tokens
          localStorage.setItem('accessToken', accessToken);
          localStorage.setItem('refreshToken', refreshToken);

          // Update state
          set({
            accessToken,
            user: { id: userId, email, username } as User,
            isLoading: false,
            error: null
          });

          toast.success('로그인 성공!');
        } catch (error) {
          const errorMessage = getErrorMessage(error);
          set({ isLoading: false, error: errorMessage });
          toast.error(errorMessage);
          throw error;
        }
      },

      // Register action
      register: async (data: RegisterRequest) => {
        set({ isLoading: true, error: null });

        try {
          const response = await api.post<ApiResponse<AuthResponse>>('/auth/register', data);
          const { accessToken, refreshToken, userId, email, username } = response.data.data;

          // Save tokens
          localStorage.setItem('accessToken', accessToken);
          localStorage.setItem('refreshToken', refreshToken);

          // Update state
          set({
            accessToken,
            user: { id: userId, email, username } as User,
            isLoading: false,
            error: null
          });

          toast.success('회원가입 성공!');
        } catch (error) {
          const errorMessage = getErrorMessage(error);
          set({ isLoading: false, error: errorMessage });
          toast.error(errorMessage);
          throw error;
        }
      },

      // Logout action
      logout: () => {
        // Call backend logout endpoint
        api.post('/auth/logout').catch(() => {
          // Ignore errors on logout
        });

        // Clear tokens
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');

        // Reset state
        set({
          accessToken: null,
          user: null,
          error: null
        });

        toast.success('로그아웃되었습니다.');
      },

      // Clear error
      clearError: () => {
        set({ error: null });
      },

      // Check authentication status (for app initialization)
      checkAuth: async () => {
        const token = localStorage.getItem('accessToken');

        if (!token) {
          set({ accessToken: null, user: null });
          return;
        }

        try {
          // Try to get current user info from backend
          const response = await api.get<ApiResponse<User>>('/auth/me');
          const user = response.data.data;

          set({
            accessToken: token,
            user
          });
        } catch (error) {
          // Token is invalid, clear everything
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          set({ accessToken: null, user: null });
        }
      }
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        // Persist both accessToken and user for HMR stability
        accessToken: state.accessToken,
        user: state.user
      })
    }
  )
);
