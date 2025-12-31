-- LobAI Database Schema
-- Created: 2025-12-30
-- Database: lobai_db
-- Charset: utf8mb4 (for emoji support)

-- Drop tables if exists (for clean re-creation)
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS user_stats_history;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS personas;

-- 1. Personas Table (5 personas)
CREATE TABLE personas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '페르소나 한글명',
    name_en VARCHAR(50) NOT NULL UNIQUE COMMENT '페르소나 영문명',
    display_name VARCHAR(100) NOT NULL COMMENT 'UI 표시명',
    system_instruction TEXT NOT NULL COMMENT 'Gemini API 시스템 프롬프트',
    icon_emoji VARCHAR(10) COMMENT '아이콘 이모지',
    display_order INT DEFAULT 0 COMMENT '표시 순서',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성화 여부',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_display_order (display_order),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 페르소나 테이블';

-- 2. Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE COMMENT '이메일 (로그인 ID)',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt 해시 비밀번호',
    username VARCHAR(100) NOT NULL COMMENT '사용자명',
    profile_image_url VARCHAR(500) COMMENT '프로필 이미지 URL',

    -- Stats (현재 값 저장)
    current_hunger INT DEFAULT 80 CHECK (current_hunger >= 0 AND current_hunger <= 100) COMMENT '배고픔 (0-100)',
    current_energy INT DEFAULT 90 CHECK (current_energy >= 0 AND current_energy <= 100) COMMENT '에너지 (0-100)',
    current_happiness INT DEFAULT 70 CHECK (current_happiness >= 0 AND current_happiness <= 100) COMMENT '행복도 (0-100)',

    -- Persona
    current_persona_id BIGINT COMMENT '현재 선택된 페르소나',

    -- OAuth (Phase 2)
    oauth_provider VARCHAR(50) COMMENT 'OAuth 제공자 (google, kakao)',
    oauth_id VARCHAR(255) COMMENT 'OAuth ID',

    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP COMMENT '마지막 로그인 시각',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성화 여부',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '사용자 권한 (USER, ADMIN)',

    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_oauth (oauth_provider, oauth_id),
    INDEX idx_created_at (created_at),

    FOREIGN KEY (current_persona_id) REFERENCES personas(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 테이블';

-- 3. Messages Table (채팅 메시지)
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    persona_id BIGINT NOT NULL COMMENT '페르소나 ID',
    role ENUM('user', 'bot') NOT NULL COMMENT '발신자 역할',
    content TEXT NOT NULL COMMENT '메시지 내용',

    -- Phase 2용 분석 필드
    sentiment_score DECIMAL(5,2) COMMENT '감정 점수 (-1.00 ~ 1.00)',
    clarity_score DECIMAL(5,2) COMMENT '명확도 점수 (0.00 ~ 1.00)',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (persona_id) REFERENCES personas(id),
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_persona (persona_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='채팅 메시지 테이블';

-- 4. User Stats History Table
CREATE TABLE user_stats_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    hunger INT NOT NULL COMMENT '배고픔 값',
    energy INT NOT NULL COMMENT '에너지 값',
    happiness INT NOT NULL COMMENT '행복도 값',
    action_type ENUM('feed', 'play', 'sleep', 'chat', 'decay') NOT NULL COMMENT '행동 유형',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_action_type (action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 스탯 히스토리';

-- 5. Refresh Tokens Table (JWT 갱신 토큰)
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    token VARCHAR(500) NOT NULL UNIQUE COMMENT 'Refresh Token',
    expires_at TIMESTAMP NOT NULL COMMENT '만료 시각',
    is_revoked BOOLEAN DEFAULT FALSE COMMENT '폐기 여부',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_token (token),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Refresh Token 테이블';
