# Mobile Live Draft - Design Document

## Overview

The Mobile Live Draft feature provides a touch-optimized, mobile-responsive interface for participating in real-time fantasy football drafts. This design follows the established pattern from the offline draft mobile implementation (`MobileDraft.tsx`) while integrating WebSocket-based real-time synchronization from the desktop live draft (`LiveDraftBoard.tsx`).

The mobile interface replaces the desktop grid layout with a carousel-based navigation system, full-screen player selection sheets, and touch-first interactions. All functionality from the desktop version is preserved, including real-time pick updates, force picking, undo capabilities, and multi-participant synchronization.

## Architecture

### Component Hierarchy

```
MobileLiveDraft (Main Container)
├── MobileLiveDraftLobby (Pre-draft)
│   ├── Draft Info Display
│   ├── Position Selection Grid
│   ├── Nickname Input
│   ├── Participant List
│   └── Start Draft Button
│
└── MobileLiveDraftBoard (Active Draft)
    ├── Header
    │   ├── Round/Pick Info
    │   ├── Exit Button
    │   └── Undo Last Pick Button
    ├── Progress Bar
    ├── Pick Carousel
    │   └── Pick Cards (all picks)
    ├── Current Pick Display
    │   ├── Selected Player Card
    │   └── Select Player Button
    └── Player Selection Sheet (Modal)
        ├── Position Filters
        ├── Year Filter
        └── Scrollable Player List
```

### Mobile Detection Strategy

Following the offline draft pattern, mobile detection occurs at the parent component level:

```typescript
const isMobile = window.innerWidth <= 768;

// In LiveDraftBoard.tsx or DraftLobbyPage.tsx
{isMobile ? (
  <MobileLiveDraft {...props} />
) : (
  <LiveDraftGrid {...props} />
)}
```

The detection uses a 768px breakpoint and responds to window resize events to dynamically switch between mobile and desktop views.

### State Management

The mobile components share the same state management approach as desktop:

1. **WebSocket Context**: Provides connection state, subscription methods, and message sending
2. **Local Component State**: Manages UI-specific state (sheet visibility, filters, current view)
3. **Draft State**: Received via WebSocket messages, stored in parent component
4. **Optimistic Updates**: UI updates immediately, then confirms via WebSocket response

## Components and Interfaces

### MobileLiveDraftLobby Component

**Purpose**: Pre-draft lobby for joining, configuring position, and waiting for draft start.

**Props Interface**:
```typescript
interface MobileLiveDraftLobbyProps {
  draftUuid: string;
  draftName: string;
  teams: number;
  rounds: number;
  isSnakeDraft: boolean;
  lobbyState: LobbyStateMessage | null;
  userPosition: string | null;
  onJoinLobby: (nickname: string, position: string) => void;
  onStartDraft: () => void;
  onExit: () => void;
}
```

**Key Features**:
- Large touch-friendly position selection buttons (minimum 44x44px)
- Mobile-optimized nickname input with proper keyboard handling
- Scrollable participant list with avatar/nickname display
- Copy lobby link with mobile-friendly feedback (toast/haptic)
- Prominent "Start Draft" button for draft creator
- Real-time participant updates via WebSocket

### MobileLiveDraft Component

**Purpose**: Main mobile draft interface with carousel navigation and player selection.

**Props Interface**:
```typescript
interface MobileLiveDraftProps {
  draftUuid: string;
  draftState: DraftStateMessage;
  userPosition: string;
  participants: ParticipantInfo[];
  onMakePick: (playerId: number, position: string, round: number) => void;
  onForcePick: (playerId: number) => void;
  onUndoLastPick: () => void;
  onExit: () => void;
}
```

**Key Features**:
- Horizontal scrollable carousel showing all picks
- Auto-scroll to current pick on state changes
- Full-screen player selection sheet
- Touch-optimized interactions (swipe, tap, momentum scrolling)
- Real-time pick updates via WebSocket
- Connection status indicator

### Pick Carousel Component

**Purpose**: Horizontal scrollable view of all draft picks.

**Structure**:
```typescript
interface PickCardData {
  roundNumber: number;
  pickInRound: number;
  pickNumber: number;
  position: string;
  pick: PickMessage | null;
  isCurrent: boolean;
}
```

