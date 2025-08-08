package org.scoula.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.user.domain.UserStatus;

@Mapper
public interface UserStatusMapper {
    void save(UserStatus status);
    boolean isNicknameDuplicated(String nickname);
    UserStatus get(Long userId);
    void update(@Param("level")String level, @Param("userId")Long userId);
    String getNickname(Long userId);
}
