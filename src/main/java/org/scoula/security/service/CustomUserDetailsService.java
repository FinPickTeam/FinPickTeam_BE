package org.scoula.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.security.account.domain.CustomUserDetails;
import org.scoula.security.account.mapper.UserDetailsMapper;
import org.scoula.user.domain.User;
import org.scoula.user.mapper.UserStatusMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserDetailsMapper mapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("🔐 사용자 인증 요청: " + email);
        User user = mapper.get(email);
        if (user == null) {
            throw new UsernameNotFoundException(email + "은(는) 존재하지 않는 이메일입니다.");
        }

        return new CustomUserDetails(user);
    }
}
