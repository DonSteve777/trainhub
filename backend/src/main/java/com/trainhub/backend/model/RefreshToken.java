package com.trainhub.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa un refresh token para autenticación JWT.
 * Mapea a la tabla "Refresh_Tokens" en PostgreSQL.
 * La clave primaria es token_hash (String), no auto-generada.
 */
@Entity
@Table(name = "Refresh_Tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @Column(name = "token_hash", length = 255, nullable = false)
    @NotNull(message = "El hash del token es requerido")
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refresh_tokens_user_id"))
    @NotNull(message = "El usuario es requerido")
    private User user;

    @Column(name = "expiration_date", nullable = false)
    @NotNull(message = "La fecha de expiración es requerida")
    private LocalDateTime expirationDate;

    @Column(name = "revoked", nullable = false)
    @NotNull
    private Boolean revoked = false;

    @Column(name = "creation_date", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime creationDate;

    // Constructor personalizado para inicializar valores por defecto
    public RefreshToken(String tokenHash, User user, LocalDateTime expirationDate) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.expirationDate = expirationDate;
        this.creationDate = LocalDateTime.now();
        this.revoked = false;
    }

    // Métodos de utilidad
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationDate);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    @PrePersist
    protected void onCreate() {
        if (creationDate == null) {
            creationDate = LocalDateTime.now();
        }
        if (revoked == null) {
            revoked = false;
        }
    }
}

