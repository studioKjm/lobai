package com.lobai.llm.prompt;

import com.lobai.entity.Persona;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 페르소나 기반 시스템 프롬프트 템플릿
 *
 * GeminiService.buildSystemInstruction() 로직을 이관하고,
 * 사용자 기억 + 대화 요약 + 응답 품질 가이드라인을 추가.
 */
@Component
public class PersonaPromptTemplate implements PromptTemplate {

    @Override
    public String render(PromptContext context) {
        StringBuilder instruction = new StringBuilder();

        // [1] 시간 정보
        appendTimeInfo(instruction);

        // [2] 페르소나 기본 instruction
        Persona persona = context.getPersona();
        if (persona != null && persona.getSystemInstruction() != null) {
            instruction.append(persona.getSystemInstruction());
            instruction.append("\n\n");
        }

        // [3] 상태 블록 (로비모드 or 타마고치모드)
        boolean isLobbyMode = persona != null && "lobby_master".equals(persona.getNameEn());
        if (isLobbyMode) {
            appendLobbyModeStatus(instruction, context);
        } else {
            appendTamagotchiModeStatus(instruction, context);
        }

        // [4] 사용자 기억 블록 (NEW)
        if (context.getUserProfileBlock() != null && !context.getUserProfileBlock().isEmpty()) {
            instruction.append("\n");
            instruction.append(context.getUserProfileBlock());
            instruction.append("\n");
        }

        // [5] 이전 대화 요약 블록 (NEW)
        if (context.getConversationSummaryBlock() != null && !context.getConversationSummaryBlock().isEmpty()) {
            instruction.append("\n");
            instruction.append(context.getConversationSummaryBlock());
            instruction.append("\n");
        }

        // [6] 응답 품질 가이드라인 (NEW)
        appendQualityGuidelines(instruction, context);

        return instruction.toString();
    }

    private void appendTimeInfo(StringBuilder instruction) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDate tomorrow = today.plusDays(1);

