package com.trainhub.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de petición para actualizar el perfil del usuario.
 */
public class UpdateUserProfileRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Size(max = 255, message = "El email no puede tener más de 255 caracteres")
    private String email;

    @Size(max = 255, message = "La URL de la foto no puede tener más de 255 caracteres")
    private String photoUrl;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede tener más de 60 caracteres")
    private String name;

    public UpdateUserProfileRequest() {
    }

    // Getters y Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
