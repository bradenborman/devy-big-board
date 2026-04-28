package devybigboard.runners;

import devybigboard.dao.PlayerRepository;
import devybigboard.models.Player;
import devybigboard.services.CfbdStatsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CfbdSyncRunner implements CommandLineRunner {

    private final PlayerRepository playerRepository;
    private final CfbdStatsService cfbdStatsService;

    public CfbdSyncRunner(PlayerRepository playerRepository, CfbdStatsService cfbdStatsService) {
        this.playerRepository = playerRepository;
        this.cfbdStatsService = cfbdStatsService;
    }

    @Override
    public void run(String... args) {
        System.out.println("[CfbdSyncRunner] Skipping startup sync (disabled).");
    }
}
