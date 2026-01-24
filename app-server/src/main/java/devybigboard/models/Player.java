package devybigboard.models;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "players", indexes = {
    @Index(name = "idx_verified", columnList = "verified"),
    @Index(name = "idx_position", columnList = "position"),
    @Index(name = "idx_adp", columnList = "average_draft_position")
})
@EntityListeners(AuditingEntityListener.class)
public class Player {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String position;
    
    private String team;
    
    private String college;
    
    @Column(nullable = false)
    private Boolean verified = false;
    
    @Column(name = "total_selections")
    private Integer totalSelections = 0;
    
    @Column(name = "sum_draft_positions")
    private Long sumDraftPositions = 0L;
    
    @Column(name = "average_draft_position", precision = 10, scale = 2)
    private BigDecimal averageDraftPosition;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Player() {
    }
    
    public Player(String name, String position, String team, String college) {
        this.name = name;
        this.position = position;
        this.team = team;
        this.college = college;
        this.verified = false;
        this.totalSelections = 0;
        this.sumDraftPositions = 0L;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public Boolean getVerified() {
        return verified;
    }
    
    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
    
    public Integer getTotalSelections() {
        return totalSelections;
    }
    
    public void setTotalSelections(Integer totalSelections) {
        this.totalSelections = totalSelections;
    }
    
    public Long getSumDraftPositions() {
        return sumDraftPositions;
    }
    
    public void setSumDraftPositions(Long sumDraftPositions) {
        this.sumDraftPositions = sumDraftPositions;
    }
    
    public BigDecimal getAverageDraftPosition() {
        return averageDraftPosition;
    }
    
    public void setAverageDraftPosition(BigDecimal averageDraftPosition) {
        this.averageDraftPosition = averageDraftPosition;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}