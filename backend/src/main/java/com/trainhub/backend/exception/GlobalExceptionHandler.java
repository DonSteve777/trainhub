package com.trainhub.backend.exception;

import com.trainhub.backend.dto.response.ErrorResponse;
import com.trainhub.backend.service.auth.EmailNotVerifiedException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.JwtException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Manejador global de excepciones para toda la aplicación.
 * Centraliza el manejo de errores y proporciona respuestas consistentes en formato JSON.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {



    /**
     * Maneja excepciones de validación (400 Bad Request).
     * Se lanza cuando los datos del request no pasan las validaciones de Bean Validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = "Error de validación";
        if (ex.getBindingResult().hasFieldErrors() && !ex.getBindingResult().getFieldErrors().isEmpty()) {
            errorMessage += ": " + ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        ErrorResponse errorResponse = new ErrorResponse(errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja excepciones de integridad de datos (409 Conflict).
     * Se lanza cuando se intenta crear un registro que viola restricciones de unicidad o integridad.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityException(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        String errorMessage;
        if (message != null && message.contains("email")) {
            errorMessage = "El email ya está registrado";
        } else {
            errorMessage = "Error de integridad de datos: " + message;
        }
        ErrorResponse errorResponse = new ErrorResponse(errorMessage);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Maneja excepciones de credenciales incorrectas (401 Unauthorized).
     * Se lanza cuando el email no existe o la contraseña es incorrecta.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Maneja excepciones de cuenta no activa (403 Forbidden).
     * Se lanza cuando la cuenta está pendiente de confirmación, bloqueada o en cualquier estado que no sea ACTIVE.
     */
    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotActiveException(AccountNotActiveException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Maneja excepciones de email no verificado (403 Forbidden).
     * Se lanza cuando el email no está verificado.
     */
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerifiedException(EmailNotVerifiedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Maneja excepciones de token de reseteo inválido o expirado (400 Bad Request).
     */
    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordResetTokenException(InvalidPasswordResetTokenException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja excepciones de token JWT expirado (401 Unauthorized).
     * Se lanza cuando el token ha caducado y necesita ser renovado.
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(ExpiredJwtException ex) {
        System.out.println("Token JWT expirado: " + ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("El token ha expirado. Por favor, inicia sesión nuevamente.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Maneja excepciones de token JWT malformado (401 Unauthorized).
     * Se lanza cuando el token no tiene el formato correcto.
     */
    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJwtException(MalformedJwtException ex) {
        System.out.println("Token JWT malformado: " + ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("El token proporcionado no es válido.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Maneja excepciones de firma de token JWT inválida (401 Unauthorized).
     * Se lanza cuando la firma del token no es válida o ha sido manipulada.
     */
    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ErrorResponse> handleSignatureException(SignatureException ex) {
        System.out.println("Firma de token JWT inválida: " + ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("El token proporcionado no es válido.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Maneja otras excepciones relacionadas con JWT (401 Unauthorized).
     * Captura cualquier otra excepción de JWT no manejada específicamente.
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex) {
        System.out.println("Error en el procesamiento del token JWT: " + ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("Error de autenticación: token inválido.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Maneja excepciones de negocio con estado HTTP explícito.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        String errorMessage = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        ErrorResponse errorResponse = new ErrorResponse(errorMessage);
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    /**
     * Maneja excepciones genéricas no manejadas (500 Internal Server Error).
     * Captura cualquier excepción que no haya sido manejada por otros handlers.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse("Error interno del servidor: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
 * Maneja excepciones de envío de email (503 Service Unavailable).
 * Se lanza cuando hay problemas al enviar emails.
 */
    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendingException(EmailSendingException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Error al enviar el email de confirmación. Por favor, intenta más tarde.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
}

