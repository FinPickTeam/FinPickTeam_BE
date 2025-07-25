package org.scoula.security.util;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.scoula.config.RootConfig;
import org.scoula.security.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class })
@Log4j2
class JwtProcessorTest {

    @Autowired
    JwtUtil jwtUtil;

    @Test
    void generateToken() {
        String email = "user0@example.com";
        String token = jwtUtil.generateAccessToken(email);
        log.info(token);
        assertNotNull(token);
    }

    @Test
    void getEmailFromToken() {
        String token = jwtUtil.generateAccessToken("user0@example.com");
        String email = jwtUtil.getEmailFromToken(token);
        log.info(email);
        assertEquals("user0@example.com", email);
    }

    @Test
    void validateToken() {
        String token = jwtUtil.generateAccessToken("user0@example.com");
        boolean isValid = jwtUtil.validateToken(token);
        log.info("Token valid: " + isValid);
        assertTrue(isValid);
    }
}