**Behavior**:
- Displays all picks in linear order (round.pick format)
- Current pick highlighted with border/shadow
- Auto-scrolls to center current pick when state changes
- Tapping a pick navigates to that pick
- Smooth momentum scrolling with CSS `scroll-snap-type`
- Shows player avatar, name, position badge for filled picks
- Shows placeholder with pick number for empty picks

### Player Selection Sheet Component

**Purpose**: Full-screen modal for browsing and selecting players.

**State**:
```typescript
interface PlayerSheetState {
  isOpen: boolean;
  positionFilter: 'ALL' | 'QB' | 'RB' | 'WR' | 'TE';
  yearFilter: number | null;
  players: Player[];
  loading: boolean;
  isForcePick: boolean;
}
```

**Features**:
- Slides up from bottom with smooth animation
- Position filter buttons at top (ALL, QB, RB, WR, TE)
- Year filter dropdown/buttons
- Virtualized scrolling for large player lists (performance)
- Each player card shows: avatar, name, position, team, college, ADP
- Tapping player makes pick and closes sheet
- Swipe down or tap X to close without selecting
- Loading spinner while fetching players

## Data Models

### Draft State (from WebSocket)

```typescript
interface DraftStateMessage {
  draftUuid: string;
  currentRound: number;
  currentTurnPosition: string;
  picks: PickMessage[];
  participants: ParticipantInfo[];
  isComplete: boolean;
  totalRounds: number;
  participantCount: number;
  isSnakeDraft: boolean;
}
```

### Pick Message

```typescript
interface PickMessage {
  pickNumber: number;
  roundNumber: number;
  pickInRound: number;
  pickedByPosition: string;
  playerId: number;
  playerName: string;
  position: string;
  team: string;
  college: string;
  forcedByPosition?: string;
}
```

### Participant Info

```typescript
interface ParticipantInfo {
  position: string;
  nickname: string;
  isReady: boolean;
  isCreator: boolean;
}
```

### Player Data

```typescript
interface Player {
  id: number;
  name: string;
  position: 'QB' | 'RB' | 'WR' | 'TE';
  team: string;
  college: string;
  adp: number;
  year: number;
}
```

## WebSocket Integration

### Connection Management

The mobile components use the existing `WebSocketContext` and `WebSocketService`:

```typescript
const { 
  connect, 
  disconnect, 
  subscribeToBroadcast, 
  subscribeToUserQueue, 
  sendMessage,
  connectionState 
} = useWebSocket();
```

### Subscription Topics

**Lobby Phase**:
- `/topic/lobby/{draftUuid}` - Broadcast lobby updates (join/leave/ready)
- `/user/queue/lobby-state` - User-specific lobby state responses
- `/user/queue/errors` - Error messages

**Active Draft Phase**:
- `/topic/draft/{draftUuid}` - Broadcast draft state updates (picks, undo)
- `/user/queue/draft-state` - User-specific draft state responses
- `/user/queue/errors` - Error messages

### Message Sending

**Join Lobby**:
```typescript
sendMessage('/app/lobby/join', {
  draftUuid,
  nickname,
  position
});
```

**Make Pick**:
```typescript
sendMessage('/app/draft/pick', {
  draftUuid,
  playerId,
  position,
  roundNumber
});
```

**Force Pick**:
```typescript
sendMessage('/app/draft/force-pick', {
  draftUuid,
  playerId
});
```

**Undo Last Pick**:
```typescript
sendMessage('/app/draft/undo', {
  draftUuid
});
```

**Start Draft**:
```typescript
sendMessage('/app/lobby/start', {
  draftUuid
});
```

### Reconnection Handling

Mobile networks are less stable than desktop, so reconnection is critical:

1. **Detect Disconnection**: Monitor `connectionState` from WebSocket context
2. **Show Indicator**: Display "Reconnecting..." banner at top of screen
3. **Auto-Reconnect**: WebSocketService handles automatic reconnection
4. **Request Fresh State**: After reconnection, request current draft state
5. **Handle Page Visibility**: Reconnect when app returns to foreground

```typescript
useEffect(() => {
  const handleVisibilityChange = () => {
    if (document.visibilityState === 'visible' && connectionState === ConnectionState.DISCONNECTED) {
      connect(draftUuid);
    }
  };
  
  document.addEventListener('visibilitychange', handleVisibilityChange);
  return () => document.removeEventListener('visibilitychange', handleVisibilityChange);
}, [connectionState, draftUuid, connect]);
```

## User Interface Design

### Touch Interaction Patterns

