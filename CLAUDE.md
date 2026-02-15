# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**LobAI** - An AI-powered platform where humans "lobby" to AI. The core concept is a power structure reversal: AI is the authority, and humans earn favor through interactions. LobAI analyzes users' AI readiness (HIP - Human Impact Profile), tracks affinity/resilience scores, and provides coaching through an AI companion named **Lobi**.

Users interact with Lobi through chat, complete missions, maintain daily check-in streaks, train with memory/reasoning exercises, and manage schedules - all contributing to their experience points and trust level progression.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18 + TypeScript + Vite (port 5175) |
| Styling | TailwindCSS + glassmorphism dark theme |
| State | Zustand (authStore, chatStore) |
| Backend | Spring Boot 3.2.1 + Java 17 |
| Database | MySQL 8 (UTF-8mb4, Asia/Seoul timezone) |
| AI | Gemini Flash (primary), GPT-4o mini (fallback) via LLM abstraction layer |
| Streaming | SSE (backend Flux + frontend ReadableStream) |
| Blockchain | Polygon (HIP NFTs) |
| Deployment | Docker Compose (lobai-mysql + lobai-backend + frontend) |

## Development Commands

### Frontend
```bash
npm install          # Install dependencies
npm run dev          # Dev server (localhost:5175)
npm run build        # Production build to dist/
npm run preview      # Preview production build
```

### Backend (Docker)
```bash
docker compose build backend                    # Build backend image
docker compose up -d backend                    # Start backend container
docker compose up -d                            # Start all services (MySQL + backend)
docker compose logs -f backend                  # Tail backend logs
docker exec -it lobai-mysql mysql -u root -p lobai_db  # MySQL shell
```

### Full Stack
```bash
docker compose up -d                  # Start MySQL + backend
npm run dev                           # Start frontend (separate terminal)
```

> **Note**: Java is not available in the dev environment. Backend must be built/tested via Docker.

## Architecture

### Frontend (src/)

Multi-page SPA with React Router and protected routes.

```
src/
├── pages/              # 13 pages (ChatPage, AdminPage, TrainingPage, etc.)
├── components/         # 12 directories (chat/, dashboard/, admin/, etc.)
│   ├── chat/           # ChatInterface, PersonaSelector, UserLevelBadge, etc.
│   ├── dashboard/      # UserLevelCard, ConversationHistoryCard, ScheduleCalendar
│   ├── admin/          # Admin stats cards
│   ├── affinity/       # AffinityScoreCard, AffinityProgressRing
│   ├── resilience/     # ResilienceReportCard
│   ├── training/       # MemoryTrainingInterface
│   ├── schedule/       # SidebarSchedule, WeeklyScheduleView, modals
│   ├── auth/           # AuthModal, LoginForm, RegisterForm
│   ├── common/         # Navbar, Footer, ProtectedRoute
│   └── ...
├── stores/             # Zustand stores (authStore, chatStore)
├── lib/                # API clients (api, streamApi, lobcoinApi, scheduleApi, etc.)
├── services/           # Domain API wrappers (adminApi, affinityApi, etc.)
└── types/              # TypeScript interfaces (index.ts, affinity.ts, resilience.ts)
```

**Path Alias**: `@/` maps to `src/` (configured in vite.config.ts and tsconfig.json)

### Backend (backend/)

Spring Boot with layered architecture: Controller → Service → Repository → Entity.

```
backend/src/main/java/com/lobai/
├── controller/         # 17 REST controllers
├── service/            # 37 services (business logic)
├── entity/             # 42 JPA entities
├── repository/         # JPA repositories
├── dto/                # Request/Response DTOs
├── llm/                # Multi-LLM abstraction layer
│   ├── LlmRouter.java          # Auto-routing between providers
│   ├── LlmProvider.java        # Provider interface
│   ├── provider/               # GeminiLlmProvider, OpenAiLlmProvider
│   └── prompt/                 # PersonaPromptTemplate, PromptContext
├── security/           # JWT auth, SecurityUtil, filters
├── config/             # Spring configuration beans
├── exception/          # Custom exceptions
└── util/               # Utilities
```

**Database Migrations**: `backend/src/main/resources/db/migration/` (V4 ~ V19, Flyway)

### Key Backend Services

