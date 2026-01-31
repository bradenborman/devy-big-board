# Mobile Live Draft - Implementation Tasks

## Phase 1: Foundation & Mobile Detection

### 1.1 Set up mobile detection infrastructure
- [ ] Add mobile detection hook/utility (reuse from offline draft pattern)
- [ ] Update routing logic to detect mobile for live draft pages
- [ ] Add viewport meta tag configuration if not present
- [ ] Test mobile detection on various screen sizes

### 1.2 Create mobile component structure
- [ ] Create `app-client/src/components/mobile/MobileLiveDraft.tsx`
- [ ] Create `app-client/src/components/mobile/MobileLiveDraftLobby.tsx`
- [ ] Create `app-client/src/components/mobile/mobileLiveDraft.scss`
- [ ] Set up basic component shells with TypeScript interfaces

### 1.3 Update parent components for mobile routing
- [ ] Update `LiveDraftBoard.tsx` to detect mobile and render mobile component
- [ ] Update `DraftLobbyPage.tsx` to detect mobile and render mobile lobby
- [ ] Ensure WebSocket context is passed to mobile components
- [ ] Test component switching on resize

## Phase 2: Mobile Lobby Implementation

### 2.1 Build mobile lobby UI
- [ ] Create mobile-optimized header with logo and back button
- [ ] Implement draft info display (name, teams, rounds)
- [ ] Create large touch-friendly position selection grid
- [ ] Style nickname input for mobile keyboards
- [ ] Add "Copy Lobby Link" button with mobile-friendly feedback

### 2.2 Implement lobby functionality
- [ ] Connect to WebSocket for lobby updates
- [ ] Handle join lobby flow with nickname and position
- [ ] Display participant list with mobile-optimized cards
- [ ] Implement "Start Draft" button for creator
- [ ] Add loading states and error handling
- [ ] Test lobby join/leave on mobile

### 2.3 Mobile lobby styling
- [ ] Apply gradient background matching desktop theme
- [ ] Style position buttons with active/disabled states
- [ ] Add touch feedback animations
- [ ] Support iOS safe areas (notch, home indicator)
- [ ] Test on various mobile screen sizes

## Phase 3: Mobile Draft Board - Core UI

### 3.1 Build draft header and progress
- [ ] Create mobile header with round/pick info and exit button
- [ ] Implement progress bar showing draft completion
- [ ] Add connection status indicator
- [ ] Style header with backdrop blur effect
- [ ] Test header on different screen sizes

### 3.2 Implement pick carousel
- [ ] Create horizontal scrollable carousel for all picks
- [ ] Display pick cards with round.pick format
- [ ] Show player avatar, name, position for filled picks
- [ ] Show placeholder for empty picks
- [ ] Highlight current pick with border/shadow
- [ ] Implement auto-scroll to current pick
- [ ] Add swipe gesture support with momentum scrolling
- [ ] Test carousel performance with full draft (12 teams x 7 rounds)

### 3.3 Build current pick display
- [ ] Create large current pick card area
- [ ] Show "Select Player" button when pick is empty
- [ ] Display selected player with avatar and details when filled
- [ ] Add undo button (X) to remove pick
- [ ] Implement visual distinction for "my turn" vs "waiting"
- [ ] Test pick display updates

## Phase 4: Mobile Player Selection

### 4.1 Build player selection sheet
- [ ] Create full-screen modal sheet that slides up from bottom
- [ ] Add sheet header with title and close button
- [ ] Implement position filter buttons (ALL, QB, RB, WR, TE)
- [ ] Add year filter dropdown/buttons
- [ ] Create scrollable player list with large touch targets
- [ ] Style player cards with avatar, name, position, team, college, ADP

### 4.2 Implement player selection logic
- [ ] Connect to draft state for available players
- [ ] Filter players by position and year
- [ ] Handle player tap to make pick
- [ ] Close sheet after selection
- [ ] Add swipe-down gesture to close sheet
- [ ] Show loading state while fetching players
- [ ] Test player selection flow

### 4.3 Player sheet styling
- [ ] Style sheet with smooth slide-up animation
- [ ] Add backdrop blur overlay
- [ ] Style filter buttons with active states
- [ ] Optimize player card layout for mobile
- [ ] Add touch feedback on player tap
- [ ] Support iOS safe areas in sheet

## Phase 5: WebSocket Integration

### 5.1 Connect mobile components to WebSocket
- [ ] Integrate `useWebSocket` hook in mobile components
- [ ] Subscribe to draft state updates
- [ ] Subscribe to lobby state updates
- [ ] Handle pick messages and update UI
- [ ] Handle participant join/leave messages
- [ ] Test real-time updates on mobile

### 5.2 Implement reconnection handling
- [ ] Detect connection loss on mobile
- [ ] Show reconnection indicator
- [ ] Auto-reconnect on network restore
- [ ] Request fresh state after reconnection
- [ ] Handle page visibility changes (app backgrounding)
- [ ] Test reconnection scenarios

### 5.3 Optimize WebSocket for mobile
- [ ] Handle mobile network switches (WiFi ↔ cellular)
- [ ] Implement heartbeat/keepalive if needed
- [ ] Test connection stability on mobile networks
- [ ] Add error handling for connection failures

## Phase 6: Pick Making & Force Pick

### 6.1 Implement regular pick flow
- [ ] Handle "Select Player" button tap
- [ ] Open player selection sheet
- [ ] Send pick message via WebSocket
- [ ] Update local state optimistically
- [ ] Close sheet and advance to next pick
- [ ] Show success feedback
- [ ] Test pick flow end-to-end

