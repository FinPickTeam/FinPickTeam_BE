package org.scoula.security.account.domain;

import lombok.Getter;
import org.scoula.user.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // 권한이 여러 개인 경우 확장 가능
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = (user.getRole() != null) ? user.getRole().name() : "USER";
        return Collections.singleton(() -> "ROLE_" + roleName);
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();  // email = username 역할
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getIsActive();  // 활동 여부
    }

    // 커스텀 getter (선택)
    public Long getUserId() {
        return user.getId();
    }

    public String getUserName() {
        return user.getUserName();
    }

    public String getPin(){return user.getAuthPw();}

}
