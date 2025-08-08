package org.scoula.security.util;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.scoula.config.RootConfig;
import org.scoula.security.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;


@TestPropertySource("classpath:application.properties")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class })
@Log4j2
class JwtProcessorTest {

    @Autowired
    JwtUtil jwtUtil;

    @Test
    void generateToken() {
        Long userId = 1L;
        String email = "user0@example.com";
        String token = jwtUtil.generateAccessToken(userId, email);
        log.info(token);
        assertNotNull(token);
    }

    @Test
    void getEmailFromToken() {
        String token = jwtUtil.generateAccessToken(1L, "user0@example.com");
        String email = jwtUtil.getEmailFromToken(token);
        log.info(email);
        assertEquals("user0@example.com", email);
    }

    @Test
    void validateToken() {
        String token = jwtUtil.generateAccessToken(1L, "user0@example.com");
        boolean isValid = jwtUtil.validateToken(token);
        log.info("Token valid: " + isValid);
        assertTrue(isValid);
    }
}