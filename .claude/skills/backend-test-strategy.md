---
name: backend-test-strategy
description: Spring Boot 백엔드 테스트 전략 (Unit/Integration/E2E 비율). Repository, Service, Controller 계층별 테스트.
triggers: ["backend test", "spring test", "integration test", "api test", "repository test", "service test"]
---

# Backend Testing Strategy (Spring Boot)

## Purpose

**Spring Boot 백엔드 테스트 전략**을 수립합니다. Repository/Service/Controller 계층별로 **효율적인 테스트 비율**을 강제하여 빠르고 안정적인 백엔드를 구축합니다.

---

## Test Pyramid (Backend)

```
        ▲
       /E2E\          10% (느림, 최소한)
      /─────\         - API 전체 플로우 (회원가입→로그인→메시지)
     /Integ.\ 30% (중간 속도)
    /────────\       - Controller + Service + DB
   /  Unit    \      60% (빠름, 많이)
  /────────────\     - Service 비즈니스 로직, Utils
```

---

## Test Types (Backend)

### 1. Unit Tests (60% 권장)

**Target**: Service 레이어 비즈니스 로직, Utils

**Characteristics**:
- **빠름**: < 100ms per test
- **독립적**: Repository는 Mock, 외부 의존성 없음
- **많이**: 수백 개
- **격리됨**: 데이터베이스, 네트워크, 파일 시스템 접근 안 함

**What to Test**:
- ✅ Service 비즈니스 로직
- ✅ 유틸리티 함수 (PasswordUtil, DateTimeUtil 등)
- ✅ DTO 변환 로직 (Entity ↔ DTO)
- ✅ 검증 로직 (Validator)
- ✅ 예외 처리 (Custom Exception)

**What NOT to Test**:
- ❌ Repository (Spring Data JPA가 이미 테스트함)
- ❌ Getter/Setter (Lombok 자동 생성)
- ❌ JPA 관계 설정 (Integration Test에서)

**Example**:
```java
// ✅ 좋은 Unit Test 예시
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_Success() {
        // Given
        CreateUserRequest request = new CreateUserRequest("test@example.com", "password123", "홍길동");
        User user = User.builder()
            .email("test@example.com")
            .passwordHash("hashed_password")
            .username("홍길동")
            .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserResponse response = userService.createUser(request);

        // Then
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getUsername()).isEqualTo("홍길동");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists_ThrowsException() {
        // Given
        CreateUserRequest request = new CreateUserRequest("existing@example.com", "password", "user");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.createUser(request);
        });
        verify(userRepository, never()).save(any(User.class));
    }
}
```

---

### 2. Integration Tests (30% 권장)

**Target**: Controller + Service + DB (실제 데이터베이스 사용)

**Characteristics**:
- **중간 속도**: < 1s per test
- **실제 DB**: H2 In-Memory 또는 Testcontainers MySQL
- **적당히**: 수십 개
- **계층 통합**: 여러 레이어가 함께 동작하는지 검증

**What to Test**:
- ✅ Controller → Service → Repository → DB 전체 플로우
- ✅ JPA 관계 설정 (@OneToMany, @ManyToOne)
- ✅ 트랜잭션 (@Transactional) 동작
- ✅ 실제 SQL 쿼리 실행 결과
- ✅ API 엔드포인트 (MockMvc)

**Example**:
```java
// ✅ 좋은 Integration Test 예시
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MessageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void sendMessage_Success() throws Exception {
        // Given
        User user = userRepository.save(User.builder()
            .email("user@example.com")
            .passwordHash("hash")
            .username("user")
            .build());

        String accessToken = "valid_jwt_token"; // JWT 생성

        SendMessageRequest request = new SendMessageRequest("안녕하세요", 1L);

        // When & Then
        mockMvc.perform(post("/api/messages")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userMessage.content").value("안녕하세요"))
            .andExpect(jsonPath("$.data.botMessage").exists());

        // DB 확인
        List<Message> messages = messageRepository.findByUserId(user.getId());
        assertThat(messages).hasSize(2); // user + bot
    }
}
```

---

### 3. End-to-End Tests (10% 권장)

**Target**: API 전체 플로우 (실제 HTTP 요청)

