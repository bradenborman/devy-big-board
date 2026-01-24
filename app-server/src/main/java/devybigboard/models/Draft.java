package devybigboard.models;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drafts", indexes = {
    @Index(name = "idx_uuid", columnList = "uuid"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class Draft {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 36)
    private String uuid;
    
    @Column(name = "draft_name")
    private String draftName;
    
    @Column(length = 50)
    private String status = "completed";
    
    @Column(name = "participant_count")
    private Integer participantCount = 1;
    
    @OneToMany(mappedBy = "draft", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DraftPick> picks = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // Constructors
    public Draft() {
    }
    
    public Draft(String uuid, String draftName, Integer participantCount) {
        this.uuid = uuid;
        this.draftName = draftName;
        this.participantCount = participantCount;
        this.status = "completed";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getDraftName() {
        return draftName;
    }
    
    public void setDraftName(String draftName) {
        this.draftName = draftName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getParticipantCount() {
        return participantCount;
    }
    
    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }
    
    public List<DraftPick> getPicks() {
        return picks;
    }
    
    public void setPicks(List<DraftPick> picks) {
        this.picks = picks;
    }
    
    public void addPick(DraftPick pick) {
        picks.add(pick);
        pick.setDraft(this);
    }
    
    public void removePick(DraftPick pick) {
        picks.remove(pick);
        pick.setDraft(null);
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
