package com.trainhub.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trainhub.backend.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 🔎 Custom queries automágicas:
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

}
