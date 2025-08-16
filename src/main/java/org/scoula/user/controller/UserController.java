package org.scoula.user.controller;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.dto.CommonResponseDTO;
import org.scoula.security.account.domain.CustomUserDetails;
import org.scoula.user.domain.User;
import org.scoula.user.dto.*;
import org.scoula.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashMap;
import java.util.Map;

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

    @PutMapping("/withdrwal")
    public CommonResponseDTO<Void> withdrawal(@RequestHeader("Authorization") String token) {
        userService.withdrawal(token);
        return CommonResponseDTO.success("회원 탈퇴가 완료되었습니다.");
    }

    //핀번호 세팅
    @ApiOperation(value = "간편비밀번호 설정 ", notes = "간편비밀번호를 초기 설정합니다.")
    @PostMapping("/pin")
    public CommonResponseDTO<Void> setPin(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody PinRequestDTO pinRequestDTO) {
        userService.setPin(userDetails.getUserId(),pinRequestDTO);
        return CommonResponseDTO.success("간편비밀번호 설정이 완료되었습니다.");
    }

    //핀번호 리셋
    @ApiOperation(value = "간편비밀번호 재설정 ", notes = "간편비밀번호를 다시 설정합니다.")
    @PutMapping("/pin/reset")
    public CommonResponseDTO<Void> resetLogin(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody PinRequestDTO pinRequestDTO) {
        userService.resetPin(userDetails.getUserId(),pinRequestDTO);
        return CommonResponseDTO.success("간편비밀번호 재설정이 완료되었습니다.");
    }

    //핀번호 여부 조회
    @ApiOperation(value = "간편비밀번호 여부 조회 ", notes = "간편비밀번호를 설정했는지 여부를 조회합니다.")
    @GetMapping("/pin/isCreated")
    public CommonResponseDTO<Boolean> isPinCreated (@AuthenticationPrincipal CustomUserDetails userDetails) {
        Boolean isPin=userService.isPin(userDetails.getUserId());
        return CommonResponseDTO.success("간편비밀번호 설정여부 조회가 완료되었습니다.",isPin);
    }

    @GetMapping("/me")
    public CommonResponseDTO<Map<String, Object>> me(@AuthenticationPrincipal CustomUserDetails p) {
        Map<String, Object> body = new HashMap<>();
        body.put("id", p.getUserId());
        body.put("email", p.getUsername());
        body.put("role", p.getUser().getRole() != null ? p.getUser().getRole().name() : "USER");
        body.put("authorities", p.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new));
        return CommonResponseDTO.success("ok", body);
    }


    // 이메일 인증
    @PostMapping("/email/verify/request")
    public CommonResponseDTO<Void> emailVerifyRequest(@RequestBody EmailVerificationRequestDTO dto) {
        userService.requestEmailVerification(dto.getEmail());
        return CommonResponseDTO.success("인증 코드가 발송되었습니다.");
    }

    @PostMapping("/email/verify/confirm")
    public CommonResponseDTO<Void> emailVerifyConfirm(@RequestBody EmailCodeConfirmDTO dto) {
        userService.confirmEmailVerification(dto.getEmail(), dto.getCode());
        return CommonResponseDTO.success("이메일 인증이 완료되었습니다.");
    }

    @GetMapping("/email/verify/status")
    public CommonResponseDTO<Boolean> emailVerifyStatus(@RequestParam String email) {
        return CommonResponseDTO.success("확인 완료", userService.isEmailVerifiedNow(email));
    }



}
