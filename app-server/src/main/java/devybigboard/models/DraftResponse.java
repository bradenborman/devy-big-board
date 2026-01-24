package devybigboard.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response object for draft data returned by the API.
 * Includes draft metadata and all picks.
 */
public class DraftResponse {
    
    private Long id;
    private String uuid;
    private String draftName;
    private String status;
    private Integer participantCount;
    private List<PickResponse> picks;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String shareableUrl;
    
    // Constructors
    public DraftResponse() {
    }
    
    public DraftResponse(Draft draft) {
        this.id = draft.getId();
        this.uuid = draft.getUuid();
        this.draftName = draft.getDraftName();
        this.status = draft.getStatus();
        this.participantCount = draft.getParticipantCount();
        this.picks = draft.getPicks().stream()
            .map(PickResponse::new)
            .collect(Collectors.toList());
        this.createdAt = draft.getCreatedAt();
        this.completedAt = draft.getCompletedAt();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public List<PickResponse> getPicks() {
        return picks;
    }
    
    public void setPicks(List<PickResponse> picks) {
        this.picks = picks;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public String getShareableUrl() {
        return shareableUrl;
    }
    
    public void setShareableUrl(String shareableUrl) {
        this.shareableUrl = shareableUrl;
    }
}
