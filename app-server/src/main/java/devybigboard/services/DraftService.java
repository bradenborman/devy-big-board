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
     * Save a completed draft with UUID generation and ADP updates.
     * Generates a unique UUID for the draft and updates ADP for all picked players.
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
            
            // Update ADP for each picked player
            playerService.updateADP(pickDTO.getPlayerId(), pickDTO.getPickNumber());
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