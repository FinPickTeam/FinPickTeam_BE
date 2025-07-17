package org.scoula.member.dto;

import lombok.Data;

@Data
public class UserLoginRequestDTO {
    private String email;
    private String password;
}
