package org.scoula.security.handler;

import lombok.extern.log4j.Log4j2;
import org.scoula.security.util.JsonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Log4j2
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String auth = request.getHeader("Authorization");

        log.error("========== 인증 에러 ============");
        log.error("요청 URI: {}", request.getRequestURI());
        log.error("Authorization 헤더 존재 여부: {}", (auth != null));
        log.error("Authorization 헤더 값(앞 20자): {}",
                auth != null ? auth.substring(0, Math.min(auth.length(), 20)) : "null");
        log.error("예외 메시지: {}", authException != null ? authException.getMessage() : "null");

        // 🔸 필터에서 넘긴 상세 사유가 있으면 그걸 우선 사용
        String detail = (String) request.getAttribute("auth_exception_message");
        String message = (detail != null)
                ? detail
                : (authException != null ? authException.getMessage() : "unknown");

        JsonResponse.sendError(response, HttpStatus.UNAUTHORIZED, "인증 실패: " + message);
    }


}
