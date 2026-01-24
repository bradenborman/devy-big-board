package devybigboard.config;

import devybigboard.models.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataConfig.class);

    @Bean
    @ConditionalOnProperty(name = "data.initial-load", havingValue = "true")
    public CommandLineRunner commandLineRunner(JdbcTemplate jdbcTemplate) {
        return args -> {
            logger.info("Loading all players into db");
            List<Player> allPlayers = new ArrayList<>();
            int currentYear = Year.now().getValue();

            var resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:players/*.txt");

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (!filename.matches("\\d{4}\\.txt")) continue;

                int fileYear = Integer.parseInt(filename.substring(0, 4));
                if (fileYear >= currentYear) {
                    try (var reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                        reader.lines()
                                .map(String::trim)
                                .filter(line -> !line.isEmpty())
                                .map(line -> parseLine(line, fileYear))
                                .forEach(allPlayers::add);
                    }
                }
            }

            String sql = """
                INSERT INTO players (name, position, team, draftyear)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    draftyear = VALUES(draftyear)
            """;

            for (Player p : allPlayers) {
                jdbcTemplate.update(sql, p.name(), p.position(), p.team(), p.draftyear());
            }

            logger.info("Inserted {} players into DB", allPlayers.size());
        };
    }

    private Player parseLine(String line, int year) {
        String[] parts = line.split("\\s*\\|\\s*");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid player line: " + line);
        }

        return new Player(
                parts[0], // name
                parts[1], // position
                parts[3], // team
                Integer.parseInt(parts[4])
        );
    }
}