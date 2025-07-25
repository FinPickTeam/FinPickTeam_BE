package org.scoula.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.user.domain.User;
import org.scoula.user.dto.UserJoinRequestDTO;
import org.scoula.user.dto.UserEmailRequestDTO;
import org.scoula.user.dto.UserResponseDTO;
import org.scoula.user.service.UserService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
    public CommonResponseDTO<UserResponseDTO> join(@RequestBody UserJoinRequestDTO r) {
        r.validate(); // validation 수행
        UserResponseDTO userResponse = userService.registerUser(r);
        return CommonResponseDTO.success("회원가입 성공", userResponse);
    }

    @PostMapping("/password-reset")
    public CommonResponseDTO<String> resetPassword(@RequestBody UserEmailRequestDTO request) {
        String tempPassword = userService.resetPassword(request.getEmail());
        return CommonResponseDTO.success("임시 비밀번호가 발급되었습니다.", tempPassword);
    }

}
