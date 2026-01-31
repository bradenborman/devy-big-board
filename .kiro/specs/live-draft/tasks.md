# Implementation Plan: Live Draft with WebSockets

## Overview

This implementation plan adds real-time collaborative draft functionality using WebSockets. Users create a draft lobby with configurable settings, participants join by selecting a position and nickname, everyone readies up, and the creator starts the draft. During the draft, users make picks in snake draft order, see live updates as other participants make selections, and can force-pick for any position. The system supports page reloads without losing state and maintains the same look and feel as the offline draft board.

## Key Features

- **Draft Lobby System**: Creator sets up draft with name, participant count, and rounds
- **Join & Ready Flow**: Participants join lobby, select position/nickname, and ready up
- **Creator Controls**: Only creator can start draft, only when all participants are ready
- **WebSocket Communication**: Real-time updates to all participants in the same draft
- **Position-Based Participation**: Each participant has a position (A-Z) and nickname
- **Draft State Persistence**: Reloading the page doesn't kick users out (auto-rejoin)
- **Snake Draft Order**: Turn-based picking with alternating direction each round
- **Forced Picks**: Any participant can force-pick for any position with attribution
- **Team Ownership UI**: Visual indicators showing "My Team (Nickname)" for the user's column
- **Identical Layout**: Same drag-and-drop interface as offline draft

## Tasks

- [ ] 1. Backend WebSocket infrastructure
  - [x] 1.1 Add WebSocket dependencies to build.gradle
    - Add Spring WebSocket starter dependency
    - Add Spring Messaging dependency
    - Add STOMP protocol support
    - _Requirements: Real-time communication_
  
  - [x] 1.2 Create WebSocket configuration
    - Configure STOMP endpoint at `/ws`
    - Enable SockJS fallback for older browsers
    - Configure message broker for `/topic` destinations
    - Set application destination prefix to `/app`
    - Configure CORS for WebSocket connections
    - _Requirements: WebSocket setup_
  
  - [x] 1.3 Update Draft entity for live draft support
    - Add fields: createdBy, startedAt, currentRound, currentPick, totalRounds
    - Update status to support: LOBBY, IN_PROGRESS, COMPLETED
    - Add relationship: OneToMany with DraftParticipant
    - Note: Reusing existing drafts table with new columns
    - _Requirements: Draft state persistence_
  
  - [x] 1.4 Create DraftParticipant entity
    - Add fields: id, draft (ManyToOne), position (A-Z letter)
    - Add fields: nickname, isReady (boolean), joinedAt
    - Add unique constraint on (draft_id, position)
    - Add unique constraint on (draft_id, nickname)
    - Maps to new draft_participants table
    - _Requirements: Participant tracking and ready status_
  
  - [x] 1.5 Update DraftPick entity for live draft support
    - Add fields: position (A-Z letter), forcedBy (nullable), roundNumber
    - Note: Reusing existing draft_picks table with new columns
    - _Requirements: Pick tracking with forced pick attribution_
  
  - [x] 1.6 Create DraftParticipantRepository
    - Add method: findByDraftId(Long draftId)
    - Add method: findByDraftIdAndPosition(Long draftId, String position)
    - Add method: countByDraftIdAndIsReady(Long draftId, boolean isReady)
    - Extend JpaRepository<DraftParticipant, Long>
    - _Requirements: Participant management_

