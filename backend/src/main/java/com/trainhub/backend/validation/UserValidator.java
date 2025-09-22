package com.trainhub.backend.validation;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.trainhub.backend.exception.EmailAlreadyExistsException;
import com.trainhub.backend.exception.UsernameAlreadyExistsException;
import com.trainhub.backend.repository.UserRepository;

@Component
public class UserValidator {
    private final UserRepository userRepository;

    @Autowired
    public UserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validateEmailAndUsername(String email, String username) {
        // Validación más elegante usando Optional
        Optional.of(email)
            .filter(e -> !userRepository.existsByEmail(e))
            .orElseThrow(() -> new EmailAlreadyExistsException("El email ya está en uso"));
            
        Optional.of(username)
            .filter(u -> !userRepository.existsByUsername(u))
            .orElseThrow(() -> new UsernameAlreadyExistsException("Username already exists: " + username));
    }
}
