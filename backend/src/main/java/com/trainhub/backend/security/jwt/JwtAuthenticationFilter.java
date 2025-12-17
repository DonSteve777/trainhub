package com.trainhub.backend.security.jwt;

import com.trainhub.backend.model.User;
import com.trainhub.backend.model.enums.AccountStatus;
import com.trainhub.backend.repository.UserRepository;
import com.trainhub.backend.service.auth.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * Filtro de autenticación JWT que se ejecuta en cada request.
 * Valida el token JWT del header Authorization y establece el contexto de seguridad.
 * 
 * Según Security_Design.md:
 * - Extrae el token del header: Authorization: Bearer <token>
 * - Valida el token usando AuthService
 * - Verifica que el usuario esté ACTIVE y emailVerified = true
 * - Establece el Authentication en SecurityContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final UserRepository userRepository;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            // Extraer token del header
            String token = extractTokenFromRequest(request);
            
            if (token != null) {
                // Validar token y obtener claims
                Claims claims = authService.validateToken(token);
                
                // Obtener ID del usuario desde el subject del token
                String userId = claims.getSubject();
                
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Cargar usuario desde la base de datos
                    Optional<User> userOptional = userRepository.findById(Integer.parseInt(userId));
                    
                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        
                        // Verificar que el usuario esté activo y verificado
                        if (isUserValid(user)) {
                            // Crear Authentication object
                            UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                                );
                            
                            authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                            );
                            
                            // Establecer en el contexto de seguridad
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            
                            log.debug("Usuario autenticado: {} (ID: {})", user.getEmail(), user.getId());
                        } else {
                            log.warn("Usuario no válido para autenticación: {} (Estado: {}, Email verificado: {})", 
                                user.getEmail(), user.getAccountStatus(), user.getEmailVerified());
                        }
                    } else {
                        log.warn("Usuario no encontrado con ID: {}", userId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error al validar token JWT: {}", e.getMessage());
            // No establecer autenticación, continuar con el filtro
            // Si el endpoint requiere autenticación, Spring Security rechazará la request
        }
        
        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization.
     * 
     * @param request HttpServletRequest
     * @return Token JWT o null si no se encuentra
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * Verifica que el usuario sea válido para autenticación.
     * Según Security_Design.md: usuario debe estar ACTIVE y emailVerified = true.
     * 
     * @param user Usuario a verificar
     * @return true si el usuario es válido, false en caso contrario
     */
    private boolean isUserValid(User user) {
        return user.getAccountStatus() == AccountStatus.ACTIVE 
            && Boolean.TRUE.equals(user.getEmailVerified());
    }
}

