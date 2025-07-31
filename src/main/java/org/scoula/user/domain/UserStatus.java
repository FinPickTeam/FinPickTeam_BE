package org.scoula.user.domain;

import lombok.Data;

@Data
public class UserStatus {
    private Long id;
    private String nickname;
    private String level; // enum으로 저장
}
