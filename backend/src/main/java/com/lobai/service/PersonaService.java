package com.lobai.service;

import com.lobai.dto.response.PersonaResponse;
import com.lobai.entity.Persona;
import com.lobai.entity.User;
import com.lobai.repository.PersonaRepository;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PersonaService
 *
 * 페르소나 관련 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonaService {

    private final PersonaRepository personaRepository;
    private final UserRepository userRepository;

    /**
     * 활성화된 페르소나 목록 조회
     *
     * @return 페르소나 목록 (순서대로)
     */
    @Transactional(readOnly = true)
    public List<PersonaResponse> getAllActivePersonas() {
        List<Persona> personas = personaRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        return personas.stream()
                .map(PersonaResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 페르소나 ID로 조회
     *
     * @param personaId 페르소나 ID
     * @return 페르소나 응답
     */
    @Transactional(readOnly = true)
    public PersonaResponse getPersonaById(Long personaId) {
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new IllegalArgumentException("페르소나를 찾을 수 없습니다: " + personaId));

        return PersonaResponse.from(persona);
    }

    /**
     * 사용자의 현재 페르소나 변경
     *
     * @param userId 사용자 ID
     * @param personaId 변경할 페르소나 ID
     * @return 변경된 페르소나 응답
     */
    @Transactional
    public PersonaResponse changeUserPersona(Long userId, Long personaId) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 2. 페르소나 조회
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new IllegalArgumentException("페르소나를 찾을 수 없습니다: " + personaId));

        // 3. 활성화된 페르소나인지 확인
        if (!persona.getIsActive()) {
            throw new IllegalArgumentException("비활성화된 페르소나입니다: " + personaId);
        }

        // 4. 페르소나 변경
        user.changePersona(persona);
        userRepository.save(user);

        log.info("User {} changed persona to {}", userId, persona.getNameEn());

        return PersonaResponse.from(persona);
    }

    /**
     * 사용자의 현재 페르소나 조회
     *
     * @param userId 사용자 ID
     * @return 현재 페르소나 (없으면 null)
     */
    @Transactional(readOnly = true)
    public PersonaResponse getUserCurrentPersona(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        if (user.getCurrentPersona() == null) {
            return null;
        }

        return PersonaResponse.from(user.getCurrentPersona());
    }

    /**
     * 페르소나별 사용자 수 통계
     *
     * @param personaId 페르소나 ID
     * @return 해당 페르소나를 사용 중인 사용자 수
     */
    @Transactional(readOnly = true)
    public long countUsersByPersona(Long personaId) {
        return userRepository.countByCurrentPersonaId(personaId);
    }
}