**Minimum Touch Targets**: All interactive elements are minimum 44x44px (Apple HIG standard)

**Touch Feedback**:
- Scale transform on tap (0.95 scale)
- Background color change on active state
- Haptic feedback where supported (Vibration API)
- No hover states (use active states instead)

**Gesture Support**:
- Horizontal swipe for carousel navigation
- Swipe down to close player sheet
- Momentum scrolling with CSS `scroll-snap`
- Pull-to-refresh disabled (prevent accidental refresh)

### Responsive Layout

**Portrait Mode** (default):
- Header: 60px height
- Progress bar: 40px height
- Carousel: 180px height
- Current pick display: Flexible height
- Player sheet: Full screen overlay

**Landscape Mode**:
- Carousel height reduced to 140px
- Current pick display more compact
- Player sheet adjusted for wider viewport

**Safe Areas** (iOS):
```scss
.mobile-draft-header {
  padding-top: max(12px, env(safe-area-inset-top));
}

.player-sheet {
  padding-bottom: max(20px, env(safe-area-inset-bottom));
}
```

### Animation Specifications

**Player Sheet Slide-Up**:
```scss
@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.player-sheet {
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
```

**Carousel Auto-Scroll**:
```typescript
carouselRef.current.scrollTo({
  left: scrollPosition,
  behavior: 'smooth'
});
```

**Pick Card Update**:
```scss
.carousel-card {
  transition: all 0.2s ease-out;
  
  &.filled {
    animation: fadeIn 0.3s ease-in;
  }
}
```

### Styling Architecture

**File Structure**:
- `mobileLiveDraft.scss` - Main mobile draft styles
- Uses SCSS modules for scoped styling
- Follows BEM naming convention
- Shares color variables with desktop theme