- [ ] 2. Draft lobby and participant management service
  - [x] 2.1 Enhance DraftService for live draft support
    - Implement createLiveDraft(draftName, creatorNickname, participantCount, totalRounds)
    - Implement getLobbyState(uuid) - returns draft + all participants
    - Implement canStartDraft(uuid) - validates all participants ready
    - Implement startDraft(uuid) - sets status to IN_PROGRESS
    - Implement getCurrentTurn(uuid) - returns position letter whose turn it is
    - Implement isValidPick(uuid, position) - validates if position can pick now
    - Note: Extends existing DraftService, reuses Draft entity
    - _Requirements: Draft lifecycle management_
  
  - [x] 2.2 Create ParticipantService
    - Implement joinDraft(draftId, nickname, position) - adds participant to lobby
    - Implement setReady(draftId, position, isReady) - toggles ready status
    - Implement leaveDraft(draftId, position) - removes participant from lobby
    - Implement getParticipants(draftId) - returns all participants
    - Implement isPositionAvailable(draftId, position) - checks if position is taken
    - Implement isNicknameAvailable(draftId, nickname) - checks if nickname is taken
    - _Requirements: Participant management_
  
  - [x] 2.3 Implement pick validation logic
    - Calculate current pick position based on snake draft order
    - Round 1: A→B→C→D, Round 2: D→C→B→A, Round 3: A→B→C→D, etc.
    - Validate pick is for current turn position
    - Allow forced picks from any position
    - _Requirements: Turn order validation_
  
  - [x] 2.4 Enhance makePick method for live drafts
    - Validate draft status is IN_PROGRESS
    - Validate player is available (not already picked)
    - Create DraftPick record with position and roundNumber
    - Increment currentPick counter
    - Update currentRound if round completes
    - Set status to COMPLETED if all picks made
    - Return updated draft state
    - Note: Reuses existing DraftPick entity with new fields
    - _Requirements: Pick execution_
  
  - [x] 2.5 Implement forcePick method
    - Accept forcingPosition parameter (who is forcing the pick)
    - Create DraftPick with forcedBy field set
    - Same logic as makePick but bypass turn validation
    - _Requirements: Forced pick functionality_
  
  - [x] 2.6 Implement getDraftState method
    - Return complete draft state: all picks, current turn, available players
    - Include participant list with nicknames and positions
    - Include pick history with forced pick attributions
    - _Requirements: State synchronization_

- [ ] 3. WebSocket message models
  - [x] 3.1 Create LobbyStateMessage
    - Add fields: draftUuid, draftName, status, participantCount, totalRounds
    - Add field: participants (List of ParticipantInfo)
    - Add field: allReady (boolean - true if all participants ready)
    - Add field: canStart (boolean - true if draft can start)
    - _Requirements: Lobby state broadcast_
  
  - [x] 3.2 Create ParticipantInfo
    - Add fields: position (A-Z), nickname, isReady, joinedAt
    - _Requirements: Participant data_
  
  - [x] 3.3 Create DraftStateMessage
    - Add fields: draftUuid, status, currentRound, currentPick
    - Add field: currentTurnPosition (A-Z letter)
    - Add field: participants (List of ParticipantInfo)
    - Add field: picks (List of PickMessage)
    - Add field: availablePlayers (List of PlayerMessage)
    - _Requirements: State broadcast_
  
  - [x] 3.4 Create PickMessage
    - Add fields: playerId, playerName, position, team, college
    - Add fields: roundNumber, pickNumber, pickedByPosition
    - Add field: forcedByPosition (nullable)
    - Add field: pickedAt timestamp
    - _Requirements: Pick notification_
  
  - [x] 3.5 Create JoinRequest
    - Add fields: draftUuid, nickname, position
    - Add validation annotations (@NotBlank, @Pattern for position)
    - _Requirements: Join lobby request_
  
  - [x] 3.6 Create ReadyRequest
    - Add fields: draftUuid, position, isReady
    - Add validation annotations
    - _Requirements: Ready toggle request_
  
  - [x] 3.7 Create MakePickRequest
    - Add fields: draftUuid, playerId, position
    - Add validation annotations
    - _Requirements: Pick request from client_
  
  - [x] 3.8 Create ForcePickRequest
    - Add fields: draftUuid, playerId, targetPosition, forcingPosition
    - Add validation annotations
    - _Requirements: Forced pick request_
  
  - [x] 3.9 Create ErrorMessage
    - Add fields: message, code, timestamp
    - _Requirements: Error communication_
  
  - [x] 3.10 Create ParticipantJoinedMessage
    - Add fields: draftUuid, participant (ParticipantInfo)
    - Add field: message (e.g., "Alice joined as Position A")
    - _Requirements: Join notification_
  
  - [x] 3.11 Create ParticipantLeftMessage
    - Add fields: draftUuid, position, nickname
    - Add field: message (e.g., "Bob left the lobby")
    - _Requirements: Leave notification_
  
  - [x] 3.12 Create DraftStartedMessage
    - Add fields: draftUuid, startedAt, firstTurnPosition
    - Add field: message ("Draft has started!")
    - _Requirements: Start notification_

