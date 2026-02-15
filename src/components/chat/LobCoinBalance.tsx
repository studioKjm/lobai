import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Coins, TrendingUp, TrendingDown, DollarSign, RotateCw, ShoppingBag } from 'lucide-react';
import { lobcoinApi, type LobCoinBalance as LobCoinBalanceType } from '@/lib/lobcoinApi';
import toast from 'react-hot-toast';

export function LobCoinBalance() {
  const navigate = useNavigate();
  const [balance, setBalance] = useState<LobCoinBalanceType | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const [showDetails, setShowDetails] = useState(false);

  const fetchBalance = async () => {
    try {
      setHasError(false);
      const data = await lobcoinApi.getBalance();
      setBalance(data);
    } catch (error: any) {
      console.error('Failed to load balance:', error);
      setHasError(true);
      // Keep previous balance if available (don't set to null)
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

  if (!balance && !hasError) return null;

  if (!balance && hasError) {
    return (
      <div className="bg-gradient-to-r from-red-500/10 to-orange-500/10 backdrop-blur-sm
                      border border-red-500/30 rounded-lg px-4 py-2.5">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 text-xs text-red-400/80">
            <Coins className="w-4 h-4" />
            <span>잔액 로딩 실패</span>
          </div>
          <button
            onClick={fetchBalance}
            className="text-xs text-red-400 hover:text-red-300 transition-colors px-2 py-1 rounded hover:bg-white/5"
          >
            재시도
          </button>
        </div>
      </div>
    );
  }

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

        {/* Right: Refresh + Shop + USD Value */}
        <div className="flex items-center gap-2">
          <button
            onClick={(e) => {
              e.stopPropagation();
              fetchBalance();
            }}
            className="text-gray-500 hover:text-gray-300 transition-all p-1.5 rounded hover:bg-white/5 hover:rotate-180"
            title="새로고침"
          >
            <RotateCw className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={(e) => {
              e.stopPropagation();
              navigate('/shop');
            }}
            className="text-yellow-500 hover:text-yellow-400 transition-all p-1.5 rounded hover:bg-white/5"
            title="상점 이동"
          >
            <ShoppingBag className="w-3.5 h-3.5" />
          </button>
          <div className="flex items-center gap-1 text-xs text-gray-400">
            <DollarSign className="w-3.5 h-3.5" />
            <span>${balance.valueInUsd.toFixed(2)}</span>
          </div>
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
