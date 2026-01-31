package devybigboard.models;

import java.time.LocalDateTime;

/**
 * WebSocket message representing a draft pick.
 * Sent when a pick is made during the draft.
 */
public class PickMessage {
    
    private Long playerId;
    private String playerName;
    private String position;
    private String team;
    private String college;
    private Integer roundNumber;
    private Integer pickNumber;
    private String pickedByPosition;
    private String forcedByPosition;
    private LocalDateTime pickedAt;
    
    // Constructors
    public PickMessage() {
    }
    
    public PickMessage(Long playerId, String playerName, String position, String team, 
                      String college, Integer roundNumber, Integer pickNumber, 
                      String pickedByPosition, String forcedByPosition, LocalDateTime pickedAt) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.position = position;
        this.team = team;
        this.college = college;
        this.roundNumber = roundNumber;
        this.pickNumber = pickNumber;
        this.pickedByPosition = pickedByPosition;
        this.forcedByPosition = forcedByPosition;
        this.pickedAt = pickedAt;
    }
    
    // Factory method to create from DraftPick entity
    public static PickMessage fromEntity(DraftPick pick) {
        Player player = pick.getPlayer();
        return new PickMessage(
            player.getId(),
            player.getName(),
            player.getPosition(),
            player.getTeam(),
            player.getCollege(),
            pick.getRoundNumber(),
            pick.getPickNumber(),
            pick.getPosition(),
            pick.getForcedBy(),
            pick.getPickedAt()
        );
    }
    
    // Getters and Setters
    public Long getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public String getTeam() {
        return team;
    }
    
    public void setTeam(String team) {
        this.team = team;
    }
    
    public String getCollege() {
        return college;
    }
    
    public void setCollege(String college) {
        this.college = college;
    }
    
    public Integer getRoundNumber() {
        return roundNumber;
    }
    
    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }
    
    public Integer getPickNumber() {
        return pickNumber;
    }
    
    public void setPickNumber(Integer pickNumber) {
        this.pickNumber = pickNumber;
    }
    
    public String getPickedByPosition() {
        return pickedByPosition;
    }
    
    public void setPickedByPosition(String pickedByPosition) {
        this.pickedByPosition = pickedByPosition;
    }
    
    public String getForcedByPosition() {
        return forcedByPosition;
    }
    
    public void setForcedByPosition(String forcedByPosition) {
        this.forcedByPosition = forcedByPosition;
    }
    
    public LocalDateTime getPickedAt() {
        return pickedAt;
    }
    
    public void setPickedAt(LocalDateTime pickedAt) {
        this.pickedAt = pickedAt;
    }
}
