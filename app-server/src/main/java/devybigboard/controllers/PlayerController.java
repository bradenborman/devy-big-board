package devybigboard.controllers;

import devybigboard.dao.PlayerAssetRepository;
import devybigboard.exceptions.PlayerNotFoundException;
import devybigboard.exceptions.UnauthorizedException;
import devybigboard.exceptions.ValidationException;
import devybigboard.models.Player;
import devybigboard.models.PlayerAsset;
import devybigboard.models.PlayerDTO;
import devybigboard.models.PlayerResponse;
import devybigboard.services.AssetService;
import devybigboard.services.PlayerService;
import devybigboard.services.VerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for player management operations.
 * Provides endpoints for creating players, retrieving all players (including pending), and managing players.
 */
@RestController
@RequestMapping("/api/players/manage")
public class PlayerController {
    
    private final PlayerService playerService;
    private final VerificationService verificationService;
    private final AssetService assetService;
    private final PlayerAssetRepository playerAssetRepository;
    
    public PlayerController(PlayerService playerService, VerificationService verificationService, 
                          AssetService assetService, PlayerAssetRepository playerAssetRepository) {
        this.playerService = playerService;
        this.verificationService = verificationService;
        this.assetService = assetService;
        this.playerAssetRepository = playerAssetRepository;
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
            // If verification code is provided and correct, verify immediately
            if (playerDTO.getVerificationCode() != null && !playerDTO.getVerificationCode().isEmpty()) {
                if (verificationService.isValidCode(playerDTO.getVerificationCode())) {
                    Player player = playerService.createPlayer(playerDTO);
                    player = playerService.verifyPlayer(player.getId());
                    Optional<PlayerAsset> asset = playerAssetRepository.findByPlayerId(player.getId());
                    return ResponseEntity.status(HttpStatus.CREATED)
                        .body(asset.isPresent() 
                            ? new PlayerResponse(player, asset.get().getImageUrl())
                            : new PlayerResponse(player));
                }
            }
            
            // Otherwise create as pending
            Player player = playerService.createPlayer(playerDTO);
            Optional<PlayerAsset> asset = playerAssetRepository.findByPlayerId(player.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(asset.isPresent() 
                    ? new PlayerResponse(player, asset.get().getImageUrl())
                    : new PlayerResponse(player));
        } catch (ValidationException e) {
            throw e; // Will be handled by global exception handler
        }
    }
    
    /**
     * Upload player headshot image.
     * POST /api/players/{id}/headshot
     * 
     * @param id the player ID
     * @param file the image file
     * @return 200 OK with the updated player data including image URL
     * @throws PlayerNotFoundException if the player does not exist (returns 404)
     */
    @PostMapping("/{id}/headshot")
    public ResponseEntity<PlayerResponse> uploadHeadshot(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new ValidationException("File cannot be empty");
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ValidationException("File must be an image");
            }
            
            // Verify player exists
            Player player = playerService.getPlayerById(id);
            
            // Check if player already has an asset
            Optional<PlayerAsset> existingAsset = playerAssetRepository.findByPlayerId(id);
            
            // Delete old image from S3 if exists
            if (existingAsset.isPresent()) {
                try {
                    assetService.deleteImageByUrl(existingAsset.get().getImageUrl());
                } catch (Exception e) {
                    // Log but don't fail if old image deletion fails
                }
            }
            
            // Upload to S3 in players/headshots folder
            String imageUrl = assetService.uploadImage(file, "players/headshots");
            
            // Create or update player asset
            PlayerAsset asset;
            if (existingAsset.isPresent()) {
                asset = existingAsset.get();
                asset.setImageUrl(imageUrl);
            } else {
                asset = new PlayerAsset(id, imageUrl);
            }
            playerAssetRepository.save(asset);
            
            return ResponseEntity.ok(new PlayerResponse(player, imageUrl));
        } catch (IOException e) {
            throw new ValidationException("Failed to upload image: " + e.getMessage());
        }
    }
    
    /**
     * Get all players (verified and pending).
     * GET /api/players
     * 
     * @return 200 OK with list of all players
     */
    @GetMapping
    public ResponseEntity<List<PlayerResponse>> getAllPlayers() {
        List<Player> players = playerService.getAllPlayers();
        List<PlayerResponse> response = players.stream()
            .map(player -> {
                Optional<PlayerAsset> asset = playerAssetRepository.findByPlayerId(player.getId());
                return asset.isPresent() 
                    ? new PlayerResponse(player, asset.get().getImageUrl())
                    : new PlayerResponse(player);
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update an existing player (requires verification code).
     * PUT /api/players/{id}
     * 
     * @param id the player ID
     * @param playerDTO the updated player data (must include verificationCode)
     * @return 200 OK with the updated player data
     * @throws UnauthorizedException if the code is incorrect (returns 403)
     * @throws PlayerNotFoundException if the player does not exist (returns 404)
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlayerResponse> updatePlayer(
            @PathVariable Long id,
            @Valid @RequestBody PlayerDTO playerDTO) {
        try {
            if (playerDTO.getVerificationCode() == null || playerDTO.getVerificationCode().isEmpty()) {
                throw new UnauthorizedException("Verification code is required to update players");
            }
            
            if (!verificationService.isValidCode(playerDTO.getVerificationCode())) {
                throw new UnauthorizedException("Invalid verification code");
            }
            
            Player player = playerService.updatePlayer(id, playerDTO);
            Optional<PlayerAsset> asset = playerAssetRepository.findByPlayerId(id);
            return ResponseEntity.ok(
                asset.isPresent() 
                    ? new PlayerResponse(player, asset.get().getImageUrl())
                    : new PlayerResponse(player)
            );
        } catch (UnauthorizedException | PlayerNotFoundException e) {
            throw e; // Will be handled by global exception handler
        }
    }
    
    /**
     * Delete a player (requires verification code).
     * DELETE /api/players/{id}
     * 
     * @param id the player ID
     * @param code the verification secret code
     * @return 204 No Content
     * @throws UnauthorizedException if the code is incorrect (returns 403)
     * @throws PlayerNotFoundException if the player does not exist (returns 404)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(
            @PathVariable Long id,
            @RequestParam String code) {
        try {
            if (!verificationService.isValidCode(code)) {
                throw new UnauthorizedException("Invalid verification code");
            }
            
            playerService.deletePlayer(id);
            return ResponseEntity.noContent().build();
        } catch (UnauthorizedException | PlayerNotFoundException e) {
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
