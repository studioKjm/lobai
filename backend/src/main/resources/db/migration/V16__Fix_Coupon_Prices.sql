-- Fix coupon prices to match 10 LobCoin = $0.1 USD ratio (100 LobCoin = $1)
-- =====================================================

-- 스타벅스 $5 기프트카드: 800 코인 → 50 코인
UPDATE partner_coupons
SET cost_lobcoin = 50
WHERE partner_name = 'STARBUCKS' AND title = '스타벅스 $5 기프트카드';

-- Udemy AI 강의 30% 할인: 1000 코인 → 200 코인
UPDATE partner_coupons
SET cost_lobcoin = 200
WHERE partner_name = 'UDEMY' AND title = 'Udemy AI 강의 30% 할인';

-- LobAI Pro 구독 1개월 무료: 7500 코인 → 150 코인
UPDATE partner_coupons
SET cost_lobcoin = 150
WHERE partner_name = 'LOBAI' AND title = 'LobAI Pro 구독 1개월 무료';

-- Note: Notion and Netflix coupons remain at 0 LobCoin (level rewards)
