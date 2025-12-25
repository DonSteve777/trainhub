package com.trainhub.backend.model.enums;

/**
 * Enum que representa los estados posibles de una cuenta de usuario.
 */
public enum AccountStatus {
    /**
     * Estado inicial cuando el usuario se registra pero aún no ha confirmado su email.
     */
    PENDING_CONFIRMATION,

    /**
     * Estado cuando la cuenta está activa y el usuario puede usar el sistema.
     */
    ACTIVE,

    /**
     * Estado cuando la cuenta ha sido bloqueada.
     */
    BLOCKED
}


