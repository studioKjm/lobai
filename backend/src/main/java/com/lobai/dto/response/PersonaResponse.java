package com.lobai.dto.response;

import com.lobai.entity.Persona;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PersonaResponse
 *
 * 페르소나 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaResponse {

    private Long id;
    private String name;          // 한글명
    private String nameEn;        // 영문명
    private String displayName;   // 표시명
    private String iconEmoji;     // 아이콘
    private Integer displayOrder; // 순서
    private Boolean isActive;     // 활성 여부

    /**
     * Entity to DTO 변환
     */
    public static PersonaResponse from(Persona persona) {
        return PersonaResponse.builder()
                .id(persona.getId())
                .name(persona.getName())
                .nameEn(persona.getNameEn())
                .displayName(persona.getDisplayName())
                .iconEmoji(persona.getIconEmoji())
                .displayOrder(persona.getDisplayOrder())
                .isActive(persona.getIsActive())
                .build();
    }
}
