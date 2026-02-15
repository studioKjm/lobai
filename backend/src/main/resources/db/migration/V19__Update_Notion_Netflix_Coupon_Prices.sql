-- Notion, Netflix 쿠폰 코인 가격 설정
-- 기존 0 (레벨 보상 무료) → 코인 구매 가능하도록 가격 부여

UPDATE partner_coupons
SET cost_lobcoin = 1000
WHERE partner_name = 'NOTION' AND title = 'Notion Plus 1개월 무료';

UPDATE partner_coupons
SET cost_lobcoin = 1500
WHERE partner_name = 'NETFLIX' AND title = 'Netflix 스탠다드 1개월 무료';
