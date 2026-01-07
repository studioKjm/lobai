# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**GENKUB** - An AI-powered virtual Tamagotchi robot companion with 3D visualization. This is an interactive web app where users care for an AI robot companion (named GENKUB) through feeding, playing, and chatting. The robot has stats (hunger, energy, happiness) that decay over time and responds conversationally in Korean using Google's Gemini AI.

This project is a prototype/MVP for **LobAI**, a platform designed to analyze users' AI readiness and communication patterns through AI interactions.

## Development Commands

```bash
# Install dependencies
npm install

# Run development server (localhost:3000)
npm run dev

# Build for production
npm run build

# Preview production build
npm preview
```

## Environment Setup

Set `GEMINI_API_KEY` in `.env.local` before running the app:

```
GEMINI_API_KEY=your_api_key_here
```

The Vite config maps this to `process.env.API_KEY` and `process.env.GEMINI_API_KEY` at build time.

## Architecture

### Single-File React App

This is a minimal Vite + React + TypeScript app contained primarily in `index.tsx`. There are no separate components, routes, or complex state management.

**Main Features:**
- **Stats System**: Three stats (hunger, energy, happiness) auto-decay every 5 seconds
- **Action Buttons**: Feed, Play, Sleep buttons restore stats and trigger bot responses
- **Chat Interface**: Real-time chat with Gemini AI (model: `gemini-3-flash-preview`)
- **3D Visualization**: Embedded Spline iframe showing the robot character

### Key Technical Details

**AI Integration:**
- Uses `@google/genai` SDK
- System instruction customizes GENKUB's personality (friendly, slightly robotic, Korean language)
- Temperature: 0.8 for natural conversation
- Stats are passed to the system prompt so the bot responds contextually

**Styling:**
- TailwindCSS (via CDN in `index.html`)
- Custom glassmorphism styling (`.glass` class)
- Dark theme with blurred gradient backgrounds
- Custom fonts: Inter (body), Outfit (headings)

**Environment Variables:**
- Vite config uses `loadEnv()` to inject `GEMINI_API_KEY` from `.env.local`
- Accessible in code as `process.env.API_KEY`

**Path Alias:**
- `@/` maps to project root (configured in both `vite.config.ts` and `tsconfig.json`)

## File Structure

- `index.tsx` - Main React application (all UI and logic)
- `index.html` - HTML entry point with TailwindCSS and font imports
- `vite.config.ts` - Vite configuration with env variable injection
- `tsconfig.json` - TypeScript configuration
- `.env.local` - Environment variables (not in git)
- `LobAI_PRD_v3.md` - Product requirements document for the broader LobAI vision

## AI Model Configuration

The bot uses **Gemini 3 Flash Preview** with a Korean-language system instruction that:
- Names the character "GENKUB"
- Gives it a friendly, slightly robotic personality
- Makes it respond based on current stat values
- Keeps responses short (1-2 sentences)
- Uses Korean speech patterns

## Important Notes

- This is a **web-only** app (no backend, no database yet)
- All state is ephemeral (resets on page reload)
- The 3D robot is an external Spline embed, not a local asset
- Stats decay continuously and never persist between sessions
- The app is designed for Korean-speaking users (UI text and bot responses in Korean)

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
