package org.scoula.user.controller;

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

    @GetMapping("/test-redis")
    public String testToken(@RequestParam String email) {
        return redisService.getRefreshToken(email);
    }
}
