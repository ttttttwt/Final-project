package com.lexia.backend.notification.websocket;

import org.springframework.context.annotation.Configuration;
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
 * @author LEXIA Team
 * @since Sprint 5
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor authInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for subscriptions to /topic and /queue
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages from clients to server
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                        "http://localhost:3000", // Next.js web
                        "http://localhost:5173", // Vite admin
                        "http://localhost:19006", // Expo web
                        "https://lexia.app" // Production
                )
                .withSockJS();

        // Raw WebSocket endpoint (for mobile clients)
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add authentication interceptor to validate JWT on CONNECT
        registration.interceptors(authInterceptor);
    }
}
