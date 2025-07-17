package org.scoula.member.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String email;
    private String password;
    private String userName;
    private String phoneNum;
    private String gender;
    private String birthday;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastPwChangeAt;
    private Boolean isActive;
}
