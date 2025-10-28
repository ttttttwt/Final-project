package com.lexia.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 configuration for Swagger UI documentation.
 * 
 * Configures API documentation with JWT Bearer authentication scheme.
 * Swagger UI will be available at /swagger-ui.html
 * OpenAPI JSON spec will be available at /api-docs
 * 
 * @author LEXIA Team
 * @version 1.0
 * @since 2025-10-28
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Configures OpenAPI documentation with project info, security schemes, and
     * server URLs.
     * 
     * @return configured OpenAPI bean
     */
    @Bean
    public OpenAPI lexiaOpenAPI() {
        // Define JWT Bearer security scheme
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT Bearer token authentication. Obtain token via /api/v1/auth/login endpoint.");

        // Define security requirement to reference the scheme
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme))
                .addSecurityItem(securityRequirement);
    }

    /**
     * Builds API information metadata.
     * 
     * @return API information object
     */
    private Info apiInfo() {
        return new Info()
                .title("LEXIA API Documentation")
                .version("1.0.0")
                .description("""
                        RESTful API for LEXIA - AI-Powered English Learning Platform

                        This API provides endpoints for:
                        - User authentication (register, login, token refresh)
                        - User profile management
                        - AI-powered learning features (coming soon)

                        ## Authentication
                        Most endpoints require JWT Bearer token authentication.
                        1. Register a new user via POST /api/v1/auth/register
                        2. Login via POST /api/v1/auth/login to obtain access token
                        3. Use the access token in Authorization header: `Bearer <token>`

                        ## Token Lifecycle
                        - Access Token: Valid for 15 minutes
                        - Refresh Token: Valid for 7 days
                        - Use POST /api/v1/auth/refresh to obtain new tokens
                        """)
                .contact(apiContact())
                .license(apiLicense());
    }

    /**
     * Defines API contact information.
     * 
     * @return contact information object
     */
    private Contact apiContact() {
        return new Contact()
                .name("LEXIA Development Team")
                .email("support@lexia.com")
                .url("https://github.com/lexia/backend");
    }

    /**
     * Defines API license information.
     * 
     * @return license information object
     */
    private License apiLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Configures server URLs for API documentation.
     * 
     * @return list of server configurations
     */
    private List<Server> serverList() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Local Development Server");

        return List.of(localServer);
    }
}
