# Lobby UI Improvements

## Changes Made

### 1. Allow Starting Draft Without All Participants Ready
**Problem**: The "Start Draft" button was disabled until all participants marked themselves as ready, but the requirement is to allow starting anytime (force-pick handles empty slots).

**Solution**: 
- Updated `StartDraftButton.tsx` to remove the `allReady` requirement
- Changed `canStart` condition from `allReady && !loading` to just `!loading`
- Updated tooltip message to inform creator they can start even if not everyone is ready
- New tooltip: "Not all participants are ready. You can still start and use force-pick for empty slots."

**Files Changed**:
- `app-client/src/components/draft/StartDraftButton.tsx`

### 2. Removed Confusing "Waiting" Message
**Problem**: The lobby page showed "Waiting for all participants to ready up..." which contradicted the ability to start without everyone being ready.

**Solution**: 
- Removed the waiting message entirely from the lobby page
- Creator can now start the draft at any time

**Files Changed**:
- `app-client/src/components/pages/DraftLobbyPage.tsx`

### 3. Fixed SPA Routing for Nested Paths
**Problem**: Navigating to `/draft/{uuid}/lobby` resulted in a "No static resource" error because the view controller only matched single-level paths.

**Solution**: 
- Changed route patterns from `/draft/*` and `/drafts/*` to `/draft/**` and `/drafts/**`
- The `**` wildcard matches multiple path segments (e.g., `/draft/123/lobby`)

**Files Changed**:
- `app-server/src/main/java/devybigboard/controllers/ViewController.java`

## UI Behavior

### Ready Button
The "Not Ready" button in the user controls section is fully functional and clickable:
- Click to toggle between "Not Ready" (gray) and "Ready" (green)
- Hover effect shows it's interactive
- Only disabled when draft is starting

### Start Draft Button
The creator can now:
- Start the draft at any time (doesn't need to wait for all participants)
- See a helpful tooltip if not everyone is ready
- Use force-pick during the draft for any empty positions

## Testing
1. Create a new draft as the creator
2. Join the lobby
3. Notice the "Start Draft" button is enabled immediately (no need to ready up)
4. Click "Not Ready" button to toggle ready status
5. Start the draft even if not all positions are filled or ready
6. During draft, use force-pick for empty positions

## Related Requirements
- ✅ Allow starting draft without all positions filled
- ✅ Force-pick logic handles empty spots
- ✅ Creator can start draft at any time
- ✅ Ready status is optional, not required
