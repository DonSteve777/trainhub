package com.trainhub.backend.model;

import com.trainhub.backend.model.enums.AccountStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un usuario en el sistema.
 * Mapea a la tabla "users" en PostgreSQL.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 60)
    @NotNull(message = "El nombre es requerido")
    @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    @NotNull(message = "El email es requerido")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    @NotNull(message = "La contraseña es requerida")
    private String passwordHash;

    @Column(name = "bio", columnDefinition = "TEXT")
    @Size(max = 280, message = "La biografía no puede exceder 280 caracteres")
    private String bio;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    @NotNull(message = "El estado de cuenta es requerido")
    private AccountStatus accountStatus;

    @Column(name = "email_verified", nullable = false)
    @NotNull
    private Boolean emailVerified = false;

    @Column(name = "registration_date", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime registrationDate;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    // Constructor personalizado
    public User(String name, String email, String passwordHash, AccountStatus accountStatus) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.accountStatus = accountStatus;
        this.registrationDate = LocalDateTime.now();
        this.emailVerified = false;
    }

    // Métodos de utilidad
    public void addRefreshToken(RefreshToken refreshToken) {
        refreshTokens.add(refreshToken);
        refreshToken.setUser(this);
    }

    public void removeRefreshToken(RefreshToken refreshToken) {
        refreshTokens.remove(refreshToken);
        refreshToken.setUser(null);
    }

    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        if (emailVerified == null) {
            emailVerified = false;
        }
    }
}

public enum AccountStatus {
    PENDING_CONFIRMATION,
    ACTIVE,
    BLOCKED
}



