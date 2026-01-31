package devybigboard.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * WebSocket request to make a pick during a draft.
 */
public class MakePickRequest {
    
    @NotBlank(message = "Draft UUID is required")
    private String draftUuid;
    
    @NotNull(message = "Player ID is required")
    private Long playerId;
    
    @NotBlank(message = "Position is required")
    @Pattern(regexp = "^[A-Z]$", message = "Position must be a single uppercase letter (A-Z)")
    private String position;
    
    // Constructors
    public MakePickRequest() {
    }
    
    public MakePickRequest(String draftUuid, Long playerId, String position) {
        this.draftUuid = draftUuid;
        this.playerId = playerId;
        this.position = position;
    }
    
    // Getters and Setters
    public String getDraftUuid() {
        return draftUuid;
    }
    
    public void setDraftUuid(String draftUuid) {
        this.draftUuid = draftUuid;
    }
    
    public Long getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
}
