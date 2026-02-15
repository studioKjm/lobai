import api, { ApiResponse } from '@/lib/api';

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

// API Functions - uses shared api instance (with token refresh logic)
export const lobcoinApi = {
  // Get balance
  getBalance: async (): Promise<LobCoinBalance> => {
    const response = await api.get<ApiResponse<LobCoinBalance>>('/lobcoin/balance');
    return response.data.data;
  },

  // Get transactions
  getTransactions: async (limit?: number): Promise<Transaction[]> => {
    const response = await api.get<ApiResponse<Transaction[]>>('/lobcoin/transactions', {
      params: { limit },
    });
    return response.data.data;
  },

  // Earn LobCoin (for testing)
  earnLobCoin: async (amount: number, source: string, description?: string) => {
    const response = await api.post<ApiResponse<Transaction>>('/lobcoin/earn', {
      amount,
      source,
      description,
    });
    return response.data.data;
  },

  // Spend LobCoin
  spendLobCoin: async (amount: number, source: string, description?: string) => {
    const response = await api.post<ApiResponse<Transaction>>('/lobcoin/spend', {
      amount,
      source,
      description,
    });
    return response.data.data;
  },

  // Get available coupons
  getCoupons: async (): Promise<Coupon[]> => {
    const response = await api.get<ApiResponse<Coupon[]>>('/coupons');
    return response.data.data;
  },

  // Purchase coupon
  purchaseCoupon: async (couponId: number): Promise<IssuedCoupon> => {
    const response = await api.post<ApiResponse<IssuedCoupon>>(`/coupons/${couponId}/purchase`);
    return response.data.data;
  },

  // Get my coupons
  getMyCoupons: async (): Promise<IssuedCoupon[]> => {
    const response = await api.get<ApiResponse<IssuedCoupon[]>>('/coupons/my');
    return response.data.data;
  },

  // Use coupon
  useCoupon: async (couponCode: string) => {
    const response = await api.post(`/coupons/use/${couponCode}`);
    return response.data;
  },
};
