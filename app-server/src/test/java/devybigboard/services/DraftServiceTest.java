package devybigboard.services;

import devybigboard.dao.DraftRepository;
import devybigboard.exceptions.DraftNotFoundException;
import devybigboard.models.Draft;
import devybigboard.models.DraftParticipant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DraftService live draft functionality.
 * Tests draft lifecycle management including creation, lobby state, starting, and turn validation.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DraftServiceTest {

    @Autowired
    private DraftService draftService;

    @Autowired
    private DraftRepository draftRepository;

    private Draft testDraft;

    @BeforeEach
    void setUp() {
        // Tests will create their own drafts as needed
    }

    // ========== createLiveDraft Tests ==========

    @Test
    void createLiveDraft_CreatesNewDraftWithCorrectProperties() {
        // Create a live draft
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);

        // Verify draft properties
        assertNotNull(draft);
        assertNotNull(draft.getId());
        assertNotNull(draft.getUuid());
        assertEquals("Test Draft", draft.getDraftName());
        assertEquals("Alice", draft.getCreatedBy());
        assertEquals("LOBBY", draft.getStatus());
        assertEquals(4, draft.getParticipantCount());
        assertEquals(10, draft.getTotalRounds());
        assertEquals(1, draft.getCurrentRound());
        assertEquals(1, draft.getCurrentPick());
        assertNull(draft.getStartedAt());
    }

    @Test
    void createLiveDraft_GeneratesUniqueUuid() {
        // Create two drafts
        Draft draft1 = draftService.createLiveDraft("Draft 1", "Alice", 4, 10, false);
        Draft draft2 = draftService.createLiveDraft("Draft 2", "Bob", 4, 10, false);

        // Verify UUIDs are unique
        assertNotEquals(draft1.getUuid(), draft2.getUuid());
    }

    @Test
    void createLiveDraft_SupportsDifferentParticipantCounts() {
        // Create drafts with different participant counts
        Draft draft2 = draftService.createLiveDraft("2 Player", "Alice", 2, 10, false);
        Draft draft12 = draftService.createLiveDraft("12 Player", "Bob", 12, 10, false);

        // Verify participant counts
        assertEquals(2, draft2.getParticipantCount());
        assertEquals(12, draft12.getParticipantCount());
    }

    @Test
    void createLiveDraft_SupportsDifferentRoundCounts() {
        // Create drafts with different round counts
        Draft draft5 = draftService.createLiveDraft("5 Rounds", "Alice", 4, 5, false);
        Draft draft20 = draftService.createLiveDraft("20 Rounds", "Bob", 4, 20, false);

        // Verify round counts
        assertEquals(5, draft5.getTotalRounds());
        assertEquals(20, draft20.getTotalRounds());
    }

    // ========== getLobbyState Tests ==========

    @Test
    void getLobbyState_ReturnsCompleteDraftWithParticipants() {
        // Create draft with participants
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        DraftParticipant participant1 = new DraftParticipant(draft, "A", "Alice");
        DraftParticipant participant2 = new DraftParticipant(draft, "B", "Bob");
        draft.addParticipant(participant1);
        draft.addParticipant(participant2);
        draftRepository.save(draft);

        // Get lobby state
        Draft lobbyState = draftService.getLobbyState(draft.getUuid());

        // Verify draft and participants are loaded
        assertNotNull(lobbyState);
        assertEquals(draft.getUuid(), lobbyState.getUuid());
        assertEquals(2, lobbyState.getParticipants().size());
    }

    @Test
    void getLobbyState_ThrowsExceptionForInvalidUuid() {
        // Attempt to get lobby state for non-existent draft
        assertThrows(DraftNotFoundException.class, () -> {
            draftService.getLobbyState("invalid-uuid");
        });
    }

    // ========== canStartDraft Tests ==========

    @Test
    void canStartDraft_ReturnsTrueWhenAllParticipantsReadyAndSlotsFilled() {
        // Create draft with all participants ready
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 3, 10, false);
        DraftParticipant participant1 = new DraftParticipant(draft, "A", "Alice");
        participant1.setIsReady(true);
        DraftParticipant participant2 = new DraftParticipant(draft, "B", "Bob");
        participant2.setIsReady(true);
        DraftParticipant participant3 = new DraftParticipant(draft, "C", "Charlie");
        participant3.setIsReady(true);
        draft.addParticipant(participant1);
        draft.addParticipant(participant2);
        draft.addParticipant(participant3);
        draftRepository.save(draft);

        // Check if draft can start
        boolean canStart = draftService.canStartDraft(draft.getUuid());

        // Verify draft can start
        assertTrue(canStart);
    }

    @Test
    void canStartDraft_ReturnsFalseWhenNotAllParticipantsReady() {
        // Create draft with one participant not ready
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 3, 10, false);
        DraftParticipant participant1 = new DraftParticipant(draft, "A", "Alice");
        participant1.setIsReady(true);
        DraftParticipant participant2 = new DraftParticipant(draft, "B", "Bob");
        participant2.setIsReady(false); // Not ready
        DraftParticipant participant3 = new DraftParticipant(draft, "C", "Charlie");
        participant3.setIsReady(true);
        draft.addParticipant(participant1);
        draft.addParticipant(participant2);
        draft.addParticipant(participant3);
        draftRepository.save(draft);

        // Check if draft can start
        boolean canStart = draftService.canStartDraft(draft.getUuid());

        // Verify draft cannot start
        assertFalse(canStart);
    }

    @Test
    void canStartDraft_ReturnsTrueWhenNotAllSlotsFilled() {
        // Create draft with only 2 of 4 participants
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        DraftParticipant participant1 = new DraftParticipant(draft, "A", "Alice");
        participant1.setIsReady(true);
        DraftParticipant participant2 = new DraftParticipant(draft, "B", "Bob");
        participant2.setIsReady(true);
        draft.addParticipant(participant1);
        draft.addParticipant(participant2);
        draftRepository.save(draft);

        // Check if draft can start
        boolean canStart = draftService.canStartDraft(draft.getUuid());

        // Verify draft CAN start (changed requirement: allow starting with fewer participants)
        assertTrue(canStart);
    }

    @Test
    void canStartDraft_ReturnsFalseWhenNoParticipants() {
        // Create draft with no participants
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);

        // Check if draft can start
        boolean canStart = draftService.canStartDraft(draft.getUuid());

        // Verify draft cannot start
        assertFalse(canStart);
    }

    // ========== startDraft Tests ==========

    @Test
    void startDraft_ChangesStatusToInProgressAndSetsStartTime() {
        // Create draft ready to start
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 2, 10, false);
        DraftParticipant participant1 = new DraftParticipant(draft, "A", "Alice");
        participant1.setIsReady(true);
        DraftParticipant participant2 = new DraftParticipant(draft, "B", "Bob");
        participant2.setIsReady(true);
        draft.addParticipant(participant1);
        draft.addParticipant(participant2);
        draftRepository.save(draft);

        // Start the draft
        Draft startedDraft = draftService.startDraft(draft.getUuid());

        // Verify status and start time
        assertEquals("IN_PROGRESS", startedDraft.getStatus());
        assertNotNull(startedDraft.getStartedAt());
    }

    @Test
    void startDraft_ThrowsExceptionWhenNotInLobbyStatus() {
        // Create draft and manually set to IN_PROGRESS
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 2, 10, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);

        // Attempt to start draft
        assertThrows(IllegalStateException.class, () -> {
            draftService.startDraft(draft.getUuid());
        });
    }

    @Test
    void startDraft_ThrowsExceptionWhenNotAllParticipantsReady() {
        // Create draft with one participant not ready
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 2, 10, false);
        DraftParticipant participant1 = new DraftParticipant(draft, "A", "Alice");
        participant1.setIsReady(true);
        DraftParticipant participant2 = new DraftParticipant(draft, "B", "Bob");
        participant2.setIsReady(false);
        draft.addParticipant(participant1);
        draft.addParticipant(participant2);
        draftRepository.save(draft);

        // Attempt to start draft
        assertThrows(IllegalStateException.class, () -> {
            draftService.startDraft(draft.getUuid());
        });
    }

    // ========== getCurrentTurn Tests ==========

    @Test
    void getCurrentTurn_ReturnsNullWhenDraftNotInProgress() {
        // Create draft in LOBBY status
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);

        // Get current turn
        String currentTurn = draftService.getCurrentTurn(draft.getUuid());

        // Verify null is returned
        assertNull(currentTurn);
    }

    @Test
    void getCurrentTurn_ReturnsFirstPositionAtStartOfRound1() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draft.setCurrentRound(1);
        draft.setCurrentPick(1);
        draftRepository.save(draft);

        // Get current turn
        String currentTurn = draftService.getCurrentTurn(draft.getUuid());

        // Verify first position (A)
        assertEquals("A", currentTurn);
    }

    @Test
    void getCurrentTurn_FollowsLinearOrderInAllRounds() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draft.setCurrentRound(1);
        draftRepository.save(draft);

        // Test picks 1-4 in round 1 (linear order: A, B, C, D)
        draft.setCurrentPick(1);
        draftRepository.save(draft);
        assertEquals("A", draftService.getCurrentTurn(draft.getUuid()));

        draft.setCurrentPick(2);
        draftRepository.save(draft);
        assertEquals("B", draftService.getCurrentTurn(draft.getUuid()));

        draft.setCurrentPick(3);
        draftRepository.save(draft);
        assertEquals("C", draftService.getCurrentTurn(draft.getUuid()));

        draft.setCurrentPick(4);
        draftRepository.save(draft);
        assertEquals("D", draftService.getCurrentTurn(draft.getUuid()));
    }

    @Test
    void getCurrentTurn_FollowsLinearOrderInRound2() {
        // Create draft in progress at round 2
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draft.setCurrentRound(2);
        draftRepository.save(draft);

        // Test picks 5-8 in round 2 (linear order: A, B, C, D)
        draft.setCurrentPick(5);
        draftRepository.save(draft);
        assertEquals("A", draftService.getCurrentTurn(draft.getUuid()));

        draft.setCurrentPick(6);
        draftRepository.save(draft);
        assertEquals("B", draftService.getCurrentTurn(draft.getUuid()));

        draft.setCurrentPick(7);
        draftRepository.save(draft);
        assertEquals("C", draftService.getCurrentTurn(draft.getUuid()));

        draft.setCurrentPick(8);
        draftRepository.save(draft);
        assertEquals("D", draftService.getCurrentTurn(draft.getUuid()));
    }

    @Test
    void getCurrentTurn_UsesLinearOrderAcrossMultipleRounds() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 3, 10, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);

        // Round 1 (picks 1-3): A, B, C
        draft.setCurrentRound(1);
        draft.setCurrentPick(1);
        draftRepository.save(draft);
        assertEquals("A", draftService.getCurrentTurn(draft.getUuid()));

        // Round 2 (picks 4-6): A, B, C (linear, not reversed)
        draft.setCurrentRound(2);
        draft.setCurrentPick(4);
        draftRepository.save(draft);
        assertEquals("A", draftService.getCurrentTurn(draft.getUuid()));

        // Round 3 (picks 7-9): A, B, C
        draft.setCurrentRound(3);
        draft.setCurrentPick(7);
        draftRepository.save(draft);
        assertEquals("A", draftService.getCurrentTurn(draft.getUuid()));
    }

    @Test
    void getCurrentTurn_WorksWithDifferentParticipantCounts() {
        // Test with 2 participants
        Draft draft2 = draftService.createLiveDraft("2 Player", "Alice", 2, 10, false);
        draft2.setStatus("IN_PROGRESS");
        draft2.setCurrentRound(1);
        draft2.setCurrentPick(1);
        draftRepository.save(draft2);
        assertEquals("A", draftService.getCurrentTurn(draft2.getUuid()));

        draft2.setCurrentPick(2);
        draftRepository.save(draft2);
        assertEquals("B", draftService.getCurrentTurn(draft2.getUuid()));

        // Test with 6 participants
        Draft draft6 = draftService.createLiveDraft("6 Player", "Bob", 6, 10, false);
        draft6.setStatus("IN_PROGRESS");
        draft6.setCurrentRound(1);
        draft6.setCurrentPick(6);
        draftRepository.save(draft6);
        assertEquals("F", draftService.getCurrentTurn(draft6.getUuid()));
    }

    // ========== isValidPick Tests ==========

    @Test
    void isValidPick_ReturnsTrueWhenPositionMatchesCurrentTurn() {
        // Create draft in progress with position A's turn
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draft.setCurrentRound(1);
        draft.setCurrentPick(1);
        draftRepository.save(draft);

        // Validate position A can pick
        boolean isValid = draftService.isValidPick(draft.getUuid(), "A");

        // Verify pick is valid
        assertTrue(isValid);
    }

    @Test
    void isValidPick_ReturnsFalseWhenPositionDoesNotMatchCurrentTurn() {
        // Create draft in progress with position A's turn
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draft.setCurrentRound(1);
        draft.setCurrentPick(1);
        draftRepository.save(draft);

        // Validate position B cannot pick
        boolean isValid = draftService.isValidPick(draft.getUuid(), "B");

        // Verify pick is not valid
        assertFalse(isValid);
    }

    @Test
    void isValidPick_ReturnsFalseWhenDraftNotInProgress() {
        // Create draft in LOBBY status
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);

        // Validate position A cannot pick
        boolean isValid = draftService.isValidPick(draft.getUuid(), "A");

        // Verify pick is not valid
        assertFalse(isValid);
    }

    @Test
    void isValidPick_WorksWithLinearDraftOrder() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);

        // Round 1, pick 1: A can pick
        draft.setCurrentRound(1);
        draft.setCurrentPick(1);
        draftRepository.save(draft);
        assertTrue(draftService.isValidPick(draft.getUuid(), "A"));
        assertFalse(draftService.isValidPick(draft.getUuid(), "D"));

        // Round 2, pick 5: A can pick (linear order, not reversed)
        draft.setCurrentRound(2);
        draft.setCurrentPick(5);
        draftRepository.save(draft);
        assertTrue(draftService.isValidPick(draft.getUuid(), "A"));
        assertFalse(draftService.isValidPick(draft.getUuid(), "D"));
    }
    
    // ========== makePick Tests ==========
    
    @Autowired
    private PlayerService playerService;
    
    @Test
    void makePick_CreatesPickRecordWithCorrectProperties() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Create a test player
        devybigboard.models.Player player = new devybigboard.models.Player();
        player.setName("Test Player");
        player.setPosition("RB");
        player.setTeam("Test Team");
        player.setCollege("Test College");
        player.setVerified(true);
        player = playerService.savePlayer(player);
        
        // Make a pick
        Draft updatedDraft = draftService.makePick(draft.getUuid(), player.getId(), "A");
        
        // Verify pick was created with correct properties
        assertEquals(1, updatedDraft.getPicks().size());
        devybigboard.models.DraftPick pick = updatedDraft.getPicks().get(0);
        assertEquals(player.getId(), pick.getPlayer().getId());
        assertEquals("A", pick.getPosition());
        assertEquals(1, pick.getRoundNumber());
        assertEquals(1, pick.getPickNumber());
        assertNotNull(pick.getPickedAt());
        assertNull(pick.getForcedBy());
    }
    
    @Test
    void makePick_IncrementsCurrentPickCounter() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Create test players
        devybigboard.models.Player player1 = new devybigboard.models.Player();
        player1.setName("Player 1");
        player1.setPosition("RB");
        player1.setVerified(true);
        player1 = playerService.savePlayer(player1);
        
        devybigboard.models.Player player2 = new devybigboard.models.Player();
        player2.setName("Player 2");
        player2.setPosition("WR");
        player2.setVerified(true);
        player2 = playerService.savePlayer(player2);
        
        // Make first pick
        Draft updatedDraft = draftService.makePick(draft.getUuid(), player1.getId(), "A");
        assertEquals(2, updatedDraft.getCurrentPick());
        
        // Make second pick
        updatedDraft = draftService.makePick(draft.getUuid(), player2.getId(), "B");
        assertEquals(3, updatedDraft.getCurrentPick());
    }
    
    @Test
    void makePick_UpdatesCurrentRoundWhenRoundCompletes() {
        // Create draft with 2 participants
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 2, 10, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Create test players
        devybigboard.models.Player player1 = new devybigboard.models.Player();
        player1.setName("Player 1");
        player1.setPosition("RB");
        player1.setVerified(true);
        player1 = playerService.savePlayer(player1);
        
        devybigboard.models.Player player2 = new devybigboard.models.Player();
        player2.setName("Player 2");
        player2.setPosition("WR");
        player2.setVerified(true);
        player2 = playerService.savePlayer(player2);
        
        devybigboard.models.Player player3 = new devybigboard.models.Player();
        player3.setName("Player 3");
        player3.setPosition("QB");
        player3.setVerified(true);
        player3 = playerService.savePlayer(player3);
        
        // Make picks in round 1
        Draft updatedDraft = draftService.makePick(draft.getUuid(), player1.getId(), "A");
        assertEquals(1, updatedDraft.getCurrentRound()); // Next pick (2) is still in round 1
        
        updatedDraft = draftService.makePick(draft.getUuid(), player2.getId(), "B");
        assertEquals(2, updatedDraft.getCurrentRound()); // Next pick (3) is in round 2
        
        // Make first pick of round 2 - should stay in round 2
        updatedDraft = draftService.makePick(draft.getUuid(), player3.getId(), "B");
        assertEquals(2, updatedDraft.getCurrentRound()); // Next pick (4) is still in round 2
    }
    
    @Test
    void makePick_SetsStatusToCompletedWhenAllPicksMade() {
        // Create draft with 2 participants and 2 rounds (4 total picks)
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 2, 2, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Create test players
        devybigboard.models.Player[] players = new devybigboard.models.Player[4];
        for (int i = 0; i < 4; i++) {
            players[i] = new devybigboard.models.Player();
            players[i].setName("Player " + (i + 1));
            players[i].setPosition("RB");
            players[i].setVerified(true);
            players[i] = playerService.savePlayer(players[i]);
        }
        
        // Make all picks
        Draft updatedDraft = draftService.makePick(draft.getUuid(), players[0].getId(), "A");
        assertEquals("IN_PROGRESS", updatedDraft.getStatus());
        
        updatedDraft = draftService.makePick(draft.getUuid(), players[1].getId(), "B");
        assertEquals("IN_PROGRESS", updatedDraft.getStatus());
        
        updatedDraft = draftService.makePick(draft.getUuid(), players[2].getId(), "B");
        assertEquals("IN_PROGRESS", updatedDraft.getStatus());
        
        // Last pick should complete the draft
        updatedDraft = draftService.makePick(draft.getUuid(), players[3].getId(), "A");
        assertEquals("COMPLETED", updatedDraft.getStatus());
        assertNotNull(updatedDraft.getCompletedAt());
    }
    
    @Test
    void makePick_ThrowsExceptionWhenDraftNotInProgress() {
        // Create draft in LOBBY status
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        
        // Create a test player
        devybigboard.models.Player player = new devybigboard.models.Player();
        player.setName("Test Player");
        player.setPosition("RB");
        player.setVerified(true);
        devybigboard.models.Player savedPlayer = playerService.savePlayer(player);
        
        // Attempt to make pick
        assertThrows(IllegalStateException.class, () -> {
            draftService.makePick(draft.getUuid(), savedPlayer.getId(), "A");
        });
    }
    
    @Test
    void makePick_ThrowsExceptionWhenPlayerAlreadyPicked() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Create a test player
        devybigboard.models.Player player = new devybigboard.models.Player();
        player.setName("Test Player");
        player.setPosition("RB");
        player.setVerified(true);
        devybigboard.models.Player savedPlayer = playerService.savePlayer(player);
        
        // Make first pick
        draftService.makePick(draft.getUuid(), savedPlayer.getId(), "A");
        
        // Attempt to pick same player again
        assertThrows(IllegalArgumentException.class, () -> {
            draftService.makePick(draft.getUuid(), savedPlayer.getId(), "B");
        });
    }
    
    @Test
    void makePick_ThrowsExceptionForInvalidPlayerId() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Attempt to pick non-existent player
        assertThrows(devybigboard.exceptions.PlayerNotFoundException.class, () -> {
            draftService.makePick(draft.getUuid(), 99999L, "A");
        });
    }
    
    @Test
    void makePick_ThrowsExceptionForInvalidDraftUuid() {
        // Create a test player
        devybigboard.models.Player player = new devybigboard.models.Player();
        player.setName("Test Player");
        player.setPosition("RB");
        player.setVerified(true);
        devybigboard.models.Player savedPlayer = playerService.savePlayer(player);
        
        // Attempt to make pick in non-existent draft
        assertThrows(DraftNotFoundException.class, () -> {
            draftService.makePick("invalid-uuid", savedPlayer.getId(), "A");
        });
    }
    
    @Test
    void makePick_HandlesMultiplePicksInSequence() {
        // Create draft with 3 participants
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 3, 3, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Create test players
        devybigboard.models.Player[] players = new devybigboard.models.Player[9];
        for (int i = 0; i < 9; i++) {
            players[i] = new devybigboard.models.Player();
            players[i].setName("Player " + (i + 1));
            players[i].setPosition("RB");
            players[i].setVerified(true);
            players[i] = playerService.savePlayer(players[i]);
        }
        
        // Make picks and verify state after each
        String[] positions = {"A", "B", "C", "C", "B", "A", "A", "B", "C"};
        int[] expectedRounds = {1, 1, 2, 2, 2, 3, 3, 3, 4}; // Round of NEXT pick
        
        for (int i = 0; i < 9; i++) {
            Draft updatedDraft = draftService.makePick(draft.getUuid(), players[i].getId(), positions[i]);
            assertEquals(i + 2, updatedDraft.getCurrentPick()); // Next pick number
            // For the last pick, we don't update round since draft is complete
            if (i < 8) {
                assertEquals(expectedRounds[i], updatedDraft.getCurrentRound());
            }
            assertEquals(i + 1, updatedDraft.getPicks().size());
        }
        
        // Verify final state
        Draft finalDraft = draftService.getDraftByUuid(draft.getUuid());
        assertEquals("COMPLETED", finalDraft.getStatus());
        assertEquals(9, finalDraft.getPicks().size());
    }
    
    @Test
    void makePick_PreservesPickOrderInDraftEntity() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 2, 2, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Create test players
        devybigboard.models.Player[] players = new devybigboard.models.Player[4];
        for (int i = 0; i < 4; i++) {
            players[i] = new devybigboard.models.Player();
            players[i].setName("Player " + (i + 1));
            players[i].setPosition("RB");
            players[i].setVerified(true);
            players[i] = playerService.savePlayer(players[i]);
        }
        
        // Make all picks
        draftService.makePick(draft.getUuid(), players[0].getId(), "A");
        draftService.makePick(draft.getUuid(), players[1].getId(), "B");
        draftService.makePick(draft.getUuid(), players[2].getId(), "B");
        Draft finalDraft = draftService.makePick(draft.getUuid(), players[3].getId(), "A");
        
        // Verify picks are in correct order
        assertEquals(4, finalDraft.getPicks().size());
        for (int i = 0; i < 4; i++) {
            assertEquals(i + 1, finalDraft.getPicks().get(i).getPickNumber());
            assertEquals(players[i].getId(), finalDraft.getPicks().get(i).getPlayer().getId());
        }
    }
    
    // ========== forcePick Tests ==========
    
    @Test
    void forcePick_CreatesPickRecordWithForcedByField() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Create a test player
        devybigboard.models.Player player = new devybigboard.models.Player();
        player.setName("Test Player");
        player.setPosition("RB");
        player.setTeam("Test Team");
        player.setCollege("Test College");
        player.setVerified(true);
        player = playerService.savePlayer(player);
        
        // Force a pick - position B forces a pick for position A
        Draft updatedDraft = draftService.forcePick(draft.getUuid(), player.getId(), "A", "B");
        
        // Verify pick was created with correct properties
        assertEquals(1, updatedDraft.getPicks().size());
        devybigboard.models.DraftPick pick = updatedDraft.getPicks().get(0);
        assertEquals(player.getId(), pick.getPlayer().getId());
        assertEquals("A", pick.getPosition()); // Target position
        assertEquals("B", pick.getForcedBy()); // Forcing position
        assertEquals(1, pick.getRoundNumber());
        assertEquals(1, pick.getPickNumber());
        assertNotNull(pick.getPickedAt());
    }
    
    @Test
    void forcePick_BypassesTurnValidation() {
        // Create draft in progress where it's position A's turn
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draft.setCurrentRound(1);
        draft.setCurrentPick(1); // Position A's turn
        draftRepository.save(draft);
        
        // Create a test player
        devybigboard.models.Player player = new devybigboard.models.Player();
        player.setName("Test Player");
        player.setPosition("RB");
        player.setVerified(true);
        player = playerService.savePlayer(player);
        
        // Position C forces a pick for position A (even though it's A's turn)
        Draft updatedDraft = draftService.forcePick(draft.getUuid(), player.getId(), "A", "C");
        
        // Verify pick was successful
        assertEquals(1, updatedDraft.getPicks().size());
        assertEquals("C", updatedDraft.getPicks().get(0).getForcedBy());
    }
    
    @Test
    void forcePick_IncrementsCurrentPickCounter() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Create test players
        devybigboard.models.Player player1 = new devybigboard.models.Player();
        player1.setName("Player 1");
        player1.setPosition("RB");
        player1.setVerified(true);
        player1 = playerService.savePlayer(player1);
        
        devybigboard.models.Player player2 = new devybigboard.models.Player();
        player2.setName("Player 2");
        player2.setPosition("WR");
        player2.setVerified(true);
        player2 = playerService.savePlayer(player2);
        
        // Make first forced pick
        Draft updatedDraft = draftService.forcePick(draft.getUuid(), player1.getId(), "A", "B");
        assertEquals(2, updatedDraft.getCurrentPick());
        
        // Make second forced pick
        updatedDraft = draftService.forcePick(draft.getUuid(), player2.getId(), "C", "D");
        assertEquals(3, updatedDraft.getCurrentPick());
    }
    
    @Test
    void forcePick_UpdatesCurrentRoundWhenRoundCompletes() {
        // Create draft with 2 participants
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 2, 10, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Create test players
        devybigboard.models.Player player1 = new devybigboard.models.Player();
        player1.setName("Player 1");
        player1.setPosition("RB");
        player1.setVerified(true);
        player1 = playerService.savePlayer(player1);
        
        devybigboard.models.Player player2 = new devybigboard.models.Player();
        player2.setName("Player 2");
        player2.setPosition("WR");
        player2.setVerified(true);
        player2 = playerService.savePlayer(player2);
        
        devybigboard.models.Player player3 = new devybigboard.models.Player();
        player3.setName("Player 3");
        player3.setPosition("QB");
        player3.setVerified(true);
        player3 = playerService.savePlayer(player3);
        
        // Make forced picks in round 1
        Draft updatedDraft = draftService.forcePick(draft.getUuid(), player1.getId(), "A", "B");
        assertEquals(1, updatedDraft.getCurrentRound()); // Next pick (2) is still in round 1
        
        updatedDraft = draftService.forcePick(draft.getUuid(), player2.getId(), "B", "A");
        assertEquals(2, updatedDraft.getCurrentRound()); // Next pick (3) is in round 2
        
        // Make first forced pick of round 2
        updatedDraft = draftService.forcePick(draft.getUuid(), player3.getId(), "B", "A");
        assertEquals(2, updatedDraft.getCurrentRound()); // Next pick (4) is still in round 2
    }
    
    @Test
    void forcePick_SetsStatusToCompletedWhenAllPicksMade() {
        // Create draft with 2 participants and 2 rounds (4 total picks)
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 2, 2, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Create test players
        devybigboard.models.Player[] players = new devybigboard.models.Player[4];
        for (int i = 0; i < 4; i++) {
            players[i] = new devybigboard.models.Player();
            players[i].setName("Player " + (i + 1));
            players[i].setPosition("RB");
            players[i].setVerified(true);
            players[i] = playerService.savePlayer(players[i]);
        }
        
        // Make all forced picks
        Draft updatedDraft = draftService.forcePick(draft.getUuid(), players[0].getId(), "A", "B");
        assertEquals("IN_PROGRESS", updatedDraft.getStatus());
        
        updatedDraft = draftService.forcePick(draft.getUuid(), players[1].getId(), "B", "A");
        assertEquals("IN_PROGRESS", updatedDraft.getStatus());
        
        updatedDraft = draftService.forcePick(draft.getUuid(), players[2].getId(), "B", "A");
        assertEquals("IN_PROGRESS", updatedDraft.getStatus());
        
        // Last forced pick should complete the draft
        updatedDraft = draftService.forcePick(draft.getUuid(), players[3].getId(), "A", "B");
        assertEquals("COMPLETED", updatedDraft.getStatus());
        assertNotNull(updatedDraft.getCompletedAt());
    }
    
    @Test
    void forcePick_ThrowsExceptionWhenDraftNotInProgress() {
        // Create draft in LOBBY status
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        
        // Create a test player
        devybigboard.models.Player player = new devybigboard.models.Player();
        player.setName("Test Player");
        player.setPosition("RB");
        player.setVerified(true);
        devybigboard.models.Player savedPlayer = playerService.savePlayer(player);
        
        // Attempt to force pick
        assertThrows(IllegalStateException.class, () -> {
            draftService.forcePick(draft.getUuid(), savedPlayer.getId(), "A", "B");
        });
    }
    
    @Test
    void forcePick_ThrowsExceptionWhenPlayerAlreadyPicked() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Create a test player
        devybigboard.models.Player player = new devybigboard.models.Player();
        player.setName("Test Player");
        player.setPosition("RB");
        player.setVerified(true);
        devybigboard.models.Player savedPlayer = playerService.savePlayer(player);
        
        // Make first forced pick
        draftService.forcePick(draft.getUuid(), savedPlayer.getId(), "A", "B");
        
        // Attempt to force pick same player again
        assertThrows(IllegalArgumentException.class, () -> {
            draftService.forcePick(draft.getUuid(), savedPlayer.getId(), "C", "D");
        });
    }
    
    @Test
    void forcePick_ThrowsExceptionForInvalidPlayerId() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Attempt to force pick non-existent player
        assertThrows(devybigboard.exceptions.PlayerNotFoundException.class, () -> {
            draftService.forcePick(draft.getUuid(), 99999L, "A", "B");
        });
    }
    
    @Test
    void forcePick_ThrowsExceptionForInvalidDraftUuid() {
        // Create a test player
        devybigboard.models.Player player = new devybigboard.models.Player();
        player.setName("Test Player");
        player.setPosition("RB");
        player.setVerified(true);
        devybigboard.models.Player savedPlayer = playerService.savePlayer(player);
        
        // Attempt to force pick in non-existent draft
        assertThrows(DraftNotFoundException.class, () -> {
            draftService.forcePick("invalid-uuid", savedPlayer.getId(), "A", "B");
        });
    }
    
    @Test
    void forcePick_AllowsAnyPositionToForceForAnyPosition() {
        // Create draft in progress where it's position A's turn
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draft.setCurrentRound(1);
        draft.setCurrentPick(1); // Position A's turn
        draftRepository.save(draft);
        
        // Create test players
        devybigboard.models.Player[] players = new devybigboard.models.Player[4];
        for (int i = 0; i < 4; i++) {
            players[i] = new devybigboard.models.Player();
            players[i].setName("Player " + (i + 1));
            players[i].setPosition("RB");
            players[i].setVerified(true);
            players[i] = playerService.savePlayer(players[i]);
        }
        
        // Position D forces a pick for position A
        Draft updatedDraft = draftService.forcePick(draft.getUuid(), players[0].getId(), "A", "D");
        assertEquals("D", updatedDraft.getPicks().get(0).getForcedBy());
        
        // Position A forces a pick for position C
        updatedDraft = draftService.forcePick(draft.getUuid(), players[1].getId(), "C", "A");
        assertEquals("A", updatedDraft.getPicks().get(1).getForcedBy());
        
        // Position B forces a pick for position B (self)
        updatedDraft = draftService.forcePick(draft.getUuid(), players[2].getId(), "B", "B");
        assertEquals("B", updatedDraft.getPicks().get(2).getForcedBy());
        
        // Verify all picks were successful
        assertEquals(3, updatedDraft.getPicks().size());
    }
    
    @Test
    void forcePick_MixedWithRegularPicks() {
        // Create draft with 3 participants
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 3, 2, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Create test players
        devybigboard.models.Player[] players = new devybigboard.models.Player[6];
        for (int i = 0; i < 6; i++) {
            players[i] = new devybigboard.models.Player();
            players[i].setName("Player " + (i + 1));
            players[i].setPosition("RB");
            players[i].setVerified(true);
            players[i] = playerService.savePlayer(players[i]);
        }
        
        // Mix regular picks and forced picks
        Draft updatedDraft = draftService.makePick(draft.getUuid(), players[0].getId(), "A");
        assertNull(updatedDraft.getPicks().get(0).getForcedBy());
        
        updatedDraft = draftService.forcePick(draft.getUuid(), players[1].getId(), "B", "C");
        assertEquals("C", updatedDraft.getPicks().get(1).getForcedBy());
        
        updatedDraft = draftService.makePick(draft.getUuid(), players[2].getId(), "C");
        assertNull(updatedDraft.getPicks().get(2).getForcedBy());
        
        updatedDraft = draftService.forcePick(draft.getUuid(), players[3].getId(), "C", "A");
        assertEquals("A", updatedDraft.getPicks().get(3).getForcedBy());
        
        updatedDraft = draftService.makePick(draft.getUuid(), players[4].getId(), "B");
        assertNull(updatedDraft.getPicks().get(4).getForcedBy());
        
        updatedDraft = draftService.forcePick(draft.getUuid(), players[5].getId(), "A", "B");
        assertEquals("B", updatedDraft.getPicks().get(5).getForcedBy());
        
        // Verify final state
        assertEquals("COMPLETED", updatedDraft.getStatus());
        assertEquals(6, updatedDraft.getPicks().size());
    }
    
    @Test
    void forcePick_PreservesPickOrderInDraftEntity() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 2, 2, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Create test players
        devybigboard.models.Player[] players = new devybigboard.models.Player[4];
        for (int i = 0; i < 4; i++) {
            players[i] = new devybigboard.models.Player();
            players[i].setName("Player " + (i + 1));
            players[i].setPosition("RB");
            players[i].setVerified(true);
            players[i] = playerService.savePlayer(players[i]);
        }
        
        // Make all forced picks
        draftService.forcePick(draft.getUuid(), players[0].getId(), "A", "B");
        draftService.forcePick(draft.getUuid(), players[1].getId(), "B", "A");
        draftService.forcePick(draft.getUuid(), players[2].getId(), "B", "A");
        Draft finalDraft = draftService.forcePick(draft.getUuid(), players[3].getId(), "A", "B");
        
        // Verify picks are in correct order
        assertEquals(4, finalDraft.getPicks().size());
        for (int i = 0; i < 4; i++) {
            assertEquals(i + 1, finalDraft.getPicks().get(i).getPickNumber());
            assertEquals(players[i].getId(), finalDraft.getPicks().get(i).getPlayer().getId());
            assertNotNull(finalDraft.getPicks().get(i).getForcedBy());
        }
    }
    
    // ========== getDraftState Tests ==========
    
    @Test
    void getDraftState_ReturnsCompleteStateForLobbyDraft() {
        // Create draft in LOBBY status with participants
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 3, 5, false);
        DraftParticipant participant1 = new DraftParticipant(draft, "A", "Alice");
        participant1.setIsReady(true);
        DraftParticipant participant2 = new DraftParticipant(draft, "B", "Bob");
        participant2.setIsReady(false);
        draft.addParticipant(participant1);
        draft.addParticipant(participant2);
        draftRepository.save(draft);
        
        // Get draft state
        devybigboard.models.DraftState state = draftService.getDraftState(draft.getUuid());
        
        // Verify basic draft properties
        assertNotNull(state);
        assertEquals(draft.getUuid(), state.getUuid());
        assertEquals("Test Draft", state.getDraftName());
        assertEquals("LOBBY", state.getStatus());
        assertEquals(1, state.getCurrentRound());
        assertEquals(1, state.getCurrentPick());
        assertEquals(5, state.getTotalRounds());
        assertEquals(3, state.getParticipantCount());
        assertNull(state.getCurrentTurnPosition()); // No turn in LOBBY
        
        // Verify participants
        assertEquals(2, state.getParticipants().size());
        
        // Verify no picks yet
        assertEquals(0, state.getPicks().size());
        
        // Verify available players (should include all verified players)
        assertNotNull(state.getAvailablePlayers());
    }
    
    @Test
    void getDraftState_ReturnsCompleteStateForInProgressDraft() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 3, 5, false);
        draft.setStatus("IN_PROGRESS");
        
        // Add participants
        DraftParticipant participant1 = new DraftParticipant(draft, "A", "Alice");
        DraftParticipant participant2 = new DraftParticipant(draft, "B", "Bob");
        DraftParticipant participant3 = new DraftParticipant(draft, "C", "Charlie");
        draft.addParticipant(participant1);
        draft.addParticipant(participant2);
        draft.addParticipant(participant3);
        draftRepository.save(draft);
        
        // Create test players and make some picks
        devybigboard.models.Player player1 = new devybigboard.models.Player();
        player1.setName("Player 1");
        player1.setPosition("RB");
        player1.setVerified(true);
        player1 = playerService.savePlayer(player1);
        
        devybigboard.models.Player player2 = new devybigboard.models.Player();
        player2.setName("Player 2");
        player2.setPosition("WR");
        player2.setVerified(true);
        player2 = playerService.savePlayer(player2);
        
        draftService.makePick(draft.getUuid(), player1.getId(), "A");
        draftService.makePick(draft.getUuid(), player2.getId(), "B");
        
        // Get draft state
        devybigboard.models.DraftState state = draftService.getDraftState(draft.getUuid());
        
        // Verify basic draft properties
        assertEquals(draft.getUuid(), state.getUuid());
        assertEquals("IN_PROGRESS", state.getStatus());
        assertEquals(1, state.getCurrentRound());
        assertEquals(3, state.getCurrentPick()); // Next pick
        assertEquals("C", state.getCurrentTurnPosition()); // Position C's turn
        
        // Verify participants
        assertEquals(3, state.getParticipants().size());
        
        // Verify picks
        assertEquals(2, state.getPicks().size());
        assertEquals(player1.getId(), state.getPicks().get(0).getPlayer().getId());
        assertEquals(player2.getId(), state.getPicks().get(1).getPlayer().getId());
        
        // Verify available players (should not include picked players)
        assertNotNull(state.getAvailablePlayers());
        final Long player1Id = player1.getId();
        final Long player2Id = player2.getId();
        assertFalse(state.getAvailablePlayers().stream()
            .anyMatch(p -> p.getId().equals(player1Id)));
        assertFalse(state.getAvailablePlayers().stream()
            .anyMatch(p -> p.getId().equals(player2Id)));
    }
    
    @Test
    void getDraftState_IncludesForcedPickAttributions() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 3, 5, false);
        draft.setStatus("IN_PROGRESS");
        
        // Add participants
        DraftParticipant participant1 = new DraftParticipant(draft, "A", "Alice");
        DraftParticipant participant2 = new DraftParticipant(draft, "B", "Bob");
        DraftParticipant participant3 = new DraftParticipant(draft, "C", "Charlie");
        draft.addParticipant(participant1);
        draft.addParticipant(participant2);
        draft.addParticipant(participant3);
        draftRepository.save(draft);
        
        // Create test players
        devybigboard.models.Player player1 = new devybigboard.models.Player();
        player1.setName("Player 1");
        player1.setPosition("RB");
        player1.setVerified(true);
        player1 = playerService.savePlayer(player1);
        
        devybigboard.models.Player player2 = new devybigboard.models.Player();
        player2.setName("Player 2");
        player2.setPosition("WR");
        player2.setVerified(true);
        player2 = playerService.savePlayer(player2);
        
        // Make a regular pick and a forced pick
        draftService.makePick(draft.getUuid(), player1.getId(), "A");
        draftService.forcePick(draft.getUuid(), player2.getId(), "B", "C");
        
        // Get draft state
        devybigboard.models.DraftState state = draftService.getDraftState(draft.getUuid());
        
        // Verify picks include forced pick attribution
        assertEquals(2, state.getPicks().size());
        assertNull(state.getPicks().get(0).getForcedBy()); // Regular pick
        assertEquals("C", state.getPicks().get(1).getForcedBy()); // Forced pick
    }
    
    @Test
    void getDraftState_ReturnsCompleteStateForCompletedDraft() {
        // Create draft with 2 participants and 2 rounds (4 total picks)
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 2, 2, false);
        draft.setStatus("IN_PROGRESS");
        
        // Add participants
        DraftParticipant participant1 = new DraftParticipant(draft, "A", "Alice");
        DraftParticipant participant2 = new DraftParticipant(draft, "B", "Bob");
        draft.addParticipant(participant1);
        draft.addParticipant(participant2);
        draftRepository.save(draft);
        
        // Create test players and complete the draft
        devybigboard.models.Player[] players = new devybigboard.models.Player[4];
        for (int i = 0; i < 4; i++) {
            players[i] = new devybigboard.models.Player();
            players[i].setName("Player " + (i + 1));
            players[i].setPosition("RB");
            players[i].setVerified(true);
            players[i] = playerService.savePlayer(players[i]);
        }
        
        draftService.makePick(draft.getUuid(), players[0].getId(), "A");
        draftService.makePick(draft.getUuid(), players[1].getId(), "B");
        draftService.makePick(draft.getUuid(), players[2].getId(), "B");
        draftService.makePick(draft.getUuid(), players[3].getId(), "A");
        
        // Get draft state
        devybigboard.models.DraftState state = draftService.getDraftState(draft.getUuid());
        
        // Verify draft is completed
        assertEquals("COMPLETED", state.getStatus());
        assertNotNull(state.getCompletedAt());
        assertNull(state.getCurrentTurnPosition()); // No turn when completed
        
        // Verify all picks are present
        assertEquals(4, state.getPicks().size());
        
        // Verify participants
        assertEquals(2, state.getParticipants().size());
        
        // Verify available players (should not include any picked players)
        assertNotNull(state.getAvailablePlayers());
        for (int i = 0; i < 4; i++) {
            final int index = i;
            assertFalse(state.getAvailablePlayers().stream()
                .anyMatch(p -> p.getId().equals(players[index].getId())));
        }
    }
    
    @Test
    void getDraftState_ThrowsExceptionForInvalidUuid() {
        // Attempt to get draft state for non-existent draft
        assertThrows(DraftNotFoundException.class, () -> {
            draftService.getDraftState("invalid-uuid");
        });
    }
    
    @Test
    void getDraftState_HandlesEmptyParticipantList() {
        // Create draft with no participants
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        
        // Get draft state
        devybigboard.models.DraftState state = draftService.getDraftState(draft.getUuid());
        
        // Verify empty participant list
        assertNotNull(state.getParticipants());
        assertEquals(0, state.getParticipants().size());
    }
    
    @Test
    void getDraftState_HandlesEmptyPickList() {
        // Create draft in progress with no picks yet
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 4, 10, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Get draft state
        devybigboard.models.DraftState state = draftService.getDraftState(draft.getUuid());
        
        // Verify empty pick list
        assertNotNull(state.getPicks());
        assertEquals(0, state.getPicks().size());
    }
    
    @Test
    void getDraftState_FiltersAvailablePlayersCorrectly() {
        // Create draft in progress
        Draft draft = draftService.createLiveDraft("Test Draft", "Alice", 2, 2, false);
        draft.setStatus("IN_PROGRESS");
        draftRepository.save(draft);
        
        // Create multiple test players
        devybigboard.models.Player pickedPlayer = new devybigboard.models.Player();
        pickedPlayer.setName("Picked Player");
        pickedPlayer.setPosition("RB");
        pickedPlayer.setVerified(true);
        pickedPlayer = playerService.savePlayer(pickedPlayer);
        
        devybigboard.models.Player availablePlayer = new devybigboard.models.Player();
        availablePlayer.setName("Available Player");
        availablePlayer.setPosition("WR");
        availablePlayer.setVerified(true);
        availablePlayer = playerService.savePlayer(availablePlayer);
        
        devybigboard.models.Player unverifiedPlayer = new devybigboard.models.Player();
        unverifiedPlayer.setName("Unverified Player");
        unverifiedPlayer.setPosition("QB");
        unverifiedPlayer.setVerified(false);
        unverifiedPlayer = playerService.savePlayer(unverifiedPlayer);
        
        // Make a pick
        draftService.makePick(draft.getUuid(), pickedPlayer.getId(), "A");
        
        // Get draft state
        devybigboard.models.DraftState state = draftService.getDraftState(draft.getUuid());
        
        // Verify picked player is not in available players
        final Long pickedPlayerId = pickedPlayer.getId();
        final Long availablePlayerId = availablePlayer.getId();
        final Long unverifiedPlayerId = unverifiedPlayer.getId();
        
        assertFalse(state.getAvailablePlayers().stream()
            .anyMatch(p -> p.getId().equals(pickedPlayerId)));
        
        // Verify available verified player is in available players
        assertTrue(state.getAvailablePlayers().stream()
            .anyMatch(p -> p.getId().equals(availablePlayerId)));
        
        // Verify unverified player is not in available players
        assertFalse(state.getAvailablePlayers().stream()
            .anyMatch(p -> p.getId().equals(unverifiedPlayerId)));
    }
}


