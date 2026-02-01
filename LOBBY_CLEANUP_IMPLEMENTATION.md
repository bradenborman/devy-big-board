# Lobby Cleanup Implementation

## Overview
Implemented an automated scheduled task that runs every hour to clean up stale lobbies that are more than 1 hour old and haven't been started.

## Changes Made

### 1. Enabled Spring Scheduling
- Added `@EnableScheduling` annotation to `DevyBigBoardApplication.java`
- This enables Spring's scheduled task execution framework

### 2. Created LobbyCleanupService
**File:** `app-server/src/main/java/devybigboard/services/LobbyCleanupService.java`

- Scheduled to run every hour using cron expression: `0 0 * * * *` (at the top of every hour)
- Finds all lobbies with status "LOBBY" that are older than 1 hour and haven't been started
- Performs **real deletion** (not status change) using `draftRepository.delete()`
- Cascade delete automatically removes all associated:
  - DraftParticipants (via `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)`)
  - DraftPicks (via `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)`)
- Includes comprehensive logging for monitoring
- Error handling ensures one failed deletion doesn't stop the cleanup of other lobbies

### 3. Added Repository Query Method
**File:** `app-server/src/main/java/devybigboard/dao/DraftRepository.java`

Added `findStaleLobbyDrafts()` method:
```java
@Query("SELECT d FROM Draft d WHERE d.status = :status AND d.createdAt < :cutoffTime AND d.startedAt IS NULL")
List<Draft> findStaleLobbyDrafts(@Param("status") String status, @Param("cutoffTime") LocalDateTime cutoffTime);
```

This query finds lobbies that:
- Have status "LOBBY"
- Were created before the cutoff time (1 hour ago)
- Have never been started (`startedAt IS NULL`)

### 4. Created Unit Tests
**File:** `app-server/src/test/java/devybigboard/services/LobbyCleanupServiceTest.java`

Test coverage includes:
- Deleting stale lobbies
- Handling no stale lobbies
- Handling multiple stale lobbies
- Continuing on error (resilience)

## How It Works

1. **Scheduled Execution**: Every hour at :00 minutes
2. **Query**: Finds lobbies created more than 1 hour ago with status "LOBBY" and no start time
3. **Delete**: Performs cascade delete on each stale lobby
4. **Cleanup**: JPA automatically removes all related participants and picks due to cascade settings
5. **Logging**: Records the number of lobbies cleaned and any errors

## Configuration

The cleanup interval is currently set to 1 hour. To modify:
- Change `LOBBY_TIMEOUT_HOURS` constant in `LobbyCleanupService.java`
- Modify the cron expression in `@Scheduled` annotation to change execution frequency

### Cron Expression Examples:
- Every hour: `0 0 * * * *`
- Every 30 minutes: `0 0/30 * * * *`
- Every 2 hours: `0 0 0/2 * * *`
- Daily at 3 AM: `0 0 3 * * *`

## Database Impact

The cleanup performs **hard deletes** from the database:
- Deletes from `drafts` table
- Cascade deletes from `draft_participants` table
- Cascade deletes from `draft_picks` table (if any exist)

This keeps the database clean and prevents accumulation of abandoned lobbies.

## Monitoring

Check application logs for cleanup activity:
- `Starting scheduled cleanup of stale lobbies`
- `Found X stale lobbies to delete`
- `Deleting stale lobby: uuid=..., name=..., createdAt=..., participants=...`
- `Completed cleanup of X stale lobbies`
- Error logs if any deletion fails