### 6.2 Implement force pick
- [ ] Add "Force Pick" button when not user's turn
- [ ] Open player selection sheet for force pick
- [ ] Send force pick message to current on-clock position
- [ ] Update UI after force pick
- [ ] Show who forced the pick
- [ ] Test force pick functionality

### 6.3 Handle pick validation
- [ ] Validate it's user's turn before allowing pick
- [ ] Prevent duplicate picks
- [ ] Handle pick errors from backend
- [ ] Show error messages to user
- [ ] Test error scenarios

## Phase 7: Touch Interactions & Animations

### 7.1 Implement touch feedback
- [ ] Add active states to all buttons (scale/color change)
- [ ] Implement tap feedback with CSS transitions
- [ ] Add haptic feedback where appropriate (if supported)
- [ ] Prevent double-tap zoom on buttons
- [ ] Test touch responsiveness

### 7.2 Add animations
- [ ] Animate sheet slide-up/down
- [ ] Animate carousel scroll
- [ ] Animate pick card updates
- [ ] Add fade-in for new picks
- [ ] Smooth transitions between states
- [ ] Ensure 60fps performance

### 7.3 Optimize for touch
- [ ] Ensure all touch targets are ≥ 44x44px
- [ ] Add padding around interactive elements
- [ ] Prevent accidental taps
- [ ] Test on real devices with fingers (not mouse)

## Phase 8: Navigation & Exit

### 8.1 Implement navigation
- [ ] Add exit button in header
- [ ] Confirm before leaving active draft
- [ ] Handle back button behavior
- [ ] Navigate back to lobby if draft not started
- [ ] Navigate to live draft landing page
- [ ] Test navigation flows

### 8.2 Handle draft completion
- [ ] Detect when draft is complete
- [ ] Show completion message
- [ ] Provide option to view results
- [ ] Allow export on mobile (if applicable)
- [ ] Test completion flow

## Phase 9: Responsive Design & Polish

### 9.1 Support different orientations
- [ ] Test portrait mode layout
- [ ] Test landscape mode layout
- [ ] Adjust carousel for landscape
- [ ] Ensure player sheet works in both orientations
- [ ] Test orientation changes during draft

### 9.2 Support iOS safe areas
- [ ] Add `env(safe-area-inset-top)` to header
- [ ] Add `env(safe-area-inset-bottom)` to footer/buttons
- [ ] Test on iPhone with notch
- [ ] Test on iPhone with home indicator
- [ ] Ensure content doesn't hide behind system UI

### 9.3 Optimize for different screen sizes
- [ ] Test on small phones (iPhone SE, 320px width)
- [ ] Test on large phones (iPhone Pro Max, 428px width)
- [ ] Test on tablets (iPad, 768px width)
- [ ] Adjust font sizes and spacing as needed
- [ ] Test on Android devices

## Phase 10: Performance & Testing

### 10.1 Optimize performance
- [ ] Implement React.memo for expensive components
- [ ] Optimize re-renders with useMemo/useCallback
- [ ] Lazy load player images
- [ ] Test with Chrome DevTools Performance tab
- [ ] Ensure smooth 60fps scrolling
- [ ] Test on mid-range Android device

### 10.2 Test on real devices
- [ ] Test on iPhone (iOS Safari)
- [ ] Test on Android (Chrome)
- [ ] Test on tablet
- [ ] Test with slow 3G network throttling
- [ ] Test with airplane mode (reconnection)
- [ ] Test with multiple participants

### 10.3 Cross-browser testing
- [ ] Test on iOS Safari 14+
- [ ] Test on Chrome Android 90+
- [ ] Test on Samsung Internet
- [ ] Fix any browser-specific issues
- [ ] Verify WebSocket works on all browsers

## Phase 11: Error Handling & Edge Cases

### 11.1 Handle errors gracefully
- [ ] Show error messages for failed picks
- [ ] Handle WebSocket connection errors
- [ ] Handle invalid draft state
- [ ] Show loading states appropriately
- [ ] Provide retry options for failures
- [ ] Test error scenarios

### 11.2 Handle edge cases
- [ ] Handle draft starting while in lobby
- [ ] Handle being kicked from draft
- [ ] Handle draft completion
- [ ] Handle network loss during pick
- [ ] Handle rapid picks from multiple users
- [ ] Test edge cases thoroughly

## Phase 12: Documentation & Cleanup

### 12.1 Code cleanup
- [ ] Remove console.logs
- [ ] Add TypeScript types for all props
- [ ] Add comments for complex logic
- [ ] Extract reusable components
- [ ] Refactor duplicated code
- [ ] Run linter and fix issues

### 12.2 Documentation
- [ ] Document mobile component structure
- [ ] Add README for mobile implementation
- [ ] Document touch interaction patterns
- [ ] Add comments for WebSocket integration
- [ ] Document known limitations

### 12.3 Final testing
- [ ] Complete end-to-end test on mobile
- [ ] Test full draft with multiple mobile users
- [ ] Verify all acceptance criteria met
- [ ] Get user feedback
- [ ] Fix any remaining issues

## Notes
- Follow the pattern from `MobileDraft.tsx` for offline drafts
- Reuse WebSocket infrastructure (no backend changes needed)
- Test frequently on real devices, not just browser dev tools
- Prioritize touch interactions over mouse/hover states
- Keep mobile bundle size small (code splitting if needed)
