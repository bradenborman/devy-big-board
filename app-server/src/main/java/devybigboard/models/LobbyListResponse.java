package devybigboard.models;

/**
 * Response object for public lobby list.
 * Excludes PIN for security - users must get PIN from share link or creator.
 */
public class LobbyListResponse {
    
    private String uuid;
    private String draftName;
    private String status;
    private Integer participantCount;
    private Integer totalRounds;
    private String createdBy;
    private String lobbyUrl;
    
    // Constructors
    public LobbyListResponse() {
    }
    
    public LobbyListResponse(Draft draft, String lobbyUrl) {
        this.uuid = draft.getUuid();
        this.draftName = draft.getDraftName();
        this.status = draft.getStatus();
        this.participantCount = draft.getParticipantCount();
        this.totalRounds = draft.getTotalRounds();
        this.createdBy = draft.getCreatedBy();
        this.lobbyUrl = lobbyUrl;
        // Intentionally exclude PIN for security
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
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getLobbyUrl() {
        return lobbyUrl;
    }
    
    public void setLobbyUrl(String lobbyUrl) {
        this.lobbyUrl = lobbyUrl;
    }
}
