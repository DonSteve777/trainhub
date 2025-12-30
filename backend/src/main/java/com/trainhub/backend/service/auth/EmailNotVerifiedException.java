package com.trainhub.backend.service.auth;

public class EmailNotVerifiedException extends RuntimeException {

    /**
     * Excepción lanzada cuando el email no está verificado.
     * Se utiliza cuando el email no está verificado.
     *
     * @param message El mensaje de la excepción
     */
    public EmailNotVerifiedException(String message) {
        super(message);
    }

    public EmailNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
