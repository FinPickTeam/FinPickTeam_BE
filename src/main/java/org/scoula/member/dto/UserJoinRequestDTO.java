package org.scoula.member.dto;

import lombok.Data;
import org.scoula.member.exception.signup.InvalidEmailFormatException;
import org.scoula.member.exception.signup.InvalidPasswordFormatException;
import org.scoula.member.exception.signup.PasswordMismatchException;

@Data
public class UserJoinRequestDTO {
    private String email;
    private String password;
    private String passwordConfirm;

    public void validate() {
        // 이메일 형식 검사
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(emailRegex)) {
            throw new InvalidEmailFormatException();
        }

        // 비밀번호 확인
        if (!password.equals(passwordConfirm)) {
            throw new PasswordMismatchException();
        }

        // 비밀번호 형식 검사
        String pwRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,}$";
        if (!password.matches(pwRegex)) {
            throw new InvalidPasswordFormatException();
        }
    }
}
