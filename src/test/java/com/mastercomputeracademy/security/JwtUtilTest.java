package com.mastercomputeracademy.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtil – token generation and validation")
class JwtUtilTest {

    // 32-byte base64 key (safe for HS256)
    private static final String TEST_SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLW9ubHktMzJieXRlcw==";
    private static final long   EXPIRY_MS   = 3_600_000L; // 1 hour

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET, EXPIRY_MS);
    }

    @Test
    @DisplayName("generateToken returns a non-blank token")
    void generateToken_returnNonBlankToken() {
        String token = jwtUtil.generateToken("admin@test.com", "ADMIN");
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("validateToken returns true for a freshly generated token")
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("admin@test.com", "ADMIN");
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken returns false for a tampered token")
    void validateToken_tamperedToken_returnsFalse() {
        String token = jwtUtil.generateToken("admin@test.com", "ADMIN");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtUtil.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false for a blank string")
    void validateToken_blankString_returnsFalse() {
        assertThat(jwtUtil.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("extractEmail returns the subject used during generation")
    void extractEmail_returnsCorrectSubject() {
        String token = jwtUtil.generateToken("admin@test.com", "ADMIN");
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("admin@test.com");
    }

    @Test
    @DisplayName("extractRole returns the role claim used during generation")
    void extractRole_returnsCorrectRole() {
        String token = jwtUtil.generateToken("admin@test.com", "ADMIN");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("expired token fails validation")
    void validateToken_expiredToken_returnsFalse() throws InterruptedException {
        JwtUtil shortLivedUtil = new JwtUtil(TEST_SECRET, 1L); // 1 ms
        String token = shortLivedUtil.generateToken("admin@test.com", "ADMIN");
        Thread.sleep(50);
        assertThat(shortLivedUtil.validateToken(token)).isFalse();
    }
}
