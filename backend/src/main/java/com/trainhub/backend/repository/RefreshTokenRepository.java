package com.trainhub.backend.repository;

import com.trainhub.backend.model.RefreshToken;
import com.trainhub.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad RefreshToken.
 * Proporciona métodos de acceso a datos para refresh tokens.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    /**
     * Busca un refresh token por su hash.
     * 
     * @param tokenHash El hash del token a buscar
     * @return Optional con el token si existe, vacío si no
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Busca todos los refresh tokens de un usuario.
     * 
     * @param user El usuario
     * @return Lista de refresh tokens del usuario
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Busca todos los refresh tokens válidos (no revocados y no expirados) de un usuario.
     * 
     * @param user El usuario
     * @param now Fecha/hora actual para comparar con expirationDate
     * @return Lista de refresh tokens válidos del usuario
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expirationDate > :now")
    List<RefreshToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Elimina todos los refresh tokens expirados.
     * 
     * @param now Fecha/hora actual para comparar con expirationDate
     * @return Número de tokens eliminados
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expirationDate < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Elimina todos los refresh tokens revocados o expirados de un usuario.
     * 
     * @param user El usuario
     * @param now Fecha/hora actual para comparar con expirationDate
     * @return Número de tokens eliminados
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user AND (rt.revoked = true OR rt.expirationDate < :now)")
    int deleteInvalidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Revoca todos los refresh tokens de un usuario (excepto el token actual si se proporciona).
     * 
     * @param user El usuario
     * @param currentTokenHash El hash del token actual que no debe revocarse (opcional)
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user AND rt.tokenHash != :currentTokenHash")
    void revokeAllTokensByUserExcept(@Param("user") User user, @Param("currentTokenHash") String currentTokenHash);
}

