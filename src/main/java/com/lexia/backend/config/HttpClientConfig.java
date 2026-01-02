package com.lexia.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Configuration for HTTP Client.
 * Provides a shared HttpClient and RestTemplate instance for the application.
 */
@Configuration
public class HttpClientConfig {

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * RestTemplate bean for REST API calls.
     * Used by FlashcardImageServiceImpl for Gemini Imagen API.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
