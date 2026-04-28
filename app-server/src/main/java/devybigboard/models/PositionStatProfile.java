package devybigboard.models;

import java.util.Map;
import java.util.Set;

/**
 * Maps each fantasy-relevant position to the stat categories and types we care about.
 * Used to filter raw CFBD stat rows down to what's meaningful for display.
 */
public enum PositionStatProfile {

    QB(Map.of(
        "passing",  Set.of("ATT", "COMPLETIONS", "PCT", "YDS", "TD", "INT", "YPA"),
        "rushing",  Set.of("CAR", "YDS", "TD")
    )),

    RB(Map.of(
        "rushing",  Set.of("CAR", "YDS", "TD", "YPC", "LONG"),
        "receiving", Set.of("REC", "YDS", "TD")
    )),

    WR(Map.of(
        "receiving", Set.of("REC", "YDS", "TD", "YPR", "LONG")
    )),

    TE(Map.of(
        "receiving", Set.of("REC", "YDS", "TD", "YPR", "LONG")
    )),

    // Fallback — return everything
    UNKNOWN(Map.of());

    private final Map<String, Set<String>> relevantStats;

    PositionStatProfile(Map<String, Set<String>> relevantStats) {
        this.relevantStats = relevantStats;
    }

    public static PositionStatProfile fromPosition(String position) {
        if (position == null) return UNKNOWN;
        return switch (position.toUpperCase()) {
            case "QB" -> QB;
            case "RB" -> RB;
            case "WR" -> WR;
            case "TE" -> TE;
            default   -> UNKNOWN;
        };
    }

    /**
     * Returns true if this stat row is relevant for this position.
     * UNKNOWN profile passes everything through.
     */
    public boolean isRelevant(String category, String statType) {
        if (this == UNKNOWN || relevantStats.isEmpty()) return true;
        Set<String> types = relevantStats.get(category);
        return types != null && types.contains(statType);
    }

    public Map<String, Set<String>> getRelevantStats() {
        return relevantStats;
    }
}
