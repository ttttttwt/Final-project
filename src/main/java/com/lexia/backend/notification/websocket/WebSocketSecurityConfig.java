package com.lexia.backend.notification.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

/**
 * WebSocket security configuration for STOMP message broker.
 * Protects WebSocket destinations from unauthorized access.
 *
 * <p>
 * Security rules:
 * </p>
 * <ul>
 * <li>CONNECT/DISCONNECT/HEARTBEAT frames are allowed (handled by JWT
 * interceptor)</li>
 * <li>/app/** - Requires authentication (messages from clients)</li>
 * <li>/user/** - Requires authentication (user-specific queues)</li>
 * <li>/topic/** & /queue/** - Requires authentication (subscriptions)</li>
 * <li>All other messages are denied</li>
 * </ul>
 *
 * @author LEXIA Team
 * @since Sprint 5
 */
@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

        /**
         * Disable CSRF for WebSockets as we use JWT for authentication.
         * This overrides the default csrfChannelInterceptor bean provided by
         * EnableWebSocketSecurity.
         * 
         * @return a no-op ChannelInterceptor
         */
        @Bean
        public ChannelInterceptor csrfChannelInterceptor() {
                return new ChannelInterceptor() {
                };
        }

        /**
         * Configure authorization for WebSocket messages.
         * Uses the new Spring Security 6.x authorization API.
         *
         * @param messages the message security builder
         * @return the authorization manager for WebSocket messages
         */
        @Bean
        AuthorizationManager<Message<?>> messageAuthorizationManager(
                        MessageMatcherDelegatingAuthorizationManager.Builder messages) {
                return messages
                                // Allow lifecycle frames (CONNECT/DISCONNECT/HEARTBEAT/ERROR)
                                // so JWT interceptor can run and handle authentication
                                .simpTypeMatchers(SimpMessageType.CONNECT,
                                                SimpMessageType.HEARTBEAT,
                                                SimpMessageType.UNSUBSCRIBE,
                                                SimpMessageType.DISCONNECT,
                                                SimpMessageType.OTHER)
                                .permitAll()
                                // Allow messages but JWT interceptor will validate authentication
                                .nullDestMatcher().permitAll()
                                // Messages from clients to server endpoints require authentication
                                .simpDestMatchers("/app/**").authenticated()
                                // User-specific destinations require authentication
                                .simpSubscribeDestMatchers("/user/**").authenticated()
                                .simpSubscribeDestMatchers("/queue/**").authenticated()
                                // Topic subscriptions require authentication
                                .simpSubscribeDestMatchers("/topic/**").authenticated()
                                // Deny all other messages by default
                                .anyMessage().denyAll()
                                .build();
        }
}
