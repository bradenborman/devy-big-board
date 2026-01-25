package devybigboard.dao;

import devybigboard.models.PlayerAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerAssetRepository extends JpaRepository<PlayerAsset, Long> {
    Optional<PlayerAsset> findByPlayerId(Long playerId);
    void deleteByPlayerId(Long playerId);
}
