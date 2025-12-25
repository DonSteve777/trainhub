package com.trainhub.backend.service.email;

/**
 * Interfaz para el servicio de envío de emails.
 */
public interface EmailService {

    /**
     * Envía un email de confirmación al usuario con el token de confirmación.
     *
     * @param email El email del destinatario
     * @param token El token de confirmación
     */
    void sendConfirmationEmail(String email, String token);
}

