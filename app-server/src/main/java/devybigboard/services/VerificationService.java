package devybigboard.services;

import devybigboard.exceptions.UnauthorizedException;
import devybigboard.models.Player;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for handling player verification with secret code authentication.
 */
@Service
public class VerificationService {
    
    private final String verificationSecret;
    private final PlayerService playerService;
    
    public VerificationService(
            PlayerService playerService,
            @Value("${app.verification.secret:default-secret-change-me}") String verificationSecret) {
        this.playerService = playerService;
        this.verificationSecret = verificationSecret;
    }
    
    /**
     * Verify a player using a secret code.
     * 
     * @param playerId the ID of the player to verify
     * @param providedSecret the secret code provided by the user
     * @return the verified player
     * @throws UnauthorizedException if the secret code is incorrect
     */
    public Player verifyPlayer(Long playerId, String providedSecret) {
        if (!verificationSecret.equals(providedSecret)) {
            throw new UnauthorizedException("Invalid verification code");
        }
        return playerService.verifyPlayer(playerId);
    }
    
    /**
     * Check if a provided code is valid.
     * 
     * @param providedSecret the secret code to check
     * @return true if the code is valid, false otherwise
     */
    public boolean isValidCode(String providedSecret) {
        return verificationSecret.equals(providedSecret);
    }
}
