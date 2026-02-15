-- V18: Daily Conversation Summary
-- DAILY 타입 추가 및 요약 날짜 컬럼

-- DAILY 타입 추가
ALTER TABLE conversation_summaries
  MODIFY COLUMN summary_type ENUM('SESSION','PERIODIC','MANUAL','DAILY') DEFAULT 'PERIODIC';

-- 요약 날짜 컬럼 추가
ALTER TABLE conversation_summaries
  ADD COLUMN summary_date DATE DEFAULT NULL;

-- 날짜 기반 조회 인덱스
CREATE INDEX idx_cs_user_date ON conversation_summaries (user_id, summary_date);

-- 유저+타입+날짜 유니크 제약 (중복 방지)
CREATE UNIQUE INDEX uk_cs_user_type_date ON conversation_summaries (user_id, summary_type, summary_date);
