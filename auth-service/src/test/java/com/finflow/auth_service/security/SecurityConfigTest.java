package com.finflow.auth_service.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import com.finflow.auth_service.controller.AuthController;
import com.finflow.auth_service.repository.UserRepository;
import com.finflow.auth_service.service.AuthService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for SecurityConfig to verify which routes are publicly accessible
 * and which routes require authentication.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserRepository userRepository;

    // ==================== PUBLIC ROUTES ====================

    @Test
    void testValidateEndpoint_IsPubliclyAccessible_NoTokenRequired() throws Exception {
        // /validate is a public route — must be accessible without any Authorization header
        mockMvc.perform(get("/validate").param("token", "any.token.here"))
                .andExpect(status().is(org.hamcrest.Matchers.not(403)));
        // Note: May return 500 since the token is garbage, but NOT 403 Forbidden
    }

    @Test
    void testSwaggerUi_IsPubliclyAccessible() throws Exception {
        // Swagger UI must be publicly accessible without a token for API exploration
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().is(org.hamcrest.Matchers.not(403)));
    }

    // ==================== PROTECTED ROUTES ====================

    @Test
    void testUnknownProtectedRoute_WithoutToken_Returns403() throws Exception {
        // Any route not in the permit list must be blocked without authentication
        mockMvc.perform(get("/admin/secret-data"))
                .andExpect(status().isForbidden());
    }
}
