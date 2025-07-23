package org.scoula.security.account.mapper;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.scoula.config.RootConfig;
import org.scoula.security.config.SecurityConfig;
import org.scoula.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class })
@Log4j2
class UserDetailsMapperTest {

    @Autowired
    private UserDetailsMapper mapper;

    @Test
    void get() {
        User user = mapper.get("admin@example.com");  // 테스트용 이메일
        assertNotNull(user);
        log.info(user);
        assertEquals("admin@example.com", user.getEmail());
        assertTrue(user.getIsActive());
    }
}
