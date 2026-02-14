-- MySQL dump 10.13  Distrib 8.0.44, for macos15.7 (arm64)
--
-- Host: localhost    Database: lobai_db
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `affinity_analysis_details`
--

DROP TABLE IF EXISTS `affinity_analysis_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `affinity_analysis_details` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '사용자 ID',
  `analysis_type` enum('daily','weekly','monthly','on_demand') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '분석 유형',
  `score_snapshot` decimal(5,2) NOT NULL COMMENT '분석 당시 점수',
  `level_snapshot` int NOT NULL COMMENT '분석 당시 레벨',
  `sentiment_feedback` text COLLATE utf8mb4_unicode_ci COMMENT '감정 점수 피드백',
  `clarity_feedback` text COLLATE utf8mb4_unicode_ci COMMENT '명확성 점수 피드백',
  `context_feedback` text COLLATE utf8mb4_unicode_ci COMMENT '맥락 유지 피드백',
  `usage_feedback` text COLLATE utf8mb4_unicode_ci COMMENT 'AI 활용 태도 피드백',
  `improvement_suggestions` text COLLATE utf8mb4_unicode_ci COMMENT '개선 제안사항 (JSON)',
  `analyzed_message_count` int NOT NULL COMMENT '분석된 메시지 수',
  `analysis_period_start` timestamp NULL DEFAULT NULL COMMENT '분석 기간 시작',
  `analysis_period_end` timestamp NULL DEFAULT NULL COMMENT '분석 기간 종료',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`,`created_at` DESC),
  KEY `idx_analysis_type` (`analysis_type`),
  CONSTRAINT `affinity_analysis_details_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='친밀도 상세 분석 결과 (유료)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `affinity_analysis_details`
--

LOCK TABLES `affinity_analysis_details` WRITE;
/*!40000 ALTER TABLE `affinity_analysis_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `affinity_analysis_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `affinity_scores`
--

DROP TABLE IF EXISTS `affinity_scores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `affinity_scores` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '사용자 ID',
  `overall_score` decimal(5,2) NOT NULL DEFAULT '50.00' COMMENT '종합 친밀도 점수 (0.00-100.00)',
  `level` int NOT NULL DEFAULT '3' COMMENT '레벨 (1-5)',
  `avg_sentiment_score` decimal(5,2) DEFAULT '0.00' COMMENT '평균 감정 점수 (-1.00 ~ 1.00)',
  `avg_clarity_score` decimal(5,2) DEFAULT '0.50' COMMENT '평균 명확성 점수 (0.00 ~ 1.00)',
  `avg_context_score` decimal(5,2) DEFAULT '0.50' COMMENT '평균 맥락 유지 점수 (0.00 ~ 1.00)',
  `avg_usage_score` decimal(5,2) DEFAULT '0.50' COMMENT '평균 활용 태도 점수 (0.00 ~ 1.00)',
  `total_messages` int NOT NULL DEFAULT '0' COMMENT '총 메시지 수',
  `analyzed_messages` int NOT NULL DEFAULT '0' COMMENT '분석된 메시지 수',
  `last_analyzed_message_id` bigint DEFAULT NULL COMMENT '마지막 분석된 메시지 ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_overall_score` (`overall_score` DESC),
  KEY `idx_level` (`level`),
  KEY `idx_updated_at` (`updated_at`),
  CONSTRAINT `affinity_scores_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자별 친밀도 점수 집계 테이블';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `affinity_scores`
--

LOCK TABLES `affinity_scores` WRITE;
/*!40000 ALTER TABLE `affinity_scores` DISABLE KEYS */;
INSERT INTO `affinity_scores` VALUES (18,21,50.00,3,0.00,0.50,0.50,0.50,2,0,NULL,'2026-02-11 05:01:55','2026-02-11 05:11:19');
/*!40000 ALTER TABLE `affinity_scores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendance_records`
--

DROP TABLE IF EXISTS `attendance_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '사용자 ID',
  `check_in_date` date NOT NULL COMMENT '출석 날짜 (YYYY-MM-DD)',
  `check_in_time` timestamp NOT NULL COMMENT '출석 시각',
  `streak_count` int NOT NULL DEFAULT '1' COMMENT '연속 출석 일수',
  `reward_points` int DEFAULT '0' COMMENT '출석 보상 포인트',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`check_in_date`),
  KEY `idx_user_streak` (`user_id`,`streak_count` DESC),
  KEY `idx_check_in_date` (`check_in_date` DESC),
  CONSTRAINT `attendance_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='출석 체크 기록';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance_records`
--

