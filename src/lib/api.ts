import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import toast from 'react-hot-toast';

// API Base URL
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 seconds
});

// Request interceptor - Add JWT token to requests
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken');

    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Handle errors and token refresh
api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // Handle 401 Unauthorized - Token expired
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');

        if (!refreshToken) {
          // No refresh token, redirect to login
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/';
          return Promise.reject(error);
        }

        // Try to refresh the access token
        const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
          refreshToken
        });

        const { accessToken } = response.data.data;

        // Save new access token
        localStorage.setItem('accessToken', accessToken);

        // Retry the original request with new token
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        }

        return api(originalRequest);
      } catch (refreshError) {
        // Refresh failed, clear tokens and redirect to login
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        toast.error('세션이 만료되었습니다. 다시 로그인해주세요.');
        window.location.href = '/';
        return Promise.reject(refreshError);
      }
    }

    // Handle 403 Forbidden - Insufficient permissions
    if (error.response?.status === 403) {
      toast.error('접근 권한이 없습니다.');
      window.location.href = '/';
    }

    // Handle 500 Internal Server Error
    if (error.response?.status === 500) {
      toast.error('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
    }

    // Handle network errors
    if (!error.response) {
      toast.error('네트워크 연결을 확인해주세요.');
    }

    return Promise.reject(error);
  }
);

export default api;

// API Response types
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  errorCode?: string;
}

// Helper function to handle API errors
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiResponse<any>>;
    return axiosError.response?.data?.message || axiosError.message || '알 수 없는 오류가 발생했습니다.';
  }

  if (error instanceof Error) {
    return error.message;
  }

  return '알 수 없는 오류가 발생했습니다.';
}
