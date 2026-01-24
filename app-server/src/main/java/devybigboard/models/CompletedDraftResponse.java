package devybigboard.models;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CompletedDraftResponse(
        long id,
        Instant createdAt,
        LocalDate draftDate,
        LocalTime draftTime,
        String type,
        String uuid,
        List<DraftPick> picks
) {}