package org.scoula.member.service;

import lombok.RequiredArgsConstructor;
import org.scoula.member.dto.*;
import org.scoula.member.domain.User;
import org.scoula.member.exception.auth.*;
import org.scoula.member.exception.verify.EmailAlreadyVerifiedException;
import org.scoula.member.exception.verify.VerificationTokenMismatchException;
import org.scoula.member.exception.verify.VerificationTokenNotFoundException;
import org.scoula.member.mapper.UserMapper;
import org.scoula.member.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final BCryptPasswordEncoder encoder;

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    // mysql 연결 테스트용
    public User getTestUser() {
        return userMapper.selectFirstUser();
    }

    public boolean isEmailDuplicated(String email) {
        return userMapper.findByEmail(email) != null;
    }

    public void registerUser(UserJoinRequestDTO req) {
        log.info("🔒 회원가입 시도: {}", req.getEmail() + " , " + req.getPassword());
        User u = new User();
        u.setEmail(req.getEmail());
        u.setPassword(encoder.encode(req.getPassword()));
        userMapper.save(u);
    }

    public TokenResponseDTO login(UserLoginRequestDTO req) {
        log.info("🔒 로그인 시도: {}", req.getEmail());
        User u = userMapper.findByEmail(req.getEmail());

        if (u == null) {
            throw new InvalidEmailException();
        }

        if (!encoder.matches(req.getPassword(), u.getPassword())) {
            throw new InvalidPasswordException();
        }

        String at = jwtUtil.generateAccessToken(u.getEmail());
        String rt = jwtUtil.generateRefreshToken(u.getEmail());
        redisService.saveRefreshToken(u.getEmail(), rt);
        return new TokenResponseDTO(at, rt);
    }


    public TokenResponseDTO refresh(String refreshToken) {
        log.info("🔒 토큰 재발급 시도: {}", refreshToken);
        if (!jwtUtil.validateToken(refreshToken))
            throw new InvalidTokenException();

        String email = jwtUtil.getEmailFromToken(refreshToken);
        String saved = redisService.getRefreshToken(email);
        if (!refreshToken.equals(saved))
            throw new ExpiredTokenException();

        String at = jwtUtil.generateAccessToken(email);
        String rt = jwtUtil.generateRefreshToken(email);
        redisService.saveRefreshToken(email, rt);
        return new TokenResponseDTO(at, rt);
    }

    public String resetPassword(String email) {
        log.info("🔒 비밀번호 재발급 시도: {}", email);
        User u = userMapper.findByEmail(email);
        if (u == null) throw new EmailNotFoundException();
        String temp = UUID.randomUUID().toString().substring(0,8); // 임시 비밀번호 생성
        u.setPassword(encoder.encode(temp)); // 암호화
        userMapper.updatePassword(u); // DB에 저장
        return temp;  // TODO: 이메일 발송으로 대체
    }

}
