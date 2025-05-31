package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.model.User;
import com.pm.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequestDTO loginRequest;
    private String testToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("USER");

        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("plainPassword");

        testToken = "test.jwt.token";
    }

    @Test
    void testAuthenticate_ValidCredentials_ReturnsToken() {
        // Given
        when(userService.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword()))
                .thenReturn(true);
        when(jwtUtil.generateToken(testUser.getEmail(), testUser.getRole()))
                .thenReturn(testToken);

        // When
        Optional<String> result = authService.authenticate(loginRequest);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testToken);
        
        verify(userService).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(jwtUtil).generateToken(testUser.getEmail(), testUser.getRole());
    }

    @Test
    void testAuthenticate_UserNotFound_ReturnsEmpty() {
        // Given
        when(userService.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.empty());

        // When
        Optional<String> result = authService.authenticate(loginRequest);

        // Then
        assertThat(result).isEmpty();
        
        verify(userService).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void testAuthenticate_InvalidPassword_ReturnsEmpty() {
        // Given
        when(userService.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword()))
                .thenReturn(false);

        // When
        Optional<String> result = authService.authenticate(loginRequest);

        // Then
        assertThat(result).isEmpty();
        
        verify(userService).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Given
        doNothing().when(jwtUtil).validateToken(testToken);

        // When
        boolean result = authService.validateToken(testToken);

        // Then
        assertThat(result).isTrue();
        verify(jwtUtil).validateToken(testToken);
    }

    @Test
    void testValidateToken_InvalidToken_ReturnsFalse() {
        // Given
        doThrow(new JwtException("Invalid token")).when(jwtUtil).validateToken(testToken);

        // When
        boolean result = authService.validateToken(testToken);

        // Then
        assertThat(result).isFalse();
        verify(jwtUtil).validateToken(testToken);
    }

    @Test
    void testValidateToken_NullToken_ReturnsFalse() {
        // Given
        doThrow(new JwtException("Invalid token")).when(jwtUtil).validateToken(any());

        // When
        boolean result = authService.validateToken(null);

        // Then
        assertThat(result).isFalse();
    }
} 