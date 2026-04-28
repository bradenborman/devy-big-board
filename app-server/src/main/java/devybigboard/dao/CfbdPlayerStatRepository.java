package devybigboard.dao;

import devybigboard.models.CfbdPlayerStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CfbdPlayerStatRepository extends JpaRepository<CfbdPlayerStat, Long> {

    List<CfbdPlayerStat> findByCfbdPlayerIdAndSeason(String cfbdPlayerId, Integer season);

    /** Returns the most recent season we have cached for this player. */
    @Query("SELECT MAX(s.season) FROM CfbdPlayerStat s WHERE s.cfbdPlayerId = :cfbdPlayerId")
    Integer findLatestSeasonByCfbdPlayerId(String cfbdPlayerId);

    void deleteByCfbdPlayerIdAndSeason(String cfbdPlayerId, Integer season);
}
