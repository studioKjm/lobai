import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor to add auth token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Types
export interface LobCoinBalance {
  balance: number;
  totalEarned: number;
  totalSpent: number;
  valueInUsd: number;
  updatedAt: string;
}

export interface Transaction {
  id: number;
  amount: number;
  balanceAfter: number;
  type: 'EARN' | 'SPEND';
  source: string;
  description: string;
  createdAt: string;
}

export interface Coupon {
  id: number;
  partnerName: string;
  couponType: string;
  costLobcoin: number;
  realValueUsd: number;
  title: string;
  description: string;
  terms: string;
  imageUrl?: string;
  isActive: boolean;
  stock?: number;
  isAffordable: boolean;
  isAvailable: boolean;
}

export interface IssuedCoupon {
  id: number;
  couponCode: string;
  couponTitle: string;
  partnerName: string;
  status: 'ISSUED' | 'USED' | 'EXPIRED' | 'REVOKED';
  issuedAt: string;
  usedAt?: string;
  expiresAt?: string;
}

// API Functions
export const lobcoinApi = {
  // Get balance
  getBalance: async (): Promise<LobCoinBalance> => {
    const response = await api.get('/api/lobcoin/balance');
    return response.data.data;
  },

  // Get transactions
  getTransactions: async (limit?: number): Promise<Transaction[]> => {
    const response = await api.get('/api/lobcoin/transactions', {
      params: { limit },
    });
    return response.data.data;
  },

  // Earn LobCoin (for testing)
  earnLobCoin: async (amount: number, source: string, description?: string) => {
    const response = await api.post('/api/lobcoin/earn', {
      amount,
      source,
      description,
    });
    return response.data.data;
  },

  // Spend LobCoin
  spendLobCoin: async (amount: number, source: string, description?: string) => {
    const response = await api.post('/api/lobcoin/spend', {
      amount,
      source,
      description,
    });
    return response.data.data;
  },

  // Get available coupons
  getCoupons: async (): Promise<Coupon[]> => {
    const response = await api.get('/api/coupons');
    return response.data.data;
  },

  // Purchase coupon
  purchaseCoupon: async (couponId: number): Promise<IssuedCoupon> => {
    const response = await api.post(`/api/coupons/${couponId}/purchase`);
    return response.data.data;
  },

  // Get my coupons
  getMyCoupons: async (): Promise<IssuedCoupon[]> => {
    const response = await api.get('/api/coupons/my');
    return response.data.data;
  },

  // Use coupon
  useCoupon: async (couponCode: string) => {
    const response = await api.post(`/api/coupons/use/${couponCode}`);
    return response.data;
  },
};
