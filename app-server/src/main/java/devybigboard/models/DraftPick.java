package devybigboard.models;

public record DraftPick(
        long draftId,
        int pickNumber,
        String name,
        String position,
        String team
) {}