- [ ] 4. WebSocket controller
  - [x] 4.1 Create LiveDraftController (WebSocket)
    - Annotate with @Controller
    - Inject LiveDraftService, ParticipantService, and SimpMessagingTemplate
    - _Requirements: WebSocket message handling_
  
  - [x] 4.2 Implement joinLobby endpoint
    - Map to @MessageMapping("/draft/{draftUuid}/join")
    - Validate nickname and position availability
    - Call participantService.joinDraft()
    - Broadcast ParticipantJoinedMessage to /topic/draft/{draftUuid}/lobby
    - Broadcast updated LobbyStateMessage to /topic/draft/{draftUuid}/lobby
    - Send error to user if validation fails
    - _Requirements: Join lobby_
  
  - [x] 4.3 Implement toggleReady endpoint
    - Map to @MessageMapping("/draft/{draftUuid}/ready")
    - Call participantService.setReady()
    - Broadcast updated LobbyStateMessage to /topic/draft/{draftUuid}/lobby
    - _Requirements: Ready toggle_
  
  - [x] 4.4 Implement leaveLobby endpoint
    - Map to @MessageMapping("/draft/{draftUuid}/leave")
    - Call participantService.leaveDraft()
    - Broadcast ParticipantLeftMessage to /topic/draft/{draftUuid}/lobby
    - Broadcast updated LobbyStateMessage to /topic/draft/{draftUuid}/lobby
    - _Requirements: Leave lobby_
  
  - [x] 4.5 Implement startDraft endpoint
    - Map to @MessageMapping("/draft/{draftUuid}/start")
    - Validate all participants are ready
    - Validate requester is the creator
    - Call liveDraftService.startDraft()
    - Broadcast DraftStartedMessage to /topic/draft/{draftUuid}/lobby
    - Broadcast initial DraftStateMessage to /topic/draft/{draftUuid}
    - _Requirements: Start draft_
  
  - [x] 4.6 Implement makePick endpoint
    - Map to @MessageMapping("/draft/{draftUuid}/pick")
    - Validate pick request
    - Call liveDraftService.makePick()
    - Broadcast updated DraftStateMessage to /topic/draft/{draftUuid}
    - Send error to user if validation fails
    - _Requirements: Pick handling_
  
  - [x] 4.7 Implement forcePick endpoint
    - Map to @MessageMapping("/draft/{draftUuid}/force-pick")
    - Validate force pick request
    - Call liveDraftService.forcePick()
    - Broadcast updated DraftStateMessage to /topic/draft/{draftUuid}
    - _Requirements: Forced pick handling_
  
  - [x] 4.8 Implement getDraftState endpoint
    - Map to @MessageMapping("/draft/{draftUuid}/state")
    - Return complete current draft state
    - _Requirements: State refresh on reconnect_
  
  - [x] 4.9 Implement getLobbyState endpoint
    - Map to @MessageMapping("/draft/{draftUuid}/lobby/state")
    - Return complete lobby state with participants
    - _Requirements: Lobby state refresh_

- [ ] 5. REST API endpoints for draft management
  - [x] 5.1 Enhance DraftController for live drafts
    - POST /api/live-drafts - create new live draft
    - GET /api/drafts/{uuid}/lobby - get lobby state (REST fallback)
    - Note: Reuses existing DraftController, adds live draft endpoints
    - _Requirements: Draft setup via REST_
  
  - [x] 5.2 Implement create live draft endpoint
    - Accept draftName, creatorNickname, participantCount, totalRounds
    - Generate UUID
    - Create Draft with status LOBBY
    - Return draft details with lobby URL
    - Format: /draft/{uuid}/lobby
    - _Requirements: Draft creation_
  
  - [x] 5.3 Implement get lobby state endpoint
    - Return draft details and participant list
    - Include ready status for each participant
    - Include canStart flag
    - _Requirements: Lobby state retrieval_

