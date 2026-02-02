package com.trainhub.backend.security;

import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Filtro JWT que intercepta cada petición HTTP para validar el token de autenticación.
 * Extrae el token del header Authorization, lo valida y configura el contexto de seguridad.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extraer el token del header Authorization
            String token = extractTokenFromRequest(request);

            if (token != null) {
                // Validar el token y obtener el userId
                Integer userId = jwtUtil.getUserIdFromToken(token);

                // Cargar el usuario desde la base de datos
                Optional<User> userOptional = userRepository.findById(userId);

                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    UserPrincipal userPrincipal = new UserPrincipal(user);

                    // Crear el objeto de autenticación
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userPrincipal,
                                    null,
                                    userPrincipal.getAuthorities()
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Configurar el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("Usuario autenticado: {} (ID: {})", user.getEmail(), userId);
                } else {
                    logger.warn("Usuario no encontrado con ID: {}", userId);
                }
            }
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT expirado: {}", e.getMessage());
            // El token ha expirado - se deja pasar la petición sin autenticación
            // El SecurityConfig rechazará peticiones no autenticadas a endpoints protegidos
        } catch (MalformedJwtException e) {
            logger.error("Token JWT malformado: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.error("Firma JWT inválida: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error al procesar el token JWT: {}", e.getMessage());
        }

        // Continuar con el siguiente filtro en la cadena
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization.
     *
     * @param request La petición HTTP
     * @return El token JWT si existe, null en caso contrario
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
