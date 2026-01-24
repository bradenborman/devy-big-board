package devybigboard.exceptions;

/**
 * Exception thrown when a player is not found in the database.
 */
public class PlayerNotFoundException extends RuntimeException {
    
    public PlayerNotFoundException(Long playerId) {
        super("Player not found with id: " + playerId);
    }
    
    public PlayerNotFoundException(String message) {
        super(message);
    }
}