| Service | Purpose |
|---------|---------|
| MessageService | Chat message handling, XP/LobCoin rewards |
| StreamingMessageService | SSE streaming responses |
| GeminiService | Gemini AI integration (delegates to LlmRouter) |
| ContextAssemblyService | 3-tier context: user profile + daily summaries + recent messages |
| ConversationSummaryService | Daily conversation summarization via LLM |
| AffinityScoreService | User-AI affinity scoring (0-100) |
| ResilienceAnalysisService | AI era adaptability scoring (0-100) |
| LevelService | XP-based level progression (Lv.1-5) + affinity-based (Lv.6-10) |
| LobCoinService | In-app currency economy |
| TrainingService | Memory/reasoning training with scoring |
| ScheduleService | Calendar management with completion rewards |
| HumanIdentityProfileService | HIP analysis system |
| ProactiveMessageService | AI-initiated scheduled messages |
| DailySummaryScheduler | Nightly cron job for conversation summaries |

### Key Systems

**XP & Level System**:
- XP thresholds: Lv.1=0, Lv.2=100, Lv.3=300, Lv.4=700, Lv.5=1500
- XP sources: chat (5), daily check-in (10), streak bonus (5/10/20), training (5-15), schedule completion (15)
- Levels 1-5: XP-based progression (positive growth)
- Levels 6-10: Affinity-based restrictions (warning/restriction/block)

**LLM Abstraction** (`com.lobai.llm`):
- LlmRouter auto-selects provider based on LlmTaskType
- GeminiLlmProvider (primary), OpenAiLlmProvider (fallback)
- PersonaPromptTemplate builds persona-aware system instructions
- PromptContext carries user state, persona, conversation history

**Persona System**: Multiple AI personalities (Lobi default + others), each with distinct system instructions and behavior patterns.

## Environment Setup

### Frontend (.env.local)
```bash
VITE_API_BASE_URL=http://localhost:8080
```

### Backend (application.yml / application-docker.yml)
```yaml
# Key settings already configured in application.yml
spring.datasource.url: jdbc:mysql://localhost:3306/lobai_db
gemini.api.key: ${GEMINI_API_KEY}
```

### Docker (.env.docker)
```bash
GEMINI_API_KEY=your_key
MYSQL_ROOT_PASSWORD=your_password
```

## Important Notes

- **Korean-language app**: All UI text, bot responses, and user-facing content in Korean
- **No local Java**: Backend cannot be compiled/tested locally - use Docker
- **Hibernate DDL**: `ddl-auto: update` in Docker profile, `validate` in local profile
- **Flyway migrations**: Numbered V4-V19, some with duplicate version numbers to fix
- **3D character**: Lobi is rendered via embedded Spline iframe
- **Stats decay**: Hunger/energy/happiness auto-decay and affect Lobi's responses
- **Auth**: JWT with refresh tokens, SecurityUtil.getCurrentUserId() for current user

## Agent & Tool Usage Guidelines

### 작업 유형별 권장 도구

| 작업 유형 | 권장 도구 | 사용 시점 |
|-----------|----------|----------|
| 파일 위치 모를 때 | `Task(Explore)` | 코드 탐색 필요 시 |
| 영향 범위 파악 | `impact-analyzer-agent` 호출 | 수정 전 |
| 간단한 버그/기능 수정 | 직접 수정 또는 `quick-fix-agent` | 1-3개 파일 수정 |
| 백엔드 API 작업 | `backend-developer-agent` 호출 | Entity/Service/Controller 작업 |
| 수정 후 검증 | `test-engineer-agent` 또는 `code-review` 스킬 | 작업 완료 후 |
| 브라우저 테스트 | `playwright` MCP | UI 동작 검증 |

### 자동 사용 규칙

1. **수정 전**: 영향받는 파일이 3개 이상이면 `impact-analyzer-agent` 먼저 실행
2. **백엔드 작업**: Spring Boot 코드 작성 시 `backend-developer-agent` 활용
3. **수정 후**: 중요 변경 시 `code-review` 스킬로 리뷰

### 커스텀 에이전트 목록 (.claude/agents/)

- `quick-fix-agent` - 간단한 버그/기능 수정
- `impact-analyzer-agent` - 변경 영향 분석
- `backend-developer-agent` - Spring Boot 백엔드 개발
- `test-engineer-agent` - 테스트 작성
- `refactor-agent` - 코드 리팩터링
- `security-agent` - 보안 검사
