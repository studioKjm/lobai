import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useNavbarAutoHide } from '@/hooks/useNavbarAutoHide';
import { useAuthStore } from '@/stores/authStore';
import { AuthModal } from '@/components/auth';

export function Navbar() {
  const navbarVisible = useNavbarAutoHide();
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const isLandingPage = location.pathname === '/';

  const handleNavClick = (e: React.MouseEvent<HTMLAnchorElement>, targetId: string) => {
    e.preventDefault();
    const element = document.getElementById(targetId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  const handleProtectedRoute = (e: React.MouseEvent<HTMLAnchorElement>, path: string) => {
    if (!user) {
      e.preventDefault();
      setIsAuthModalOpen(true);
    }
  };

  return (
    <>
      <nav className={`navbar ${!navbarVisible ? 'hidden' : ''}`}>
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <Link to="/" className="logo">
            <div className="logo-mark">L</div>
            <div className="logo-text">LobAI</div>
          </Link>

          <div className="flex items-center gap-6">
            {isLandingPage ? (
              <>
                <Link
                  to="/chat"
                  onClick={(e) => handleProtectedRoute(e, '/chat')}
                  className="text-sm font-medium opacity-70 hover:opacity-100 transition-opacity"
                >
                  Start
                </Link>
                <a
                  href="#features"
                  onClick={(e) => handleNavClick(e, 'features')}
                  className="text-sm font-medium opacity-70 hover:opacity-100 transition-opacity"
                >
                  Features
                </a>
                <a
                  href="#pricing"
                  onClick={(e) => handleNavClick(e, 'pricing')}
                  className="text-sm font-medium opacity-70 hover:opacity-100 transition-opacity"
                >
                  Pricing
                </a>
                <Link
                  to="/about"
                  className="text-sm font-medium opacity-70 hover:opacity-100 transition-opacity"
                >
                  About
                </Link>
              </>
            ) : (
              <>
                <Link
                  to="/about"
                  className={`text-sm font-medium transition-all ${
                    location.pathname === '/about'
                      ? 'text-cyan-400 font-semibold opacity-100'
                      : 'opacity-70 hover:opacity-100'
                  }`}
                >
                  About
                </Link>
                <Link
                  to="/pricing"
                  className={`text-sm font-medium transition-all ${
                    location.pathname === '/pricing'
                      ? 'text-cyan-400 font-semibold opacity-100'
                      : 'opacity-70 hover:opacity-100'
                  }`}
                >
                  Pricing
                </Link>
                <Link
                  to="/chat"
                  onClick={(e) => handleProtectedRoute(e, '/chat')}
                  className={`text-sm font-medium transition-all ${
                    location.pathname === '/chat'
                      ? 'text-cyan-400 font-semibold opacity-100'
                      : 'opacity-70 hover:opacity-100'
                  }`}
                >
                  Chat
                </Link>
                <Link
                  to="/dashboard"
                  onClick={(e) => handleProtectedRoute(e, '/dashboard')}
                  className={`text-sm font-medium transition-all ${
                    location.pathname === '/dashboard'
                      ? 'text-cyan-400 font-semibold opacity-100'
                      : 'opacity-70 hover:opacity-100'
                  }`}
                >
                  HIP Dashboard
                </Link>
                <Link
                  to="/affinity"
                  onClick={(e) => handleProtectedRoute(e, '/affinity')}
                  className={`text-sm font-medium transition-all ${
                    location.pathname === '/affinity'
                      ? 'text-cyan-400 font-semibold opacity-100'
                      : 'opacity-70 hover:opacity-100'
                  }`}
                >
                  Affinity
                </Link>
                <Link
                  to="/resilience"
                  onClick={(e) => handleProtectedRoute(e, '/resilience')}
                  className={`text-sm font-medium transition-all ${
                    location.pathname === '/resilience'
                      ? 'text-cyan-400 font-semibold opacity-100'
                      : 'opacity-70 hover:opacity-100'
                  }`}
                >
                  Resilience
                </Link>
                <Link
                  to="/training"
                  onClick={(e) => handleProtectedRoute(e, '/training')}
                  className={`text-sm font-medium transition-all ${
                    location.pathname === '/training'
                      ? 'text-cyan-400 font-semibold opacity-100'
                      : 'opacity-70 hover:opacity-100'
                  }`}
                >
                  AI 독립 훈련
                </Link>
              </>
            )}

            {user ? (
              <div className="flex items-center gap-4">
                <span className="text-sm opacity-70">{user.username}</span>
                <button onClick={logout} className="btn-secondary text-sm px-4 py-2">
                  로그아웃
                </button>
              </div>
            ) : (
              <button
                onClick={() => setIsAuthModalOpen(true)}
                className="btn-primary text-sm px-4 py-2"
              >
                로그인
              </button>
            )}
          </div>
        </div>
      </nav>

      {/* Auth Modal */}
      <AuthModal
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        initialMode="login"
      />
    </>
  );
}
