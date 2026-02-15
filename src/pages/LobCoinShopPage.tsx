import { Navbar } from '@/components/common/Navbar';
import { LobCoinShop } from '@/components/chat/LobCoinShop';
import { LobCoinBalance } from '@/components/chat/LobCoinBalance';

export function LobCoinShopPage() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      <Navbar />

      <div className="max-w-6xl mx-auto px-4 py-24">
        {/* Page Header with Balance */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold mb-6 bg-gradient-to-r from-yellow-400 to-orange-400 bg-clip-text text-transparent">
            LobCoin 상점
          </h1>
          <LobCoinBalance />
        </div>

        {/* Shop Component */}
        <LobCoinShop />
      </div>
    </div>
  );
}
