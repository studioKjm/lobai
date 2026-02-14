-- LobCoin 시스템 테이블 생성
-- Phase 1: Foundation

-- =====================================================
-- 1. LobCoin 거래 내역
-- =====================================================
CREATE TABLE IF NOT EXISTS lobcoin_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    amount INT NOT NULL COMMENT '획득(+) 또는 사용(-) 금액',
    balance_after INT NOT NULL COMMENT '거래 후 잔액',
    type ENUM('EARN', 'SPEND') NOT NULL COMMENT '거래 유형',
    source VARCHAR(50) NOT NULL COMMENT '획득/사용 경로: CHECK_IN, MISSION_COMPLETE, PARTNER_COUPON, etc.',
    description VARCHAR(255) COMMENT '거래 설명',
    metadata JSON COMMENT '추가 정보 (JSON)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_lobcoin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_type_source (type, source),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LobCoin 거래 내역';

-- =====================================================
-- 2. LobCoin 잔액 (캐시 테이블)
-- =====================================================
CREATE TABLE IF NOT EXISTS lobcoin_balances (
    user_id BIGINT PRIMARY KEY,
    balance INT NOT NULL DEFAULT 0 COMMENT '현재 잔액',
    total_earned INT NOT NULL DEFAULT 0 COMMENT '총 획득량 (누적)',
    total_spent INT NOT NULL DEFAULT 0 COMMENT '총 사용량 (누적)',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_balance_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_balance_positive CHECK (balance >= 0),
    INDEX idx_balance (balance DESC),
    INDEX idx_total_earned (total_earned DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LobCoin 잔액 캐시';

-- =====================================================
-- 3. 파트너 쿠폰 카탈로그
-- =====================================================
CREATE TABLE IF NOT EXISTS partner_coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    partner_name VARCHAR(100) NOT NULL COMMENT '파트너 이름 (Notion, Netflix, Starbucks, etc.)',
    coupon_type VARCHAR(50) NOT NULL COMMENT '쿠폰 유형 (DISCOUNT, FREE_TRIAL, GIFT_CARD, etc.)',
    cost_lobcoin INT NOT NULL COMMENT 'LobCoin 비용',
    real_value_usd DECIMAL(10,2) COMMENT '실제 가치 (USD)',
    title VARCHAR(200) NOT NULL COMMENT '쿠폰 제목',
    description TEXT COMMENT '쿠폰 설명',
    terms TEXT COMMENT '이용 조건 및 약관',
    image_url VARCHAR(500) COMMENT '이미지 URL',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '판매 중 여부',
    stock INT COMMENT '재고 (NULL = 무제한)',
    display_order INT NOT NULL DEFAULT 0 COMMENT '표시 순서',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_partner (partner_name),
    INDEX idx_active_cost (is_active, cost_lobcoin),
    INDEX idx_display_order (display_order, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파트너 쿠폰 카탈로그';

-- =====================================================
-- 4. 쿠폰 발급 내역
-- =====================================================
CREATE TABLE IF NOT EXISTS coupon_issuances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    coupon_code VARCHAR(50) UNIQUE NOT NULL COMMENT '실제 쿠폰 코드 (사용자에게 제공)',
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP COMMENT '사용 일시',
    expires_at TIMESTAMP COMMENT '만료 일시',
    status ENUM('ISSUED', 'USED', 'EXPIRED', 'REVOKED') NOT NULL DEFAULT 'ISSUED' COMMENT '쿠폰 상태',

    CONSTRAINT fk_issuance_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_issuance_coupon FOREIGN KEY (coupon_id) REFERENCES partner_coupons(id),
    INDEX idx_user_status (user_id, status),
    INDEX idx_expires (expires_at),
    INDEX idx_coupon_code (coupon_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='쿠폰 발급 내역';

-- =====================================================
-- 5. 레벨 보상 이력
-- =====================================================
CREATE TABLE IF NOT EXISTS level_rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    level INT NOT NULL COMMENT '달성 레벨 (1-5)',
    reward_type VARCHAR(50) NOT NULL COMMENT '보상 유형 (LOBCOIN, COUPON, BADGE, etc.)',
    reward_data JSON COMMENT '보상 상세 정보 (JSON)',
    claimed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reward_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_level_type (user_id, level, reward_type),
    INDEX idx_user_level (user_id, level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='레벨 보상 이력';

-- =====================================================
-- 6. 추천 시스템 (기존 users 테이블 확장)
-- =====================================================
ALTER TABLE users ADD COLUMN IF NOT EXISTS referral_code VARCHAR(10) UNIQUE COMMENT '추천 코드 (8자리)';
ALTER TABLE users ADD COLUMN IF NOT EXISTS referred_by_user_id BIGINT COMMENT '추천인 ID';
ALTER TABLE users ADD COLUMN IF NOT EXISTS referral_count INT NOT NULL DEFAULT 0 COMMENT '추천한 유저 수';
ALTER TABLE users ADD INDEX IF NOT EXISTS idx_referral_code (referral_code);
ALTER TABLE users ADD INDEX IF NOT EXISTS idx_referred_by (referred_by_user_id);

-- =====================================================
-- 7. 초기 파트너 쿠폰 데이터 삽입 (Phase 1 MVP)
-- =====================================================

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

-- =====================================================
-- 8. 기존 유저에게 초기 LobCoin 지급 (Welcome Bonus)
-- =====================================================

-- 모든 기존 유저에게 100 LobCoin 지급 (환영 보너스)
INSERT INTO lobcoin_balances (user_id, balance, total_earned, total_spent)
SELECT
    id,
    100,
    100,
    0
FROM users
WHERE NOT EXISTS (
    SELECT 1 FROM lobcoin_balances WHERE user_id = users.id
);

-- 거래 내역 기록
INSERT INTO lobcoin_transactions (user_id, amount, balance_after, type, source, description)
SELECT
    id,
    100,
    100,
    'EARN',
    'WELCOME_BONUS',
    'LobCoin 시스템 런칭 환영 보너스'
FROM users
WHERE NOT EXISTS (
    SELECT 1 FROM lobcoin_transactions WHERE user_id = users.id AND source = 'WELCOME_BONUS'
);

-- =====================================================
-- 마이그레이션 완료
-- =====================================================
