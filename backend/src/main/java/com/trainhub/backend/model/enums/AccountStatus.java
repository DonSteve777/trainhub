package com.trainhub.backend.model.enums;

/**
 * Enum que representa los estados posibles de una cuenta de usuario.
 */
public enum AccountStatus {
    /**
     * Cuenta pendiente de confirmación de email.
     */
    PENDING_CONFIRMATION,
    
    /**
     * Cuenta activa y verificada.
     */
    ACTIVE,
    
    /**
     * Cuenta bloqueada (por seguridad o administración).
     */
    BLOCKED
}

