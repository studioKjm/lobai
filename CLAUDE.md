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

## Multi-Session Workflow

### 세션 역할 분담

**세션1: 프론트엔드 & UX** 🎨
- GENKUB 인터페이스 개선
- HIP 대시보드 개발
- 사용자 경험 최적화
- React, TypeScript, TailwindCSS

**세션2: 백엔드 코어** ⚙️
- Spring Boot API 개발
- HIP 분석 로직 구현
- 데이터베이스 관리
- Java, Spring, MySQL, Gemini AI

**세션3: 블록체인 인프라** 🔐 (현재 세션)
- Smart Contract 개발 (Solidity)
- Polygon/Ethereum 통합
- IPFS 분산 저장
- Web3j 연동

### 세션 간 협업 규칙

1. **작업 시작 시**: 다른 세션의 진행 상황 확인
   ```bash
   git status
   git log --oneline -5
   ```

2. **충돌 방지**: 세션별 브랜치 전략
   - `session1/feature-name`
   - `session2/feature-name`
   - `session3/blockchain-integration`

3. **세션 인수인계**: 작업 완료 후 문서화
   - 진행 사항을 `SESSION_LOG.md`에 기록
   - 미완료 작업은 TODO로 명시

### 세션3 전용 규칙 (블록체인)

#### 작업 환경

**필수 도구**:
- Node.js 18+ (Hardhat)
- Java 17+ (Web3j)
- Polygon Mumbai 테스트넷 RPC
- IPFS 클라이언트

**환경 변수** (`.env.local`):
```bash
# Blockchain
POLYGON_RPC_URL=https://rpc-mumbai.maticvigil.com
PRIVATE_KEY=your_private_key
CONTRACT_ADDRESS=deployed_contract_address

# IPFS
IPFS_API_URL=https://ipfs.infura.io:5001
IPFS_API_KEY=your_api_key
IPFS_API_SECRET=your_api_secret
```

#### 작업 유형별 도구

| 작업 | 도구 | 사용 시점 |
|------|------|----------|
| Smart Contract 작성 | Hardhat, Remix | Solidity 개발 |
| Contract 배포 | Hardhat scripts | 테스트넷/메인넷 배포 |
| Web3 통합 | Web3j, `backend-developer-agent` | Spring Boot 연동 |
| IPFS 연동 | Pinata API, Java IPFS | 데이터 저장 |
| 보안 검증 | `security-agent` | Contract Audit |
| 테스트 | Hardhat Test, `test-engineer-agent` | 배포 전 검증 |

#### 자동 사용 규칙 (세션3)

1. **Smart Contract 작성 전**: `security-agent`로 취약점 검토
2. **Web3 통합 시**: `backend-developer-agent` 활용
3. **배포 전**: Hardhat 테스트 100% 통과 확인

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
