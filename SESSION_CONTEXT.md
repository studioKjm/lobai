# Session Context - LobAI (Lobi) Landing Page & Backend Infrastructure

**Date**: 2025-12-28 to 2025-12-30
**Status**: ✅ Frontend Complete, Backend Infrastructure In Progress

---

## Session Overview

This session included the initial project setup, landing page redesign, 3D character implementation, critical technical fixes for scroll behavior and 3D rendering issues, and **backend infrastructure setup with Spring Boot + MySQL**.

### Key Accomplishments

1. **Repository Initialization**
   - Created CLAUDE.md documentation for project guidance
   - Set up development environment with npm dependencies
   - Created .env.local.example template
   - Initialized git repository with initial commit

2. **Landing Page Redesign**
   - Implemented auto-hiding navigation bar with smooth animations
   - Changed typography to DM Sans (body) and Syne (display) for distinctive aesthetic
   - Created scrollable service introduction sections (Features, How It Works, CTA)
   - Added professional footer
   - Maintained glassmorphism design aesthetic with dark theme

3. **3D Character Interactivity**
   - Integrated Spline 3D character (initially GENKUB, renamed to Lobi) with local scene file
   - Implemented click-to-toggle state changes for character expressions
   - Successfully configured "Mouth Move 2" and "Eyes Move 2" objects to switch between default and "cry" states

4. **Branding Update**
   - Replaced all instances of "GENKUB" with "Lobi" throughout the application
   - Updated component names, AI system instructions, and UI text

5. **Critical Technical Fixes**
   - **Scroll Position Issue**: Fixed page starting at scrolled position (~209px down) on refresh
     - Root cause: Browser scroll anchoring during React rendering
     - Solution: Disabled scroll anchoring + 500ms scroll lock + message-based scroll logic
   - **Spline 3D Rendering Issues**: Fixed white flash and flat-to-3D transformation on load
     - White flash: Made iframe background transparent
     - Flat character: Implemented triple requestAnimationFrame + 200ms timeout + fade-in
   - **Layout Shifts**: Fixed system status panel resizing on load
     - Rolled back layout changes that caused shifts while keeping scroll fixes
   - **Chat Scroll Bug**: Fixed entire page scrolling when sending messages
     - Changed from `scrollIntoView()` to container `scrollTop` manipulation

6. **Backend Infrastructure Setup** ⭐ NEW (2025-12-30)
   - Installed 3 MCP servers (MySQL, GitHub, Playwright)
   - Created Spring Boot 3.2.1 project with Gradle
   - Set up MySQL database (lobai_db)
   - Configured JWT authentication dependencies
   - Established project structure and basic configuration

---

## Technical Stack

### Frontend
- **Framework**: React 19.2.3 + TypeScript 5.8.2 + Vite 6.2.0
- **Styling**: TailwindCSS (via CDN)
- **3D Graphics**: @splinetool/react-spline 4.0.0, @splinetool/runtime 1.9.46
- **AI Integration**: @google/genai (Gemini AI)
- **Fonts**: DM Sans (body), Syne (display headings)

### Backend ⭐ NEW
- **Framework**: Spring Boot 3.2.1
- **Language**: Java 17 (OpenJDK)
- **Build Tool**: Gradle 8.5
- **Database**: MySQL 8.0.44
- **ORM**: Hibernate (JPA)
- **Security**: Spring Security + JWT (JJWT 0.12.3)
- **Connection Pool**: HikariCP

### Development Tools ⭐ NEW
- **MCP Servers**: MySQL, GitHub, Playwright
- **Database Client**: MySQL MCP for schema management
- **CI/CD**: GitHub MCP for PR/Issue automation
- **Testing**: Playwright MCP for E2E testing

---

## Backend Infrastructure Setup (Session 4 - 2025-12-30)

### MCP Server Installation

Installed 3 Model Context Protocol servers to automate backend development workflows:

#### 1. MySQL MCP
**Purpose**: Database query, schema inspection, and validation

**Installation Steps**:
1. Installed MySQL 8.0.44 via Homebrew
2. Created database: `lobai_db` (utf8mb4 charset)
3. Created user: `lobai_user` with password `lobai_dev_password`
4. Granted full privileges on `lobai_db`
5. Added MCP server to Claude Code config

**Configuration** (claude_desktop_config.json):
```json
{
  "mysql": {
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-mysql"],
    "env": {
      "MYSQL_HOST": "localhost",
      "MYSQL_PORT": "3306",
      "MYSQL_USER": "lobai_user",
      "MYSQL_PASSWORD": "lobai_dev_password",
      "MYSQL_DATABASE": "lobai_db"
    }
  }
}
```

**Use Cases**:
- "users 테이블 스키마 보여줘" → Shows CREATE TABLE statement
- "최근 가입 사용자 10명 조회" → SELECT query execution
- "messages 테이블 인덱스 확인" → SHOW INDEX output

**Setup Guide**: `.claude/mcp-configs/mysql-setup.md`

---

#### 2. GitHub MCP
**Purpose**: PR/Issue automation, commit analysis, review automation

**Installation Steps**:
1. Added GitHub MCP to Claude Code config
2. Configured to use `${GITHUB_TOKEN}` environment variable
3. Token generation required: https://github.com/settings/tokens

**Configuration** (claude_desktop_config.json):
```json
{
  "github": {
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-github"],
    "env": {
      "GITHUB_TOKEN": "${GITHUB_TOKEN}"
    }
  }
}
```

**Required Token Permissions**:
- ✅ `repo` (Full control of private repositories)
- ✅ `read:org` (Read org and team membership)
- ✅ `workflow` (Update GitHub Action workflows)

