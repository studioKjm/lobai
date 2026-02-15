import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { LandingPage } from '@/pages/LandingPage';
import { ChatPage } from '@/pages/ChatPage';
import { AdminPage } from '@/pages/AdminPage';
import HIPDashboard from '@/pages/HIPDashboard';
import RankingPage from '@/pages/RankingPage';
import PublicProfilePage from '@/pages/PublicProfilePage';
import { AffinityAnalysisPage } from '@/pages/AffinityAnalysisPage';
import { ResilienceAnalysisPage } from '@/pages/ResilienceAnalysisPage';
import { TrainingPage } from '@/pages/TrainingPage';
import { AboutPage } from '@/pages/AboutPage';
import { PricingPage } from '@/pages/PricingPage';
import { SettingsPage } from '@/pages/SettingsPage';
import { LobCoinShopPage } from '@/pages/LobCoinShopPage';
import { ProtectedRoute } from '@/components/common/ProtectedRoute';
import { useAuthStore } from '@/stores/authStore';

function App() {
  const { checkAuth } = useAuthStore();

  // Check authentication status on app initialization
  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  // Re-authenticate on HMR (Hot Module Replacement)
  useEffect(() => {
    if (import.meta.hot) {
      import.meta.hot.on('vite:beforeUpdate', () => {
        // Preserve authentication state during HMR
        const token = localStorage.getItem('accessToken');
        if (token) {
          console.log('🔄 HMR detected - preserving auth state');
        }
      });

      import.meta.hot.on('vite:afterUpdate', () => {
        // Re-check auth after HMR completes
        const token = localStorage.getItem('accessToken');
        if (token) {
          console.log('✅ HMR complete - restoring auth state');
          checkAuth();
        }
      });
    }
  }, [checkAuth]);

  return (
    <Router>
      {/* Toast notifications */}
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 3000,
          style: {
            background: 'rgba(255, 255, 255, 0.1)',
            backdropFilter: 'blur(20px)',
            color: '#fff',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            borderRadius: '12px',
          },
          success: {
            iconTheme: {
              primary: '#00d9ff',
              secondary: '#fff',
            },
          },
          error: {
            iconTheme: {
              primary: '#ef4444',
              secondary: '#fff',
            },
          },
        }}
      />

      {/* Routes */}
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/about" element={<AboutPage />} />
        <Route path="/pricing" element={<PricingPage />} />
        <Route
          path="/chat"
          element={
            <ProtectedRoute>
              <ChatPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/shop"
          element={
            <ProtectedRoute>
              <LobCoinShopPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <HIPDashboard />
            </ProtectedRoute>
          }
        />
        <Route path="/dashboard/ranking" element={<RankingPage />} />
        <Route path="/hip/:hipId" element={<PublicProfilePage />} />
        <Route
          path="/affinity"
          element={
            <ProtectedRoute>
              <AffinityAnalysisPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/resilience"
          element={
            <ProtectedRoute>
              <ResilienceAnalysisPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/training"
          element={
            <ProtectedRoute>
              <TrainingPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/settings"
          element={
            <ProtectedRoute>
              <SettingsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <ProtectedRoute>
              <AdminPage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
