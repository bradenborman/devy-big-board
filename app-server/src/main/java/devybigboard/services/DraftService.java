package devybigboard.services;

import devybigboard.dao.DraftDao;
import devybigboard.dao.DraftRepository;
import devybigboard.exceptions.DraftNotFoundException;
import devybigboard.models.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DraftService {

    private final DraftDao draftDao;
    private final DraftRepository draftRepository;
    private final PlayerService playerService;

    public DraftService(DraftDao draftDao, DraftRepository draftRepository, PlayerService playerService) {
        this.draftDao = draftDao;
        this.draftRepository = draftRepository;
        this.playerService = playerService;
    }
    
    // ========== Live Draft Methods ==========
    
    /**
     * Create a new live draft in LOBBY status.
     * 
     * @param draftName the name of the draft
     * @param creatorNickname the nickname of the creator
     * @param participantCount the number of participants (2-26)
     * @param totalRounds the total number of rounds
     * @param isSnakeDraft true for snake draft, false for linear draft
     * @return the created draft entity
     */
    @Transactional
    public Draft createLiveDraft(String draftName, String creatorNickname, Integer participantCount, Integer totalRounds, Boolean isSnakeDraft) {
        Draft draft = new Draft();
        draft.setUuid(UUID.randomUUID().toString());
        draft.setDraftName(draftName);
        draft.setCreatedBy(creatorNickname);
        draft.setStatus("LOBBY");
        draft.setParticipantCount(participantCount);
        draft.setTotalRounds(totalRounds);
        draft.setCurrentRound(1);
        draft.setCurrentPick(1);
        draft.setIsSnakeDraft(isSnakeDraft != null ? isSnakeDraft : false);
        
        return draftRepository.save(draft);
    }
    
    /**
     * Get all drafts currently in LOBBY status.
     * 
     * @return list of drafts in lobby status, ordered by creation date (most recent first)
     */
    @Transactional(readOnly = true)
    public List<Draft> getAllLobbyDrafts() {
        return draftRepository.findByStatusOrderByCreatedAtDesc("LOBBY");
    }
    
    /**
     * Get the complete lobby state including draft and all participants.
     * 
     * @param uuid the unique identifier of the draft
     * @return the draft entity with participants loaded
     * @throws DraftNotFoundException if draft does not exist
     */
    @Transactional(readOnly = true)
    public Draft getLobbyState(String uuid) {
        Draft draft = getDraftByUuid(uuid);
        // Force load participants to avoid lazy loading issues
        draft.getParticipants().size();
        return draft;
    }
    
    /**
     * Check if a draft can be started.
     * A draft can start if at least one participant has joined.
     * Participants are automatically ready when they join.
     * 
     * @param uuid the unique identifier of the draft
     * @return true if the draft can be started, false otherwise
     * @throws DraftNotFoundException if draft does not exist
     */
    @Transactional(readOnly = true)
    public boolean canStartDraft(String uuid) {
        Draft draft = getDraftByUuid(uuid);
        
        // Check if at least one participant has joined
        // Since participants are auto-ready when joining, no need to check ready status
        return !draft.getParticipants().isEmpty();
    }
    
    /**
     * Start a draft by changing its status to IN_PROGRESS.
     * 
     * @param uuid the unique identifier of the draft
     * @return the updated draft entity
     * @throws DraftNotFoundException if draft does not exist
     * @throws IllegalStateException if draft cannot be started
     */
    @Transactional
    public Draft startDraft(String uuid) {
        Draft draft = getDraftByUuid(uuid);
        
        if (!"LOBBY".equals(draft.getStatus())) {
            throw new IllegalStateException("Draft is not in LOBBY status");
        }
        
        if (!canStartDraft(uuid)) {
            throw new IllegalStateException("Cannot start draft: not all participants are ready");
        }
        
        draft.setStatus("IN_PROGRESS");
        draft.setStartedAt(LocalDateTime.now());
        
        return draftRepository.save(draft);
    }
    
    /**
     * Get the position letter of the participant whose turn it is.
     * Supports both linear and snake draft modes.
     * 
     * Linear draft: All rounds go A→B→C→D
     * Snake draft: Round 1: A→B→C→D, Round 2: D→C→B→A, Round 3: A→B→C→D, etc.
     * 
     * @param uuid the unique identifier of the draft
     * @return the position letter (A-Z) of the current turn
     * @throws DraftNotFoundException if draft does not exist
     */
    @Transactional(readOnly = true)
    public String getCurrentTurn(String uuid) {
        Draft draft = getDraftByUuid(uuid);
        
        if (!"IN_PROGRESS".equals(draft.getStatus())) {
            return null;
        }
        
        return calculatePickPosition(
            draft.getCurrentRound(),
            draft.getCurrentPick(),
            draft.getParticipantCount(),
            draft.getIsSnakeDraft() != null ? draft.getIsSnakeDraft() : false
        );
    }
    
    /**
     * Calculate which position should pick based on round, pick number, and draft mode.
     * 
     * @param round the current round number (1-indexed)
     * @param pickNumber the overall pick number (1-indexed)
     * @param participantCount the number of participants in the draft
     * @param isSnakeDraft true for snake draft, false for linear draft
     * @return the position letter (A-Z) that should pick
     */
    public String calculatePickPosition(int round, int pickNumber, int participantCount, boolean isSnakeDraft) {
        // Calculate position within the current round (0-indexed)
        int pickInRound = (pickNumber - 1) % participantCount;
        
        int positionIndex;
        
        if (isSnakeDraft) {
            // Snake draft: odd rounds forward, even rounds reverse
            if (round % 2 == 1) {
                // Odd rounds: forward order (A, B, C, D)
                positionIndex = pickInRound;
            } else {
                // Even rounds: reverse order (D, C, B, A)
                positionIndex = participantCount - 1 - pickInRound;
            }
        } else {
            // Linear draft: always forward order (A, B, C, D) for all rounds
            positionIndex = pickInRound;
        }
        
        // Convert to letter (0=A, 1=B, etc.)
        return String.valueOf((char) ('A' + positionIndex));
    }
    
    /**
     * Validate if a specific position can make a pick now.
     * 
     * @param uuid the unique identifier of the draft
     * @param position the position letter (A-Z) to validate
     * @return true if the position can pick now, false otherwise
     * @throws DraftNotFoundException if draft does not exist
     */
    @Transactional(readOnly = true)
    public boolean isValidPick(String uuid, String position) {
        String currentTurn = getCurrentTurn(uuid);
        return currentTurn != null && currentTurn.equals(position);
    }
    
    /**
     * Make a pick in a live draft.
     * Validates draft status, player availability, creates the pick record,
     * and updates draft state (current pick, round, and completion status).
     * 
     * @param uuid the unique identifier of the draft
     * @param playerId the ID of the player being picked
     * @param position the position letter (A-Z) making the pick
     * @return the updated draft entity
     * @throws DraftNotFoundException if draft does not exist
     * @throws IllegalStateException if draft is not in progress
     * @throws IllegalArgumentException if player is not available or already picked
     */
    @Transactional
    public Draft makePick(String uuid, Long playerId, String position) {
        // Get the draft
        Draft draft = getDraftByUuid(uuid);
        
        // Validate draft status is IN_PROGRESS
        if (!"IN_PROGRESS".equals(draft.getStatus())) {
            throw new IllegalStateException("Draft is not in progress");
        }
        
        // Get the player
        Player player = playerService.getPlayerById(playerId);
        
        // Validate player is available (not already picked in this draft)
        boolean playerAlreadyPicked = draft.getPicks().stream()
            .anyMatch(pick -> pick.getPlayer().getId().equals(playerId));
        
        if (playerAlreadyPicked) {
            throw new IllegalArgumentException("Player has already been picked in this draft");
        }
        
        // Create DraftPick record with position and roundNumber
        DraftPick pick = new DraftPick();
        pick.setDraft(draft);
        pick.setPlayer(player);
        pick.setPickNumber(draft.getCurrentPick());
        pick.setPosition(position);
        pick.setRoundNumber(draft.getCurrentRound());
        pick.setPickedAt(LocalDateTime.now());
        
        // Add pick to draft
        draft.addPick(pick);
        
        // Calculate total picks needed
        int totalPicks = draft.getParticipantCount() * draft.getTotalRounds();
        
        // Increment currentPick counter
        int newPickNumber = draft.getCurrentPick() + 1;
        draft.setCurrentPick(newPickNumber);
        
        // Update currentRound based on the NEW pick number (the next pick to be made)
        // If newPickNumber > totalPicks, we're done, so keep current round
        if (newPickNumber <= totalPicks) {
            int picksPerRound = draft.getParticipantCount();
            int newRound = ((newPickNumber - 1) / picksPerRound) + 1;
            draft.setCurrentRound(newRound);
        }
        
        // Set status to COMPLETED if all picks made
        if (newPickNumber > totalPicks) {
            draft.setStatus("COMPLETED");
            draft.setCompletedAt(LocalDateTime.now());
        }
        
        // Save and return updated draft state
        return draftRepository.save(draft);
    }
    
    /**
     * Force a pick in a live draft, bypassing turn validation.
     * Any participant can force a pick for any position, with attribution tracking.
     * 
     * @param uuid the unique identifier of the draft
     * @param playerId the ID of the player being picked
     * @param targetPosition the position letter (A-Z) for whom the pick is being made
     * @param forcingPosition the position letter (A-Z) of the participant forcing the pick
     * @return the updated draft entity
     * @throws DraftNotFoundException if draft does not exist
     * @throws IllegalStateException if draft is not in progress
     * @throws IllegalArgumentException if player is not available or already picked
     */
    @Transactional
    public Draft forcePick(String uuid, Long playerId, String targetPosition, String forcingPosition) {
        // Get the draft
        Draft draft = getDraftByUuid(uuid);
        
        // Validate draft status is IN_PROGRESS
        if (!"IN_PROGRESS".equals(draft.getStatus())) {
            throw new IllegalStateException("Draft is not in progress");
        }
        
        // Get the player
        Player player = playerService.getPlayerById(playerId);
        
        // Validate player is available (not already picked in this draft)
        boolean playerAlreadyPicked = draft.getPicks().stream()
            .anyMatch(pick -> pick.getPlayer().getId().equals(playerId));
        
        if (playerAlreadyPicked) {
            throw new IllegalArgumentException("Player has already been picked in this draft");
        }
        
        // Create DraftPick record with position, roundNumber, and forcedBy
        DraftPick pick = new DraftPick();
        pick.setDraft(draft);
        pick.setPlayer(player);
        pick.setPickNumber(draft.getCurrentPick());
        pick.setPosition(targetPosition);
        pick.setRoundNumber(draft.getCurrentRound());
        pick.setForcedBy(forcingPosition);
        pick.setPickedAt(LocalDateTime.now());
        
        // Add pick to draft
        draft.addPick(pick);
        
        // Calculate total picks needed
        int totalPicks = draft.getParticipantCount() * draft.getTotalRounds();
        
        // Increment currentPick counter
        int newPickNumber = draft.getCurrentPick() + 1;
        draft.setCurrentPick(newPickNumber);
        
        // Update currentRound based on the NEW pick number (the next pick to be made)
        // If newPickNumber > totalPicks, we're done, so keep current round
        if (newPickNumber <= totalPicks) {
            int picksPerRound = draft.getParticipantCount();
            int newRound = ((newPickNumber - 1) / picksPerRound) + 1;
            draft.setCurrentRound(newRound);
        }
        
        // Set status to COMPLETED if all picks made
        if (newPickNumber > totalPicks) {
            draft.setStatus("COMPLETED");
            draft.setCompletedAt(LocalDateTime.now());
        }
        
        // Save and return updated draft state
        return draftRepository.save(draft);
    }
    
    /**
     * Get the complete draft state for WebSocket synchronization.
     * Returns all picks, current turn, available players, participants with nicknames and positions,
     * and pick history with forced pick attributions.
     * 
     * @param uuid the unique identifier of the draft
     * @return a DraftState object containing complete draft information
     * @throws DraftNotFoundException if draft does not exist
     */
    @Transactional(readOnly = true)
    public DraftState getDraftState(String uuid) {
        // Get the draft with all relationships loaded
        Draft draft = getDraftByUuid(uuid);
        
        // Force load participants and picks to avoid lazy loading issues
        draft.getParticipants().size();
        draft.getPicks().size();
        
        // Get all verified players
        List<Player> allPlayers = playerService.getVerifiedPlayers();
        
        // Get IDs of already picked players
        List<Long> pickedPlayerIds = draft.getPicks().stream()
            .map(pick -> pick.getPlayer().getId())
            .toList();
        
        // Filter to get available players (not yet picked)
        List<Player> availablePlayers = allPlayers.stream()
            .filter(player -> !pickedPlayerIds.contains(player.getId()))
            .toList();
        
        // Get current turn position
        String currentTurnPosition = getCurrentTurn(uuid);
        
        // Build and return the draft state
        return new DraftState(
            draft.getUuid(),
            draft.getDraftName(),
            draft.getStatus(),
            draft.getCurrentRound(),
            draft.getCurrentPick(),
            draft.getTotalRounds(),
            draft.getParticipantCount(),
            draft.getIsSnakeDraft(),
            currentTurnPosition,
            draft.getParticipants(),
            draft.getPicks(),
            availablePlayers,
            draft.getStartedAt(),
            draft.getCompletedAt()
        );
    }

    public Integer draftsCompletedCount() {
        return draftDao.draftsCompletedCount();
    }

    public List<LeagueFilter> getAllLeagueFilters() {
        return draftDao.getAllLeagueFilters();
    }

    public long createLeagueFilter(String leagueName) {
        return draftDao.createFilter(leagueName);
    }

    public void addPlayerToFilter(long filterId, Player player) {
        draftDao.addPlayerToFilter(filterId, player.getName(), player.getPosition(), player.getTeam());
    }

    public void removePlayerFromFilter(long filterId, Player player) {
        draftDao.removePlayerFromFilter(filterId, player.getName(), player.getPosition(), player.getTeam());
    }

    public void deleteLeagueFilter(long filterId) {
        draftDao.deleteFilter(filterId);
    }

    /**
     * Save a completed draft with UUID generation.
     * Generates a unique UUID for the draft.
     * 
     * @param draftDTO the draft data transfer object
     * @return the saved draft entity
     */
    @Transactional
    public Draft saveDraft(DraftDTO draftDTO) {
        // Create draft entity
        Draft draft = new Draft();
        draft.setUuid(UUID.randomUUID().toString());
        draft.setDraftName(draftDTO.getDraftName());
        draft.setStatus("completed");
        draft.setParticipantCount(draftDTO.getParticipantCount() != null ? draftDTO.getParticipantCount() : 1);
        draft.setCompletedAt(LocalDateTime.now());
        
        // Create draft picks
        List<DraftPick> picks = new ArrayList<>();
        for (PickDTO pickDTO : draftDTO.getPicks()) {
            Player player = playerService.getPlayerById(pickDTO.getPlayerId());
            
            DraftPick pick = new DraftPick();
            pick.setDraft(draft);
            pick.setPlayer(player);
            pick.setPickNumber(pickDTO.getPickNumber());
            pick.setPickedAt(LocalDateTime.now());
            picks.add(pick);
        }
        
        draft.setPicks(picks);
        return draftRepository.save(draft);
    }
    
    /**
     * Retrieve a draft by its UUID.
     * 
     * @param uuid the unique identifier of the draft
     * @return the draft entity
     * @throws DraftNotFoundException if draft does not exist
     */
    public Draft getDraftByUuid(String uuid) {
        return draftRepository.findByUuid(uuid)
            .orElseThrow(() -> new DraftNotFoundException(uuid));
    }
    
    /**
     * Get the most recent drafts ordered by creation date.
     * 
     * @param limit the maximum number of drafts to return
     * @return list of recent drafts
     */
    public List<Draft> getRecentDrafts(int limit) {
        return draftRepository.findTopNByOrderByCreatedAtDesc(limit);
    }

}