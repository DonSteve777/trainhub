package com.trainhub.backend.service.auth;

import java.util.Optional;
import java.util.UUID;

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
import com.trainhub.backend.model.User;
import com.trainhub.backend.model.enums.AccountStatus;
import com.trainhub.backend.repository.UserRepository;
import com.trainhub.backend.security.JwtUtil;
import com.trainhub.backend.service.email.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio para operaciones de autenticación y registro de usuarios.
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

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

        // 2. Hashear contraseña
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // 3. Generar token UUID para confirmación
        String confirmationToken = UUID.randomUUID().toString();

        // 4. Crear User con accountStatus=ACTIVE, emailVerified=true
        // (La confirmación por email no está implementada todavía)
        User user = new User(
                request.getEmail(),
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
        // 1. Buscar usuario por token
        Optional<User> userOptional = userRepository.findByEmailConfirmationToken(token);
        if (userOptional.isEmpty()) {
            logger.error("Token de confirmación inválido: {}", token);
            throw new BadCredentialsException("Token de confirmación inválido");
        }

        User user = userOptional.get();

        // 2. Actualizar estado de cuenta a ACTIVE
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setEmailConfirmationToken(null);
        userRepository.save(user);
    }
}

