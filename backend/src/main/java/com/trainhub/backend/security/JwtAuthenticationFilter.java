package com.trainhub.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trainhub.backend.dto.response.ErrorResponse;
import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Filtro JWT que intercepta cada petición HTTP para validar el token de autenticación.
 * Extrae el token del header Authorization, lo valida y configura el contexto de seguridad.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractTokenFromRequest(request);

            if (token != null) {
                Integer userId = jwtUtil.getUserIdFromToken(token);

                Optional<User> userOptional = userRepository.findById(userId);

                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    UserPrincipal userPrincipal = new UserPrincipal(user);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userPrincipal,
                                    null,
                                    userPrincipal.getAuthorities()
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    System.out.println("Usuario autenticado: " + user.getEmail() + " (ID: " + userId + ")");
                } else {
                    System.out.println("Usuario no encontrado con ID: " + userId);
                    writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "El usuario asociado al token ya no existe.");
                    return;
                }
            }
        } catch (ExpiredJwtException e) {
            System.out.println("Token JWT expirado: " + e.getMessage());
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "El token ha expirado. Por favor, inicia sesión nuevamente.");
            return;
        } catch (MalformedJwtException e) {
            System.out.println("Token JWT malformado: " + e.getMessage());
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "El token proporcionado no es válido.");
            return;
        } catch (SignatureException e) {
            System.out.println("Firma JWT inválida: " + e.getMessage());
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "El token proporcionado no es válido.");
            return;
        } catch (JwtException e) {
            System.out.println("Error al procesar el token JWT: " + e.getMessage());
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Error de autenticación: token inválido.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(message));
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
