package devybigboard.services;

import devybigboard.dao.PlayerRepository;
import devybigboard.exceptions.PlayerNotFoundException;
import devybigboard.exceptions.ValidationException;
import devybigboard.models.Player;
import devybigboard.models.PlayerDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing player operations including creation and verification.
 */
@Service
public class PlayerService {
    
    private final PlayerRepository playerRepository;
    
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }
    
    /**
     * Create a new player from DTO with validation.
     * Player is created with verified status set to false.
     * 
     * @param playerDTO the player data transfer object
     * @return the created player entity
     * @throws ValidationException if validation fails
     */
    @Transactional
    public Player createPlayer(PlayerDTO playerDTO) {
        validatePlayerData(playerDTO);
        
        Player player = new Player();
        player.setName(playerDTO.getName());
        player.setPosition(playerDTO.getPosition());
        player.setTeam(playerDTO.getTeam());
        player.setCollege(playerDTO.getCollege());
        player.setDraftyear(playerDTO.getDraftyear());
        player.setVerified(false);
        
        return playerRepository.save(player);
    }
    
    /**
     * Update an existing player.
     * 
     * @param playerId the ID of the player to update
     * @param playerDTO the updated player data
     * @return the updated player entity
     * @throws PlayerNotFoundException if player does not exist
     */
    @Transactional
    public Player updatePlayer(Long playerId, PlayerDTO playerDTO) {
        validatePlayerData(playerDTO);
        
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new PlayerNotFoundException(playerId));
        
        player.setName(playerDTO.getName());
        player.setPosition(playerDTO.getPosition());
        player.setTeam(playerDTO.getTeam());
        player.setCollege(playerDTO.getCollege());
        player.setDraftyear(playerDTO.getDraftyear());
        
        return playerRepository.save(player);
    }
    
    /**
     * Delete a player by ID.
     * 
     * @param playerId the ID of the player to delete
     * @throws PlayerNotFoundException if player does not exist
     */
    @Transactional
    public void deletePlayer(Long playerId) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new PlayerNotFoundException(playerId));
        playerRepository.delete(player);
    }
    
    /**
     * Get all players (verified and pending).
     * 
     * @return list of all players
     */
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }
    
    /**
     * Get all verified players available for the draft pool.
     * 
     * @return list of verified players
     */
    public List<Player> getVerifiedPlayers() {
        return playerRepository.findByVerifiedTrue();
    }
    
    /**
     * Verify a player by setting their verified status to true.
     * 
     * @param playerId the ID of the player to verify
     * @return the verified player entity
     * @throws PlayerNotFoundException if player does not exist
     */
    @Transactional
    public Player verifyPlayer(Long playerId) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new PlayerNotFoundException(playerId));
        
        player.setVerified(true);
        return playerRepository.save(player);
    }
    
    /**
     * Get a player by ID.
     * 
     * @param playerId the player ID
     * @return the player entity
     * @throws PlayerNotFoundException if player does not exist
     */
    public Player getPlayerById(Long playerId) {
        return playerRepository.findById(playerId)
            .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }
    
    /**
     * Validate player data from DTO.
     * 
     * @param dto the player DTO to validate
     * @throws ValidationException if validation fails
     */
    private void validatePlayerData(PlayerDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ValidationException("Player name is required");
        }
        if (dto.getPosition() == null || dto.getPosition().trim().isEmpty()) {
            throw new ValidationException("Player position is required");
        }
        if (dto.getName().length() > 255) {
            throw new ValidationException("Name must not exceed 255 characters");
        }
        if (dto.getPosition().length() > 50) {
            throw new ValidationException("Position must not exceed 50 characters");
        }
        if (dto.getTeam() != null && dto.getTeam().length() > 255) {
            throw new ValidationException("Team must not exceed 255 characters");
        }
        if (dto.getCollege() != null && dto.getCollege().length() > 255) {
            throw new ValidationException("College must not exceed 255 characters");
        }
    }
}
