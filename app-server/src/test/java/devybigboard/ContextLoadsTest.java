package devybigboard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test to verify that the Spring application context loads successfully
 * with all beans properly configured.
 */
@SpringBootTest
@ActiveProfiles("local")
class ContextLoadsTest {

    @Test
    void contextLoads() {
        // This test will fail if the application context cannot be loaded
        // due to missing beans, circular dependencies, or configuration issues
    }

}
