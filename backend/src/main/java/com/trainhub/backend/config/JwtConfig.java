package com.trainhub.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de propiedades JWT.
 * Las propiedades se leen desde application.properties con el prefijo "jwt"
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {
    
    /**
     * Clave secreta para firmar y verificar los tokens JWT.
     * Debe ser una cadena segura y aleatoria (mínimo 256 bits recomendado).
     * En producción, debe estar en variables de entorno.
     */
    private String secret;
    
    /**
     * Tiempo de expiración del access token en milisegundos.
     * Por defecto: 15 minutos (900000 ms)
     */
    private Long accessTokenExpiration = 900000L; // 15 minutos
    
    /**
     * Tiempo de expiración del refresh token en milisegundos.
     * Por defecto: 7 días (604800000 ms)
     */
    private Long refreshTokenExpiration = 604800000L; // 7 días
}