- [ ] 6. Frontend WebSocket client
  - [x] 6.1 Add WebSocket dependencies
    - Add sockjs-client to package.json
    - Add @stomp/stompjs to package.json
    - Install dependencies
    - _Requirements: WebSocket client libraries_
  
  - [x] 6.2 Create WebSocketService class
    - Implement connect(draftUuid) method
    - Implement disconnect() method
    - Implement subscribe(topic, callback) method
    - Implement send(destination, message) method
    - Handle reconnection logic
    - _Requirements: WebSocket communication layer_
  
  - [x] 6.3 Implement connection management
    - Connect to /ws endpoint
    - Subscribe to /topic/draft/{draftUuid}
    - Handle connection errors and reconnection
    - Store connection state in React context
    - _Requirements: Connection lifecycle_
  
  - [x] 6.4 Create message handlers
    - Handle DraftStateMessage - update entire draft state
    - Handle PickMessage - update single pick
    - Handle ErrorMessage - display error to user
    - Handle ParticipantJoined - show notification
    - _Requirements: Message processing_

- [x] 7. Live draft UI components
  - [x] 7.1 Create DraftSetupPage component
    - Form to create new live draft
    - Fields: draft name (required), creator nickname (required)
    - Fields: number of participants (2-12 dropdown), rounds (1-20 dropdown)
    - Submit creates draft via REST API
    - On success, redirect to /draft/{uuid}/lobby
    - Show loading state during creation
    - Display validation errors
    - _Requirements: Draft creation UI_
  
  - [x] 7.2 Create DraftLobbyPage component
    - Parse draftUuid from URL params
    - Connect to WebSocket on mount
    - Subscribe to /topic/draft/{draftUuid}/lobby
    - Display draft details: name, participant count, rounds
    - Show "Join Lobby" form if user hasn't joined
    - Show participant list with ready status
    - Show "Ready" toggle button for current user
    - Show "Start Draft" button (only for creator, only when all ready)
    - Auto-redirect to /draft/{uuid} when draft starts
    - _Requirements: Lobby UI_
  
  - [x] 7.3 Create JoinLobbyForm component
    - Input: nickname (required, 2-20 characters)
    - Dropdown: select position (show available positions only)
    - Submit button: "Join Lobby"
    - Show validation errors (nickname taken, position taken)
    - Disable form during submission
    - _Requirements: Join form_
  
  - [x] 7.4 Create ParticipantList component
    - Display list of participants with position, nickname, ready status
    - Show green checkmark for ready participants
    - Show "Waiting..." for not ready participants
    - Highlight current user's row
    - Show empty slots for positions not yet filled
    - Format: "Position A: Alice ✓" or "Position B: (Empty)"
    - _Requirements: Participant display_
  
  - [x] 7.5 Create ReadyButton component
    - Toggle button: "Ready" / "Not Ready"
    - Show current ready state
    - Disable if draft is starting
    - Send ready toggle via WebSocket
    - _Requirements: Ready toggle UI_
  
  - [x] 7.6 Create StartDraftButton component
    - Only visible to draft creator
    - Only enabled when all participants are ready
    - Show tooltip if not all ready: "Waiting for all participants to be ready"
    - Send start command via WebSocket
    - Show loading state during start
    - _Requirements: Start button_
  
  - [x] 7.7 Create LiveDraftBoard component
    - Parse draftUuid and position from URL params
    - Connect to WebSocket on mount
    - Subscribe to /topic/draft/{draftUuid}
    - Display draft board with same layout as offline version
    - Show column headers with position letters and nicknames
    - Highlight "My Team" column for user's position
    - Display current turn indicator
    - _Requirements: Main draft interface_
  
  - [x] 7.8 Implement player pool display
    - Show available players (not yet picked)
    - Display player stats: name, position, team, college, ADP
    - Enable drag-and-drop for current turn holder
    - Disable drag for non-turn holders (show "Not your turn" message)
    - Add "Force Pick" button on each player (always enabled)
    - _Requirements: Player selection interface_
  
  - [x] 7.9 Implement draft board grid
    - Display grid: rows = rounds, columns = positions
    - Show picked players in their slots
    - Display forced pick attribution: "Forced by {position}" in subtle text
    - Highlight current pick slot
    - Show empty slots for future picks
    - _Requirements: Draft board visualization_
  
  - [x] 7.10 Implement team ownership indicators
    - Add header row above round 1
    - Show "My Team ({nickname})" in user's position column
    - Show "{nickname}" in other columns
    - Style user's column with subtle background color
    - _Requirements: Team identification_
  
  - [x] 7.11 Implement pick interaction
    - On drag-and-drop: send makePick message via WebSocket
    - On "Force Pick" button: show modal to select target position
    - Send forcePick message with forcingPosition and targetPosition
    - Show loading state during pick submission
    - Display error if pick fails
    - _Requirements: Pick submission_
  
  - [x] 7.12 Implement real-time updates
    - Listen for DraftStateMessage from WebSocket
    - Update draft board when picks are made
    - Update available player pool
    - Update current turn indicator
    - Show toast notification: "{Nickname} picked {Player}"
    - _Requirements: Live updates_
  
  - [x] 7.13 Implement reconnection handling
    - Detect page reload or connection loss
    - Automatically reconnect to WebSocket
    - Request current draft state on reconnect
    - Show "Reconnecting..." indicator during reconnection
    - _Requirements: Resilient connection_

