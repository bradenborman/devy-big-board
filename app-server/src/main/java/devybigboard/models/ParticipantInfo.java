package devybigboard.models;

import java.time.LocalDateTime;

/**
 * DTO representing participant information for WebSocket messages.
 * Used in lobby state and draft state messages.
 */
public class ParticipantInfo {
    
    private String position;
    private String nickname;
    private Boolean isReady;
    private Boolean isVerified;
    private LocalDateTime joinedAt;
    
    // Constructors
    public ParticipantInfo() {
    }
    
    public ParticipantInfo(String position, String nickname, Boolean isReady, Boolean isVerified, LocalDateTime joinedAt) {
        this.position = position;
        this.nickname = nickname;
        this.isReady = isReady;
        this.isVerified = isVerified;
        this.joinedAt = joinedAt;
    }
    
    // Factory method to create from DraftParticipant entity
    public static ParticipantInfo fromEntity(DraftParticipant participant) {
        return new ParticipantInfo(
            participant.getPosition(),
            participant.getNickname(),
            participant.getIsReady(),
            participant.getIsVerified(),
            participant.getJoinedAt()
        );
    }
    
    // Getters and Setters
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
    
    public Boolean getIsReady() {
        return isReady;
    }
    
    public void setIsReady(Boolean isReady) {
        this.isReady = isReady;
    }
    
    public Boolean getIsVerified() {
        return isVerified;
    }
    
    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }
    
    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
    
    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
