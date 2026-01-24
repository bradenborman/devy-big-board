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
            // TODO: Refactor this to work with new Player entity structure
            // The new Player entity uses: name, position, team, college, verified, etc.
            // This old code was using: name, position, team, draftyear
            // Will be reimplemented in later tasks
            
            logger.info("Database initialization skipped - will be reimplemented with new entity structure");
        };
    }
}