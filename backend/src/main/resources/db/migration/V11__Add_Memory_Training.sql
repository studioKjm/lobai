-- ================================================
-- V11: Add MEMORY Training Type
-- ================================================
-- 암기력(기억력) 훈련 유형 추가 및 샘플 데이터 삽입
-- ================================================

-- 1. training_sessions 테이블의 training_type ENUM에 MEMORY 추가
ALTER TABLE training_sessions
    MODIFY COLUMN training_type ENUM('CRITICAL_THINKING', 'PROBLEM_SOLVING', 'CREATIVE_THINKING', 'SELF_REFLECTION', 'REASONING', 'MEMORY') NOT NULL COMMENT '훈련 유형';

-- 2. training_problems 테이블의 training_type ENUM에 MEMORY 추가
ALTER TABLE training_problems
    MODIFY COLUMN training_type ENUM('CRITICAL_THINKING', 'PROBLEM_SOLVING', 'CREATIVE_THINKING', 'SELF_REFLECTION', 'REASONING', 'MEMORY') NOT NULL;

-- 3. training_statistics 테이블의 training_type ENUM에 MEMORY 추가
ALTER TABLE training_statistics
    MODIFY COLUMN training_type ENUM('CRITICAL_THINKING', 'PROBLEM_SOLVING', 'CREATIVE_THINKING', 'SELF_REFLECTION', 'REASONING', 'MEMORY') NOT NULL;

-- 4. MEMORY 타입 샘플 문제 삽입 (난이도 1-10)
-- problem_text에는 JSON 형태로 문제 데이터를 저장 (프론트엔드에서 파싱하여 인터랙티브 UI 구현)

-- 난이도 1-2: 쉬운 암기 (짧은 시퀀스)
INSERT INTO training_problems (training_type, difficulty_level, problem_text, correct_answer, hints, is_active) VALUES
('MEMORY', 1,
 '{"type":"number_sequence","data":[3,7,2],"display_time_per_item":1500,"instruction":"다음 숫자들을 순서대로 기억하세요"}',
 '3,7,2',
 '["힌트 1: 숫자를 소리내어 읽어보세요", "힌트 2: 3-7-2를 하나의 패턴으로 생각해보세요"]',
 TRUE),

('MEMORY', 2,
 '{"type":"number_sequence","data":[5,9,2,8,1],"display_time_per_item":1200,"instruction":"다음 숫자들을 순서대로 기억하세요"}',
 '5,9,2,8,1',
 '["힌트 1: 숫자를 그룹으로 나눠보세요 (5-9, 2-8, 1)", "힌트 2: 리듬을 만들어보세요"]',
 TRUE);

-- 난이도 3-4: 중간 암기 (단어 리스트, 긴 시퀀스)
INSERT INTO training_problems (training_type, difficulty_level, problem_text, correct_answer, hints, is_active) VALUES
('MEMORY', 3,
 '{"type":"word_list","data":["사과","의자","하늘","연필","고양이"],"display_time_total":8000,"instruction":"다음 단어들을 순서대로 기억하세요"}',
 '사과,의자,하늘,연필,고양이',
 '["힌트 1: 단어들로 이야기를 만들어보세요", "힌트 2: 각 단어의 첫 글자를 기억하세요 (사의하연고)"]',
 TRUE),

('MEMORY', 4,
 '{"type":"number_sequence","data":[7,3,9,1,5,8,2,6],"display_time_per_item":1000,"instruction":"다음 숫자들을 순서대로 기억하세요"}',
 '7,3,9,1,5,8,2,6',
 '["힌트 1: 숫자를 4개씩 묶어보세요 (7391, 5826)", "힌트 2: 패턴을 찾아보세요"]',
 TRUE);

-- 난이도 5-6: 어려운 암기 (색깔 패턴, 위치 기억)
INSERT INTO training_problems (training_type, difficulty_level, problem_text, correct_answer, hints, is_active) VALUES
('MEMORY', 5,
 '{"type":"color_pattern","data":["빨강","파랑","노랑","빨강","초록","파랑"],"display_time_per_item":1000,"instruction":"다음 색깔 순서를 기억하세요"}',
 '빨강,파랑,노랑,빨강,초록,파랑',
 '["힌트 1: 반복되는 색깔을 찾아보세요", "힌트 2: 색깔을 첫 글자로 줄여보세요 (빨파노빨초파)"]',
 TRUE),

('MEMORY', 6,
 '{"type":"word_list","data":["컴퓨터","산","바다","책","음악","커피","별","나무"],"display_time_total":10000,"instruction":"다음 단어들을 순서대로 기억하세요"}',
 '컴퓨터,산,바다,책,음악,커피,별,나무',
 '["힌트 1: 단어를 카테고리별로 그룹화하세요", "힌트 2: 연상 이미지를 만드세요"]',
 TRUE);

-- 난이도 7-8: 매우 어려운 암기 (긴 시퀀스, 복잡한 패턴)
INSERT INTO training_problems (training_type, difficulty_level, problem_text, correct_answer, hints, is_active) VALUES
('MEMORY', 7,
 '{"type":"number_sequence","data":[4,8,3,9,2,7,5,1,6,0],"display_time_per_item":900,"instruction":"다음 숫자들을 순서대로 기억하세요"}',
 '4,8,3,9,2,7,5,1,6,0',
 '["힌트 1: 숫자를 3-3-4로 나눠보세요", "힌트 2: 짝수/홀수 패턴을 찾아보세요"]',
 TRUE),

('MEMORY', 8,
 '{"type":"mixed_sequence","data":["A3","B7","C2","D9","E5","F1"],"display_time_per_item":1000,"instruction":"다음 문자-숫자 조합을 순서대로 기억하세요"}',
 'A3,B7,C2,D9,E5,F1',
 '["힌트 1: 알파벳과 숫자를 따로 기억하세요", "힌트 2: A=1, B=2, C=3... 규칙을 찾아보세요"]',
 TRUE);

-- 난이도 9-10: 최고난이도 암기 (매우 긴 시퀀스, 복합 정보)
INSERT INTO training_problems (training_type, difficulty_level, problem_text, correct_answer, hints, is_active) VALUES
('MEMORY', 9,
 '{"type":"word_list","data":["지하철","햄버거","우주선","피아노","지진","태풍","박물관","올림픽","화산","북극곰","사막","은하수"],"display_time_total":12000,"instruction":"다음 12개 단어를 순서대로 기억하세요"}',
 '지하철,햄버거,우주선,피아노,지진,태풍,박물관,올림픽,화산,북극곰,사막,은하수',
 '["힌트 1: 단어들로 긴 이야기를 만드세요", "힌트 2: 시각적 이미지로 연결하세요", "힌트 3: 3개씩 묶어서 4그룹으로 나누세요"]',
 TRUE),

('MEMORY', 10,
 '{"type":"number_sequence","data":[7,1,9,3,8,2,5,4,6,0,9,1,3,7,2],"display_time_per_item":800,"instruction":"다음 15개 숫자를 순서대로 기억하세요"}',
 '7,1,9,3,8,2,5,4,6,0,9,1,3,7,2',
 '["힌트 1: 5개씩 묶어서 3그룹으로 나누세요", "힌트 2: 각 그룹에 의미를 부여하세요", "힌트 3: 반복되는 숫자를 찾아보세요"]',
 TRUE);

-- 완료 메시지
SELECT '✅ V11 Migration: MEMORY Training Type added successfully' AS Status;
