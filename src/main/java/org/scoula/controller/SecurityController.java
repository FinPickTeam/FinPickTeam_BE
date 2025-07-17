package org.scoula.controller;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.security.account.domain.CustomUser;


import java.security.Principal;

@Log4j2
@RequestMapping("/api/security")
@RestController
public class SecurityController {
//    @GetMapping("/all") // 모두 접근 가능
//    public void doAll() {
//        log.info("do all can access everybody");
//    }
    /**
     * 모든 사용자 접근 가능 (인증 불필요)
     */
    @GetMapping("/all") //  /api/security/all
    public ResponseEntity<String> doAll() {
        log.info("do all can access everybody");
        return ResponseEntity.ok("All can access everybody");
    }

//    @GetMapping("/member") // MEMBER 또는 ADMIN 권한 필요
//    public void doMember(Principal principal) {
//        // UserDetails 구현체에서 username 반환
//        String username = principal.getName();
//
//        log.info("logined member : {}", username);
//
//    }
    /**
     * ROLE_MEMBER 권한 필요
     */
    @GetMapping("/member") // /api/security/member
    public ResponseEntity<String> doMember(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("username = " + userDetails.getUsername());
        return ResponseEntity.ok(userDetails.getUsername());
    }

//    @GetMapping("/admin") // ADMIN 권한 필요
//    public void doAdmin() {
//        log.info("admin only");
//    }
    /**
     * ROLE_ADMIN 권한 필요
     */
    @GetMapping("/admin") //  /api/security/admin
    public ResponseEntity<MemberVO> doAdmin(
            @AuthenticationPrincipal CustomUser customUser
    ) {
        MemberVO member = customUser.getMember();
        log.info("username = " + member);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/login")
    public void login() {
        log.info("login page");
    }
    @GetMapping("/logout")
    public void logout() {
        log.info("logout page");
    }
}
