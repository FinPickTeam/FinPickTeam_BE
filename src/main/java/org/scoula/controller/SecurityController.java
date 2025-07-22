package org.scoula.controller;

import lombok.extern.log4j.Log4j2;
import org.scoula.security.account.domain.CustomUserDetails;
import org.scoula.user.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/security")
public class SecurityController {

    /**
     * 모든 사용자 접근 가능 (인증 불필요)
     */
    @GetMapping("/all")
    public ResponseEntity<String> doAll() {
        log.info("do all can access everybody");
        return ResponseEntity.ok("All can access everybody");
    }

    /**
     * ROLE_USER 권한 필요 (일반 로그인된 사용자)
     */
    @GetMapping("/member")
    public ResponseEntity<String> doMember(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("로그인된 사용자 email: {}", userDetails.getUsername());
        return ResponseEntity.ok(userDetails.getUsername());
    }

    /**
     * ROLE_ADMIN 권한 필요 (관리자용 - 현재 권한 시스템이 없다면 테스트용으로 사용 가능)
     */
    @GetMapping("/admin")
    public ResponseEntity<User> doAdmin(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User user = customUserDetails.getUser();
        log.info("관리자 접근: {}", user.getEmail());
        return ResponseEntity.ok(user);
    }

    /**
     * 로그인 성공 후 진입
     */
    @GetMapping("/login")
    public void login() {
        log.info("login page");
    }

    /**
     * 로그아웃 성공 후 진입
     */
    @GetMapping("/logout")
    public void logout() {
        log.info("logout page");
    }
}