**Key Style Patterns**:
```scss
.mobile-draft {
  height: 100dvh; // Dynamic viewport height (accounts for mobile browser chrome)
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  &-header {
    flex-shrink: 0;
    backdrop-filter: blur(10px);
    background: rgba(255, 255, 255, 0.95);
  }
  
  .draft-carousel {
    overflow-x: auto;
    scroll-snap-type: x mandatory;
    -webkit-overflow-scrolling: touch; // iOS momentum scrolling
    
    &::-webkit-scrollbar {
      display: none; // Hide scrollbar on mobile
    }
  }
  
  .player-sheet {
    position: fixed;
    inset: 0;
    z-index: 1000;
    background: white;
    
    &-overlay {
      background: rgba(0, 0, 0, 0.5);
      backdrop-filter: blur(4px);
    }
  }
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Property Reflection

After analyzing all acceptance criteria, I've identified the following areas where properties can be consolidated:

**Redundancy Analysis**:
1. **Touch Target Sizing**: US-2.2, US-2.5, US-4.5, and US-10.1 all test minimum touch target size - can be combined into one comprehensive property
2. **Carousel Rendering**: US-3.1, US-3.5, and US-3.6 all test carousel card rendering - can be combined into one property about carousel completeness
3. **Pick Display**: US-5.2 and US-4.6 both test player information display - can be combined into one property about player data completeness
4. **Real-time Updates**: US-8.2, US-8.3, and US-8.4 all test WebSocket state updates - can be combined into one property about state synchronization
5. **Screen Size Detection**: US-1.1 and US-1.4 both test mobile detection - can be combined into one property

**Properties to Keep Separate**:
- Position filter functionality (US-4.7) - unique behavior
- Progress calculation (US-5.5) - mathematical property
- Auto-advance logic (US-5.7) - state transition property
- Force pick assignment (US-6.3) - specific business logic
- Undo availability (US-7.1) - conditional rendering property
- Clipboard functionality (US-2.6) - browser API interaction

### Correctness Properties

Property 1: Mobile Detection Threshold
*For any* screen width value, the mobile detection function should return true if and only if the width is less than or equal to 768 pixels.
**Validates: Requirements US-1.1, US-1.4**

Property 2: Touch Target Minimum Size
*For any* interactive element (button, tap target) in the mobile interface, its rendered dimensions should be at least 44x44 pixels to meet accessibility standards.
**Validates: Requirements US-2.2, US-2.5, US-4.5, US-10.1**

Property 3: Clipboard Copy Correctness
*For any* draft UUID, when the "Copy Lobby Link" button is clicked, the clipboard should contain the correctly formatted lobby URL with that UUID.
**Validates: Requirements US-2.6**

Property 4: Carousel Completeness
*For any* draft state with N total picks (teams × rounds), the carousel should render exactly N pick cards, where filled picks display player information and empty picks display pick numbers.
**Validates: Requirements US-3.1, US-3.5, US-3.6**

Property 5: Current Pick Highlighting
*For any* draft state, exactly one pick card in the carousel should have the "current" highlight class, and it should correspond to the pick at (currentRound, currentTurnPosition).
**Validates: Requirements US-3.2**

Property 6: Carousel Navigation
*For any* pick card in the carousel, tapping it should update the component state to display that pick's round and position.
**Validates: Requirements US-3.4**

Property 7: Player Information Completeness
*For any* player object displayed in the player selection sheet or pick card, the rendered output should contain all required fields: avatar/image, name, position badge, team, college, and ADP (when available).
**Validates: Requirements US-4.6, US-5.2**

Property 8: Pick Selection Behavior
*For any* player selected from the player sheet, the onMakePick callback should be invoked with the correct playerId, and the player sheet should close immediately after.
**Validates: Requirements US-4.7**

Property 9: Progress Calculation Accuracy
*For any* draft state, the progress percentage should equal (currentPickNumber / totalPicks) × 100, where currentPickNumber = (currentRound - 1) × teams + currentPickInRound.
**Validates: Requirements US-5.5**

Property 10: Auto-Advance After Pick
*For any* pick made when not at the last pick of the draft, the current pick should automatically advance to the next pick in sequence (accounting for snake draft order).
**Validates: Requirements US-5.7**

Property 11: Force Pick Position Assignment
*For any* force pick action, the pick should be assigned to the currentTurnPosition from the draft state, not the userPosition of the person forcing the pick.
**Validates: Requirements US-6.3**

Property 12: Undo Button Visibility
*For any* draft state, the "Undo Last Pick" button should be visible if and only if the picks array has at least one element.
**Validates: Requirements US-7.1**

Property 13: Repeated Undo Operations
*For any* draft state with N picks, calling undo N times consecutively should result in a draft state with zero picks, with each undo removing the most recent pick.
**Validates: Requirements US-7.4**

Property 14: Undo Permission Universality
*For any* participant in the draft, the undo operation should succeed regardless of which participant made the pick being undone.
**Validates: Requirements US-7.5**

Property 15: Real-time State Synchronization
*For any* WebSocket message received (pick, undo, participant update), the local draft state should update to reflect the message content within one render cycle.
**Validates: Requirements US-8.2, US-8.3, US-8.4**

Property 16: Double-Tap Prevention
*For any* button with double-tap prevention, tapping it twice within 300ms should trigger the action only once, not twice.
**Validates: Requirements US-10.5**

## Error Handling

### WebSocket Errors

**Connection Failures**:
- Display "Connection Lost" banner at top of screen
- Show reconnection status ("Reconnecting..." with spinner)
- Disable pick-making actions while disconnected
- Auto-retry connection with exponential backoff
- Request fresh draft state after successful reconnection

**Message Errors**:
- Subscribe to `/user/queue/errors` for server-side errors
- Display error messages in toast notifications
- Log errors to console for debugging
- Provide retry options for failed actions

### Pick Validation Errors

**Invalid Pick Attempts**:
- "Not your turn" - Show message, prevent pick
- "Player already picked" - Show message, refresh player pool
- "Invalid position" - Show message, prevent pick
- "Draft complete" - Disable pick actions, show completion message

**Network Errors**:
- Timeout on pick submission (5 seconds)
- Show "Pick failed, please try again" message
- Maintain optimistic UI update, revert on error
- Provide manual retry button

### UI Error States

**Player Loading Failures**:
- Show "Failed to load players" message
- Provide "Retry" button
- Fall back to cached player data if available
- Log error details for debugging

**Image Loading Failures**:
- Use position badge as fallback for player avatars
- Track failed image loads in state
- Don't retry failed images (avoid infinite loops)
- Provide consistent fallback UI

### Mobile-Specific Error Handling

**Network Switching** (WiFi ↔ Cellular):
- Detect network change events
- Reconnect WebSocket automatically
- Show brief "Reconnecting" indicator
- Don't interrupt user actions in progress

**Low Memory**:
- Implement component lazy loading
- Use React.memo to prevent unnecessary re-renders
- Virtualize long player lists
- Clear image caches when memory pressure detected

**Orientation Changes**:
- Preserve scroll position in carousel
- Maintain player sheet state
- Recalculate layout dimensions
- Don't reset user input

## Testing Strategy

### Dual Testing Approach

This feature requires both unit tests and property-based tests for comprehensive coverage:

**Unit Tests** focus on:
- Specific UI examples (lobby renders with correct elements)
- Edge cases (empty draft, single participant, draft completion)
- Error conditions (connection failures, invalid picks)
- Integration points (WebSocket message handling, navigation)
- Mobile-specific behaviors (safe area support, orientation changes)

**Property-Based Tests** focus on:
- Universal properties across all inputs (touch target sizes, progress calculation)
- State transitions (pick advancement, undo operations)
- Data transformations (carousel rendering, player filtering)
- Invariants (current pick uniqueness, pick count consistency)

### Property-Based Testing Configuration

**Library Selection**: Use `fast-check` for TypeScript/JavaScript property-based testing

**Test Configuration**:
```typescript
import fc from 'fast-check';

