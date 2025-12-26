package com.trainhub.backend.exception;

/**
 * Excepción lanzada cuando la cuenta del usuario no está activa.
 * Se utiliza cuando accountStatus es PENDING_CONFIRMATION, BLOCKED o cualquier otro estado que no sea ACTIVE.
 */
public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(String message) {
        super(message);
    }

    public AccountNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }
}

