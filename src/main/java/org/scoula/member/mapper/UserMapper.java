package org.scoula.member.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.member.domain.User;

@Mapper
public interface UserMapper {
    User selectFirstUser();  // 테스트용 1명 조회

    void save(User user); // 회원가입
    User findByEmail(String email); // 이메일로 비밀번호 찾기
    void updatePassword(User user); // 비밀번호 재발급
}
