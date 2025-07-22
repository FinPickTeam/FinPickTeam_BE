package org.scoula.user.service;

import org.scoula.security.account.dto.UserLoginRequestDTO;
import org.scoula.user.dto.*;
import org.scoula.user.domain.User;

public interface UserService {
    User getTestUser();
    boolean isEmailDuplicated(String email);
    UserResponseDTO registerUser(UserJoinRequestDTO req);
    TokenResponseDTO login(UserLoginRequestDTO req);
    TokenResponseDTO refresh(String refreshToken);
    String resetPassword(String email);
}
