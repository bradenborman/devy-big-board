package devybigboard.dao;

import devybigboard.models.PlayerWithAdp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PlayerDao {

    private final JdbcTemplate jdbcTemplate;

    public PlayerDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PlayerWithAdp> getAllPlayers() {
        String sql = """
        SELECT p.name, p.position, p.team, p.draftyear,
               COALESCE((
                   SELECT AVG(dp.pick_number)
                   FROM draft_picks dp
                   JOIN drafts d ON d.id = dp.draft_id
                   WHERE dp.player_id = p.id
                     AND d.type = 'offline'
               ), 999) AS adp
        FROM players p
        ORDER BY adp
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new PlayerWithAdp(
                rs.getString("name"),
                rs.getString("position"),
                rs.getString("team"),
                rs.getInt("draftyear"),
                rs.getDouble("adp")
        ));
    }

    public List<PlayerWithAdp> getPlayersExcludingFilter(long filterId) {
        String sql = """
        SELECT p.name, p.position, p.team, p.draftyear,
               COALESCE((
                   SELECT AVG(dp.pick_number)
                   FROM draft_picks dp
                   JOIN drafts d ON d.id = dp.draft_id
                   WHERE dp.player_id = p.id
                     AND d.type = 'offline'
               ), 999) AS adp
        FROM players p
        WHERE NOT EXISTS (
            SELECT 1
            FROM filter_players fp
            WHERE fp.filter_id = :filterId
              AND fp.player_name = p.name
              AND fp.player_position = p.position
              AND fp.player_team = p.team
        )
        ORDER BY adp
    """;

        var params = new MapSqlParameterSource()
                .addValue("filterId", filterId);

        return new NamedParameterJdbcTemplate(jdbcTemplate)
                .query(sql, params, (rs, rowNum) -> new PlayerWithAdp(
                        rs.getString("name"),
                        rs.getString("position"),
                        rs.getString("team"),
                        rs.getInt("draftyear"),
                        rs.getDouble("adp")
                ));
    }



}