import React, { useState, useRef, useEffect } from 'react';
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
  const [isMoreMenuOpen, setIsMoreMenuOpen] = useState(false);
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const moreMenuRef = useRef<HTMLDivElement>(null);
  const profileMenuRef = useRef<HTMLDivElement>(null);
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

  // Close dropdowns when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (moreMenuRef.current && !moreMenuRef.current.contains(event.target as Node)) {
        setIsMoreMenuOpen(false);
      }
      if (profileMenuRef.current && !profileMenuRef.current.contains(event.target as Node)) {
        setIsProfileMenuOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    setIsProfileMenuOpen(false);
    navigate('/');
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
                {/* Main navigation links - Chat first */}
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
                  to="/shop"
                  onClick={(e) => handleProtectedRoute(e, '/shop')}
                  className={`text-sm font-medium transition-all ${
                    location.pathname === '/shop'
                      ? 'text-cyan-400 font-semibold opacity-100'
                      : 'opacity-70 hover:opacity-100'
                  }`}
                >
                  Shop
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

                {/* More dropdown (About/Pricing) */}
                <div className="relative" ref={moreMenuRef}>
                  <button
                    onClick={() => setIsMoreMenuOpen(!isMoreMenuOpen)}
                    className="text-sm font-medium opacity-70 hover:opacity-100 transition-opacity flex items-center gap-1"
                  >
                    More
                    <svg
                      className={`w-4 h-4 transition-transform ${isMoreMenuOpen ? 'rotate-180' : ''}`}
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                  </button>
                  {isMoreMenuOpen && (
                    <div className="absolute right-0 mt-2 w-40 bg-gray-900/95 backdrop-blur-xl border border-white/10 rounded-lg shadow-xl overflow-hidden z-50">
                      <Link
                        to="/about"
                        onClick={() => setIsMoreMenuOpen(false)}
                        className="block px-4 py-2 text-sm hover:bg-white/5 transition-colors"
                      >
                        About
                      </Link>
                      <Link
                        to="/pricing"
                        onClick={() => setIsMoreMenuOpen(false)}
                        className="block px-4 py-2 text-sm hover:bg-white/5 transition-colors"
                      >
                        Pricing
                      </Link>
                    </div>
                  )}
                </div>

                {/* Profile dropdown */}
                {user && (
                  <div className="relative" ref={profileMenuRef}>
                    <button
                      onClick={() => setIsProfileMenuOpen(!isProfileMenuOpen)}
                      className="w-8 h-8 rounded-full bg-gradient-to-br from-cyan-500 to-purple-600 flex items-center justify-center text-sm font-semibold hover:shadow-lg hover:shadow-cyan-500/50 transition-all"
                    >
                      {user.username.charAt(0).toUpperCase()}
                    </button>
                    {isProfileMenuOpen && (
                      <div className="absolute right-0 mt-2 w-48 bg-gray-900/95 backdrop-blur-xl border border-white/10 rounded-lg shadow-xl overflow-hidden z-50">
                        <div className="px-4 py-3 border-b border-white/10">
                          <p className="text-sm font-medium">{user.username}</p>
                          <p className="text-xs opacity-50">{user.email}</p>
                        </div>
                        <Link
                          to="/settings"
                          onClick={() => setIsProfileMenuOpen(false)}
                          className="block px-4 py-2 text-sm hover:bg-white/5 transition-colors"
                        >
                          Settings
                        </Link>
                        <button
                          onClick={handleLogout}
                          className="w-full text-left px-4 py-2 text-sm hover:bg-white/5 transition-colors text-red-400"
                        >
                          Sign out
                        </button>
                      </div>
                    )}
                  </div>
                )}
              </>
            )}

            {/* Login button for non-authenticated users */}
            {!user && !isLandingPage && (
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
