package com.trainhub.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de petición para actualizar el perfil del usuario.
 */
public class UpdateUserProfileRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede tener más de 60 caracteres")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Size(max = 255, message = "El email no puede tener más de 255 caracteres")
    private String email;

    @Size(max = 1000, message = "La biografía no puede tener más de 1000 caracteres")
    private String bio;

    @Size(max = 255, message = "La URL de la foto no puede tener más de 255 caracteres")
    private String photoUrl;

    public UpdateUserProfileRequest() {
    }

    public UpdateUserProfileRequest(String name, String email, String bio, String photoUrl) {
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.photoUrl = photoUrl;
    }

    // Getters y Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
