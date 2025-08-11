package org.scoula.user.service;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.agree.mapper.AgreeMapper;
import org.scoula.avatar.mapper.AvatarMapper;
import org.scoula.avatar.service.AvatarService;
import org.scoula.coin.mapper.CoinMapper;
import org.scoula.common.redis.RedisService;
import org.scoula.user.domain.User;
import org.scoula.user.domain.UserStatus;
import org.scoula.user.dto.*;
import org.scoula.security.account.dto.UserLoginRequestDTO;
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
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserStatusMapper userStatusMapper;
    private final CoinMapper coinMapper;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final PasswordEncoder encoder;
    private final NicknameGenerator nicknameGenerator;
    private final AvatarMapper avatarMapper;
    private final AgreeMapper agreeMapper;

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final AvatarService avatarService;

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

        // 3. coin row 초기화
        coinMapper.insertInitialCoin(user.getId());

        // 4. 챌린지 요약 초기화
        userMapper.insertUserChallengeSummary(user.getId());

        // 5. 내 아바타 초기화
        avatarMapper.insertAvatar(user.getId());

        // 6. 옷장에 기본 착장 넣어주기
        avatarMapper.insertClothe(user.getId(), 1L);

        // 7. 옷장에 상태 바꿔주기
        avatarMapper.updateClotheByItemId(user.getId(),true,1L);

        // 8. 동의정보 초기화
        agreeMapper.insert(user.getId());

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

        // 계정이 활성화 상태인지 확인합니다.
        if (!u.getIsActive()) {
            throw new DisabledException("비활성화된 계정입니다.");
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

    // 로그아웃 시, refreshToken 삭제 및 블랙리스트에 accessToken 추가
    public void logout(String bearerToken){

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.warn("로그아웃 요청 실패: 유효하지 않은 토큰 형식");
            throw new InvalidTokenException();
        }
        log.info("로그아웃 시도: {}", bearerToken);

        //1. 토큰 검증
        if (bearerToken.startsWith("Bearer ")) {

            String token = bearerToken.substring("Bearer ".length());

            // 2. 토큰에서 사용자 ID 추출
            Long userId = jwtUtil.getIdFromToken(token);

            // 3. Redis에서 refreshToken 삭제 및 블랙리스트에 accessToken 추가
            redisService.deleteRefreshToken(userId);
            redisService.blacklistAccessToken(token);
            log.info("로그아웃 성공: {},{}", token, userId);
        }
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


    public void withdrawal(String bearerToken) {

        String token = bearerToken.substring("Bearer ".length());

        if (!jwtUtil.validateToken(token))
            throw new InvalidTokenException();

        Long id = jwtUtil.getIdFromToken(token);
        log.info("회원 탈퇴 시도: {}",id);

        userMapper.updateIsActive(id);
        redisService.deleteRefreshToken(id);
        redisService.blacklistAccessToken(token);
    }

    @Override
    @Transactional
    public void checkAndLevelUp(Long userId){
        int culAmount= coinMapper.getCumulativeAmount(userId);
        UserStatus userStatus = userStatusMapper.get(userId);
        UserLevel level = UserLevel.getLevelForPoints(culAmount);

        if(!Objects.equals(userStatus.getLevel(), level.getLabel())){
            log.info("누적포인트가 {}입니다. {}로 승급하셨습니다.", culAmount, level.getLabel());
            userStatusMapper.update(level.getLabel(), userId); // status 칭호 변경
            avatarService.insertClothe(userId, level.getItemId()); //  변경된 칭호에 해당하는 스킨을 옷장에 추가
            avatarService.updateAvatarByItemId(userId, level.getItemId() ); // 변경된 칭호에 해당하는 스킨으로 착장 변경
        } else {
            log.info("포인트는 증가했으나, 승급에는 실패했습니다.");
        }
    }

    @ApiOperation(value = "간편비밀번호 설정 ", notes = "간편비밀번호를 초기 설정합니다.")
    @Override
    public void setPin(Long userId, PinRequestDTO req) {

        log.info("🔒 간편비밀번호 설정 시도: {}", req.getPin());

        //간편비밀번호 문자열로 형변환하여 암호화
        String encodedPin= encoder.encode(String.valueOf(req.getPin()));

        //유저테이블에 저장
        User u=new User();
        u.setId(userId);
        u.setAuthPw(encodedPin);
        userMapper.updatePin(u);
    }

    @ApiOperation(value = "간편비밀번호 일치여부 확인.", notes = "사용자가 입력한 간편비밀번호와 실제 간편비밀번호가 일치하는지 조회합니다.")
    @Override
    public void pinLogin(String email, Long userId, PinRequestDTO req) {

        //유저정보 조회
        User u = userMapper.findByEmail(email);

        //유저정보의 pin과, 유저가 입력한 pin의 일치여부 판단
        if (!encoder.matches(String.valueOf(req.getPin()), u.getAuthPw())) {
            throw new InvalidPinException();
        }
    }

    @ApiOperation(value = "간편비밀번호 재설정 ", notes = "간편비밀번호를 재설정합니다.")
    @Override
    public void resetPin(Long userId, PinRequestDTO req) {

        log.info("🔒 간편비밀번호 재설정 시도: {}", req.getPin());

        //간편비밀번호 암호화
        String encodedPin= encoder.encode(String.valueOf(req.getPin()));

        //암호화된 간편비밀번호 저장
        User u=new User();
        u.setId(userId);
        u.setAuthPw(encodedPin);
        userMapper.updatePin(u);
    }
}
