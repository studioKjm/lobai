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
