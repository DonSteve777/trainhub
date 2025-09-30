package com.trainhub.backend.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("Ya existe un usuario registrado con el Username: " + username);
    }
}
