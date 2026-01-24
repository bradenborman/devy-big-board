package devybigboard.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for Player creation and updates.
 * Contains validation annotations to ensure data integrity.
 */
public class PlayerDTO {
    
    @NotBlank(message = "Player name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Position is required")
    @Size(max = 50, message = "Position must not exceed 50 characters")
    private String position;
    
    @Size(max = 255, message = "Team must not exceed 255 characters")
    private String team;
    
    @Size(max = 255, message = "College must not exceed 255 characters")
    private String college;
    
    // Constructors
    public PlayerDTO() {
    }
    
    public PlayerDTO(String name, String position, String team, String college) {
        this.name = name;
        this.position = position;
        this.team = team;
        this.college = college;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
}
