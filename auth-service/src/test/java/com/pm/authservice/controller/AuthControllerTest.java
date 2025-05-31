package com.pm.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.LoginResponseDTO;
import com.pm.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequestDTO validLoginRequest;
    private String validToken;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequestDTO();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");
        
        validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature";
    }

    @Test
    @WithMockUser
    void testLogin_ValidCredentials_ReturnsToken() throws Exception {
        // Given
        when(authService.authenticate(any(LoginRequestDTO.class)))
                .thenReturn(Optional.of(validToken));

        // When & Then
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(validToken));
    }

    @Test
    @WithMockUser
    void testLogin_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Given
        when(authService.authenticate(any(LoginRequestDTO.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testLogin_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        LoginRequestDTO invalidRequest = new LoginRequestDTO();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testLogin_ShortPassword_ReturnsBadRequest() throws Exception {
        // Given
        LoginRequestDTO invalidRequest = new LoginRequestDTO();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("123");

        // When & Then
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testValidateToken_ValidToken_ReturnsOk() throws Exception {
        // Given
        when(authService.validateToken(validToken)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/validate")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testValidateToken_InvalidToken_ReturnsUnauthorized() throws Exception {
        // Given
        when(authService.validateToken(any(String.class))).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/validate")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testValidateToken_MissingBearer_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/validate")
                        .header("Authorization", validToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testValidateToken_MissingAuthHeader_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/validate"))
                .andExpect(status().isUnauthorized());
    }
} 