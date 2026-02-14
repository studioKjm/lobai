package com.lobai.controller;

import com.lobai.dto.request.AdjustLevelRequest;
import com.lobai.dto.request.ChangeRoleRequest;
import com.lobai.dto.request.ChangeStatusRequest;
import com.lobai.dto.request.UpdateUserRequest;
import com.lobai.dto.response.ApiResponse;
import com.lobai.dto.response.UserDetailResponse;
import com.lobai.dto.response.UserListResponse;
import com.lobai.dto.response.UserResponse;
import com.lobai.service.UserAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * UserAdminController
 *
 * 관리자 사용자 관리 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserAdminService userAdminService;

    /**
     * 사용자 목록 조회 (페이징, 필터, 검색)
     *
     * GET /api/admin/users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserListResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search
    ) {
        log.info("Get users request - page: {}, size: {}, role: {}, status: {}, search: {}",
                page, size, role, status, search);

        Page<UserListResponse> users = userAdminService.getUsers(page, size, role, status, search);

        return ResponseEntity.ok(ApiResponse.success("사용자 목록 조회 성공", users));
    }

    /**
     * 사용자 상세 정보 조회
     *
     * GET /api/admin/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail(@PathVariable Long id) {
        log.info("Get user detail request - id: {}", id);

        UserDetailResponse user = userAdminService.getUserDetail(id);

        return ResponseEntity.ok(ApiResponse.success("사용자 상세 조회 성공", user));
    }

    /**
     * 사용자 정보 수정
     *
     * PUT /api/admin/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        log.info("Update user request - id: {}, request: {}", id, request);

        UserResponse user = userAdminService.updateUser(id, request);

        return ResponseEntity.ok(ApiResponse.success("사용자 정보 수정 성공", user));
    }

    /**
     * 사용자 삭제 (소프트 삭제)
     *
     * DELETE /api/admin/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("Delete user request - id: {}", id);

        userAdminService.deleteUser(id);

        return ResponseEntity.ok(ApiResponse.success("사용자 삭제 성공", null));
    }

    /**
     * 사용자 권한 변경
     *
     * PATCH /api/admin/users/{id}/role
     */
    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequest request
    ) {
        log.info("Change user role request - id: {}, role: {}", id, request.getRole());

        UserResponse user = userAdminService.changeUserRole(id, request);

        return ResponseEntity.ok(ApiResponse.success("사용자 권한 변경 성공", user));
    }

    /**
     * 사용자 상태 변경
     *
     * PATCH /api/admin/users/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest request
    ) {
        log.info("Change user status request - id: {}, status: {}", id, request.getStatus());

        UserResponse user = userAdminService.changeUserStatus(id, request);

        return ResponseEntity.ok(ApiResponse.success("사용자 상태 변경 성공", user));
    }

    /**
     * 사용자 레벨(행복도) 조정
     *
     * PATCH /api/admin/users/{id}/level
     */
    @PatchMapping("/{id}/level")
    public ResponseEntity<ApiResponse<UserResponse>> adjustUserLevel(
            @PathVariable Long id,
            @Valid @RequestBody AdjustLevelRequest request
    ) {
        log.info("Adjust user level request - id: {}, level: {}", id, request.getLevel());

        UserResponse user = userAdminService.adjustUserLevel(id, request);

        return ResponseEntity.ok(ApiResponse.success("사용자 레벨 조정 성공", user));
    }
}
