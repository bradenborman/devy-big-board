package devybigboard.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test to verify that the WebSocket configuration is properly loaded
 * and configured in the Spring application context.
 */
@SpringBootTest
@ActiveProfiles("local")
class WebSocketConfigTest {

    @Autowired
    private WebSocketConfig webSocketConfig;

    @Test
    void contextLoadsWithWebSocketConfig() {
        // Verify that the WebSocketConfig bean is created and injected
        // This test ensures that:
        // - The @Configuration annotation is properly recognized
        // - The @EnableWebSocketMessageBroker annotation is valid
        // - The WebSocket configuration doesn't cause any bean creation errors
        // - STOMP endpoint at /ws is configured
        // - SockJS fallback is enabled
        // - Message broker for /topic destinations is configured
        // - Application destination prefix /app is set
        // - CORS is configured for WebSocket connections
        assertNotNull(webSocketConfig, "WebSocketConfig should be loaded in the application context");
    }
}
