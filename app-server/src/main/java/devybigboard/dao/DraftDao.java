package devybigboard.dao;

import devybigboard.models.CompletedDraftResponse;
import devybigboard.models.LeagueFilter;
import devybigboard.models.PlayerWithAdp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DraftDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DraftDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public int draftsCompletedCount() {
        String sql = """
        SELECT COUNT(DISTINCT draft_id)
        FROM draft_picks
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public String queryForUUID(long draftId) {
        String sql = "SELECT uuid FROM drafts WHERE id = ?";

        return jdbcTemplate.queryForObject(
                sql,
                String.class,
                draftId
        );
    }



    public void insertDraftPickResult(long draftId, PlayerWithAdp playerWithAdp) {
        String sql = """
        INSERT INTO draft_picks (draft_id, pick_number, name, position, team)
        VALUES (?, ?, ?, ?, ?)
    """;

        jdbcTemplate.update(sql,
                draftId,
                playerWithAdp.adp(),
                playerWithAdp.name(),
                playerWithAdp.position(),
                playerWithAdp.team()
        );
    }


    public long createDraft(String draftType) {
        String sql = """
        INSERT INTO drafts (created_at, draft_date, draft_time, type, uuid)
        VALUES (NOW(), CURRENT_DATE(), CURRENT_TIME(), :type, UUID())
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("type", draftType);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});

        return keyHolder.getKey().longValue();
    }



    private final RowMapper<LeagueFilter> leagueFilterMapper = (rs, rowNum) -> new LeagueFilter(
            rs.getLong("id"),
            rs.getString("league_name"),
            rs.getTimestamp("created_at").toInstant()
    );

    public List<LeagueFilter> getAllLeagueFilters() {
        String sql = """
        SELECT id, league_name, created_at
        FROM filters
        ORDER BY created_at DESC
    """;

        return jdbcTemplate.query(sql, leagueFilterMapper);
    }


    public CompletedDraftResponse draftByUUID(String uuid) {
        // TODO: Refactor to use JPA repositories
        // This method uses old JDBC template and DraftPick record structure
        throw new UnsupportedOperationException("Will be reimplemented with JPA repositories");
    }


    public long createFilter(String leagueName) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = """
            INSERT INTO filters (league_name)
            VALUES (:leagueName)
        """;

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("leagueName", leagueName);
        namedParameterJdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        return keyHolder.getKey().longValue();
    }

    public void addPlayerToFilter(long filterId, String playerName, String playerPosition, String playerTeam) {
        String sql = """
        INSERT INTO filter_players (filter_id, player_name, player_position, player_team)
        VALUES (:filterId, :playerName, :playerPosition, :playerTeam)
    """;

        var params = new MapSqlParameterSource()
                .addValue("filterId", filterId)
                .addValue("playerName", playerName)
                .addValue("playerPosition", playerPosition)
                .addValue("playerTeam", playerTeam);

        namedParameterJdbcTemplate.update(sql, params);
    }

    public void removePlayerFromFilter(long filterId, String playerName, String playerPosition, String playerTeam) {
        String sql = """
        DELETE FROM filter_players
        WHERE filter_id = :filterId
          AND player_name = :playerName
          AND player_position = :playerPosition
          AND player_team = :playerTeam
    """;

        var params = new MapSqlParameterSource()
                .addValue("filterId", filterId)
                .addValue("playerName", playerName)
                .addValue("playerPosition", playerPosition)
                .addValue("playerTeam", playerTeam);

        namedParameterJdbcTemplate.update(sql, params);
    }


    public void deleteFilter(long filterId) {
        String sql = "DELETE FROM filters WHERE id = :filterId";

        var params = new MapSqlParameterSource()
                .addValue("filterId", filterId);

        namedParameterJdbcTemplate.update(sql, params);
    }

}