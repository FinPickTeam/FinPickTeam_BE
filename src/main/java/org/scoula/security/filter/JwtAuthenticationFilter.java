package org.scoula.security.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.redis.RedisService;
import org.scoula.security.Exception.BlackListException;
import org.scoula.security.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

@Component
@Log4j2
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String ACCESS_TOKEN_ATTR = "ACCESS_TOKEN";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RedisService redisService;

    private Authentication buildAuthentication(String token) {
        // subject(email)로 UserDetails 로드 (CustomUserDetails)
        String username = jwtUtil.getEmailFromToken(token);
        UserDetails principal = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private String resolveBearerToken(String header) {
        if (header == null) return null;
        // 대소문자/공백 안전 처리
        String lower = header.toLowerCase(Locale.ROOT).trim();
        if (!lower.startsWith("bearer ")) return null;
        return header.substring(7).trim();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String bearerHeader = request.getHeader(AUTHORIZATION_HEADER);
        String token = resolveBearerToken(bearerHeader);

        if (token != null) {
            // 블랙리스트 체크
            if (redisService.isTokenBlacklisted(token)) {
                throw new BlackListException("블랙리스트에 등록된 토큰입니다.");
            }

            // 유효 토큰이면 SecurityContext 세팅
            if (jwtUtil.validateToken(token)) {
                Authentication authentication = buildAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 컨트롤러에서 필요할 수 있는 원본 액세스 토큰을 요청 속성에 저장
                request.setAttribute(ACCESS_TOKEN_ATTR, token);
            }
        }

        filterChain.doFilter(request, response);
    }
}
