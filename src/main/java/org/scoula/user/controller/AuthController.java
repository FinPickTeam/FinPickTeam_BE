package org.scoula.user.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.redis.RedisService;
import org.scoula.security.account.dto.UserLoginRequestDTO;
import org.scoula.user.dto.TokenRefreshRequestDTO;
import org.scoula.user.dto.TokenResponseDTO;
import org.scoula.user.service.UserService;
import org.scoula.common.dto.CommonResponseDTO;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RedisService redisService;

    // Swagger 테스트용 로그인
    @PostMapping("/test-login")
    public CommonResponseDTO<TokenResponseDTO> login(@RequestBody UserLoginRequestDTO request) {
        log.info("🛂 로그인 컨트롤러 진입");
        TokenResponseDTO token = userService.login(request);
        return CommonResponseDTO.success("로그인 성공", token);
    }

    @PostMapping("/refresh")
    public CommonResponseDTO<TokenResponseDTO> refresh(@RequestBody TokenRefreshRequestDTO request) {
        TokenResponseDTO token = userService.refresh(request.getRefreshToken());
        return CommonResponseDTO.success("토큰 재발급 성공", token);
    }

    //@AuthenticationPrincipal도 사용가능
    //swagger에서 테스트할 경우, 꼭 토큰 앞에 Bearer<공백> 문자열을 붙이기
    @PostMapping("/logout")
    public CommonResponseDTO<Void> logout(@RequestHeader("Authorization") String bearerToken) {

        userService.logout(bearerToken);
        // 클라이언트에 성공 응답 전송
        return CommonResponseDTO.success("로그아웃 성공");
    }

    @GetMapping("/test-redis")
    public String testToken(@RequestParam Long id) {
        return redisService.getRefreshToken(id);
    }


    @ApiOperation(value = "사용자 로그인", notes = "이메일과 비밀번호로 로그인하고 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public void swaggerLoginForDocs(@RequestBody UserLoginRequestDTO request) {
        // 실제 요청은 JwtEmailPasswordAuthenticationFilter가 가로채서 처리합니다.
        throw new IllegalStateException("swagger 상 필터호출을 위한 엔드포인트입니다.");
    }
}