- [x] 8. Draft routing and navigation
  - [x] 8.1 Add live draft routes
    - Add route: /live-draft/setup - draft creation page
    - Add route: /draft/:draftUuid/lobby - lobby/waiting room page
    - Add route: /draft/:draftUuid - live draft board page
    - Add to View controller if needed on backend so spring can handle
    - Parse query param ?x={position} for user's position (optional in lobby)
    - _Requirements: URL routing_
  
  - [x] 8.2 Implement lobby to draft transition
    - Listen for DraftStartedMessage in lobby
    - Auto-redirect to /draft/{draftUuid}?x={userPosition}
    - Preserve user's position in URL
    - _Requirements: Seamless transition_
  
  - [x] 8.3 Implement position validation
    - In draft board: validate position query param is A-Z
    - In draft board: validate position is within participant count
    - In draft board: require position query param
    - Show error if invalid or missing position
    - _Requirements: URL validation_
  
  - [x] 8.4 Add navigation links
    - Add "Create Live Draft" button on home page
    - Add "Back to Lobby" button on draft board (if draft not started)
    - Add breadcrumb navigation
    - _Requirements: User navigation_

- [x] 9. Snake draft order logic
  - [x] 9.1 Implement snake draft calculator (if mode is selected) (could be linear draft)
    - Create utility function: calculatePickPosition(round, pick, participantCount)
    - Round 1: positions in order (A, B, C, D)
    - Round 2: positions in reverse (D, C, B, A)
    - Round 3: positions in order (A, B, C, D)
    - Continue alternating for all rounds
    - _Requirements: Turn order calculation_
  
  - [x] 9.2 Implement turn indicator
    - Calculate whose turn it is based on currentRound and currentPick
    - Display "Your turn!" banner for current turn holder
    - Display "Waiting for {Position}..." for others
    - Show countdown timer (optional enhancement)
    - _Requirements: Turn visualization_
  
  - [x] 9.3 Add pick validation
    - Validate pick is made by current turn position
    - Allow forced picks from any position
    - Prevent picking already-selected players
    - Show validation errors to user
    - _Requirements: Pick validation_

- [ ] 10. Draft completion and export
  - [ ] 10.1 Implement draft completion detection
    - Detect when all picks are made (currentPick > totalRounds * participantCount)
    - Set status to COMPLETED
    - Broadcast completion message to all participants
    - _Requirements: Draft completion_
  
  - [ ] 10.2 Create completion screen
    - Show "Draft Complete!" message
    - Display final draft results
    - Show each team's picks organized by position
    - Add export buttons (CSV, JSON, PDF)
    - _Requirements: Completion UI_
  
  - [ ] 10.3 Implement live draft export
    - Reuse ExportService from offline draft
    - Export includes forced pick attributions
    - Export shows snake draft order
    - _Requirements: Export functionality_

