package com.trainhub.backend.service.email;

import com.trainhub.backend.exception.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de email usando Spring Mail.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendConfirmationEmail(String email, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Confirma tu cuenta de TrainHub");

            String confirmationUrl = baseUrl + "/api/auth/confirm-email/" + token;
            String htmlContent = buildEmailContent(confirmationUrl);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email de confirmación enviado exitosamente a {}", email);
        } catch (MessagingException e) {
            logger.error("Error al enviar email de confirmación a {}: {}", email, e.getMessage(), e);
            throw new EmailSendingException("Error al enviar email de confirmación a " + email, e);
        }
    }

    private String buildEmailContent(String confirmationUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f4f4f4;
                    }
                    .container {
                        background-color: #ffffff;
                        border-radius: 10px;
                        padding: 30px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .header h1 {
                        color: #2c3e50;
                        margin: 0;
                    }
                    .content {
                        margin-bottom: 30px;
                    }
                    .button {
                        display: inline-block;
                        padding: 12px 30px;
                        background-color: #3498db;
                        color: #ffffff;
                        text-decoration: none;
                        border-radius: 5px;
                        margin: 20px 0;
                        font-weight: bold;
                    }
                    .button:hover {
                        background-color: #2980b9;
                    }
                    .footer {
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #eeeeee;
                        font-size: 12px;
                        color: #7f8c8d;
                        text-align: center;
                    }
                    .link {
                        color: #3498db;
                        word-break: break-all;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Bienvenido a TrainHub</h1>
                    </div>
                    <div class="content">
                        <p>Gracias por registrarte en TrainHub. Para completar tu registro y activar tu cuenta, por favor confirma tu dirección de correo electrónico haciendo clic en el siguiente botón:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Confirmar mi cuenta</a>
                        </p>
                        <p>O copia y pega el siguiente enlace en tu navegador:</p>
                        <p class="link">%s</p>
                        <p>Si no te registraste en TrainHub, puedes ignorar este correo de forma segura.</p>
                    </div>
                    <div class="footer">
                        <p>Este es un correo automático, por favor no respondas a este mensaje.</p>
                        <p>&copy; 2024 TrainHub. Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(confirmationUrl, confirmationUrl);
    }
}

