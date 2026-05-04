package com.example.tool.controller;

import com.example.tool.config.JwtAuthFilter;
import com.example.tool.config.JwtUtil;
import com.example.tool.entity.User;
import com.example.tool.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("encoded-password");
        user.setRole("ROLE_VIEWER");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /auth/register
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /auth/register")
    class Register {

        @Test
        @DisplayName("should return 200 with token on successful registration")
        void register_success_returns200WithToken() throws Exception {
            when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtUtil.generateToken("alice", "ROLE_VIEWER")).thenReturn("mock-jwt-token");

            String body = """
                    {"username": "alice", "password": "secret123"}
                    """;

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                    .andExpect(jsonPath("$.username").value("alice"))
                    .andExpect(jsonPath("$.role").value("ROLE_VIEWER"));
        }

        @Test
        @DisplayName("should return 400 when username already exists")
        void register_duplicateUsername_returns400() throws Exception {
            when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

            String body = """
                    {"username": "alice", "password": "secret123"}
                    """;

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when username is blank")
        void register_blankUsername_returns400() throws Exception {
            String body = """
                    {"username": "", "password": "secret123"}
                    """;

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when password is too short")
        void register_shortPassword_returns400() throws Exception {
            String body = """
                    {"username": "alice", "password": "abc"}
                    """;

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /auth/login
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /auth/login")
    class Login {

        @Test
        @DisplayName("should return 200 with token on successful login")
        void login_success_returns200WithToken() throws Exception {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
            when(jwtUtil.generateToken("alice", "ROLE_VIEWER")).thenReturn("mock-jwt-token");

            String body = """
                    {"username": "alice", "password": "secret123"}
                    """;

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                    .andExpect(jsonPath("$.username").value("alice"))
                    .andExpect(jsonPath("$.role").value("ROLE_VIEWER"));
        }

        @Test
        @DisplayName("should return 401 on bad credentials")
        void login_badCredentials_returns401() throws Exception {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            String body = """
                    {"username": "alice", "password": "wrong-password"}
                    """;

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 400 when username is blank")
        void login_blankUsername_returns400() throws Exception {
            String body = """
                    {"username": "", "password": "secret123"}
                    """;

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when password is blank")
        void login_blankPassword_returns400() throws Exception {
            String body = """
                    {"username": "alice", "password": ""}
                    """;

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }
}
