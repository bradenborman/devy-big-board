package devybigboard.services;

import devybigboard.dao.DraftParticipantRepository;
import devybigboard.dao.DraftRepository;
import devybigboard.exceptions.DraftNotFoundException;
import devybigboard.exceptions.ValidationException;
import devybigboard.models.Draft;
import devybigboard.models.DraftParticipant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ParticipantService.
 * Tests participant management including joining, leaving, ready status, and validation.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ParticipantServiceTest {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private DraftService draftService;

    @Autowired
    private DraftRepository draftRepository;

    @Autowired
    private DraftParticipantRepository participantRepository;

    private Draft testDraft;

    @BeforeEach
    void setUp() {
        // Create a test draft in LOBBY status
        testDraft = draftService.createLiveDraft("Test Draft", "Creator", 4, 10, "1234", false);
    }

    // ========== joinDraft Tests ==========

    @Test
    void joinDraft_CreatesParticipantWithCorrectProperties() {
        // Join draft
        DraftParticipant participant = participantService.joinDraft(testDraft.getId(), "Alice", "A");

        // Verify participant properties
        assertNotNull(participant);
        assertNotNull(participant.getId());
        assertEquals(testDraft.getId(), participant.getDraft().getId());
        assertEquals("Alice", participant.getNickname());
        assertEquals("A", participant.getPosition());
        assertFalse(participant.getIsReady()); // Non-creators start as not ready
        assertFalse(participant.getIsVerified()); // Non-creators start as not verified
        assertNotNull(participant.getJoinedAt());
    }

    @Test
    void joinDraft_AllowsMultipleParticipantsWithDifferentPositions() {
        // Join multiple participants
        DraftParticipant participant1 = participantService.joinDraft(testDraft.getId(), "Alice", "A");
        DraftParticipant participant2 = participantService.joinDraft(testDraft.getId(), "Bob", "B");
        DraftParticipant participant3 = participantService.joinDraft(testDraft.getId(), "Charlie", "C");

        // Verify all participants were created
        assertNotNull(participant1);
        assertNotNull(participant2);
        assertNotNull(participant3);
        assertEquals(3, participantService.getParticipants(testDraft.getId()).size());
    }

    @Test
    void joinDraft_ThrowsExceptionWhenPositionAlreadyTaken() {
        // Join first participant
        participantService.joinDraft(testDraft.getId(), "Alice", "A");

        // Attempt to join with same position
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            participantService.joinDraft(testDraft.getId(), "Bob", "A");
        });

        // Verify error message
        assertTrue(exception.getMessage().contains("Position A is already taken"));
    }

    @Test
    void joinDraft_ThrowsExceptionWhenNicknameAlreadyTaken() {
        // Join first participant
        participantService.joinDraft(testDraft.getId(), "Alice", "A");

        // Attempt to join with same nickname
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            participantService.joinDraft(testDraft.getId(), "Alice", "B");
        });

        // Verify error message
        assertTrue(exception.getMessage().contains("Nickname 'Alice' is already taken"));
    }

    @Test
    void joinDraft_ThrowsExceptionWhenLobbyIsFull() {
        // Fill all 4 slots
        participantService.joinDraft(testDraft.getId(), "Alice", "A");
        participantService.joinDraft(testDraft.getId(), "Bob", "B");
        participantService.joinDraft(testDraft.getId(), "Charlie", "C");
        participantService.joinDraft(testDraft.getId(), "Diana", "D");

        // Attempt to join when full
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            participantService.joinDraft(testDraft.getId(), "Eve", "E");
        });

        // Verify error message
        assertTrue(exception.getMessage().contains("lobby is full"));
    }

    @Test
    void joinDraft_ThrowsExceptionWhenDraftNotInLobbyStatus() {
        // Change draft status to IN_PROGRESS
        testDraft.setStatus("IN_PROGRESS");
        draftRepository.save(testDraft);

        // Attempt to join
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            participantService.joinDraft(testDraft.getId(), "Alice", "A");
        });

        // Verify error message
        assertTrue(exception.getMessage().contains("not in LOBBY status"));
    }

    @Test
    void joinDraft_ThrowsExceptionForInvalidDraftId() {
        // Attempt to join non-existent draft
        assertThrows(DraftNotFoundException.class, () -> {
            participantService.joinDraft(99999L, "Alice", "A");
        });
    }

    @Test
    void joinDraft_ThrowsExceptionForInvalidPositionFormat() {
        // Test lowercase letter
        ValidationException exception1 = assertThrows(ValidationException.class, () -> {
            participantService.joinDraft(testDraft.getId(), "Alice", "a");
        });
        assertTrue(exception1.getMessage().contains("must be a single uppercase letter"));

        // Test multiple letters
        ValidationException exception2 = assertThrows(ValidationException.class, () -> {
            participantService.joinDraft(testDraft.getId(), "Bob", "AB");
        });
        assertTrue(exception2.getMessage().contains("must be a single uppercase letter"));

        // Test number
        ValidationException exception3 = assertThrows(ValidationException.class, () -> {
            participantService.joinDraft(testDraft.getId(), "Charlie", "1");
        });
        assertTrue(exception3.getMessage().contains("must be a single uppercase letter"));
    }

    @Test
    void joinDraft_ThrowsExceptionForPositionOutOfRange() {
        // Attempt to join position E when only 4 participants allowed
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            participantService.joinDraft(testDraft.getId(), "Alice", "E");
        });

        // Verify error message
        assertTrue(exception.getMessage().contains("not valid for a draft with 4 participants"));
    }

    @Test
    void joinDraft_ThrowsExceptionForEmptyNickname() {
        // Test null nickname
        ValidationException exception1 = assertThrows(ValidationException.class, () -> {
            participantService.joinDraft(testDraft.getId(), null, "A");
        });
        assertTrue(exception1.getMessage().contains("cannot be empty"));

        // Test empty nickname
        ValidationException exception2 = assertThrows(ValidationException.class, () -> {
            participantService.joinDraft(testDraft.getId(), "", "A");
        });
        assertTrue(exception2.getMessage().contains("cannot be empty"));

        // Test whitespace-only nickname
        ValidationException exception3 = assertThrows(ValidationException.class, () -> {
            participantService.joinDraft(testDraft.getId(), "   ", "A");
        });
        assertTrue(exception3.getMessage().contains("cannot be empty"));
    }

    @Test
    void joinDraft_ThrowsExceptionForNicknameTooShort() {
        // Test 1-character nickname
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            participantService.joinDraft(testDraft.getId(), "A", "A");
        });

        // Verify error message
        assertTrue(exception.getMessage().contains("must be between 2 and 50 characters"));
    }

    @Test
    void joinDraft_ThrowsExceptionForNicknameTooLong() {
        // Test 51-character nickname
        String longNickname = "A".repeat(51);
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            participantService.joinDraft(testDraft.getId(), longNickname, "A");
        });

        // Verify error message
        assertTrue(exception.getMessage().contains("must be between 2 and 50 characters"));
    }

    @Test
    void joinDraft_TrimsNicknameWhitespace() {
        // Join with nickname that has leading/trailing whitespace
        DraftParticipant participant = participantService.joinDraft(testDraft.getId(), "  Alice  ", "A");

        // Verify nickname is trimmed
        assertEquals("Alice", participant.getNickname());
    }

    @Test
    void joinDraft_NicknameComparisonIsCaseInsensitive() {
        // Join first participant
        participantService.joinDraft(testDraft.getId(), "Alice", "A");

        // Attempt to join with different case
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            participantService.joinDraft(testDraft.getId(), "ALICE", "B");
        });

        // Verify error message
        assertTrue(exception.getMessage().contains("Nickname 'ALICE' is already taken"));
    }

    // ========== setReady Tests ==========

    @Test
    void setReady_TogglesReadyStatusToTrue() {
        // Join participant
        participantService.joinDraft(testDraft.getId(), "Alice", "A");

        // Set ready to true with valid PIN
        DraftParticipant participant = participantService.setReady(testDraft.getId(), "A", true, "1234");

        // Verify ready status and verification
        assertTrue(participant.getIsReady());
        assertTrue(participant.getIsVerified());
    }

    @Test
    void setReady_TogglesReadyStatusToFalse() {
        // Join participant and set ready
        participantService.joinDraft(testDraft.getId(), "Alice", "A");
        participantService.setReady(testDraft.getId(), "A", true, "1234");

        // Set ready to false
        DraftParticipant participant = participantService.setReady(testDraft.getId(), "A", false, null);

        // Verify ready status
        assertFalse(participant.getIsReady());
    }

    @Test
    void setReady_ThrowsExceptionForNonExistentParticipant() {
        // Attempt to set ready for non-existent participant
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            participantService.setReady(testDraft.getId(), "A", true, null);
        });

        // Verify error message
        assertTrue(exception.getMessage().contains("Participant not found at position A"));
    }

    @Test
    void setReady_WorksForMultipleParticipants() {
        // Join multiple participants
        participantService.joinDraft(testDraft.getId(), "Alice", "A");
        participantService.joinDraft(testDraft.getId(), "Bob", "B");

        // Set both ready with valid PIN
        participantService.setReady(testDraft.getId(), "A", true, "1234");
        participantService.setReady(testDraft.getId(), "B", true, "1234");

        // Verify both are ready
        List<DraftParticipant> participants = participantService.getParticipants(testDraft.getId());
        assertTrue(participants.stream().allMatch(DraftParticipant::getIsReady));
    }

    // ========== leaveDraft Tests ==========

    @Test
    void leaveDraft_RemovesParticipantFromDraft() {
        // Join participant
        participantService.joinDraft(testDraft.getId(), "Alice", "A");

        // Leave draft
        participantService.leaveDraft(testDraft.getId(), "A");

        // Verify participant is removed
        List<DraftParticipant> participants = participantService.getParticipants(testDraft.getId());
        assertEquals(0, participants.size());
    }

    @Test
    void leaveDraft_ThrowsExceptionForNonExistentParticipant() {
        // Attempt to leave when not joined
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            participantService.leaveDraft(testDraft.getId(), "A");
        });

        // Verify error message
        assertTrue(exception.getMessage().contains("Participant not found at position A"));
    }

    @Test
    void leaveDraft_AllowsPositionToBeReused() {
        // Join and leave
        participantService.joinDraft(testDraft.getId(), "Alice", "A");
        participantService.leaveDraft(testDraft.getId(), "A");

        // Join again with same position
        DraftParticipant newParticipant = participantService.joinDraft(testDraft.getId(), "Bob", "A");

        // Verify new participant was created
        assertNotNull(newParticipant);
        assertEquals("Bob", newParticipant.getNickname());
        assertEquals("A", newParticipant.getPosition());
    }

    @Test
    void leaveDraft_AllowsNicknameToBeReused() {
        // Join and leave
        participantService.joinDraft(testDraft.getId(), "Alice", "A");
        participantService.leaveDraft(testDraft.getId(), "A");

        // Join again with same nickname
        DraftParticipant newParticipant = participantService.joinDraft(testDraft.getId(), "Alice", "B");

        // Verify new participant was created
        assertNotNull(newParticipant);
        assertEquals("Alice", newParticipant.getNickname());
        assertEquals("B", newParticipant.getPosition());
    }

    // ========== getParticipants Tests ==========

    @Test
    void getParticipants_ReturnsEmptyListWhenNoParticipants() {
        // Get participants for empty draft
        List<DraftParticipant> participants = participantService.getParticipants(testDraft.getId());

        // Verify empty list
        assertNotNull(participants);
        assertEquals(0, participants.size());
    }

    @Test
    void getParticipants_ReturnsAllParticipants() {
        // Join multiple participants
        participantService.joinDraft(testDraft.getId(), "Alice", "A");
        participantService.joinDraft(testDraft.getId(), "Bob", "B");
        participantService.joinDraft(testDraft.getId(), "Charlie", "C");

        // Get participants
        List<DraftParticipant> participants = participantService.getParticipants(testDraft.getId());

        // Verify all participants returned
        assertEquals(3, participants.size());
    }

    @Test
    void getParticipants_OnlyReturnsParticipantsForSpecificDraft() {
        // Create second draft
        Draft draft2 = draftService.createLiveDraft("Draft 2", "Creator", 4, 10, "1234", false);

        // Join participants to both drafts
        participantService.joinDraft(testDraft.getId(), "Alice", "A");
        participantService.joinDraft(testDraft.getId(), "Bob", "B");
        participantService.joinDraft(draft2.getId(), "Charlie", "A");

        // Get participants for first draft
        List<DraftParticipant> participants = participantService.getParticipants(testDraft.getId());

        // Verify only participants from first draft returned
        assertEquals(2, participants.size());
        assertTrue(participants.stream().allMatch(p -> p.getDraft().getId().equals(testDraft.getId())));
    }

    // ========== isPositionAvailable Tests ==========

    @Test
    void isPositionAvailable_ReturnsTrueWhenPositionNotTaken() {
        // Check position availability
        boolean available = participantService.isPositionAvailable(testDraft.getId(), "A");

        // Verify position is available
        assertTrue(available);
    }

    @Test
    void isPositionAvailable_ReturnsFalseWhenPositionTaken() {
        // Join participant
        participantService.joinDraft(testDraft.getId(), "Alice", "A");

        // Check position availability
        boolean available = participantService.isPositionAvailable(testDraft.getId(), "A");

        // Verify position is not available
        assertFalse(available);
    }

    @Test
    void isPositionAvailable_ReturnsTrueForDifferentPosition() {
        // Join participant at position A
        participantService.joinDraft(testDraft.getId(), "Alice", "A");

        // Check position B availability
        boolean available = participantService.isPositionAvailable(testDraft.getId(), "B");

        // Verify position B is available
        assertTrue(available);
    }

    @Test
    void isPositionAvailable_ReturnsTrueAfterParticipantLeaves() {
        // Join and leave
        participantService.joinDraft(testDraft.getId(), "Alice", "A");
        participantService.leaveDraft(testDraft.getId(), "A");

        // Check position availability
        boolean available = participantService.isPositionAvailable(testDraft.getId(), "A");

        // Verify position is available again
        assertTrue(available);
    }

    // ========== isNicknameAvailable Tests ==========

    @Test
    void isNicknameAvailable_ReturnsTrueWhenNicknameNotTaken() {
        // Check nickname availability
        boolean available = participantService.isNicknameAvailable(testDraft.getId(), "Alice");

        // Verify nickname is available
        assertTrue(available);
    }

    @Test
    void isNicknameAvailable_ReturnsFalseWhenNicknameTaken() {
        // Join participant
        participantService.joinDraft(testDraft.getId(), "Alice", "A");

        // Check nickname availability
        boolean available = participantService.isNicknameAvailable(testDraft.getId(), "Alice");

        // Verify nickname is not available
        assertFalse(available);
    }

    @Test
    void isNicknameAvailable_IsCaseInsensitive() {
        // Join participant
        participantService.joinDraft(testDraft.getId(), "Alice", "A");

        // Check nickname availability with different case
        boolean available = participantService.isNicknameAvailable(testDraft.getId(), "ALICE");

        // Verify nickname is not available
        assertFalse(available);
    }

    @Test
    void isNicknameAvailable_TrimsWhitespace() {
        // Join participant
        participantService.joinDraft(testDraft.getId(), "Alice", "A");

        // Check nickname availability with whitespace
        boolean available = participantService.isNicknameAvailable(testDraft.getId(), "  Alice  ");

        // Verify nickname is not available
        assertFalse(available);
    }

    @Test
    void isNicknameAvailable_ReturnsTrueForDifferentNickname() {
        // Join participant
        participantService.joinDraft(testDraft.getId(), "Alice", "A");

        // Check different nickname availability
        boolean available = participantService.isNicknameAvailable(testDraft.getId(), "Bob");

        // Verify nickname is available
        assertTrue(available);
    }

    @Test
    void isNicknameAvailable_ReturnsTrueAfterParticipantLeaves() {
        // Join and leave
        participantService.joinDraft(testDraft.getId(), "Alice", "A");
        participantService.leaveDraft(testDraft.getId(), "A");

        // Check nickname availability
        boolean available = participantService.isNicknameAvailable(testDraft.getId(), "Alice");

        // Verify nickname is available again
        assertTrue(available);
    }

    @Test
    void isNicknameAvailable_OnlyChecksSpecificDraft() {
        // Create second draft
        Draft draft2 = draftService.createLiveDraft("Draft 2", "Creator", 4, 10, "1234", false);

        // Join participant to first draft
        participantService.joinDraft(testDraft.getId(), "Alice", "A");

        // Check nickname availability in second draft
        boolean available = participantService.isNicknameAvailable(draft2.getId(), "Alice");

        // Verify nickname is available in second draft
        assertTrue(available);
    }
}

