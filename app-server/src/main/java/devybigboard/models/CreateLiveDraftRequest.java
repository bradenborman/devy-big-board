package devybigboard.models;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request object for creating a new live draft.
 * Used by POST /api/live-drafts endpoint.
 */
public class CreateLiveDraftRequest {
    
    @NotBlank(message = "Draft name is required")
    private String draftName;
    
    @NotBlank(message = "Creator nickname is required")
    private String creatorNickname;
    
    @NotNull(message = "Participant count is required")
    @Min(value = 2, message = "Participant count must be at least 2")
    @Max(value = 26, message = "Participant count cannot exceed 26")
    private Integer participantCount;
    
    @NotNull(message = "Total rounds is required")
    @Min(value = 1, message = "Total rounds must be at least 1")
    @Max(value = 20, message = "Total rounds cannot exceed 20")
    private Integer totalRounds;
    
    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "^\\d{4}$", message = "PIN must be exactly 4 digits")
    private String pin;
    
    private Boolean isSnakeDraft = false;
    
    // Constructors
    public CreateLiveDraftRequest() {
    }
    
    public CreateLiveDraftRequest(String draftName, String creatorNickname, Integer participantCount, Integer totalRounds) {
        this.draftName = draftName;
        this.creatorNickname = creatorNickname;
        this.participantCount = participantCount;
        this.totalRounds = totalRounds;
        this.pin = null;
        this.isSnakeDraft = false;
    }
    
    public CreateLiveDraftRequest(String draftName, String creatorNickname, Integer participantCount, Integer totalRounds, String pin, Boolean isSnakeDraft) {
        this.draftName = draftName;
        this.creatorNickname = creatorNickname;
        this.participantCount = participantCount;
        this.totalRounds = totalRounds;
        this.pin = pin;
        this.isSnakeDraft = isSnakeDraft;
    }
    
    // Getters and Setters
    public String getDraftName() {
        return draftName;
    }
    
    public void setDraftName(String draftName) {
        this.draftName = draftName;
    }
    
    public String getCreatorNickname() {
        return creatorNickname;
    }
    
    public void setCreatorNickname(String creatorNickname) {
        this.creatorNickname = creatorNickname;
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
    
    public String getPin() {
        return pin;
    }
    
    public void setPin(String pin) {
        this.pin = pin;
    }
    
    public Boolean getIsSnakeDraft() {
        return isSnakeDraft;
    }
    
    public void setIsSnakeDraft(Boolean isSnakeDraft) {
        this.isSnakeDraft = isSnakeDraft;
    }
}
