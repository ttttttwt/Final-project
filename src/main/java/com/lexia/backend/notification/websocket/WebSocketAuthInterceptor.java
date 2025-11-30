package com.lexia.backend.notification.websocket;

import com.lexia.backend.auth.JwtTokenProvider;
import com.lexia.backend.entity.User;
import com.lexia.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * WebSocket authentication interceptor.
 * Validates JWT tokens on STOMP CONNECT frames and sets the user principal.
 *
 * <p>
 * Authentication flow:
 * </p>
 * <ol>
 * <li>Client sends CONNECT with Authorization header</li>
 * <li>Interceptor extracts and validates JWT token</li>
 * <li>If valid, sets authenticated principal for the session</li>
 * <li>If invalid, connection is rejected</li>
 * </ol>
 *
 * @author LEXIA Team
 * @since Sprint 5
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public WebSocketAuthInterceptor(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                LOG.warn("WebSocket connection rejected: No valid Authorization header");
                throw new org.springframework.security.access.AccessDeniedException(
                        "Missing or invalid Authorization header. Please provide a valid Bearer token.");
            }

            String token = authHeader.substring(7);

            try {
                if (!jwtTokenProvider.validateToken(token)) {
                    LOG.warn("WebSocket connection rejected: Invalid JWT token");
                    throw new org.springframework.security.access.AccessDeniedException(
                            "Invalid or expired JWT token.");
                }

                UUID userId = jwtTokenProvider.getUserIdFromToken(token);
                String email = jwtTokenProvider.getEmailFromToken(token);

                // Load user to verify existence and get roles
                User user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    LOG.warn("WebSocket connection rejected: User not found: {}", userId);
                    throw new org.springframework.security.access.AccessDeniedException(
                            "User not found.");
                }

                if (!user.getIsActive()) {
                    LOG.warn("WebSocket connection rejected: User inactive: {}", userId);
                    throw new org.springframework.security.access.AccessDeniedException(
                            "User account is inactive.");
                }

                // Create authentication token
                List<SimpleGrantedAuthority> authorities = user.getUserRoles().stream()
                        .map(ur -> new SimpleGrantedAuthority("ROLE_" + ur.getRole().getName()))
                        .toList();

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        user, null, authorities);

                accessor.setUser(auth);
                LOG.info("WebSocket authenticated for user: {}", email);

            } catch (org.springframework.security.access.AccessDeniedException e) {
                throw e; // Re-throw access denied exceptions
            } catch (Exception e) {
                LOG.error("WebSocket authentication error", e);
                throw new org.springframework.security.access.AccessDeniedException(
                        "Authentication failed: " + e.getMessage());
            }
        }

        return message;
    }
}
