package com.trainhub.backend.repository;

import com.trainhub.backend.model.GoalParticipant;
import com.trainhub.backend.model.GoalParticipantId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad GoalParticipant.
 */
@Repository
public interface GoalParticipantRepository extends JpaRepository<GoalParticipant, GoalParticipantId> {

    /**
     * Participantes de un objetivo, con usuario cargado.
     */
    @Query("""
            SELECT gp FROM GoalParticipant gp
            JOIN FETCH gp.user
            WHERE gp.id.goalId = :goalId
            ORDER BY gp.owner DESC, gp.startedAt ASC
            """)
    List<GoalParticipant> findByGoalIdWithUser(@Param("goalId") Integer goalId);

    /**
     * Participantes de varios objetivos, con usuario cargado (batch para la lista).
     */
    @Query("""
            SELECT gp FROM GoalParticipant gp
            JOIN FETCH gp.user
            WHERE gp.id.goalId IN :goalIds
            ORDER BY gp.id.goalId, gp.owner DESC, gp.startedAt ASC
            """)
    List<GoalParticipant> findByGoalIdsWithUser(@Param("goalIds") List<Integer> goalIds);

    /**
     * Participación concreta de un usuario en un objetivo.
     */
    @Query("""
            SELECT gp FROM GoalParticipant gp
            WHERE gp.id.goalId = :goalId AND gp.id.userId = :userId
            """)
    Optional<GoalParticipant> findByGoalIdAndUserId(@Param("goalId") Integer goalId,
                                                    @Param("userId") Integer userId);

    /**
     * Comprueba si el usuario ya participa en el objetivo.
     */
    boolean existsByIdGoalIdAndIdUserId(Integer goalId, Integer userId);

    /**
     * Número de participantes de un objetivo.
     */
    long countByIdGoalId(Integer goalId);

    /**
     * IDs de objetivos (del conjunto dado) en los que el usuario participa.
     */
    @Query("""
            SELECT gp.id.goalId
            FROM GoalParticipant gp
            WHERE gp.id.goalId IN :goalIds
              AND gp.id.userId = :userId
            """)
    List<Integer> findParticipatingGoalIds(@Param("goalIds") List<Integer> goalIds,
                                           @Param("userId") Integer userId);

    /**
     * Adhesiones a objetivos creados por el usuario (no owners), más recientes primero.
     */
    @Query("""
            SELECT gp FROM GoalParticipant gp
            JOIN FETCH gp.user
            JOIN FETCH gp.goal g
            JOIN FETCH g.createdBy
            WHERE g.createdBy.id = :creatorUserId
              AND gp.owner = false
            ORDER BY gp.startedAt DESC
            """)
    List<GoalParticipant> findJoinsOnCreatedGoals(@Param("creatorUserId") Integer creatorUserId);
}
