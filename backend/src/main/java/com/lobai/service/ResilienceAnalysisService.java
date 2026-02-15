package com.lobai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lobai.entity.AffinityScore;
import com.lobai.entity.Message;
import com.lobai.entity.ResilienceReport;
import com.lobai.entity.User;
import com.lobai.llm.LlmRequest;
import com.lobai.llm.LlmResponse;
import com.lobai.llm.LlmRouter;
import com.lobai.llm.LlmTaskType;
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
 * AI 적응력 분석 비즈니스 로직 (Phase 2 - Gemini 연동 + 친밀도 통합)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResilienceAnalysisService {

    private final MessageRepository messageRepository;
    private final ResilienceReportRepository resilienceReportRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final LlmRouter llmRouter;
    private final AffinityScoreService affinityScoreService;
    private final ObjectMapper objectMapper;

    private static final int MINIMUM_MESSAGES_FOR_ANALYSIS = 10;
    private static final int ANALYSIS_PERIOD_DAYS = 30;

    /**
     * AI 적응력 리포트 생성
     */
    @Transactional
    public ResilienceReport generateReport(Long userId, ResilienceReport.ReportType reportType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(ANALYSIS_PERIOD_DAYS);

        Pageable limit = PageRequest.of(0, 100);
        List<Message> userMessages = messageRepository.findRecentUserMessages(userId, limit);

        if (userMessages.size() < MINIMUM_MESSAGES_FOR_ANALYSIS) {
            throw new IllegalStateException(
                    String.format("분석할 메시지가 부족합니다 (최소 %d개 필요, 현재 %d개)",
                            MINIMUM_MESSAGES_FOR_ANALYSIS, userMessages.size()));
        }

        // 4가지 핵심 점수 계산
        BigDecimal communicationScore = calculateCommunicationScore(userMessages);
        BigDecimal automationRiskScore = calculateAutomationRiskScore(userMessages);
        BigDecimal collaborationScore = calculateCollaborationScore(userMessages);
        BigDecimal misuseRiskScore = calculateMisuseRiskScore(userMessages);

        BigDecimal readinessScore = calculateReadinessScore(
                communicationScore, automationRiskScore, collaborationScore, misuseRiskScore);
        Integer readinessLevel = determineReadinessLevel(readinessScore);

        // 친밀도 정보 연동
        AffinityScore affinity = affinityScoreService.getUserAffinityScore(userId);

        ResilienceReport report = ResilienceReport.builder()
                .userId(userId)
                .reportType(reportType)
                .reportVersion("v2.0")
                .readinessScore(readinessScore)
                .readinessLevel(readinessLevel)
                .communicationScore(communicationScore)
                .automationRiskScore(automationRiskScore)
                .collaborationScore(collaborationScore)
                .misuseRiskScore(misuseRiskScore)
                // 친밀도 연동
                .affinityScoreAtReport(affinity.getOverallScore())
                .affinityLevelAtReport(affinity.getLevel())
                .avgEngagementDepth(affinity.getAvgEngagementDepth())
                .avgSelfDisclosure(affinity.getAvgSelfDisclosure())
                .avgReciprocity(affinity.getAvgReciprocity())
                // 분석 메타데이터
                .analyzedMessageCount(userMessages.size())
                .analyzedPeriodDays((int) ChronoUnit.DAYS.between(startDate, endDate))
                .analysisStartDate(startDate)
                .analysisEndDate(endDate)
                .isUnlocked(reportType == ResilienceReport.ReportType.basic)
                .build();

        // AI 인사이트 생성 (Gemini 실제 호출)
        generateAIInsights(report, userMessages, affinity);

        ResilienceReport saved = resilienceReportRepository.save(report);

        log.info("AI 적응력 리포트 생성 완료: userId={}, type={}, readiness={}/100, level={}, affinity={}",
                userId, reportType, readinessScore, readinessLevel, affinity.getOverallScore());

        return saved;
    }

    /**
     * AI 친화 커뮤니케이션 지수 계산
     */
    private BigDecimal calculateCommunicationScore(List<Message> messages) {
        double avgClarity = messages.stream()
                .filter(m -> m.getClarityScore() != null)
                .mapToDouble(m -> m.getClarityScore().doubleValue())
                .average()
                .orElse(0.5);

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

        long questionCount = messages.stream()
                .filter(m -> m.getContent().contains("?") || m.getContent().contains("어떻게") ||
                        m.getContent().contains("무엇") || m.getContent().contains("왜"))
                .count();
        double questionRatio = (double) questionCount / messages.size();

        double score = (avgClarity * 100 * 0.5) + (lengthScore * 100 * 0.3) + (questionRatio * 100 * 0.2);
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 자동화 위험도 계산
     */
    private BigDecimal calculateAutomationRiskScore(List<Message> messages) {
        long simpleCommandCount = messages.stream()
                .filter(m -> {
                    String content = m.getContent();
                    return content.length() < 10 ||
                            content.matches(".*\\b(해줘|알려줘|보여줘|찾아줘)\\b.*");
                })
                .count();
        double simpleRatio = (double) simpleCommandCount / messages.size();

        Set<String> uniqueMessages = new HashSet<>();
        messages.forEach(m -> uniqueMessages.add(m.getContent().toLowerCase()));
        double diversity = (double) uniqueMessages.size() / messages.size();

        double riskScore = (simpleRatio * 70) + ((1 - diversity) * 30);
        return BigDecimal.valueOf(riskScore).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * AI 협업 적합도 계산 (Phase 2: self_disclosure + initiative 반영)
     */
    private BigDecimal calculateCollaborationScore(List<Message> messages) {
        double avgContext = messages.stream()
                .filter(m -> m.getContextScore() != null)
                .mapToDouble(m -> m.getContextScore().doubleValue())
                .average()
                .orElse(0.5);

        double avgUsage = messages.stream()
                .filter(m -> m.getUsageScore() != null)
                .mapToDouble(m -> m.getUsageScore().doubleValue())
                .average()
                .orElse(0.5);

        // Phase 2: 자기 개방도 + 주도성 반영
        double avgDisclosure = messages.stream()
                .filter(m -> m.getSelfDisclosureDepth() != null)
                .mapToDouble(m -> m.getSelfDisclosureDepth().doubleValue())
                .average()
                .orElse(0.0);

        long initiativeCount = messages.stream()
                .filter(m -> m.getIsInitiative() != null && m.getIsInitiative())
                .count();
        double initiativeRate = (double) initiativeCount / messages.size();

        // 협업 점수 = context*0.35 + usage*0.25 + disclosure*0.20 + initiative*0.20
        double score = (avgContext * 35) + (avgUsage * 25) + (avgDisclosure * 20) + (initiativeRate * 20);
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * AI 오해/오용 가능성 계산
     */
    private BigDecimal calculateMisuseRiskScore(List<Message> messages) {
        long extremeSentimentCount = messages.stream()
                .filter(m -> m.getSentimentScore() != null)
                .filter(m -> {
                    double sentiment = m.getSentimentScore().doubleValue();
                    return sentiment < -0.5 || sentiment > 0.8;
                })
                .count();
        double extremeRatio = (double) extremeSentimentCount / messages.size();

        long commandCount = messages.stream()
                .filter(m -> m.getContent().matches(".*\\b(해|해라|하세요|하십시오)\\b.*"))
                .count();
        double commandRatio = (double) commandCount / messages.size();

        double riskScore = (extremeRatio * 50) + (commandRatio * 50);
        return BigDecimal.valueOf(riskScore).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * AI Readiness Score 계산
     */
    private BigDecimal calculateReadinessScore(BigDecimal communication, BigDecimal automationRisk,
                                                 BigDecimal collaboration, BigDecimal misuseRisk) {
        double score = (communication.doubleValue() * 0.3) +
                       ((100 - automationRisk.doubleValue()) * 0.25) +
                       (collaboration.doubleValue() * 0.3) +
                       ((100 - misuseRisk.doubleValue()) * 0.15);
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    private Integer determineReadinessLevel(BigDecimal readinessScore) {
        double score = readinessScore.doubleValue();
        if (score >= 80) return 5;
        if (score >= 60) return 4;
        if (score >= 40) return 3;
        if (score >= 20) return 2;
        return 1;
    }

    /**
     * AI 기반 인사이트 생성 (LlmRouter 사용 + 친밀도 연동)
     */
    private void generateAIInsights(ResilienceReport report, List<Message> messages, AffinityScore affinity) {
        try {
            List<String> sampleMessages = messages.stream()
                    .limit(20)
                    .map(Message::getContent)
                    .toList();

            String prompt = buildAnalysisPrompt(report, sampleMessages, affinity);

            // LlmRouter를 통해 Gemini 호출
            LlmRequest request = LlmRequest.builder()
                    .userMessage(prompt)
                    .temperature(0.5)
                    .maxOutputTokens(1024)
                    .jsonMode(true)
                    .taskType(LlmTaskType.RESILIENCE_ANALYSIS)
                    .build();

            LlmResponse response = llmRouter.executeWithFallback(LlmTaskType.RESILIENCE_ANALYSIS, request);

            parseAndSetInsights(report, response.getContent());

            log.info("AI 인사이트 생성 성공: userId={}", report.getUserId());

        } catch (Exception e) {
            log.error("AI 인사이트 생성 실패, 더미 데이터 사용: userId={}, error={}", report.getUserId(), e.getMessage());
            try {
                String dummyResponse = generateDummyInsights(report);
                parseAndSetInsights(report, dummyResponse);
            } catch (Exception ex) {
                setDefaultInsights(report);
            }
        }
    }

    /**
     * 분석 프롬프트 (친밀도 정보 포함)
     */
    private String buildAnalysisPrompt(ResilienceReport report, List<String> sampleMessages, AffinityScore affinity) {
        String messagesStr = String.join("\n- ", sampleMessages);
        if (messagesStr.length() > 500) {
            messagesStr = messagesStr.substring(0, 500);
        }

        return String.format("""
                사용자의 AI 대화 패턴을 분석하여 다음을 JSON 형식으로만 제공해주세요. 다른 텍스트 없이 JSON만 반환하세요.

                [점수 정보]
                - AI 준비도: %.2f/100 (레벨 %d)
                - 커뮤니케이션: %.2f/100
                - 자동화 위험도: %.2f/100
                - 협업 적합도: %.2f/100
                - 오용 가능성: %.2f/100

                [친밀도 정보]
                - 친밀도 점수: %.2f/100 (레벨 %d)
                - 참여 깊이: %.2f/1.0
                - 자기 개방도: %.2f/1.0
                - 상호작용 품질: %.2f/1.0
                - 관계 단계: %s

                [샘플 메시지]
                - %s

                다음 형식의 JSON으로 답변해주세요:
                {
                  "strengths": ["강점1", "강점2", "강점3"],
                  "weaknesses": ["약점1", "약점2"],
                  "simulation": "AI 시대에서의 포지션 예측 (2-3문장)",
                  "feedback": "개선을 위한 구체적인 피드백 (3-4문장)",
                  "recommendations": ["구체적 개선 행동 1", "구체적 개선 행동 2", "구체적 개선 행동 3"]
                }
                """,
                report.getReadinessScore(), report.getReadinessLevel(),
                report.getCommunicationScore(), report.getAutomationRiskScore(),
                report.getCollaborationScore(), report.getMisuseRiskScore(),
                affinity.getOverallScore(), affinity.getLevel(),
                affinity.getAvgEngagementDepth(), affinity.getAvgSelfDisclosure(),
                affinity.getAvgReciprocity(), affinity.getRelationshipStage(),
                messagesStr
        );
    }

    /**
     * 더미 인사이트 생성 (Gemini 연동 전 fallback)
     */
    private String generateDummyInsights(ResilienceReport report) throws JsonProcessingException {
        Map<String, Object> insights = new HashMap<>();

        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (report.getCommunicationScore().doubleValue() >= 70) {
            strengths.add("명확하고 구체적인 질문으로 AI와 효과적으로 소통합니다");
        } else {
            weaknesses.add("질문의 명확성을 높이면 더 나은 답변을 받을 수 있습니다");
            recommendations.add("질문할 때 구체적인 맥락과 원하는 결과를 함께 설명해보세요");
        }

        if (report.getCollaborationScore().doubleValue() >= 70) {
            strengths.add("대화의 맥락을 잘 유지하며 AI와 협업합니다");
        } else {
            weaknesses.add("이전 대화 맥락을 활용하면 더 깊이 있는 대화가 가능합니다");
            recommendations.add("이전 대화 내용을 참조하며 연속적인 대화를 시도해보세요");
        }

        if (report.getAutomationRiskScore().doubleValue() <= 30) {
            strengths.add("창의적이고 다양한 질문으로 AI를 활용합니다");
        } else {
            weaknesses.add("반복적인 패턴을 벗어나 더 다양한 질문을 시도해보세요");
            recommendations.add("다양한 주제와 질문 방식으로 AI 활용 범위를 넓혀보세요");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("현재 수준을 유지하며 더 깊이 있는 대화를 시도해보세요");
        }

        insights.put("strengths", strengths);
        insights.put("weaknesses", weaknesses);
        insights.put("recommendations", recommendations);
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

            // Phase 2: recommendations
            if (insights.containsKey("recommendations")) {
                report.setRecommendations(objectMapper.writeValueAsString(insights.get("recommendations")));
            }
        } catch (Exception e) {
            log.error("AI 응답 파싱 실패", e);
            setDefaultInsights(report);
        }
    }

    private void setDefaultInsights(ResilienceReport report) {
        try {
            report.setStrengths(objectMapper.writeValueAsString(
                    List.of("AI와의 대화에 참여하고 있습니다")));
            report.setWeaknesses(objectMapper.writeValueAsString(
                    List.of("더 많은 대화 데이터가 필요합니다")));
            report.setSimulationResult("분석 중입니다. 더 많은 대화 후 다시 확인해주세요.");
            report.setDetailedFeedback("AI와의 대화를 지속하며 다양한 질문을 시도해보세요.");
            report.setRecommendations(objectMapper.writeValueAsString(
                    List.of("AI와 매일 대화하는 습관을 만들어보세요")));
        } catch (JsonProcessingException e) {
            log.error("기본 인사이트 설정 실패", e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<ResilienceReport> getLatestReport(Long userId) {
        return resilienceReportRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<ResilienceReport> getAllReports(Long userId) {
        return resilienceReportRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

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