- [ ] 11. Error handling and edge cases
  - [ ] 11.1 Handle lobby errors
    - Show error if nickname is already taken
    - Show error if position is already taken
    - Show error if lobby is full
    - Show error if draft has already started
    - _Requirements: Lobby validation_
  
  - [ ] 11.2 Handle participant leaving
    - Remove participant from lobby when they disconnect
    - Unready all participants if someone leaves
    - Broadcast participant left message
    - Show notification to remaining participants
    - _Requirements: Participant management_
  
  - [ ] 11.3 Handle creator leaving
    - Transfer creator role to another participant
    - Show notification of new creator
    - Allow new creator to start draft
    - _Requirements: Creator transfer_
  
  - [ ] 11.4 Handle disconnections during draft
    - Show "Connection lost" indicator
    - Attempt automatic reconnection (exponential backoff)
    - Request full state on successful reconnection
    - Allow other participants to force-pick for disconnected user
    - _Requirements: Connection resilience_
  
  - [ ] 11.5 Handle concurrent picks
    - Backend validates pick hasn't been made already
    - Return error if player already picked
    - Refresh draft state for all participants
    - _Requirements: Race condition handling_
  
  - [ ] 11.6 Handle invalid positions
    - Validate position is within participant count
    - Show error for invalid position in URL
    - Redirect to lobby if position not joined
    - _Requirements: Input validation_
  
  - [ ] 11.7 Handle draft not found
    - Show user-friendly error if draftUuid is invalid
    - Provide link to create new draft
    - _Requirements: Error recovery_
  
  - [ ] 11.8 Handle page reload in lobby
    - Detect if user was previously in lobby (localStorage)
    - Auto-rejoin with same nickname and position
    - Restore ready state
    - _Requirements: Lobby persistence_

- [ ] 12. Testing and validation
  - [ ] 12.1 Test WebSocket connectivity
    - Test connection establishment
    - Test message sending and receiving
    - Test reconnection after disconnect
    - _Requirements: WebSocket reliability_
  
  - [ ] 12.2 Test lobby functionality
    - Test joining with unique nickname and position
    - Test nickname collision handling
    - Test position collision handling
    - Test ready/unready toggle
    - Test participant leaving
    - Test creator starting draft
    - _Requirements: Lobby correctness_
  
  - [ ] 12.3 Test snake draft order
    - Verify correct turn order for 2-12 participants
    - Test odd and even round transitions
    - Verify forced picks don't affect turn order
    - _Requirements: Turn order correctness_
  
  - [ ] 12.4 Test concurrent users
    - Test multiple users in same lobby
    - Test multiple users in same draft
    - Test simultaneous pick attempts
    - Test forced picks from multiple users
    - _Requirements: Multi-user functionality_
  
  - [ ] 12.5 Test page reload
    - Test reload in lobby (should auto-rejoin)
    - Test reload in draft (should reconnect with position)
    - Verify draft state is preserved
    - Verify WebSocket reconnects properly
    - _Requirements: State persistence_
  
  - [ ] 12.6 Test edge cases
    - Test all participants leaving lobby
    - Test creator leaving before start
    - Test starting draft with not all ready
    - Test joining full lobby
    - _Requirements: Edge case handling_

