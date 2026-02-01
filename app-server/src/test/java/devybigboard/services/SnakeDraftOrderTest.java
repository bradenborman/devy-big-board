package devybigboard.services;

import devybigboard.dao.DraftRepository;
import devybigboard.models.Draft;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for linear draft order calculation.
 * Validates that the turn order follows linear pattern (Aâ†’Bâ†’Câ†’D for all rounds).
 * 
 * NOTE: Snake draft support (alternating direction) is planned for future implementation
 * via the isSnakeDraft boolean flag on the Draft entity.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SnakeDraftOrderTest {

    @Autowired
    private DraftService draftService;

    @Autowired
    private DraftRepository draftRepository;

    /**
     * Test complete linear draft order for 4 participants across 3 rounds.
     * Expected pattern:
     * Round 1: A, B, C, D
     * Round 2: A, B, C, D
     * Round 3: A, B, C, D
     */
    @Test
    void testCompleteDraftOrder_4Participants_3Rounds() {
        // Create draft with 4 participants, 3 rounds
        Draft draft = draftService.createLiveDraft("Linear Test", "Alice", 4, 3, "1234", false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);

        // Expected order for 12 picks (linear: same order every round)
        String[] expectedOrder = {
            "A", "B", "C", "D",  // Round 1
            "A", "B", "C", "D",  // Round 2
            "A", "B", "C", "D"   // Round 3
        };

        // Verify each pick
        for (int pick = 1; pick <= 12; pick++) {
            int round = ((pick - 1) / 4) + 1;
            draft.setCurrentRound(round);
            draft.setCurrentPick(pick);
            draftRepository.save(draft);

            String currentTurn = draftService.getCurrentTurn(draft.getUuid());
            String expected = expectedOrder[pick - 1];
            
            assertEquals(expected, currentTurn, 
                String.format("Pick %d (Round %d): Expected %s but got %s", 
                    pick, round, expected, currentTurn));
        }
    }

    /**
     * Test linear draft order for 3 participants across 4 rounds.
     * Expected pattern:
     * Round 1: A, B, C
     * Round 2: A, B, C
     * Round 3: A, B, C
     * Round 4: A, B, C
     */
    @Test
    void testCompleteDraftOrder_3Participants_4Rounds() {
        // Create draft with 3 participants, 4 rounds
        Draft draft = draftService.createLiveDraft("Linear Test", "Alice", 3, 4, "1234", false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);

        // Expected order for 12 picks (linear: same order every round)
        String[] expectedOrder = {
            "A", "B", "C",  // Round 1
            "A", "B", "C",  // Round 2
            "A", "B", "C",  // Round 3
            "A", "B", "C"   // Round 4
        };

        // Verify each pick
        for (int pick = 1; pick <= 12; pick++) {
            int round = ((pick - 1) / 3) + 1;
            draft.setCurrentRound(round);
            draft.setCurrentPick(pick);
            draftRepository.save(draft);

            String currentTurn = draftService.getCurrentTurn(draft.getUuid());
            String expected = expectedOrder[pick - 1];
            
            assertEquals(expected, currentTurn, 
                String.format("Pick %d (Round %d): Expected %s but got %s", 
                    pick, round, expected, currentTurn));
        }
    }

    /**
     * Test linear draft order for 2 participants (minimum).
     * Expected pattern:
     * Round 1: A, B
     * Round 2: A, B
     * Round 3: A, B
     */
    @Test
    void testCompleteDraftOrder_2Participants() {
        // Create draft with 2 participants, 3 rounds
        Draft draft = draftService.createLiveDraft("Linear Test", "Alice", 2, 3, "1234", false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);

        // Expected order for 6 picks (linear: same order every round)
        String[] expectedOrder = {
            "A", "B",  // Round 1
            "A", "B",  // Round 2
            "A", "B"   // Round 3
        };

        // Verify each pick
        for (int pick = 1; pick <= 6; pick++) {
            int round = ((pick - 1) / 2) + 1;
            draft.setCurrentRound(round);
            draft.setCurrentPick(pick);
            draftRepository.save(draft);

            String currentTurn = draftService.getCurrentTurn(draft.getUuid());
            String expected = expectedOrder[pick - 1];
            
            assertEquals(expected, currentTurn, 
                String.format("Pick %d (Round %d): Expected %s but got %s", 
                    pick, round, expected, currentTurn));
        }
    }

    /**
     * Test linear draft order for 6 participants.
     * Expected pattern:
     * Round 1: A, B, C, D, E, F
     * Round 2: A, B, C, D, E, F
     */
    @Test
    void testCompleteDraftOrder_6Participants() {
        // Create draft with 6 participants, 2 rounds
        Draft draft = draftService.createLiveDraft("Linear Test", "Alice", 6, 2, "1234", false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);

        // Expected order for 12 picks (linear: same order every round)
        String[] expectedOrder = {
            "A", "B", "C", "D", "E", "F",  // Round 1
            "A", "B", "C", "D", "E", "F"   // Round 2
        };

        // Verify each pick
        for (int pick = 1; pick <= 12; pick++) {
            int round = ((pick - 1) / 6) + 1;
            draft.setCurrentRound(round);
            draft.setCurrentPick(pick);
            draftRepository.save(draft);

            String currentTurn = draftService.getCurrentTurn(draft.getUuid());
            String expected = expectedOrder[pick - 1];
            
            assertEquals(expected, currentTurn, 
                String.format("Pick %d (Round %d): Expected %s but got %s", 
                    pick, round, expected, currentTurn));
        }
    }

    /**
     * Test that isValidPick correctly validates turn order.
     * Only the current turn position should be able to make a pick.
     */
    @Test
    void testPickValidation_OnlyCurrentTurnCanPick() {
        // Create draft with 4 participants
        Draft draft = draftService.createLiveDraft("Linear Test", "Alice", 4, 2, "1234", false);
        draft.setStatus("IN_PROGRESS");
        draft.setCurrentRound(1);
        draft.setCurrentPick(1);
        draftRepository.save(draft);

        // Only position A should be able to pick
        assertTrue(draftService.isValidPick(draft.getUuid(), "A"));
        assertFalse(draftService.isValidPick(draft.getUuid(), "B"));
        assertFalse(draftService.isValidPick(draft.getUuid(), "C"));
        assertFalse(draftService.isValidPick(draft.getUuid(), "D"));

        // Move to pick 2 (position B)
        draft.setCurrentPick(2);
        draftRepository.save(draft);

        assertFalse(draftService.isValidPick(draft.getUuid(), "A"));
        assertTrue(draftService.isValidPick(draft.getUuid(), "B"));
        assertFalse(draftService.isValidPick(draft.getUuid(), "C"));
        assertFalse(draftService.isValidPick(draft.getUuid(), "D"));
    }

    /**
     * Test that isValidPick works correctly in round 2 (linear order, not reversed).
     */
    @Test
    void testPickValidation_LinearOrderInRound2() {
        // Create draft with 4 participants in round 2
        Draft draft = draftService.createLiveDraft("Linear Test", "Alice", 4, 2, "1234", false);
        draft.setStatus("IN_PROGRESS");
        draft.setCurrentRound(2);
        draft.setCurrentPick(5);  // First pick of round 2
        draftRepository.save(draft);

        // Position A should be able to pick (linear order, not reversed)
        assertTrue(draftService.isValidPick(draft.getUuid(), "A"));
        assertFalse(draftService.isValidPick(draft.getUuid(), "B"));
        assertFalse(draftService.isValidPick(draft.getUuid(), "C"));
        assertFalse(draftService.isValidPick(draft.getUuid(), "D"));

        // Move to pick 6 (position B)
        draft.setCurrentPick(6);
        draftRepository.save(draft);

        assertFalse(draftService.isValidPick(draft.getUuid(), "A"));
        assertTrue(draftService.isValidPick(draft.getUuid(), "B"));
        assertFalse(draftService.isValidPick(draft.getUuid(), "C"));
        assertFalse(draftService.isValidPick(draft.getUuid(), "D"));
    }

    /**
     * Test edge case: Last pick of a round transitions correctly to first pick of next round.
     */
    @Test
    void testRoundTransition() {
        // Create draft with 3 participants
        Draft draft = draftService.createLiveDraft("Linear Test", "Alice", 3, 3, "1234", false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);

        // Last pick of round 1 (pick 3, position C)
        draft.setCurrentRound(1);
        draft.setCurrentPick(3);
        draftRepository.save(draft);
        assertEquals("C", draftService.getCurrentTurn(draft.getUuid()));

        // First pick of round 2 (pick 4, position A - linear order)
        draft.setCurrentRound(2);
        draft.setCurrentPick(4);
        draftRepository.save(draft);
        assertEquals("A", draftService.getCurrentTurn(draft.getUuid()));

        // Last pick of round 2 (pick 6, position C)
        draft.setCurrentPick(6);
        draftRepository.save(draft);
        assertEquals("C", draftService.getCurrentTurn(draft.getUuid()));

        // First pick of round 3 (pick 7, position A - linear order)
        draft.setCurrentRound(3);
        draft.setCurrentPick(7);
        draftRepository.save(draft);
        assertEquals("A", draftService.getCurrentTurn(draft.getUuid()));
    }

    /**
     * Test that getCurrentTurn returns null when draft is not in progress.
     */
    @Test
    void testGetCurrentTurn_NotInProgress() {
        // Create draft in LOBBY status
        Draft draft = draftService.createLiveDraft("Linear Test", "Alice", 4, 2, "1234", false);
        assertNull(draftService.getCurrentTurn(draft.getUuid()));

        // Create draft in COMPLETED status
        Draft completedDraft = draftService.createLiveDraft("Completed", "Bob", 4, 2, "1234", false);
        completedDraft.setStatus("COMPLETED");
        draftRepository.save(completedDraft);
        assertNull(draftService.getCurrentTurn(completedDraft.getUuid()));
    }

    /**
     * Test that isValidPick returns false when draft is not in progress.
     */
    @Test
    void testIsValidPick_NotInProgress() {
        // Create draft in LOBBY status
        Draft draft = draftService.createLiveDraft("Linear Test", "Alice", 4, 2, "1234", false);
        
        // No position should be able to pick
        assertFalse(draftService.isValidPick(draft.getUuid(), "A"));
        assertFalse(draftService.isValidPick(draft.getUuid(), "B"));
        assertFalse(draftService.isValidPick(draft.getUuid(), "C"));
        assertFalse(draftService.isValidPick(draft.getUuid(), "D"));
    }
}

