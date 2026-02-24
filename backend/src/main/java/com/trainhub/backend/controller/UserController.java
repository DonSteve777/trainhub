package com.trainhub.backend.controller;

import com.trainhub.backend.dto.request.UpdateUserProfileRequest;
import com.trainhub.backend.dto.response.UserProfileResponse;
import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.UserRepository;
import com.trainhub.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * Controlador REST para operaciones de usuario.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtiene la información del perfil del usuario autenticado.
     *
     * @param userPrincipal El usuario autenticado actual
     * @return La información del perfil del usuario
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        
        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBio(),
                user.getPhotoUrl(),
                user.getAccountStatus(),
                user.getEmailVerified()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     *
     * @param userPrincipal El usuario autenticado actual
     * @param request Los datos del perfil a actualizar
     * @return El perfil actualizado
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        
        User user = userPrincipal.getUser();

        // Verificar si el email ha cambiado y si ya está en uso por otro usuario
        if (!user.getEmail().equals(request.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El email ya está en uso por otro usuario"
                );
            }
        }

        // Actualizar los campos del usuario
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setBio(request.getBio());
        user.setPhotoUrl(request.getPhotoUrl());

        // Guardar cambios
        User updatedUser = userRepository.save(user);

        // Crear respuesta
        UserProfileResponse response = new UserProfileResponse(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getBio(),
                updatedUser.getPhotoUrl(),
                updatedUser.getAccountStatus(),
                updatedUser.getEmailVerified()
        );

        return ResponseEntity.ok(response);
    }
}
