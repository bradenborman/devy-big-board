package devybigboard.services;

import devybigboard.dao.DraftRepository;
import devybigboard.models.Draft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for cleaning up stale lobby drafts.
 * Runs hourly to delete lobbies that are more than 1 hour old and haven't been started.
 */
@Service
public class LobbyCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(LobbyCleanupService.class);
    private static final int LOBBY_TIMEOUT_HOURS = 1;
    
    private final DraftRepository draftRepository;
    
    public LobbyCleanupService(DraftRepository draftRepository) {
        this.draftRepository = draftRepository;
    }
    
    /**
     * Scheduled task that runs every hour to clean up stale lobbies.
     * Deletes lobbies that are more than 1 hour old and haven't been started.
     * The cascade delete will automatically remove all associated participants and picks.
     */
    @Scheduled(cron = "0 0 * * * *") // Run at the top of every hour
    @Transactional
    public void cleanupStaleLobbies() {
        logger.info("Starting scheduled cleanup of stale lobbies");
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(LOBBY_TIMEOUT_HOURS);
        List<Draft> staleLobbies = draftRepository.findStaleLobbyDrafts("LOBBY", cutoffTime);
        
        if (staleLobbies.isEmpty()) {
            logger.info("No stale lobbies found");
            return;
        }
        
        logger.info("Found {} stale lobbies to delete", staleLobbies.size());
        
        for (Draft draft : staleLobbies) {
            try {
                logger.info("Deleting stale lobby: uuid={}, name={}, createdAt={}, participants={}", 
                    draft.getUuid(), 
                    draft.getDraftName(), 
                    draft.getCreatedAt(),
                    draft.getParticipants().size());
                
                // Delete the draft - cascade will handle participants and picks
                draftRepository.delete(draft);
                
            } catch (Exception e) {
                logger.error("Error deleting stale lobby with uuid={}: {}", draft.getUuid(), e.getMessage(), e);
            }
        }
        
        logger.info("Completed cleanup of {} stale lobbies", staleLobbies.size());
    }
}