- [ ] 13. Polish and UX improvements
  - [ ] 13.1 Add loading states
    - Show spinner during draft creation
    - Show spinner during pick submission
    - Show skeleton UI while loading draft state
    - _Requirements: User feedback_
  
  - [ ] 13.2 Add animations
    - Animate new picks appearing on board
    - Animate turn indicator changes
    - Animate player removal from available pool
    - _Requirements: Visual polish_
  
  - [ ] 13.3 Add sound effects (optional)
    - Play sound when it's user's turn
    - Play sound when pick is made
    - Add mute toggle
    - _Requirements: Audio feedback_
  
  - [ ] 13.4 Add mobile optimizations
    - Ensure touch-friendly drag-and-drop
    - Optimize layout for mobile screens
    - Test on various devices
    - _Requirements: Mobile support_
  
  - [ ] 13.5 Add accessibility features
    - Add ARIA labels for all interactive elements
    - Support keyboard navigation
    - Add screen reader announcements for picks
    - _Requirements: Accessibility_

## Notes

- **Lobby Flow**: Create draft → Join lobby → Select position & nickname → Ready up → Creator starts → Draft begins
- **Unified Schema**: Live drafts and offline drafts use the same `drafts` and `draft_picks` tables
  - Offline drafts: status='completed', no participants, no position/forcedBy data
  - Live drafts: status='LOBBY'/'IN_PROGRESS'/'COMPLETED', has participants, includes position/forcedBy
- **New Table**: Only `draft_participants` is new, tracks lobby participants and ready status
- WebSocket endpoint: `ws://localhost:8080/ws` (dev) or `wss://domain.com/ws` (prod)
- STOMP topics: 
  - `/topic/draft/{draftUuid}/lobby` for lobby updates (join, ready, start)
  - `/topic/draft/{draftUuid}` for draft updates (picks, state changes)
- Position encoding: A=1, B=2, C=3, ..., Z=26 (supports up to 26 participants)
- Nicknames must be unique within a draft (2-50 characters)
- All participants must be ready before draft can start
- Only the creator can start the draft
- Snake draft order alternates direction each round
- Forced picks include attribution in `forced_by` column but don't change turn order
- Draft state is persisted in database, not just in-memory
- WebSocket messages are only sent to participants in the same draft (topic-based)
- SockJS provides fallback for browsers without WebSocket support
- Page reload in lobby: auto-rejoin with stored nickname/position (localStorage)
- Page reload in draft: reconnect with position from URL query param

## Technical Architecture

```
Frontend (React)
  ├── DraftSetupPage (create draft)
  ├── DraftLobbyPage (join, ready, wait)
  │   ├── JoinLobbyForm (nickname + position)
  │   ├── ParticipantList (show all participants)
  │   ├── ReadyButton (toggle ready)
  │   └── StartDraftButton (creator only)
  ├── LiveDraftBoard (main draft UI)
  │   ├── WebSocketService (connection)
  │   ├── PlayerPool (available players)
  │   ├── DraftGrid (picked players)
  │   └── TurnIndicator (whose turn)
  └── Routes
      ├── /live-draft/setup
      ├── /draft/:uuid/lobby
      └── /draft/:uuid?x=position

Backend (Spring Boot)
  ├── WebSocket Config (STOMP + SockJS)
  ├── LiveDraftController (WebSocket messages)
  │   ├── /join (join lobby)
  │   ├── /ready (toggle ready)
  │   ├── /start (start draft)
  │   ├── /pick (make pick)
  │   └── /force-pick (force pick)
  ├── DraftController (REST API - enhanced)
  ├── DraftService (draft logic - enhanced)
  ├── ParticipantService (participant logic - new)
  ├── Draft Entity (reused, enhanced)
  ├── DraftParticipant Entity (new)
  └── DraftPick Entity (reused, enhanced)

Database (MySQL)
  ├── drafts table (enhanced with live draft fields)
  ├── draft_participants table (new)
  └── draft_picks table (enhanced with position/forced_by)

WebSocket Topics
  ├── /topic/draft/{uuid}/lobby (lobby updates)
  └── /topic/draft/{uuid} (draft updates)
```

## Implementation Order

1. Backend infrastructure (tasks 1-2)
2. WebSocket messaging (tasks 3-4)
3. REST API (task 5)
4. Frontend WebSocket client (task 6)
5. UI components (task 7)
6. Routing and navigation (task 8)
7. Snake draft logic (task 9)
8. Completion and export (task 10)
9. Error handling (task 11)
10. Testing (task 12)
11. Polish (task 13)
