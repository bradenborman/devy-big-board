package devybigboard.models;

/**
 * WebSocket message sent when a participant joins the draft lobby.
 */
public class ParticipantJoinedMessage {
    
    private String draftUuid;
    private ParticipantInfo participant;
    private String message;
    
    // Constructors
    public ParticipantJoinedMessage() {
    }
    
    public ParticipantJoinedMessage(String draftUuid, ParticipantInfo participant, String message) {
        this.draftUuid = draftUuid;
        this.participant = participant;
        this.message = message;
    }
    
    // Convenience constructor that generates the message
    public ParticipantJoinedMessage(String draftUuid, ParticipantInfo participant) {
        this.draftUuid = draftUuid;
        this.participant = participant;
        this.message = participant.getNickname() + " joined as Position " + participant.getPosition();
    }
    
    // Getters and Setters
    public String getDraftUuid() {
        return draftUuid;
    }
    
    public void setDraftUuid(String draftUuid) {
        this.draftUuid = draftUuid;
    }
    
    public ParticipantInfo getParticipant() {
        return participant;
    }
    
    public void setParticipant(ParticipantInfo participant) {
        this.participant = participant;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
