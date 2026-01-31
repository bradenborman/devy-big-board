# Linear Draft Order Fix

## Issue
The live draft implementation was using snake draft order (alternating A→B→C→D then D→C→B→A) instead of the requested linear draft order (A→B→C→D for all rounds).

## User Requirement
- Draft order should be **linear**: All rounds go A→B→C→D (or A→B→C for 3 participants, etc.)
- Pick notation is `{round}.{pick_in_round}` (e.g., 1.1, 1.2, 2.1, 2.2)
- Add `isSnakeDraft` boolean field to Draft entity for future snake draft support

## Changes Made

### 1. Backend Code (`DraftService.java`)
**File**: `app-server/src/main/java/devybigboard/services/DraftService.java`

Updated `getCurrentTurn()` method to use linear draft order:
- Removed snake draft logic (even round reversal)
- All rounds now use forward order: A→B→C→D
- Added TODO comments for future snake draft implementation
- Kept commented-out snake draft code for reference

```java
// Linear draft order: always forward (A, B, C, D) for all rounds
int positionIndex = pickInRound;

// TODO: Snake draft support - uncomment when needed
// boolean isSnakeDraft = draft.getIsSnakeDraft();
// if (isSnakeDraft) {
//     int currentRound = draft.getCurrentRound();
//     if (currentRound % 2 == 0) {
//         // Even rounds: reverse order (D, C, B, A)
//         positionIndex = participantCount - 1 - pickInRound;
//     }
// }
```

### 2. Database Schema (`schema.sql`)
**File**: `app-server/src/main/resources/schema.sql`

Added `is_snake_draft` column to drafts table:
```sql
is_snake_draft BOOLEAN DEFAULT FALSE,
```

This allows future implementation of snake draft functionality without breaking existing drafts.

### 3. Draft Entity (`Draft.java`)
**File**: `app-server/src/main/java/devybigboard/models/Draft.java`

Added `isSnakeDraft` field:
```java
@Column(name = "is_snake_draft")
private Boolean isSnakeDraft = false;
```

With getter and setter methods.

### 4. Test Updates

#### DraftServiceTest.java
**File**: `app-server/src/test/java/devybigboard/services/DraftServiceTest.java`

Updated 4 test methods to expect linear order:
- `getCurrentTurn_FollowsLinearOrderInAllRounds()` - renamed from `getCurrentTurn_FollowsForwardOrderInOddRounds()`
- `getCurrentTurn_FollowsLinearOrderInRound2()` - renamed from `getCurrentTurn_FollowsReverseOrderInEvenRounds()`
- `getCurrentTurn_UsesLinearOrderAcrossMultipleRounds()` - renamed from `getCurrentTurn_AlternatesDirectionAcrossMultipleRounds()`
- `isValidPick_WorksWithLinearDraftOrder()` - renamed from `isValidPick_WorksWithSnakeDraftOrder()`

All tests now expect:
- Round 1: A, B, C, D
- Round 2: A, B, C, D (not D, C, B, A)
- Round 3: A, B, C, D

#### SnakeDraftOrderTest.java
**File**: `app-server/src/test/java/devybigboard/services/SnakeDraftOrderTest.java`

Completely rewrote all tests to validate linear draft order:
- Updated class documentation to reflect linear order testing
- Added note about future snake draft support via `isSnakeDraft` flag
- All test methods now expect linear order (A→B→C→D for all rounds)
- Updated test names from "Snake Test" to "Linear Test"
- Removed reverse order validation tests

## Draft Order Examples

### 4 Participants, 3 Rounds (Linear Order)
```
Round 1: 1.1(A), 1.2(B), 1.3(C), 1.4(D)
Round 2: 2.1(A), 2.2(B), 2.3(C), 2.4(D)
Round 3: 3.1(A), 3.2(B), 3.3(C), 3.4(D)
```

### 3 Participants, 4 Rounds (Linear Order)
```
Round 1: 1.1(A), 1.2(B), 1.3(C)
Round 2: 2.1(A), 2.2(B), 2.3(C)
Round 3: 3.1(A), 3.2(B), 3.3(C)
Round 4: 4.1(A), 4.2(B), 4.3(C)
```

## Future Snake Draft Implementation

When snake draft support is needed:
1. Set `isSnakeDraft = true` when creating a draft
2. Uncomment the snake draft logic in `DraftService.getCurrentTurn()`
3. Update tests to validate snake draft behavior when flag is enabled
4. Add UI toggle for snake draft option in draft creation form

## Test Results
✅ All tests passing
- `DraftServiceTest.getCurrentTurn*` - All passing
- `DraftServiceTest.isValidPick*` - All passing
- `SnakeDraftOrderTest` - All passing

## Verification
Run tests with:
```bash
.\gradlew.bat :app-server:test --tests "devybigboard.services.DraftServiceTest" --tests "devybigboard.services.SnakeDraftOrderTest"
```
