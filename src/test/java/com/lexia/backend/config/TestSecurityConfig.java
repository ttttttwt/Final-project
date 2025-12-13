package com.lexia.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Test configuration that registers the AuthenticationPrincipalArgumentResolver
 * for @WebMvcTest classes that need @AuthenticationPrincipal support.
 * 
 * <p>When using @WebMvcTest with addFilters=false, the security filters are disabled
 * but the @AuthenticationPrincipal annotation still needs to resolve the principal
 * from the SecurityContext. This configuration ensures the resolver is registered.</p>
 * 
 * @author LEXIA Team
 * @since Sprint 5
 */
@TestConfiguration
public class TestSecurityConfig implements WebMvcConfigurer {
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthenticationPrincipalArgumentResolver());
    }
}
