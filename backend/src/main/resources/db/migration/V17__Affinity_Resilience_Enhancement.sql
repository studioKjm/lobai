-- V17: 친밀도 고도화 + AI 적응력 보고서 연동
-- Gemini AI 기반 감성 분석, 7차원 친밀도, 노벨티 할인, 데이터 임계치

-- =====================================================
-- 1. messages 테이블: Gemini 분석 결과 컬럼 추가
-- =====================================================
ALTER TABLE messages
  ADD COLUMN primary_emotion VARCHAR(30) DEFAULT NULL,
  ADD COLUMN self_disclosure_depth DECIMAL(3,2) DEFAULT NULL,
  ADD COLUMN honorific_level VARCHAR(20) DEFAULT NULL,
  ADD COLUMN is_question BOOLEAN DEFAULT FALSE,
  ADD COLUMN is_initiative BOOLEAN DEFAULT FALSE;

-- =====================================================
-- 2. affinity_scores 테이블: 새 차원 + 메타데이터
-- =====================================================
ALTER TABLE affinity_scores
  ADD COLUMN avg_engagement_depth DECIMAL(5,2) DEFAULT 0.50,
  ADD COLUMN avg_self_disclosure DECIMAL(5,2) DEFAULT 0.00,
  ADD COLUMN avg_reciprocity DECIMAL(5,2) DEFAULT 0.50,
  ADD COLUMN relationship_stage VARCHAR(20) DEFAULT 'EARLY',
  ADD COLUMN total_sessions INT DEFAULT 0,
  ADD COLUMN novelty_discount_factor DECIMAL(3,2) DEFAULT 0.60,
  ADD COLUMN data_threshold_status VARCHAR(20) DEFAULT 'COLLECTING',
  ADD COLUMN honorific_transition_detected BOOLEAN DEFAULT FALSE;

-- =====================================================
-- 3. 친밀도 일별 스냅샷 테이블 (트렌드용)
-- =====================================================
CREATE TABLE affinity_score_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  snapshot_date DATE NOT NULL,
  overall_score DECIMAL(5,2) NOT NULL,
  level INT NOT NULL,
  avg_sentiment_score DECIMAL(5,2),
  avg_clarity_score DECIMAL(5,2),
  avg_context_score DECIMAL(5,2),
  avg_usage_score DECIMAL(5,2),
  avg_engagement_depth DECIMAL(5,2),
  avg_self_disclosure DECIMAL(5,2),
  avg_reciprocity DECIMAL(5,2),
  relationship_stage VARCHAR(20),
  data_threshold_status VARCHAR(20),
  total_messages INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  UNIQUE KEY uk_user_date (user_id, snapshot_date),
  INDEX idx_user_date (user_id, snapshot_date),
  CONSTRAINT fk_ash_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. resilience_reports 테이블: 친밀도 연동 컬럼
-- =====================================================
ALTER TABLE resilience_reports
  ADD COLUMN avg_engagement_depth DECIMAL(5,2) DEFAULT NULL,
  ADD COLUMN avg_self_disclosure DECIMAL(5,2) DEFAULT NULL,
  ADD COLUMN avg_reciprocity DECIMAL(5,2) DEFAULT NULL,
  ADD COLUMN affinity_score_at_report DECIMAL(5,2) DEFAULT NULL,
  ADD COLUMN affinity_level_at_report INT DEFAULT NULL,
  ADD COLUMN recommendations JSON DEFAULT NULL;
