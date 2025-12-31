import { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';

interface ProtectedRouteProps {
  children: ReactNode;
}

/**
 * Protected Route Component
 * Redirects to landing page if user is not authenticated
 * Used to protect routes that require authentication (e.g., /chat)
 */
export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { accessToken } = useAuthStore();

  if (!accessToken) {
    // Not authenticated, redirect to landing page
    return <Navigate to="/" replace />;
  }

  // Authenticated, render the protected content
  return <>{children}</>;
}