**Characteristics**:
- **느림**: 초~분 단위
- **실제 환경**: 실제 서버, 실제 DB
- **최소한**: 수개~수십 개
- **크리티컬 패스**: 핵심 비즈니스 시나리오만

**What to Test**:
- ✅ 회원가입 → 로그인 → 메시지 전송 플로우
- ✅ JWT 토큰 발급 → 인증 → API 호출
- ✅ 페르소나 변경 → 메시지 전송 → 응답 확인
- ✅ Stats 업데이트 → DB 반영 확인

**What NOT to Test**:
- ❌ 모든 엣지 케이스 (Unit에서 처리)
- ❌ 에러 메시지 문구
- ❌ 단순 CRUD

**Example**:
```java
// ✅ 좋은 E2E Test 예시
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserFlowE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void completeUserFlow() {
        String baseUrl = "http://localhost:" + port;

        // 1. 회원가입
        RegisterRequest registerRequest = new RegisterRequest("new@example.com", "password123", "신규유저");
        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
            baseUrl + "/api/auth/register",
            registerRequest,
            AuthResponse.class
        );
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String accessToken = registerResponse.getBody().getAccessToken();

        // 2. 로그인 (선택적, 이미 토큰 받음)
        // ...

        // 3. 메시지 전송
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        SendMessageRequest messageRequest = new SendMessageRequest("안녕하세요", 1L);
        HttpEntity<SendMessageRequest> entity = new HttpEntity<>(messageRequest, headers);

        ResponseEntity<MessageResponse> messageResponse = restTemplate.exchange(
            baseUrl + "/api/messages",
            HttpMethod.POST,
            entity,
            MessageResponse.class
        );
        assertThat(messageResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(messageResponse.getBody().getUserMessage().getContent()).isEqualTo("안녕하세요");
        assertThat(messageResponse.getBody().getBotMessage()).isNotNull();

        // 4. Stats 확인
        ResponseEntity<StatsResponse> statsResponse = restTemplate.exchange(
            baseUrl + "/api/stats",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            StatsResponse.class
        );
        assertThat(statsResponse.getBody().getHappiness()).isGreaterThan(70); // 초기값 + 메시지 보너스
    }
}
```

---

## Coverage Goals (Backend)

| Layer | Coverage Target | Priority |
|-------|----------------|----------|
| **Service (비즈니스 로직)** | ≥80% | 최상 |
| **Controller** | ≥70% | 상 |
| **Repository** | Integration으로 대체 | 중 |
| **Utilities** | ≥90% | 상 |
| **Entity** | Getter/Setter 제외 | 낮음 |
| **DTO** | 검증 로직만 | 중 |

---

## Test Patterns (Backend)

### AAA Pattern (Arrange-Act-Assert)

```java
@Test
void updateStats_FeedAction_IncreasesHunger() {
    // Arrange: 준비
    User user = User.builder().hunger(50).energy(80).happiness(70).build();
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    // Act: 실행
    StatsResponse response = statsService.updateStats(1L, "feed");

    // Assert: 검증
    assertThat(response.getHunger()).isEqualTo(65); // 50 + 15
    assertThat(response.getHappiness()).isEqualTo(75); // 70 + 5
    verify(userRepository, times(1)).save(user);
}
```

---

### Given-When-Then (BDD Style)

```java
@Test
void givenExistingUser_whenDeleteUser_thenUserIsDeleted() {
    // Given: 기존 사용자가 있고
    User user = userRepository.save(User.builder()
        .email("delete@example.com")
        .passwordHash("hash")
        .username("user")
        .build());

    // When: 사용자를 삭제하면
    userService.deleteUser(user.getId());

    // Then: 사용자가 DB에서 삭제됨
    Optional<User> deletedUser = userRepository.findById(user.getId());
    assertThat(deletedUser).isEmpty();
}
```

---

### Test Fixtures (재사용 가능한 테스트 데이터)

```java
// 테스트 데이터 팩토리
public class UserFixtures {
    public static User createUser() {
        return User.builder()
            .email("test@example.com")
            .passwordHash("hashed_password")
            .username("테스트유저")
            .currentHunger(80)
            .currentEnergy(90)
            .currentHappiness(70)
            .build();
    }

    public static CreateUserRequest createUserRequest() {
        return new CreateUserRequest("new@example.com", "password123", "신규유저");
    }
}

// 사용
@Test
void test() {
    User user = UserFixtures.createUser();
    when(userRepository.save(any())).thenReturn(user);
    // ...
}
```

