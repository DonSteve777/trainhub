package com.trainhub.backend.repository;

import com.trainhub.backend.model.Box;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad Box.
 */
@Repository
public interface BoxRepository extends JpaRepository<Box, Integer> {
}
