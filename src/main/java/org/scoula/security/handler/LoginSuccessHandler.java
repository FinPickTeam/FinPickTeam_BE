package org.scoula.security.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.common.redis.RedisService;
import org.scoula.security.util.JwtUtil;
import org.scoula.security.account.dto.AuthResultDTO;
import org.scoula.security.account.dto.UserInfoDTO;
import org.scoula.security.account.domain.CustomUserDetails;
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

        // 사용자 정보 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        Long id = userDetails.getUser().getId();

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(id, email);
        String refreshToken = jwtUtil.generateRefreshToken(id, email);

        // Redis에 refreshToken 저장
        try {
            redisService.saveRefreshToken(id, refreshToken);
            log.info("✅ Redis 저장 성공: {} → {}", id, refreshToken);
        } catch (Exception e) {
            log.error("❌ Redis 저장 실패: {}", e.getMessage());
        }

        // 유저 정보 DTO 변환
        UserInfoDTO userInfo = UserInfoDTO.from(userDetails.getUser());
        String nickname=userStatusMapper.getNickname(id);

        // 응답용 DTO 생성
        AuthResultDTO authResult = new AuthResultDTO(accessToken, refreshToken, userInfo,nickname);
        CommonResponseDTO<AuthResultDTO> responseDTO = CommonResponseDTO.success("로그인 성공", authResult);

        // JSON 응답 전송
        JsonResponse.send(response, responseDTO);
    }
}
