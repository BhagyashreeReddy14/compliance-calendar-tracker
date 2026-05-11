package com.internship.tool.security;

import com.internship.tool.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User user;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 604800000L);

        user = User.builder()
                .email("test@example.com")
                .role(User.Role.ADMIN)
                .build();
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtUtil.generateToken(user);
        assertNotNull(token);
        assertEquals("test@example.com", jwtUtil.extractUsername(token));
    }

    @Test
    void isTokenValid_ShouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken(user);
        assertTrue(jwtUtil.isTokenValid(token, user));
    }
}
