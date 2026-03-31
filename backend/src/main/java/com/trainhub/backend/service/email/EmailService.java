package com.trainhub.backend.service.email;

import com.trainhub.backend.exception.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de email usando Spring Mail.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;
// produccion
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String MAIL_USERNAME;

    @Value("${spring.mail.password}")
    private String MAIL_PASSWORD;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendConfirmationEmail(String email, String token) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(MAIL_USERNAME);
            helper.setTo(email);
            helper.setSubject("Confirma tu cuenta de TrainHub");

            String confirmationUrl = baseUrl + "/api/auth/confirm-email/" + token;
            String htmlContent = buildEmailContent(confirmationUrl);

            helper.setText(htmlContent, true);


            mailSender.send(message);
     
            System.out.println("Email de confirmación enviado exitosamente a " + email);
        } catch (MessagingException e) {

            System.out.println("Error al enviar email de confirmación a " + email + ", con token " + token + ", " + 
                "variable MAIL_USERNAME: " + MAIL_USERNAME + ", variable MAIL_PASSWORD: " + MAIL_PASSWORD + ", con error: " + e.getMessage());
            throw new EmailSendingException("Error al enviar email de confirmación a " + email + ": " + e.getMessage(), e);
        } catch (Exception e) {

            System.out.println("Error inesperado al enviar email de confirmación a " + email + ": " + e.getMessage() + ", con error: " + e.getMessage());
            throw new EmailSendingException("Error al enviar email de confirmación a " + email, e);
            
        }
    }

    public void sendPasswordResetEmail(String email, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(MAIL_USERNAME);
            helper.setTo(email);
            helper.setSubject("Restablece tu contraseña de TrainHub");

            String resetUrl = frontendUrl + "/reset-password/" + token;
            String htmlContent = buildPasswordResetEmailContent(resetUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Email de reseteo de contraseña enviado exitosamente a " + email);
        } catch (MessagingException e) {
            System.out.println("Error al enviar email de reseteo de contraseña a " + email + ", con token " + token + ", " +
                    "variable MAIL_USERNAME: " + MAIL_USERNAME + ", variable MAIL_PASSWORD: " + MAIL_PASSWORD + ", con error: " + e.getMessage());
            throw new EmailSendingException("Error al enviar email de reseteo de contraseña a " + email + ": " + e.getMessage(), e);
        } catch (Exception e) {
            System.out.println("Error inesperado al enviar email de reseteo de contraseña a " + email + ": " + e.getMessage() + ", con error: " + e.getMessage());
            throw new EmailSendingException("Error al enviar email de reseteo de contraseña a " + email, e);
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

    private String buildPasswordResetEmailContent(String resetUrl) {
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
                        background-color: #e67e22;
                        color: #ffffff;
                        text-decoration: none;
                        border-radius: 5px;
                        margin: 20px 0;
                        font-weight: bold;
                    }
                    .button:hover {
                        background-color: #d35400;
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
                        color: #e67e22;
                        word-break: break-all;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Restablecer contraseña</h1>
                    </div>
                    <div class="content">
                        <p>Hemos recibido una solicitud para restablecer tu contraseña. Si has sido tú, haz clic en el siguiente botón:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Restablecer contraseña</a>
                        </p>
                        <p>O copia y pega el siguiente enlace en tu navegador:</p>
                        <p class="link">%s</p>
                        <p>Si no solicitaste este cambio, puedes ignorar este correo de forma segura.</p>
                    </div>
                    <div class="footer">
                        <p>Este es un correo automático, por favor no respondas a este mensaje.</p>
                        <p>&copy; 2024 TrainHub. Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(resetUrl, resetUrl);
    }
}

