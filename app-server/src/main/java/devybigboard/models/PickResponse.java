package devybigboard.models;

import java.time.LocalDateTime;

/**
 * Response object for draft pick data.
 * Includes pick details and associated player information.
 */
public class PickResponse {
    
    private Long id;
    private Integer pickNumber;
    private PlayerResponse player;
    private LocalDateTime pickedAt;
    
    // Constructors
    public PickResponse() {
    }
    
    public PickResponse(DraftPick draftPick) {
        this.id = draftPick.getId();
        this.pickNumber = draftPick.getPickNumber();
        this.player = new PlayerResponse(draftPick.getPlayer());
        this.pickedAt = draftPick.getPickedAt();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getPickNumber() {
        return pickNumber;
    }
    
    public void setPickNumber(Integer pickNumber) {
        this.pickNumber = pickNumber;
    }
    
    public PlayerResponse getPlayer() {
        return player;
    }
    
    public void setPlayer(PlayerResponse player) {
        this.player = player;
    }
    
    public LocalDateTime getPickedAt() {
        return pickedAt;
    }
    
    public void setPickedAt(LocalDateTime pickedAt) {
        this.pickedAt = pickedAt;
    }
}
