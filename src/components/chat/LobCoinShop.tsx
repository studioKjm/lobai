import { useState, useEffect } from 'react';
import { Coins, ShoppingBag, Gift, Ticket, X, Check } from 'lucide-react';
import { lobcoinApi, type Coupon, type IssuedCoupon } from '@/lib/lobcoinApi';
import toast from 'react-hot-toast';

export function LobCoinShop() {
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [myCoupons, setMyCoupons] = useState<IssuedCoupon[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'shop' | 'my'>('shop');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedCoupon, setSelectedCoupon] = useState<Coupon | null>(null);

  const fetchCoupons = async () => {
    try {
      const [availableCoupons, purchasedCoupons] = await Promise.all([
        lobcoinApi.getCoupons(),
        lobcoinApi.getMyCoupons(),
      ]);
      setCoupons(availableCoupons);
      setMyCoupons(purchasedCoupons);
    } catch (error) {
      console.error('Failed to load coupons:', error);
      toast.error('쿠폰 목록 로드 실패');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCoupons();
  }, []);

  const handlePurchase = async (coupon: Coupon) => {
    if (!coupon.isAffordable) {
      toast.error('잔액이 부족합니다');
      return;
    }

    if (!coupon.isAvailable) {
      toast.error('재고가 부족합니다');
      return;
    }

    try {
      const issued = await lobcoinApi.purchaseCoupon(coupon.id);
      toast.success(`쿠폰 구매 성공! 코드: ${issued.couponCode}`);
      setIsModalOpen(false);
      fetchCoupons();
    } catch (error: any) {
      const message = error.response?.data?.message || '쿠폰 구매 실패';
      toast.error(message);
    }
  };

  const getPartnerIcon = (partnerName: string) => {
    const icons: Record<string, string> = {
      NOTION: '📝',
      NETFLIX: '🎬',
      STARBUCKS: '☕',
      UDEMY: '🎓',
      LOBAI: '🤖',
    };
    return icons[partnerName] || '🎁';
  };

  return (
    <>
      <div className="bg-gray-800/50 backdrop-blur-sm rounded-xl p-6">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold flex items-center gap-2">
            <ShoppingBag className="w-6 h-6 text-purple-400" />
            LobCoin 상점
          </h2>
        </div>

        {/* Tabs */}
        <div className="flex gap-2 mb-6">
          <button
            onClick={() => setActiveTab('shop')}
            className={`flex-1 py-2 px-4 rounded-lg font-semibold transition-colors ${
              activeTab === 'shop'
                ? 'bg-purple-600 text-white'
                : 'bg-gray-700 text-gray-400 hover:bg-gray-600'
            }`}
          >
            🛒 상점
          </button>
          <button
            onClick={() => setActiveTab('my')}
            className={`flex-1 py-2 px-4 rounded-lg font-semibold transition-colors ${
              activeTab === 'my'
                ? 'bg-purple-600 text-white'
                : 'bg-gray-700 text-gray-400 hover:bg-gray-600'
            }`}
          >
            🎫 내 쿠폰
          </button>
        </div>

        {/* Content */}
        {isLoading ? (
          <div className="text-center py-12 text-gray-400">로딩 중...</div>
        ) : activeTab === 'shop' ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {coupons.map((coupon) => (
              <div
                key={coupon.id}
                className={`bg-gray-700/50 rounded-lg p-4 border ${
                  coupon.isAvailable
                    ? 'border-gray-600 hover:border-purple-500'
                    : 'border-gray-700 opacity-60'
                } transition-all cursor-pointer`}
                onClick={() => {
                  setSelectedCoupon(coupon);
                  setIsModalOpen(true);
                }}
              >
                <div className="flex items-start justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <span className="text-2xl">{getPartnerIcon(coupon.partnerName)}</span>
                    <div>
                      <h3 className="font-bold text-sm">{coupon.title}</h3>
                      <p className="text-xs text-gray-400">{coupon.partnerName}</p>
                    </div>
                  </div>
                </div>

                <p className="text-xs text-gray-400 mb-3 line-clamp-2">{coupon.description}</p>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-1">
                    <Coins className="w-4 h-4 text-yellow-400" />
                    <span className="font-bold text-yellow-400">{coupon.costLobcoin}</span>
                  </div>

                  <div className="flex items-center gap-2">
                    {coupon.realValueUsd && (
                      <span className="text-xs text-green-400">${coupon.realValueUsd}</span>
                    )}
                    {!coupon.isAffordable && (
                      <span className="text-xs bg-red-500/20 text-red-400 px-2 py-1 rounded">
                        잔액 부족
                      </span>
                    )}
                    {coupon.isAffordable && !coupon.isAvailable && (
                      <span className="text-xs bg-gray-500/20 text-gray-400 px-2 py-1 rounded">
                        품절
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="space-y-3">
            {myCoupons.length === 0 ? (
              <div className="text-center py-12 text-gray-400">
                구매한 쿠폰이 없습니다
              </div>
            ) : (
              myCoupons.map((coupon) => (
                <div
                  key={coupon.id}
                  className="bg-gray-700/50 rounded-lg p-4 border border-gray-600"
                >
                  <div className="flex items-start justify-between mb-2">
                    <div>
                      <h3 className="font-bold">{coupon.couponTitle}</h3>
                      <p className="text-sm text-gray-400">{coupon.partnerName}</p>
                    </div>
                    <span
                      className={`text-xs px-2 py-1 rounded ${
                        coupon.status === 'ISSUED'
                          ? 'bg-green-500/20 text-green-400'
                          : coupon.status === 'USED'
                          ? 'bg-gray-500/20 text-gray-400'
                          : 'bg-red-500/20 text-red-400'
                      }`}
                    >
                      {coupon.status === 'ISSUED' ? '사용 가능' : coupon.status}
                    </span>
                  </div>

                  <div className="bg-gray-800/50 rounded p-2 mb-2">
                    <p className="text-xs text-gray-500 mb-1">쿠폰 코드</p>
                    <p className="font-mono font-bold text-sm">{coupon.couponCode}</p>
                  </div>

                  <div className="flex items-center justify-between text-xs text-gray-500">
                    <span>발급일: {new Date(coupon.issuedAt).toLocaleDateString('ko-KR')}</span>
                    {coupon.expiresAt && (
                      <span>만료일: {new Date(coupon.expiresAt).toLocaleDateString('ko-KR')}</span>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        )}
      </div>

      {/* Purchase Modal */}
      {isModalOpen && selectedCoupon && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4"
             onClick={() => setIsModalOpen(false)}>
          <div className="bg-gray-800 rounded-xl p-6 max-w-md w-full" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-start justify-between mb-4">
              <h3 className="text-xl font-bold">쿠폰 구매</h3>
              <button
                onClick={() => setIsModalOpen(false)}
                className="text-gray-400 hover:text-white transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-4">
              <div className="flex items-center gap-3">
                <span className="text-3xl">{getPartnerIcon(selectedCoupon.partnerName)}</span>
                <div>
                  <h4 className="font-bold">{selectedCoupon.title}</h4>
                  <p className="text-sm text-gray-400">{selectedCoupon.partnerName}</p>
                </div>
              </div>

              <p className="text-sm text-gray-300">{selectedCoupon.description}</p>

              {selectedCoupon.terms && (
                <div className="bg-gray-700/50 rounded p-3">
                  <p className="text-xs text-gray-400 mb-1">이용 조건</p>
                  <p className="text-xs text-gray-300">{selectedCoupon.terms}</p>
                </div>
              )}

              <div className="flex items-center justify-between py-3 border-t border-gray-700">
                <span className="text-gray-400">가격</span>
                <div className="flex items-center gap-2">
                  <Coins className="w-5 h-5 text-yellow-400" />
                  <span className="text-xl font-bold text-yellow-400">
                    {selectedCoupon.costLobcoin}
                  </span>
                </div>
              </div>

              {selectedCoupon.realValueUsd && (
                <div className="flex items-center justify-between text-sm">
                  <span className="text-gray-400">실제 가치</span>
                  <span className="text-green-400">${selectedCoupon.realValueUsd}</span>
                </div>
              )}

              <button
                onClick={() => handlePurchase(selectedCoupon)}
                disabled={!selectedCoupon.isAffordable || !selectedCoupon.isAvailable}
                className="w-full py-3 px-4 bg-purple-600 hover:bg-purple-700 disabled:bg-gray-600
                           disabled:cursor-not-allowed rounded-lg font-semibold transition-colors
                           flex items-center justify-center gap-2"
              >
                {selectedCoupon.isAffordable && selectedCoupon.isAvailable ? (
                  <>
                    <Check className="w-5 h-5" />
                    구매하기
                  </>
                ) : !selectedCoupon.isAffordable ? (
                  '잔액 부족'
                ) : (
                  '재고 부족'
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
