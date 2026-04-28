package devybigboard.runners;

import devybigboard.dao.PlayerRepository;
import devybigboard.services.CfbdStatsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CfbdSyncRunner implements CommandLineRunner {

    public CfbdSyncRunner(PlayerRepository playerRepository, CfbdStatsService cfbdStatsService) {
        // dependencies kept for future cron usage
    }

    @Override
    public void run(String... args) {
        System.out.println("[CfbdSyncRunner] Skipping startup sync (disabled).");
    }
}
