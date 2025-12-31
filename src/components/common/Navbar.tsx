import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useNavbarAutoHide } from '@/hooks/useNavbarAutoHide';
import { useAuthStore } from '@/stores/authStore';
import { AuthModal } from '@/components/auth';

export function Navbar() {
  const navbarVisible = useNavbarAutoHide();
  const { user, logout } = useAuthStore();
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);

  const handleNavClick = (e: React.MouseEvent<HTMLAnchorElement>, targetId: string) => {
    e.preventDefault();
    const element = document.getElementById(targetId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
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
            <a
              href="#features"
              onClick={(e) => handleNavClick(e, 'features')}
              className="text-sm font-medium opacity-70 hover:opacity-100 transition-opacity"
            >
              Features
            </a>
            <a
              href="#about"
              onClick={(e) => handleNavClick(e, 'about')}
              className="text-sm font-medium opacity-70 hover:opacity-100 transition-opacity"
            >
              About
            </a>

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
