package com.lexia.backend.notification.websocket;

import com.lexia.backend.auth.JwtTokenProvider;
import com.lexia.backend.entity.User;
import com.lexia.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private static final String CONTEXT_APPLIED_HEADER = "LEXIA_WS_CONTEXT_APPLIED";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public WebSocketAuthInterceptor(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            LOG.debug("Processing STOMP CONNECT frame for session {}", accessor.getSessionId());
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                LOG.warn("WebSocket connection rejected: No valid Authorization header");
                throw new MessagingException(
                        "Missing or invalid Authorization header. Please provide a valid Bearer token.");
            }

            String token = authHeader.substring(7);

            try {
                if (!jwtTokenProvider.validateToken(token)) {
                    LOG.warn("WebSocket connection rejected: Invalid JWT token");
                    throw new MessagingException("Invalid or expired JWT token.");
                }

                UUID userId = jwtTokenProvider.getUserIdFromToken(token);
                String email = jwtTokenProvider.getEmailFromToken(token);

                // Load user with roles eagerly to avoid LazyInitializationException
                User user = userRepository.findByIdWithRoles(userId).orElse(null);
                if (user == null) {
                    LOG.warn("WebSocket connection rejected: User not found: {}", userId);
                    throw new MessagingException("User not found.");
                }

                if (!user.getIsActive()) {
                    LOG.warn("WebSocket connection rejected: User inactive: {}", userId);
                    throw new MessagingException("User account is inactive.");
                }

                // Create authentication token
                List<SimpleGrantedAuthority> authorities = user.getUserRoles().stream()
                        .map(ur -> new SimpleGrantedAuthority("ROLE_" + ur.getRole().getName()))
                        .toList();

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        user, null, authorities);

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);

                if (accessor.isMutable()) {
                    accessor.setUser(auth);
                    accessor.setHeader(CONTEXT_APPLIED_HEADER, Boolean.TRUE);
                    LOG.info("✅ WebSocket authenticated for user: {} ({})", email, userId);
                    return message;
                } else {
                    StompHeaderAccessor newAccessor = StompHeaderAccessor.create(StompCommand.CONNECT);
                    newAccessor.copyHeaders(accessor.getMessageHeaders());
                    newAccessor.setUser(auth);
                    newAccessor.setHeader(CONTEXT_APPLIED_HEADER, Boolean.TRUE);

                    LOG.info("✅ WebSocket authenticated for user: {} ({}) - Recreated immutable message", email,
                            userId);
                    return MessageBuilder.createMessage(message.getPayload(), newAccessor.getMessageHeaders());
                }

            } catch (MessagingException e) {
                LOG.error("❌ WebSocket authentication failed: {}", e.getMessage());
                throw e; // Re-throw to trigger STOMP ERROR frame
            } catch (Exception e) {
                LOG.error("❌ WebSocket authentication error", e);
                throw new MessagingException("Authentication failed: " + e.getMessage(), e);
            }
        }

        return message;
    }

    @Override
    public void afterSendCompletion(@NonNull Message<?> message, @NonNull MessageChannel channel,
            boolean sent, @Nullable Exception ex) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && Boolean.TRUE.equals(accessor.getHeader(CONTEXT_APPLIED_HEADER))) {
            SecurityContextHolder.clearContext();
            if (accessor.isMutable()) {
                accessor.removeHeader(CONTEXT_APPLIED_HEADER);
            }
        }
    }
}
