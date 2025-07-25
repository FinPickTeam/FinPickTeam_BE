package org.scoula.security.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.user.domain.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private String email;
    private String userName;
    private String phoneNum;
    private String gender;
    private String birthday;

    public static UserInfoDTO from(User user) {
        return new UserInfoDTO(
                user.getEmail(),
                user.getUserName(),
                user.getPhoneNum(),
                user.getGender(),
                user.getBirthday()
        );
    }
}
