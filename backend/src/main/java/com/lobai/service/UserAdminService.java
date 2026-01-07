package com.lobai.service;

import com.lobai.dto.request.ChangeRoleRequest;
import com.lobai.dto.request.ChangeStatusRequest;
import com.lobai.dto.request.UpdateUserRequest;
import com.lobai.dto.response.UserDetailResponse;
import com.lobai.dto.response.UserListResponse;
import com.lobai.dto.response.UserResponse;
import com.lobai.entity.AffinityScore;
import com.lobai.entity.Role;
import com.lobai.entity.User;
import com.lobai.repository.AffinityScoreRepository;
import com.lobai.repository.MessageRepository;
import com.lobai.repository.UserRepository;
import com.lobai.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserAdminService
 *
 * 관리자 사용자 관리 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final AffinityScoreRepository affinityScoreRepository;

    /**
     * 사용자 목록 조회 (페이징, 필터링, 검색)
     *
     * @param page   페이지 번호
     * @param size   페이지 크기
     * @param role   역할 필터 (선택)
     * @param status 상태 필터 (선택)
     * @param search 검색어 (이메일 또는 사용자명)
     * @return 사용자 목록 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<UserListResponse> getUsers(int page, int size, String role, String status, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<User> users;

        // 검색어가 있으면 검색, 없으면 전체 조회
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.findAll(pageable);
            // Filter by search in memory (can be optimized with custom query)
            users = users.map(user -> {
                if (user.getEmail().contains(search) || user.getUsername().contains(search)) {
                    return user;
                }
                return null;
            });
        } else {
            users = userRepository.findAll(pageable);
        }

        // Convert to response DTO
        return users.map(this::toUserListResponse);
    }

    /**
     * 사용자 상세 정보 조회
     *
     * @param id 사용자 ID
     * @return 사용자 상세 정보
     */
    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));

        // Get stats
        Integer totalMessages = (int) messageRepository.countByUserId(id);
        Integer totalAttendanceDays = user.getTotalAttendanceDays();
        Double affinityScore = affinityScoreRepository.findByUserId(id)
                .map(AffinityScore::getOverallScore)
                .map(score -> score.doubleValue())
                .orElse(null);

        return UserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .totalMessages(totalMessages)
                .totalAttendanceDays(totalAttendanceDays)
                .affinityScore(affinityScore)
                .build();
    }

    /**
     * 사용자 정보 수정
     *
     * @param id      사용자 ID
     * @param request 수정 요청
     * @return 수정된 사용자 정보
     */
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));

        // 이메일 중복 체크 (다른 사용자가 사용 중인지)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
            }
        }

        // 업데이트
        user.updateProfile(request.getEmail(), request.getUsername());
        User savedUser = userRepository.save(user);

        log.info("User updated by admin: {}", savedUser.getEmail());

        return toUserResponse(savedUser);
    }

    /**
     * 사용자 삭제 (소프트 삭제)
     *
     * @param id 사용자 ID
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));

        // 본인은 삭제할 수 없음
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId.equals(id)) {
            throw new IllegalStateException("자기 자신은 삭제할 수 없습니다");
        }

        // 마지막 ADMIN은 삭제할 수 없음
        if (user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.countByRoleExcludingUser(Role.ADMIN, id);
            if (adminCount == 0) {
                throw new IllegalStateException("마지막 관리자는 삭제할 수 없습니다");
            }
        }

        // 소프트 삭제
        user.softDelete();
        userRepository.save(user);

        log.info("User soft deleted: {}", user.getEmail());
    }

    /**
     * 사용자 권한 변경
     *
     * @param id      사용자 ID
     * @param request 권한 변경 요청
     * @return 변경된 사용자 정보
     */
    @Transactional
    public UserResponse changeUserRole(Long id, ChangeRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));

        // 본인의 권한은 변경할 수 없음
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId.equals(id)) {
            throw new IllegalStateException("자기 자신의 권한은 변경할 수 없습니다");
        }

        // 마지막 ADMIN을 USER로 변경할 수 없음
        if (user.getRole() == Role.ADMIN && request.getRole() == Role.USER) {
            long adminCount = userRepository.countByRoleExcludingUser(Role.ADMIN, id);
            if (adminCount == 0) {
                throw new IllegalStateException("마지막 관리자의 권한을 변경할 수 없습니다");
            }
        }

        // 권한 변경
        user.changeRole(request.getRole());
        User savedUser = userRepository.save(user);

        log.info("User role changed: {} -> {}", user.getEmail(), request.getRole());

        return toUserResponse(savedUser);
    }

    /**
     * 사용자 상태 변경
     *
     * @param id      사용자 ID
     * @param request 상태 변경 요청
     * @return 변경된 사용자 정보
     */
    @Transactional
    public UserResponse changeUserStatus(Long id, ChangeStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));

        // 본인의 상태는 변경할 수 없음
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId.equals(id)) {
            throw new IllegalStateException("자기 자신의 상태는 변경할 수 없습니다");
        }

        // 상태 변경
        user.changeStatus(request.getStatus());
        User savedUser = userRepository.save(user);

        log.info("User status changed: {} -> {}", user.getEmail(), request.getStatus());

        return toUserResponse(savedUser);
    }

    // ==================== Private Helper Methods ====================

    private UserListResponse toUserListResponse(User user) {
        return UserListResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .currentHunger(user.getCurrentHunger())
                .currentEnergy(user.getCurrentEnergy())
                .currentHappiness(user.getCurrentHappiness())
                .currentPersonaId(user.getCurrentPersona() != null ? user.getCurrentPersona().getId() : null)
                .build();
    }
}
