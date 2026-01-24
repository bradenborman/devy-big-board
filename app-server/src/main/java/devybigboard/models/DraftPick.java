package devybigboard.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "draft_picks", indexes = {
    @Index(name = "idx_draft_id", columnList = "draft_id"),
    @Index(name = "idx_player_id", columnList = "player_id")
})
public class DraftPick {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_id", nullable = false)
    private Draft draft;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @Column(name = "pick_number", nullable = false)
    private Integer pickNumber;
    
    @Column(name = "picked_at")
    private LocalDateTime pickedAt;
    
    // Constructors
    public DraftPick() {
    }
    
    public DraftPick(Draft draft, Player player, Integer pickNumber) {
        this.draft = draft;
        this.player = player;
        this.pickNumber = pickNumber;
        this.pickedAt = LocalDateTime.now();
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
    
    public Player getPlayer() {
        return player;
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public Integer getPickNumber() {
        return pickNumber;
    }
    
    public void setPickNumber(Integer pickNumber) {
        this.pickNumber = pickNumber;
    }
    
    public LocalDateTime getPickedAt() {
        return pickedAt;
    }
    
    public void setPickedAt(LocalDateTime pickedAt) {
        this.pickedAt = pickedAt;
    }
}