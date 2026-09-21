package com.mastercomputeracademy.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Provide a JWT token. Obtain one from POST /api/admin/auth/login"
)
public class OpenApiConfig {

    /**
     * The public API base URL, injected from the API_BASE_URL environment variable.
     * Falls back to the local development URL when the variable is not set.
     * On production this should be https://api.mastercomputeracademy.org
     */
    @Value("${app.api.base-url:http://localhost:8080}")
    private String apiBaseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = new ArrayList<>();
        servers.add(new Server().url(apiBaseUrl).description("API server"));

        // Only show local dev server when running locally (not in the production Swagger,
        // which is disabled entirely via springdoc.swagger-ui.enabled=false on prod profile).
        if (apiBaseUrl.contains("localhost")) {
            // Already added above – no duplicate needed
        } else {
            // Add local dev as a secondary option for frontend developers
            servers.add(new Server().url("http://localhost:8080").description("Local development"));
        }

        return new OpenAPI()
                .info(new Info()
                        .title("Master Computer Academy – Certificate Verification API")
                        .version("1.0.0")
                        .description("""
                                Backend API for the Master Computer Academy Certificate Verification System.

                                **Public endpoints** (no authentication required):
                                - `GET /api/health` – health check
                                - `GET /api/certificates/verify/{certificateNumber}` – verify a certificate

                                **Admin endpoints** (JWT Bearer token required):
                                - `POST /api/admin/auth/login` – obtain a token
                                - `/api/admin/certificates/**` – manage certificates

                                **Address:** Wathoda Layout, Lok Kalyan Society, Anmol Nagar,
                                Dighori, Nagpur, Maharashtra 440034

                                **Phone:** 9156348591
                                """)
                        .contact(new Contact()
                                .name("Master Computer Academy")
                                .email("admin@mastercomputeracademy.com"))
                        .license(new License()
                                .name("Proprietary")))
                .servers(servers);
    }
}
