package com.trainhub.backend.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para toda la aplicación.
 * Centraliza el manejo de errores y proporciona respuestas consistentes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de validación (400 Bad Request).
     * Se lanza cuando los datos del request no pasan las validaciones de Bean Validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = "Error de validación";
        if (ex.getBindingResult().hasFieldErrors() && !ex.getBindingResult().getFieldErrors().isEmpty()) {
            errorMessage += ": " + ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    /**
     * Maneja excepciones de integridad de datos (409 Conflict).
     * Se lanza cuando se intenta crear un registro que viola restricciones de unicidad o integridad.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityException(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("email")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El email ya está registrado");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Error de integridad de datos: " + message);
    }

    /**
     * Maneja excepciones genéricas no manejadas (500 Internal Server Error).
     * Captura cualquier excepción que no haya sido manejada por otros handlers.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: " + ex.getMessage());
    }
}

