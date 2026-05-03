package com.trainhub.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import com.trainhub.backend.enums.AccountStatus;
import com.trainhub.backend.enums.Gender;

/**
 * Entidad que representa un usuario en el sistema.
 * Mapeada a la tabla "users" de la base de datos.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    @NotNull
    @Email
    private String email;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    @NotNull
    @Size(max = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    @NotNull
    private String passwordHash;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    @NotNull
    private AccountStatus accountStatus;

    @Column(name = "email_verified", nullable = false)
    @NotNull
    private Boolean emailVerified = false;

    @Column(name = "email_confirmation_token", length = 255)
    private String emailConfirmationToken;

    @Column(name = "password_reset_token", length = 255)
    private String passwordResetToken;

    @Column(name = "password_reset_expires_at")
    private OffsetDateTime passwordResetExpiresAt;

    @Column(name = "notifications_last_seen_at")
    private LocalDateTime notificationsLastSeenAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    // Constructores
    public User() {
        this.emailVerified = false;
    }

    public User(String email, String passwordHash, AccountStatus accountStatus) {
        this();
        this.email = email;
        this.passwordHash = passwordHash;
        this.accountStatus = accountStatus;
    }

    public User(String email, String username, String passwordHash, AccountStatus accountStatus) {
        this();
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.accountStatus = accountStatus;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public String getEmailConfirmationToken() {
        return emailConfirmationToken;
    }

    public void setEmailConfirmationToken(String emailConfirmationToken) {
        this.emailConfirmationToken = emailConfirmationToken;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public OffsetDateTime getPasswordResetExpiresAt() {
        return passwordResetExpiresAt;
    }

    public void setPasswordResetExpiresAt(OffsetDateTime passwordResetExpiresAt) {
        this.passwordResetExpiresAt = passwordResetExpiresAt;
    }

    public LocalDateTime getNotificationsLastSeenAt() {
        return notificationsLastSeenAt;
    }

    public void setNotificationsLastSeenAt(LocalDateTime notificationsLastSeenAt) {
        this.notificationsLastSeenAt = notificationsLastSeenAt;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

}

