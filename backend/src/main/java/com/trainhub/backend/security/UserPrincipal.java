package com.trainhub.backend.security;

import com.trainhub.backend.enums.AccountStatus;
import com.trainhub.backend.model.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementación de UserDetails que envuelve un User entity.
 * Proporciona información de autenticación y autorización para Spring Security.
 */
public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    /**
     * Obtiene el usuario envuelto.
     */
    public User getUser() {
        return user;
    }

    /**
     * Obtiene el ID del usuario.
     */
    public Integer getId() {
        return user.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Por ahora, todos los usuarios tienen rol USER
        // En el futuro se puede expandir para soportar diferentes roles
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // La cuenta no está bloqueada si el estado es ACTIVE
        return user.getAccountStatus() == AccountStatus.ACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // La cuenta está habilitada si el estado es ACTIVE y el email está verificado
        return user.getAccountStatus() == AccountStatus.ACTIVE && user.getEmailVerified();
    }
}
