package org.scoula.security.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.common.redis.RedisService;
import org.scoula.security.account.dto.AuthResultDTO;
import org.scoula.security.account.dto.UserInfoDTO;
import org.scoula.security.account.domain.CustomUserDetails;
import org.scoula.security.util.CookieUtil;
import org.scoula.security.util.JwtUtil;
import org.scoula.security.util.JsonResponse;
import org.scoula.user.mapper.UserStatusMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final UserStatusMapper userStatusMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // 1) 사용자 정보
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();
        String email = userDetails.getUsername();

        // 2) 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(userId, email);
        String refreshToken = jwtUtil.generateRefreshToken(userId, email);

        // 3) RT Redis 저장
        try {
            redisService.saveRefreshToken(userId, refreshToken);
            log.info("✅ Redis 저장 성공: {} → {}", userId, refreshToken);
        } catch (Exception e) {
            log.error("❌ Redis 저장 실패: {}", e.getMessage());
        }

        // 4) RT를 httpOnly 쿠키로 발급 (HTTP + same-site 환경)
        //    Vite 프록시/운영 Nginx 프록시로 동일 오리진처럼 보이므로 Lax/secure=false 사용
        CookieUtil.addHttpOnlyCookie(
                response,
                "refreshToken",
                refreshToken,
                7 * 24 * 60 * 60,   // Max-Age = 7일
                false,              // Secure=false (HTTP)
                "Lax"               // SameSite=Lax
        );

        // 5) AT는 응답 헤더로 전달 (FE가 헤더에서 읽어 저장)
        response.setHeader("Authorization", "Bearer " + accessToken);

        // 6) 응답 바디(토큰은 포함하지 않음)
        UserInfoDTO userInfo = UserInfoDTO.from(userDetails.getUser());
        String nickname = userStatusMapper.getNickname(userId);

        AuthResultDTO result = new AuthResultDTO(
                null,               // accessToken 바디 미포함
                null,               // refreshToken 바디 미포함
                userInfo,
                nickname
        );

        CommonResponseDTO<AuthResultDTO> body =
                CommonResponseDTO.success("로그인 성공", result);

        JsonResponse.send(response, body);
    }
}
