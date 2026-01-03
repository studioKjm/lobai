package com.lobai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lobai.entity.Message;
import com.lobai.entity.ResilienceReport;
import com.lobai.entity.User;
import com.lobai.repository.MessageRepository;
import com.lobai.repository.ResilienceReportRepository;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * ResilienceAnalysisService
 * AI 적응력 분석 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResilienceAnalysisService {

    private final MessageRepository messageRepository;
    private final ResilienceReportRepository resilienceReportRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    private static final int MINIMUM_MESSAGES_FOR_ANALYSIS = 10;
    private static final int ANALYSIS_PERIOD_DAYS = 30;

    /**
     * AI 적응력 리포트 생성
     * @param userId 사용자 ID
     * @param reportType 리포트 유형 (basic, premium)
     * @return 생성된 리포트
     * @throws IllegalStateException 분석할 메시지가 부족한 경우
     */
    @Transactional
    public ResilienceReport generateReport(Long userId, ResilienceReport.ReportType reportType) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 2. 분석 기간 설정 (최근 30일)
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(ANALYSIS_PERIOD_DAYS);

        // 3. 사용자 메시지 조회 (최근 100개)
        Pageable limit = PageRequest.of(0, 100);
        List<Message> userMessages = messageRepository.findRecentUserMessages(userId, limit);

        if (userMessages.size() < MINIMUM_MESSAGES_FOR_ANALYSIS) {
            throw new IllegalStateException(
                    String.format("분석할 메시지가 부족합니다 (최소 %d개 필요, 현재 %d개)",
                            MINIMUM_MESSAGES_FOR_ANALYSIS, userMessages.size()));
        }

        // 4. 4가지 핵심 점수 계산
        BigDecimal communicationScore = calculateCommunicationScore(userMessages);
        BigDecimal automationRiskScore = calculateAutomationRiskScore(userMessages);
        BigDecimal collaborationScore = calculateCollaborationScore(userMessages);
        BigDecimal misuseRiskScore = calculateMisuseRiskScore(userMessages);

        // 5. AI Readiness Score 계산
        BigDecimal readinessScore = calculateReadinessScore(
                communicationScore, automationRiskScore, collaborationScore, misuseRiskScore);

        // 6. Readiness Level 결정 (1-5)
        Integer readinessLevel = determineReadinessLevel(readinessScore);

        // 7. 리포트 엔티티 생성
        ResilienceReport report = ResilienceReport.builder()
                .userId(userId)
                .reportType(reportType)
                .reportVersion("v1.0")
                .readinessScore(readinessScore)
                .readinessLevel(readinessLevel)
                .communicationScore(communicationScore)
                .automationRiskScore(automationRiskScore)
                .collaborationScore(collaborationScore)
                .misuseRiskScore(misuseRiskScore)
                .analyzedMessageCount(userMessages.size())
                .analyzedPeriodDays((int) ChronoUnit.DAYS.between(startDate, endDate))
                .analysisStartDate(startDate)
                .analysisEndDate(endDate)
                .isUnlocked(reportType == ResilienceReport.ReportType.basic) // basic은 자동 잠금 해제
                .build();

        // 8. AI 분석 결과 생성 (Gemini 사용)
        if (reportType == ResilienceReport.ReportType.premium || true) { // MVP에서는 모두 생성
            generateAIInsights(report, userMessages);
        }

        // 9. 저장 및 반환
        ResilienceReport saved = resilienceReportRepository.save(report);

        log.info("AI 적응력 리포트 생성 완료: userId={}, type={}, readiness={}/100, level={}",
                userId, reportType, readinessScore, readinessLevel);

        return saved;
    }

    /**
     * AI 친화 커뮤니케이션 지수 계산
     * - 메시지 명확성 (clarity)
     * - 평균 메시지 길이 (적절한 길이)
     * - 구체성 (구체적인 요청/질문 포함 여부)
     */
    private BigDecimal calculateCommunicationScore(List<Message> messages) {
        // clarity 점수 평균
        double avgClarity = messages.stream()
                .filter(m -> m.getClarityScore() != null)
                .mapToDouble(m -> m.getClarityScore().doubleValue())
                .average()
                .orElse(0.5);

        // 평균 메시지 길이 (20-150자가 적절)
        double avgLength = messages.stream()
                .mapToInt(m -> m.getContent().length())
                .average()
                .orElse(0);

        double lengthScore = 0.0;
        if (avgLength >= 20 && avgLength <= 150) {
            lengthScore = 1.0;
        } else if (avgLength < 20) {
            lengthScore = avgLength / 20.0;
        } else {
            lengthScore = Math.max(0, 1.0 - (avgLength - 150) / 200.0);
        }

        // 질문 포함 비율
        long questionCount = messages.stream()
                .filter(m -> m.getContent().contains("?") || m.getContent().contains("어떻게") ||
                        m.getContent().contains("무엇") || m.getContent().contains("왜"))
                .count();
        double questionRatio = (double) questionCount / messages.size();

        // 가중 평균
        double score = (avgClarity * 100 * 0.5) + (lengthScore * 100 * 0.3) + (questionRatio * 100 * 0.2);

        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 자동화 위험도 계산 (0-100, 낮을수록 안전)
     * - 반복적인 단순 질문 비율
     * - 창의성 부족 (유사한 메시지 반복)
     * - AI 의존도
     */
    private BigDecimal calculateAutomationRiskScore(List<Message> messages) {
        // 단순 명령어 패턴 (높을수록 위험)
        long simpleCommandCount = messages.stream()
                .filter(m -> {
                    String content = m.getContent();
                    return content.length() < 10 ||
                            content.matches(".*\\b(해줘|알려줘|보여줘|찾아줘)\\b.*");
                })
                .count();
        double simpleRatio = (double) simpleCommandCount / messages.size();

        // 메시지 다양성 (낮을수록 위험)
        Set<String> uniqueMessages = new HashSet<>();
        messages.forEach(m -> uniqueMessages.add(m.getContent().toLowerCase()));
        double diversity = (double) uniqueMessages.size() / messages.size();

        // 위험도 = (단순 명령 비율 * 70) + ((1 - 다양성) * 30)
        double riskScore = (simpleRatio * 70) + ((1 - diversity) * 30);

        return BigDecimal.valueOf(riskScore).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * AI 협업 적합도 계산
     * - 맥락 유지 능력 (context score)
     * - 대화 지속성
     * - 피드백 반영 능력
     */
    private BigDecimal calculateCollaborationScore(List<Message> messages) {
        // context 점수 평균
        double avgContext = messages.stream()
                .filter(m -> m.getContextScore() != null)
                .mapToDouble(m -> m.getContextScore().doubleValue())
                .average()
                .orElse(0.5);

        // usage 점수 평균 (AI 활용 태도)
        double avgUsage = messages.stream()
                .filter(m -> m.getUsageScore() != null)
                .mapToDouble(m -> m.getUsageScore().doubleValue())
                .average()
                .orElse(0.5);

        // 협업 점수 = (context * 60) + (usage * 40)
        double score = (avgContext * 60) + (avgUsage * 40);

        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * AI 오해/오용 가능성 계산 (0-100, 낮을수록 안전)
     * - 감정적인 메시지 비율
     * - 부정확한 기대 (AI 과신)
     * - 명령형 표현 과다
     */
    private BigDecimal calculateMisuseRiskScore(List<Message> messages) {
        // 극단적인 감정 점수 (매우 낮거나 높음)
        long extremeSentimentCount = messages.stream()
                .filter(m -> m.getSentimentScore() != null)
                .filter(m -> {
                    double sentiment = m.getSentimentScore().doubleValue();
                    return sentiment < -0.5 || sentiment > 0.8;
                })
                .count();
        double extremeRatio = (double) extremeSentimentCount / messages.size();

        // 명령형 표현 비율
        long commandCount = messages.stream()
                .filter(m -> m.getContent().matches(".*\\b(해|해라|하세요|하십시오)\\b.*"))
                .count();
        double commandRatio = (double) commandCount / messages.size();

        // 오용 위험도 = (극단적 감정 * 50) + (명령형 * 50)
        double riskScore = (extremeRatio * 50) + (commandRatio * 50);

        return BigDecimal.valueOf(riskScore).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * AI Readiness Score 계산
     * 가중 평균:
     * - Communication: 30%
     * - Automation Risk: 25% (역산: 100 - risk)
     * - Collaboration: 30%
     * - Misuse Risk: 15% (역산: 100 - risk)
     */
    private BigDecimal calculateReadinessScore(BigDecimal communication, BigDecimal automationRisk,
                                                 BigDecimal collaboration, BigDecimal misuseRisk) {
        double score = (communication.doubleValue() * 0.3) +
                       ((100 - automationRisk.doubleValue()) * 0.25) +
                       (collaboration.doubleValue() * 0.3) +
                       ((100 - misuseRisk.doubleValue()) * 0.15);

        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Readiness Level 결정 (1-5)
     * 0-20: 1 (초보자)
     * 20-40: 2 (학습자)
     * 40-60: 3 (활용자)
     * 60-80: 4 (전문가)
     * 80-100: 5 (마스터)
     */
    private Integer determineReadinessLevel(BigDecimal readinessScore) {
        double score = readinessScore.doubleValue();
        if (score >= 80) return 5;
        if (score >= 60) return 4;
        if (score >= 40) return 3;
        if (score >= 20) return 2;
        return 1;
    }

    /**
     * AI 기반 인사이트 생성 (강점, 약점, 시뮬레이션, 피드백)
     * Gemini API를 사용하여 개인화된 분석 결과 생성
     */
    private void generateAIInsights(ResilienceReport report, List<Message> messages) {
        try {
            // 최근 20개 메시지 샘플링
            List<String> sampleMessages = messages.stream()
                    .limit(20)
                    .map(Message::getContent)
                    .toList();

            String prompt = buildAnalysisPrompt(report, sampleMessages);

            // Gemini API 호출 (간단한 분석 요청)
            // TODO: GeminiService에 일반화된 메서드가 없어서, 간단히 더미 데이터로 대체
            String aiResponse = generateDummyInsights(report);

            // JSON 파싱 및 저장
            parseAndSetInsights(report, aiResponse);

        } catch (Exception e) {
            log.error("AI 인사이트 생성 실패: userId={}", report.getUserId(), e);
            // 실패해도 리포트는 저장 (점수는 이미 계산됨)
            setDefaultInsights(report);
        }
    }

    /**
     * 분석 프롬프트 구성
     */
    private String buildAnalysisPrompt(ResilienceReport report, List<String> sampleMessages) {
        return String.format("""
                사용자의 AI 대화 패턴을 분석하여 다음을 JSON 형식으로 제공해주세요:

                [점수 정보]
                - AI 준비도: %.2f/100 (레벨 %d)
                - 커뮤니케이션: %.2f/100
                - 자동화 위험도: %.2f/100
                - 협업 적합도: %.2f/100
                - 오용 가능성: %.2f/100

                [샘플 메시지]
                %s

                다음 형식의 JSON으로 답변해주세요:
                {
                  "strengths": ["강점1", "강점2", "강점3"],
                  "weaknesses": ["약점1", "약점2"],
                  "simulation": "AI 시대에서의 포지션 예측 (2-3문장)",
                  "feedback": "개선을 위한 구체적인 피드백 (3-4문장)"
                }
                """,
                report.getReadinessScore(), report.getReadinessLevel(),
                report.getCommunicationScore(), report.getAutomationRiskScore(),
                report.getCollaborationScore(), report.getMisuseRiskScore(),
                String.join("\n- ", sampleMessages).substring(0, Math.min(500, String.join("\n- ", sampleMessages).length()))
        );
    }

    /**
     * 더미 인사이트 생성 (Gemini 연동 전 임시)
     */
    private String generateDummyInsights(ResilienceReport report) throws JsonProcessingException {
        Map<String, Object> insights = new HashMap<>();

        // 점수 기반 강점/약점 자동 생성
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();

        if (report.getCommunicationScore().doubleValue() >= 70) {
            strengths.add("명확하고 구체적인 질문으로 AI와 효과적으로 소통합니다");
        } else {
            weaknesses.add("질문의 명확성을 높이면 더 나은 답변을 받을 수 있습니다");
        }

        if (report.getCollaborationScore().doubleValue() >= 70) {
            strengths.add("대화의 맥락을 잘 유지하며 AI와 협업합니다");
        } else {
            weaknesses.add("이전 대화 맥락을 활용하면 더 깊이 있는 대화가 가능합니다");
        }

        if (report.getAutomationRiskScore().doubleValue() <= 30) {
            strengths.add("창의적이고 다양한 질문으로 AI를 활용합니다");
        } else {
            weaknesses.add("반복적인 패턴을 벗어나 더 다양한 질문을 시도해보세요");
        }

        insights.put("strengths", strengths);
        insights.put("weaknesses", weaknesses);
        insights.put("simulation",
                String.format("현재 AI 준비도 레벨 %d (%s) 단계입니다. " +
                        "이 수준은 AI 시대에서 기본적인 활용이 가능한 수준이며, " +
                        "지속적인 학습과 실습을 통해 더 높은 단계로 성장할 수 있습니다.",
                        report.getReadinessLevel(), report.getReadinessLevelName()));
        insights.put("feedback",
                "AI와의 대화에서 구체적인 맥락과 명확한 의도를 전달하면 더 나은 결과를 얻을 수 있습니다. " +
                "다양한 질문 유형을 시도하고, 이전 대화 내용을 참조하는 연습을 해보세요.");

        return objectMapper.writeValueAsString(insights);
    }

    /**
     * AI 응답 파싱 및 설정
     */
    private void parseAndSetInsights(ResilienceReport report, String aiResponse) {
        try {
            Map<String, Object> insights = objectMapper.readValue(aiResponse, Map.class);

            report.setStrengths(objectMapper.writeValueAsString(insights.get("strengths")));
            report.setWeaknesses(objectMapper.writeValueAsString(insights.get("weaknesses")));
            report.setSimulationResult((String) insights.get("simulation"));
            report.setDetailedFeedback((String) insights.get("feedback"));

        } catch (Exception e) {
            log.error("AI 응답 파싱 실패", e);
            setDefaultInsights(report);
        }
    }

    /**
     * 기본 인사이트 설정 (AI 생성 실패 시)
     */
    private void setDefaultInsights(ResilienceReport report) {
        try {
            report.setStrengths(objectMapper.writeValueAsString(
                    List.of("AI와의 대화에 참여하고 있습니다")));
            report.setWeaknesses(objectMapper.writeValueAsString(
                    List.of("더 많은 대화 데이터가 필요합니다")));
            report.setSimulationResult("분석 중입니다. 더 많은 대화 후 다시 확인해주세요.");
            report.setDetailedFeedback("AI와의 대화를 지속하며 다양한 질문을 시도해보세요.");
        } catch (JsonProcessingException e) {
            log.error("기본 인사이트 설정 실패", e);
        }
    }

    /**
     * 사용자의 최근 리포트 조회
     */
    @Transactional(readOnly = true)
    public Optional<ResilienceReport> getLatestReport(Long userId) {
        return resilienceReportRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 사용자의 모든 리포트 조회
     */
    @Transactional(readOnly = true)
    public List<ResilienceReport> getAllReports(Long userId) {
        return resilienceReportRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 리포트 잠금 해제
     */
    @Transactional
    public ResilienceReport unlockReport(Long reportId, Long userId) {
        ResilienceReport report = resilienceReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다"));

        if (!report.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 리포트만 잠금 해제할 수 있습니다");
        }

        if (report.getIsUnlocked()) {
            throw new IllegalStateException("이미 잠금 해제된 리포트입니다");
        }

        report.unlock();
        return resilienceReportRepository.save(report);
    }
}
