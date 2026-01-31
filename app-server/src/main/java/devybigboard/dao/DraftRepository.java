package devybigboard.dao;

import devybigboard.models.Draft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DraftRepository extends JpaRepository<Draft, Long> {
    
    /**
     * Find a draft by its UUID.
     * 
     * @param uuid The unique identifier of the draft
     * @return Optional containing the draft if found
     */
    Optional<Draft> findByUuid(String uuid);
    
    /**
     * Find the top N most recent drafts ordered by creation date descending.
     * 
     * @param limit The maximum number of drafts to return
     * @return List of recent drafts
     */
    @Query("SELECT d FROM Draft d ORDER BY d.createdAt DESC LIMIT :limit")
    List<Draft> findTopNByOrderByCreatedAtDesc(@Param("limit") int limit);
    
    /**
     * Find all drafts with a specific status.
     * 
     * @param status The status to filter by (e.g., "LOBBY", "IN_PROGRESS", "COMPLETED")
     * @return List of drafts with the specified status
     */
    List<Draft> findByStatus(String status);
    
    /**
     * Find all drafts with a specific status ordered by creation date descending.
     * 
     * @param status The status to filter by
     * @return List of drafts with the specified status, most recent first
     */
    List<Draft> findByStatusOrderByCreatedAtDesc(String status);
}
