package devybigboard.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Cached season stat row from collegefootballdata.com /stats/player/season.
 * Stored in long/narrow format: one row per (player, season, category, statType).
 */
@Entity
@Table(name = "cfbd_player_stats",
    uniqueConstraints = @UniqueConstraint(
        name = "unique_player_season_stat",
        columnNames = {"cfbd_player_id", "season", "category", "stat_type"}
    ),
    indexes = {
        @Index(name = "idx_cfbd_player_season", columnList = "cfbd_player_id, season"),
        @Index(name = "idx_season", columnList = "season")
    }
)
public class CfbdPlayerStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cfbd_player_id", nullable = false)
    private String cfbdPlayerId;

    @Column(nullable = false)
    private Integer season;

    private String team;
    private String conference;
    private String position;

    @Column(nullable = false)
    private String category;

    @Column(name = "stat_type", nullable = false)
    private String statType;

    @Column(nullable = false)
    private String stat;

    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;

    public CfbdPlayerStat() {}

    public CfbdPlayerStat(String cfbdPlayerId, Integer season, String team, String conference,
                           String position, String category, String statType, String stat) {
        this.cfbdPlayerId = cfbdPlayerId;
        this.season = season;
        this.team = team;
        this.conference = conference;
        this.position = position;
        this.category = category;
        this.statType = statType;
        this.stat = stat;
        this.fetchedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getCfbdPlayerId() { return cfbdPlayerId; }
    public void setCfbdPlayerId(String cfbdPlayerId) { this.cfbdPlayerId = cfbdPlayerId; }
    public Integer getSeason() { return season; }
    public void setSeason(Integer season) { this.season = season; }
    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }
    public String getConference() { return conference; }
    public void setConference(String conference) { this.conference = conference; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatType() { return statType; }
    public void setStatType(String statType) { this.statType = statType; }
    public String getStat() { return stat; }
    public void setStat(String stat) { this.stat = stat; }
    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }
}
