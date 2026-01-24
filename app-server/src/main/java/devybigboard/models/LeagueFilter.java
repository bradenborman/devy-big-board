package devybigboard.models;

import java.time.Instant;

public record LeagueFilter(
        long id,
        String leagueName,
        Instant createdAt
) {}