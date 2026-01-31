# Removed Ready Button Logic

## Rationale
The "ready" status was unnecessary friction. If someone joins a position, they're ready to draft. The ready button added an extra step without providing value.

## Changes Made

### Frontend Changes

**File**: `app-client/src/components/pages/DraftLobbyPage.tsx`

1. **Removed ReadyButton component**
   - Removed import of `ReadyButton`
   - Removed `<ReadyButton>` from the user controls section
   - Removed `handleToggleReady` callback function
   - Removed `currentParticipant` variable (no longer needed)

2. **Simplified UI**
   - User controls now only show:
     - User info (nickname and position)
     - Start Draft button (for creator only)
   - No more ready/not ready toggle

### Backend Changes

**File**: `app-server/src/main/java/devybigboard/services/ParticipantService.java`

1. **Auto-ready on join**
   - Changed `participant.setIsReady(false)` to `participant.setIsReady(true)`
   - Participants are now automatically marked as ready when they join
   - Comment updated: "Create and save participant (auto-ready when joining)"

2. **Kept ready endpoint**
   - The `/ready` WebSocket endpoint still exists in the controller
   - The `setReady()` method still works in ParticipantService
   - This allows for future flexibility if needed
   - Just not exposed in the UI anymore

## User Experience

**Before**:
1. Join lobby → select position
2. Click "Not Ready" button to toggle to "Ready"
3. Wait for all participants to ready up
4. Creator can start draft

**After**:
1. Join lobby → select position (automatically ready)
2. Creator can start draft immediately

## Benefits
- ✅ Simpler, more intuitive UX
- ✅ Fewer clicks to start a draft
- ✅ Less confusion about what "ready" means
- ✅ Joining a position = ready to draft (obvious)
- ✅ Creator can still start anytime (force-pick handles empty slots)

## Technical Notes
- The `isReady` field still exists in the database and model
- The ready toggle endpoint still exists but is unused
- ParticipantList component still shows ready status (green checkmark)
- This is fine - it just always shows everyone as ready now
- Could remove the ready field entirely in a future cleanup

## Testing
1. Create a new draft
2. Join as any position
3. Notice no "Ready" button in your controls
4. See green checkmark next to your name in participants list
5. Creator can immediately start the draft
