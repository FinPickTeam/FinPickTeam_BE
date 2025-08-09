package org.scoula.security.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.redis.RedisService;
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
    private static final String AUTH_EXCEPTION_ATTR = "auth_exception_message"; // ★ 추가

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RedisService redisService;

    private Authentication buildAuthentication(String token) {
        String username = jwtUtil.getEmailFromToken(token); // subject(email)
        UserDetails principal = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private String resolveBearerToken(String header) {
        if (header == null) return null;
        String lower = header.toLowerCase(Locale.ROOT).trim();
        if (!lower.startsWith("bearer ")) return null;
        return header.substring(7).trim();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // OPTIONS 프리플라이트, 로그인/리프레시/스웨거/정적/스케줄러 등 공개 엔드포인트
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || uri.startsWith("/api/auth/login")
                || uri.startsWith("/api/auth/test-login")
                || uri.startsWith("/api/auth/refresh")
                || uri.startsWith("/swagger") || uri.startsWith("/v2") || uri.startsWith("/v3")
                || uri.startsWith("/webjars") || uri.startsWith("/assets")
                || uri.startsWith("/api/challenge/scheduler/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String uri = request.getRequestURI();
        final String bearerHeader = request.getHeader(AUTHORIZATION_HEADER);

        // 1) 헤더 로깅
        if (bearerHeader == null) {
            log.debug("[JWT] Authorization 헤더 없음: uri={}", uri);
        } else {
            log.debug("[JWT] Authorization 헤더 수신: uri={}, 앞 20자={}",
                    uri, bearerHeader.substring(0, Math.min(bearerHeader.length(), 20)));
        }

        // 2) Bearer 토큰 파싱
        String token = resolveBearerToken(bearerHeader);
        if (token == null) {
            // 헤더는 있으나 형식이 잘못됐을 수도 있으니 메시지 남김
            if (bearerHeader != null) {
                log.warn("[JWT] Bearer 형식 오류: uri={}, header 앞 20자={}",
                        uri, bearerHeader.substring(0, Math.min(bearerHeader.length(), 20)));
                request.setAttribute(AUTH_EXCEPTION_ATTR, "Authorization 헤더 형식 오류(Bearer 필요)");
            }
            filterChain.doFilter(request, response);
            return;
        }

        // 3) 블랙리스트 체크
        try {
            if (redisService.isTokenBlacklisted(token)) {
                log.error("[JWT] 블랙리스트 토큰: uri={}", uri);
                request.setAttribute(AUTH_EXCEPTION_ATTR, "블랙리스트에 등록된 토큰입니다.");
                filterChain.doFilter(request, response);
                return;
            }
        } catch (Exception e) {
            // Redis 통신 이슈 등
            log.error("[JWT] 블랙리스트 확인 실패: uri={}, msg={}", uri, e.getMessage());
            request.setAttribute(AUTH_EXCEPTION_ATTR, "인증 처리 중 오류(블랙리스트 확인 실패)");
            filterChain.doFilter(request, response);
            return;
        }

        // 4) 유효성 검사
        boolean valid = false;
        try {
            valid = jwtUtil.validateToken(token);
        } catch (Exception e) {
            // JwtUtil 내부에서 만료/서명오류 등 던지면 여기서 잡아 로그
            log.error("[JWT] 토큰 검증 예외: uri={}, msg={}", uri, e.getMessage());
            request.setAttribute(AUTH_EXCEPTION_ATTR, "유효하지 않은 토큰: " + e.getMessage());
        }

        if (!valid) {
            if (request.getAttribute(AUTH_EXCEPTION_ATTR) == null) {
                log.warn("[JWT] 토큰 검증 실패: uri={}", uri);
                request.setAttribute(AUTH_EXCEPTION_ATTR, "유효하지 않은 토큰입니다.");
            }
            filterChain.doFilter(request, response);
            return;
        }

        // 5) 인증 컨텍스트 설정
        try {
            Authentication authentication = buildAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute(ACCESS_TOKEN_ATTR, token);
            log.debug("[JWT] 인증 컨텍스트 설정 성공: uri={}", uri);
        } catch (Exception e) {
            log.error("[JWT] 인증 컨텍스트 설정 실패: uri={}, msg={}", uri, e.getMessage());
            request.setAttribute(AUTH_EXCEPTION_ATTR, "인증 컨텍스트 설정 실패: " + e.getMessage());
            // 컨텍스트 없이 진행 → 보호자원에서 EntryPoint가 401
        }

        filterChain.doFilter(request, response);
    }
}
