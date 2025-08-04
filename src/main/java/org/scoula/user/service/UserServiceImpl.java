package org.scoula.user.service;

import lombok.RequiredArgsConstructor;
import org.scoula.common.redis.RedisService;
import org.scoula.user.domain.User;
import org.scoula.user.domain.UserStatus;
import org.scoula.user.dto.TokenResponseDTO;
import org.scoula.user.dto.UserJoinRequestDTO;
import org.scoula.security.account.dto.UserLoginRequestDTO;
import org.scoula.user.dto.UserResponseDTO;
import org.scoula.user.enums.UserLevel;
import org.scoula.user.exception.auth.*;
import org.scoula.user.exception.signup.DuplicateEmailException;
import org.scoula.user.exception.signup.NicknameGenerationException;
import org.scoula.user.mapper.UserMapper;
import org.scoula.security.util.JwtUtil;
import org.scoula.user.mapper.UserStatusMapper;
import org.scoula.user.util.NicknameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserStatusMapper userStatusMapper;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final PasswordEncoder encoder;
    private final NicknameGenerator nicknameGenerator;


    private final Logger log = LoggerFactory.getLogger(UserService.class);

    // mysql 연결 테스트용
    public User getTestUser() {
        return userMapper.selectFirstUser();
    }

    public boolean isEmailDuplicated(String email) {
        return userMapper.findByEmail(email) != null;
    }

    public UserResponseDTO registerUser(UserJoinRequestDTO req) {
        log.info("🔒 회원가입 시도: {}", req.getEmail() + " , " + req.getPassword());

        // 이메일 중복 검사
        if (userMapper.findByEmail(req.getEmail()) != null) {
            throw new DuplicateEmailException();  // 이 부분 추가
        }

        User user = req.toUser(); // DTO → 도메인 객체
        user.setPassword(encoder.encode(user.getPassword())); // 비밀번호 암호화

        // 기본값 설정 (선택 사항)
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setIsVerified(false); // default 값 (인증 미완료)
        user.setLastPwChangeAt(LocalDateTime.now());

        try {
            userMapper.save(user); // 1. 유저 저장
        } catch (DuplicateKeyException e) {
            log.warn("❌ DB 이메일 중복 에러 발생: {}", user.getEmail());
            throw new DuplicateEmailException();
        }

        // 닉네임 생성 (중복 피해서 생성)
        String nickname;
        do {
            nickname = nicknameGenerator.generateNickname();
        } while (userStatusMapper.isNicknameDuplicated(nickname));

        // user_status 저장
        UserStatus status = new UserStatus();
        status.setId(user.getId());
        status.setNickname(nickname);
        status.setLevel(UserLevel.SEEDLING.getLabel()); // → "금융새싹"
        userStatusMapper.save(status); // 2. 상태 저장
        userMapper.insertUserChallengeSummary(user.getId()); // 3. 챌린지 요약 0으로 초기화

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .userName(user.getUserName())
                .createdAt(user.getCreatedAt().toString())
                .nickname(status.getNickname())
                .level(status.getLevel())
                .build();
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

        String at = jwtUtil.generateAccessToken(u.getId(), u.getEmail());
        String rt = jwtUtil.generateRefreshToken(u.getId(), u.getEmail());
        redisService.saveRefreshToken(u.getId(), rt);
        return new TokenResponseDTO(at, rt);
    }


    public TokenResponseDTO refresh(String refreshToken) {
        log.info("🔒 토큰 재발급 시도: {}", refreshToken);
        if (!jwtUtil.validateToken(refreshToken))
            throw new InvalidTokenException();

        Long id = jwtUtil.getIdFromToken(refreshToken);
        String email = jwtUtil.getEmailFromToken(refreshToken);
        String saved = redisService.getRefreshToken(id);
        if (!refreshToken.equals(saved))
            throw new ExpiredTokenException();

        String at = jwtUtil.generateAccessToken(id, email);
        String rt = jwtUtil.generateRefreshToken(id, email);
        redisService.saveRefreshToken(id, rt);
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
