package com.mastercomputeracademy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CORS configuration.
 *
 * Allowed origins are driven by the FRONTEND_URL environment variable.
 * Multiple origins can be specified as a comma-separated list:
 *
 *   FRONTEND_URL=https://mastercomputeracademy.org,http://localhost:5173
 *
 * Production example (single origin):
 *   FRONTEND_URL=https://mastercomputeracademy.org
 *
 * Development default (when FRONTEND_URL is not set):
 *   http://localhost:5173  (Vite dev server)
 *
 * Rules enforced:
 *   - Wildcards (*) are NEVER used as allowed origins.
 *   - allowCredentials is true, which requires explicit origins (not *).
 *   - OPTIONS pre-flight requests are permitted in SecurityConfig.
 */
@Configuration
@Slf4j
public class CorsConfig {

    /**
     * Comma-separated list of allowed origins.
     * Injected from the FRONTEND_URL environment variable.
     */
    @Value("${app.cors.allowed-origins}")
    private String allowedOriginsRaw;

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origins = parseOrigins(allowedOriginsRaw);

        log.info("CORS – allowed origins: {}", origins);

        CorsConfiguration config = new CorsConfiguration();

        // Explicit origin list – never wildcard
        config.setAllowedOrigins(origins);

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control"
        ));

        config.setExposedHeaders(List.of("Authorization"));

        // allowCredentials=true requires an explicit origin list (not *)
        config.setAllowCredentials(true);

        config.setMaxAge(3600L); // Pre-flight cache: 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Splits the raw comma-separated origins string and trims whitespace.
     * Returns a non-empty immutable list.
     */
    private List<String> parseOrigins(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of("http://localhost:5173");
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
