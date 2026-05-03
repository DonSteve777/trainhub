package com.trainhub.backend.repository;

import com.trainhub.backend.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad User.
 * Proporciona métodos de acceso a datos para usuarios.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Busca un usuario por su email.
     *
     * @param email El email del usuario a buscar
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por su username.
     *
     * @param username El username del usuario a buscar
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por su token de confirmación de email.
     *
     * @param token El token de confirmación de email
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<User> findByEmailConfirmationToken(String token);

    /**
     * Busca un usuario por su token de reseteo de contraseña.
     *
     * @param token El token de reseteo de contraseña
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Busca usuarios cuyo username empieza por el prefijo dado (case-insensitive),
     * excluyendo al propio usuario y los no activos.
     */
    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.username) LIKE LOWER(CONCAT(:prefix, '%'))
            AND u.id <> :excludeId
            AND u.accountStatus = com.trainhub.backend.enums.AccountStatus.ACTIVE
            ORDER BY u.username ASC
            """)
    List<User> findByUsernamePrefix(
            @Param("prefix") String prefix,
            @Param("excludeId") Integer excludeId,
            Pageable pageable);
}

