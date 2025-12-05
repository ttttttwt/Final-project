package com.lexia.backend.notification.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * WebSocket Event Listener for debugging and monitoring.
 * Logs all WebSocket connection events.
 *
 * @author LEXIA Team
 * @since Sprint 5
 */
@Component
public class WebSocketEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketEventListener.class);

    /**
     * Called when a STOMP CONNECT frame is received (before authentication).
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String authHeader = headerAccessor.getFirstNativeHeader("Authorization");

        LOG.info("📥 WebSocket CONNECT attempt - Session: {}, Has Auth: {}",
                sessionId,
                authHeader != null ? "Yes (Bearer " + (authHeader.length() > 20 ? "..." : "") + ")" : "No");
    }

    /**
     * Called when a WebSocket connection is fully established (after
     * authentication).
     */
    @EventListener
    public void handleWebSocketConnectedListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String user = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "anonymous";

        LOG.info("✅ WebSocket CONNECTED - Session: {}, User: {}", sessionId, user);
    }

    /**
     * Called when a client subscribes to a destination.
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        String user = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "anonymous";

        LOG.info("📬 WebSocket SUBSCRIBE - Session: {}, User: {}, Destination: {}",
                sessionId, user, destination);
    }

    /**
     * Called when a WebSocket connection is closed.
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String user = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "anonymous";
        String closeStatus = event.getCloseStatus() != null ? event.getCloseStatus().toString() : "unknown";

        LOG.info("🔌 WebSocket DISCONNECT - Session: {}, User: {}, CloseStatus: {}",
                sessionId, user, closeStatus);
    }
}
