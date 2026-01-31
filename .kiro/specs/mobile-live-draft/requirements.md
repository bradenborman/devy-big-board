# Mobile Live Draft - Requirements

## Overview
Create a mobile-responsive version of the live draft feature that provides the same functionality as the desktop version but optimized for touch interactions and smaller screens. This follows the same pattern as the offline draft, which has separate desktop and mobile components.

## User Stories

### US-1: Mobile Detection and Routing
**As a** user accessing the live draft on a mobile device  
**I want** the app to automatically detect my screen size and show the mobile-optimized interface  
**So that** I have a better experience on my phone or tablet

**Acceptance Criteria:**
- App detects screen width ≤ 768px as mobile
- Mobile users are automatically shown the mobile live draft component
- Desktop users continue to see the desktop live draft component
- Screen resize dynamically switches between mobile and desktop views
- Mobile detection works for both lobby and active draft pages

### US-2: Mobile Lobby Experience
**As a** user joining a draft on mobile  
**I want** a touch-friendly lobby interface  
**So that** I can easily join, see participants, and start the draft

**Acceptance Criteria:**
- Mobile lobby shows draft name, teams, and rounds prominently
- Position selection uses large touch-friendly buttons
- Nickname input is optimized for mobile keyboards
- Participant list is scrollable and easy to read
- "Start Draft" button is prominent and easy to tap
- "Copy Lobby Link" button works on mobile
- All lobby functionality from desktop is available

### US-3: Mobile Draft Board with Carousel
**As a** user drafting on mobile  
**I want** to see all picks in a horizontal scrollable carousel  
**So that** I can easily navigate through the draft and see what's been picked

**Acceptance Criteria:**
- Horizontal carousel shows all picks (round.pick format)
- Current pick is highlighted and auto-scrolls into view
- Swipe left/right to navigate through picks
- Tapping a pick in carousel jumps to that pick
- Filled picks show player avatar, name, and position badge
- Empty picks show placeholder with pick number
- Carousel supports smooth scrolling with momentum
- Visual indicator shows current pick location

### US-4: Mobile Player Selection
**As a** user making a pick on mobile  
**I want** a full-screen player selection sheet  
**So that** I can easily browse and select players with touch

**Acceptance Criteria:**
- "Select Player" button opens full-screen player sheet
- Player sheet slides up from bottom with animation
- Position filter buttons (ALL, QB, RB, WR, TE) at top
- Year filter available (same as desktop)
- Scrollable player list with large touch targets
- Each player shows avatar, name, position, team, college, ADP
- Tapping a player immediately makes the pick
- Sheet closes after selection
- Swipe down or tap X to close without selecting
- Loading state while fetching players

### US-5: Mobile Current Pick Display
**As a** user on mobile  
**I want** to see the current pick prominently displayed  
**So that** I know whose turn it is and what's happening

**Acceptance Criteria:**
- Large display shows current round and pick number
- If pick is made, shows player card with avatar and details
- If pick is empty, shows "Select Player" button
- Undo button (X) to remove a pick
- Progress bar shows overall draft progress
- Visual feedback when it's my turn vs waiting
- Auto-advances to next pick after selection

### US-6: Mobile Force Pick
**As a** user on mobile who wants to help move the draft along  
**I want** to force pick for the current on-clock position  
**So that** the draft doesn't stall

**Acceptance Criteria:**
- "Force Pick" button available when not my turn
- Opens same player selection sheet
- Automatically assigns to current on-clock position
- Shows confirmation that pick was forced
- Same force pick logic as desktop (no modal needed)

### US-7: Mobile Real-time Updates
**As a** user on mobile  
**I want** to see picks update in real-time  
**So that** I stay synchronized with other participants

**Acceptance Criteria:**
- WebSocket connection maintained on mobile
- Picks update immediately when made by others
- Carousel updates to show new picks
- Current turn indicator updates
- Reconnection handling if connection drops
- Visual feedback for connection status
- No page refresh needed

### US-8: Mobile Navigation and Exit
**As a** user on mobile  
**I want** easy navigation and the ability to leave the draft  
**So that** I can manage my participation

**Acceptance Criteria:**
- Header shows round/pick info and exit button
- Exit button (X) in top-left corner
- Confirmation before leaving active draft
- Can return to lobby if draft hasn't started
- Breadcrumb navigation works on mobile
- Back button behavior is intuitive

### US-9: Mobile Touch Interactions
**As a** mobile user  
**I want** all interactions optimized for touch  
**So that** the app feels native and responsive

**Acceptance Criteria:**
- All buttons have minimum 44x44px touch targets
- Tap feedback (visual response on touch)
- Swipe gestures work smoothly
- No hover states (use active states instead)
- Prevent accidental double-taps
- Support for iOS safe areas (notch, home indicator)
- Prevent zoom on input focus
- Smooth animations (60fps)

### US-10: Mobile Performance
**As a** mobile user  
**I want** the app to perform well on my device  
**So that** I have a smooth drafting experience

**Acceptance Criteria:**
- Initial load time < 3 seconds on 4G
- Smooth scrolling (no jank)
- Efficient re-renders (React optimization)
- Images lazy load where appropriate
- WebSocket reconnection is fast
- Works on iOS Safari and Chrome Android
- Supports landscape and portrait orientations
- Handles low memory situations gracefully

## Technical Requirements

### TR-1: Component Structure
- Create `MobileLiveDraft.tsx` component (similar to `MobileDraft.tsx`)
- Create `MobileLiveDraftLobby.tsx` component
- Create `mobileLiveDraft.scss` for styling
- Reuse existing WebSocket context and services
- Detect mobile in parent component and route accordingly

### TR-2: WebSocket Integration
- Use existing `WebSocketContext` and `WebSocketService`
- Subscribe to same topics as desktop
- Handle reconnection on mobile network changes
- Maintain connection during screen sleep (where possible)

### TR-3: State Management
- Share state management approach with desktop
- Use same message types and interfaces
- Sync with backend using existing endpoints
- Handle optimistic updates for better UX

### TR-4: Styling Approach
- Use SCSS modules (same as offline mobile)
- Support iOS safe areas with `env(safe-area-inset-*)`
- Use `dvh` units for dynamic viewport height
- Prevent body scroll when modals open
- Use CSS transforms for smooth animations

### TR-5: Browser Support
- iOS Safari 14+
- Chrome Android 90+
- Support touch events
- Handle viewport meta tag properly
- Prevent zoom on input focus

## Out of Scope
- Tablet-specific layouts (use mobile for tablets)
- Offline mode / PWA features
- Push notifications
- Native app wrappers
- Desktop drag-and-drop on mobile (use tap to select)

## Dependencies
- Existing WebSocket infrastructure
- Existing backend endpoints (no changes needed)
- Existing player data and images
- React Router for navigation

## Success Metrics
- Mobile users can complete full draft without issues
- Touch interactions feel responsive (< 100ms feedback)
- No WebSocket disconnections during normal use
- 95%+ of mobile users successfully join and draft
- Page load time < 3s on 4G connection

## Notes
- Follow the same pattern as offline draft's mobile implementation
- Reuse as much logic as possible from desktop components
- Focus on touch-first interactions
- Test on real devices, not just browser dev tools
- Consider network conditions (3G/4G/5G)
