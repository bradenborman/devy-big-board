# Migration from localStorage to URL Parameters

## Problem
Using localStorage to store user position and nickname caused issues when:
- Testing multiple drafts in the same browser
- Opening multiple tabs with different positions
- The stored data from one draft conflicted with another draft
- Error: `Cannot read properties of undefined (reading 'find')` when participants array was undefined

## Solution
Migrated from localStorage to URL query parameters for storing user position and nickname.

## Changes Made

### 1. Updated DraftLobbyPage.tsx
**File**: `app-client/src/components/pages/DraftLobbyPage.tsx`

**Before**:
- Used `localStorage.getItem()` and `localStorage.setItem()` to store position/nickname
- State variables: `currentUserPosition` and `currentUserNickname`

**After**:
- Uses `useSearchParams()` hook from react-router-dom
- Reads position and nickname directly from URL: `?position=A&nickname=John`
- Updates URL when user joins: `setSearchParams({ position, nickname })`

**Key Changes**:
- Removed localStorage read/write operations
- Changed state variables to read from `searchParams.get('position')` and `searchParams.get('nickname')`
- Added optional chaining to `lobbyState?.participants?.find()` to prevent undefined errors
- URL now contains all necessary state for the page

### 2. URL Format
**Old**: `/draft/{uuid}/lobby` (state in localStorage)
**New**: `/draft/{uuid}/lobby?position=A&nickname=John`

### 3. Benefits
- ✅ Each tab/window is independent
- ✅ Can test multiple positions in different tabs
- ✅ Shareable URLs (though not recommended for security)
- ✅ No localStorage conflicts between drafts
- ✅ Browser back/forward works correctly
- ✅ Refresh preserves your position

## Testing
1. Create a new draft
2. Join as position A with nickname "Alice"
3. URL becomes: `/draft/{uuid}/lobby?position=A&nickname=Alice`
4. Open a new tab with the same draft UUID
5. Join as position B with nickname "Bob"
6. URL becomes: `/draft/{uuid}/lobby?position=B&nickname=Bob`
7. Both tabs work independently without conflicts

## Migration Notes
- Old localStorage data is ignored (no migration needed)
- Users will need to rejoin if they refresh without URL params
- The creator nickname is still passed via navigation state initially
- After joining, URL params take precedence

## Related Files
- `app-client/src/components/pages/DraftLobbyPage.tsx` - Main changes
- No backend changes required
