package com.zygoo13.admin.security;

import com.zygoo13.common.entity.Role;
import com.zygoo13.common.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EcommerceUserDetails implements UserDetails {

    private final User user;

    public EcommerceUserDetails(User user) {
        this.user = user;
    }

    // Chuyển đổi danh sách Role của User thành danh sách GrantedAuthority
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Thêm tiền tố "ROLE_" vào tên vai trò để phù hợp với chuẩn của Spring Security
        return user.getRoles().stream()
                .map(Role::getName)
                .map(name -> "ROLE_" + name)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }


    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getEmail(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.isEnabled(); }

    public String getFullName() {
        return user.getFullName();
    }

    public User getUser() { return user; }
}
