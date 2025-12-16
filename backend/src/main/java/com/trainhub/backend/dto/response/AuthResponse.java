package com.trainhub.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de autenticación.
 * Contiene el access token y la información básica del usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    /**
     * Access Token JWT (válido por 15 minutos según Security_Design.md).
     */
    private String accessToken;

    /**
     * Información del usuario autenticado.
     */
    private UserInfo user;

    /**
     * Clase interna que contiene la información básica del usuario.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Integer id;
        private String name;
        private String email;
        private String bio;
        private String photoUrl;
        private Boolean emailVerified;
        private LocalDateTime registrationDate;
        private LocalDateTime lastLogin;
    }
}

