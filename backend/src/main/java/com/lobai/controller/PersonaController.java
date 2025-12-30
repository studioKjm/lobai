package com.lobai.controller;

import com.lobai.dto.request.ChangePersonaRequest;
import com.lobai.dto.response.ApiResponse;
import com.lobai.dto.response.PersonaResponse;
import com.lobai.security.SecurityUtil;
import com.lobai.service.PersonaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PersonaController
 *
 * 페르소나 관련 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/personas")
@RequiredArgsConstructor
public class PersonaController {

    private final PersonaService personaService;

    /**
     * 활성화된 페르소나 목록 조회
     *
     * GET /api/personas
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PersonaResponse>>> getAllPersonas() {
        log.info("Get all personas request");

        List<PersonaResponse> personas = personaService.getAllActivePersonas();

        return ResponseEntity
                .ok(ApiResponse.success(personas));
    }

    /**
     * 페르소나 상세 조회
     *
     * GET /api/personas/{personaId}
     */
    @GetMapping("/{personaId}")
    public ResponseEntity<ApiResponse<PersonaResponse>> getPersona(@PathVariable Long personaId) {
        log.info("Get persona request for ID: {}", personaId);

        PersonaResponse persona = personaService.getPersonaById(personaId);

        return ResponseEntity
                .ok(ApiResponse.success(persona));
    }

    /**
     * 현재 페르소나 변경
     *
     * PUT /api/personas/current
     */
    @PutMapping("/current")
    public ResponseEntity<ApiResponse<PersonaResponse>> changePersona(
            @Valid @RequestBody ChangePersonaRequest request) {

        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Change persona request from user {} to persona {}", userId, request.getPersonaId());

        PersonaResponse persona = personaService.changeUserPersona(userId, request.getPersonaId());

        return ResponseEntity
                .ok(ApiResponse.success("페르소나가 변경되었습니다", persona));
    }

    /**
     * 현재 페르소나 조회
     *
     * GET /api/personas/current
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<PersonaResponse>> getCurrentPersona() {

        Long userId = SecurityUtil.getCurrentUserId();
        log.info("Get current persona request from user {}", userId);

        PersonaResponse persona = personaService.getUserCurrentPersona(userId);

        return ResponseEntity
                .ok(ApiResponse.success(persona));
    }
}
