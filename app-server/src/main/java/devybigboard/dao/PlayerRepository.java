package devybigboard.dao;

import devybigboard.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByVerifiedTrue();

    /** Players with no CFBD ID linked yet — need name search + initial stat fetch. */
    @Query("SELECT p FROM Player p WHERE p.cfbdPlayerId IS NULL OR p.cfbdPlayerId = ''")
    List<Player> findPlayersWithoutCfbdId();

    /** Players whose stats haven't been synced, or were last synced before the given cutoff. */
    @Query("SELECT p FROM Player p WHERE p.cfbdPlayerId IS NOT NULL AND p.cfbdPlayerId <> '' " +
           "AND (p.statsLastSyncedAt IS NULL OR p.statsLastSyncedAt < :cutoff)")
    List<Player> findPlayersNeedingStatRefresh(@Param("cutoff") LocalDateTime cutoff);
}
