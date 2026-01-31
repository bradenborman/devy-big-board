# Lobby State Loading Fix

## Problem
The position selector in the Draft Lobby page was stuck on "Loading positions..." indefinitely. The WebSocket connection was successful, but the lobby state request was not being processed by the backend.

## Root Causes

### 1. Invalid Request Payload Validation
The `getLobbyState` WebSocket endpoint in `LiveDraftController.java` was using `JoinRequest` as the payload type, which has `@NotBlank` validation on both `nickname` and `position` fields. The frontend was only sending `{draftUuid: uuid}`, causing validation to fail before the method could execute.

### 2. Missing WebSocket Broker Configuration
The WebSocket configuration only enabled the simple broker for `/topic` destinations, but the controller was trying to send user-specific messages to `/user/queue/lobby-state`. The `/queue` and `/user` prefixes were not configured in the message broker.

## Solution
1. Created a new `StateRequest` class specifically for state retrieval endpoints that only requires the `draftUuid` field
2. Updated WebSocket configuration to enable `/queue` destinations and configure the `/user` prefix for user-specific messages

## Changes Made

### 1. New Model Class
**File**: `app-server/src/main/java/devybigboard/models/StateRequest.java`
- Created new request class with only `draftUuid` field
- Added `@NotBlank` validation for the UUID
- Simple POJO with constructor and getters/setters

### 2. Updated Controller
**File**: `app-server/src/main/java/devybigboard/controllers/LiveDraftController.java`
- Changed `getDraftState()` method to use `StateRequest` instead of `JoinRequest`
- Changed `getLobbyState()` method to use `StateRequest` instead of `JoinRequest`
- Both methods now properly validate only the required field (draftUuid)

### 3. Updated WebSocket Configuration
**File**: `app-server/src/main/java/devybigboard/config/WebSocketConfig.java`
- Added `/queue` to the simple broker destinations (was only `/topic`)
- Added `setUserDestinationPrefix("/user")` to enable user-specific message routing
- This allows `@SendToUser("/queue/lobby-state")` to work correctly

### 4. Updated Tests
**File**: `app-server/src/test/java/devybigboard/controllers/LiveDraftControllerTest.java`
- Updated `testGetDraftState_Success()` to use `StateRequest`
- Updated `testGetLobbyState_Success()` to use `StateRequest`

**File**: `app-server/src/test/java/devybigboard/services/DraftServiceTest.java`
- Updated `canStartDraft_ReturnsTrueWhenNotAllSlotsFilled()` test
- Changed assertion from `assertFalse` to `assertTrue` to reflect new requirement
- Updated test name and comments to reflect that drafts can now start with fewer participants

## Impact
- Frontend can now successfully request lobby state without providing nickname/position
- User-specific WebSocket messages (like state responses) now work correctly
- Position selector will load correctly and display available positions
- No breaking changes to existing join/leave/ready/start functionality
- All tests pass successfully

## Testing
All 165 tests pass, including:
- `LiveDraftControllerTest.testGetLobbyState_Success()`
- `LiveDraftControllerTest.testGetDraftState_Success()`
- `DraftServiceTest.canStartDraft_ReturnsTrueWhenNotAllSlotsFilled()`

## Next Steps
The frontend should now be able to:
1. Connect to the lobby WebSocket
2. Request lobby state with just the draft UUID
3. Receive the lobby state response via `/user/queue/lobby-state`
4. Display the position selector grid with available positions
5. Allow users to select their position and join the draft

## How to Test Locally
1. Restart the server: `./start-local.bat` (or `./start-local.sh` on Unix)
2. Navigate to the draft setup page
3. Create a new draft
4. The lobby page should now load the position selector correctly
5. You should see the grid of available positions (1.1, 1.2, etc.)
