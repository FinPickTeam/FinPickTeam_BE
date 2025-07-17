package org.scoula.member.dto;

import lombok.Data;

@Data
public class TokenResponseDTO {
    private String accessToken;
    private String refreshToken;

    public TokenResponseDTO(String at, String rt) {
        this.accessToken = at;
        this.refreshToken = rt;
    }
}