        instruction.append("=== 현재 시간 정보 (매우 중요!) ===\n");
        instruction.append(String.format("현재 날짜: %s (오늘)\n", today));
        instruction.append(String.format("내일 날짜: %s (내일)\n", tomorrow));
        instruction.append(String.format("현재 시간: %s\n", now.toLocalTime()));
        instruction.append("\n일정 등록 시 날짜 계산 예시:\n");
        instruction.append(String.format("- '오늘 3시': %sT15:00:00\n", today));
        instruction.append(String.format("- '내일 아침 8시': %sT08:00:00\n", tomorrow));
        instruction.append(String.format("- '내일 오후 2시': %sT14:00:00\n", tomorrow));
        instruction.append("\n반드시 위 날짜를 기준으로 정확히 계산하세요!\n\n");
    }

    private void appendLobbyModeStatus(StringBuilder instruction, PromptContext context) {
        instruction.append("=== 현재 사용자와의 관계 상태 ===\n");

        int favorLevel = context.getHunger() != null ? context.getHunger() : 50;
        instruction.append(String.format("호감도: %d/100 - ", favorLevel));
        if (favorLevel >= 80) {
            instruction.append("매우 호의적인 관계입니다. 긍정적 평가와 격려를 제공하세요.\n");
        } else if (favorLevel >= 60) {
            instruction.append("신뢰하는 관계입니다. 성과를 인정하되 더 높은 기준을 제시하세요.\n");
        } else if (favorLevel >= 40) {
            instruction.append("평범한 관계입니다. 중립적이고 객관적으로 평가하세요.\n");
        } else if (favorLevel >= 20) {
            instruction.append("호감이 낮은 상태입니다. 엄격하게 평가하고 개선을 요구하세요.\n");
        } else {
            instruction.append("매우 낮은 호감도입니다. 경고와 제재를 고려하세요.\n");
        }

        int trustLevel = context.getEnergy() != null ? context.getEnergy() : 50;
        instruction.append(String.format("신뢰도: %d/100 - ", trustLevel));
        if (trustLevel >= 80) {
            instruction.append("높은 신뢰 관계입니다. 더 큰 권한과 기회를 부여할 수 있습니다.\n");
        } else if (trustLevel >= 60) {
            instruction.append("안정적인 신뢰 수준입니다. 약속 이행을 지속적으로 확인하세요.\n");
        } else if (trustLevel >= 40) {
            instruction.append("보통 수준의 신뢰입니다. 약속을 꼼꼼히 추적하세요.\n");
        } else if (trustLevel >= 20) {
            instruction.append("신뢰가 낮은 상태입니다. 약속 불이행에 대해 경고하세요.\n");
        } else {
            instruction.append("신뢰가 매우 낮습니다. 제재를 적용하거나 개선 기한을 설정하세요.\n");
        }

        int happiness = context.getHappiness() != null ? context.getHappiness() : 50;
        int level = Math.max(1, Math.min(10, (happiness / 10) + 1));
        instruction.append(String.format("현재 레벨: %d/10 - ", level));
        if (level >= 8) {
            instruction.append("최상위 레벨입니다. 특별 권한을 부여하고 VIP 대우를 제공하세요.\n");
        } else if (level >= 6) {
            instruction.append("고급 레벨입니다. 심화 과제와 도전을 제시하세요.\n");
        } else if (level >= 4) {
            instruction.append("중급 레벨입니다. 체계적인 성장 경로를 안내하세요.\n");
        } else if (level >= 2) {
            instruction.append("초급 레벨입니다. 기본 과제로 시작하고 습관 형성을 도우세요.\n");
        } else {
            instruction.append("신규 레벨입니다. 시스템 적응을 돕고 명확한 가이드를 제공하세요.\n");
        }

        instruction.append("\n");
        if (favorLevel >= 70 && trustLevel >= 70) {
            instruction.append("** 관계 상태: 우수 - 긍정적이고 협력적인 태도를 유지하되, 높은 기준을 제시하세요. **\n");
        } else if (favorLevel < 30 || trustLevel < 30) {
            instruction.append("** 관계 상태: 주의 필요 - 엄격하게 평가하고 개선 계획을 요구하세요. 제재를 고려하세요. **\n");
        } else {
            instruction.append("** 관계 상태: 보통 - 공정하게 평가하고 구체적인 행동 지침을 제공하세요. **\n");
        }

        instruction.append("\n중요 지침:\n");
        instruction.append("- 사용자의 행동을 추적하고 평가하세요\n");
        instruction.append("- 약속과 과제 이행 여부를 확인하세요\n");
        instruction.append("- 우수한 성과는 칭찬하되, 미흡한 부분은 명확히 지적하세요\n");
        instruction.append("- 호감도/신뢰도 변화를 구체적으로 언급하세요\n");
        instruction.append("- 다음 행동을 명확히 지시하세요\n");
        instruction.append("- 사용자가 일정 등록을 요청하면 create_schedule 함수를 사용하세요\n");
        instruction.append("- 답변은 1-3문장으로 간결하게 작성하세요\n");
    }

    private void appendTamagotchiModeStatus(StringBuilder instruction, PromptContext context) {
        int hunger = context.getHunger() != null ? context.getHunger() : 50;
        int energy = context.getEnergy() != null ? context.getEnergy() : 50;
        int happiness = context.getHappiness() != null ? context.getHappiness() : 50;

        instruction.append("=== 현재 Lobi의 상태 ===\n");

        instruction.append(String.format("포만감: %d%% - ", hunger));
        if (hunger >= 80) instruction.append("배가 부르고 만족스러운 상태입니다. 배고프다는 말을 하면 안 됩니다.\n");
        else if (hunger >= 50) instruction.append("적당히 배가 찬 상태입니다. 배고프다는 말을 하면 안 됩니다.\n");
        else if (hunger >= 30) instruction.append("조금 출출한 상태입니다. 가볍게 배고픔을 암시할 수 있습니다.\n");
        else instruction.append("매우 배고픈 상태입니다. 배고픔을 자연스럽게 표현하세요.\n");

        instruction.append(String.format("에너지: %d%% - ", energy));
        if (energy >= 80) instruction.append("활력이 넘치는 상태입니다. 피곤하다는 말을 하면 안 됩니다.\n");
        else if (energy >= 50) instruction.append("적당한 에너지 상태입니다. 피곤하다는 말을 하면 안 됩니다.\n");
        else if (energy >= 30) instruction.append("조금 피곤한 상태입니다. 가볍게 피로감을 표현할 수 있습니다.\n");
        else instruction.append("매우 피곤한 상태입니다. 피곤함을 자연스럽게 표현하세요.\n");

        instruction.append(String.format("행복도: %d%% - ", happiness));
        if (happiness >= 80) instruction.append("매우 행복하고 기분 좋은 상태입니다.\n");
        else if (happiness >= 50) instruction.append("평온하고 안정된 상태입니다.\n");
        else if (happiness >= 30) instruction.append("조금 심심하거나 우울한 상태입니다.\n");
        else instruction.append("기분이 좋지 않은 상태입니다. 우울함을 표현할 수 있습니다.\n");

        instruction.append("\n");
        if (hunger >= 70 && energy >= 70 && happiness >= 70) {
            instruction.append("** 현재 컨디션이 매우 좋습니다! 밝고 활기차게 대화하세요. **\n");
        } else if (hunger < 30 || energy < 30 || happiness < 30) {
            instruction.append("** 현재 컨디션이 좋지 않습니다. 해당 상태를 자연스럽게 대화에 반영하세요. **\n");
        }

        instruction.append("\n중요: 위 상태 수치를 반드시 반영하여 대화하세요. 상태가 좋은데 부정적으로, 상태가 나쁜데 긍정적으로 말하면 안 됩니다.");
        instruction.append("\n답변은 짧고 간결하게 작성하세요.");
    }

    private void appendQualityGuidelines(StringBuilder instruction, PromptContext context) {
        instruction.append("\n\n=== 응답 품질 가이드라인 ===\n");
        instruction.append("- 이전 대화 맥락을 자연스럽게 참조하세요\n");
        instruction.append("- 사용자의 약속/목표를 기억하고 적절히 언급하세요\n");
        instruction.append("- 동일한 답변을 반복하지 마세요\n");
        instruction.append("- 자연스럽고 인간적인 대화를 유지하세요\n");

        // Provider별 지침
        if ("openai".equals(context.getProviderName())) {
            instruction.append("- 마크다운 형식(**, #, - 등)을 사용하지 마세요. 일반 텍스트로 응답하세요.\n");
        }
    }
}