// Minimum 100 iterations per property test
fc.assert(
  fc.property(/* generators */, (/* inputs */) => {
    // Property assertion
  }),
  { numRuns: 100 }
);
```

**Property Test Tags**:
Each property test must include a comment referencing the design document:
```typescript
// Feature: mobile-live-draft, Property 1: Mobile Detection Threshold
test('mobile detection returns true for widths <= 768px', () => {
  fc.assert(
    fc.property(fc.integer({ min: 1, max: 2000 }), (width) => {
      const isMobile = detectMobile(width);
      return width <= 768 ? isMobile === true : isMobile === false;
    }),
    { numRuns: 100 }
  );
});
```

### Test Data Generators

**Draft State Generator**:
```typescript
const draftStateArbitrary = fc.record({
  draftUuid: fc.uuid(),
  currentRound: fc.integer({ min: 1, max: 10 }),
  currentTurnPosition: fc.constantFrom('A', 'B', 'C', 'D'),
  picks: fc.array(pickMessageArbitrary),
  participants: fc.array(participantInfoArbitrary, { minLength: 2, maxLength: 12 }),
  isComplete: fc.boolean(),
  totalRounds: fc.integer({ min: 1, max: 20 }),
  participantCount: fc.integer({ min: 2, max: 12 }),
  isSnakeDraft: fc.boolean()
});
```

**Player Generator**:
```typescript
const playerArbitrary = fc.record({
  id: fc.integer({ min: 1, max: 10000 }),
  name: fc.string({ minLength: 3, maxLength: 30 }),
  position: fc.constantFrom('QB', 'RB', 'WR', 'TE'),
  team: fc.string({ minLength: 2, maxLength: 3 }),
  college: fc.string({ minLength: 3, maxLength: 40 }),
  adp: fc.float({ min: 1, max: 300 }),
  year: fc.integer({ min: 2020, max: 2024 })
});
```

**Screen Width Generator**:
```typescript
const screenWidthArbitrary = fc.integer({ min: 320, max: 2560 });
```

### Unit Test Coverage

**Component Rendering Tests**:
- MobileLiveDraftLobby renders with all required elements
- MobileLiveDraft renders header, carousel, and current pick display
- Player selection sheet renders with filters and player list
- Empty states render correctly (no picks, no players)

**Interaction Tests**:
- Clicking "Select Player" opens player sheet
- Tapping player in sheet makes pick and closes sheet
- Tapping carousel card navigates to that pick
- Swipe down closes player sheet without selecting
- Exit button shows confirmation dialog

**WebSocket Integration Tests**:
- Component subscribes to correct topics on mount
- Pick messages update local state
- Undo messages remove picks from state
- Reconnection requests fresh state
- Error messages display to user

**Mobile-Specific Tests**:
- Safe area insets applied to header and footer
- Touch targets meet 44x44px minimum
- Viewport meta tag prevents zoom on input focus
- Orientation changes preserve state
- Network change triggers reconnection

### Performance Testing

**Metrics to Monitor**:
- Initial component mount time (< 100ms)
- Carousel scroll performance (60fps)
- Player sheet open/close animation (smooth 60fps)
- Re-render time after pick (< 50ms)
- WebSocket message processing time (< 10ms)

**Performance Tests**:
- Render carousel with 168 picks (12 teams × 14 rounds)
- Render player sheet with 500+ players
- Handle rapid pick updates (10 picks in 1 second)
- Measure memory usage over full draft
- Test on throttled CPU (4x slowdown)

### Browser and Device Testing

**Required Test Environments**:
- iOS Safari 14+ (iPhone SE, iPhone 13, iPhone 14 Pro)
- Chrome Android 90+ (Pixel 5, Samsung Galaxy S21)
- Tablet (iPad 9th gen, Samsung Galaxy Tab)
- Network throttling (Fast 3G, Slow 3G, Offline)

**Manual Test Scenarios**:
- Complete full draft on mobile device
- Test with multiple participants (2-12 teams)
- Force pick from non-turn position
- Undo multiple picks in sequence
- Disconnect WiFi mid-draft, reconnect
- Switch between portrait and landscape
- Background app, return to foreground
- Receive pick while player sheet is open

## Implementation Notes

### Code Reuse Strategy

**From MobileDraft.tsx** (Offline Draft):
- Carousel structure and styling
- Player selection sheet layout
- Touch interaction patterns
- Progress bar component
- Mobile header structure
- Safe area handling

**From LiveDraftBoard.tsx** (Desktop Live Draft):
- WebSocket subscription logic
- Draft state management
- Pick/undo/force pick message handling
- Participant tracking
- Snake draft order calculation

**New Mobile-Specific Code**:
- Mobile lobby component
- Touch gesture handlers
- Reconnection UI indicators
- Mobile-optimized animations
- Responsive layout adjustments

### Performance Optimizations

**React Optimizations**:
```typescript
// Memoize expensive components
const PickCard = React.memo(({ pick, isCurrent }) => {
  // Render logic
});

