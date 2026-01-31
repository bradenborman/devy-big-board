package devybigboard.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a participant in a live draft lobby.
 * Each participant has a position (A-Z), nickname, and ready status.
 */
@Entity
@Table(name = "draft_participants", 
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_draft_position", columnNames = {"draft_id", "position"}),
        @UniqueConstraint(name = "unique_draft_nickname", columnNames = {"draft_id", "nickname"})
    },
    indexes = {
        @Index(name = "idx_draft_id", columnList = "draft_id")
    }
)
public class DraftParticipant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_id", nullable = false)
    private Draft draft;
    
    @Column(name = "position", nullable = false, length = 1)
    private String position;
    
    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;
    
    @Column(name = "is_ready", nullable = false)
    private Boolean isReady = false;
    
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
    
    // Constructors
    public DraftParticipant() {
    }
    
    public DraftParticipant(Draft draft, String position, String nickname) {
        this.draft = draft;
        this.position = position;
        this.nickname = nickname;
        this.isReady = false;
        this.isVerified = false;
        this.joinedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Draft getDraft() {
        return draft;
    }
    
    public void setDraft(Draft draft) {
        this.draft = draft;
    }
    
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
