package org.scoula.user.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.redis.RedisService;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.security.account.dto.UserLoginRequestDTO;
import org.scoula.security.util.CookieUtil;
import org.scoula.user.dto.TokenResponseDTO;
import org.scoula.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RedisService redisService;

    @PostMapping("/test-login")
    public CommonResponseDTO<TokenResponseDTO> login(@RequestBody UserLoginRequestDTO request) {
        log.info("🛂 로그인 컨트롤러 진입");
        TokenResponseDTO token = userService.login(request);
        return CommonResponseDTO.success("로그인 성공", token);
    }

    // AuthController.refresh 위에 추가 (스웨거 문서 노출용)
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "Cookie",
                    value = "refreshToken=<리프레시 토큰 값>",
                    required = true,
                    paramType = "header"
            )
    })
    @PostMapping("/refresh")
    public CommonResponseDTO<Void> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return CommonResponseDTO.error("리프레시 토큰 없음", 401);
        }

        // 서비스에서 새 AT/RT 생성
        TokenResponseDTO token = userService.refresh(refreshToken);

        // ★ 새 AT는 헤더로 전달
        response.setHeader("Authorization", "Bearer " + token.getAccessToken());

        // ★ RT 회전(선택) — 보안권장. 회전 안 하면 아래 3줄 생략 가능
        org.scoula.security.util.CookieUtil.addHttpOnlyCookie(
                response, "refreshToken", token.getRefreshToken(),
                7 * 24 * 60 * 60,  // Max-Age=7일
                false,             // Secure=false (HTTP)
                "Lax"              // SameSite=Lax
        );

        // 바디엔 토큰 안 넣음
        return CommonResponseDTO.success("토큰 재발급 성공");
    }

    /**
     * 로그아웃: AT 블랙리스트/RT 삭제 + RT 쿠키 제거
     * 요청 헤더: Authorization: Bearer <accessToken>
     */
    @PostMapping("/logout")
    public CommonResponseDTO<Void> logout(
            @RequestHeader("Authorization") String bearerToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        userService.logout(bearerToken);

        // ★ RT 쿠키 삭제 (개발/운영 구분)
        boolean isDev = request.getServerName().contains("localhost");
        String sameSite = isDev ? "Lax" : "None";
        boolean secure = !isDev;
        CookieUtil.deleteCookie(response, "refreshToken", secure, sameSite);

        return CommonResponseDTO.success("로그아웃 성공");
    }

    @GetMapping("/test-redis")
    public String testToken(@RequestParam Long id) {
        return redisService.getRefreshToken(id);
    }

    @PostMapping("/login")
    public void swaggerLoginForDocs(@RequestBody UserLoginRequestDTO request) {
        throw new IllegalStateException("swagger 상 필터호출을 위한 엔드포인트입니다.");
    }
}
