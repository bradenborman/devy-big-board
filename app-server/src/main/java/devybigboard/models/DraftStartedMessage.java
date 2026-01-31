package devybigboard.models;

import java.time.LocalDateTime;

/**
 * WebSocket message sent when a draft is started.
 */
public class DraftStartedMessage {
    
    private String draftUuid;
    private LocalDateTime startedAt;
    private String firstTurnPosition;
    private String message;
    
    // Constructors
    public DraftStartedMessage() {
    }
    
    public DraftStartedMessage(String draftUuid, LocalDateTime startedAt, String firstTurnPosition, String message) {
        this.draftUuid = draftUuid;
        this.startedAt = startedAt;
        this.firstTurnPosition = firstTurnPosition;
        this.message = message;
    }
    
    // Convenience constructor with default message
    public DraftStartedMessage(String draftUuid, LocalDateTime startedAt, String firstTurnPosition) {
        this.draftUuid = draftUuid;
        this.startedAt = startedAt;
        this.firstTurnPosition = firstTurnPosition;
        this.message = "Draft has started!";
    }
    
    // Getters and Setters
    public String getDraftUuid() {
        return draftUuid;
    }
    
    public void setDraftUuid(String draftUuid) {
        this.draftUuid = draftUuid;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public String getFirstTurnPosition() {
        return firstTurnPosition;
    }
    
    public void setFirstTurnPosition(String firstTurnPosition) {
        this.firstTurnPosition = firstTurnPosition;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
