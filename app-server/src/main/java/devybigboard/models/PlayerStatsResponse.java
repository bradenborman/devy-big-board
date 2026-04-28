package devybigboard.models;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Response DTO for a player's season stats.
 * Groups the long/narrow stat rows into a map of category -> (statType -> value).
 *
 * Example:
 * {
 *   "season": 2023,
 *   "team": "Alabama",
 *   "position": "QB",
 *   "stats": {
 *     "passing": { "YDS": "3500", "TD": "28", "INT": "6", ... },
 *     "rushing": { "CAR": "45", "YDS": "210", "TD": "4" }
 *   }
 * }
 */
public class PlayerStatsResponse {

    private Integer season;
    private String team;
    private String position;
    private Map<String, Map<String, String>> stats;

    public PlayerStatsResponse(Integer season, String team, String position,
                                List<CfbdPlayerStat> rows) {
        this.season = season;
        this.team = team;
        this.position = position;

        PositionStatProfile profile = PositionStatProfile.fromPosition(position);

        this.stats = rows.stream()
            .filter(r -> profile.isRelevant(r.getCategory(), r.getStatType()))
            .collect(Collectors.groupingBy(
                CfbdPlayerStat::getCategory,
                Collectors.toMap(CfbdPlayerStat::getStatType, CfbdPlayerStat::getStat)
            ));
    }

    public Integer getSeason() { return season; }
    public String getTeam() { return team; }
    public String getPosition() { return position; }
    public Map<String, Map<String, String>> getStats() { return stats; }
}