**Use Cases**:
- "GitHub MCP로 PR 생성해줘" → Automated PR creation
- "PR #5 리뷰해줘" → Code review comments
- "최근 커밋 10개 분석해줘" → Commit history analysis

**Setup Guide**: `.claude/mcp-configs/github-setup.md`

---

#### 3. Playwright MCP
**Purpose**: E2E testing, UI automation, cross-browser testing

**Installation Steps**:
1. Installed Playwright via npm: `npm install -D @playwright/test`
2. Installed Chromium browser: `npx playwright install chromium`
3. Added Playwright MCP to Claude Code config

**Configuration** (claude_desktop_config.json):
```json
{
  "playwright": {
    "command": "npx",
    "args": ["-y", "@playwright/mcp-server"],
    "env": {
      "PLAYWRIGHT_HEADLESS": "false",
      "PLAYWRIGHT_BROWSER": "chromium"
    }
  }
}
```

**Use Cases**:
- "Playwright로 로그인 플로우 테스트해줘" → E2E test automation
- "Stats 패널이 제대로 표시되는지 확인해줘" → UI element verification
- "Chrome, Firefox, Safari에서 모두 테스트해줘" → Cross-browser testing

**Setup Guide**: `.claude/mcp-configs/playwright-setup.md`

---

### Spring Boot Project Structure

Created backend project with standard Spring Boot architecture:

```
backend/
├── build.gradle                     # Gradle dependencies
├── settings.gradle                  # Project name
├── gradlew / gradlew.bat           # Gradle wrapper
├── .gitignore                      # Gradle, IDE, env files
│
└── src/
    ├── main/
    │   ├── java/com/lobai/
    │   │   ├── LobaiBackendApplication.java    # Main class
    │   │   │
    │   │   ├── config/
    │   │   │   ├── CorsConfig.java             # CORS settings
    │   │   │   └── SecurityConfig.java         # Spring Security (permitAll for now)
    │   │   │
    │   │   ├── controller/
    │   │   │   └── HealthController.java       # GET /api/health
    │   │   │
    │   │   ├── dto/
    │   │   │   ├── request/                    # Request DTOs (empty)
    │   │   │   └── response/                   # Response DTOs (empty)
    │   │   │
    │   │   ├── entity/                         # JPA Entities (empty)
    │   │   ├── exception/                      # Global exception handler (empty)
    │   │   ├── repository/                     # JPA Repositories (empty)
    │   │   ├── security/                       # JWT classes (empty)
    │   │   └── service/                        # Business logic (empty)
    │   │
    │   └── resources/
    │       └── application.yml                 # App configuration
    │
    └── test/java/com/lobai/                   # Unit tests (empty)
```

---

### Spring Boot Dependencies (build.gradle)

```gradle
dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // MySQL
    runtimeOnly 'com.mysql:mysql-connector-j'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Development Tools
    developmentOnly 'org.springframework.boot:spring-boot-devtools'

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}
```

---

### application.yml Configuration

**Database Connection**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lobai_db?useSSL=false&serverTimezone=Asia/Seoul
    username: lobai_user
    password: lobai_dev_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        show_sql: true
```

**Server Configuration**:
```yaml
server:
  port: 8080
  servlet:
    context-path: /api
```

**JWT Configuration**:
```yaml
jwt:
  secret: your-very-secure-secret-key-at-least-256-bits-long
  access-token-expiry: 900000      # 15분
  refresh-token-expiry: 604800000  # 7일
```

**Gemini API Configuration**:
```yaml
gemini:
  api-key: ${GEMINI_API_KEY:your-gemini-api-key}
  model: gemini-2.0-flash-exp
  temperature: 0.8
```

**CORS Configuration**:
```yaml
cors:
  allowed-origins: http://localhost:3000,http://localhost:5173
  allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
  allowed-headers: "*"
  allow-credentials: true
```

---

### Spring Boot Build & Run Results

**Build Output**:
```
./gradlew clean build -x test

BUILD SUCCESSFUL in 5s
6 actionable tasks: 6 executed
```

**Application Startup Log**:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.1)

2025-12-30T22:36:48 INFO  --- Starting LobaiBackendApplication with PID 64111
2025-12-30T22:36:49 INFO  --- HikariPool-1 - Start completed.
2025-12-30T22:36:50 INFO  --- Tomcat started on port 8080 (http) with context path '/api'
2025-12-30T22:36:50 INFO  --- Started LobaiBackendApplication in 1.83 seconds
```

**Health Check Endpoint**:
- URL: `http://localhost:8080/api/health`
- Method: GET
- Response:
```json
{
  "status": "UP",
  "service": "lobai-backend",
  "timestamp": "2025-12-30T22:36:50",
  "version": "0.0.1-SNAPSHOT"
}
```

---

### Key Technical Decisions (Backend)

#### 1. Spring Boot 3.2.1
**Decision**: Use Spring Boot 3.x instead of 2.x
**Rationale**:
- Native support for Java 17+
- Improved performance with virtual threads
- Modern Spring Security configuration
- Better observability features

#### 2. JWT Token Expiry
**Decision**: Access Token 15분, Refresh Token 7일
**Rationale**:
- Balance between security and user experience
- Short-lived access tokens reduce attack surface
- Refresh tokens allow seamless session extension
- Standard industry practice for web applications

#### 3. JPA DDL Auto: validate
**Decision**: Use `validate` instead of `update` or `create-drop`
**Rationale**:
- Prevents accidental schema changes in production
- Forces explicit schema management via migration scripts
- Catches schema mismatches early
- Professional production-ready approach

