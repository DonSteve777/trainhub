package com.trainhub.backend.service.auth;

import com.trainhub.backend.dto.request.RegisterRequest;
import com.trainhub.backend.dto.response.RegisterResponse;
import com.trainhub.backend.model.User;
import com.trainhub.backend.model.enums.AccountStatus;
import com.trainhub.backend.repository.UserRepository;
import com.trainhub.backend.service.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para operaciones de autenticación y registro de usuarios.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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

        // 4. Crear User con accountStatus=PENDING_CONFIRMATION, emailVerified=false
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
}

