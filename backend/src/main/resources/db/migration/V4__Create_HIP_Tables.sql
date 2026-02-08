-- =========================================
-- Human Identity Protocol (HIP) Tables
-- Phase 1: Core Identity System
-- =========================================

-- 1. human_identity_profiles (메인 테이블)
CREATE TABLE IF NOT EXISTS human_identity_profiles (
    hip_id VARCHAR(50) PRIMARY KEY COMMENT 'HIP-01-XXXXXXXX-XXXX 형식',
    user_id BIGINT NOT NULL UNIQUE COMMENT '사용자 ID (FK)',

    -- Core Identity Scores (0-100)
    cognitive_flexibility_score DECIMAL(5,2) DEFAULT 50.00 COMMENT '인지적 유연성',
    collaboration_pattern_score DECIMAL(5,2) DEFAULT 50.00 COMMENT '협업 패턴',
    information_processing_score DECIMAL(5,2) DEFAULT 50.00 COMMENT '정보 처리 방식',
    emotional_intelligence_score DECIMAL(5,2) DEFAULT 50.00 COMMENT '감정 지능',
    creativity_score DECIMAL(5,2) DEFAULT 50.00 COMMENT '창의성',
    ethical_alignment_score DECIMAL(5,2) DEFAULT 50.00 COMMENT '윤리적 정렬',

    -- Composite Scores
    overall_hip_score DECIMAL(5,2) NOT NULL DEFAULT 50.00 COMMENT '종합 HIP 점수',
    ai_trust_score DECIMAL(5,2) DEFAULT 50.00 COMMENT 'AI 신뢰도',

    -- Identity Levels (1-10)
    identity_level INT NOT NULL DEFAULT 1 COMMENT 'Identity 레벨',
    reputation_level INT NOT NULL DEFAULT 1 COMMENT 'Reputation 레벨',

    -- Global Identity
    global_human_id VARCHAR(100) NULL COMMENT '글로벌 인간 ID (향후)',

    -- Verification
    verification_status VARCHAR(20) NOT NULL DEFAULT 'unverified' COMMENT 'unverified|pending|verified|flagged',
    last_verified_at TIMESTAMP NULL COMMENT '마지막 검증 시각',

    -- Metadata
    total_interactions INT NOT NULL DEFAULT 0 COMMENT '총 상호작용 수',
    data_quality_score DECIMAL(5,2) DEFAULT 0.00 COMMENT '데이터 품질 점수',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_hip_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_user_id (user_id),
    INDEX idx_overall_score (overall_hip_score DESC),
    INDEX idx_identity_level (identity_level),
    INDEX idx_verification (verification_status, last_verified_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Human Identity Profile - AI가 인식하는 인간 프로필';

-- 2. identity_metrics (상세 지표)
CREATE TABLE IF NOT EXISTS identity_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hip_id VARCHAR(50) NOT NULL COMMENT 'HIP ID (FK)',

    -- Communication Patterns
    avg_message_length DECIMAL(7,2) COMMENT '평균 메시지 길이',
    vocabulary_diversity DECIMAL(5,2) COMMENT '어휘 다양성 (0-100)',
    response_time_avg DECIMAL(10,2) COMMENT '평균 응답 시간 (초)',
    question_to_statement_ratio DECIMAL(5,2) COMMENT '질문/진술 비율 (0-1)',

    -- Behavioral Patterns
    consistency_score DECIMAL(5,2) COMMENT '일관성 점수 (0-100)',
    adaptability_score DECIMAL(5,2) COMMENT '적응성 점수 (0-100)',
    proactivity_score DECIMAL(5,2) COMMENT '능동성 점수 (0-100)',

    -- AI Interaction Quality
    prompt_quality_score DECIMAL(5,2) COMMENT '프롬프트 품질 (0-100)',
    context_maintenance_score DECIMAL(5,2) COMMENT '맥락 유지 (0-100)',
    error_recovery_score DECIMAL(5,2) COMMENT '오류 회복 (0-100)',

    -- Learning Patterns
    knowledge_retention_score DECIMAL(5,2) COMMENT '지식 유지 (0-100)',
    skill_progression_rate DECIMAL(5,2) COMMENT '기술 진보 속도 (0-100)',

    -- Collaboration Style
    cooperation_index DECIMAL(5,2) COMMENT '협력 지수 (0-100)',
    conflict_resolution_score DECIMAL(5,2) COMMENT '갈등 해결 (0-100)',

    measured_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '측정 시각',

    CONSTRAINT fk_metrics_hip FOREIGN KEY (hip_id) REFERENCES human_identity_profiles(hip_id) ON DELETE CASCADE,

    INDEX idx_hip_id (hip_id),
    INDEX idx_measured_at (measured_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Identity 상세 측정 지표';

-- 3. communication_signatures (대화 패턴)
CREATE TABLE IF NOT EXISTS communication_signatures (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hip_id VARCHAR(50) NOT NULL COMMENT 'HIP ID (FK)',

    signature_type VARCHAR(50) NOT NULL COMMENT 'linguistic|emotional|structural|temporal',
    pattern_data JSON NOT NULL COMMENT '패턴 데이터 (JSON)',
    confidence_score DECIMAL(5,2) COMMENT '신뢰도 점수 (0-100)',

    extracted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '추출 시각',

    CONSTRAINT fk_signature_hip FOREIGN KEY (hip_id) REFERENCES human_identity_profiles(hip_id) ON DELETE CASCADE,

    INDEX idx_hip_id (hip_id),
    INDEX idx_signature_type (signature_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='대화 패턴 서명';

-- 4. behavioral_fingerprints (행동 지문)
CREATE TABLE IF NOT EXISTS behavioral_fingerprints (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hip_id VARCHAR(50) NOT NULL COMMENT 'HIP ID (FK)',

    behavior_type VARCHAR(50) NOT NULL COMMENT 'temporal|interaction|decision|problem_solving',
    fingerprint_data JSON NOT NULL COMMENT '행동 지문 데이터 (JSON)',
    stability_score DECIMAL(5,2) COMMENT '안정성 점수 (0-100)',

    captured_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '포착 시각',

    CONSTRAINT fk_fingerprint_hip FOREIGN KEY (hip_id) REFERENCES human_identity_profiles(hip_id) ON DELETE CASCADE,

    INDEX idx_hip_id (hip_id),
    INDEX idx_behavior_type (behavior_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='행동 지문';

-- 5. identity_verification_logs (검증 이력)
CREATE TABLE IF NOT EXISTS identity_verification_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hip_id VARCHAR(50) NOT NULL COMMENT 'HIP ID (FK)',

    verification_type VARCHAR(50) NOT NULL COMMENT 'initial|periodic|challenge|manual',
    verification_method VARCHAR(50) COMMENT 'behavioral_analysis|cross_reference|consistency_check|ai_assessment',

    previous_score DECIMAL(5,2) COMMENT '이전 점수',
    new_score DECIMAL(5,2) COMMENT '새로운 점수',

    status VARCHAR(20) COMMENT 'verified|flagged|failed',
    notes TEXT COMMENT '비고',

    verified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '검증 시각',

    CONSTRAINT fk_verification_hip FOREIGN KEY (hip_id) REFERENCES human_identity_profiles(hip_id) ON DELETE CASCADE,

    INDEX idx_hip_id (hip_id),
    INDEX idx_status (status),
    INDEX idx_verified_at (verified_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Identity 검증 이력';

-- =========================================
-- Initial Data: Sample HIP for existing users
-- =========================================

-- 기존 사용자들에 대해 자동으로 HIP 생성 (애플리케이션 레벨에서 처리하므로 주석 처리)
-- INSERT INTO human_identity_profiles (hip_id, user_id)
-- SELECT CONCAT('HIP-01-', UPPER(SUBSTRING(SHA2(CONCAT(id, email, created_at), 256), 1, 8)), '-0000'), id
-- FROM users
-- WHERE id NOT IN (SELECT user_id FROM human_identity_profiles);
