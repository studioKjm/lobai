// Core Types for LobAI Application

export interface Stats {
  hunger: number;
  energy: number;
  happiness: number;
}

export interface Message {
  role: 'user' | 'bot';
  text: string;
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

export type ActionType = 'hunger' | 'energy' | 'happiness';

// User types (for future backend integration)
export interface User {
  id: number;
  email: string;
  username: string;
  currentHunger: number;
  currentEnergy: number;
  currentHappiness: number;
  currentPersonaId?: number;
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
