package com.trainhub.backend.exception;

import com.trainhub.backend.dto.response.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
     * Maneja excepciones genéricas no manejadas (500 Internal Server Error).
     * Captura cualquier excepción que no haya sido manejada por otros handlers.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse("Error interno del servidor: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

