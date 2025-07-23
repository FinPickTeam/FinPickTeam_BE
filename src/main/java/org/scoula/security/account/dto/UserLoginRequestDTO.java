package org.scoula.security.account.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;

import javax.servlet.http.HttpServletRequest;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserLoginRequestDTO {
    private String email;
    private String password;

    public static UserLoginRequestDTO of(HttpServletRequest request) {
        ObjectMapper om = new ObjectMapper();
        try {
            return om.readValue(request.getInputStream(), UserLoginRequestDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadCredentialsException("email 또는 password가 없습니다.");
        }
    }
}