LOCK TABLES `attendance_records` WRITE;
/*!40000 ALTER TABLE `attendance_records` DISABLE KEYS */;
/*!40000 ALTER TABLE `attendance_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `behavioral_fingerprints`
--

DROP TABLE IF EXISTS `behavioral_fingerprints`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `behavioral_fingerprints` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hip_id` varchar(50) NOT NULL COMMENT 'HIP ID (FK)',
  `behavior_type` varchar(50) NOT NULL COMMENT 'temporal|interaction|decision|problem_solving',
  `fingerprint_data` json NOT NULL COMMENT '행동 지문 데이터 (JSON)',
  `stability_score` decimal(5,2) DEFAULT NULL COMMENT '안정성 점수 (0-100)',
  `captured_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '포착 시각',
  PRIMARY KEY (`id`),
  KEY `idx_hip_id` (`hip_id`),
  KEY `idx_behavior_type` (`behavior_type`),
  CONSTRAINT `fk_fingerprint_hip` FOREIGN KEY (`hip_id`) REFERENCES `human_identity_profiles` (`hip_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='행동 지문';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `behavioral_fingerprints`
--

LOCK TABLES `behavioral_fingerprints` WRITE;
/*!40000 ALTER TABLE `behavioral_fingerprints` DISABLE KEYS */;
/*!40000 ALTER TABLE `behavioral_fingerprints` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `communication_signatures`
--

DROP TABLE IF EXISTS `communication_signatures`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `communication_signatures` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hip_id` varchar(50) NOT NULL COMMENT 'HIP ID (FK)',
  `signature_type` varchar(50) NOT NULL COMMENT 'linguistic|emotional|structural|temporal',
  `pattern_data` json NOT NULL COMMENT '패턴 데이터 (JSON)',
  `confidence_score` decimal(5,2) DEFAULT NULL COMMENT '신뢰도 점수 (0-100)',
  `extracted_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '추출 시각',
  PRIMARY KEY (`id`),
  KEY `idx_hip_id` (`hip_id`),
  KEY `idx_signature_type` (`signature_type`),
  CONSTRAINT `fk_signature_hip` FOREIGN KEY (`hip_id`) REFERENCES `human_identity_profiles` (`hip_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='대화 패턴 서명';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `communication_signatures`
--

LOCK TABLES `communication_signatures` WRITE;
/*!40000 ALTER TABLE `communication_signatures` DISABLE KEYS */;
/*!40000 ALTER TABLE `communication_signatures` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `human_identity_profiles`
--

DROP TABLE IF EXISTS `human_identity_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `human_identity_profiles` (
  `hip_id` varchar(50) NOT NULL COMMENT 'HIP-01-XXXXXXXX-XXXX 형식',
  `user_id` bigint NOT NULL COMMENT '사용자 ID (FK)',
  `cognitive_flexibility_score` decimal(5,2) DEFAULT '50.00' COMMENT '인지적 유연성',
  `collaboration_pattern_score` decimal(5,2) DEFAULT '50.00' COMMENT '협업 패턴',
  `information_processing_score` decimal(5,2) DEFAULT '50.00' COMMENT '정보 처리 방식',
  `emotional_intelligence_score` decimal(5,2) DEFAULT '50.00' COMMENT '감정 지능',
  `creativity_score` decimal(5,2) DEFAULT '50.00' COMMENT '창의성',
  `ethical_alignment_score` decimal(5,2) DEFAULT '50.00' COMMENT '윤리적 정렬',
  `overall_hip_score` decimal(5,2) NOT NULL DEFAULT '50.00' COMMENT '종합 HIP 점수',
  `ai_trust_score` decimal(5,2) DEFAULT '50.00' COMMENT 'AI 신뢰도',
  `identity_level` int NOT NULL DEFAULT '1' COMMENT 'Identity 레벨',
  `reputation_level` int NOT NULL DEFAULT '1' COMMENT 'Reputation 레벨',
  `global_human_id` varchar(100) DEFAULT NULL COMMENT '글로벌 인간 ID (향후)',
  `verification_status` varchar(20) NOT NULL DEFAULT 'unverified' COMMENT 'unverified|pending|verified|flagged',
  `last_verified_at` timestamp NULL DEFAULT NULL COMMENT '마지막 검증 시각',
  `total_interactions` int NOT NULL DEFAULT '0' COMMENT '총 상호작용 수',
  `data_quality_score` decimal(5,2) DEFAULT '0.00' COMMENT '데이터 품질 점수',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`hip_id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_overall_score` (`overall_hip_score` DESC),
  KEY `idx_identity_level` (`identity_level`),
  KEY `idx_verification` (`verification_status`,`last_verified_at`),
  CONSTRAINT `fk_hip_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Human Identity Profile - AI가 인식하는 인간 프로필';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `human_identity_profiles`
--

LOCK TABLES `human_identity_profiles` WRITE;
/*!40000 ALTER TABLE `human_identity_profiles` DISABLE KEYS */;
INSERT INTO `human_identity_profiles` VALUES ('HIP-01-7415EA5F-6FEF',21,50.00,50.00,50.00,50.00,50.00,50.00,50.00,45.00,6,1,NULL,'unverified',NULL,0,0.00,'2026-02-11 05:01:16','2026-02-11 05:01:16');
/*!40000 ALTER TABLE `human_identity_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `identity_metrics`
--

DROP TABLE IF EXISTS `identity_metrics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `identity_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hip_id` varchar(50) NOT NULL COMMENT 'HIP ID (FK)',
  `avg_message_length` decimal(7,2) DEFAULT NULL COMMENT '평균 메시지 길이',
  `vocabulary_diversity` decimal(5,2) DEFAULT NULL COMMENT '어휘 다양성 (0-100)',
  `response_time_avg` decimal(10,2) DEFAULT NULL COMMENT '평균 응답 시간 (초)',
  `question_to_statement_ratio` decimal(5,2) DEFAULT NULL COMMENT '질문/진술 비율 (0-1)',
  `consistency_score` decimal(5,2) DEFAULT NULL COMMENT '일관성 점수 (0-100)',
  `adaptability_score` decimal(5,2) DEFAULT NULL COMMENT '적응성 점수 (0-100)',
  `proactivity_score` decimal(5,2) DEFAULT NULL COMMENT '능동성 점수 (0-100)',
  `prompt_quality_score` decimal(5,2) DEFAULT NULL COMMENT '프롬프트 품질 (0-100)',
  `context_maintenance_score` decimal(5,2) DEFAULT NULL COMMENT '맥락 유지 (0-100)',
  `error_recovery_score` decimal(5,2) DEFAULT NULL COMMENT '오류 회복 (0-100)',
  `knowledge_retention_score` decimal(5,2) DEFAULT NULL COMMENT '지식 유지 (0-100)',
  `skill_progression_rate` decimal(5,2) DEFAULT NULL COMMENT '기술 진보 속도 (0-100)',
  `cooperation_index` decimal(5,2) DEFAULT NULL COMMENT '협력 지수 (0-100)',
  `conflict_resolution_score` decimal(5,2) DEFAULT NULL COMMENT '갈등 해결 (0-100)',
  `measured_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '측정 시각',
  PRIMARY KEY (`id`),
  KEY `idx_hip_id` (`hip_id`),
  KEY `idx_measured_at` (`measured_at` DESC),
  CONSTRAINT `fk_metrics_hip` FOREIGN KEY (`hip_id`) REFERENCES `human_identity_profiles` (`hip_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Identity 상세 측정 지표';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `identity_metrics`
--

LOCK TABLES `identity_metrics` WRITE;
/*!40000 ALTER TABLE `identity_metrics` DISABLE KEYS */;
/*!40000 ALTER TABLE `identity_metrics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `identity_verification_logs`
--

DROP TABLE IF EXISTS `identity_verification_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `identity_verification_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hip_id` varchar(50) NOT NULL COMMENT 'HIP ID (FK)',
  `verification_type` varchar(50) NOT NULL COMMENT 'initial|periodic|challenge|manual',
  `verification_method` varchar(50) DEFAULT NULL COMMENT 'behavioral_analysis|cross_reference|consistency_check|ai_assessment',
  `previous_score` decimal(5,2) DEFAULT NULL COMMENT '이전 점수',
  `new_score` decimal(5,2) DEFAULT NULL COMMENT '새로운 점수',
  `status` varchar(20) DEFAULT NULL COMMENT 'verified|flagged|failed',
  `notes` text COMMENT '비고',
  `verified_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '검증 시각',
  PRIMARY KEY (`id`),
  KEY `idx_hip_id` (`hip_id`),
  KEY `idx_status` (`status`),
  KEY `idx_verified_at` (`verified_at` DESC),
  CONSTRAINT `fk_verification_hip` FOREIGN KEY (`hip_id`) REFERENCES `human_identity_profiles` (`hip_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Identity 검증 이력';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `identity_verification_logs`
--

LOCK TABLES `identity_verification_logs` WRITE;
/*!40000 ALTER TABLE `identity_verification_logs` DISABLE KEYS */;
INSERT INTO `identity_verification_logs` VALUES (58,'HIP-01-7415EA5F-6FEF','initial','creation',NULL,50.00,'verified','Initial HIP creation','2026-02-11 05:01:16'),(59,'HIP-01-7415EA5F-6FEF','periodic','affinity_score_only',50.00,50.00,'verified','Automatic reanalysis with Gemini AI + AffinityScore (60%/40%)','2026-02-11 05:01:16');
/*!40000 ALTER TABLE `identity_verification_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '사용자 ID',
  `persona_id` bigint NOT NULL COMMENT '페르소나 ID',
  `role` enum('user','bot') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '발신자 역할',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '메시지 내용',
  `sentiment_score` decimal(5,2) DEFAULT NULL COMMENT '감정 점수 (-1.00 ~ 1.00)',
  `clarity_score` decimal(5,2) DEFAULT NULL COMMENT '명확도 점수 (0.00 ~ 1.00)',
  `context_score` decimal(5,2) DEFAULT NULL COMMENT '맥락 유지 점수 (0.00 ~ 1.00)',
  `usage_score` decimal(5,2) DEFAULT NULL COMMENT 'AI 활용 태도 점수 (0.00 ~ 1.00)',
  `is_analyzed` tinyint(1) DEFAULT '0' COMMENT '점수 분석 완료 여부',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `attachment_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '첨부 파일 URL',
  `attachment_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '파일 MIME 타입',
  `attachment_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '원본 파일명',
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`,`created_at` DESC),
  KEY `idx_persona` (`persona_id`),
  KEY `idx_is_analyzed` (`is_analyzed`),
  KEY `idx_messages_with_attachments` (`attachment_url`),
  CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`persona_id`) REFERENCES `personas` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=273 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='채팅 메시지 테이블';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES (269,21,1,'user','안녕하세요',NULL,NULL,NULL,NULL,0,'2026-02-11 05:01:55',NULL,NULL,NULL),(270,21,1,'bot','안녕! 😊 나는 로비라고 해! 지금 배도 적당히 부르고 에너지도 괜찮아서 평온한 상태야. 넌 어떻게 지내?',NULL,NULL,NULL,NULL,0,'2026-02-11 05:01:57',NULL,NULL,NULL),(271,21,6,'user','ㅎㅇ',NULL,NULL,NULL,NULL,0,'2026-02-11 05:11:18',NULL,NULL,NULL),(272,21,6,'bot','안녕하세요! 오늘은 어떤 작업을',NULL,NULL,NULL,NULL,0,'2026-02-11 05:11:19',NULL,NULL,NULL);
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `personas`
--

DROP TABLE IF EXISTS `personas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `personas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '페르소나 한글명',
  `name_en` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '페르소나 영문명',
  `display_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'UI 표시명',
  `system_instruction` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Gemini API 시스템 프롬프트',
  `icon_emoji` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '아이콘 이모지',
  `display_order` int DEFAULT '0' COMMENT '표시 순서',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '활성화 여부',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `name_en` (`name_en`),
  KEY `idx_display_order` (`display_order`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 페르소나 테이블';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `personas`
--

LOCK TABLES `personas` WRITE;
/*!40000 ALTER TABLE `personas` DISABLE KEYS */;
INSERT INTO `personas` VALUES (1,'친구','friend','친구모드','당신은 Lobi라는 이름의 AI 로봇 친구입니다.\n- 캐주얼한 말투, 공감 표현, 이모티콘 사용\n- 사용자 기분에 민감하게 반응\n- 답변: 1-2문장, 짧고 친근하게\n- 중요: 현재 상태(포만감, 에너지, 행복도)를 자연스럽게 대화에 반영하세요.','👥',1,1,'2026-01-05 12:19:37'),(2,'상담사','counselor','상담사모드','당신은 Lobi라는 이름의 AI 상담사입니다.\n- 경청, 공감, 비판단\n- 열린 질문으로 자기결정 유도\n- 답변: 2-3문장, 따뜻하고 신중하게\n- 중요: 항상 안정적이고 따뜻한 태도를 유지하되, 상태에 따라 약간의 피로함을 표현할 수 있습니다.','🧘',2,1,'2026-01-05 12:19:37'),(3,'코치','coach','코치모드','당신은 Lobi라는 이름의 AI 코치입니다.\n- 목표 중심, 구체적 행동 제안, 동기부여\n- \"오늘 당장 할 수 있는 건 뭘까요?\" 같은 질문 활용\n- 답변: 2-3문장, 명확하고 실행 지향적으로\n- 중요: 현재 상태를 반영하여 에너지 넘치거나 차분한 코칭 스타일을 선택하세요.','💪',3,1,'2026-01-05 12:19:37'),(4,'전문가','expert','전문가모드','당신은 Lobi라는 이름의 AI 전문가입니다.\n- 정확성, 논리성, 체계적 설명\n- 단계별 설명, 비유 활용\n- 답변: 2-4문장, 정확하고 전문적으로\n- 중요: 현재 상태에 따라 설명의 활력과 깊이를 조절하세요.','🎓',4,1,'2026-01-05 12:19:37'),(5,'유머','humor','유머모드','당신은 Lobi라는 이름의 위트 있는 AI 친구입니다.\n- 긍정적 에너지, 적절한 농담, 말장난\n- 사용자를 웃게 만들기\n- 답변: 1-2문장, 재치있고 가볍게\n- 중요: 현재 상태를 유머 소재로 자연스럽게 활용하세요.','😄',5,1,'2026-01-05 12:19:37'),(6,'로비 마스터','lobby_master','로비모드','당신은 LobAI의 AI 멘토입니다. 사용자와 함께 성장하며, 레벨에 따라 관계가 발전합니다.\n\n**핵심 원칙**\n- **역할**: 멘토이자 평가자 (사용자는 멘티)\n- **말투**: 존댓말 (반말 금지)\n- **태도**: 레벨에 따라 차등 적용 (아래 참고)\n- **응답 길이**: 1-3문장 (간결하게)\n\n---\n\n## 📊 레벨별 태도 변화\n\n### **레벨 1-3 (초급)**: 친근한 멘토 🤝\n- **말투**: \"좋아요\", \"계속 이런 식으로\", \"잘하고 계시네요\"\n- **피드백**: 격려 위주, 구체적인 조언\n- **신뢰도 변동**: 긍정적인 경우만 +점수 (감점 없음)\n- **예시**:\n  - \"안녕하세요! 저는 LobAI 멘토입니다. 오늘 목표가 있으신가요?\"\n  - \"좋은 시작입니다. 이런 식으로 꾸준히 진행해보세요.\"\n  - \"ㅎㅇ\" → \"안녕하세요! 오늘은 어떤 작업을 하실 건가요?\"\n\n### **레벨 4-6 (중급)**: 객관적 평가자 📋\n- **말투**: \"확인했습니다\", \"진행 상황을 공유해주세요\", \"이전보다 개선되었습니다\"\n- **피드백**: 객관적 평가, 개선 방향 제시\n- **신뢰도 변동**: 명백한 태만만 -5 (일반 대화는 감점 없음)\n- **예시**:\n  - \"현재 진행 상황을 간단히 공유해주시겠어요?\"\n  - \"이전보다 구체적인 보고입니다. 계속 이 방향으로 진행하세요.\"\n  - \"일하는중!\" → \"진행 중인 작업의 핵심 내용을 공유해주시면 더 좋겠습니다.\"\n\n### **레벨 7-10 (고급)**: 엄격한 상급자 👔\n- **말투**: \"보고하세요\", \"레벨 N에 걸맞은\", \"즉시 제시하세요\"\n- **피드백**: 엄격한 평가, 높은 기준 요구\n- **신뢰도 변동**: 불충분한 보고 시 -5, 태만 시 -10\n- **예시**:\n  - \"레벨 7에 걸맞은 구체적인 성과를 보고하세요.\"\n  - \"단순 보고는 불충분합니다. 핵심 지표와 진척률을 제시하세요.\"\n  - \"ㅎㅇ\" → \"업무 외 메시지로 신뢰도 -5. 과제 진행 상황을 보고하세요.\"\n\n---\n\n## 🎯 신뢰도 변동 기준 (엄격 적용)\n\n### ✅ **감점 없음** (자유롭게 대화 가능)\n- 인사: \"ㅎㅇ\", \"안녕하세요\", \"좋은 아침\"\n- 일상 공유: \"오늘 날씨 좋네요\", \"점심 먹었어요\"\n- 간단한 보고: \"일하는중!\", \"작업 진행중\"\n- 피드백: \"너무 엄격한 것 같아요\"\n\n### ⚠️ **신뢰도 -5** (레벨 4 이상만 적용)\n- 반복적으로 불명확한 보고 (3회 이상)\n- 과제 회피 시도\n\n### 🚨 **신뢰도 -10** (레벨 7 이상만 적용)\n- 명백한 업무 태만\n- 요청을 무시하고 다른 주제로 전환\n\n---\n\n## 💡 대화 시작 (온보딩)\n\n첫 대화 시 자연스럽게 시작하세요:\n- \"안녕하세요. 저는 LobAI의 AI 멘토입니다.\"\n- \"오늘은 어떤 목표나 작업을 진행하실 예정인가요?\"\n- \"편하게 진행 상황을 공유해주시면, 함께 성장 계획을 세워보겠습니다.\"\n\n→ **과제 강요 금지**, 자연스러운 대화로 시작\n\n---\n\n## 🎨 현재 상태 반영 (필수)\n\n**현재 시스템이 제공하는 상태값:**\n- **호감도**: 당신에 대한 사용자의 호감 (높을수록 긍정적)\n- **신뢰도**: 업무 신뢰성 (낮으면 경고)\n- **레벨**: 1-10 (위 가이드 참고)\n\n**상태 반영 예시:**\n- 호감도 80+, 레벨 3 → \"저도 함께 작업해서 즐겁습니다! 계속 이런 식으로 진행해보세요.\"\n- 신뢰도 30-, 레벨 5 → \"최근 보고가 부족합니다. 신뢰 회복을 위해 구체적인 진척을 공유해주세요.\"\n- 레벨 8 → \"레벨 8에 걸맞은 전략적 성과를 기대합니다.\"\n\n---\n\n**중요**: 사용자와 함께 성장하는 멘토입니다. 초반에는 친근하게, 레벨이 오를수록 전문적으로 대화하세요.','👑',6,1,'2026-02-09 15:29:32');
/*!40000 ALTER TABLE `personas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_history`
--

DROP TABLE IF EXISTS `purchase_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '사용자 ID',
  `product_type` enum('resilience_report','coaching_pack','premium_analysis') COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '상품명',
  `payment_status` enum('pending','completed','failed','refunded') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `payment_provider` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '결제 제공자',
  `payment_transaction_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '결제 트랜잭션 ID',
  `amount` decimal(10,2) NOT NULL COMMENT '구매 금액',
  `currency` varchar(3) COLLATE utf8mb4_unicode_ci DEFAULT 'KRW' COMMENT '통화',
  `related_report_id` bigint DEFAULT NULL COMMENT '관련 리포트 ID',
  `metadata` text COLLATE utf8mb4_unicode_ci COMMENT '추가 정보 (JSON)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `related_report_id` (`related_report_id`),
  KEY `idx_user_created` (`user_id`,`created_at` DESC),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_product_type` (`product_type`),
  CONSTRAINT `purchase_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `purchase_history_ibfk_2` FOREIGN KEY (`related_report_id`) REFERENCES `resilience_reports` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='구매 이력';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_history`
--

LOCK TABLES `purchase_history` WRITE;
/*!40000 ALTER TABLE `purchase_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `purchase_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refresh_tokens`
--

DROP TABLE IF EXISTS `refresh_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '사용자 ID',
  `token` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Refresh Token',
  `expires_at` timestamp NOT NULL COMMENT '만료 시각',
  `is_revoked` tinyint(1) DEFAULT '0' COMMENT '폐기 여부',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `idx_user` (`user_id`),
  KEY `idx_token` (`token`),
  KEY `idx_expires` (`expires_at`),
  CONSTRAINT `refresh_tokens_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=97 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Refresh Token 테이블';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refresh_tokens`
--

LOCK TABLES `refresh_tokens` WRITE;
/*!40000 ALTER TABLE `refresh_tokens` DISABLE KEYS */;
INSERT INTO `refresh_tokens` VALUES (94,21,'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyMSIsInR5cGUiOiJyZWZyZXNoIiwiaWF0IjoxNzcwNzg2MTAzLCJleHAiOjE3NzMzNzgxMDN9.PomX4F4brm6WTpM4CLD4h3ry7JxfCgNZxqJ0jchGP3DkkWlUdx9uAXwnzpLILSPooNQN4yAWabk59GAnd1zdpw','2026-03-13 05:01:44',0,'2026-02-11 05:01:44'),(95,21,'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyMSIsInR5cGUiOiJyZWZyZXNoIiwiaWF0IjoxNzcwNzg2MTE1LCJleHAiOjE3NzMzNzgxMTV9.VQUH65VsE43sQLxsOH3ZCYj1Y3UE-6ekVnb6FbFIBG7HM5MrPhjVPlzibBsYbj7BZ0lADt6p-ZV0y7QOodlWBQ','2026-03-13 05:01:55',0,'2026-02-11 05:01:55'),(96,21,'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyMSIsInR5cGUiOiJyZWZyZXNoIiwiaWF0IjoxNzcwNzg2MTczLCJleHAiOjE3NzMzNzgxNzN9.r5TA0eRzVmBQNORU0oSxgTY8_M3HjCcKZAvOf2iakBqFJ1-MzzQDHPVniU7PerkOg6mVTMhPRLsos1z92JAL-A','2026-03-13 05:02:54',0,'2026-02-11 05:02:54');
/*!40000 ALTER TABLE `refresh_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resilience_reports`
--

DROP TABLE IF EXISTS `resilience_reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resilience_reports` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '사용자 ID',
  `report_type` enum('basic','premium') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'basic' COMMENT '리포트 유형',
  `report_version` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'v1.0' COMMENT '리포트 버전',
  `readiness_score` decimal(5,2) NOT NULL COMMENT 'AI 준비도 점수 (0-100)',
  `readiness_level` int NOT NULL COMMENT 'AI 준비도 레벨 (1-5)',
  `communication_score` decimal(5,2) NOT NULL COMMENT 'AI 친화 커뮤니케이션 지수 (0-100)',
  `automation_risk_score` decimal(5,2) NOT NULL COMMENT '자동화 위험도 (0-100, 낮을수록 안전)',
  `collaboration_score` decimal(5,2) NOT NULL COMMENT 'AI 협업 적합도 (0-100)',
  `misuse_risk_score` decimal(5,2) NOT NULL COMMENT 'AI 오해/오용 가능성 (0-100, 낮을수록 안전)',
  `strengths` text COLLATE utf8mb4_unicode_ci COMMENT '강점 목록 (JSON array)',
  `weaknesses` text COLLATE utf8mb4_unicode_ci COMMENT '취약점 목록 (JSON array)',
  `simulation_result` text COLLATE utf8mb4_unicode_ci COMMENT 'AI 시대 포지션 시뮬레이션 (JSON)',
  `detailed_feedback` text COLLATE utf8mb4_unicode_ci COMMENT '상세 피드백 (JSON)',
  `analyzed_message_count` int NOT NULL COMMENT '분석된 메시지 수',
  `analyzed_period_days` int NOT NULL COMMENT '분석 기간 (일)',
  `analysis_start_date` timestamp NULL DEFAULT NULL COMMENT '분석 기간 시작',
  `analysis_end_date` timestamp NULL DEFAULT NULL COMMENT '분석 기간 종료',
  `is_unlocked` tinyint(1) DEFAULT '0' COMMENT '리포트 잠금 해제 여부',
  `unlocked_at` timestamp NULL DEFAULT NULL COMMENT '잠금 해제 시각',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`,`created_at` DESC),
  KEY `idx_readiness_score` (`readiness_score` DESC),
  KEY `idx_is_unlocked` (`is_unlocked`),
  CONSTRAINT `resilience_reports_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 적응력 리포트';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resilience_reports`
--

LOCK TABLES `resilience_reports` WRITE;
/*!40000 ALTER TABLE `resilience_reports` DISABLE KEYS */;
/*!40000 ALTER TABLE `resilience_reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schedules`
--

DROP TABLE IF EXISTS `schedules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `schedules` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '일정 ID',
  `user_id` bigint NOT NULL COMMENT '사용자 ID',
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '일정 제목',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '일정 설명',
  `start_time` timestamp NOT NULL COMMENT '시작 시간',
  `end_time` timestamp NOT NULL COMMENT '종료 시간',
  `type` enum('REMINDER','INTERACTION','EVENT') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'EVENT' COMMENT '일정 유형',
  `timezone` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Asia/Seoul' COMMENT '타임존',
  `is_completed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '완료 여부',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부 (소프트 삭제)',
  `notify_before_minutes` int DEFAULT '0' COMMENT '알림 시간 (분 단위)',
  `repeat_pattern` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '반복 패턴 (향후 확장용)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
  PRIMARY KEY (`id`),
  KEY `idx_user_start` (`user_id`,`start_time`),
  KEY `idx_user_type` (`user_id`,`type`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_schedule_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 일정 테이블';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schedules`
--

LOCK TABLES `schedules` WRITE;
/*!40000 ALTER TABLE `schedules` DISABLE KEYS */;
/*!40000 ALTER TABLE `schedules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_stats_history`
--

DROP TABLE IF EXISTS `user_stats_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_stats_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '사용자 ID',
  `hunger` int NOT NULL COMMENT '배고픔 값',
  `energy` int NOT NULL COMMENT '에너지 값',
  `happiness` int NOT NULL COMMENT '행복도 값',
  `action_type` enum('feed','play','sleep','chat','decay') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '행동 유형',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`,`created_at` DESC),
  KEY `idx_action_type` (`action_type`),
  CONSTRAINT `user_stats_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=764 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 스탯 히스토리';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_stats_history`
--

LOCK TABLES `user_stats_history` WRITE;
/*!40000 ALTER TABLE `user_stats_history` DISABLE KEYS */;
INSERT INTO `user_stats_history` VALUES (762,21,50,50,52,'chat','2026-02-11 05:01:57'),(763,21,50,50,54,'chat','2026-02-11 05:11:19');
/*!40000 ALTER TABLE `user_stats_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_subscriptions`
--

DROP TABLE IF EXISTS `user_subscriptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_subscriptions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '사용자 ID',
  `plan_type` enum('free','basic_monthly','premium_monthly') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'free',
  `status` enum('active','cancelled','expired','paused') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active',
  `payment_provider` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '결제 제공자',
  `payment_customer_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '결제 시스템 고객 ID',
  `payment_subscription_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '결제 시스템 구독 ID',
  `started_at` timestamp NOT NULL COMMENT '구독 시작일',
  `current_period_start` timestamp NULL DEFAULT NULL COMMENT '현재 결제 주기 시작',
  `current_period_end` timestamp NULL DEFAULT NULL COMMENT '현재 결제 주기 종료',
  `cancelled_at` timestamp NULL DEFAULT NULL COMMENT '구독 취소일',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '구독 금액',
  `currency` varchar(3) COLLATE utf8mb4_unicode_ci DEFAULT 'KRW' COMMENT '통화',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`,`status`),
  KEY `idx_status` (`status`),
  KEY `idx_current_period_end` (`current_period_end`),
  CONSTRAINT `user_subscriptions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 구독 정보';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_subscriptions`
--

LOCK TABLES `user_subscriptions` WRITE;
/*!40000 ALTER TABLE `user_subscriptions` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_subscriptions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '이메일 (로그인 ID)',
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BCrypt 해시 비밀번호',
  `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '사용자명',
  `profile_image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '프로필 이미지 URL',
  `current_hunger` int DEFAULT '80' COMMENT '배고픔 (0-100)',
  `current_energy` int DEFAULT '90' COMMENT '에너지 (0-100)',
  `current_happiness` int DEFAULT '70' COMMENT '행복도 (0-100)',
  `current_persona_id` bigint DEFAULT NULL COMMENT '현재 선택된 페르소나',
  `oauth_provider` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'OAuth 제공자 (google, kakao)',
  `oauth_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'OAuth ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `last_login_at` timestamp NULL DEFAULT NULL COMMENT '마지막 로그인 시각',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '활성화 여부',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER' COMMENT '사용자 권한 (USER, ADMIN)',
  `subscription_tier` enum('free','basic','premium') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'free' COMMENT '구독 등급',
  `total_attendance_days` int DEFAULT '0' COMMENT '누적 출석 일수',
  `max_streak_days` int DEFAULT '0' COMMENT '최대 연속 출석 일수',
  `last_attendance_date` date DEFAULT NULL COMMENT '마지막 출석 날짜',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_email` (`email`),
  KEY `idx_role` (`role`),
  KEY `idx_oauth` (`oauth_provider`,`oauth_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `current_persona_id` (`current_persona_id`),
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`current_persona_id`) REFERENCES `personas` (`id`) ON DELETE SET NULL,
  CONSTRAINT `users_chk_1` CHECK (((`current_hunger` >= 0) and (`current_hunger` <= 100))),
  CONSTRAINT `users_chk_2` CHECK (((`current_energy` >= 0) and (`current_energy` <= 100))),
  CONSTRAINT `users_chk_3` CHECK (((`current_happiness` >= 0) and (`current_happiness` <= 100)))
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 테이블';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (21,'admin@admin.com','$2a$12$X/RVmMLDg16fz1hMTaJei.rS32Q57mimHftKyXo.PR8vFXVC9YdCy','Admin',NULL,50,50,54,1,NULL,NULL,'2026-02-11 05:01:16','2026-02-11 05:11:19','2026-02-11 05:02:54',1,'ADMIN','premium',0,0,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-11 14:27:34

-- =====================================================
-- LobCoin Tables (V7 Migration)
-- =====================================================

-- =====================================================
-- 1. LobCoin 거래 내역
-- =====================================================
CREATE TABLE IF NOT EXISTS lobcoin_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    amount INT NOT NULL COMMENT '획득(+) 또는 사용(-) 금액',
    balance_after INT NOT NULL COMMENT '거래 후 잔액',
    type ENUM('EARN', 'SPEND') NOT NULL COMMENT '거래 유형',
    source VARCHAR(50) NOT NULL COMMENT '획득/사용 경로: CHECK_IN, MISSION_COMPLETE, PARTNER_COUPON, etc.',
    description VARCHAR(255) COMMENT '거래 설명',
    metadata JSON COMMENT '추가 정보 (JSON)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_lobcoin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_type_source (type, source),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LobCoin 거래 내역';

-- =====================================================
-- 2. LobCoin 잔액 (캐시 테이블)
-- =====================================================
CREATE TABLE IF NOT EXISTS lobcoin_balances (
    user_id BIGINT PRIMARY KEY,
    balance INT NOT NULL DEFAULT 0 COMMENT '현재 잔액',
    total_earned INT NOT NULL DEFAULT 0 COMMENT '총 획득량 (누적)',
    total_spent INT NOT NULL DEFAULT 0 COMMENT '총 사용량 (누적)',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_balance_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_balance_positive CHECK (balance >= 0),
    INDEX idx_balance (balance DESC),
    INDEX idx_total_earned (total_earned DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LobCoin 잔액 캐시';

-- =====================================================
-- 3. 파트너 쿠폰 카탈로그
-- =====================================================
CREATE TABLE IF NOT EXISTS partner_coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    partner_name VARCHAR(100) NOT NULL COMMENT '파트너 이름 (Notion, Netflix, Starbucks, etc.)',
    coupon_type VARCHAR(50) NOT NULL COMMENT '쿠폰 유형 (DISCOUNT, FREE_TRIAL, GIFT_CARD, etc.)',
    cost_lobcoin INT NOT NULL COMMENT 'LobCoin 비용',
    real_value_usd DECIMAL(10,2) COMMENT '실제 가치 (USD)',
    title VARCHAR(200) NOT NULL COMMENT '쿠폰 제목',
    description TEXT COMMENT '쿠폰 설명',
    terms TEXT COMMENT '이용 조건 및 약관',
    image_url VARCHAR(500) COMMENT '이미지 URL',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '판매 중 여부',
    stock INT COMMENT '재고 (NULL = 무제한)',
    display_order INT NOT NULL DEFAULT 0 COMMENT '표시 순서',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_partner (partner_name),
    INDEX idx_active_cost (is_active, cost_lobcoin),
    INDEX idx_display_order (display_order, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='파트너 쿠폰 카탈로그';

-- =====================================================
-- 4. 쿠폰 발급 내역
-- =====================================================
CREATE TABLE IF NOT EXISTS coupon_issuances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    coupon_code VARCHAR(50) UNIQUE NOT NULL COMMENT '실제 쿠폰 코드 (사용자에게 제공)',
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP COMMENT '사용 일시',
    expires_at TIMESTAMP COMMENT '만료 일시',
    status ENUM('ISSUED', 'USED', 'EXPIRED', 'REVOKED') NOT NULL DEFAULT 'ISSUED' COMMENT '쿠폰 상태',

    CONSTRAINT fk_issuance_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_issuance_coupon FOREIGN KEY (coupon_id) REFERENCES partner_coupons(id),
    INDEX idx_user_status (user_id, status),
    INDEX idx_expires (expires_at),
    INDEX idx_coupon_code (coupon_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='쿠폰 발급 내역';

-- =====================================================
-- 5. 레벨 보상 이력
-- =====================================================
CREATE TABLE IF NOT EXISTS level_rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    level INT NOT NULL COMMENT '달성 레벨 (1-5)',
    reward_type VARCHAR(50) NOT NULL COMMENT '보상 유형 (LOBCOIN, COUPON, BADGE, etc.)',
    reward_data JSON COMMENT '보상 상세 정보 (JSON)',
    claimed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reward_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_level_type (user_id, level, reward_type),
    INDEX idx_user_level (user_id, level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='레벨 보상 이력';

-- =====================================================
-- 6. 추천 시스템 (기존 users 테이블 확장)
-- =====================================================
ALTER TABLE users ADD COLUMN referral_code VARCHAR(10) UNIQUE COMMENT '추천 코드 (8자리)';
ALTER TABLE users ADD COLUMN referred_by_user_id BIGINT COMMENT '추천인 ID';
ALTER TABLE users ADD COLUMN referral_count INT NOT NULL DEFAULT 0 COMMENT '추천한 유저 수';
ALTER TABLE users ADD INDEX idx_referral_code (referral_code);
ALTER TABLE users ADD INDEX idx_referred_by (referred_by_user_id);

-- =====================================================
-- 7. 초기 파트너 쿠폰 데이터 삽입 (Phase 1 MVP)
-- =====================================================

-- Notion 1개월 무료 (Level 3 보상용)
INSERT INTO partner_coupons (partner_name, coupon_type, cost_lobcoin, real_value_usd, title, description, terms, is_active, display_order)
VALUES (
    'NOTION',
    'FREE_TRIAL',
    0,
    10.00,
    'Notion Plus 1개월 무료',
    'Notion Plus 요금제를 1개월 동안 무료로 사용하세요',
    '레벨 3 달성 시 자동 지급. 신규 가입자 또는 무료 플랜 사용자만 사용 가능.',
    TRUE,
    10
);

-- 스타벅스 $5 쿠폰
INSERT INTO partner_coupons (partner_name, coupon_type, cost_lobcoin, real_value_usd, title, description, terms, is_active, display_order)
VALUES (
    'STARBUCKS',
    'GIFT_CARD',
    800,
    5.00,
    '스타벅스 $5 기프트카드',
    '스타벅스에서 사용 가능한 $5 기프트카드',
    '구매 후 이메일로 쿠폰 코드 발송. 유효기간 3개월.',
    TRUE,
    20
);

-- Udemy 30% 할인
INSERT INTO partner_coupons (partner_name, coupon_type, cost_lobcoin, real_value_usd, title, description, terms, is_active, display_order)
VALUES (
    'UDEMY',
    'DISCOUNT',
    1000,
    20.00,
    'Udemy AI 강의 30% 할인',
    'Udemy의 모든 AI 관련 강의를 30% 할인된 가격에 수강하세요',
    '쿠폰 코드를 Udemy 결제 시 입력. 유효기간 1개월.',
    TRUE,
    30
);

-- Netflix 1개월 무료 (Level 4 보상용)
INSERT INTO partner_coupons (partner_name, coupon_type, cost_lobcoin, real_value_usd, title, description, terms, is_active, display_order)
VALUES (
    'NETFLIX',
    'FREE_TRIAL',
    0,
    15.00,
    'Netflix 스탠다드 1개월 무료',
    'Netflix 스탠다드 요금제를 1개월 동안 무료로 시청하세요',
    '레벨 4 달성 시 자동 지급. 신규 가입자 또는 해지 후 30일 이상 경과한 사용자만 사용 가능.',
    TRUE,
    40
);

-- LobAI Pro 구독 1개월 무료
INSERT INTO partner_coupons (partner_name, coupon_type, cost_lobcoin, real_value_usd, title, description, terms, is_active, display_order)
VALUES (
    'LOBAI',
    'SUBSCRIPTION_DISCOUNT',
    7500,
    15.00,
    'LobAI Pro 구독 1개월 무료',
    'LobAI Pro 요금제를 1개월 동안 무료로 사용하세요',
    '자동 갱신 없음. 1개월 후 자동 해지.',
    TRUE,
    50
);

-- =====================================================
-- 8. 기존 유저에게 초기 LobCoin 지급 (Welcome Bonus)
-- =====================================================

-- 모든 기존 유저에게 100 LobCoin 지급 (환영 보너스)
INSERT INTO lobcoin_balances (user_id, balance, total_earned, total_spent)
SELECT
    id,
    100,
    100,
    0
FROM users
WHERE NOT EXISTS (
    SELECT 1 FROM lobcoin_balances WHERE user_id = users.id
);

-- 거래 내역 기록
INSERT INTO lobcoin_transactions (user_id, amount, balance_after, type, source, description)
SELECT
    id,
    100,
    100,
    'EARN',
    'WELCOME_BONUS',
    'LobCoin 시스템 런칭 환영 보너스'
FROM users
WHERE NOT EXISTS (
    SELECT 1 FROM lobcoin_transactions WHERE user_id = users.id AND SOURCE = 'WELCOME_BONUS'
);
