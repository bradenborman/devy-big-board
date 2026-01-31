package devybigboard.models;

import devybigboard.dao.DraftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DraftParticipant entity.
 * Tests entity creation, relationships, and unique constraints.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DraftParticipantTest {

    @Autowired
    private DraftRepository draftRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Draft testDraft;

    @BeforeEach
    void setUp() {
        // Create a test draft for participants
        testDraft = new Draft();
        testDraft.setUuid(UUID.randomUUID().toString());
        testDraft.setDraftName("Test Live Draft");
        testDraft.setStatus("LOBBY");
        testDraft.setParticipantCount(4);
        testDraft.setTotalRounds(10);
        testDraft = draftRepository.save(testDraft);
    }

    @Test
    void draftParticipant_CanBeCreatedWithAllFields() {
        // Create a participant
        DraftParticipant participant = new DraftParticipant();
        participant.setDraft(testDraft);
        participant.setPosition("A");
        participant.setNickname("Alice");
        participant.setIsReady(false);
        participant.setJoinedAt(LocalDateTime.now());

        // Add to draft
        testDraft.addParticipant(participant);
        Draft savedDraft = draftRepository.save(testDraft);

        // Verify participant was saved
        assertNotNull(savedDraft.getParticipants());
        assertEquals(1, savedDraft.getParticipants().size());
        
        DraftParticipant savedParticipant = savedDraft.getParticipants().get(0);
        assertNotNull(savedParticipant.getId(), "Participant ID should be generated");
        assertEquals("A", savedParticipant.getPosition());
        assertEquals("Alice", savedParticipant.getNickname());
        assertFalse(savedParticipant.getIsReady());
        assertNotNull(savedParticipant.getJoinedAt());
    }

    @Test
    void draftParticipant_ConstructorSetsDefaultValues() {
        // Create participant using constructor
        DraftParticipant participant = new DraftParticipant(testDraft, "B", "Bob");

        // Verify defaults
        assertEquals(testDraft, participant.getDraft());
        assertEquals("B", participant.getPosition());
        assertEquals("Bob", participant.getNickname());
        assertFalse(participant.getIsReady(), "isReady should default to false");
        assertNotNull(participant.getJoinedAt(), "joinedAt should be set automatically");
    }

    @Test
    void draftParticipant_UniqueConstraint_PositionPerDraft() {
        // Create first participant with position A
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        testDraft.addParticipant(participant1);
        draftRepository.save(testDraft);
        entityManager.flush();

        // Try to create second participant with same position
        DraftParticipant participant2 = new DraftParticipant(testDraft, "A", "Bob");
        testDraft.addParticipant(participant2);

        // Should throw constraint violation
        assertThrows(DataIntegrityViolationException.class, () -> {
            draftRepository.save(testDraft);
            entityManager.flush();
        }, "Should not allow duplicate position in same draft");
    }

    @Test
    void draftParticipant_UniqueConstraint_NicknamePerDraft() {
        // Create first participant with nickname Alice
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        testDraft.addParticipant(participant1);
        draftRepository.save(testDraft);
        entityManager.flush();

        // Try to create second participant with same nickname
        DraftParticipant participant2 = new DraftParticipant(testDraft, "B", "Alice");
        testDraft.addParticipant(participant2);

        // Should throw constraint violation
        assertThrows(DataIntegrityViolationException.class, () -> {
            draftRepository.save(testDraft);
            entityManager.flush();
        }, "Should not allow duplicate nickname in same draft");
    }

    @Test
    void draftParticipant_SamePositionInDifferentDrafts_IsAllowed() {
        // Create second draft
        Draft draft2 = new Draft();
        draft2.setUuid(UUID.randomUUID().toString());
        draft2.setDraftName("Second Draft");
        draft2.setStatus("LOBBY");
        draft2.setParticipantCount(4);
        Draft savedDraft2 = draftRepository.save(draft2);

        // Create participants with same position in different drafts
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        DraftParticipant participant2 = new DraftParticipant(savedDraft2, "A", "Bob");

        testDraft.addParticipant(participant1);
        savedDraft2.addParticipant(participant2);

        // Should not throw exception
        assertDoesNotThrow(() -> {
            draftRepository.save(testDraft);
            draftRepository.save(savedDraft2);
            entityManager.flush();
        }, "Same position in different drafts should be allowed");
    }

    @Test
    void draftParticipant_SameNicknameInDifferentDrafts_IsAllowed() {
        // Create second draft
        Draft draft2 = new Draft();
        draft2.setUuid(UUID.randomUUID().toString());
        draft2.setDraftName("Second Draft");
        draft2.setStatus("LOBBY");
        draft2.setParticipantCount(4);
        Draft savedDraft2 = draftRepository.save(draft2);

        // Create participants with same nickname in different drafts
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        DraftParticipant participant2 = new DraftParticipant(savedDraft2, "B", "Alice");

        testDraft.addParticipant(participant1);
        savedDraft2.addParticipant(participant2);

        // Should not throw exception
        assertDoesNotThrow(() -> {
            draftRepository.save(testDraft);
            draftRepository.save(savedDraft2);
            entityManager.flush();
        }, "Same nickname in different drafts should be allowed");
    }

    @Test
    void draftParticipant_ReadyStatus_CanBeToggled() {
        // Create participant
        DraftParticipant participant = new DraftParticipant(testDraft, "A", "Alice");
        testDraft.addParticipant(participant);
        Draft savedDraft = draftRepository.save(testDraft);

        DraftParticipant savedParticipant = savedDraft.getParticipants().get(0);
        assertFalse(savedParticipant.getIsReady(), "Should start as not ready");

        // Toggle to ready
        savedParticipant.setIsReady(true);
        savedDraft = draftRepository.save(savedDraft);
        assertTrue(savedDraft.getParticipants().get(0).getIsReady(), "Should be ready");

        // Toggle back to not ready
        savedParticipant.setIsReady(false);
        savedDraft = draftRepository.save(savedDraft);
        assertFalse(savedDraft.getParticipants().get(0).getIsReady(), "Should be not ready again");
    }

    @Test
    void draftParticipant_MultipleParticipants_CanExistInSameDraft() {
        // Create multiple participants
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        DraftParticipant participant2 = new DraftParticipant(testDraft, "B", "Bob");
        DraftParticipant participant3 = new DraftParticipant(testDraft, "C", "Charlie");

        testDraft.addParticipant(participant1);
        testDraft.addParticipant(participant2);
        testDraft.addParticipant(participant3);

        Draft savedDraft = draftRepository.save(testDraft);

        // Verify all participants were saved
        assertEquals(3, savedDraft.getParticipants().size());
        
        // Verify each participant has correct data
        assertTrue(savedDraft.getParticipants().stream()
            .anyMatch(p -> p.getPosition().equals("A") && p.getNickname().equals("Alice")));
        assertTrue(savedDraft.getParticipants().stream()
            .anyMatch(p -> p.getPosition().equals("B") && p.getNickname().equals("Bob")));
        assertTrue(savedDraft.getParticipants().stream()
            .anyMatch(p -> p.getPosition().equals("C") && p.getNickname().equals("Charlie")));
    }

    @Test
    void draftParticipant_CascadeDelete_ParticipantsDeletedWithDraft() {
        // Create participants
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        DraftParticipant participant2 = new DraftParticipant(testDraft, "B", "Bob");

        testDraft.addParticipant(participant1);
        testDraft.addParticipant(participant2);
        Draft savedDraft = draftRepository.save(testDraft);

        Long draftId = savedDraft.getId();
        assertEquals(2, savedDraft.getParticipants().size());

        // Delete the draft
        draftRepository.deleteById(draftId);
        entityManager.flush();

        // Verify draft is deleted
        assertFalse(draftRepository.findById(draftId).isPresent(), "Draft should be deleted");
        
        // Participants should also be deleted due to cascade
        Long participantCount = entityManager.createQuery(
            "SELECT COUNT(p) FROM DraftParticipant p WHERE p.draft.id = :draftId", Long.class)
            .setParameter("draftId", draftId)
            .getSingleResult();
        assertEquals(0L, participantCount, "Participants should be deleted with draft");
    }

    @Test
    void draftParticipant_RemoveParticipant_RemovesFromDraft() {
        // Create participants
        DraftParticipant participant1 = new DraftParticipant(testDraft, "A", "Alice");
        DraftParticipant participant2 = new DraftParticipant(testDraft, "B", "Bob");

        testDraft.addParticipant(participant1);
        testDraft.addParticipant(participant2);
        Draft savedDraft = draftRepository.save(testDraft);
        entityManager.flush();
        entityManager.clear();

        // Reload the draft
        savedDraft = draftRepository.findById(savedDraft.getId()).orElseThrow();
        assertEquals(2, savedDraft.getParticipants().size());

        // Remove one participant using clear and re-add
        DraftParticipant keepParticipant = savedDraft.getParticipants().stream()
            .filter(p -> p.getPosition().equals("B"))
            .findFirst()
            .orElseThrow();
        
        savedDraft.getParticipants().clear();
        entityManager.flush();
        
        // Add back only one participant
        DraftParticipant newParticipant = new DraftParticipant(savedDraft, "B", "Bob");
        savedDraft.addParticipant(newParticipant);
        savedDraft = draftRepository.save(savedDraft);
        entityManager.flush();

        // Verify only one participant remains
        assertEquals(1, savedDraft.getParticipants().size());
    }

    @Test
    void draftParticipant_PositionField_IsLimitedToOneCharacter() {
        // Create participant with single character position
        DraftParticipant participant = new DraftParticipant(testDraft, "Z", "Zoe");
        testDraft.addParticipant(participant);
        
        // Should save successfully
        assertDoesNotThrow(() -> {
            draftRepository.save(testDraft);
            entityManager.flush();
        });
    }

    @Test
    void draftParticipant_NicknameField_CanBe50Characters() {
        // Create participant with 50 character nickname
        String longNickname = "A".repeat(50);
        DraftParticipant participant = new DraftParticipant(testDraft, "A", longNickname);
        testDraft.addParticipant(participant);
        
        Draft savedDraft = draftRepository.save(testDraft);
        entityManager.flush();

        // Verify it was saved
        assertEquals(longNickname, savedDraft.getParticipants().get(0).getNickname());
    }
}
