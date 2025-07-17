package org.scoula.member.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.member.domain.User;
import org.scoula.member.dto.*;
import org.scoula.member.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final RedisService redisService;

    private final Logger log = LoggerFactory.getLogger(UserController.class);

    // 테스트용
    @GetMapping("/test-db")
    public User testDbConnection() {
        return userService.getTestUser();
    }

    @GetMapping("/email-check")
    public CommonResponseDTO<Void> checkEmailDuplicate(@RequestParam String email) {
        if (userService.isEmailDuplicated(email)) {
            return CommonResponseDTO.error("이미 사용 중인 이메일입니다.", 409);
        }
        return CommonResponseDTO.success("사용 가능한 이메일입니다.");
    }

    @PostMapping("/signup")
    public CommonResponseDTO<Void> join(@RequestBody UserJoinRequestDTO r) {
        r.validate(); // validation 수행
        userService.registerUser(r);
        return CommonResponseDTO.success("회원가입 성공");
    }

    @PostMapping("/login")
    public CommonResponseDTO<TokenResponseDTO> login(@RequestBody UserLoginRequestDTO r) {
        log.info("🛂 로그인 컨트롤러 진입");
        TokenResponseDTO token = userService.login(r);
        return CommonResponseDTO.success("로그인 성공", token);
    }

    @PostMapping("/token-refresh")
    public CommonResponseDTO<TokenResponseDTO> refresh(@RequestBody TokenRefreshRequestDTO request) {
        TokenResponseDTO token = userService.refresh(request.getRefreshToken());
        return CommonResponseDTO.success("토큰 재발급 성공", token);
    }


    @PostMapping("/password-reset")
    public CommonResponseDTO<String> resetPassword(@RequestBody UserEmailRequestDTO request) {
        String tempPassword = userService.resetPassword(request.getEmail());
        return CommonResponseDTO.success("임시 비밀번호가 발급되었습니다.", tempPassword);
    }


    @GetMapping("/token-test")
    public String testToken(@RequestParam String email) {
        return redisService.getRefreshToken(email);
    }
}
