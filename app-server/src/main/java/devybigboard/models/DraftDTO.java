package devybigboard.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Data Transfer Object for draft information.
 * Used when submitting completed draft data to the API.
 */
public class DraftDTO {
    
    @Size(max = 255, message = "Draft name must not exceed 255 characters")
    private String draftName;
    
    @Min(value = 1, message = "Participant count must be at least 1")
    private Integer participantCount;
    
    @NotEmpty(message = "Draft must contain at least one pick")
    @Valid
    private List<PickDTO> picks;
    
    // Constructors
    public DraftDTO() {
    }
    
    public DraftDTO(String draftName, Integer participantCount, List<PickDTO> picks) {
        this.draftName = draftName;
        this.participantCount = participantCount;
        this.picks = picks;
    }
    
    // Getters and Setters
    public String getDraftName() {
        return draftName;
    }
    
    public void setDraftName(String draftName) {
        this.draftName = draftName;
    }
    
    public Integer getParticipantCount() {
        return participantCount;
    }
    
    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }
    
    public List<PickDTO> getPicks() {
        return picks;
    }
    
    public void setPicks(List<PickDTO> picks) {
        this.picks = picks;
    }
}
