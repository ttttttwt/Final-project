package com.lexia.backend.notification.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time notifications.
 * Configures STOMP message broker for notification delivery.
 *
 * <p>
 * Endpoints:
 * </p>
 * <ul>
 * <li>/ws - WebSocket connection endpoint with SockJS fallback</li>
 * <li>/user/queue/notifications - Personal notification queue</li>
 * <li>/topic/announcements - Broadcast announcements</li>
 * </ul>
 *
 * <p>
 * Allowed origins are configured via application.properties:
 * {@code lexia.websocket.allowed-origins}
 * </p>
 *
 * @author LEXIA Team
 * @since Sprint 5
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor authInterceptor;

    /**
     * Allowed origins for WebSocket connections.
     * Configured via application.properties: lexia.websocket.allowed-origins
     * Default: localhost origins for development
     */
    @Value("${lexia.websocket.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:19006}")
    private String allowedOriginsString;

    public WebSocketConfig(WebSocketAuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // Enable simple broker for subscriptions to /topic and /queue
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages from clients to server
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Parse allowed origins from configuration
        String[] allowedOrigins = allowedOriginsString.split(",");

        // WebSocket endpoint with SockJS fallback (for web browsers)
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins)
                .withSockJS();

        // Raw WebSocket endpoint for mobile clients
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins);
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        // Add authentication interceptor to validate JWT on CONNECT
        registration.interceptors(authInterceptor);
    }
}
