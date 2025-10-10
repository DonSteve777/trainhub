package com.trainhub.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank(message = "El nombre de usuario o email es obligatorio")
    String usernameOrEmail,
    
    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {}

