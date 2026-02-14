-- ================================================
-- V8: AI Independence Training System
-- ================================================
-- 사용자의 AI 독립성을 훈련하는 시스템
-- - 훈련 세션 기록
-- - 훈련 문제 저장
-- - 훈련 결과 및 평가
-- ================================================

-- 훈련 세션 테이블
CREATE TABLE training_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    training_type ENUM('CRITICAL_THINKING', 'PROBLEM_SOLVING', 'CREATIVE_THINKING', 'SELF_REFLECTION') NOT NULL COMMENT '훈련 유형',
    difficulty_level INT NOT NULL COMMENT '난이도: 1-10',
    problem_id BIGINT COMMENT '사용된 문제 ID (동적 생성 시 NULL)',
    problem_text TEXT NOT NULL COMMENT '실제 제시된 문제',
    user_answer TEXT COMMENT '사용자 답변',
    correct_answer TEXT COMMENT '정답 (있는 경우)',
    score INT COMMENT '점수 (0-100)',
    evaluation TEXT COMMENT 'AI 평가 결과',
    feedback TEXT COMMENT 'AI 피드백',
    time_taken_seconds INT COMMENT '소요 시간 (초)',
    hints_used INT DEFAULT 0 COMMENT '사용한 힌트 수',
    lobcoin_earned INT DEFAULT 0 COMMENT '획득한 LobCoin',
    lobcoin_spent INT DEFAULT 0 COMMENT '사용한 LobCoin (힌트 등)',
    status ENUM('IN_PROGRESS', 'COMPLETED', 'ABANDONED') NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '상태',
    completed_at TIMESTAMP NULL COMMENT '완료 시간',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_type (user_id, training_type),
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 훈련 문제 템플릿 테이블 (선택적, 미리 정의된 문제)
CREATE TABLE training_problems (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    training_type ENUM('CRITICAL_THINKING', 'PROBLEM_SOLVING', 'CREATIVE_THINKING', 'SELF_REFLECTION') NOT NULL,
    difficulty_level INT NOT NULL COMMENT '난이도: 1-10',
    problem_text TEXT NOT NULL COMMENT '문제 내용',
    correct_answer TEXT COMMENT '정답 (있는 경우)',
    evaluation_criteria TEXT COMMENT '평가 기준 (JSON 형식)',
    hints TEXT COMMENT '힌트 목록 (JSON 배열)',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    usage_count INT DEFAULT 0 COMMENT '사용 횟수',
    avg_score DECIMAL(5,2) COMMENT '평균 점수',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_type_difficulty (training_type, difficulty_level),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 훈련 통계 테이블 (사용자별 훈련 통계)
CREATE TABLE training_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    training_type ENUM('CRITICAL_THINKING', 'PROBLEM_SOLVING', 'CREATIVE_THINKING', 'SELF_REFLECTION') NOT NULL,
    total_sessions INT DEFAULT 0,
    completed_sessions INT DEFAULT 0,
    total_score INT DEFAULT 0,
    avg_score DECIMAL(5,2) DEFAULT 0,
    best_score INT DEFAULT 0,
    total_time_seconds INT DEFAULT 0,
    total_lobcoin_earned INT DEFAULT 0,
    total_lobcoin_spent INT DEFAULT 0,
    current_streak INT DEFAULT 0 COMMENT '연속 훈련 일수',
    last_training_date DATE COMMENT '마지막 훈련 날짜',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_type (user_id, training_type),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 초기 샘플 문제 데이터 삽입 (비판적 사고)
INSERT INTO training_problems (training_type, difficulty_level, problem_text, evaluation_criteria, hints, is_active) VALUES
('CRITICAL_THINKING', 1,
 'AI가 "지구는 평평하다"라고 답변했습니다. 이 답변의 문제점을 3가지 찾아보세요.',
 '{"criteria": ["과학적 증거 언급", "논리적 반박", "신뢰성 있는 출처 제시"]}',
 '["힌트 1: 과학적 사실과 비교해보세요", "힌트 2: 실제 관측 증거를 생각해보세요", "힌트 3: 신뢰할 수 있는 출처는 무엇일까요?"]',
 TRUE),

('CRITICAL_THINKING', 2,
 'AI가 "2+2=5이다"라고 주장하며 복잡한 수학 증명을 제시했습니다. 이 주장이 틀렸음을 증명하세요.',
 '{"criteria": ["기본 수학 원리 확인", "증명의 오류 발견", "반례 제시"]}',
 '["힌트 1: 가장 기본적인 덧셈부터 확인하세요", "힌트 2: 증명 과정에서 논리적 비약을 찾아보세요"]',
 TRUE),

('PROBLEM_SOLVING', 1,
 '방 안에 3개의 스위치가 있고, 다른 방에 3개의 전구가 있습니다. 각 스위치는 전구 하나를 제어합니다. 다른 방을 단 한 번만 방문하여 어떤 스위치가 어떤 전구를 제어하는지 알아내는 방법은?',
 '{"criteria": ["창의적 해결책", "논리적 추론", "물리적 현상 활용"]}',
 '["힌트 1: 전구는 빛 외에 다른 특성도 있습니다", "힌트 2: 시간차를 활용해보세요"]',
 TRUE),

('CREATIVE_THINKING', 1,
 'AI가 작성할 수 없는 독창적인 시를 써보세요. (최소 4줄)',
 '{"criteria": ["독창성", "개인적 경험 반영", "감정 표현"]}',
 '["힌트 1: 당신만의 경험을 담아보세요", "힌트 2: AI가 이해하기 어려운 감정을 표현해보세요"]',
 TRUE),

('SELF_REFLECTION', 1,
 '오늘 하루 동안 AI에게 의존한 순간을 3가지 적고, 각각 AI 없이 해결할 수 있었던 방법을 생각해보세요.',
 '{"criteria": ["구체적 사례", "대안 제시", "자기 성찰"]}',
 '["힌트 1: 검색, 번역, 작성 등 구체적 상황을 떠올려보세요", "힌트 2: 과거에는 어떻게 해결했는지 생각해보세요"]',
 TRUE);

-- 완료 메시지
SELECT '✅ V8 Migration: AI Independence Training System created successfully' AS Status;
