package com.trainhub.backend.repository;

import com.trainhub.backend.model.Post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad Post.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    /**
     * Devuelve los posts de amigos y el contenido del box del usuario (primera página, sin cursor).
     * El orden es creation_date DESC, id DESC para un cursor estable.
     */
    @Query("""
            SELECT p FROM Post p
            JOIN FETCH p.user u
            LEFT JOIN FETCH p.mate
            LEFT JOIN FETCH p.box
            WHERE p.user.id <> :userId
            AND (
                (
                    p.postType IN (
                        com.trainhub.backend.enums.PostType.CHECKIN,
                        com.trainhub.backend.enums.PostType.RESULT
                    )
                    AND EXISTS (
                        SELECT f FROM Friendship f
                        WHERE f.status = com.trainhub.backend.enums.FriendshipStatus.FRIEND
                        AND (
                            (f.id.userAId = :userId AND f.id.userBId = p.user.id)
                            OR
                            (f.id.userBId = :userId AND f.id.userAId = p.user.id)
                        )
                    )
                )
                OR (
                    :boxId IS NOT NULL
                    AND p.box.id = :boxId
                    AND p.postType IN (
                        com.trainhub.backend.enums.PostType.BOX_WOD,
                        com.trainhub.backend.enums.PostType.BOX_CHALLENGE,
                        com.trainhub.backend.enums.PostType.BOX_ANNOUNCEMENT
                    )
                )
            )
            ORDER BY p.creationDate DESC, p.id DESC
            """)
    List<Post> findFeedFirstPage(
            @Param("userId") Integer userId,
            @Param("boxId") Integer boxId,
            Pageable pageable);

    default List<Post> findFeedFirstPage(Integer userId, Pageable pageable) {
        return findFeedFirstPage(userId, null, pageable);
    }

    /**
     * Devuelve los tiempos de cada post del propio usuario ,
     * ordenados por fecha ascendente.
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
            AND p.postType = com.trainhub.backend.enums.PostType.RESULT
            AND p.creationDate >= :since
            ORDER BY p.creationDate ASC
            """)
    List<Object[]> findUserPostTimes(
            @Param("userId") Integer userId,
            @Param("since") LocalDateTime since);

    /**
     * Devuelve los tiempos de todos los posts de los amigos del usuario, con su nombre de usuario
     * y fecha, sin límite de fecha. Se usa para pintar los marcadores de amigos en las gráficas
     * de progresión.
     * Cada fila: [username, totalTime, r1..r8, w1..w8, creationDate] (19 columnas).
     */
    @Query("""
            SELECT p.user.username, p.totalTime,
                   p.running1, p.running2, p.running3, p.running4,
                   p.running5, p.running6, p.running7, p.running8,
                   p.skiErg, p.sledPush, p.sledPull, p.burpeeBroadJump,
                   p.row, p.farmersCarry, p.sandbagLunges, p.wallBalls,
                   p.creationDate
            FROM Post p
            WHERE p.postType = com.trainhub.backend.enums.PostType.RESULT
            AND EXISTS (
                SELECT f FROM Friendship f
                WHERE f.status = com.trainhub.backend.enums.FriendshipStatus.FRIEND
                AND (
                    (f.id.userAId = :userId AND f.id.userBId = p.user.id)
                    OR
                    (f.id.userBId = :userId AND f.id.userAId = p.user.id)
                )
            )
            ORDER BY p.user.username ASC, p.creationDate ASC
            """)
    List<Object[]> findFriendsPostTimesWithUser(@Param("userId") Integer userId);

    /**
     * Devuelve todos los posts de un usuario concreto, ordenados del más reciente al más antiguo.
     * Sin límite de fecha: se devuelve el historial completo.
     */
    @Query("""
            SELECT p FROM Post p JOIN FETCH p.user u LEFT JOIN FETCH p.mate
            WHERE p.user.id = :targetUserId
            ORDER BY p.creationDate DESC, p.id DESC
            """)
    List<Post> findPostsByUserId(@Param("targetUserId") Integer targetUserId);

    /**
     * Devuelve los tiempos individuales de todos los posts del usuario (sin filtro de fecha),
     * ordenados cronológicamente. Se usa para calcular records personales all-time.
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
            AND p.postType = com.trainhub.backend.enums.PostType.RESULT
            ORDER BY p.creationDate ASC
            """)
    List<Object[]> findAllUserPostTimes(@Param("userId") Integer userId);

    /**
     * Devuelve los días únicos en los que el usuario tuvo actividad propia.
     */
    @Query(value = """
            SELECT DISTINCT CAST(p.creation_date AS date)
            FROM posts p
            WHERE p.user_id = :userId
            AND p.post_type IN ('CHECKIN', 'RESULT')
            ORDER BY CAST(p.creation_date AS date) DESC
            """, nativeQuery = true)
    List<LocalDate> findActivityDatesForUser(@Param("userId") Integer userId);

    /**
     * Devuelve los posts de amigos y el contenido del box después del cursor dado (paginación keyset).
     * El cursor es (creationDate, id): se traen posts anteriores en el tiempo al cursor.
     */
    @Query("""
            SELECT p FROM Post p
            JOIN FETCH p.user u
            LEFT JOIN FETCH p.mate
            LEFT JOIN FETCH p.box
            WHERE p.user.id <> :userId
            AND (
                (
                    p.postType IN (
                        com.trainhub.backend.enums.PostType.CHECKIN,
                        com.trainhub.backend.enums.PostType.RESULT
                    )
                    AND EXISTS (
                        SELECT f FROM Friendship f
                        WHERE f.status = com.trainhub.backend.enums.FriendshipStatus.FRIEND
                        AND (
                            (f.id.userAId = :userId AND f.id.userBId = p.user.id)
                            OR
                            (f.id.userBId = :userId AND f.id.userAId = p.user.id)
                        )
                    )
                )
                OR (
                    :boxId IS NOT NULL
                    AND p.box.id = :boxId
                    AND p.postType IN (
                        com.trainhub.backend.enums.PostType.BOX_WOD,
                        com.trainhub.backend.enums.PostType.BOX_CHALLENGE,
                        com.trainhub.backend.enums.PostType.BOX_ANNOUNCEMENT
                    )
                )
            )
            AND (p.creationDate < :cursorDate
                OR (p.creationDate = :cursorDate AND p.id < :cursorId))
            ORDER BY p.creationDate DESC, p.id DESC
            """)
    List<Post> findFeedWithCursor(
            @Param("userId") Integer userId,
            @Param("boxId") Integer boxId,
            @Param("cursorDate") LocalDateTime cursorDate,
            @Param("cursorId") Integer cursorId,
            Pageable pageable);

    default List<Post> findFeedWithCursor(
            Integer userId,
            LocalDateTime cursorDate,
            Integer cursorId,
            Pageable pageable) {
        return findFeedWithCursor(userId, null, cursorDate, cursorId, pageable);
    }
}
