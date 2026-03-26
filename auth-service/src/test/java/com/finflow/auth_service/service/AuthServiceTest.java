package com.finflow.auth_service.service;

import com.finflow.auth_service.entity.User;
import com.finflow.auth_service.repository.UserRepository;
import com.finflow.auth_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setPassword("password");
        testUser.setRole("APPLICANT");
    }

    @Test
    void testSaveUser() {
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(repository.save(any(User.class))).thenReturn(testUser);

        User savedUser = authService.saveUser(testUser);

        assertNotNull(savedUser);
        assertEquals("encodedPassword", testUser.getPassword());
        assertEquals("APPLICANT", testUser.getRole());
        verify(repository, times(1)).save(testUser);
        verify(passwordEncoder, times(1)).encode("password");
    }

    @Test
    void testSaveUser_DefaultRole() {
        testUser.setRole(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(repository.save(any(User.class))).thenReturn(testUser);

        User savedUser = authService.saveUser(testUser);

        assertNotNull(savedUser);
        assertEquals("APPLICANT", testUser.getRole(), "Role should default to APPLICANT");
    }

    @Test
    void testGenerateToken_Success() {
        when(repository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken("test@test.com", "APPLICANT", 1L)).thenReturn("mockToken");

        String token = authService.generateToken("test@test.com");

        assertEquals("mockToken", token);
        verify(repository, times(1)).findByEmail("test@test.com");
        verify(jwtUtil, times(1)).generateToken("test@test.com", "APPLICANT", 1L);
    }

    @Test
    void testGenerateToken_UserNotFound() {
        when(repository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.generateToken("unknown@test.com"));
        verify(repository, times(1)).findByEmail("unknown@test.com");
        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyLong());
    }

    @Test
    void testSaveUser_PasswordIsNeverStoredAsRawText() {
        // SECURITY: Raw passwords must NEVER be persisted in the database.
        // After saveUser(), the password field must be replaced with the encoded version.
        when(passwordEncoder.encode("password")).thenReturn("$2a$10$encodedBcryptHash");
        when(repository.save(any(User.class))).thenReturn(testUser);

        authService.saveUser(testUser);

        // The user's password field must now contain the encoded value, not "password"
        assertNotEquals("password", testUser.getPassword(),
                "Raw password must never be stored — it must be replaced with encoded hash");
        assertEquals("$2a$10$encodedBcryptHash", testUser.getPassword(),
                "Password must be replaced with BCrypt encoded hash");
    }

    @Test
    void testValidateToken_DelegatesToJwtUtil() {
        // AuthService.validateToken() must always delegate to JwtUtil for actual validation.
        // This ensures the JWT signature and expiry check is never bypassed.
        doNothing().when(jwtUtil).validateToken("some.jwt.token");

        authService.validateToken("some.jwt.token");

        verify(jwtUtil, times(1)).validateToken("some.jwt.token");
    }
}
