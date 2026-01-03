-- LobAI Phase 2 Database Schema
-- Phase 2: BM 검증을 위한 추가 테이블
-- Created: 2026-01-02

-- 1. 출석 체크 테이블
CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    check_in_date DATE NOT NULL COMMENT '출석 날짜 (YYYY-MM-DD)',
    check_in_time TIMESTAMP NOT NULL COMMENT '출석 시각',

    -- 연속 출석
    streak_count INT NOT NULL DEFAULT 1 COMMENT '연속 출석 일수',

    -- 보상
    reward_points INT DEFAULT 0 COMMENT '출석 보상 포인트',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_user_date (user_id, check_in_date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_streak (user_id, streak_count DESC),
    INDEX idx_check_in_date (check_in_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='출석 체크 기록';

-- 2. AI 적응력 리포트 테이블
CREATE TABLE IF NOT EXISTS resilience_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',

    -- 리포트 메타데이터
    report_type ENUM('basic', 'premium') NOT NULL DEFAULT 'basic' COMMENT '리포트 유형',
    report_version VARCHAR(10) NOT NULL DEFAULT 'v1.0' COMMENT '리포트 버전',

    -- AI Readiness Score
    readiness_score DECIMAL(5,2) NOT NULL COMMENT 'AI 준비도 점수 (0-100)',
    readiness_level INT NOT NULL COMMENT 'AI 준비도 레벨 (1-5)',

    -- 4가지 핵심 분석 점수
    communication_score DECIMAL(5,2) NOT NULL COMMENT 'AI 친화 커뮤니케이션 지수 (0-100)',
    automation_risk_score DECIMAL(5,2) NOT NULL COMMENT '자동화 위험도 (0-100, 낮을수록 안전)',
    collaboration_score DECIMAL(5,2) NOT NULL COMMENT 'AI 협업 적합도 (0-100)',
    misuse_risk_score DECIMAL(5,2) NOT NULL COMMENT 'AI 오해/오용 가능성 (0-100, 낮을수록 안전)',

    -- 분석 결과 (JSON 형태)
    strengths TEXT COMMENT '강점 목록 (JSON array)',
    weaknesses TEXT COMMENT '취약점 목록 (JSON array)',
    simulation_result TEXT COMMENT 'AI 시대 포지션 시뮬레이션 (JSON)',
    detailed_feedback TEXT COMMENT '상세 피드백 (JSON)',

    -- 분석 기반 데이터
    analyzed_message_count INT NOT NULL COMMENT '분석된 메시지 수',
    analyzed_period_days INT NOT NULL COMMENT '분석 기간 (일)',
    analysis_start_date TIMESTAMP COMMENT '분석 기간 시작',
    analysis_end_date TIMESTAMP COMMENT '분석 기간 종료',

    -- 유료 기능 잠금
    is_unlocked BOOLEAN DEFAULT FALSE COMMENT '리포트 잠금 해제 여부',
    unlocked_at TIMESTAMP COMMENT '잠금 해제 시각',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_readiness_score (readiness_score DESC),
    INDEX idx_is_unlocked (is_unlocked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 적응력 리포트';

-- 3. 단건 구매 이력 테이블
CREATE TABLE IF NOT EXISTS purchase_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',

    -- 상품 정보
    product_type ENUM('resilience_report', 'coaching_pack', 'premium_analysis') NOT NULL,
    product_name VARCHAR(255) NOT NULL COMMENT '상품명',

    -- 결제 정보
    payment_status ENUM('pending', 'completed', 'failed', 'refunded') NOT NULL DEFAULT 'pending',
    payment_provider VARCHAR(50) COMMENT '결제 제공자',
    payment_transaction_id VARCHAR(255) COMMENT '결제 트랜잭션 ID',

    -- 금액
    amount DECIMAL(10,2) NOT NULL COMMENT '구매 금액',
    currency VARCHAR(3) DEFAULT 'KRW' COMMENT '통화',

    -- 연결된 리소스
    related_report_id BIGINT COMMENT '관련 리포트 ID',

    -- 메타데이터
    metadata TEXT COMMENT '추가 정보 (JSON)',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (related_report_id) REFERENCES resilience_reports(id) ON DELETE SET NULL,
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_payment_status (payment_status),
    INDEX idx_product_type (product_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='구매 이력';

-- 4. 사용자 구독 정보 테이블 (Phase 2-B용, 미리 생성)
CREATE TABLE IF NOT EXISTS user_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',

    -- 구독 정보
    plan_type ENUM('free', 'basic_monthly', 'premium_monthly') NOT NULL DEFAULT 'free',
    status ENUM('active', 'cancelled', 'expired', 'paused') NOT NULL DEFAULT 'active',

    -- 결제 정보
    payment_provider VARCHAR(50) COMMENT '결제 제공자',
    payment_customer_id VARCHAR(255) COMMENT '결제 시스템 고객 ID',
    payment_subscription_id VARCHAR(255) COMMENT '결제 시스템 구독 ID',

    -- 기간
    started_at TIMESTAMP NOT NULL COMMENT '구독 시작일',
    current_period_start TIMESTAMP COMMENT '현재 결제 주기 시작',
    current_period_end TIMESTAMP COMMENT '현재 결제 주기 종료',
    cancelled_at TIMESTAMP COMMENT '구독 취소일',

    -- 금액
    amount DECIMAL(10,2) COMMENT '구독 금액',
    currency VARCHAR(3) DEFAULT 'KRW' COMMENT '통화',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_status (user_id, status),
    INDEX idx_status (status),
    INDEX idx_current_period_end (current_period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 구독 정보';

-- 5. 기존 users 테이블에 컬럼 추가
-- Note: IF NOT EXISTS is not supported in ALTER TABLE, so this may fail if columns already exist
-- Run this manually if needed or check for column existence first
ALTER TABLE users
ADD COLUMN subscription_tier ENUM('free', 'basic', 'premium') NOT NULL DEFAULT 'free' COMMENT '구독 등급' AFTER role,
ADD COLUMN total_attendance_days INT DEFAULT 0 COMMENT '누적 출석 일수' AFTER subscription_tier,
ADD COLUMN max_streak_days INT DEFAULT 0 COMMENT '최대 연속 출석 일수' AFTER total_attendance_days,
ADD COLUMN last_attendance_date DATE COMMENT '마지막 출석 날짜' AFTER max_streak_days;
