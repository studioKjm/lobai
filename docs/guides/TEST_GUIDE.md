# Testing Guide

**LobAI 프로젝트 테스트 가이드**
**Version**: 1.0.0
**Last Updated**: 2026-01-04

이 문서는 LobAI 프로젝트의 테스트 전략, TDD 가이드, E2E 테스트 방법을 다룹니다.

---

## Table of Contents

1. [테스트 철학](#1-테스트-철학)
2. [Unit Testing](#2-unit-testing)
3. [Integration Testing](#3-integration-testing)
4. [E2E Testing](#4-e2e-testing)
5. [테스트 데이터 관리](#5-테스트-데이터-관리)
6. [CI/CD에서의 테스트](#6-cicd에서의-테스트)
7. [TDD (Test-Driven Development)](#7-tdd-test-driven-development)
8. [테스트 베스트 프랙티스](#8-테스트-베스트-프랙티스)

---

## 1. 테스트 철학

### 1.1 Test Pyramid

```
         /\
        /  \  E2E (5-10%)
       /____\
      /      \  Integration (20-30%)
     /________\
    /          \  Unit (60-75%)
   /____________\
```

**비율 목표**:
- **Unit Tests**: 60-75% - 빠르고 많이
- **Integration Tests**: 20-30% - 모듈 간 상호작용
- **E2E Tests**: 5-10% - 핵심 사용자 시나리오

### 1.2 테스트 커버리지 목표

- **전체 커버리지**: 80% 이상
- **Service Layer**: 90% 이상 (비즈니스 로직)
- **Controller Layer**: 80% 이상
- **Utility 함수**: 95% 이상

**커버리지 확인**:

```bash
# Frontend
npm run test:coverage

# Backend
./gradlew test jacocoTestReport
open backend/build/reports/jacoco/test/html/index.html
```

### 1.3 테스트 작성 원칙

**FIRST**:
- **F**ast - 빠르게 실행
- **I**ndependent - 독립적 실행 (순서 무관)
- **R**epeatable - 반복 가능 (동일한 결과)
- **S**elf-validating - 자동 검증 (수동 확인 불필요)
- **T**imely - 적시에 작성 (코드 작성 전후)

**AAA Pattern**:
```
// Arrange - 준비
// Act - 실행
// Assert - 검증
```

---

## 2. Unit Testing

### 2.1 Frontend (Vitest + React Testing Library)

#### 2.1.1 설정

**vite.config.ts**:

```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'src/test/',
        '**/*.d.ts',
        '**/*.config.*',
        '**/mockData',
      ],
    },
  },
});
```

**src/test/setup.ts**:

```typescript
import { expect, afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';
import matchers from '@testing-library/jest-dom/matchers';

// Extend matchers
expect.extend(matchers);

// Cleanup after each test
afterEach(() => {
  cleanup();
});
```

#### 2.1.2 Component 테스트

**src/components/common/Button.test.tsx**:

```typescript
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import Button from './Button';

describe('Button Component', () => {
  it('renders with correct label', () => {
    // Arrange
    render(<Button label="Click me" onClick={() => {}} />);

    // Act
    const button = screen.getByRole('button', { name: /click me/i });

    // Assert
    expect(button).toBeInTheDocument();
  });

  it('calls onClick when clicked', () => {
    // Arrange
    const handleClick = vi.fn();
    render(<Button label="Click me" onClick={handleClick} />);

    // Act
    const button = screen.getByRole('button');
    fireEvent.click(button);

    // Assert
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('applies variant className correctly', () => {
    // Arrange
    render(<Button label="Primary" onClick={() => {}} variant="primary" />);

    // Act
    const button = screen.getByRole('button');

    // Assert
    expect(button).toHaveClass('btn-primary');
  });

  it('disables button when disabled prop is true', () => {
    // Arrange
    render(<Button label="Disabled" onClick={() => {}} disabled />);

    // Act
    const button = screen.getByRole('button');

    // Assert
    expect(button).toBeDisabled();
  });
});
```

#### 2.1.3 Hook 테스트

**src/hooks/useAuth.test.ts**:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { useAuth } from './useAuth';
import { authApi } from '@/services/authApi';

// Mock authApi
vi.mock('@/services/authApi', () => ({
  authApi: {
    getCurrentUser: vi.fn(),
  },
}));

describe('useAuth Hook', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('returns loading true initially', () => {
    // Arrange & Act
    const { result } = renderHook(() => useAuth());

    // Assert
    expect(result.current.loading).toBe(true);
  });

  it('fetches user data successfully', async () => {
    // Arrange
    const mockUser = { id: 1, email: 'test@test.com', username: 'Test User' };
    vi.mocked(authApi.getCurrentUser).mockResolvedValue({
      data: { success: true, data: mockUser },
    } as any);

    // Act
    const { result } = renderHook(() => useAuth());

    // Assert
    await waitFor(() => {
      expect(result.current.loading).toBe(false);
      expect(result.current.user).toEqual(mockUser);
    });
  });

  it('handles fetch error gracefully', async () => {
    // Arrange
    vi.mocked(authApi.getCurrentUser).mockRejectedValue(new Error('Network error'));

    // Act
    const { result } = renderHook(() => useAuth());

    // Assert
    await waitFor(() => {
      expect(result.current.loading).toBe(false);
      expect(result.current.user).toBeNull();
    });
  });
});
```

#### 2.1.4 Service 테스트

**src/services/authApi.test.ts**:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import { authApi } from './authApi';

vi.mock('@/lib/api', () => ({
  default: axios.create(),
}));

describe('authApi Service', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('login', () => {
    it('sends POST request to /auth/login', async () => {
      // Arrange
      const loginData = { email: 'test@test.com', password: 'Test1234!' };
      const mockResponse = {
        data: {
          success: true,
          data: {
            accessToken: 'token123',
            userId: 1,
          },
        },
      };

      vi.spyOn(axios, 'post').mockResolvedValue(mockResponse);

      // Act
      const result = await authApi.login(loginData);

      // Assert
      expect(axios.post).toHaveBeenCalledWith('/auth/login', loginData);
      expect(result.data.success).toBe(true);
      expect(result.data.data.accessToken).toBe('token123');
    });
  });
});
```

### 2.2 Backend (JUnit 5 + Mockito)

#### 2.2.1 Service Layer 테스트

**backend/src/test/java/com/lobai/service/AuthServiceTest.java**:

```java
package com.lobai.service;

import com.lobai.dto.request.RegisterRequest;
import com.lobai.dto.response.AuthResponse;
import com.lobai.entity.User;
import com.lobai.repository.UserRepository;
import com.lobai.repository.RefreshTokenRepository;
import com.lobai.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
            .email("test@test.com")
            .password("Test1234!")
            .username("Test User")
            .build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void register_Success() {
        // Arrange
        given(userRepository.existsByEmail(registerRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(registerRequest.getPassword())).willReturn("hashed_password");

        User savedUser = User.builder()
            .id(1L)
            .email(registerRequest.getEmail())
            .username(registerRequest.getUsername())
            .build();

        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(jwtTokenProvider.createAccessToken(anyLong(), anyString(), anyString())).willReturn("access_token");
        given(jwtTokenProvider.createRefreshToken(anyLong())).willReturn("refresh_token");
        given(jwtTokenProvider.getAccessTokenExpiry()).willReturn(900000L);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
        assertThat(response.getEmail()).isEqualTo(registerRequest.getEmail());

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void register_Fail_DuplicateEmail() {
        // Arrange
        given(userRepository.existsByEmail(registerRequest.getEmail())).willReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이미 사용 중인 이메일입니다");

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any());
    }
}
```

#### 2.2.2 Repository 테스트 (H2 in-memory)

**backend/src/test/java/com/lobai/repository/UserRepositoryTest.java**:

```java
package com.lobai.repository;

import com.lobai.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 사용자 조회 - 성공")
    void findByEmail_Success() {
        // Arrange
        User user = User.builder()
            .email("test@test.com")
            .passwordHash("hashed_password")
            .username("Test User")
            .build();

        entityManager.persist(user);
        entityManager.flush();

        // Act
        Optional<User> found = userRepository.findByEmail("test@test.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@test.com");
        assertThat(found.get().getUsername()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("이메일로 사용자 조회 - 실패")
    void findByEmail_NotFound() {
        // Act
        Optional<User> found = userRepository.findByEmail("nonexistent@test.com");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("이메일 존재 확인")
    void existsByEmail() {
        // Arrange
        User user = User.builder()
            .email("test@test.com")
            .passwordHash("hashed_password")
            .username("Test User")
            .build();

        entityManager.persist(user);
        entityManager.flush();

        // Act & Assert
        assertThat(userRepository.existsByEmail("test@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@test.com")).isFalse();
    }
}
```

#### 2.2.3 Controller 테스트 (MockMvc)

**backend/src/test/java/com/lobai/controller/AuthControllerTest.java**:

```java
package com.lobai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lobai.dto.request.LoginRequest;
import com.lobai.dto.response.AuthResponse;
import com.lobai.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/login - 성공")
    @WithMockUser
    void login_Success() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
            .email("test@test.com")
            .password("Test1234!")
            .build();

        AuthResponse authResponse = AuthResponse.builder()
            .accessToken("access_token")
            .refreshToken("refresh_token")
            .userId(1L)
            .email("test@test.com")
            .username("Test User")
            .build();

        given(authService.login(any(LoginRequest.class))).willReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value("access_token"))
            .andExpect(jsonPath("$.data.email").value("test@test.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 실패 (잘못된 입력)")
    @WithMockUser
    void login_Fail_InvalidInput() throws Exception {
        // Arrange
        LoginRequest invalidRequest = LoginRequest.builder()
            .email("invalid-email")  // Invalid format
            .password("123")         // Too short
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }
}
```

---

## 3. Integration Testing

### 3.1 API 통합 테스트

**backend/src/test/java/com/lobai/integration/AuthIntegrationTest.java**:

```java
package com.lobai.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lobai.dto.request.LoginRequest;
import com.lobai.dto.request.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("인증 API 통합 테스트")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 → 로그인 플로우")
    void registerAndLogin_Success() throws Exception {
        // 1. 회원가입
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("integration@test.com")
            .password("Test1234!")
            .username("Integration Test")
            .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").exists());

        // 2. 로그인
        LoginRequest loginRequest = LoginRequest.builder()
            .email("integration@test.com")
            .password("Test1234!")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").exists());
    }
}
```

### 3.2 Database 마이그레이션 테스트

**backend/src/test/resources/application-test.yml**:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

  h2:
    console:
      enabled: true
```

### 3.3 외부 API Mocking (Gemini AI)

**backend/src/test/java/com/lobai/service/GeminiServiceTest.java**:

```java
package com.lobai.service;

import com.lobai.config.GeminiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeminiService 테스트 (Mocked)")
class GeminiServiceTest {

    @Mock
    private GeminiConfig geminiConfig;

    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        geminiService = new GeminiService(geminiConfig);
    }

    @Test
    @DisplayName("AI 응답 생성 - 성공")
    void generateResponse_Success() {
        // Arrange
        String userMessage = "안녕하세요!";
        String expectedResponse = "안녕하세요! 무엇을 도와드릴까요?";

        // Mock Gemini API response
        // (실제로는 WireMock 또는 MockWebServer 사용 권장)

        // Act
        String response = geminiService.generateResponse(userMessage);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
    }
}
```

---

## 4. E2E Testing

### 4.1 Playwright 설정

**playwright.config.ts**:

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
  ],

  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
  },
});
```

### 4.2 핵심 시나리오 테스트

**e2e/auth.spec.ts**:

```typescript
import { test, expect } from '@playwright/test';

test.describe('인증 플로우', () => {
  test('회원가입 → 로그인 → 로그아웃', async ({ page }) => {
    // 1. 홈페이지 접속
    await page.goto('/');

    // 2. 회원가입 페이지로 이동
    await page.click('text=회원가입');
    await expect(page).toHaveURL(/.*register/);

    // 3. 회원가입 폼 작성
    const timestamp = Date.now();
    await page.fill('input[name="email"]', `test${timestamp}@test.com`);
    await page.fill('input[name="password"]', 'Test1234!');
    await page.fill('input[name="username"]', 'E2E Test User');
    await page.click('button:has-text("가입하기")');

    // 4. 채팅 페이지로 리다이렉트 확인
    await expect(page).toHaveURL(/.*chat/);
    await expect(page.locator('text=E2E Test User')).toBeVisible();

    // 5. 로그아웃
    await page.click('button:has-text("로그아웃")');
    await expect(page).toHaveURL('/');
  });

  test('잘못된 비밀번호로 로그인 실패', async ({ page }) => {
    await page.goto('/');
    await page.click('text=로그인');

    await page.fill('input[name="email"]', 'test@test.com');
    await page.fill('input[name="password"]', 'wrongpassword');
    await page.click('button:has-text("로그인")');

    // 에러 메시지 확인
    await expect(page.locator('text=이메일 또는 비밀번호가 올바르지 않습니다')).toBeVisible();
  });
});
```

**e2e/chat.spec.ts**:

```typescript
import { test, expect } from '@playwright/test';

test.describe('채팅 기능', () => {
  test.beforeEach(async ({ page }) => {
    // 로그인
    await page.goto('/');
    await page.click('text=로그인');
    await page.fill('input[name="email"]', 'test@test.com');
    await page.fill('input[name="password"]', 'Test1234!');
    await page.click('button:has-text("로그인")');
    await page.waitForURL(/.*chat/);
  });

  test('메시지 전송 및 AI 응답 확인', async ({ page }) => {
    // 1. 메시지 입력
    const messageInput = page.locator('textarea[placeholder*="메시지"]');
    await messageInput.fill('안녕하세요!');

    // 2. 전송 버튼 클릭
    await page.click('button:has-text("전송")');

    // 3. 사용자 메시지 표시 확인
    await expect(page.locator('text=안녕하세요!').first()).toBeVisible();

    // 4. AI 응답 대기 (최대 10초)
    await page.waitForSelector('.ai-message', { timeout: 10000 });

    // 5. AI 응답 표시 확인
    const aiMessage = page.locator('.ai-message').first();
    await expect(aiMessage).toBeVisible();
    await expect(aiMessage).not.toBeEmpty();
  });

  test('스탯 업데이트 (Feed)', async ({ page }) => {
    // 1. 현재 hunger 값 확인
    const hungerBefore = await page.locator('[data-stat="hunger"]').textContent();

    // 2. Feed 버튼 클릭
    await page.click('button:has-text("먹이 주기")');

    // 3. hunger 값이 증가했는지 확인 (최대 100)
    await page.waitForTimeout(500);
    const hungerAfter = await page.locator('[data-stat="hunger"]').textContent();

    const before = parseInt(hungerBefore || '0');
    const after = parseInt(hungerAfter || '0');

    expect(after).toBeGreaterThanOrEqual(before);
  });
});
```

**e2e/attendance.spec.ts**:

```typescript
import { test, expect } from '@playwright/test';

test.describe('출석 체크', () => {
  test.beforeEach(async ({ page }) => {
    // 로그인
    await page.goto('/');
    await page.click('text=로그인');
    await page.fill('input[name="email"]', 'phase2@test.com');
    await page.fill('input[name="password"]', 'Test1234!');
    await page.click('button:has-text("로그인")');
    await page.waitForURL(/.*chat/);
  });

  test('출석 체크 성공', async ({ page }) => {
    // 1. 출석 체크 버튼 클릭
    await page.click('button:has-text("출석 체크")');

    // 2. 성공 메시지 확인
    await expect(page.locator('text=출석 체크 완료')).toBeVisible({ timeout: 5000 });

    // 3. 출석 현황 업데이트 확인
    const streakCount = await page.locator('[data-testid="current-streak"]').textContent();
    expect(parseInt(streakCount || '0')).toBeGreaterThan(0);
  });

  test('중복 출석 방지', async ({ page }) => {
    // 1. 첫 번째 출석 체크
    await page.click('button:has-text("출석 체크")');
    await page.waitForSelector('text=출석 체크 완료', { timeout: 5000 });

    // 2. 두 번째 출석 시도
    await page.click('button:has-text("출석 체크")');

    // 3. 중복 출석 방지 메시지 확인
    await expect(page.locator('text=이미 오늘 출석하셨습니다')).toBeVisible();
  });
});
```

### 4.3 E2E 테스트 실행

```bash
# Playwright 설치 (최초 1회)
npx playwright install

# 모든 E2E 테스트 실행
npx playwright test

# 특정 파일만 실행
npx playwright test e2e/auth.spec.ts

# UI 모드 (디버깅)
npx playwright test --ui

# 헤드리스 모드 비활성화 (브라우저 확인)
npx playwright test --headed

# 리포트 확인
npx playwright show-report
```

---

## 5. 테스트 데이터 관리

### 5.1 Fixture 작성

**src/test/fixtures/users.ts**:

```typescript
export const mockUsers = {
  validUser: {
    id: 1,
    email: 'test@test.com',
    username: 'Test User',
    currentHunger: 80,
    currentEnergy: 90,
    currentHappiness: 70,
  },
  adminUser: {
    id: 2,
    email: 'admin@lobai.com',
    username: 'Admin',
    role: 'ADMIN',
  },
};

export const mockAuthResponse = {
  accessToken: 'mock_access_token',
  refreshToken: 'mock_refresh_token',
  expiresIn: 900000,
  tokenType: 'Bearer',
  userId: 1,
  email: 'test@test.com',
  username: 'Test User',
};
```

### 5.2 Seed Data (Backend)

**backend/src/test/resources/data-test.sql**:

```sql
-- Test users
INSERT INTO users (id, email, password_hash, username, role, created_at, updated_at)
VALUES
  (1, 'test@test.com', '$2a$12$hashed_password_here', 'Test User', 'USER', NOW(), NOW()),
  (2, 'admin@lobai.com', '$2a$12$hashed_password_here', 'Admin', 'ADMIN', NOW(), NOW());

-- Test messages
INSERT INTO messages (user_id, content, is_user, created_at)
VALUES
  (1, '안녕하세요!', true, NOW()),
  (1, '안녕하세요! 무엇을 도와드릴까요?', false, NOW());

-- Test attendance
INSERT INTO attendance_records (user_id, check_in_date, created_at)
VALUES
  (1, CURRENT_DATE, NOW());
```

### 5.3 Test Isolation

**각 테스트는 독립적으로 실행**:

```java
@BeforeEach
void setUp() {
    // 테스트 데이터 초기화
}

@AfterEach
void tearDown() {
    // 테스트 데이터 정리 (또는 @Transactional 사용)
}
```

```typescript
beforeEach(() => {
  vi.clearAllMocks();
  localStorage.clear();
});
```

---

## 6. CI/CD에서의 테스트

### 6.1 GitHub Actions 워크플로우

**.github/workflows/test.yml**:

```yaml
name: Tests

on:
  pull_request:
    branches: [develop, main]
  push:
    branches: [develop, main]

jobs:
  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'

      - name: Install dependencies
        run: npm ci

      - name: Run unit tests
        run: npm run test:coverage

      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./coverage/lcov.info

  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run tests
        run: ./gradlew test jacocoTestReport

      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./backend/build/reports/jacoco/test/jacocoTestReport.xml

  e2e-tests:
    runs-on: ubuntu-latest
    needs: [frontend-tests, backend-tests]
    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Install dependencies
        run: npm ci

      - name: Install Playwright
        run: npx playwright install --with-deps

      - name: Start backend
        run: |
          cd backend
          ./gradlew bootRun &
          sleep 30

      - name: Run E2E tests
        run: npx playwright test

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: playwright-report/
```

### 6.2 테스트 실패 시 대응

**Pull Request가 차단됨**:
1. 로컬에서 테스트 재실행: `npm run test`
2. 실패한 테스트 확인 및 수정
3. 다시 커밋 및 푸시
4. CI가 자동으로 재실행

---

## 7. TDD (Test-Driven Development)

### 7.1 TDD 사이클 (Red-Green-Refactor)

```
1. Red   - 실패하는 테스트 작성
   ↓
2. Green - 최소한의 코드로 테스트 통과
   ↓
3. Refactor - 코드 개선 (테스트는 계속 통과)
   ↓
(반복)
```

### 7.2 TDD 예시 (Affinity Score 계산)

#### Step 1: Red (실패하는 테스트)

```typescript
// src/services/affinityCalculator.test.ts
import { describe, it, expect } from 'vitest';
import { calculateAffinityScore } from './affinityCalculator';

describe('calculateAffinityScore', () => {
  it('calculates overall score with correct weights', () => {
    // Arrange
    const scores = {
      sentimentScore: 80,
      clarityScore: 70,
      contextScore: 60,
      usageScore: 90,
    };

    // Act
    const result = calculateAffinityScore(scores);

    // Assert
    // overallScore = (80 * 0.3) + (70 * 0.2) + (60 * 0.25) + (90 * 0.25)
    //              = 24 + 14 + 15 + 22.5 = 75.5
    expect(result.overallScore).toBeCloseTo(75.5);
    expect(result.level).toBe(4); // 61-80 range
  });
});
```

**실행**: ❌ FAIL (함수가 아직 없음)

#### Step 2: Green (최소 구현)

```typescript
// src/services/affinityCalculator.ts
export interface AffinityScores {
  sentimentScore: number;
  clarityScore: number;
  contextScore: number;
  usageScore: number;
}

export interface AffinityResult {
  overallScore: number;
  level: number;
}

export function calculateAffinityScore(scores: AffinityScores): AffinityResult {
  const { sentimentScore, clarityScore, contextScore, usageScore } = scores;

  const overallScore =
    sentimentScore * 0.3 +
    clarityScore * 0.2 +
    contextScore * 0.25 +
    usageScore * 0.25;

  let level = 1;
  if (overallScore > 80) level = 5;
  else if (overallScore > 60) level = 4;
  else if (overallScore > 40) level = 3;
  else if (overallScore > 20) level = 2;

  return { overallScore, level };
}
```

**실행**: ✅ PASS

#### Step 3: Refactor (코드 개선)

```typescript
// 상수 분리
const WEIGHTS = {
  sentiment: 0.3,
  clarity: 0.2,
  context: 0.25,
  usage: 0.25,
} as const;

const LEVEL_THRESHOLDS = [
  { min: 81, level: 5 },
  { min: 61, level: 4 },
  { min: 41, level: 3 },
  { min: 21, level: 2 },
  { min: 0, level: 1 },
];

function getLevelFromScore(score: number): number {
  return LEVEL_THRESHOLDS.find((t) => score >= t.min)?.level ?? 1;
}

export function calculateAffinityScore(scores: AffinityScores): AffinityResult {
  const overallScore =
    scores.sentimentScore * WEIGHTS.sentiment +
    scores.clarityScore * WEIGHTS.clarity +
    scores.contextScore * WEIGHTS.context +
    scores.usageScore * WEIGHTS.usage;

  const level = getLevelFromScore(overallScore);

  return { overallScore, level };
}
```

**실행**: ✅ PASS (리팩토링 후에도 테스트 통과)

---

## 8. 테스트 베스트 프랙티스

### 8.1 DO's

✅ **테스트 이름은 명확하게**:
```typescript
// ❌ Bad
it('works');

// ✅ Good
it('returns 400 when email is invalid');
```

✅ **AAA 패턴 사용**:
```typescript
it('calculates total price correctly', () => {
  // Arrange - 준비
  const items = [{ price: 100, quantity: 2 }, { price: 50, quantity: 3 }];

  // Act - 실행
  const total = calculateTotal(items);

  // Assert - 검증
  expect(total).toBe(350);
});
```

✅ **한 테스트에 한 가지만 검증**:
```typescript
// ❌ Bad
it('user creation', () => {
  expect(user.id).toBeDefined();
  expect(user.email).toBe('test@test.com');
  expect(user.role).toBe('USER');
  expect(userRepository.save).toHaveBeenCalled();
});

// ✅ Good (분리)
it('assigns default USER role to new user', () => {
  expect(user.role).toBe('USER');
});

it('saves user to repository', () => {
  expect(userRepository.save).toHaveBeenCalledWith(user);
});
```

### 8.2 DON'Ts

❌ **Production 코드에 테스트 코드 포함 금지**:
```typescript
// ❌ Bad
if (process.env.NODE_ENV !== 'test') {
  // production logic
}
```

❌ **console.log로 검증 금지**:
```typescript
// ❌ Bad
it('logs user data', () => {
  console.log(user); // 수동 확인 필요
});

// ✅ Good
it('returns user data', () => {
  expect(user).toEqual(expectedUser);
});
```

❌ **테스트 간 의존성 금지**:
```typescript
// ❌ Bad
let user;

it('creates user', () => {
  user = createUser();
});

it('updates user', () => {
  updateUser(user); // 이전 테스트에 의존
});

// ✅ Good
it('updates user', () => {
  const user = createUser();
  updateUser(user);
});
```

---

## Changelog

| Version | Date       | Changes                      | Author |
|---------|------------|------------------------------|--------|
| 1.0.0   | 2026-01-04 | Initial TEST_GUIDE created   | Team   |

---

**다음 문서**: [CICD_GUIDE.md](./CICD_GUIDE.md)
**관련 문서**: [DEV_GUIDE.md](./DEV_GUIDE.md), [PROJECT_CONSTITUTION.md](../../PROJECT_CONSTITUTION.md)
