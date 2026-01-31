package devybigboard.dao;

import devybigboard.models.Draft;
import devybigboard.models.DraftParticipant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DraftParticipantRepository.
 * Tests custom query methods for participant management.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DraftParticipantRepositoryTest {

    @Autowired
    private DraftParticipantRepository participantRepository;

    @Autowired
    private DraftRepository draftRepository;

    private Draft testDraft;

    @BeforeEach
    void setUp() {
        // Create a test draft
        testDraft = new Draft();
        testDraft.setUuid(UUID.randomUUID().toString());
        testDraft.setDraftName("Test Live Draft");
        testDraft.setStatus("LOBBY");
        testDraft.setParticipantCount(4);
        testDraft.setTotalRounds(10);
        testDraft = draftRepository.save(testDraft);
    }

    @Test
    void findByDraftId_ReturnsAllParticipantsForDraft() {
        // Create multiple participants
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        DraftParticipant participant2 = new DraftParticipant(testDraft, "B", "Bob");
        DraftParticipant participant3 = new DraftParticipant(testDraft, "C", "Charlie");

        testDraft.addParticipant(participant1);
        testDraft.addParticipant(participant2);
        testDraft.addParticipant(participant3);
        draftRepository.save(testDraft);

        // Query by draft ID
        List<DraftParticipant> participants = participantRepository.findByDraftId(testDraft.getId());

        // Verify all participants are returned
        assertNotNull(participants);
        assertEquals(3, participants.size());
        
        // Verify participants have correct data
        assertTrue(participants.stream().anyMatch(p -> p.getPosition().equals("A") && p.getNickname().equals("Alice")));
        assertTrue(participants.stream().anyMatch(p -> p.getPosition().equals("B") && p.getNickname().equals("Bob")));
        assertTrue(participants.stream().anyMatch(p -> p.getPosition().equals("C") && p.getNickname().equals("Charlie")));
    }

    @Test
    void findByDraftId_ReturnsEmptyListWhenNoParticipants() {
        // Query draft with no participants
        List<DraftParticipant> participants = participantRepository.findByDraftId(testDraft.getId());

        // Verify empty list is returned
        assertNotNull(participants);
        assertTrue(participants.isEmpty());
    }

    @Test
    void findByDraftId_OnlyReturnsParticipantsForSpecificDraft() {
        // Create second draft
        Draft draft2 = new Draft();
        draft2.setUuid(UUID.randomUUID().toString());
        draft2.setDraftName("Second Draft");
        draft2.setStatus("LOBBY");
        draft2.setParticipantCount(4);
        draft2 = draftRepository.save(draft2);

        // Add participants to both drafts
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        DraftParticipant participant2 = new DraftParticipant(draft2, "A", "Bob");

        testDraft.addParticipant(participant1);
        draft2.addParticipant(participant2);
        draftRepository.save(testDraft);
        draftRepository.save(draft2);

        // Query first draft
        List<DraftParticipant> participants = participantRepository.findByDraftId(testDraft.getId());

        // Verify only first draft's participant is returned
        assertEquals(1, participants.size());
        assertEquals("Alice", participants.get(0).getNickname());
    }

    @Test
    void findByDraftIdAndPosition_ReturnsCorrectParticipant() {
        // Create participants
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        DraftParticipant participant2 = new DraftParticipant(testDraft, "B", "Bob");

        testDraft.addParticipant(participant1);
        testDraft.addParticipant(participant2);
        draftRepository.save(testDraft);

        // Query by draft ID and position
        Optional<DraftParticipant> result = participantRepository.findByDraftIdAndPosition(testDraft.getId(), "A");

        // Verify correct participant is returned
        assertTrue(result.isPresent());
        assertEquals("A", result.get().getPosition());
        assertEquals("Alice", result.get().getNickname());
    }

    @Test
    void findByDraftIdAndPosition_ReturnsEmptyWhenPositionNotFound() {
        // Create participant
        DraftParticipant participant = new DraftParticipant(testDraft, "A", "Alice");
        testDraft.addParticipant(participant);
        draftRepository.save(testDraft);

        // Query for non-existent position
        Optional<DraftParticipant> result = participantRepository.findByDraftIdAndPosition(testDraft.getId(), "Z");

        // Verify empty optional is returned
        assertFalse(result.isPresent());
    }

    @Test
    void findByDraftIdAndPosition_OnlyReturnsParticipantFromSpecificDraft() {
        // Create second draft
        Draft draft2 = new Draft();
        draft2.setUuid(UUID.randomUUID().toString());
        draft2.setDraftName("Second Draft");
        draft2.setStatus("LOBBY");
        draft2.setParticipantCount(4);
        draft2 = draftRepository.save(draft2);

        // Add participants with same position to different drafts
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        DraftParticipant participant2 = new DraftParticipant(draft2, "A", "Bob");

        testDraft.addParticipant(participant1);
        draft2.addParticipant(participant2);
        draftRepository.save(testDraft);
        draftRepository.save(draft2);

        // Query first draft for position A
        Optional<DraftParticipant> result = participantRepository.findByDraftIdAndPosition(testDraft.getId(), "A");

        // Verify correct participant is returned
        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getNickname());
    }

    @Test
    void countByDraftIdAndIsReady_CountsReadyParticipants() {
        // Create participants with different ready states
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        participant1.setIsReady(true);
        
        DraftParticipant participant2 = new DraftParticipant(testDraft, "B", "Bob");
        participant2.setIsReady(true);
        
        DraftParticipant participant3 = new DraftParticipant(testDraft, "C", "Charlie");
        participant3.setIsReady(false);

        testDraft.addParticipant(participant1);
        testDraft.addParticipant(participant2);
        testDraft.addParticipant(participant3);
        draftRepository.save(testDraft);

        // Count ready participants
        long readyCount = participantRepository.countByDraftIdAndIsReady(testDraft.getId(), true);

        // Verify count is correct
        assertEquals(2, readyCount);
    }

    @Test
    void countByDraftIdAndIsReady_CountsNotReadyParticipants() {
        // Create participants with different ready states
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        participant1.setIsReady(true);
        
        DraftParticipant participant2 = new DraftParticipant(testDraft, "B", "Bob");
        participant2.setIsReady(false);
        
        DraftParticipant participant3 = new DraftParticipant(testDraft, "C", "Charlie");
        participant3.setIsReady(false);

        testDraft.addParticipant(participant1);
        testDraft.addParticipant(participant2);
        testDraft.addParticipant(participant3);
        draftRepository.save(testDraft);

        // Count not ready participants
        long notReadyCount = participantRepository.countByDraftIdAndIsReady(testDraft.getId(), false);

        // Verify count is correct
        assertEquals(2, notReadyCount);
    }

    @Test
    void countByDraftIdAndIsReady_ReturnsZeroWhenNoParticipantsMatchStatus() {
        // Create all ready participants
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        participant1.setIsReady(true);
        
        DraftParticipant participant2 = new DraftParticipant(testDraft, "B", "Bob");
        participant2.setIsReady(true);

        testDraft.addParticipant(participant1);
        testDraft.addParticipant(participant2);
        draftRepository.save(testDraft);

        // Count not ready participants (should be zero)
        long notReadyCount = participantRepository.countByDraftIdAndIsReady(testDraft.getId(), false);

        // Verify count is zero
        assertEquals(0, notReadyCount);
    }

    @Test
    void countByDraftIdAndIsReady_ReturnsZeroWhenNoParticipants() {
        // Count ready participants in draft with no participants
        long readyCount = participantRepository.countByDraftIdAndIsReady(testDraft.getId(), true);

        // Verify count is zero
        assertEquals(0, readyCount);
    }

    @Test
    void countByDraftIdAndIsReady_OnlyCountsParticipantsFromSpecificDraft() {
        // Create second draft
        Draft draft2 = new Draft();
        draft2.setUuid(UUID.randomUUID().toString());
        draft2.setDraftName("Second Draft");
        draft2.setStatus("LOBBY");
        draft2.setParticipantCount(4);
        draft2 = draftRepository.save(draft2);

        // Add ready participants to both drafts
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        participant1.setIsReady(true);
        
        DraftParticipant participant2 = new DraftParticipant(draft2, "A", "Bob");
        participant2.setIsReady(true);

        testDraft.addParticipant(participant1);
        draft2.addParticipant(participant2);
        draftRepository.save(testDraft);
        draftRepository.save(draft2);

        // Count ready participants in first draft
        long readyCount = participantRepository.countByDraftIdAndIsReady(testDraft.getId(), true);

        // Verify only first draft's participant is counted
        assertEquals(1, readyCount);
    }

    @Test
    void countByDraftIdAndIsReady_CanBeUsedToCheckIfAllReady() {
        // Create participants
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        participant1.setIsReady(true);
        
        DraftParticipant participant2 = new DraftParticipant(testDraft, "B", "Bob");
        participant2.setIsReady(true);
        
        DraftParticipant participant3 = new DraftParticipant(testDraft, "C", "Charlie");
        participant3.setIsReady(true);

        testDraft.addParticipant(participant1);
        testDraft.addParticipant(participant2);
        testDraft.addParticipant(participant3);
        draftRepository.save(testDraft);

        // Check if all participants are ready
        long totalParticipants = participantRepository.findByDraftId(testDraft.getId()).size();
        long readyCount = participantRepository.countByDraftIdAndIsReady(testDraft.getId(), true);

        // Verify all are ready
        assertEquals(totalParticipants, readyCount);
        assertTrue(readyCount > 0, "Should have participants");
    }
}
