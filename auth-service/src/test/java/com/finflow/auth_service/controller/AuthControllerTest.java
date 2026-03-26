package com.finflow.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.auth_service.dto.AuthRequest;
import com.finflow.auth_service.entity.User;
import com.finflow.auth_service.repository.UserRepository;
import com.finflow.auth_service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController endpoints using MockMvc.
 * No real database or network calls are made.
 */
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("alice@test.com");
        testUser.setPassword("password123");
        testUser.setRole("APPLICANT");
    }

    // ==================== SIGNUP TESTS ====================

    @Test
    void testSignup_NewUser_Returns200() throws Exception {
        // Email does not exist yet, registration should succeed
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.empty());
        when(authService.saveUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk());
    }

    @Test
    void testSignup_DuplicateEmail_Returns400() throws Exception {
        // Email already registered — should return 400 Bad Request
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));
    }

    // ==================== LOGIN TESTS ====================

    @Test
    void testLogin_ValidCredentials_ReturnsTokenAndUserId() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("alice@test.com");
        authRequest.setPassword("password123");

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(authService.generateToken("alice@test.com")).thenReturn("mockJwtToken");
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockJwtToken"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.role").value("APPLICANT"));
    }

    @Test
    void testLogin_WrongPassword_Returns401() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("alice@test.com");
        authRequest.setPassword("wrongPassword");

        // Spring Security's AuthenticationManager throws BadCredentialsException for wrong password
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== VALIDATE TOKEN TESTS ====================

    @Test
    void testValidateToken_ValidToken_Returns200() throws Exception {
        // validateToken in AuthService does nothing if token is valid (no exception)
        doNothing().when(authService).validateToken("valid.jwt.token");

        mockMvc.perform(get("/validate")
                        .param("token", "valid.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Token is valid"));
    }

    @Test
    void testValidateToken_InvalidToken_Returns500() throws Exception {
        // Invalid token causes JwtUtil to throw a runtime exception
        doThrow(new RuntimeException("Invalid JWT token"))
                .when(authService).validateToken("bad.token.here");

        mockMvc.perform(get("/validate")
                        .param("token", "bad.token.here"))
                .andExpect(status().is5xxServerError());
    }
}
