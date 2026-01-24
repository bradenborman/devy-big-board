package devybigboard.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for draft pick information.
 * Used when submitting draft data to the API.
 */
public class PickDTO {
    
    @NotNull(message = "Player ID is required")
    private Long playerId;
    
    @NotNull(message = "Pick number is required")
    @Min(value = 1, message = "Pick number must be at least 1")
    private Integer pickNumber;
    
    // Constructors
    public PickDTO() {
    }
    
    public PickDTO(Long playerId, Integer pickNumber) {
        this.playerId = playerId;
        this.pickNumber = pickNumber;
    }
    
    // Getters and Setters
    public Long getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
    
    public Integer getPickNumber() {
        return pickNumber;
    }
    
    public void setPickNumber(Integer pickNumber) {
        this.pickNumber = pickNumber;
    }
}
