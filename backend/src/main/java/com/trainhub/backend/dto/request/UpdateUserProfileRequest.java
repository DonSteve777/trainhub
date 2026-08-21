package com.trainhub.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.trainhub.backend.enums.Gender;

/**
 * DTO de petición para actualizar el perfil del usuario.
 * El email no se incluye: se fija en el registro y no es editable.
 */
public class UpdateUserProfileRequest {

    @Size(max = 255, message = "La URL de la foto no puede tener más de 255 caracteres")
    private String photoUrl;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede tener más de 60 caracteres")
    private String name;

    private Gender gender;

    /** Id del box asignado; {@code null} para dejar al usuario sin box. */
    private Integer boxId;

    public UpdateUserProfileRequest() {
    }

    // Getters y Setters
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Integer getBoxId() {
        return boxId;
    }

    public void setBoxId(Integer boxId) {
        this.boxId = boxId;
    }
}
