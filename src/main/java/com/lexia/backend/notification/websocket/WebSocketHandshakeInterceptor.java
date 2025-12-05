package com.lexia.backend.notification.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket Handshake Interceptor for debugging.
 * Logs HTTP handshake details before WebSocket upgrade.
 *
 * @author LEXIA Team
 * @since Sprint 5
 */
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes) {

        String origin = request.getHeaders().getOrigin();
        String remoteAddress = request.getRemoteAddress() != null
                ? request.getRemoteAddress().toString()
                : "unknown";
        String uri = request.getURI().toString();

        // Get client info from servlet request if available
        String userAgent = "unknown";
        if (request instanceof ServletServerHttpRequest servletRequest) {
            userAgent = servletRequest.getServletRequest().getHeader("User-Agent");
        }

        LOG.info("🤝 WebSocket Handshake START - URI: {}, Origin: {}, RemoteAddr: {}",
                uri, origin, remoteAddress);
        LOG.debug("   User-Agent: {}", userAgent);
        LOG.debug("   Headers: {}", request.getHeaders());

        return true; // Allow handshake to proceed
    }

    @Override
    public void afterHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            Exception exception) {

        if (exception != null) {
            LOG.error("❌ WebSocket Handshake FAILED - URI: {}, Error: {}",
                    request.getURI(), exception.getMessage());
        } else {
            LOG.info("✅ WebSocket Handshake SUCCESS - URI: {}", request.getURI());
        }
    }
}
