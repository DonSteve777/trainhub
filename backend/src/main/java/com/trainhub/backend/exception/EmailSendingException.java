package com.trainhub.backend.exception;

/**
 * Excepción lanzada cuando ocurre un error al enviar un email.
 * Se utiliza cuando hay problemas con el servicio de correo electrónico.
 */
public class EmailSendingException extends RuntimeException {

    public EmailSendingException(String message) {
        super(message);
    }

    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}


