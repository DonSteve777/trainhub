package com.trainhub.backend.dto.response;

/**
 * DTO para respuestas de error de la API.
 * Proporciona un formato consistente para todos los errores.
 */
public class ErrorResponse {

    private String message;
    private String error;

    // Constructores
    public ErrorResponse() {
    }

    public ErrorResponse(String message) {
        this.message = message;
        this.error = message;
    }

    public ErrorResponse(String message, String error) {
        this.message = message;
        this.error = error;
    }

    // Getters y Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

