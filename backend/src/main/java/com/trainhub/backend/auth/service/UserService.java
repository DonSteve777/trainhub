package com.trainhub.backend.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.trainhub.backend.auth.repository.UserRepository;
import com.trainhub.backend.auth.validator.UserValidator;
import com.trainhub.backend.auth.dto.LoginRequestDTO;
import com.trainhub.backend.auth.dto.LoginResponseDTO;
import com.trainhub.backend.auth.dto.UserCreationDTO;
import com.trainhub.backend.auth.model.User;
import com.trainhub.backend.auth.security.JwtService;
import com.trainhub.backend.auth.security.CustomUserDetailsService;

import java.time.LocalDateTime;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public UserService(
            UserRepository userRepository, 
            PasswordEncoder passwordEncoder, 
            UserValidator userValidator,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userValidator = userValidator;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }


    public User register(User user) {
        // ✅ Validaciones
        userValidator.validateEmailAndUsername(user.getEmail(), user.getUsername());

        // 🔐 Hasheamos la password
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        
        // ⏱️ Fechas
        LocalDateTime now = LocalDateTime.now();
        
        // 🏗️ Usando Builder para crear el usuario final
        User userToSave = User.builder()
            .email(user.getEmail())
            .username(user.getUsername())
            .password(hashedPassword)  // Password hasheada
            .createdAt(now)
            .build();

        // 💾 Guardamos en la DB
        return userRepository.save(userToSave);
    }
    
    // Método alternativo usando Builder desde cero
    public User registerNewUser(UserCreationDTO request) {
        String email = request.getEmail();
        String username = request.getUsername();
        String password = request.getPassword();
        // String fullName = request.getFullName();
        // ✅ Validaciones
        userValidator.validateEmailAndUsername(email, username);
        // 🔐 Hasheamos la password
        String hashedPassword = passwordEncoder.encode(password);
        // ⏱️ Fechas
        LocalDateTime now = LocalDateTime.now();
        
        // 🏗️ Crear usuario con Builder
        User newUser = User.builder()
            .email(email)
            .username(username)
            .password(hashedPassword)
            // .fullName(fullName)
            .createdAt(now)
            .build();

        // 💾 Guardamos en la DB
        return userRepository.save(newUser);
    }

    /**
     * 🔐 Método de Login
     * Autentica al usuario y genera un token JWT
     */
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        // 1. Autenticar con Spring Security
//         Las valida contra la base de datos (usando tu CustomUserDetailsService)
// Compara la contraseña ingresada con la almacenada (usando el encoder de contraseñas)
// Si las credenciales son correctas, continúa la ejecución
// Si son incorrectas, lanza una excepción BadCredentialsException
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.usernameOrEmail(),
                        loginRequest.password()
                )
        );

        // 2. Si llega aquí, la autenticación fue exitosa (si no, excepción BadCredentialsException)
        // Obtener el usuario completo de la BD
        User user = userDetailsService.loadUserEntityByUsername(loginRequest.usernameOrEmail());

        // 3. Generar el token JWT
        var userDetails = userDetailsService.loadUserByUsername(loginRequest.usernameOrEmail());
        String jwtToken = jwtService.generateToken(userDetails);

        // 4. Crear y retornar la respuesta
        return new LoginResponseDTO(
                jwtToken,
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    /**
     * 👤 Obtener usuario actual por username
     * Usado para el endpoint /me
     */
    public User getCurrentUser(String username) {
        return userDetailsService.loadUserEntityByUsername(username);
    }
}