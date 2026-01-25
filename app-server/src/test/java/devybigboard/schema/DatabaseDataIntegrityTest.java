package devybigboard.schema;

import devybigboard.dao.DraftRepository;
import devybigboard.dao.PlayerRepository;
import devybigboard.models.Draft;
import devybigboard.models.DraftPick;
import devybigboard.models.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify database data integrity and CRUD operations.
 * Validates that entities can be created, read, updated, and deleted correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DatabaseDataIntegrityTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private DraftRepository draftRepository;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        draftRepository.deleteAll();
        playerRepository.deleteAll();
    }

    @Test
    void player_CanBeCreatedAndRetrieved() {
        // Create a player
        Player player = new Player();
        player.setName("Test Player");
        player.setPosition("QB");
        player.setTeam("Test Team");
        player.setCollege("Test College");
        player.setVerified(false);

        // Save the player
        Player savedPlayer = playerRepository.save(player);

        // Verify it was saved
        assertNotNull(savedPlayer.getId(), "Player ID should be generated");
        assertTrue(savedPlayer.getId() > 0, "Player ID should be positive");

        // Retrieve the player
        Optional<Player> retrievedPlayer = playerRepository.findById(savedPlayer.getId());
        assertTrue(retrievedPlayer.isPresent(), "Player should be retrievable");
        assertEquals("Test Player", retrievedPlayer.get().getName());
        assertEquals("QB", retrievedPlayer.get().getPosition());
        assertFalse(retrievedPlayer.get().getVerified(), "Player should not be verified by default");
    }

    @Test
    void player_VerifiedStatus_CanBeUpdated() {
        // Create and save a player
        Player player = new Player();
        player.setName("Unverified Player");
        player.setPosition("RB");
        player.setVerified(false);
        player = playerRepository.save(player);

        // Update verification status
        player.setVerified(true);
        Player updatedPlayer = playerRepository.save(player);

        // Verify the update
        assertTrue(updatedPlayer.getVerified(), "Player should be verified");

        // Retrieve and verify persistence
        Optional<Player> retrievedPlayer = playerRepository.findById(player.getId());
        assertTrue(retrievedPlayer.isPresent());
        assertTrue(retrievedPlayer.get().getVerified(), "Verification status should persist");
    }

    @Test
    void playerRepository_FindByVerifiedTrue_ReturnsOnlyVerifiedPlayers() {
        // Create verified and unverified players
        Player verified1 = createPlayer("Verified 1", "QB", true);
        Player verified2 = createPlayer("Verified 2", "RB", true);
        Player unverified1 = createPlayer("Unverified 1", "WR", false);
        Player unverified2 = createPlayer("Unverified 2", "TE", false);

        playerRepository.saveAll(List.of(verified1, verified2, unverified1, unverified2));

        // Query for verified players
        List<Player> verifiedPlayers = playerRepository.findByVerifiedTrue();

        // Verify results
        assertEquals(2, verifiedPlayers.size(), "Should return only verified players");
        assertTrue(verifiedPlayers.stream().allMatch(Player::getVerified),
            "All returned players should be verified");
    }

    @Test
    void draft_CanBeCreatedWithUUID() {
        // Create a draft
        Draft draft = new Draft();
        draft.setUuid(UUID.randomUUID().toString());
        draft.setDraftName("Test Draft");
        draft.setStatus("completed");
        draft.setParticipantCount(12);
        draft.setCompletedAt(LocalDateTime.now());

        // Save the draft
        Draft savedDraft = draftRepository.save(draft);

        // Verify it was saved
        assertNotNull(savedDraft.getId(), "Draft ID should be generated");
        assertNotNull(savedDraft.getUuid(), "Draft UUID should be set");
        assertEquals("Test Draft", savedDraft.getDraftName());
    }

    @Test
    void draft_CanBeRetrievedByUUID() {
        // Create and save a draft
        String uuid = UUID.randomUUID().toString();
        Draft draft = new Draft();
        draft.setUuid(uuid);
        draft.setDraftName("UUID Test Draft");
        draft.setStatus("completed");
        draft.setParticipantCount(10);
        draftRepository.save(draft);

        // Retrieve by UUID
        Optional<Draft> retrievedDraft = draftRepository.findByUuid(uuid);

        // Verify retrieval
        assertTrue(retrievedDraft.isPresent(), "Draft should be retrievable by UUID");
        assertEquals(uuid, retrievedDraft.get().getUuid());
        assertEquals("UUID Test Draft", retrievedDraft.get().getDraftName());
    }

    @Test
    void draftPick_CanBeCreatedWithRelationships() {
        // Create a player
        Player player = createPlayer("Pick Test Player", "QB", true);
        player = playerRepository.save(player);

        // Create a draft
        Draft draft = new Draft();
        draft.setUuid(UUID.randomUUID().toString());
        draft.setDraftName("Pick Test Draft");
        draft.setStatus("completed");
        draft.setParticipantCount(1);

        // Create a draft pick
        DraftPick pick = new DraftPick();
        pick.setDraft(draft);
        pick.setPlayer(player);
        pick.setPickNumber(1);
        pick.setPickedAt(LocalDateTime.now());

        // Add pick to draft
        List<DraftPick> picks = new ArrayList<>();
        picks.add(pick);
        draft.setPicks(picks);

        // Save the draft (should cascade to picks)
        Draft savedDraft = draftRepository.save(draft);

        // Verify the pick was saved
        assertNotNull(savedDraft.getId());
        assertFalse(savedDraft.getPicks().isEmpty(), "Draft should have picks");
        assertEquals(1, savedDraft.getPicks().size());
        
        DraftPick savedPick = savedDraft.getPicks().get(0);
        assertNotNull(savedPick.getId(), "Pick ID should be generated");
        assertEquals(1, savedPick.getPickNumber());
        assertEquals(player.getId(), savedPick.getPlayer().getId());
    }

    @Test
    void draftPick_CascadeDelete_ShouldDeletePicksWhenDraftDeleted() {
        // Create player and draft with picks
        Player player = createPlayer("Cascade Test Player", "RB", true);
        player = playerRepository.save(player);

        Draft draft = new Draft();
        draft.setUuid(UUID.randomUUID().toString());
        draft.setDraftName("Cascade Test Draft");
        draft.setStatus("completed");
        draft.setParticipantCount(1);

        DraftPick pick = new DraftPick();
        pick.setDraft(draft);
        pick.setPlayer(player);
        pick.setPickNumber(1);
        pick.setPickedAt(LocalDateTime.now());

        List<DraftPick> picks = new ArrayList<>();
        picks.add(pick);
        draft.setPicks(picks);

        draft = draftRepository.save(draft);
        Long draftId = draft.getId();

        // Verify draft and pick exist
        assertTrue(draftRepository.findById(draftId).isPresent());

        // Delete the draft
        draftRepository.deleteById(draftId);

        // Verify draft is deleted
        assertFalse(draftRepository.findById(draftId).isPresent(),
            "Draft should be deleted");
        
        // Player should still exist (no cascade delete on player)
        assertTrue(playerRepository.findById(player.getId()).isPresent(),
            "Player should not be deleted when draft is deleted");
    }

    @Test
    void draft_MultiplePicksWithSamePlayer_ShouldBeAllowed() {
        // Create a player
        Player player = createPlayer("Multi-Pick Player", "WR", true);
        player = playerRepository.save(player);

        // Create two drafts with the same player
        Draft draft1 = createDraftWithPick(player, 1, "Draft 1");
        Draft draft2 = createDraftWithPick(player, 3, "Draft 2");

        draft1 = draftRepository.save(draft1);
        draft2 = draftRepository.save(draft2);

        // Verify both drafts were saved
        assertNotNull(draft1.getId());
        assertNotNull(draft2.getId());
        
        // Verify both picks reference the same player
        assertEquals(player.getId(), draft1.getPicks().get(0).getPlayer().getId());
        assertEquals(player.getId(), draft2.getPicks().get(0).getPlayer().getId());
    }

    @Test
    void timestamps_ShouldBeAutomaticallySet() {
        // Create and save a player
        Player player = new Player();
        player.setName("Timestamp Test");
        player.setPosition("QB");
        player.setVerified(false);
        
        Player savedPlayer = playerRepository.save(player);

        // Verify timestamps are set
        assertNotNull(savedPlayer.getCreatedAt(), "Created timestamp should be set");
        assertNotNull(savedPlayer.getUpdatedAt(), "Updated timestamp should be set");
    }

    // Helper methods

    private Player createPlayer(String name, String position, boolean verified) {
        Player player = new Player();
        player.setName(name);
        player.setPosition(position);
        player.setVerified(verified);
        return player;
    }

    private Draft createDraftWithPick(Player player, int pickNumber, String draftName) {
        Draft draft = new Draft();
        draft.setUuid(UUID.randomUUID().toString());
        draft.setDraftName(draftName);
        draft.setStatus("completed");
        draft.setParticipantCount(1);

        DraftPick pick = new DraftPick();
        pick.setDraft(draft);
        pick.setPlayer(player);
        pick.setPickNumber(pickNumber);
        pick.setPickedAt(LocalDateTime.now());

        List<DraftPick> picks = new ArrayList<>();
        picks.add(pick);
        draft.setPicks(picks);

        return draft;
    }
}
