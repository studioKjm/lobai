import { Link } from 'react-router-dom';
import { useNavbarAutoHide } from '@/hooks/useNavbarAutoHide';

export function Navbar() {
  const navbarVisible = useNavbarAutoHide();

  const handleNavClick = (e: React.MouseEvent<HTMLAnchorElement>, targetId: string) => {
    e.preventDefault();
    const element = document.getElementById(targetId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
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
          <button className="btn-primary text-sm">Login</button>
        </div>
      </div>
    </nav>
  );
}
