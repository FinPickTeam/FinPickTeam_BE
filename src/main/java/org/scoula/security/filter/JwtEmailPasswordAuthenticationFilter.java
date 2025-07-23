package org.scoula.security.filter;

import lombok.extern.log4j.Log4j2;
import org.scoula.security.account.dto.UserLoginRequestDTO;
import org.scoula.security.handler.LoginFailureHandler;
import org.scoula.security.handler.LoginSuccessHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Log4j2
@Component
public class JwtEmailPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    public JwtEmailPasswordAuthenticationFilter(
//            AuthenticationManager authenticationManager,
            LoginSuccessHandler loginSuccessHandler,
            LoginFailureHandler loginFailureHandler
    ) {
//        super(authenticationManager);
        this.loginSuccessHandler = loginSuccessHandler;
        this.loginFailureHandler = loginFailureHandler;
        setFilterProcessesUrl("/api/auth/login");
        setAuthenticationSuccessHandler(loginSuccessHandler);
        setAuthenticationFailureHandler(loginFailureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        log.info("🚨 [JwtEmailPasswordAuthenticationFilter] 로그인 요청 감지됨");
        UserLoginRequestDTO login = UserLoginRequestDTO.of(request);
        log.info("📥 로그인 시도 - 이메일: {}, 비밀번호: {}", login.getEmail(), login.getPassword());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword());

        return getAuthenticationManager().authenticate(authenticationToken);
    }
}
