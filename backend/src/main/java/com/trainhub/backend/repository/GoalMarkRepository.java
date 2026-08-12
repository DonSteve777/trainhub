package com.trainhub.backend.repository;

import com.trainhub.backend.model.GoalMark;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad GoalMark.
 */
@Repository
public interface GoalMarkRepository extends JpaRepository<GoalMark, Integer> {

    /**
     * Marcas de un usuario en un objetivo, más recientes primero.
     */
    @Query("""
            SELECT gm FROM GoalMark gm
            WHERE gm.goal.id = :goalId AND gm.user.id = :userId
            ORDER BY gm.recordedAt DESC, gm.id DESC
            """)
    List<GoalMark> findByGoalIdAndUserId(@Param("goalId") Integer goalId,
                                         @Param("userId") Integer userId);

    /**
     * Todas las marcas de un objetivo, más recientes primero.
     */
    @Query("""
            SELECT gm FROM GoalMark gm
            JOIN FETCH gm.user
            WHERE gm.goal.id = :goalId
            ORDER BY gm.recordedAt DESC, gm.id DESC
            """)
    List<GoalMark> findByGoalIdWithUser(@Param("goalId") Integer goalId);

    /**
     * Marcas de varios objetivos (batch para la lista).
     */
    @Query("""
            SELECT gm FROM GoalMark gm
            JOIN FETCH gm.goal
            JOIN FETCH gm.user
            WHERE gm.goal.id IN :goalIds
            ORDER BY gm.goal.id, gm.recordedAt DESC, gm.id DESC
            """)
    List<GoalMark> findByGoalIds(@Param("goalIds") List<Integer> goalIds);

    /**
     * Marcas de varios usuarios en un objetivo (p. ej. ranking de amigos).
     */
    @Query("""
            SELECT gm FROM GoalMark gm
            JOIN FETCH gm.user
            WHERE gm.goal.id = :goalId
              AND gm.user.id IN :userIds
            ORDER BY gm.recordedAt DESC, gm.id DESC
            """)
    List<GoalMark> findByGoalIdAndUserIdIn(@Param("goalId") Integer goalId,
                                           @Param("userIds") List<Integer> userIds);
}
