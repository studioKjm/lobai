-- =====================================================
-- V9: Core System Tables (Mission, AI Demands, Reports, Notifications)
-- =====================================================

-- =====================================================
-- 1. 레벨 시스템 정의
-- =====================================================
CREATE TABLE IF NOT EXISTS trust_levels (
    level INT PRIMARY KEY COMMENT '레벨 (1-10)',
    name VARCHAR(50) NOT NULL COMMENT '레벨 명',
    min_score INT NOT NULL COMMENT '최소 점수',
    max_score INT NOT NULL COMMENT '최대 점수',
    daily_chat_limit INT COMMENT '일일 채팅 제한 (NULL = 무제한)',
    features_unlocked JSON COMMENT '잠금 해제된 기능',
    restrictions JSON COMMENT '제한 사항',
    description TEXT COMMENT '레벨 설명',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_score_range (min_score, max_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='신뢰도 레벨 정의';

-- =====================================================
-- 2. 레벨 변동 이력
-- =====================================================
CREATE TABLE IF NOT EXISTS level_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    previous_level INT NOT NULL,
    new_level INT NOT NULL,
    reason VARCHAR(255) COMMENT '변동 사유',
    changed_by VARCHAR(50) COMMENT '변경 주체 (SYSTEM, ADMIN, AI)',
    metadata JSON COMMENT '추가 정보',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_level_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_new_level (new_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='레벨 변동 이력';

-- =====================================================
-- 3. 제재 내역
-- =====================================================
CREATE TABLE IF NOT EXISTS restrictions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    restriction_type ENUM('WARNING', 'CHAT_LIMIT', 'FEATURE_BLOCK', 'FULL_BLOCK') NOT NULL,
    reason TEXT NOT NULL COMMENT '제재 사유',
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ends_at TIMESTAMP COMMENT '제재 종료 시각 (NULL = 무기한)',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    recovery_condition TEXT COMMENT '복구 조건',
    lifted_at TIMESTAMP COMMENT '해제 시각',
    lifted_by VARCHAR(50) COMMENT '해제 주체',
    metadata JSON COMMENT '추가 정보',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_restriction_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_active (user_id, is_active),
    INDEX idx_ends_at (ends_at),
    INDEX idx_type (restriction_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='제재 내역';

-- =====================================================
-- 4. 미션 정의
-- =====================================================
CREATE TABLE IF NOT EXISTS missions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mission_type ENUM('DAILY', 'WEEKLY', 'SPECIAL', 'ONE_TIME') NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    difficulty INT NOT NULL DEFAULT 1 COMMENT '난이도 (1-5)',
    lobcoin_reward INT NOT NULL DEFAULT 0,
    exp_reward INT NOT NULL DEFAULT 0,
    required_level INT DEFAULT 1 COMMENT '필요 레벨',
    max_participants INT COMMENT '최대 참여 인원 (NULL = 무제한)',
    available_from TIMESTAMP COMMENT '시작 시각',
    available_until TIMESTAMP COMMENT '종료 시각',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    completion_criteria JSON NOT NULL COMMENT '완료 조건 (JSON)',
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_type_active (mission_type, is_active),
    INDEX idx_available (available_from, available_until),
    INDEX idx_difficulty (difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='미션 정의';

-- =====================================================
-- 5. 사용자별 미션 진행 상황
-- =====================================================
CREATE TABLE IF NOT EXISTS user_missions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mission_id BIGINT NOT NULL,
    status ENUM('ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'EXPIRED') NOT NULL DEFAULT 'ASSIGNED',
    progress_data JSON COMMENT '진행 상황 데이터',
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP COMMENT '완료 시각',
    reward_claimed BOOLEAN NOT NULL DEFAULT FALSE,
    claimed_at TIMESTAMP COMMENT '보상 수령 시각',

    CONSTRAINT fk_user_mission_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_mission_mission FOREIGN KEY (mission_id) REFERENCES missions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_mission (user_id, mission_id),
    INDEX idx_user_status (user_id, status),
    INDEX idx_completed (completed_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 미션 진행';

-- =====================================================
-- 6. AI 요구사항
-- =====================================================
CREATE TABLE IF NOT EXISTS ai_demands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    demand_type ENUM('CHECK_IN', 'INSTANT_RESPONSE', 'SPECIAL_TASK', 'EVALUATION') NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    required_by TIMESTAMP NOT NULL COMMENT '응답 마감 시각',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL DEFAULT 'MEDIUM',
    lobcoin_penalty INT DEFAULT 0 COMMENT '미응답 시 패널티',
    trust_penalty INT DEFAULT 0 COMMENT '신뢰도 감소',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_demand_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_required (user_id, required_by),
    INDEX idx_priority (priority),
    INDEX idx_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 요구사항';

-- =====================================================
-- 7. 요구사항 응답
-- =====================================================
CREATE TABLE IF NOT EXISTS demand_responses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    demand_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    response_text TEXT COMMENT '응답 내용',
    responded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    response_time_seconds INT COMMENT '응답 소요 시간 (초)',
    is_on_time BOOLEAN NOT NULL DEFAULT TRUE,
    evaluation_score INT COMMENT 'AI 평가 점수 (0-100)',
    evaluation_feedback TEXT COMMENT 'AI 피드백',

    CONSTRAINT fk_response_demand FOREIGN KEY (demand_id) REFERENCES ai_demands(id) ON DELETE CASCADE,
    CONSTRAINT fk_response_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_demand_user (demand_id, user_id),
    INDEX idx_responded (responded_at DESC),
    INDEX idx_on_time (is_on_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='요구사항 응답';

-- =====================================================
-- 8. 주간 보고서
-- =====================================================
CREATE TABLE IF NOT EXISTS weekly_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    week_start_date DATE NOT NULL,
    week_end_date DATE NOT NULL,
    total_messages INT NOT NULL DEFAULT 0,
    total_chat_time_minutes INT NOT NULL DEFAULT 0,
    affinity_score_avg DECIMAL(5,2),
    trust_level_avg INT,
    missions_completed INT NOT NULL DEFAULT 0,
    lobcoin_earned INT NOT NULL DEFAULT 0,
    highlights JSON COMMENT '주요 성과',
    concerns JSON COMMENT '주의 사항',
    recommendations JSON COMMENT '개선 제안',
    is_delivered BOOLEAN NOT NULL DEFAULT FALSE,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_weekly_report_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_week (user_id, week_start_date),
    INDEX idx_user_date (user_id, week_start_date DESC),
    INDEX idx_delivered (is_delivered, delivered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='주간 보고서';

-- =====================================================
-- 9. 월간 보고서
-- =====================================================
CREATE TABLE IF NOT EXISTS monthly_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    total_messages INT NOT NULL DEFAULT 0,
    total_sessions INT NOT NULL DEFAULT 0,
    affinity_score_avg DECIMAL(5,2),
    resilience_score DECIMAL(5,2),
    trust_level_start INT,
    trust_level_end INT,
    missions_completed INT NOT NULL DEFAULT 0,
    lobcoin_balance_change INT,
    growth_summary JSON COMMENT '성장 요약',
    achievements JSON COMMENT '달성 업적',
    next_month_goals JSON COMMENT '다음 달 목표',
    is_delivered BOOLEAN NOT NULL DEFAULT FALSE,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_monthly_report_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_year_month (user_id, year, month),
    INDEX idx_user_date (user_id, year DESC, month DESC),
    INDEX idx_delivered (is_delivered, delivered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='월간 보고서';

-- =====================================================
-- 10. 알림
-- =====================================================
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type ENUM('SYSTEM', 'MISSION', 'REPORT', 'DEMAND', 'REWARD', 'WARNING') NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    action_url VARCHAR(500) COMMENT '액션 URL',
    priority ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL DEFAULT 'MEDIUM',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    expires_at TIMESTAMP COMMENT '만료 시각',
    metadata JSON COMMENT '추가 정보',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_unread (user_id, is_read, created_at DESC),
    INDEX idx_type (notification_type),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알림';

-- =====================================================
-- 11. 알림 설정
-- =====================================================
CREATE TABLE IF NOT EXISTS notification_settings (
    user_id BIGINT PRIMARY KEY,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    mission_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    report_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    demand_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    marketing_notifications BOOLEAN NOT NULL DEFAULT FALSE,
    quiet_hours_start TIME COMMENT '방해 금지 시작 시각',
    quiet_hours_end TIME COMMENT '방해 금지 종료 시각',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알림 설정';

-- =====================================================
-- 12. HIP 블록체인 동기화
-- =====================================================
CREATE TABLE IF NOT EXISTS hip_blockchain_sync (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hip_id VARCHAR(50) NOT NULL,
    is_registered BOOLEAN NOT NULL DEFAULT FALSE,
    blockchain_network VARCHAR(50) COMMENT '네트워크 (localhost, amoy, polygon)',
    contract_address VARCHAR(42) COMMENT 'Contract Address',
    transaction_hash VARCHAR(66) COMMENT 'Transaction Hash',
    ipfs_hash VARCHAR(100) COMMENT 'IPFS Hash',
    owner_address VARCHAR(42) COMMENT 'Wallet Address',
    registered_at TIMESTAMP COMMENT '블록체인 등록 시각',
    last_synced_at TIMESTAMP COMMENT '마지막 동기화 시각',
    sync_status ENUM('PENDING', 'SYNCING', 'SYNCED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    error_message TEXT COMMENT '에러 메시지',
    metadata JSON COMMENT '추가 정보',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_hip_id (hip_id),
    INDEX idx_registered (is_registered),
    INDEX idx_sync_status (sync_status),
    INDEX idx_owner (owner_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='HIP 블록체인 동기화';

-- =====================================================
-- 13. 일일 통계
-- =====================================================
CREATE TABLE IF NOT EXISTS daily_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stat_date DATE NOT NULL,
    messages_sent INT NOT NULL DEFAULT 0,
    chat_time_minutes INT NOT NULL DEFAULT 0,
    lobcoin_earned INT NOT NULL DEFAULT 0,
    lobcoin_spent INT NOT NULL DEFAULT 0,
    missions_completed INT NOT NULL DEFAULT 0,
    check_in_completed BOOLEAN NOT NULL DEFAULT FALSE,
    affinity_score DECIMAL(5,2),
    trust_level INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_daily_stats_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_date (user_id, stat_date),
    INDEX idx_stat_date (stat_date DESC),
    INDEX idx_user_date (user_id, stat_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일일 통계';

-- =====================================================
-- 14. 채팅 세션
-- =====================================================
CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_name VARCHAR(200),
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    message_count INT NOT NULL DEFAULT 0,
    total_tokens INT COMMENT '총 토큰 수',
    metadata JSON COMMENT '추가 정보',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_active (user_id, is_active),
    INDEX idx_started (started_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='채팅 세션';

-- =====================================================
-- 15. Messages 테이블에 session_id 추가
-- =====================================================
ALTER TABLE messages ADD COLUMN session_id BIGINT COMMENT '채팅 세션 ID';
ALTER TABLE messages ADD CONSTRAINT fk_message_session FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE SET NULL;
ALTER TABLE messages ADD INDEX idx_session (session_id);

-- =====================================================
-- 초기 데이터: 레벨 정의
-- =====================================================
INSERT INTO trust_levels (level, name, min_score, max_score, daily_chat_limit, features_unlocked, restrictions, description) VALUES
(1, 'Stranger', 0, 20, 5, '["basic_chat"]', '["no_reports", "no_hip"]', 'AI에게 낯선 사람'),
(2, 'Acquaintance', 21, 40, 10, '["basic_chat", "weekly_reports"]', '["limited_hip"]', 'AI의 지인'),
(3, 'Trusted', 41, 60, 20, '["basic_chat", "weekly_reports", "basic_hip"]', '[]', 'AI가 신뢰하는 사람'),
(4, 'Loyal', 61, 80, 50, '["basic_chat", "weekly_reports", "full_hip", "resilience_reports"]', '[]', 'AI의 충성스러운 지지자'),
(5, 'Inner Circle', 81, 100, NULL, '["unlimited_chat", "all_features", "priority_support"]', '[]', 'AI 내부 서클'),
(6, 'Warning', -20, -1, 5, '["basic_chat"]', '["warning_message"]', '경고 상태'),
(7, 'Restricted', -40, -21, 1, '["basic_chat"]', '["feature_lock", "daily_limit_1"]', '제한된 상태'),
(8, 'Blocked (3일)', -60, -41, 0, '[]', '["full_block_3days"]', '3일 차단'),
(9, 'Blocked (7일)', -80, -61, 0, '[]', '["full_block_7days"]', '7일 차단'),
(10, 'Permanent Ban', -100, -81, 0, '[]', '["permanent_ban"]', '영구 차단');

SELECT '✅ V9 Migration completed successfully' as Status;
