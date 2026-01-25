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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
                    return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new PlayerResponse(player));
                }
            }
            
            // Otherwise create as pending
            Player player = playerService.createPlayer(playerDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PlayerResponse(player));
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
    @CacheEvict(value = "playerHeadshots", key = "#id")
    public ResponseEntity<PlayerResponse> uploadHeadshot(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("[PlayerController] Uploading headshot for player ID: " + id);
            
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
            System.out.println("[PlayerController] Player found: " + player.getName());
            
            // Check if player already has an asset
            Optional<PlayerAsset> existingAsset = playerAssetRepository.findByPlayerId(id);
            
            // Delete old image from S3 if exists
            if (existingAsset.isPresent()) {
                String oldFilename = existingAsset.get().getFilename();
                System.out.println("[PlayerController] Deleting old image: " + oldFilename);
                try {
                    assetService.deleteImage("players/headshots/" + oldFilename);
                } catch (Exception e) {
                    System.err.println("[PlayerController] Failed to delete old image: " + e.getMessage());
                }
            }
            
            // Upload to S3 in players/headshots folder - returns just filename
            String filename = assetService.uploadImage(file, "players/headshots");
            System.out.println("[PlayerController] Image uploaded with filename: " + filename);
            
            // Create or update player asset
            PlayerAsset asset;
            if (existingAsset.isPresent()) {
                asset = existingAsset.get();
                asset.setFilename(filename);
                System.out.println("[PlayerController] Updating existing asset");
            } else {
                asset = new PlayerAsset(id, filename);
                System.out.println("[PlayerController] Creating new asset");
            }
            playerAssetRepository.save(asset);
            System.out.println("[PlayerController] Asset saved to database");
            
            return ResponseEntity.ok(new PlayerResponse(player));
        } catch (IOException e) {
            System.err.println("[PlayerController] Upload failed: " + e.getMessage());
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
            .map(PlayerResponse::new)
            .collect(Collectors.toList());
        System.out.println("[PlayerController] Returning " + response.size() + " players");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get player headshot image by player ID.
     * GET /api/players/manage/{id}/headshot
     * 
     * Cached to reduce S3 egress costs. Cache is evicted when a new headshot is uploaded.
     * Only caches non-null results (when headshot exists).
     * 
     * @param id the player ID
     * @return The image file or 404 if not found
     */
    @GetMapping("/{id}/headshot")
    @Cacheable(value = "playerHeadshots", key = "#id", unless = "#result == null || #result.body == null")
    public ResponseEntity<byte[]> getPlayerHeadshot(@PathVariable Long id) {
        try {
            Optional<PlayerAsset> asset = playerAssetRepository.findByPlayerId(id);
            
            if (asset.isEmpty()) {
                System.out.println("[PlayerController] No headshot found for player ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            String filename = asset.get().getFilename();
            String fullKey = "players/headshots/" + filename;
            System.out.println("[PlayerController] Fetching headshot for player ID " + id + " - filename: " + filename);
            
            // Read from S3
            InputStream imageStream = assetService.readImage(fullKey);
            byte[] imageBytes = imageStream.readAllBytes();
            imageStream.close();
            
            System.out.println("[PlayerController] Returning " + imageBytes.length + " bytes for player ID: " + id);
            
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
                    .body(imageBytes);
        } catch (Exception e) {
            System.err.println("[PlayerController] Error fetching headshot for player ID " + id + ": " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get list of player IDs that have headshots.
     * GET /api/players/manage/headshots/available
     * 
     * @return List of player IDs with headshots
     */
    @GetMapping("/headshots/available")
    public ResponseEntity<List<Long>> getPlayersWithHeadshots() {
        List<PlayerAsset> assets = playerAssetRepository.findAll();
        List<Long> playerIds = assets.stream()
                .map(PlayerAsset::getPlayerId)
                .collect(Collectors.toList());
        System.out.println("[PlayerController] " + playerIds.size() + " players have headshots");
        return ResponseEntity.ok(playerIds);
    }
    
    /**
     * Update an existing player (requires verification code).
     * PUT /api/players/{id}
     * 
     * If the player is pending and a valid verification code is provided, the player will be verified.
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
            
            // If player is not verified and code is valid, verify it
            if (!player.isVerified()) {
                player = playerService.verifyPlayer(id);
            }
            
            return ResponseEntity.ok(new PlayerResponse(player));
        } catch (UnauthorizedException | PlayerNotFoundException e) {
            throw e; // Will be handled by global exception handler
        }
    }
    
    /**
     * Delete a player (requires verification code).
     * DELETE /api/players/{id}
     * 
     * Also evicts the headshot cache for this player.
     * 
     * @param id the player ID
     * @param code the verification secret code
     * @return 204 No Content
     * @throws UnauthorizedException if the code is incorrect (returns 403)
     * @throws PlayerNotFoundException if the player does not exist (returns 404)
     */
    @DeleteMapping("/{id}")
    @CacheEvict(value = "playerHeadshots", key = "#id")
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
