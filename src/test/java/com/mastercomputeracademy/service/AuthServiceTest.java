package com.mastercomputeracademy.service;

import com.mastercomputeracademy.dto.request.LoginRequest;
import com.mastercomputeracademy.dto.response.LoginResponse;
import com.mastercomputeracademy.entity.AdminUser;
import com.mastercomputeracademy.exception.BadCredentialsException;
import com.mastercomputeracademy.repository.AdminUserRepository;
import com.mastercomputeracademy.security.JwtUtil;
import com.mastercomputeracademy.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService – login tests")
class AuthServiceTest {

    @Mock private AdminUserRepository adminUserRepository;
    @Mock private JwtUtil              jwtUtil;

    // Use a real PasswordEncoder so BCrypt hashing is tested properly
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    @InjectMocks
    private AuthServiceImpl authService;

    private AdminUser adminUser;

    @BeforeEach
    void setUp() {
        // Inject real passwordEncoder via the @InjectMocks approach isn't reliable
        // for non-@Mock beans, so we rebuild the service manually:
        authService = new AuthServiceImpl(adminUserRepository, passwordEncoder, jwtUtil);

        adminUser = AdminUser.builder()
                .id(1L)
                .email("admin@mastercomputer.local")
                .password(passwordEncoder.encode("CorrectPassword@123"))
                .role(AdminUser.Role.ADMIN)
                .build();
    }

    @Test
    @DisplayName("login – valid credentials return a LoginResponse with a Bearer token")
    void login_validCredentials_returnsLoginResponse() {
        when(adminUserRepository.findByEmail("admin@mastercomputer.local"))
                .thenReturn(Optional.of(adminUser));
        when(jwtUtil.generateToken(eq("admin@mastercomputer.local"), eq("ADMIN")))
                .thenReturn("mock-jwt-token");

        LoginResponse response = authService.login(
                new LoginRequest("admin@mastercomputer.local", "CorrectPassword@123"));

        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("login – unknown email throws BadCredentialsException")
    void login_unknownEmail_throwsBadCredentialsException() {
        when(adminUserRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("unknown@example.com", "anyPassword")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("login – wrong password throws BadCredentialsException")
    void login_wrongPassword_throwsBadCredentialsException() {
        when(adminUserRepository.findByEmail("admin@mastercomputer.local"))
                .thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("admin@mastercomputer.local", "WrongPassword!")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
