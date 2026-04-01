package com.trainhub.backend.repository;

import com.trainhub.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}

