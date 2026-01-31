package devybigboard.models;

/**
 * WebSocket message sent when a participant leaves the draft lobby.
 */
public class ParticipantLeftMessage {
    
    private String draftUuid;
    private String position;
    private String nickname;
    private String message;
    
    // Constructors
    public ParticipantLeftMessage() {
    }
    
    public ParticipantLeftMessage(String draftUuid, String position, String nickname, String message) {
        this.draftUuid = draftUuid;
        this.position = position;
        this.nickname = nickname;
        this.message = message;
    }
    
    // Convenience constructor that generates the message
    public ParticipantLeftMessage(String draftUuid, String position, String nickname) {
        this.draftUuid = draftUuid;
        this.position = position;
        this.nickname = nickname;
        this.message = nickname + " left the lobby";
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
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
