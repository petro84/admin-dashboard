package com.petro.admin_dashboard.model;

import com.petro.admin_dashboard.model.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static com.petro.admin_dashboard.mapper.UserDTOMapper.fromUser;
import static java.util.Arrays.stream;

@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {
    private final User user;
    private final Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return stream(role.getPermission().split(",".trim())).map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getEmail();
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.user.isNotLocked();
    }

    @Override
    public boolean isEnabled() {
        return this.user.isEnabled();
    }

    public UserDTO getUser() {
        return fromUser(user, role);
    }
}
