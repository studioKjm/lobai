-- V14: Proactive Messaging (선제 대화) 지원
-- 챗봇이 조건에 따라 먼저 메시지를 보내는 기능

-- 1. 선제 메시지 로그 테이블
CREATE TABLE proactive_message_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    persona_id BIGINT NOT NULL,
    message_id BIGINT,
    trigger_type VARCHAR(50) NOT NULL,
    trigger_detail VARCHAR(500),
    trigger_date DATE NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    was_displayed BOOLEAN DEFAULT FALSE,
    INDEX idx_pml_user_date (user_id, trigger_date, trigger_type),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (persona_id) REFERENCES personas(id),
    FOREIGN KEY (message_id) REFERENCES messages(id)
);

-- 2. messages 테이블에 메시지 타입 컬럼 추가
ALTER TABLE messages ADD COLUMN message_type VARCHAR(20) DEFAULT 'NORMAL';
