package devybigboard.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import devybigboard.dao.CfbdPlayerStatRepository;
import devybigboard.dao.PlayerRepository;
import devybigboard.exceptions.PlayerNotFoundException;
import devybigboard.models.CfbdPlayerStat;
import devybigboard.models.Player;
import devybigboard.models.PlayerStatsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CfbdStatsService {

    private static final String BASE_URL = "https://api.collegefootballdata.com";
    // Re-fetch stats if cached data is older than this many hours
    private static final int CACHE_TTL_HOURS = 24;

    private final CfbdPlayerStatRepository statRepository;
    private final PlayerRepository playerRepository;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${app.cfbd.api-key}")
    private String apiKey;

    public CfbdStatsService(CfbdPlayerStatRepository statRepository,
                             PlayerRepository playerRepository) {
        this.statRepository = statRepository;
        this.playerRepository = playerRepository;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * For a player with no cfbd_player_id: search CFBD by name, link the ID, then fetch stats.
     * Updates the player record and saves stats to the cache.
     *
     * @param player a Player with no cfbdPlayerId set
     */
    public void linkAndSyncPlayer(Player player) {
        try {
            String cfbdId = null;
            for (int season : resolveTargetSeasons(player)) {
                cfbdId = searchCfbdIdByName(player.getName(), season);
                if (cfbdId != null) break;
            }
            if (cfbdId == null) {
                System.out.println("[CfbdStatsService] No CFBD match for: " + player.getName());
                // Still stamp the date so we don't retry every startup
                player.setStatsLastSyncedAt(LocalDateTime.now());
                playerRepository.save(player);
                return;
            }

            player.setCfbdPlayerId(cfbdId);
            syncStatsForPlayer(player);
            playerRepository.save(player);
            System.out.println("[CfbdStatsService] Linked + synced: " + player.getName() + " -> " + cfbdId);
        } catch (Exception e) {
            System.err.println("[CfbdStatsService] Failed to link " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * For a player who already has a cfbd_player_id: clear old stats and re-fetch.
     *
     * @param player a Player with cfbdPlayerId already set
     */
    public void refreshStatsForPlayer(Player player) {
        try {
            syncStatsForPlayer(player);
            playerRepository.save(player);
            System.out.println("[CfbdStatsService] Refreshed stats: " + player.getName());
        } catch (Exception e) {
            System.err.println("[CfbdStatsService] Failed to refresh " + player.getName() + ": " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------

    private void syncStatsForPlayer(Player player) {
        List<CfbdPlayerStat> fetched = List.of();
        int season = 0;

        // Try seasons going backwards until we find data
        for (int s : resolveTargetSeasons(player)) {
            fetched = fetchFromApi(player.getCfbdPlayerId(), player.getCollege(), s);
            if (!fetched.isEmpty()) {
                season = s;
                break;
            }
        }

        if (!fetched.isEmpty()) {
            statRepository.deleteByCfbdPlayerIdAndSeason(player.getCfbdPlayerId(), season);
            statRepository.saveAll(fetched);
        }

        player.setStatsLastSyncedAt(LocalDateTime.now());
    }

    /** Calls /player/search and returns the best matching CFBD player ID using name similarity, or null. */
    private String searchCfbdIdByName(String name, int year) {
        try {
            String encoded = java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8);
            String url = BASE_URL + "/player/search?searchTerm=" + encoded + "&year=" + year;

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;

            JsonNode root = objectMapper.readTree(response.body());
            if (!root.isArray() || root.size() == 0) return null;

            org.apache.commons.text.similarity.JaroWinklerSimilarity similarity =
                new org.apache.commons.text.similarity.JaroWinklerSimilarity();

            String normalizedInput = normalizeName(name);
            String bestId = null;
            double bestScore = 0.0;
            // Require at least this score to accept a match
            double THRESHOLD = 0.88;

            for (JsonNode node : root) {
                String candidateName = node.path("name").asText("");
                double score = similarity.apply(normalizedInput, normalizeName(candidateName));
                if (score > bestScore) {
                    bestScore = score;
                    bestId = node.path("id").asText(null);
                }
            }

            if (bestScore >= THRESHOLD) {
                return bestId;
            }

            System.out.println("[CfbdStatsService] No confident match for '" + name + "' (best score: " + String.format("%.2f", bestScore) + ")");
            return null;
        } catch (Exception e) {
            System.err.println("[CfbdStatsService] Name search failed for " + name + ": " + e.getMessage());
        }
        return null;
    }

    /** Lowercase, strip punctuation and suffixes like Jr./Sr./II/III for cleaner comparison. */
    private String normalizeName(String name) {
        return name.toLowerCase()
            .replaceAll("\\b(jr|sr|ii|iii|iv)\\b\\.?", "")
            .replaceAll("[^a-z ]", "")
            .replaceAll("\\s+", " ")
            .trim();
    }

    /**
     * Returns stats for the most recent season we have data for.
     * Fetches from CFBD if not cached or cache is stale.
     */
    public PlayerStatsResponse getLatestStatsForPlayer(Long playerId) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new PlayerNotFoundException("Player not found: " + playerId));

        if (player.getCfbdPlayerId() == null || player.getCfbdPlayerId().isBlank()) {
            return null;
        }

        String cfbdId = player.getCfbdPlayerId();
        Integer latestCachedSeason = statRepository.findLatestSeasonByCfbdPlayerId(cfbdId);

        // If we have fresh cached data, use it
        if (latestCachedSeason != null) {
            List<CfbdPlayerStat> cached = statRepository.findByCfbdPlayerIdAndSeason(cfbdId, latestCachedSeason);
            if (!cached.isEmpty() && isFresh(cached.get(0).getFetchedAt())) {
                return buildResponse(latestCachedSeason, cached);
            }
        }

        // Try seasons going backwards until we find data
        for (int season : resolveTargetSeasons(player)) {
            List<CfbdPlayerStat> fetched = fetchFromApi(cfbdId, player.getCollege(), season);
            if (!fetched.isEmpty()) {
                statRepository.deleteByCfbdPlayerIdAndSeason(cfbdId, season);
                statRepository.saveAll(fetched);
                return buildResponse(season, fetched);
            }
        }

        // Fall back to whatever we have cached
        if (latestCachedSeason != null) {
            List<CfbdPlayerStat> fallback = statRepository.findByCfbdPlayerIdAndSeason(cfbdId, latestCachedSeason);
            return buildResponse(latestCachedSeason, fallback);
        }

        return null;
    }

    // -------------------------------------------------------------------------

    /**
     * Returns a list of seasons to try, most recent first.
     * e.g. draftyear=2026 -> [2025, 2024, 2023]
     * This handles players whose draft year is in the future (no stats for draftyear-1 yet).
     */
    private List<Integer> resolveTargetSeasons(Player player) {
        int currentYear = LocalDateTime.now().getYear();
        int startYear;
        if (player.getDraftyear() != null && player.getDraftyear() > 0) {
            startYear = player.getDraftyear() - 1;
        } else {
            startYear = currentYear;
        }
        // Try up to 3 years back
        List<Integer> seasons = new ArrayList<>();
        for (int y = startYear; y >= startYear - 2 && y >= 2020; y--) {
            seasons.add(y);
        }
        return seasons;
    }

    private boolean isFresh(LocalDateTime fetchedAt) {
        if (fetchedAt == null) return false;
        return fetchedAt.isAfter(LocalDateTime.now().minusHours(CACHE_TTL_HOURS));
    }

    private PlayerStatsResponse buildResponse(Integer season, List<CfbdPlayerStat> rows) {
        if (rows.isEmpty()) return null;
        CfbdPlayerStat first = rows.get(0);
        return new PlayerStatsResponse(season, first.getTeam(), first.getPosition(), rows);
    }

    private List<CfbdPlayerStat> fetchFromApi(String cfbdPlayerId, String team, int season) {
        try {
            // Build URL — filter by team to keep response small, then filter by playerId client-side
            StringBuilder url = new StringBuilder(BASE_URL)
                .append("/stats/player/season?year=").append(season);
            if (team != null && !team.isBlank()) {
                url.append("&team=").append(java.net.URLEncoder.encode(team, java.nio.charset.StandardCharsets.UTF_8));
            }

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("[CfbdStatsService] API returned " + response.statusCode() + " for season " + season);
                return List.of();
            }

            JsonNode root = objectMapper.readTree(response.body());
            List<CfbdPlayerStat> results = new ArrayList<>();

            for (JsonNode node : root) {
                String pid = node.path("playerId").asText();
                if (!cfbdPlayerId.equals(pid)) continue;

                results.add(new CfbdPlayerStat(
                    cfbdPlayerId,
                    node.path("season").asInt(),
                    node.path("team").asText(null),
                    node.path("conference").asText(null),
                    node.path("position").asText(null),
                    node.path("category").asText(),
                    node.path("statType").asText(),
                    node.path("stat").asText()
                ));
            }

            return results;
        } catch (Exception e) {
            System.err.println("[CfbdStatsService] Failed to fetch stats: " + e.getMessage());
            return List.of();
        }
    }
}
