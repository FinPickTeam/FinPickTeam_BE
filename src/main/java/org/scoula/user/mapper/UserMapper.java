package org.scoula.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.user.domain.User;

@Mapper
public interface UserMapper {
    User selectFirstUser();  // 테스트용 1명 조회

    void save(User user); // 회원가입
    User findByEmail(String email); // 이메일로 비밀번호 찾기
    String findNicknameById(Long id);
    void updatePassword(User user); // 비밀번호 재발급
    void insertUserChallengeSummary(@Param("userId") Long userId);
    void updateIsActive(@Param("id") Long id);
    Boolean getIsPin(Long userId);

    void updatePin(User user);
    String getPin(@Param("userId") Long userId);

    // 이메일 인증
    void updateIsVerifiedByEmail(@Param("email") String email, @Param("isVerified") boolean isVerified);
    Boolean selectIsVerifiedByEmail(@Param("email") String email);
}
