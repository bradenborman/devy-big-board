package devybigboard.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * WebSocket request to toggle ready status in a draft lobby.
 */
public class ReadyRequest {
    
    @NotBlank(message = "Draft UUID is required")
    private String draftUuid;
    
    @NotBlank(message = "Position is required")
    @Pattern(regexp = "^[A-Z]$", message = "Position must be a single uppercase letter (A-Z)")
    private String position;
    
    @NotNull(message = "Ready status is required")
    private Boolean isReady;
    
    // Constructors
    public ReadyRequest() {
    }
    
    public ReadyRequest(String draftUuid, String position, Boolean isReady) {
        this.draftUuid = draftUuid;
        this.position = position;
        this.isReady = isReady;
    }
    
    // Getters and Setters
    public String getDraftUuid() {
        return draftUuid;
    }
    
    public void setDraftUuid(String draftUuid) {
        this.draftUuid = draftUuid;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public Boolean getIsReady() {
        return isReady;
    }
    
    public void setIsReady(Boolean isReady) {
        this.isReady = isReady;
    }
}
