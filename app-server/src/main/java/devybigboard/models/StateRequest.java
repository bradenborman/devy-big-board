package devybigboard.models;

import jakarta.validation.constraints.NotBlank;

/**
 * WebSocket request to get draft or lobby state.
 * Only requires the draft UUID, no participant information needed.
 */
public class StateRequest {
    
    @NotBlank(message = "Draft UUID is required")
    private String draftUuid;
    
    // Constructors
    public StateRequest() {
    }
    
    public StateRequest(String draftUuid) {
        this.draftUuid = draftUuid;
    }
    
    // Getters and Setters
    public String getDraftUuid() {
        return draftUuid;
    }
    
    public void setDraftUuid(String draftUuid) {
        this.draftUuid = draftUuid;
    }
}
