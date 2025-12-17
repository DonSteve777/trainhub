package com.trainhub.backend.security;

import com.trainhub.backend.model.User;
import com.trainhub.backend.model.enums.AccountStatus;
import com.trainhub.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementación de UserDetailsService para Spring Security.
 * Carga usuarios desde la base de datos para autenticación.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carga un usuario por email para autenticación.
     * 
     * @param email Email del usuario
     * @return UserDetails con la información del usuario
     * @throws UsernameNotFoundException si el usuario no existe
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Cargando usuario por email: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("Usuario no encontrado con email: {}", email);
                return new UsernameNotFoundException("Usuario no encontrado: " + email);
            });

        return new CustomUserDetails(user);
    }

    /**
     * Implementación de UserDetails que envuelve la entidad User.
     */
    public static class CustomUserDetails implements UserDetails {
        private final User user;

        public CustomUserDetails(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // Por ahora solo rol USER, se puede expandir en el futuro
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
            return true; // No implementamos expiración de cuenta por ahora
        }

        @Override
        public boolean isAccountNonLocked() {
            // La cuenta no está bloqueada si el estado no es BLOCKED
            return user.getAccountStatus() != AccountStatus.BLOCKED;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true; // No implementamos expiración de credenciales por ahora
        }

        @Override
        public boolean isEnabled() {
            // El usuario está habilitado si no está bloqueado
            return user.getAccountStatus() != AccountStatus.BLOCKED;
        }

        /**
         * Obtiene la entidad User completa.
         * Útil para acceder a otros campos del usuario después de la autenticación.
         */
        public User getUser() {
            return user;
        }
    }
}

