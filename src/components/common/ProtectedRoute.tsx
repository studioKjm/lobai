import { ReactNode, useEffect, useState } from 'react';
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
  const { accessToken, checkAuth } = useAuthStore();
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    // Check localStorage directly first
    const token = localStorage.getItem('accessToken');

    if (token && !accessToken) {
      // Token exists in localStorage but not in store, need to sync
      checkAuth().finally(() => setIsChecking(false));
    } else {
      setIsChecking(false);
    }
  }, [accessToken, checkAuth]);

  // Show loading while checking auth
  if (isChecking) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-purple-900 to-violet-800">
        <div className="text-white text-xl">인증 확인 중...</div>
      </div>
    );
  }

  // Check both store and localStorage
  const token = accessToken || localStorage.getItem('accessToken');

  if (!token) {
    // Not authenticated, redirect to landing page
    return <Navigate to="/" replace />;
  }

  // Authenticated, render the protected content
  return <>{children}</>;
}
