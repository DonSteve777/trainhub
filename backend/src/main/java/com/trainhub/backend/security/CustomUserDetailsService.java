package com.trainhub.backend.security;

import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación personalizada de UserDetailsService para cargar usuarios desde la base de datos.
 * Se utiliza en el proceso de autenticación de Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Carga un usuario por su email (username en el contexto de Spring Security).
     *
     * @param email El email del usuario a cargar
     * @return UserDetails con la información del usuario
     * @throws UsernameNotFoundException si el usuario no existe
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email: " + email
                ));

        return new UserPrincipal(user);
    }

    /**
     * Carga un usuario por su ID.
     * Útil para el filtro JWT que extrae el userId del token.
     *
     * @param userId El ID del usuario a cargar
     * @return UserDetails con la información del usuario
     * @throws UsernameNotFoundException si el usuario no existe
     */
    public UserDetails loadUserById(Integer userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con ID: " + userId
                ));

        return new UserPrincipal(user);
    }
}
