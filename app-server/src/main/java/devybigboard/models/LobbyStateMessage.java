package devybigboard.models;

import java.util.List;

/**
 * WebSocket message representing the current state of a draft lobby.
 * Sent to all participants when lobby state changes (join, ready, leave).
 */
public class LobbyStateMessage {
    
    private String draftUuid;
    private String draftName;
    private String status;
    private Integer participantCount;
    private Integer totalRounds;
    private List<ParticipantInfo> participants;
    private Boolean allReady;
    private Boolean canStart;
    private String createdBy;
    
    // Constructors
    public LobbyStateMessage() {
    }
    
    public LobbyStateMessage(String draftUuid, String draftName, String status, 
                            Integer participantCount, Integer totalRounds,
                            List<ParticipantInfo> participants, Boolean allReady, Boolean canStart,
                            String createdBy) {
        this.draftUuid = draftUuid;
        this.draftName = draftName;
        this.status = status;
        this.participantCount = participantCount;
        this.totalRounds = totalRounds;
        this.participants = participants;
        this.allReady = allReady;
        this.canStart = canStart;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public String getDraftUuid() {
        return draftUuid;
    }
    
    public void setDraftUuid(String draftUuid) {
        this.draftUuid = draftUuid;
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
    
    public Integer getParticipantCount() {
        return participantCount;
    }
    
    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }
    
    public Integer getTotalRounds() {
        return totalRounds;
    }
    
    public void setTotalRounds(Integer totalRounds) {
        this.totalRounds = totalRounds;
    }
    
    public List<ParticipantInfo> getParticipants() {
        return participants;
    }
    
    public void setParticipants(List<ParticipantInfo> participants) {
        this.participants = participants;
    }
    
    public Boolean getAllReady() {
        return allReady;
    }
    
    public void setAllReady(Boolean allReady) {
        this.allReady = allReady;
    }
    
    public Boolean getCanStart() {
        return canStart;
    }
    
    public void setCanStart(Boolean canStart) {
        this.canStart = canStart;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
