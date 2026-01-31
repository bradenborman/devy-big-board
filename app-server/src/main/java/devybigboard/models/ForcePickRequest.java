package devybigboard.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * WebSocket request to force a pick for another position during a draft.
 */
public class ForcePickRequest {
    
    @NotBlank(message = "Draft UUID is required")
    private String draftUuid;
    
    @NotNull(message = "Player ID is required")
    private Long playerId;
    
    @NotBlank(message = "Target position is required")
    @Pattern(regexp = "^[A-Z]$", message = "Target position must be a single uppercase letter (A-Z)")
    private String targetPosition;
    
    @NotBlank(message = "Forcing position is required")
    @Pattern(regexp = "^[A-Z]$", message = "Forcing position must be a single uppercase letter (A-Z)")
    private String forcingPosition;
    
    // Constructors
    public ForcePickRequest() {
    }
    
    public ForcePickRequest(String draftUuid, Long playerId, String targetPosition, String forcingPosition) {
        this.draftUuid = draftUuid;
        this.playerId = playerId;
        this.targetPosition = targetPosition;
        this.forcingPosition = forcingPosition;
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
    
    public String getTargetPosition() {
        return targetPosition;
    }
    
    public void setTargetPosition(String targetPosition) {
        this.targetPosition = targetPosition;
    }
    
    public String getForcingPosition() {
        return forcingPosition;
    }
    
    public void setForcingPosition(String forcingPosition) {
        this.forcingPosition = forcingPosition;
    }
}
