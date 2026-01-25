package devybigboard.controllers;

import devybigboard.dao.PlayerAssetRepository;
import devybigboard.models.PlayerAsset;
import devybigboard.services.AssetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/S3-delete")
public class S3MaintenanceController {
    
    private final AssetService assetService;
    private final PlayerAssetRepository playerAssetRepository;
    
    public S3MaintenanceController(AssetService assetService, PlayerAssetRepository playerAssetRepository) {
        this.assetService = assetService;
        this.playerAssetRepository = playerAssetRepository;
    }
    
    /**
     * Delete all player headshots from S3 and database.
     * DELETE /S3-delete
     * 
     * WARNING: This will permanently delete all player headshot images!
     * 
     * @return Summary of deletion operation
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteAllAssets() {
        System.out.println("[S3Maintenance] Starting deletion of all assets...");
        
        Map<String, Object> result = new HashMap<>();
        int deletedFromS3 = 0;
        int failedS3Deletions = 0;
        
        try {
            // Get all assets from database
            List<PlayerAsset> assets = playerAssetRepository.findAll();
            int totalAssets = assets.size();
            
            System.out.println("[S3Maintenance] Found " + totalAssets + " assets in database");
            
            // Delete each file from S3
            for (PlayerAsset asset : assets) {
                try {
                    String fullKey = "players/headshots/" + asset.getFilename();
                    System.out.println("[S3Maintenance] Deleting from S3: " + fullKey);
                    assetService.deleteImage(fullKey);
                    deletedFromS3++;
                } catch (Exception e) {
                    System.err.println("[S3Maintenance] Failed to delete from S3: " + asset.getFilename() + " - " + e.getMessage());
                    failedS3Deletions++;
                }
            }
            
            // Delete all records from database
            playerAssetRepository.deleteAll();
            System.out.println("[S3Maintenance] Deleted all records from database");
            
            result.put("success", true);
            result.put("totalAssets", totalAssets);
            result.put("deletedFromS3", deletedFromS3);
            result.put("failedS3Deletions", failedS3Deletions);
            result.put("deletedFromDatabase", totalAssets);
            result.put("message", "All assets have been deleted");
            
            System.out.println("[S3Maintenance] Deletion complete: " + deletedFromS3 + " deleted, " + failedS3Deletions + " failed");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("[S3Maintenance] Error during deletion: " + e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("deletedFromS3", deletedFromS3);
            result.put("failedS3Deletions", failedS3Deletions);
            return ResponseEntity.status(500).body(result);
        }
    }
}
