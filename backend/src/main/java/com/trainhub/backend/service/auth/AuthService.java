package com.trainhub.backend.service.auth;

import java.util.Optional;
import java.util.UUID;
import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trainhub.backend.dto.request.LoginRequest;
import com.trainhub.backend.dto.request.RegisterRequest;
import com.trainhub.backend.dto.response.LoginResponse;
import com.trainhub.backend.dto.response.RegisterResponse;
import com.trainhub.backend.exception.AccountNotActiveException;
import com.trainhub.backend.exception.BadCredentialsException;
import com.trainhub.backend.exception.InvalidPasswordResetTokenException;
import com.trainhub.backend.model.User;
import com.trainhub.backend.model.enums.AccountStatus;
import com.trainhub.backend.repository.UserRepository;
import com.trainhub.backend.security.JwtUtil;
import com.trainhub.backend.service.email.EmailService;

/**
 * Servicio para operaciones de autenticación y registro de usuarios.
 */
@Service
public class AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request La solicitud de registro con los datos del usuario
     * @return La respuesta con el mensaje de éxito y el email
     * @throws DataIntegrityViolationException Si el email ya existe
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // 1. Verificar que el email no exista
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new DataIntegrityViolationException("El email ya está registrado");
        }

        // 1.1 Verificar que el username no exista
        Optional<User> existingUsername = userRepository.findByUsername(request.getUsername());
        if (existingUsername.isPresent()) {
            throw new DataIntegrityViolationException("El nombre de usuario ya está registrado");
        }

        // 2. Hashear contraseña
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // 3. Generar token UUID para confirmación
        String confirmationToken = UUID.randomUUID().toString();

        // 4. Crear User con accountStatus=PENDING_CONFIRMATION
        User user = new User(
                request.getEmail(),
                request.getUsername(),
                passwordHash,
                AccountStatus.PENDING_CONFIRMATION
        );
        user.setEmailVerified(false);
        user.setEmailConfirmationToken(confirmationToken);

        // 5. Guardar en BD
        userRepository.save(user);

        // 6. Enviar email de confirmación (mock)
        emailService.sendConfirmationEmail(request.getEmail(), confirmationToken);

        // 7. Retornar RegisterResponse
        return new RegisterResponse(
                "Registro exitoso. Por favor, revisa tu correo para confirmar tu cuenta.",
                request.getEmail()
        );
    }

    /**
     * Autentica un usuario y genera un token JWT.
     *
     * @param request La solicitud de login con email y contraseña
     * @return La respuesta con el token JWT y mensaje de éxito
     * @throws BadCredentialsException Si el email no existe o la contraseña es incorrecta
     * @throws AccountNotActiveException Si la cuenta no está activa
     */
    public LoginResponse login(LoginRequest request) {
        // 1. Buscar usuario por email
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        User user = userOptional.get();

        // 2. Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        // Después de verificar password
        if (!user.getEmailVerified()) {
            throw new EmailNotVerifiedException("Por favor, confirma tu email primero");
        }

        // 3. Validar estado de cuenta
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("La cuenta no está activa");
        }

        // 4. Generar token JWT
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        // 5. Retornar LoginResponse
        return new LoginResponse(token, "Login exitoso");
    }

    public void confirmEmail(String token) {
        System.out.println("=== BACKEND: Confirmando email ===");
        System.out.println("Token: " + token);
        System.out.println("Endpoint: GET /api/auth/confirm-email/" + token);
        
        // 1. Buscar usuario por token
        Optional<User> userOptional = userRepository.findByEmailConfirmationToken(token);
        if (userOptional.isEmpty()) {
            System.out.println("Token de confirmación inválido: " + token);
            throw new BadCredentialsException("Token de confirmación inválido");
        }

        User user = userOptional.get();

        // 2. Actualizar estado de cuenta a ACTIVE
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setEmailConfirmationToken(null);
        userRepository.save(user);
        System.out.println("=== BACKEND: Email confirmado exitosamente ===");
        System.out.println("User: " + user.getEmail());
        System.out.println("Account Status: " + user.getAccountStatus());
        System.out.println("Email Verified: " + user.getEmailVerified());
        System.out.println("Email Confirmation Token: " + user.getEmailConfirmationToken());
    }

    @Transactional
    public void requestPasswordReset(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();
        String resetToken = UUID.randomUUID().toString();

        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiresAt(OffsetDateTime.now().plusHours(1));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByPasswordResetToken(token);
        if (userOptional.isEmpty()) {
            throw new InvalidPasswordResetTokenException("El token de reseteo es inválido o ha caducado");
        }

        User user = userOptional.get();
        OffsetDateTime expiresAt = user.getPasswordResetExpiresAt();
        if (expiresAt == null || expiresAt.isBefore(OffsetDateTime.now())) {
            throw new InvalidPasswordResetTokenException("El token de reseteo es inválido o ha caducado");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        userRepository.save(user);
    }
}

