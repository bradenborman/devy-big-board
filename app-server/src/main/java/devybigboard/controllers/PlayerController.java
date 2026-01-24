package devybigboard.controllers;

import devybigboard.exceptions.PlayerNotFoundException;
import devybigboard.exceptions.UnauthorizedException;
import devybigboard.exceptions.ValidationException;
import devybigboard.models.Player;
import devybigboard.models.PlayerDTO;
import devybigboard.models.PlayerResponse;
import devybigboard.services.PlayerService;
import devybigboard.services.VerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for player management operations.
 * Provides endpoints for creating players, retrieving verified players, and verifying players.
 */
@RestController
@RequestMapping("/api/players")
public class PlayerController {
    
    private final PlayerService playerService;
    private final VerificationService verificationService;
    
    public PlayerController(PlayerService playerService, VerificationService verificationService) {
        this.playerService = playerService;
        this.verificationService = verificationService;
    }
    
    /**
     * Create a new player.
     * POST /api/players
     * 
     * @param playerDTO the player data
     * @return 201 Created with the created player data
     * @throws ValidationException if validation fails (returns 400)
     */
    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(@Valid @RequestBody PlayerDTO playerDTO) {
        try {
            Player player = playerService.createPlayer(playerDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PlayerResponse(player));
        } catch (ValidationException e) {
            throw e; // Will be handled by global exception handler
        }
    }
    
    /**
     * Verify a player using a secret code.
     * POST /api/players/{id}/verify
     * 
     * @param id the player ID
     * @param code the verification secret code
     * @return 200 OK with the verified player data
     * @throws UnauthorizedException if the code is incorrect (returns 403)
     * @throws PlayerNotFoundException if the player does not exist (returns 404)
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<PlayerResponse> verifyPlayer(
            @PathVariable Long id,
            @RequestParam String code) {
        try {
            Player player = verificationService.verifyPlayer(id, code);
            return ResponseEntity.ok(new PlayerResponse(player));
        } catch (UnauthorizedException e) {
            throw e; // Will be handled by global exception handler
        } catch (PlayerNotFoundException e) {
            throw e; // Will be handled by global exception handler
        }
    }
}
