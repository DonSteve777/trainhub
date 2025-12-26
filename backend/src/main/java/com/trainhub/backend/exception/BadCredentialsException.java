package com.trainhub.backend.exception;

/**
 * Excepción lanzada cuando las credenciales de login son incorrectas.
 * Se utiliza cuando el email no existe o la contraseña no coincide.
 */
public class BadCredentialsException extends RuntimeException {

    public BadCredentialsException(String message) {
        super(message);
    }

    public BadCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}