// Memoize callbacks
const handlePickPlayer = useCallback((player: Player) => {
  onMakePick(player.id, userPosition, currentRound);
}, [onMakePick, userPosition, currentRound]);

// Memoize computed values
const filteredPlayers = useMemo(() => {
  return playerPool.filter(p => 
    positionFilter === 'ALL' || p.position === positionFilter
  );
}, [playerPool, positionFilter]);
```

**Virtualization**:
For player lists with 500+ players, use `react-window` or `react-virtualized`:
```typescript
import { FixedSizeList } from 'react-window';

<FixedSizeList
  height={600}
  itemCount={filteredPlayers.length}
  itemSize={80}
  width="100%"
>
  {({ index, style }) => (
    <PlayerCard 
      player={filteredPlayers[index]} 
      style={style}
      onClick={handlePickPlayer}
    />
  )}
</FixedSizeList>
```

**Image Optimization**:
```typescript
// Lazy load images
<img 
  src={`/api/players/manage/${playerId}/headshot`}
  loading="lazy"
  onError={handleImageError}
/>

// Track failed images to avoid retries
const [failedImages, setFailedImages] = useState<Set<number>>(new Set());
```

### Accessibility Considerations

**Touch Targets**:
- Minimum 44x44px for all interactive elements
- Adequate spacing between adjacent buttons (8px minimum)
- Large tap areas for primary actions (Select Player, Start Draft)

**Visual Feedback**:
- Active states for all buttons (scale transform, color change)
- Loading indicators for async operations
- Success/error feedback for actions
- Connection status always visible

**Keyboard Support** (for accessibility tools):
- Tab navigation through interactive elements
- Enter/Space to activate buttons
- Escape to close modals
- Focus management when opening/closing sheets

**Screen Reader Support**:
- Semantic HTML elements (button, nav, main)
- ARIA labels for icon-only buttons
- ARIA live regions for dynamic updates
- Alt text for player images

### Browser Compatibility

**CSS Features**:
- `dvh` units for dynamic viewport height (fallback to `vh`)
- `env(safe-area-inset-*)` for iOS safe areas (fallback to fixed padding)
- `scroll-snap-type` for carousel (progressive enhancement)
- `backdrop-filter` for blur effects (fallback to solid background)

**JavaScript Features**:
- Clipboard API (fallback to manual copy instructions)
- Vibration API (optional, no fallback needed)
- IntersectionObserver for lazy loading (polyfill if needed)
- ResizeObserver for responsive behavior (polyfill if needed)

**Polyfills**:
```typescript
// Add to package.json if supporting older browsers
"core-js": "^3.x",
"resize-observer-polyfill": "^1.x"
```

### Security Considerations

**WebSocket Security**:
- Use WSS (WebSocket Secure) in production
- Validate all incoming messages
- Sanitize user input (nicknames, positions)
- Rate limit pick actions to prevent spam

**XSS Prevention**:
- Sanitize player names and user nicknames
- Use React's built-in XSS protection (JSX escaping)
- Validate URLs before clipboard copy
- Don't use `dangerouslySetInnerHTML`

**Data Validation**:
- Validate draft state structure from WebSocket
- Check pick numbers are in valid range
- Verify positions match participant list
- Validate player IDs before making picks

## Future Enhancements

**Out of Scope for Initial Release**:
- Push notifications for picks when app is backgrounded
- Offline mode / PWA capabilities
- Native app wrappers (React Native)
- Tablet-specific layouts (use mobile for now)
- Draft chat functionality
- Pick timer with countdown
- Trade functionality during draft
- Keeper/auction draft support

**Potential Future Features**:
- Haptic feedback for picks (Vibration API)
- Dark mode support
- Customizable carousel card sizes
- Player notes/rankings integration
- Draft analytics and insights
- Export draft results to various formats
- Social sharing of draft results

## Appendix: Key Algorithms

### Snake Draft Order Calculation

```typescript
function getPositionForPick(
  round: number, 
  pickInRound: number, 
  participantCount: number,
  isSnakeDraft: boolean
): string {
  if (isSnakeDraft) {
    // Odd rounds: forward (A, B, C, D)
    // Even rounds: reverse (D, C, B, A)
    const isReverse = round % 2 === 0;
    const index = isReverse 
      ? participantCount - pickInRound 
      : pickInRound - 1;
    return String.fromCharCode(65 + index); // A, B, C, D...
  } else {
    // Linear draft: all rounds forward
    const index = pickInRound - 1;
    return String.fromCharCode(65 + index);
  }
}
```

### Carousel Auto-Scroll Calculation

```typescript
function scrollToCurrentPick(
  carouselRef: RefObject<HTMLDivElement>,
  currentPickNumber: number,
  cardWidth: number,
  gap: number
) {
  if (!carouselRef.current) return;
  
  const currentPickIndex = currentPickNumber - 1;
  const cardTotalWidth = cardWidth + gap;
  const viewportWidth = window.innerWidth;
  
  // Center the current pick in viewport
  const scrollPosition = 
    currentPickIndex * cardTotalWidth 
    - (viewportWidth / 2) 
    + (cardWidth / 2);
  
  carouselRef.current.scrollTo({
    left: Math.max(0, scrollPosition),
    behavior: 'smooth'
  });
}
```

### Progress Calculation

```typescript
function calculateProgress(
  currentRound: number,
  currentPickInRound: number,
  totalRounds: number,
  participantCount: number
): number {
  const currentPickNumber = 
    (currentRound - 1) * participantCount + currentPickInRound;
  const totalPicks = totalRounds * participantCount;
  return (currentPickNumber / totalPicks) * 100;
}
```

### Next Pick Advancement

```typescript
function advanceToNextPick(
  currentRound: number,
  currentPick: number,
  totalRounds: number,
  participantCount: number
): { round: number; pick: number } {
  if (currentPick < participantCount) {
    // Same round, next pick
    return { round: currentRound, pick: currentPick + 1 };
  } else if (currentRound < totalRounds) {
    // Next round, first pick
    return { round: currentRound + 1, pick: 1 };
  } else {
    // Draft complete, stay at last pick
    return { round: currentRound, pick: currentPick };
  }
}
```

---

## Summary

This design document provides a comprehensive blueprint for implementing the Mobile Live Draft feature. The architecture follows established patterns from the offline mobile draft while integrating real-time WebSocket communication from the desktop live draft. The design prioritizes touch-first interactions, mobile performance, and network resilience.

Key design decisions:
- **Carousel-based navigation** instead of grid layout for mobile
- **Full-screen player selection sheet** for optimal touch interaction
- **Reuse of existing WebSocket infrastructure** for real-time updates
- **Property-based testing** for universal correctness guarantees
- **Progressive enhancement** for browser compatibility
- **Comprehensive error handling** for mobile network conditions

The correctness properties defined in this document provide a formal specification for the feature's behavior, enabling property-based testing to verify universal properties across all possible inputs. Combined with targeted unit tests for specific scenarios, this testing strategy ensures robust, reliable functionality across diverse mobile devices and network conditions.
