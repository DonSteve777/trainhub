package com.trainhub.backend.dto.response;

/**
 * DTO para la respuesta del registro de un nuevo usuario.
 */
public class RegisterResponse {

    private String message;
    private String email;

    // Constructores
    public RegisterResponse() {
    }

    public RegisterResponse(String message, String email) {
        this.message = message;
        this.email = email;
    }

    // Getters y Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

