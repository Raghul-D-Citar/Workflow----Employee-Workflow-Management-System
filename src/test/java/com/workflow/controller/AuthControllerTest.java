package com.workflow.controller;

import com.workflow.dto.auth.AuthResponse;
import com.workflow.dto.auth.LoginRequest;
import com.workflow.dto.auth.RegisterRequest;
import com.workflow.entity.enums.Role;
import com.workflow.exception.GlobalExceptionHandler;
import com.workflow.exception.InvalidRequestException;
import com.workflow.service.AuthService;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest {

    private AuthService authService;
    private AuthController authController;
    private MockMvc mockMvc;

    @BeforeMethod
    public void setUp() {
        authService = Mockito.mock(AuthService.class);
        authController = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void registerSuccess() throws Exception {
        String jsonPayload = """
                {
                    "username": "sarah",
                    "email": "sarah@example.com",
                    "password": "password123",
                    "firstName": "Sarah",
                    "lastName": "Connor",
                    "role": "EMPLOYEE"
                }
                """;

        AuthResponse authResponse = new AuthResponse("mock.jwt.token", 1L, "sarah", "sarah@example.com", Role.EMPLOYEE);
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("sarah"));
    }

    @Test
    public void registerValidationError() throws Exception {
        String invalidPayload = "{}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    public void loginSuccess() throws Exception {
        String jsonPayload = """
                {
                    "username": "sarah",
                    "password": "password123"
                }
                """;

        AuthResponse authResponse = new AuthResponse("mock.jwt.token", 1L, "sarah", "sarah@example.com", Role.EMPLOYEE);
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.username").value("sarah"));
    }

    @Test
    public void loginValidationError() throws Exception {
        String invalidPayload = "{}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    public void malformedJsonReturnsConsistentBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request body is invalid or malformed"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    public void loginInvalidCredentials() throws Exception {
        String jsonPayload = """
                {
                    "username": "sarah",
                    "password": "wrongpassword"
                }
                """;

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidRequestException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }
}