---

## Anti-Patterns (하지 말 것)

### ❌ Repository Test (불필요)

```java
// ❌ 나쁜 예: Repository를 직접 테스트
@Test
void userRepository_Save_Success() {
    User user = new User();
    userRepository.save(user);
    assertThat(user.getId()).isNotNull();
}

// ✅ 좋은 예: Integration Test에서 Repository 포함
@Test
void createUser_SavesUserToDatabase() {
    CreateUserRequest request = new CreateUserRequest("test@example.com", "password", "user");
    userService.createUser(request);

    Optional<User> savedUser = userRepository.findByEmail("test@example.com");
    assertThat(savedUser).isPresent();
}
```

---

### ❌ Testing Implementation Details

```java
// ❌ 나쁜 예: Private 메서드 테스트
@Test
void _validatePassword_Success() {
    // private 메서드를 리플렉션으로 테스트
    Method method = UserService.class.getDeclaredMethod("_validatePassword", String.class);
    method.setAccessible(true);
    boolean result = (boolean) method.invoke(userService, "password123");
    assertTrue(result);
}

// ✅ 좋은 예: Public API 테스트
@Test
void createUser_InvalidPassword_ThrowsException() {
    CreateUserRequest request = new CreateUserRequest("test@example.com", "weak", "user");
    assertThrows(InvalidPasswordException.class, () -> {
        userService.createUser(request);
    });
}
```

---

### ❌ Over-Mocking

```java
// ❌ 나쁜 예: 모든 것을 Mock
@Test
void test() {
    when(userRepository.save(any())).thenReturn(user);
    when(passwordEncoder.encode(any())).thenReturn("hashed");
    when(jwtProvider.generate(any())).thenReturn("token");
    when(emailService.sendWelcome(any())).thenReturn(true);
    // ... 실제 로직이 하나도 테스트 안 됨
}

// ✅ 좋은 예: 외부 의존성만 Mock
@Test
void test() {
    when(userRepository.save(any())).thenReturn(user);
    // 나머지는 실제 로직 실행 (passwordEncoder, jwtProvider 등)
}
```

---

## Test Organization (Folder Structure)

```
backend/
├── src/
│   ├── main/java/com/lobai/
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── service/
│   │   └── controller/
│   │
│   └── test/java/com/lobai/
│       ├── unit/                     # Unit Tests
│       │   ├── service/
│       │   │   ├── UserServiceTest.java
│       │   │   ├── MessageServiceTest.java
│       │   │   └── StatsServiceTest.java
│       │   └── util/
│       │       ├── PasswordUtilTest.java
│       │       └── JwtTokenProviderTest.java
│       │
│       ├── integration/              # Integration Tests
│       │   ├── UserIntegrationTest.java
│       │   ├── MessageIntegrationTest.java
│       │   └── StatsIntegrationTest.java
│       │
│       └── e2e/                      # E2E Tests
│           ├── UserFlowE2ETest.java
│           └── AuthFlowE2ETest.java
```

---

## Naming Convention

```
{ClassName}Test.java           # Unit Test
{Feature}IntegrationTest.java  # Integration Test
{Flow}E2ETest.java             # E2E Test

Examples:
- UserServiceTest.java
- MessageIntegrationTest.java
- AuthFlowE2ETest.java
```

---

## Frameworks & Tools (Spring Boot)

| Type | Tool | Purpose |
|------|------|---------|
| **Unit** | JUnit 5 | 테스트 프레임워크 |
| **Mocking** | Mockito | Service 레이어 Mock |
| **Integration** | Spring Boot Test | @SpringBootTest |
| **API Test** | MockMvc | Controller 테스트 |
| **DB Test** | H2 또는 Testcontainers | 실제 DB 테스트 |
| **Assertion** | AssertJ | 유창한 단언문 |
| **Coverage** | JaCoCo | 커버리지 리포트 |

---

## Setup Commands

### Maven (pom.xml)

```xml
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- H2 In-Memory DB (테스트용) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Testcontainers (MySQL) -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>mysql</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<!-- JaCoCo 플러그인 -->
<build>
    <plugins>
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Run Tests

```bash
# 전체 테스트 실행
mvn test