#### 4. Context Path: /api
**Decision**: All endpoints under `/api` prefix
**Rationale**:
- Clear separation between API and static resources
- Standard REST API convention
- Easier CORS and proxy configuration
- Future-proof for API versioning (/api/v1, /api/v2)

---

## Database Schema Design

### Tables to be Created (Next Step)

#### 1. users
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,

    -- Stats
    current_hunger INT DEFAULT 80,
    current_energy INT DEFAULT 90,
    current_happiness INT DEFAULT 70,

    -- Persona
    current_persona_id BIGINT,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,

    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 2. personas (5 personas)
```sql
CREATE TABLE personas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,           -- '친구', '상담사', '코치', '전문가', '유머'
    name_en VARCHAR(50) NOT NULL,               -- 'friend', 'counselor', 'coach', 'expert', 'humor'
    display_name VARCHAR(100) NOT NULL,         -- '친구모드'
    system_instruction TEXT NOT NULL,           -- Gemini API 프롬프트
    icon_emoji VARCHAR(10),                     -- '👥', '💬', '🎯', '🎓', '😄'
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE
);
```

**Initial Data (5 Personas)**:
1. **친구모드** (Friend 👥): Casual, empathetic, emoji usage
2. **상담사모드** (Counselor 💬): Listening, non-judgmental, supportive
3. **코치모드** (Coach 🎯): Goal-oriented, action-focused, motivating
4. **전문가모드** (Expert 🎓): Accurate, logical, systematic
5. **유머모드** (Humor 😄): Witty, lighthearted, positive energy

#### 3. messages
```sql
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    persona_id BIGINT NOT NULL,
    role ENUM('user', 'bot') NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (persona_id) REFERENCES personas(id),
    INDEX idx_user_created (user_id, created_at DESC)
);
```

#### 4. user_stats_history
```sql
CREATE TABLE user_stats_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    hunger INT NOT NULL,
    energy INT NOT NULL,
    happiness INT NOT NULL,
    action_type ENUM('feed', 'play', 'sleep', 'chat', 'decay'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 5. refresh_tokens
```sql
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**Design Document**: `docs/plans/PLAN_Backend_Database_Design.md`

---

## Problems Encountered & Solutions

### Problem 1: 403 Forbidden Error with Remote Spline Scene
**Error**: `GET https://prod.spline.design/genkubgreetingrobot-pkBX9j3rcGADQwxJfUloayrR/scene.splinecode 403 (Forbidden)`

**Root Cause**: Spline's CDN prevents direct external access to .splinecode files

**Solution**:
1. Downloaded scene file locally
2. Placed in `/public/scene.splinecode`
3. Changed scene prop to `scene="/scene.splinecode"`

### Problem 2: State Changes Not Visually Appearing
**Symptoms**: Console logs showed successful state changes, but character didn't update visually

**Debugging Steps**:
1. Verified objects were found correctly
2. Inspected object properties (discovered state getter/setter)
3. Tried multiple state change methods (emitEvent, emitEventReverse, direct assignment)
4. Logged state values before/after changes
5. Verified state names matched Spline editor configuration

**Solution**: Direct state property assignment with correct state names
```typescript
mouthObjRef.current.state = stateName; // 'cry' or 'State'
eyesObjRef.current.state = stateName;
```

**Code Reference**: index.tsx:162-176

### Problem 3: Page Starting at Scrolled Position on Refresh (CRITICAL)
**Symptoms**: Page starts at ~209.5px scroll position instead of top (0px) on refresh

**Initial Wrong Approach**: Added forced `window.scrollTo(0,0)` calls everywhere - this treated symptoms, not cause

**Root Cause Investigation**:
1. Added console logging to track scroll position on mount
2. Discovered `isInitialMount` ref was false when chat scroll effect ran
3. Found `chatEndRef.scrollIntoView` was triggering on initial mount
4. Identified browser scroll anchoring during React rendering as primary cause
5. Found flexbox layout shifts during hydration contributing to problem

**Root Causes**:
- Browser scroll anchoring automatically adjusting scroll during DOM changes
- `chatEndRef.scrollIntoView` running on component mount
- Layout shifts from flexbox order properties during responsive breakpoints
- Hero section `justify-center` pushing content down during render

**Solutions Applied**:
1. **HTML (index.html)**:
   - Added `overflow-anchor: none` to html, body, #root
   - Added scroll lock script in head (runs before React, 500ms duration)
   - Set `history.scrollRestoration = 'manual'`

2. **React (index.tsx)**:
   - Changed chat scroll logic to message count-based (only scroll when messages > 1)
   - Removed hash from URL on mount to prevent anchor scroll
   - Implemented controlled navigation with `scrollIntoView`
   - Added `hasScrolledToChat` ref to prevent initial scroll

**Code References**:
- index.html:9-24 (scroll lock script)
- index.html:37-52 (overflow-anchor CSS)
- index.tsx chat scroll logic (message count-based)

### Problem 4: Layout Shifts After Scroll Fixes
**Symptoms**: After implementing scroll fixes, three new issues appeared:
1. White flash when Spline character loads
2. Character appears flat then transforms to 3D
3. System status panel shrinks then expands

**User Directive**: "Keep only fixes that worked for scroll issue, rollback changes causing layout shifts"

**Solution**:
- Kept: overflow-anchor, scroll lock, hash removal, message-based scroll
- Rolled back: useLayoutEffect→useEffect, height changes, inline styles, flexbox order changes, debug logs
- Result: Status panel issue fixed

