package org.scoula.user.dto;
import lombok.Data;

@Data
public class EmailCodeConfirmDTO {
    private String email;
    private String code; // "123456"
}
