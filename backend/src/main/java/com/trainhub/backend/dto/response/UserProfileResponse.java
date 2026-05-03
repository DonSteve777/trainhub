package com.trainhub.backend.dto.response;

import com.trainhub.backend.enums.AccountStatus;
import com.trainhub.backend.enums.Gender;

/**
 * DTO de respuesta para el perfil del usuario.
 */
public class UserProfileResponse {

    private Integer id;
    private String email;
    private String photoUrl;
    private String name;
    private AccountStatus accountStatus;
    private Boolean emailVerified;
    private Gender gender;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Integer id, String email, String photoUrl, String name, AccountStatus accountStatus, Boolean emailVerified, Gender gender) {
        this.id = id;
        this.email = email;
        this.photoUrl = photoUrl;
        this.name = name;
        this.accountStatus = accountStatus;
        this.emailVerified = emailVerified;
        this.gender = gender;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }
}