### Problem 5: Spline White Flash on Load
**Symptoms**: White background briefly appears before character renders

**Root Cause**: Iframe default background color

**Solution**: Added CSS to make iframe background transparent
```css
.spline-container iframe {
  background-color: transparent !important;
}
```

**Code Reference**: index.html:211-213

### Problem 6: Spline Character Flat-to-3D Transformation (CRITICAL)
**Symptoms**: Character appears flat/2D for brief moment, then "pops" into 3D form

**First Attempt**: 100ms delay - insufficient, problem persisted

**Root Cause**: Spline 3D geometry loads in phases:
1. Iframe loads
2. Scene file downloads
3. Geometry processes
4. Initial render (may be incomplete)
5. Full 3D render completes

**Solution**: Triple requestAnimationFrame + 200ms timeout + visibility control + fade-in
```typescript
const onSplineLoad = (splineApp: any) => {
  // ... object references ...

  // Wait for full 3D render (~250-300ms total)
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        setTimeout(() => {
          setSplineReady(true);  // triggers fade-in
        }, 200);
      });
    });
  });
};
```

**UI Changes**:
- Added `visibility: hidden` until `splineReady` is true
- Added 700ms fade-in transition with ease-out timing
- Prevents any intermediate render frames from showing

**Code Reference**: index.tsx onSplineLoad function, Spline container div

**Result**: Character now loads completely before appearing, with smooth fade-in

### Problem 7: Page Scroll on Chat Message & System Status Actions (CRITICAL)
**Date**: 2025-12-30

**Symptoms**:
- When sending AI messages → entire page scrolls down
- When clicking System Status buttons (FEED UNIT, INITIATE PLAY, SLEEP MODE) → entire page scrolls down
- Previously fixed "page starts at scrolled position" bug did not recur
- Only chat container should scroll, not the entire page

**Root Cause Investigation**:
- Line 143-151: `chatEndRef.scrollIntoView({ behavior: 'smooth' })` was triggering on every message change
- `scrollIntoView()` scrolls the **entire page** to bring the element into view
- `handleAction()` at line 166 adds bot response messages → triggers scroll effect
- This affected both AI chat and System Status button clicks

**Root Cause**:
- `useEffect` watching `messages` array was using `scrollIntoView()` which scrolls the whole page
- No distinction between "scroll chat container" vs "scroll page"

**Solution Applied**: Changed from page scroll to container scroll
1. **Added chatContainerRef** (index.tsx:33):
   ```typescript
   const chatContainerRef = useRef<HTMLDivElement>(null);
   ```

2. **Modified scroll logic** (index.tsx:144-149):
   ```typescript
   // ❌ Before: Scrolls entire page
   chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });

   // ✅ After: Scrolls only chat container
   chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
   ```

3. **Connected ref to JSX** (index.tsx:334):
   ```typescript
   <div ref={chatContainerRef} className="flex-1 overflow-y-auto p-6 space-y-4">
   ```

4. **Removed unused ref**: Deleted `hasScrolledToChat` ref (no longer needed)

**Code References**:
- index.tsx:33 (chatContainerRef declaration)
- index.tsx:144-149 (scroll logic)
- index.tsx:334 (JSX ref connection)

**Result**:
- ✅ Chat messages scroll to bottom inside chat container only
- ✅ System Status button clicks no longer scroll the page
- ✅ Page stays at user's current scroll position
- ✅ Initial page load still starts at top (previous fix maintained)

**Agent Used**: Refactor Agent workflow applied (read → analyze → refactor → verify)

### Problem 8: Gradle Build Failures (Backend Setup)
**Date**: 2025-12-30

**Issue 1: Java Runtime Not Found**
**Symptoms**: `Unable to locate a Java Runtime`

**Solution**:
- Installed OpenJDK 17 via Homebrew
- Set JAVA_HOME environment variable
```bash
brew install openjdk@17
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
```

**Issue 2: Main Class Not Found**
**Symptoms**: `Main class name has not been configured`

**Root Cause**: Source files were created outside backend directory

**Solution**: Moved `src/` directory into `backend/` folder
```bash
mv src backend/
```

**Issue 3: Nested Backend Directory**
**Symptoms**: `backend/backend/gradlew` path confusion

**Root Cause**: Gradle wrapper files were generated in wrong location

**Solution**: Consolidated files into single `backend/` directory
```bash
mv backend/backend/* backend/
rmdir backend/backend
```

**Final Build Result**:
```
./gradlew clean build -x test
BUILD SUCCESSFUL in 5s
```

---

## File Changes Summary

### Created Files (Session 4 - Backend)

#### MCP Configuration
- `.claude/mcp-configs/mysql-setup.md` - MySQL MCP installation guide
- `.claude/mcp-configs/github-setup.md` - GitHub MCP installation guide
- `.claude/mcp-configs/playwright-setup.md` - Playwright MCP installation guide

#### Backend Project Structure
- `backend/build.gradle` - Gradle build configuration
- `backend/settings.gradle` - Project settings
- `backend/.gitignore` - Git ignore rules
- `backend/gradlew` - Gradle wrapper (Unix)
- `backend/gradlew.bat` - Gradle wrapper (Windows)
- `backend/gradle/` - Gradle wrapper files

#### Java Source Files
- `backend/src/main/java/com/lobai/LobaiBackendApplication.java` - Main application class
- `backend/src/main/java/com/lobai/config/CorsConfig.java` - CORS configuration
- `backend/src/main/java/com/lobai/config/SecurityConfig.java` - Spring Security configuration
- `backend/src/main/java/com/lobai/controller/HealthController.java` - Health check endpoint

