package com.trainhub.backend.repository;

import com.trainhub.backend.model.Post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad Post.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    /**
     * Devuelve los posts de amigos del usuario (primera página, sin cursor).
     * El orden es creation_date DESC, id DESC para un cursor estable.
     */
    @Query("""
            SELECT p FROM Post p JOIN FETCH p.user u LEFT JOIN FETCH p.mate
            WHERE EXISTS (
                SELECT f FROM Friendship f
                WHERE f.status = com.trainhub.backend.enums.FriendshipStatus.FRIEND
                AND (
                    (f.id.userAId = :userId AND f.id.userBId = p.user.id)
                    OR
                    (f.id.userBId = :userId AND f.id.userAId = p.user.id)
                )
            )
            ORDER BY p.creationDate DESC, p.id DESC
            """)
    List<Post> findFeedFirstPage(
            @Param("userId") Integer userId,
            Pageable pageable);

    /**
     * Devuelve los tiempos individuales de cada post de los amigos del usuario,
     * sin paginación, para construir las distribuciones en el frontend.
     * Cada fila: [totalTime, r1..r8, w1..w8] (17 columnas, índice i = mismo post)
     */
    @Query("""
            SELECT p.totalTime,
                   p.running1, p.running2, p.running3, p.running4,
                   p.running5, p.running6, p.running7, p.running8,
                   p.skiErg, p.sledPush, p.sledPull, p.burpeeBroadJump,
                   p.row, p.farmersCarry, p.sandbagLunges, p.wallBalls
            FROM Post p
            WHERE EXISTS (
                SELECT f FROM Friendship f
                WHERE f.status = com.trainhub.backend.enums.FriendshipStatus.FRIEND
                AND (
                    (f.id.userAId = :userId AND f.id.userBId = p.user.id)
                    OR
                    (f.id.userBId = :userId AND f.id.userAId = p.user.id)
                )
            )
            """)
    List<Object[]> findFriendPostTimes(@Param("userId") Integer userId);

    /**
     * Devuelve los tiempos de cada post del propio usuario, ordenados por fecha ascendente.
     * Cada fila: [totalTime, r1..r8, w1..w8, creationDate] (18 columnas).
     */
    @Query("""
            SELECT p.totalTime,
                   p.running1, p.running2, p.running3, p.running4,
                   p.running5, p.running6, p.running7, p.running8,
                   p.skiErg, p.sledPush, p.sledPull, p.burpeeBroadJump,
                   p.row, p.farmersCarry, p.sandbagLunges, p.wallBalls,
                   p.creationDate
            FROM Post p
            WHERE p.user.id = :userId
            ORDER BY p.creationDate ASC
            """)
    List<Object[]> findUserPostTimes(@Param("userId") Integer userId);

    /**
     * Devuelve los posts de amigos del usuario después del cursor dado (paginación keyset).
     * El cursor es (creationDate, id): se traen posts anteriores en el tiempo al cursor.
     */
    @Query("""
            SELECT p FROM Post p JOIN FETCH p.user u LEFT JOIN FETCH p.mate
            WHERE EXISTS (
                SELECT f FROM Friendship f
                WHERE f.status = com.trainhub.backend.enums.FriendshipStatus.FRIEND
                AND (
                    (f.id.userAId = :userId AND f.id.userBId = p.user.id)
                    OR
                    (f.id.userBId = :userId AND f.id.userAId = p.user.id)
                )
            )
            AND (p.creationDate < :cursorDate
                OR (p.creationDate = :cursorDate AND p.id < :cursorId))
            ORDER BY p.creationDate DESC, p.id DESC
            """)
    List<Post> findFeedWithCursor(
            @Param("userId") Integer userId,
            @Param("cursorDate") LocalDateTime cursorDate,
            @Param("cursorId") Integer cursorId,
            Pageable pageable);
}
