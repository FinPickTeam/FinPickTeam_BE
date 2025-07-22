package org.scoula.user.dto;

import lombok.Data;

@Data
public class TokenRefreshRequestDTO {
    private String refreshToken;
}