#### Configuration Files
- `backend/src/main/resources/application.yml` - Application properties

#### Package Directories (Empty, Ready for Development)
- `backend/src/main/java/com/lobai/dto/request/`
- `backend/src/main/java/com/lobai/dto/response/`
- `backend/src/main/java/com/lobai/entity/`
- `backend/src/main/java/com/lobai/exception/`
- `backend/src/main/java/com/lobai/repository/`
- `backend/src/main/java/com/lobai/security/`
- `backend/src/main/java/com/lobai/service/`
- `backend/src/test/java/com/lobai/`

### Modified Files

#### Claude Code Configuration
- `~/Library/Application Support/Claude/claude_desktop_config.json` - Added MySQL, GitHub, Playwright MCPs

#### MCP Configs README
- `.claude/mcp-configs/README.md` - Added documentation for 3 new MCP servers

#### Frontend Files (Previous Sessions)
- `index.html` - Scroll fixes, Spline rendering fixes, font imports
- `index.tsx` - Chat scroll container fix, navbar auto-hide, Spline integration
- `CLAUDE.md` - Project documentation
- `.env.local.example` - Environment variable template

---

## Current Implementation Details

### 3D Character State System

**Object Names in Spline**:
- "Mouth Move 2" - Controls mouth expression
- "Eyes Move 2" - Controls eye expression

**Available States**:
- `"State"` - Default/neutral expression
- `"cry"` - Crying/sad expression

**Toggle Behavior**:
- Click character → Switch to "cry" state
- Click again → Return to "State" (default)
- State persists in component state (`isCrying`)

**Implementation**:
```typescript
const newState = !isCrying;
setIsCrying(newState);
const stateName = newState ? 'cry' : 'State';

if (mouthObjRef.current && eyesObjRef.current) {
  mouthObjRef.current.state = stateName;
  eyesObjRef.current.state = stateName;
}
```

### Navigation Bar Behavior

**Auto-Hide Logic**:
1. Starts visible on page load
2. After 3 seconds → hides if scrolled past 100px
3. Mouse hover at top 50px → shows navbar
4. Mouse leaves top area → starts 3-second hide timer again

**Implementation**: Event listeners for scroll, mousemove, and mouseleave

### Backend API Endpoints

**Current**:
- `GET /api/health` - Health check endpoint

