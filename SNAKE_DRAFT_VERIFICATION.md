# Snake Draft Order Verification

## Implementation Analysis

The `getCurrentTurn()` method in `DraftService.java` implements snake draft order calculation.

### Algorithm

```java
// Calculate position within the current round (0-indexed)
int pickInRound = (currentPick - 1) % participantCount;

// Determine position based on snake draft order
int positionIndex;
if (currentRound % 2 == 1) {
    // Odd rounds: forward order (A, B, C, D)
    positionIndex = pickInRound;
} else {
    // Even rounds: reverse order (D, C, B, A)
    positionIndex = participantCount - 1 - pickInRound;
}

// Convert to letter (0=A, 1=B, etc.)
return String.valueOf((char) ('A' + positionIndex));
```

### Verification for 4 Participants

#### Round 1 (Odd - Forward Order)
- Pick 1: pickInRound = (1-1) % 4 = 0, positionIndex = 0 → **A** ✓
- Pick 2: pickInRound = (2-1) % 4 = 1, positionIndex = 1 → **B** ✓
- Pick 3: pickInRound = (3-1) % 4 = 2, positionIndex = 2 → **C** ✓
- Pick 4: pickInRound = (4-1) % 4 = 3, positionIndex = 3 → **D** ✓

#### Round 2 (Even - Reverse Order)
- Pick 5: pickInRound = (5-1) % 4 = 0, positionIndex = 4-1-0 = 3 → **D** ✓
- Pick 6: pickInRound = (6-1) % 4 = 1, positionIndex = 4-1-1 = 2 → **C** ✓
- Pick 7: pickInRound = (7-1) % 4 = 2, positionIndex = 4-1-2 = 1 → **B** ✓
- Pick 8: pickInRound = (8-1) % 4 = 3, positionIndex = 4-1-3 = 0 → **A** ✓

#### Round 3 (Odd - Forward Order)
- Pick 9: pickInRound = (9-1) % 4 = 0, positionIndex = 0 → **A** ✓
- Pick 10: pickInRound = (10-1) % 4 = 1, positionIndex = 1 → **B** ✓
- Pick 11: pickInRound = (11-1) % 4 = 2, positionIndex = 2 → **C** ✓
- Pick 12: pickInRound = (12-1) % 4 = 3, positionIndex = 3 → **D** ✓

### Verification for 3 Participants

#### Round 1 (Odd - Forward Order)
- Pick 1: pickInRound = 0, positionIndex = 0 → **A** ✓
- Pick 2: pickInRound = 1, positionIndex = 1 → **B** ✓
- Pick 3: pickInRound = 2, positionIndex = 2 → **C** ✓

#### Round 2 (Even - Reverse Order)
- Pick 4: pickInRound = 0, positionIndex = 3-1-0 = 2 → **C** ✓
- Pick 5: pickInRound = 1, positionIndex = 3-1-1 = 1 → **B** ✓
- Pick 6: pickInRound = 2, positionIndex = 3-1-2 = 0 → **A** ✓

#### Round 3 (Odd - Forward Order)
- Pick 7: pickInRound = 0, positionIndex = 0 → **A** ✓
- Pick 8: pickInRound = 1, positionIndex = 1 → **B** ✓
- Pick 9: pickInRound = 2, positionIndex = 2 → **C** ✓

## Conclusion

✅ **The implementation is CORRECT**

The snake draft order calculation properly:
1. Calculates the position within the current round using modulo
2. Applies forward order (A→B→C→D) for odd rounds
3. Applies reverse order (D→C→B→A) for even rounds
4. Converts numeric position to letter (A-Z)

## Test Coverage

The existing `DraftServiceTest.java` includes comprehensive tests:
- ✅ Forward order in odd rounds
- ✅ Reverse order in even rounds
- ✅ Alternating direction across multiple rounds
- ✅ Different participant counts (2, 3, 4, 6)
- ✅ Pick validation with snake draft order

All tests pass successfully.

## Task Requirements Met

✓ Calculate current pick position based on snake draft order
✓ Round 1: A→B→C→D, Round 2: D→C→B→A, Round 3: A→B→C→D, etc.
✓ Validate pick is for current turn position
✓ Allow forced picks from any position (handled by separate forcePick method)
✓ Requirements: Turn order validation
