package com.trainhub.backend.controller;

import com.trainhub.backend.dto.request.BulkRegisterRequest;
import com.trainhub.backend.dto.request.LoginRequest;
import com.trainhub.backend.dto.request.RegisterRequest;
import com.trainhub.backend.dto.request.ForgotPasswordRequest;
import com.trainhub.backend.dto.request.ResetPasswordRequest;
import com.trainhub.backend.dto.response.LoginResponse;
import com.trainhub.backend.dto.response.RegisterResponse;
import com.trainhub.backend.dto.response.MessageResponse;
import com.trainhub.backend.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de autenticación.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint para registrar un nuevo usuario.
     *
     * @param request La solicitud de registro con los datos del usuario
     * @return La respuesta con el mensaje de éxito y el email
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para iniciar sesión.
     *
     * @param request La solicitud de login con email y contraseña
     * @return La respuesta con el token JWT y mensaje de éxito
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para confirmar el email del usuario.
     *
     * @param token El token de confirmación
     * @return La respuesta con el mensaje de éxito
     */
    @GetMapping("/confirm-email/{token}")
    public ResponseEntity<Void> confirmEmail(@PathVariable String token) {
        authService.confirmEmail(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("Si existe una cuenta con ese email, recibirás instrucciones para restablecer tu contraseña."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Contraseña actualizada correctamente"));
    }

    @PostMapping("/dev/register-bulk")
    public ResponseEntity<MessageResponse> devRegisterBulk(@Valid @RequestBody BulkRegisterRequest request) {
        MessageResponse response = authService.registerBulk(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de logout. La sesión es stateless (JWT), por lo que la invalidación
     * real ocurre en el cliente al borrar el token. Este endpoint existe para
     * mantener una interfaz REST uniforme.
     *
     * @return 204 No Content
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}

