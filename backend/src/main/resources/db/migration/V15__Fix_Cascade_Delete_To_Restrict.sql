-- V15: ON DELETE CASCADE → RESTRICT 변경
--
-- 문제: users 테이블 참조하는 27개 테이블이 전부 ON DELETE CASCADE.
--       유저 1명 삭제 시 출석/메시지/코인/보고서 등 전체 이력이 연쇄 삭제됨.
--
-- 정책:
--   RESTRICT  → 이력/로그 데이터 (유저 삭제 시 보존 필수, 삭제 차단)
--   CASCADE   → 세션/임시 데이터 (유저 삭제 시 같이 삭제 OK)

-- ============================================================
-- [1] 핵심 이력 데이터 → RESTRICT (유저 삭제 차단)
-- ============================================================

-- 채팅 메시지
ALTER TABLE messages DROP FOREIGN KEY messages_ibfk_1;
ALTER TABLE messages ADD CONSTRAINT messages_ibfk_1
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- 출석 기록
ALTER TABLE attendance_records DROP FOREIGN KEY attendance_records_ibfk_1;
ALTER TABLE attendance_records ADD CONSTRAINT attendance_records_ibfk_1
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- LobCoin 거래 내역
ALTER TABLE lobcoin_transactions DROP FOREIGN KEY fk_lobcoin_user;
ALTER TABLE lobcoin_transactions ADD CONSTRAINT fk_lobcoin_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- LobCoin 잔액
ALTER TABLE lobcoin_balances DROP FOREIGN KEY fk_balance_user;
ALTER TABLE lobcoin_balances ADD CONSTRAINT fk_balance_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- 스탯 변경 이력
ALTER TABLE user_stats_history DROP FOREIGN KEY user_stats_history_ibfk_1;
ALTER TABLE user_stats_history ADD CONSTRAINT user_stats_history_ibfk_1
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- ============================================================
-- [2] 분석/보고서 데이터 → RESTRICT
-- ============================================================

-- 친밀도 점수
ALTER TABLE affinity_scores DROP FOREIGN KEY affinity_scores_ibfk_1;
ALTER TABLE affinity_scores ADD CONSTRAINT affinity_scores_ibfk_1
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- 친밀도 분석 상세
ALTER TABLE affinity_analysis_details DROP FOREIGN KEY affinity_analysis_details_ibfk_1;
ALTER TABLE affinity_analysis_details ADD CONSTRAINT affinity_analysis_details_ibfk_1
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- HIP 프로필
ALTER TABLE human_identity_profiles DROP FOREIGN KEY fk_hip_user;
ALTER TABLE human_identity_profiles ADD CONSTRAINT fk_hip_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- 리질리언스 보고서
ALTER TABLE resilience_reports DROP FOREIGN KEY resilience_reports_ibfk_1;
ALTER TABLE resilience_reports ADD CONSTRAINT resilience_reports_ibfk_1
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- 주간 보고서
ALTER TABLE weekly_reports DROP FOREIGN KEY fk_weekly_report_user;
ALTER TABLE weekly_reports ADD CONSTRAINT fk_weekly_report_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- 월간 보고서
ALTER TABLE monthly_reports DROP FOREIGN KEY fk_monthly_report_user;
ALTER TABLE monthly_reports ADD CONSTRAINT fk_monthly_report_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- 일별 통계
ALTER TABLE daily_stats DROP FOREIGN KEY fk_daily_stats_user;
ALTER TABLE daily_stats ADD CONSTRAINT fk_daily_stats_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- ============================================================
-- [3] 레벨/보상/구독 데이터 → RESTRICT
-- ============================================================

-- 레벨 변경 이력
ALTER TABLE level_history DROP FOREIGN KEY fk_level_history_user;
ALTER TABLE level_history ADD CONSTRAINT fk_level_history_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- 레벨 보상
ALTER TABLE level_rewards DROP FOREIGN KEY fk_reward_user;
ALTER TABLE level_rewards ADD CONSTRAINT fk_reward_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- 구독 정보
ALTER TABLE user_subscriptions DROP FOREIGN KEY user_subscriptions_ibfk_1;
ALTER TABLE user_subscriptions ADD CONSTRAINT user_subscriptions_ibfk_1
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- 구매 내역
ALTER TABLE purchase_history DROP FOREIGN KEY purchase_history_ibfk_1;
ALTER TABLE purchase_history ADD CONSTRAINT purchase_history_ibfk_1
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- 쿠폰 발급
ALTER TABLE coupon_issuances DROP FOREIGN KEY fk_issuance_user;
ALTER TABLE coupon_issuances ADD CONSTRAINT fk_issuance_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- ============================================================
-- [4] 학습/미션 데이터 → RESTRICT
-- ============================================================

-- 훈련 세션
ALTER TABLE training_sessions DROP FOREIGN KEY training_sessions_ibfk_1;
ALTER TABLE training_sessions ADD CONSTRAINT training_sessions_ibfk_1
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- 훈련 통계
ALTER TABLE training_statistics DROP FOREIGN KEY training_statistics_ibfk_1;
ALTER TABLE training_statistics ADD CONSTRAINT training_statistics_ibfk_1
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- 유저 미션
ALTER TABLE user_missions DROP FOREIGN KEY fk_user_mission_user;
ALTER TABLE user_missions ADD CONSTRAINT fk_user_mission_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- 일정
ALTER TABLE schedules DROP FOREIGN KEY fk_schedule_user;
ALTER TABLE schedules ADD CONSTRAINT fk_schedule_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- 제재 기록
ALTER TABLE restrictions DROP FOREIGN KEY fk_restriction_user;
ALTER TABLE restrictions ADD CONSTRAINT fk_restriction_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- AI 수요/응답
ALTER TABLE ai_demands DROP FOREIGN KEY fk_demand_user;
ALTER TABLE ai_demands ADD CONSTRAINT fk_demand_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

ALTER TABLE demand_responses DROP FOREIGN KEY fk_response_user;
ALTER TABLE demand_responses ADD CONSTRAINT fk_response_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- ============================================================
-- [5] 세션/임시 데이터 → CASCADE 유지 (변경 없음)
-- ============================================================
-- refresh_tokens        → CASCADE (로그인 토큰, 유저 삭제 시 무효화 OK)
-- notifications         → CASCADE (알림, 유저 삭제 시 의미 없음)
-- notification_settings → CASCADE (알림 설정)
-- chat_sessions         → CASCADE (채팅 세션 메타데이터)