# 특정 테스트만 실행
mvn test -Dtest=UserServiceTest

# 커버리지 리포트 생성
mvn test jacoco:report
open target/site/jacoco/index.html
```

---

## Best Practices

### ✅ 1. Test Isolation (테스트 격리)

```java
@Transactional // 각 테스트 후 롤백
@Sql(scripts = "/clear-database.sql", executionPhase = BEFORE_TEST_METHOD)
class UserIntegrationTest {
    // 각 테스트는 독립적
}
```

---

### ✅ 2. Descriptive Test Names

```java
// ❌ 불명확
@Test
void test1() { }

// ✅ 명확
@Test
void createUser_WhenEmailAlreadyExists_ThrowsEmailAlreadyExistsException() { }
```

---

### ✅ 3. One Assertion per Concept

```java
// ✅ 좋은 예: 한 개념당 하나의 테스트
@Test
void createUser_SetsDefaultStats() {
    UserResponse response = userService.createUser(request);
    assertThat(response.getStats().getHunger()).isEqualTo(80);
}

@Test
void createUser_HashesPassword() {
    userService.createUser(request);
    User user = userRepository.findByEmail("test@example.com").get();
    assertThat(user.getPasswordHash()).startsWith("$2a$"); // BCrypt
}
```

---

### ✅ 4. Use @BeforeEach for Common Setup

```java
@BeforeEach
void setUp() {
    user = UserFixtures.createUser();
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
}

@Test
void test1() {
    // user 사용
}

@Test
void test2() {
    // user 사용
}
```

---

### ✅ 5. Test Edge Cases

```java
@Test
void updateStats_WhenHungerExceeds100_CapsAt100() {
    user.setCurrentHunger(95);
    StatsResponse response = statsService.updateStats(1L, "feed"); // +15
    assertThat(response.getHunger()).isEqualTo(100); // not 110
}

@Test
void updateStats_WhenHungerBelowZero_CapsAtZero() {
    user.setCurrentHunger(5);
    statsService.applyDecay(user); // -0.5
    assertThat(user.getCurrentHunger()).isEqualTo(0); // not negative
}
```

---

## TDD Workflow (Backend)

```
1. 🔴 RED: 실패하는 테스트 작성
   @Test
   void createUser_Success() {
       UserResponse response = userService.createUser(request);
       assertThat(response.getEmail()).isEqualTo("test@example.com");
       // Fails: UserService.createUser() not implemented
   }

2. 🟢 GREEN: 최소 코드로 통과
   public UserResponse createUser(CreateUserRequest request) {
       User user = new User();
       user.setEmail(request.getEmail());
       userRepository.save(user);
       return new UserResponse(user.getEmail());
   }

3. 🔵 REFACTOR: 코드 개선
   public UserResponse createUser(CreateUserRequest request) {
       validateEmail(request.getEmail());
       String hashedPassword = passwordEncoder.encode(request.getPassword());

       User user = User.builder()
           .email(request.getEmail())
           .passwordHash(hashedPassword)
           .username(request.getUsername())
           .build();

       User savedUser = userRepository.save(user);
       return UserResponse.from(savedUser);
   }
```

---

## Integration with Architecture

### Controller → Service → Repository 테스트 전략

```
Controller (Integration Test):
  - MockMvc로 HTTP 요청/응답 검증
  - @WebMvcTest 또는 @SpringBootTest

Service (Unit Test):
  - 비즈니스 로직만 집중
  - Repository는 Mock

Repository (Integration Test에 포함):
  - @DataJpaTest
  - 실제 DB (H2 또는 Testcontainers)
```

---

## Coverage Commands

```bash
# Maven
mvn test jacoco:report
open target/site/jacoco/index.html

# Gradle
gradle test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

**Coverage Thresholds (pom.xml)**:
```xml
<configuration>
    <rules>
        <rule>
            <element>PACKAGE</element>
            <limits>
                <limit>
                    <counter>LINE</counter>
                    <value>COVEREDRATIO</value>
                    <minimum>0.80</minimum>
                </limit>
            </limits>
        </rule>
    </rules>
</configuration>
```

---

**Skill Version**: 1.0.0
**Last Updated**: 2025-12-30
**Framework**: Spring Boot 3.x, JUnit 5, Mockito
**Next Review**: 백엔드 개발 시작 후
