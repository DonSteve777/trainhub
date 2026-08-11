package com.trainhub.backend.repository;

import com.trainhub.backend.enums.GoalStatus;
import com.trainhub.backend.model.Goal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Goal.
 */
@Repository
public interface GoalRepository extends JpaRepository<Goal, Integer> {

    /**
     * Objetivos en los que el usuario participa, más recientes primero.
     */
    @Query("""
            SELECT g FROM Goal g
            JOIN GoalParticipant gp ON gp.goal = g
            WHERE gp.user.id = :userId
            ORDER BY g.createdAt DESC
            """)
    List<Goal> findByParticipantUserId(@Param("userId") Integer userId);

    /**
     * Objetivos del usuario filtrados por estado, más recientes primero.
     */
    @Query("""
            SELECT g FROM Goal g
            JOIN GoalParticipant gp ON gp.goal = g
            WHERE gp.user.id = :userId
              AND g.status = :status
            ORDER BY g.createdAt DESC
            """)
    List<Goal> findByParticipantUserIdAndStatus(@Param("userId") Integer userId,
                                                @Param("status") GoalStatus status);

    /**
     * Objetivo con su creador cargado (evita LazyInitializationException).
     */
    @Query("""
            SELECT g FROM Goal g
            JOIN FETCH g.createdBy
            WHERE g.id = :goalId
            """)
    Optional<Goal> findByIdWithCreatedBy(@Param("goalId") Integer goalId);
}
