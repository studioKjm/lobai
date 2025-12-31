import { useEffect } from 'react';
import type { Stats } from '@/types';

/**
 * Custom hook for auto-decaying stats over time
 * Decreases stats every 5 seconds:
 * - Hunger: -0.5%
 * - Energy: -0.3%
 * - Happiness: -0.4%
 * Stats are clamped to minimum of 0
 */
export function useStatsDecay(
  setStats: React.Dispatch<React.SetStateAction<Stats>>
) {
  useEffect(() => {
    const timer = setInterval(() => {
      setStats(prev => ({
        hunger: Math.max(0, prev.hunger - 0.5),
        energy: Math.max(0, prev.energy - 0.3),
        happiness: Math.max(0, prev.happiness - 0.4)
      }));
    }, 5000);

    return () => clearInterval(timer);
  }, [setStats]);
}
