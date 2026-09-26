package com.mastercomputeracademy.config;

import com.mastercomputeracademy.security.JwtAuthenticationEntryPoint;
import com.mastercomputeracademy.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Wire Spring Security's CORS support to our CorsConfigurationSource bean.
            // This ensures the CorsFilter runs BEFORE the authorization checks, so that
            // OPTIONS preflight requests receive the correct CORS headers and are never
            // blocked with 401 by the security filter chain.
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // Disable CSRF – stateless JWT API, no session/cookies
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless session – no HttpSession created
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Custom 401 handler
            .exceptionHandling(ex ->
                ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // CORS pre-flight requests must always be permitted – browsers send
                // OPTIONS before every cross-origin request and must receive 200, not 401.
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public endpoints – no token required
                .requestMatchers(HttpMethod.GET,  "/api/health").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/certificates/verify/**").permitAll()
                // Student self-service portal – public lookup by studentId + mobile
                .requestMatchers(HttpMethod.GET,  "/api/students/lookup").permitAll()
                // Admin auth – open (login does not require a token)
                .requestMatchers(HttpMethod.POST, "/api/admin/auth/login").permitAll()
                // Swagger / OpenAPI docs
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/api-docs",
                    "/api-docs/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**"
                ).permitAll()
                // All other admin endpoints require ROLE_ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Reject everything else
                .anyRequest().authenticated()
            )

            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
