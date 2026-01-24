package devybigboard.dao;

import devybigboard.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    /**
     * Find all players where verified status is true.
     * This method returns only verified players that are available for the draft pool.
     * 
     * @return List of verified players
     */
    List<Player> findByVerifiedTrue();
}
