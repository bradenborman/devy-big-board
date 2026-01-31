package devybigboard.dao;

import devybigboard.models.DraftParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing DraftParticipant entities.
 * Provides custom query methods for participant management in live drafts.
 */
@Repository
public interface DraftParticipantRepository extends JpaRepository<DraftParticipant, Long> {
    
    /**
     * Find all participants for a specific draft.
     * 
     * @param draftId The ID of the draft
     * @return List of participants in the draft
     */
    List<DraftParticipant> findByDraftId(Long draftId);
    
    /**
     * Find a specific participant by draft ID and position.
     * 
     * @param draftId The ID of the draft
     * @param position The position (A-Z letter) of the participant
     * @return Optional containing the participant if found
     */
    Optional<DraftParticipant> findByDraftIdAndPosition(Long draftId, String position);
    
    /**
     * Count participants in a draft by their ready status.
     * Useful for checking if all participants are ready before starting the draft.
     * 
     * @param draftId The ID of the draft
     * @param isReady The ready status to count (true for ready, false for not ready)
     * @return The count of participants matching the ready status
     */
    long countByDraftIdAndIsReady(Long draftId, boolean isReady);
}
