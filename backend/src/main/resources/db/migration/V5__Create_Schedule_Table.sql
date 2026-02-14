-- V5__Create_Schedule_Table.sql
-- Schedule management system for LobAI

CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '일정 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    title VARCHAR(255) NOT NULL COMMENT '일정 제목',
    description TEXT COMMENT '일정 설명',
    start_time TIMESTAMP NOT NULL COMMENT '시작 시간',
    end_time TIMESTAMP NOT NULL COMMENT '종료 시간',
    type ENUM('REMINDER', 'INTERACTION', 'EVENT') NOT NULL DEFAULT 'EVENT' COMMENT '일정 유형',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Seoul' COMMENT '타임존',
    is_completed BOOLEAN NOT NULL DEFAULT FALSE COMMENT '완료 여부',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '삭제 여부 (소프트 삭제)',
    notify_before_minutes INT DEFAULT 0 COMMENT '알림 시간 (분 단위)',
    repeat_pattern VARCHAR(50) COMMENT '반복 패턴 (향후 확장용)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',

    CONSTRAINT fk_schedule_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_start (user_id, start_time),
    INDEX idx_user_type (user_id, type),
    INDEX idx_start_time (start_time),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 일정 테이블';