**Planned (Next Phase)**:
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login (JWT tokens)
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/logout` - Logout
- `GET /api/messages` - Get chat history
- `POST /api/messages` - Send message + AI response
- `GET /api/stats` - Get current stats
- `PUT /api/stats` - Update stats (feed/play/sleep)
- `GET /api/personas` - Get 5 personas
- `PUT /api/personas/current` - Change current persona

---

## Environment Setup

### Frontend Environment Variables
Create `.env.local` file:
```bash
GEMINI_API_KEY=your_actual_api_key_here
```

### Backend Environment Variables
Create `backend/.env` file (optional, currently using application.yml):
```bash
DB_PASSWORD=lobai_dev_password
JWT_SECRET=your-very-secure-secret-key-at-least-256-bits-long
GEMINI_API_KEY=your_gemini_api_key
SPRING_PROFILES_ACTIVE=dev
```

### Development Servers

**Frontend**:
```bash
npm install
npm run dev  # Runs on http://localhost:3000
```

**Backend**:
```bash
cd backend
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
./gradlew bootRun  # Runs on http://localhost:8080
```

**MySQL**:
```bash
brew services start mysql@8.0
mysql -u lobai_user -p lobai_db  # password: lobai_dev_password
```

---

## Design System

### Typography Scale
- **Display Headlines**: Syne 700-800, 48-64px
- **Section Titles**: Syne 600-700, 32-40px
- **Body Text**: DM Sans 400-500, 16-18px
- **UI Elements**: DM Sans 500-600, 14-16px

### Color Variables
```css
--color-primary: #00d9ff;    /* Cyan - primary brand */
--color-secondary: #ffb800;   /* Amber - accent */
--color-bg: #050505;          /* Near-black background */
--color-text: #f5f5f5;        /* Off-white text */
```

### Glass Effect
```css
background: rgba(255, 255, 255, 0.03);
backdrop-filter: blur(20px);
border: 1px solid rgba(255, 255, 255, 0.05);
border-radius: 24px;
```

---

## Landing Page Sections

1. **Hero Section** (Viewport height)
   - Left: System status panel (hunger, energy, happiness stats)
   - Center: 3D Spline character (clickable)
   - Right: Chat interface with Gemini AI

2. **Features Section**
   - 3 feature cards with gradient accents
   - AI 친화도 분석 (Affinity Analysis)
   - AI 시대 적응력 진단 (Resilience Assessment)
   - 개인 맞춤 코칭 (Personalized Coaching)

3. **How It Works Section**
   - 3-step process explanation
   - Visual step indicators with gradients

4. **CTA Section**
   - Call-to-action with gradient button
   - Emphasis on starting AI readiness journey

5. **Footer**
   - Copyright and legal links
   - Minimal, clean design

---

## Performance Considerations

### Animations
- Uses CSS-only animations for navbar hide/show
- Scroll-triggered animations with `animation-delay` for stagger effect
- Hardware-accelerated transforms (`translateY`, `translateX`)

### 3D Model
- Local .splinecode file (~size varies)
- Loaded asynchronously with onLoad callback
- Object references cached in useRef to avoid re-lookups

### Asset Loading
- TailwindCSS loaded via CDN (faster initial setup, slower first load)
- Google Fonts with preconnect for faster font loading
- Spline runtime loaded as npm package (bundled)

### Backend Performance
- **HikariCP Connection Pool**: Efficient database connection management
- **JPA Query Optimization**: Use `@Query` with JPQL for complex queries
- **JWT Stateless Auth**: No session storage, scales horizontally
- **Spring Boot DevTools**: Hot reload during development

---

## Known Limitations & Future Improvements

### Current Limitations

**Frontend**:
1. No state persistence - character state resets on page reload
2. Only two states implemented (default + cry)
3. No transition animations between states
4. Stats system not connected to character state
5. No backend integration yet

**Backend**:
1. No JPA entities implemented yet
2. No JWT authentication implemented (all endpoints permitAll)
3. No database schema applied
4. No service layer logic
5. No exception handling

### Suggested Improvements

**Phase 1 (Week 1-2)**:
1. **Database Schema**: Apply SQL migrations to create tables
2. **JPA Entities**: Create User, Persona, Message, UserStatsHistory, RefreshToken entities
3. **JWT Authentication**: Implement JwtTokenProvider, AuthController
4. **Basic CRUD**: UserService, MessageService, PersonaService

**Phase 2 (Week 3-4)**:
5. **Frontend Integration**: Connect React to Spring Boot APIs
6. **Stats Integration**: Character expression changes based on hunger/energy/happiness
7. **Persona System**: 5 personas with Gemini API integration
8. **Message History**: Load previous conversations from database

**Phase 3 (Future)**:
9. **State Persistence**: Save character state to localStorage/backend
10. **More States**: Add happy, surprised, angry expressions
11. **State Transitions**: Smooth animations between state changes
12. **Analytics**: Track user interactions and AI affinity scores
13. **Responsive Design**: Optimize for mobile viewport sizes
14. **Performance**: Lazy-load Spline for faster initial page load

---

## Key Technical Learnings

### Browser Scroll Behavior
- **Scroll Anchoring**: Browsers automatically adjust scroll position during DOM content changes to keep visible content in view
- **Solution**: Disable with `overflow-anchor: none` on html, body, and root containers
- **Scroll Restoration API**: `history.scrollRestoration = 'manual'` prevents browser from restoring previous scroll position

### React Rendering Timing
- **useEffect vs useLayoutEffect**: useLayoutEffect runs synchronously after DOM mutations but before paint
- **Message Count Pattern**: Use message array length to conditionally trigger effects instead of mount flags
- **Ref Timing**: useRef values may not update in expected order across multiple useEffect hooks

### 3D Model Loading with Spline
- **Loading Phases**: Spline goes through multiple render cycles before geometry is fully loaded
- **requestAnimationFrame**: Nest multiple RAF calls to wait for browser render cycles to complete
- **Visibility Control**: Use `visibility: hidden` instead of `display: none` to allow rendering but hide output
- **Fade-in Pattern**: Combine RAF timing with CSS transitions for smooth appearance

### Performance Patterns
- **Early Script Execution**: Critical scroll locks should run in HTML head before React loads
- **Passive Event Listeners**: Use `{ passive: false, capture: true }` for scroll prevention
- **Cleanup Timing**: Remove event listeners with setTimeout after React mounts (500ms sufficient)

### CSS Architecture
- **Glassmorphism**: Combine `backdrop-filter`, low-opacity backgrounds, and subtle borders
- **Transparent Iframes**: Use `!important` flag for iframe background overrides
- **Hardware Acceleration**: Use `transform` properties instead of `top`/`left` for animations

### Scroll Behavior Patterns
- **scrollIntoView() Behavior**: Always scrolls the **entire page** to bring element into view, even if element is inside a scrollable container
- **Container Scrolling**: Use `element.scrollTop = element.scrollHeight` to scroll only within a specific container
- **When to Use Each**:
  - `scrollIntoView()`: For navigating to different sections of the page (navbar links, anchor links)
  - `scrollTop`: For scrolling within a fixed-height overflow container (chat windows, modals)
- **React Refs for Containers**: Need separate refs for both the container (`chatContainerRef`) and the scroll target (`chatEndRef`)
- **Pattern**: Container ref controls scroll position, end marker ref provides scroll target

### Spring Boot Best Practices
- **Package Structure**: Separate concerns (controller, service, repository, entity, dto, config)
- **DTO Pattern**: Never expose entities directly in API responses
- **Service Layer**: All business logic in service layer, thin controllers
- **Exception Handling**: Global exception handler with @ControllerAdvice
- **Configuration**: Externalize sensitive data (application.yml, environment variables)
- **Testing**: Unit tests for services, integration tests for controllers

### Database Design Patterns
- **utf8mb4 Charset**: Required for emoji and special character support
- **Soft Deletes**: Consider `is_deleted` flag instead of hard deletes
- **Timestamps**: Always include `created_at` and `updated_at`
- **Indexes**: Add indexes on foreign keys and frequently queried columns
- **Constraints**: Use CHECK constraints for valid ranges (stats 0-100)

---

## Testing Checklist

**Frontend** ✅:
- [✓] Dev server runs without errors
- [✓] 3D character loads correctly
- [✓] Character state changes on click
- [✓] State toggles between default and cry
- [✓] Navbar auto-hides after scroll
- [✓] Navbar reappears on top hover
- [✓] Chat interface functional with Gemini AI
- [✓] Stats decay system operational
- [✓] Feed/Play/Sleep buttons work
- [✓] All sections scroll smoothly
- [✓] Glassmorphism effects render correctly
- [✓] Gradient text displays properly
- [✓] Animations trigger on scroll
- [✓] Page starts at top (0px) on refresh/load
- [✓] No white flash during Spline character load
- [✓] Character renders fully in 3D (no flat-to-3D transformation)
- [✓] System status panel maintains size on load
- [✓] All text changed from GENKUB to Lobi
- [✓] Build completes without errors
- [✓] Chat messages scroll within chat container only (not entire page)
- [✓] System Status buttons don't scroll the page
- [✓] AI message responses don't scroll the page

**Backend** ⏳ (In Progress):
- [✓] Spring Boot application starts successfully
- [✓] MySQL database connection established
- [✓] Health check endpoint returns 200 OK
- [✓] Gradle build completes without errors
- [✓] CORS configuration allows frontend requests
- [ ] Database schema applied
- [ ] 5 personas inserted into database
- [ ] JWT token generation works
- [ ] User registration endpoint functional
- [ ] User login endpoint functional
- [ ] Message persistence works
- [ ] Stats update works
- [ ] Persona switching works

**MCP Servers** ⚠️ (Requires Claude Code Restart):
- [✓] MySQL MCP configured in claude_desktop_config.json
- [✓] GitHub MCP configured in claude_desktop_config.json
- [✓] Playwright MCP configured in claude_desktop_config.json
- [ ] MySQL MCP responds to queries (requires restart)
- [ ] GitHub MCP responds to commands (requires token)
- [ ] Playwright MCP responds to test commands (requires restart)

---

## Related Documentation

- **Project Requirements**: `LobAI_PRD_v3.md` - Full product vision and roadmap
- **Development Guide**: `CLAUDE.md` - Codebase architecture and setup instructions
- **Environment Template**: `.env.local.example` - Required API keys
- **Backend Design Plan**: `docs/plans/PLAN_Backend_Database_Design.md` - Database schema and implementation plan
- **AI Infrastructure Review**: `docs/AI_INFRASTRUCTURE_REVIEW.md` - Agents, Skills, MCP servers analysis
- **Automated Workflow**: `docs/workflows/AUTOMATED_WORKFLOW.md` - AI-powered development workflows

---

## Session Timeline

### Session 1: Initial Setup (2025-12-28)
1. **Initial Setup** (10-15 min)
   - Created CLAUDE.md
   - Ran npm install
   - Initialized git repository

2. **Landing Page Redesign** (20-30 min)
   - Implemented new typography and color system
   - Created auto-hiding navbar
   - Added scrollable content sections
   - Added footer

3. **3D Character Integration** (30-40 min)
   - Attempted remote Spline URL (failed with 403)
   - Integrated local .splinecode file
   - Implemented state change logic
   - Debugged state visibility issues
   - **Resolution**: Direct state property assignment works

### Session 2: Branding & Critical Fixes (2025-12-28 to 2025-12-29)
1. **Branding Update** (5-10 min)
   - Replaced GENKUB with Lobi throughout application
   - Session crashed with Korean character boundary error
   - Recovery: Broke edits into smaller chunks

2. **Scroll Position Investigation & Fix** (60-90 min)
   - **Initial Problem**: Page starting at 209.5px scroll position
   - **Wrong Approach**: Forced scrollTo(0,0) - symptom suppression
   - **User Feedback**: Find root cause, not forced fixes
   - **Investigation**: Added console logging, discovered scroll anchoring
   - **Root Causes**: Browser scroll anchoring + chatEndRef scroll + layout shifts
   - **Solutions**: overflow-anchor:none + scroll lock + message-based scroll logic
   - **Result**: Page now starts at top

3. **Layout Shift Fixes** (20-30 min)
   - **New Problems**: White flash, flat character, panel resize
   - **User Directive**: Keep scroll fixes, rollback layout changes
   - **Actions**: Selective rollback of changes
   - **Result**: Panel resize fixed

4. **Spline Rendering Fixes** (30-45 min)
   - **White Flash**: Made iframe background transparent
   - **Flat-to-3D Transform**:
     - First attempt: 100ms delay (insufficient)
     - Final solution: Triple RAF + 200ms + visibility:hidden + 700ms fade-in
   - **Result**: Smooth 3D character appearance

**Total Time Across Sessions**: ~3-4 hours

### Session 3: Chat Scroll Bug Fix (2025-12-30)
1. **Bug Report** (User feedback)
   - User reported: AI messages and System Status button clicks scroll the entire page down
   - Previously fixed "page starts at scrolled position" bug did not recur (good)
   - Expected: Only chat container should scroll, not entire page

2. **Investigation & Analysis** (10-15 min)
   - Read index.tsx to understand scroll logic
   - Identified root cause: `scrollIntoView()` at lines 143-151
   - Analyzed how `messages` array changes trigger scroll effect
   - Found `handleAction()` adds messages → triggers unwanted page scroll

3. **Refactor Agent Workflow Applied** (15-20 min)
   - Added `chatContainerRef` to reference chat container div
   - Changed scroll approach from `scrollIntoView()` to `scrollTop = scrollHeight`
   - Connected ref to JSX chat container element
   - Removed unused `hasScrolledToChat` ref
   - Started dev server to verify fix

4. **Documentation Update** (10 min)
   - Updated SESSION_CONTEXT.md with Problem 7 details
   - Added code references and solution explanation
   - Updated file changes summary

**Session Time**: ~30-45 min

### Session 4: Backend Infrastructure Setup (2025-12-30) ⭐ NEW
1. **MCP Server Installation** (45-60 min)
   - Installed MySQL 8.0.44 via Homebrew
   - Created lobai_db database and lobai_user
   - Configured MySQL MCP server
   - Added GitHub MCP server (requires token generation)
   - Installed Playwright + added Playwright MCP server
   - Updated claude_desktop_config.json with 3 MCP servers

2. **Spring Boot Project Creation** (30-45 min)
   - Created backend directory structure
   - Generated build.gradle with dependencies
   - Created application.yml configuration
   - Set up package structure (config, controller, dto, entity, etc.)
   - Created main application class
   - Added CORS and Security configuration
   - Created health check endpoint

3. **Build & Run Testing** (20-30 min)
   - Installed Gradle
   - Installed OpenJDK 17
   - Fixed nested directory structure issues
   - Successfully built project (BUILD SUCCESSFUL)
   - Ran Spring Boot application (started in 1.83s)
   - Verified health check endpoint works
   - Stopped application

4. **Documentation & Planning** (15-20 min)
   - Created MCP setup guides (mysql-setup.md, github-setup.md, playwright-setup.md)
   - Updated .claude/mcp-configs/README.md
   - Prepared for database schema creation (next step)

**Session Time**: ~2-2.5 hours

**Total Project Time**: ~6-7 hours across 4 sessions

---

## Next Steps

### Immediate (Git & Database)
1. **Git Commit**: Commit Spring Boot project creation
   - Branch: `feature/backend-setup` or directly to `master`
   - Commit message: "Add Spring Boot backend with MySQL MCP integration"

2. **Database Schema Creation**:
   - Create `backend/src/main/resources/db/schema.sql`
   - Apply schema to MySQL database
   - Verify with MySQL MCP: "lobai_db 데이터베이스 테이블 목록 조회"

3. **Initial Data Insertion**:
   - Create `backend/src/main/resources/db/data.sql`
   - Insert 5 personas with system instructions
   - Verify with MySQL MCP: "personas 테이블 데이터 조회"

### Phase 1 - Week 1 (JWT + Entities)
4. **JPA Entity Creation** (Backend Developer Agent):
   - User.java
   - Persona.java
   - Message.java
   - UserStatsHistory.java
   - RefreshToken.java

5. **JWT Authentication System**:
   - JwtTokenProvider.java (token generation/validation)
   - JwtAuthenticationFilter.java (request filtering)
   - CustomUserDetailsService.java (Spring Security integration)

6. **Authentication Controller**:
   - POST /api/auth/register
   - POST /api/auth/login
   - POST /api/auth/refresh
   - POST /api/auth/logout

### Phase 1 - Week 2 (Core Features)
7. **Repository Layer**:
   - UserRepository extends JpaRepository
   - MessageRepository extends JpaRepository
   - PersonaRepository extends JpaRepository
   - UserStatsHistoryRepository extends JpaRepository
   - RefreshTokenRepository extends JpaRepository

8. **Service Layer**:
   - AuthService (registration, login, token refresh)
   - MessageService (save, retrieve, Gemini API integration)
   - StatsService (get, update, history)
   - PersonaService (list, get, switch)

9. **Controller Layer**:
   - MessageController (GET /messages, POST /messages)
   - StatsController (GET /stats, PUT /stats)
   - PersonaController (GET /personas, PUT /personas/current)

### Phase 1 - Week 3 (Frontend Integration)
10. **Frontend API Integration**:
    - Replace Gemini direct call with backend API
    - Add login/signup UI
    - Persist JWT tokens in localStorage
    - Load message history on login
    - Sync stats with backend

11. **Testing**:
    - Unit tests for services (Backend Test Strategy)
    - Integration tests for controllers (MockMvc)
    - E2E tests with Playwright MCP

### Phase 1 - Week 4 (Deployment Prep)
12. **Production Readiness**:
    - Environment-specific configs (dev, prod)
    - Security hardening (JWT secret from env)
    - HTTPS configuration
    - Logging and monitoring
    - Error handling and validation

**Design Document**: `docs/plans/PLAN_Backend_Database_Design.md`

---

## Final Notes

- ✅ All critical UI/UX bugs resolved (including chat scroll bug - 2025-12-30)
- ✅ Page loads correctly at top position
- ✅ Chat messages scroll correctly within container (not entire page)
- ✅ 3D character renders smoothly without visual artifacts
- ✅ Spring Boot backend infrastructure created
- ✅ MySQL database configured and connected
- ✅ MCP servers installed (MySQL, GitHub, Playwright)
- ⏳ Database schema creation pending
- ⏳ JWT authentication implementation pending
- ⏳ Frontend-backend integration pending

### Session Continuity Notes
- **Session 1-2**: Continued from previous conversation that ran out of context. Conversation summary preserved.
- **Session 3 (2025-12-30)**: New session for chat scroll bug fix. Applied Refactor Agent workflow.
- **Session 4 (2025-12-30)**: Backend infrastructure setup. Installed MCP servers, created Spring Boot project.

### Development Environment Status
- **Frontend Dev Server**: Ready to start on localhost:3000
- **Backend Dev Server**: Ready to start on localhost:8080 (Spring Boot)
- **MySQL Server**: Running on localhost:3306 (lobai_db)
- **MCP Servers**: Configured, requires Claude Code restart to activate

### Security Notes
- ⚠️ MySQL password currently in application.yml (development only)
- ⚠️ GitHub token required for GitHub MCP (generate at github.com/settings/tokens)
- ⚠️ JWT secret in application.yml needs to be changed for production
- ⚠️ Gemini API key should be moved to environment variable

---

**Status: Frontend complete and tested. Backend infrastructure ready. Database schema creation next.**
**Last Updated**: 2025-12-30 22:40 KST
