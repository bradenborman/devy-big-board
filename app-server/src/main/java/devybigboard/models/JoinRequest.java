package devybigboard.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * WebSocket request to join a draft lobby.
 * Contains validation to ensure proper nickname and position format.
 */
public class JoinRequest {
    
    @NotBlank(message = "Draft UUID is required")
    private String draftUuid;
    
    @NotBlank(message = "Nickname is required")
    @Size(min = 2, max = 50, message = "Nickname must be between 2 and 50 characters")
    private String nickname;
    
    @NotBlank(message = "Position is required")
    @Pattern(regexp = "^[A-Z]$", message = "Position must be a single uppercase letter (A-Z)")
    private String position;
    
    // Constructors
    public JoinRequest() {
    }
    
    public JoinRequest(String draftUuid, String nickname, String position) {
        this.draftUuid = draftUuid;
        this.nickname = nickname;
        this.position = position;
    }
    
    // Getters and Setters
    public String getDraftUuid() {
        return draftUuid;
    }
    
    public void setDraftUuid(String draftUuid) {
        this.draftUuid = draftUuid;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
}
