package devybigboard.models;

import java.util.List;

/**
 * WebSocket message representing the complete state of an active draft.
 * Sent to all participants when draft state changes (picks made, round changes).
 */
public class DraftStateMessage {
    
    private String draftUuid;
    private String status;
    private Integer currentRound;
    private Integer currentPick;
    private String currentTurnPosition;
    private Integer participantCount;
    private List<ParticipantInfo> participants;
    private List<PickMessage> picks;
    private List<PlayerResponse> availablePlayers;
    
    // Constructors
    public DraftStateMessage() {
    }
    
    public DraftStateMessage(String draftUuid, String status, Integer currentRound, 
                            Integer currentPick, String currentTurnPosition,
                            Integer participantCount, List<ParticipantInfo> participants,
                            List<PickMessage> picks, List<PlayerResponse> availablePlayers) {
        this.draftUuid = draftUuid;
        this.status = status;
        this.currentRound = currentRound;
        this.currentPick = currentPick;
        this.currentTurnPosition = currentTurnPosition;
        this.participantCount = participantCount;
        this.participants = participants;
        this.picks = picks;
        this.availablePlayers = availablePlayers;
    }
    
    // Getters and Setters
    public String getDraftUuid() {
        return draftUuid;
    }
    
    public void setDraftUuid(String draftUuid) {
        this.draftUuid = draftUuid;
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
    
    public String getCurrentTurnPosition() {
        return currentTurnPosition;
    }
    
    public void setCurrentTurnPosition(String currentTurnPosition) {
        this.currentTurnPosition = currentTurnPosition;
    }
    
    public Integer getParticipantCount() {
        return participantCount;
    }
    
    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }
    
    public List<ParticipantInfo> getParticipants() {
        return participants;
    }
    
    public void setParticipants(List<ParticipantInfo> participants) {
        this.participants = participants;
    }
    
    public List<PickMessage> getPicks() {
        return picks;
    }
    
    public void setPicks(List<PickMessage> picks) {
        this.picks = picks;
    }
    
    public List<PlayerResponse> getAvailablePlayers() {
        return availablePlayers;
    }
    
    public void setAvailablePlayers(List<PlayerResponse> availablePlayers) {
        this.availablePlayers = availablePlayers;
    }
}
