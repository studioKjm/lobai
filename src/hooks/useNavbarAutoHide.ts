import { useState, useEffect, useRef } from 'react';

/**
 * Custom hook for auto-hiding navbar on scroll
 * Shows navbar when:
 * - At top of page (scrollY < 100)
 * - Scrolling up
 * - Mouse hovering at top (clientY < 80)
 * Hides navbar after 3 seconds of inactivity when scrolled down
 */
export function useNavbarAutoHide() {
  const [navbarVisible, setNavbarVisible] = useState(true);
  const hideTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const lastScrollY = useRef(0);

  useEffect(() => {
    const handleScroll = () => {
      const currentScrollY = window.scrollY;

      // Show navbar when at top or scrolling up
      if (currentScrollY < 100) {
        setNavbarVisible(true);
        resetHideTimer();
      } else if (currentScrollY < lastScrollY.current) {
        setNavbarVisible(true);
        resetHideTimer();
      }

      lastScrollY.current = currentScrollY;
    };

    const handleMouseMove = (e: MouseEvent) => {
      if (e.clientY < 80) {
        setNavbarVisible(true);
        resetHideTimer();
      }
    };

    const resetHideTimer = () => {
      if (hideTimeoutRef.current) {
        clearTimeout(hideTimeoutRef.current);
      }
      hideTimeoutRef.current = setTimeout(() => {
        if (window.scrollY > 100) {
          setNavbarVisible(false);
        }
      }, 3000);
    };

    // Initial hide timer
    resetHideTimer();

    window.addEventListener('scroll', handleScroll);
    window.addEventListener('mousemove', handleMouseMove);

    return () => {
      window.removeEventListener('scroll', handleScroll);
      window.removeEventListener('mousemove', handleMouseMove);
      if (hideTimeoutRef.current) {
        clearTimeout(hideTimeoutRef.current);
      }
    };
  }, []);

  return navbarVisible;
}
