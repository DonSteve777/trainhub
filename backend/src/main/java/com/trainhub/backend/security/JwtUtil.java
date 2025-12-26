package com.trainhub.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilidad para generar y validar tokens JWT.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Genera un token JWT para un usuario.
     *
     * @param userId El ID del usuario
     * @param email  El email del usuario
     * @return El token JWT generado
     */
    public String generateToken(Integer userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Valida un token JWT y retorna los claims.
     *
     * @param token El token JWT a validar
     * @return Los claims del token
     */
    public Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extrae el ID del usuario desde el token.
     *
     * @param token El token JWT
     * @return El ID del usuario
     */
    public Integer getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return Integer.parseInt(claims.getSubject());
    }

    /**
     * Extrae el email desde el token.
     *
     * @param token El token JWT
     * @return El email del usuario
     */
    public String getEmailFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("email", String.class);
    }
}

