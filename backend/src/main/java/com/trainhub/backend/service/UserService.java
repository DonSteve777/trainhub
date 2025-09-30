package com.trainhub.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.trainhub.backend.repository.UserRepository;
import com.trainhub.backend.validation.UserValidator;
import com.trainhub.backend.dto.UserCreationDTO;
import com.trainhub.backend.model.User;
import java.time.LocalDateTime;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userValidator = userValidator;
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
            .fullName(user.getFullName())
            .bio(user.getBio())
            .profilePictureUrl(user.getProfilePictureUrl())
            .location(user.getLocation())
            .createdAt(now)
            .updatedAt(now)
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
            .updatedAt(now)
            .build();

        // 💾 Guardamos en la DB
        return userRepository.save(newUser);
    }


    
}
