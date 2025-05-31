package com.pm.authservice.util;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = "Y2hhVEc3aHJnb0hYTzMyZ2ZqVkpiZ1RkZG93YWxrUkM=";
    private final String testEmail = "test@example.com";
    private final String testRole = "USER";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(testSecret);
    }

    @Test
    void testGenerateToken_ValidInputs_ReturnsToken() {
        // When
        String token = jwtUtil.generateToken(testEmail, testRole);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void testGenerateToken_DifferentInputs_GeneratesDifferentTokens() {
        // Given
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";
        String role1 = "USER";
        String role2 = "ADMIN";

        // When
        String token1 = jwtUtil.generateToken(email1, role1);
        String token2 = jwtUtil.generateToken(email2, role2);

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void testValidateToken_ValidToken_DoesNotThrow() {
        // Given
        String token = jwtUtil.generateToken(testEmail, testRole);

        // When & Then
        jwtUtil.validateToken(token); // Should not throw an exception
    }

    @Test
    void testValidateToken_InvalidToken_ThrowsJwtException() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.validateToken(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void testValidateToken_MalformedToken_ThrowsJwtException() {
        // Given
        String malformedToken = "not-a-jwt-token";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.validateToken(malformedToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void testValidateToken_NullToken_ThrowsJwtException() {
        // When & Then
        assertThatThrownBy(() -> jwtUtil.validateToken(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testValidateToken_EmptyToken_ThrowsJwtException() {
        // When & Then
        assertThatThrownBy(() -> jwtUtil.validateToken(""))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testGenerateToken_NullEmail_GeneratesToken() {
        // When
        String token = jwtUtil.generateToken(null, testRole);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void testGenerateToken_NullRole_GeneratesToken() {
        // When
        String token = jwtUtil.generateToken(testEmail, null);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }
} 