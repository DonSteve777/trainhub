package com.trainhub.backend.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementación mock del servicio de email.
 * Solo registra en logs, no envía emails reales.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    public void sendConfirmationEmail(String email, String token) {
        logger.info("Email de confirmación enviado a {} con token {}", email, token);
        // En una implementación real, aquí se enviaría el email usando un servicio como
        // Spring Mail, SendGrid, AWS SES, etc.
    }
}

