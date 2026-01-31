package devybigboard.models;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data transfer object representing the complete state of a draft.
 * Used for WebSocket synchronization to provide all necessary information
 * about the draft, participants, picks, and available players.
 */
public class DraftState {
    
    private String uuid;
    private String draftName;
    private String status;
    private Integer currentRound;
    private Integer currentPick;
    private Integer totalRounds;
    private Integer participantCount;
    private String currentTurnPosition;
    private List<DraftParticipant> participants;
    private List<DraftPick> picks;
    private List<Player> availablePlayers;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    // Constructor
    public DraftState(String uuid, String draftName, String status, 
                     Integer currentRound, Integer currentPick, Integer totalRounds,
                     Integer participantCount, String currentTurnPosition,
                     List<DraftParticipant> participants, List<DraftPick> picks,
                     List<Player> availablePlayers, LocalDateTime startedAt,
                     LocalDateTime completedAt) {
        this.uuid = uuid;
        this.draftName = draftName;
        this.status = status;
        this.currentRound = currentRound;
        this.currentPick = currentPick;
        this.totalRounds = totalRounds;
        this.participantCount = participantCount;
        this.currentTurnPosition = currentTurnPosition;
        this.participants = participants;
        this.picks = picks;
        this.availablePlayers = availablePlayers;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }
    
    // Getters and Setters
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getDraftName() {
        return draftName;
    }
    
    public void setDraftName(String draftName) {
        this.draftName = draftName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getCurrentRound() {
        return currentRound;
    }
    
    public void setCurrentRound(Integer currentRound) {
        this.currentRound = currentRound;
    }
    
    public Integer getCurrentPick() {
        return currentPick;
    }
    
    public void setCurrentPick(Integer currentPick) {
        this.currentPick = currentPick;
    }
    
    public Integer getTotalRounds() {
        return totalRounds;
    }
    
    public void setTotalRounds(Integer totalRounds) {
        this.totalRounds = totalRounds;
    }
    
    public Integer getParticipantCount() {
        return participantCount;
    }
    
    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }
    
    public String getCurrentTurnPosition() {
        return currentTurnPosition;
    }
    
    public void setCurrentTurnPosition(String currentTurnPosition) {
        this.currentTurnPosition = currentTurnPosition;
    }
    
    public List<DraftParticipant> getParticipants() {
        return participants;
    }
    
    public void setParticipants(List<DraftParticipant> participants) {
        this.participants = participants;
    }
    
    public List<DraftPick> getPicks() {
        return picks;
    }
    
    public void setPicks(List<DraftPick> picks) {
        this.picks = picks;
    }
    
    public List<Player> getAvailablePlayers() {
        return availablePlayers;
    }
    
    public void setAvailablePlayers(List<Player> availablePlayers) {
        this.availablePlayers = availablePlayers;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
