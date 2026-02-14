-- V13: 챗봇 고도화 - 컨텍스트 관리 & LLM 사용량 추적

-- 1. 대화 요약 테이블
CREATE TABLE conversation_summaries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    persona_id BIGINT,
    summary_text TEXT NOT NULL,
    key_facts TEXT,
    message_start_id BIGINT,
    message_end_id BIGINT,
    message_count INT DEFAULT 0,
    token_count INT DEFAULT 0,
    summary_type ENUM('SESSION','PERIODIC','MANUAL') DEFAULT 'PERIODIC',
    llm_provider VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cs_user_persona (user_id, persona_id),
    INDEX idx_cs_user_created (user_id, created_at DESC),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (persona_id) REFERENCES personas(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 사용자 기억 테이블
CREATE TABLE user_memories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    memory_key VARCHAR(100) NOT NULL,
    memory_value TEXT NOT NULL,
    memory_type ENUM('FACT','PROMISE','PREFERENCE','BEHAVIORAL_PATTERN') NOT NULL,
    confidence_score DECIMAL(3,2) DEFAULT 0.50,
    source_message_id BIGINT,
    last_referenced_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_um_user_active (user_id, is_active),
    UNIQUE INDEX idx_um_user_key (user_id, memory_key),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. LLM 사용량 추적 테이블
CREATE TABLE llm_usage_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider_name VARCHAR(50) NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    prompt_tokens INT DEFAULT 0,
    completion_tokens INT DEFAULT 0,
    total_tokens INT DEFAULT 0,
    estimated_cost_usd DECIMAL(10,6) DEFAULT 0,
    response_time_ms INT DEFAULT 0,
    is_fallback BOOLEAN DEFAULT FALSE,
    error_message VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_lul_user (user_id),
    INDEX idx_lul_provider (provider_name, created_at),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. messages 테이블에 LLM 메타데이터 추가
ALTER TABLE messages
ADD COLUMN llm_provider VARCHAR(50) DEFAULT NULL,
ADD COLUMN llm_model VARCHAR(100) DEFAULT NULL,
ADD COLUMN token_count INT DEFAULT NULL;
