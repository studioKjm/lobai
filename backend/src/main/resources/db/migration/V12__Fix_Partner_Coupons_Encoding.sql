-- ================================================
-- V12: Fix Partner Coupons Korean Encoding
-- ================================================
-- 한글 깨짐 문제 해결: 기존 데이터 삭제 후 올바른 인코딩으로 재삽입
-- ================================================

-- 1. 기존 쿠폰 데이터 삭제
DELETE FROM partner_coupons;

-- 2. 올바른 한글로 쿠폰 데이터 재삽입

-- Notion 1개월 무료 (Level 3 보상용)
INSERT INTO partner_coupons (partner_name, coupon_type, cost_lobcoin, real_value_usd, title, description, terms, is_active, display_order)
VALUES (
    'NOTION',
    'FREE_TRIAL',
    0,  -- 레벨 보상으로 무료 지급
    10.00,
    'Notion Plus 1개월 무료',
    'Notion Plus 요금제를 1개월 동안 무료로 사용하세요',
    '레벨 3 달성 시 자동 지급. 신규 가입자 또는 무료 플랜 사용자만 사용 가능.',
    TRUE,
    10
);

-- 스타벅스 $5 쿠폰
INSERT INTO partner_coupons (partner_name, coupon_type, cost_lobcoin, real_value_usd, title, description, terms, is_active, display_order)
VALUES (
    'STARBUCKS',
    'GIFT_CARD',
    800,
    5.00,
    '스타벅스 $5 기프트카드',
    '스타벅스에서 사용 가능한 $5 기프트카드',
    '구매 후 이메일로 쿠폰 코드 발송. 유효기간 3개월.',
    TRUE,
    20
);

-- Udemy 30% 할인
INSERT INTO partner_coupons (partner_name, coupon_type, cost_lobcoin, real_value_usd, title, description, terms, is_active, display_order)
VALUES (
    'UDEMY',
    'DISCOUNT',
    1000,
    20.00,
    'Udemy AI 강의 30% 할인',
    'Udemy의 모든 AI 관련 강의를 30% 할인된 가격에 수강하세요',
    '쿠폰 코드를 Udemy 결제 시 입력. 유효기간 1개월.',
    TRUE,
    30
);

-- Netflix 1개월 무료 (Level 4 보상용)
INSERT INTO partner_coupons (partner_name, coupon_type, cost_lobcoin, real_value_usd, title, description, terms, is_active, display_order)
VALUES (
    'NETFLIX',
    'FREE_TRIAL',
    0,  -- 레벨 보상으로 무료 지급
    15.00,
    'Netflix 스탠다드 1개월 무료',
    'Netflix 스탠다드 요금제를 1개월 동안 무료로 시청하세요',
    '레벨 4 달성 시 자동 지급. 신규 가입자 또는 해지 후 30일 이상 경과한 사용자만 사용 가능.',
    TRUE,
    40
);

-- LobAI Pro 구독 1개월 무료
INSERT INTO partner_coupons (partner_name, coupon_type, cost_lobcoin, real_value_usd, title, description, terms, is_active, display_order)
VALUES (
    'LOBAI',
    'SUBSCRIPTION_DISCOUNT',
    7500,
    15.00,
    'LobAI Pro 구독 1개월 무료',
    'LobAI Pro 요금제를 1개월 동안 무료로 사용하세요',
    '자동 갱신 없음. 1개월 후 자동 해지.',
    TRUE,
    50
);

-- 완료 메시지
SELECT '✅ V12 Migration: Partner Coupons Encoding Fixed' AS Status;
