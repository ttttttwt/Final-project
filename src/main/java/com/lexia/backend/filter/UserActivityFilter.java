package com.lexia.backend.filter;

import com.lexia.backend.auth.AuthenticatedUserDetails;
import com.lexia.backend.service.UserSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to update user's last activity time on every authenticated request.
 * This ensures accurate "online" status tracking.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserActivityFilter extends OncePerRequestFilter {

    private final UserSessionService userSessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() 
                    && authentication.getPrincipal() instanceof AuthenticatedUserDetails) {
                
                AuthenticatedUserDetails userDetails = (AuthenticatedUserDetails) authentication.getPrincipal();
                UUID userId = userDetails.getUser().getId();
                
                // Update last activity time asynchronously to not block the request
                // Note: For simplicity in this version, we're calling it directly.
                // Ideally, this should be throttled (e.g., once per minute) or async.
                // Given the requirement to fix "0 online users", we'll update it here.
                // To avoid excessive DB writes, we could check a "lastUpdated" timestamp in a cache,
                // but since we don't have a cache layer set up yet, we'll rely on the service.
                
                // Only update for API requests, ignore static resources if any
                String path = request.getRequestURI();
                if (path.startsWith("/api/")) {
                    userSessionService.updateLastActivity(userId);
                }
            }
        } catch (Exception e) {
            // Log but don't fail the request
            log.warn("Failed to update user activity: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
