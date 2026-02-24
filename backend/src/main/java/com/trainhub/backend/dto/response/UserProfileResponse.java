package com.trainhub.backend.dto.response;

import com.trainhub.backend.model.enums.AccountStatus;

/**
 * DTO de respuesta para el perfil del usuario.
 */
public class UserProfileResponse {

    private Integer id;
    private String name;
    private String email;
    private String bio;
    private String photoUrl;
    private AccountStatus accountStatus;
    private Boolean emailVerified;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Integer id, String name, String email, String bio, String photoUrl, AccountStatus accountStatus, Boolean emailVerified) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.photoUrl = photoUrl;
        this.accountStatus = accountStatus;
        this.emailVerified = emailVerified;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}
