import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { LandingPage } from '@/pages/LandingPage';
import { ChatPage } from '@/pages/ChatPage';
import { AdminPage } from '@/pages/AdminPage';
import HIPDashboard from '@/pages/HIPDashboard';
import RankingPage from '@/pages/RankingPage';
import PublicProfilePage from '@/pages/PublicProfilePage';
import { ProtectedRoute } from '@/components/common/ProtectedRoute';
import { useAuthStore } from '@/stores/authStore';

function App() {
  const { checkAuth } = useAuthStore();

  // Check authentication status on app initialization
  useEffect(() => {
    checkAuth();
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
        <Route
          path="/chat"
          element={
            <ProtectedRoute>
              <ChatPage />
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
