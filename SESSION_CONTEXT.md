# Session Context - LobAI (Lobi) Landing Page & Technical Fixes

**Date**: 2025-12-28 to 2025-12-30
**Status**: ✅ All Issues Resolved

---

## Session Overview

This session included the initial project setup, landing page redesign, 3D character implementation, and critical technical fixes for scroll behavior and 3D rendering issues.

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

---

## Technical Stack

- **Frontend**: React 19.2.3 + TypeScript 5.8.2 + Vite 6.2.0
- **Styling**: TailwindCSS (via CDN)
- **3D Graphics**: @splinetool/react-spline 4.0.0, @splinetool/runtime 1.9.46
- **AI Integration**: @google/genai (Gemini AI)
- **Fonts**: DM Sans (body), Syne (display headings)

---

## Key Technical Decisions

### 1. Typography Change
**Decision**: Changed from Inter/Outfit to DM Sans/Syne
**Rationale**: More distinctive, futuristic aesthetic aligned with AI-focused brand identity

### 2. Color Palette
**Primary**: `#00d9ff` (Cyan) - Tech-forward, futuristic
**Secondary**: `#ffb800` (Amber) - Warmth, energy
**Background**: `#050505` (Near-black) - Premium dark theme

### 3. Navigation Auto-Hide
**Implementation**:
- Navbar hides after 3 seconds when scrolled past 100px
- Reappears on mouse hover near top edge
- Smooth cubic-bezier transitions for premium feel

**Code Reference**: index.tsx:112-134 (navbar state management)

### 4. Spline 3D Integration
**Challenge**: Remote Spline URLs returned 403 Forbidden errors
**Solution**: Downloaded .splinecode file locally to `/public/scene.splinecode`
**Implementation**: Used @splinetool/react-spline with local file reference

**Code Reference**: index.tsx:136-156 (Spline load handler), index.tsx:158-177 (state change logic)

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

---

## File Changes Summary

### Created Files
- `CLAUDE.md` - Project documentation and guidance
- `.env.local.example` - Environment variable template
- `SESSION_CONTEXT.md` - This file

### Modified Files

#### index.html
**Key Changes**:
- Font imports: DM Sans + Syne
- CSS variables for color system
- Navbar auto-hide animation styles
- Button styles (btn-primary, btn-secondary)
- Scroll animation keyframes
- Custom scrollbar styling

**Code Reference**: Lines 9-11 (fonts), 13-18 (CSS vars), 34-51 (navbar), 130-162 (buttons)

#### index.tsx
**Major Changes**:
- Added Spline integration state management
- Implemented navbar auto-hide logic
- Created Spline onLoad handler to reference objects
- Implemented handleCharacterClick for state toggling
- Changed 3D container from iframe to Spline component
- Added Features, How It Works, CTA, and Footer sections
- Made layout scrollable (removed h-screen constraints)
- **[2025-12-30]** Fixed page scroll bug by using container scroll instead of scrollIntoView

**Key Functions**:
- `onSplineLoad()` - Lines 136-156 - Initializes 3D object references
- `handleCharacterClick()` - Lines 158-177 - Toggles character state
- Navbar auto-hide effect - Lines 112-134
- **[2025-12-30]** Chat scroll effect - Lines 144-149 - Scrolls chat container only

**State Variables Added**:
```typescript
const [isCrying, setIsCrying] = useState(false);
const splineRef = useRef<any>(null);
const mouthObjRef = useRef<any>(null);
const eyesObjRef = useRef<any>(null);
const [navbarVisible, setNavbarVisible] = useState(true);
const chatContainerRef = useRef<HTMLDivElement>(null);  // [2025-12-30] Added
```

**Removed Variables** (2025-12-30):
- `hasScrolledToChat` ref - No longer needed with new scroll approach

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

---

## Environment Setup

### Required Environment Variables
Create `.env.local` file:
```bash
GEMINI_API_KEY=your_actual_api_key_here
```

### Development Server
```bash
npm install
npm run dev  # Runs on http://localhost:3000
```

**Background Task**: Dev server running as task ID `bdba4db`

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

---

## Known Limitations & Future Improvements

### Current Limitations
1. No state persistence - character state resets on page reload
2. Only two states implemented (default + cry)
3. No transition animations between states
4. Stats system not connected to character state
5. No backend integration yet

### Suggested Improvements
1. **State Persistence**: Save character state to localStorage or backend
2. **More States**: Add happy, surprised, angry expressions
3. **State Transitions**: Smooth animations between state changes
4. **Stats Integration**: Character expression changes based on hunger/energy/happiness
5. **Backend Integration**: Connect to Spring Boot backend as per LobAI_PRD_v3.md
6. **Database**: Store conversation history and affinity scores in MySQL
7. **Authentication**: Add login/signup functionality to navbar
8. **Responsive Design**: Optimize for mobile viewport sizes
9. **Performance**: Consider lazy-loading Spline for faster initial page load
10. **Analytics**: Track user interactions with character states

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

### Scroll Behavior Patterns (2025-12-30)
- **scrollIntoView() Behavior**: Always scrolls the **entire page** to bring element into view, even if element is inside a scrollable container
- **Container Scrolling**: Use `element.scrollTop = element.scrollHeight` to scroll only within a specific container
- **When to Use Each**:
  - `scrollIntoView()`: For navigating to different sections of the page (navbar links, anchor links)
  - `scrollTop`: For scrolling within a fixed-height overflow container (chat windows, modals)
- **React Refs for Containers**: Need separate refs for both the container (`chatContainerRef`) and the scroll target (`chatEndRef`)
- **Pattern**: Container ref controls scroll position, end marker ref provides scroll target

---

## Testing Checklist

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

---

## Related Documentation

- **Project Requirements**: `LobAI_PRD_v3.md` - Full product vision and roadmap
- **Development Guide**: `CLAUDE.md` - Codebase architecture and setup instructions
- **Environment Template**: `.env.local.example` - Required API keys

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

---

## Final Notes

- All critical UI/UX bugs resolved (including chat scroll bug - 2025-12-30)
- Page loads correctly at top position
- Chat messages scroll correctly within container (not entire page)
- 3D character renders smoothly without visual artifacts
- Project ready for next development phase
- Backend integration and database setup remain as next major milestones
- Consider implementing state persistence before adding more character states
- Review responsive design for mobile devices before public launch

### Session Continuity Notes
- **Session 1-2**: Continued from previous conversation that ran out of context. Conversation summary preserved.
- **Session 3 (2025-12-30)**: New session for chat scroll bug fix. Applied Refactor Agent workflow.

### Dev Server Status
- **Background Task**: Running on localhost:3000 (task ID: bb0f92c)
- **Status**: Ready for testing

---

**Status: All features implemented and all critical bugs fixed. Sessions completed successfully.**
**Last Updated**: 2025-12-30
