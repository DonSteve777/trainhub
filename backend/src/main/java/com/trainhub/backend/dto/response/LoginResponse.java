package com.trainhub.backend.dto.response;

/**
 * DTO para la respuesta del login de un usuario.
 */
public class LoginResponse {

    private String token;
    private String message;

    // Constructores
    public LoginResponse() {
    }

    public LoginResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

