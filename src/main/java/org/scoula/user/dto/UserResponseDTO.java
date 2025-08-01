package org.scoula.user.dto;

import lombok.Builder;
import lombok.Getter;
import org.scoula.user.domain.User;

@Getter
@Builder
public class UserResponseDTO {
    private Long id;
    private String email;
    private String userName;
    private String createdAt;
    private String nickname;
    private String level;
}

