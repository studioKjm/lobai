import { useState, useEffect } from 'react';
import { Coins, TrendingUp, TrendingDown, DollarSign } from 'lucide-react';
import { lobcoinApi, type LobCoinBalance as LobCoinBalanceType } from '@/lib/lobcoinApi';
import toast from 'react-hot-toast';

export function LobCoinBalance() {
  const [balance, setBalance] = useState<LobCoinBalanceType | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [showDetails, setShowDetails] = useState(false);

  const fetchBalance = async () => {
    try {
      const data = await lobcoinApi.getBalance();
      setBalance(data);
    } catch (error: any) {
      console.error('Failed to load balance:', error);
      // Don't show error toast - user will be redirected to login by interceptor
      // Just set balance to null to hide the component
      setBalance(null);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchBalance();
    // Refresh balance every 30 seconds
    const interval = setInterval(fetchBalance, 30000);
    return () => clearInterval(interval);
  }, []);

  if (isLoading) {
    return (
      <div className="bg-gradient-to-r from-yellow-500/10 to-orange-500/10 backdrop-blur-sm
                      border border-yellow-500/30 rounded-xl p-4 animate-pulse">
        <div className="h-8 bg-gray-700/50 rounded w-32"></div>
      </div>
    );
  }

  if (!balance) return null;

  return (
    <div className="bg-gradient-to-r from-yellow-500/10 to-orange-500/10 backdrop-blur-sm
                    border border-yellow-500/30 rounded-lg px-4 py-2.5 hover:border-yellow-500/50
                    transition-all cursor-pointer"
         onClick={() => setShowDetails(!showDetails)}>
      {/* Compact Horizontal Layout */}
      <div className="flex items-center justify-between gap-4">
        {/* Left: Icon + Balance */}
        <div className="flex items-center gap-3">
          <Coins className="w-5 h-5 text-yellow-400 animate-pulse flex-shrink-0" />
          <div className="flex items-baseline gap-2">
            <span className="text-2xl font-bold text-yellow-400">
              {balance.balance.toLocaleString()}
            </span>
            <span className="text-xs text-gray-400">코인</span>
          </div>
        </div>

        {/* Right: USD Value + Refresh */}
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-1 text-xs text-gray-400">
            <DollarSign className="w-3.5 h-3.5" />
            <span>${balance.valueInUsd.toFixed(2)}</span>
          </div>
          <button
            onClick={(e) => {
              e.stopPropagation();
              fetchBalance();
            }}
            className="text-xs text-gray-500 hover:text-gray-300 transition-colors px-2 py-1 rounded hover:bg-white/5"
          >
            새로고침
          </button>
        </div>
      </div>

      {/* Details (Expandable) */}
      {showDetails && (
        <div className="mt-3 pt-3 border-t border-yellow-500/20 grid grid-cols-2 gap-3 animate-fadeIn">
          <div className="flex items-center justify-between text-xs">
            <div className="flex items-center gap-1.5 text-green-400">
              <TrendingUp className="w-3.5 h-3.5" />
              <span>총 획득</span>
            </div>
            <span className="font-semibold">{balance.totalEarned.toLocaleString()}</span>
          </div>

          <div className="flex items-center justify-between text-xs">
            <div className="flex items-center gap-1.5 text-red-400">
              <TrendingDown className="w-3.5 h-3.5" />
              <span>총 사용</span>
            </div>
            <span className="font-semibold">{balance.totalSpent.toLocaleString()}</span>
          </div>
        </div>
      )}
    </div>
  );
}
