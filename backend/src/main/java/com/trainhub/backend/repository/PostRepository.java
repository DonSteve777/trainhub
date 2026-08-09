package com.trainhub.backend.repository;

import com.trainhub.backend.model.Post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
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
            LEFT JOIN FETCH p.box
            LEFT JOIN FETCH p.wodPost
            WHERE p.user.id <> :userId
            AND (
                (
                    p.postType = com.trainhub.backend.enums.PostType.CHECKIN
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
     * Devuelve los posts visibles de un usuario concreto, ordenados del más reciente al más antiguo.
     * Solo tipos del modelo actual (excluye restos legacy como RESULT en BD).
     * Sin límite de fecha: se devuelve el historial completo.
     */
    @Query("""
            SELECT p FROM Post p JOIN FETCH p.user u LEFT JOIN FETCH p.box LEFT JOIN FETCH p.wodPost
            WHERE p.user.id = :targetUserId
            AND p.postType IN (
                com.trainhub.backend.enums.PostType.CHECKIN,
                com.trainhub.backend.enums.PostType.BOX_WOD,
                com.trainhub.backend.enums.PostType.BOX_CHALLENGE,
                com.trainhub.backend.enums.PostType.BOX_ANNOUNCEMENT
            )
            ORDER BY p.creationDate DESC, p.id DESC
            """)
    List<Post> findPostsByUserId(@Param("targetUserId") Integer targetUserId);

    /**
     * Días de actividad propios (CHECKIN): como máximo uno por fecha.
     * Cada fila: [date, training_tag, post_type].
     */
    @Query(value = """
            SELECT DISTINCT ON (CAST(p.creation_date AS date))
                   CAST(p.creation_date AS date),
                   p.training_tag,
                   p.post_type
            FROM posts p
            WHERE p.user_id = :userId
            AND p.post_type = 'CHECKIN'
            ORDER BY CAST(p.creation_date AS date) DESC, p.creation_date DESC
            """, nativeQuery = true)
    List<Object[]> findActivityDaysForUser(@Param("userId") Integer userId);

    /**
     * Días de actividad para varios usuarios (batch feed).
     * Cada fila: [userId, date, training_tag, post_type].
     */
    @Query(value = """
            SELECT DISTINCT ON (p.user_id, CAST(p.creation_date AS date))
                   p.user_id,
                   CAST(p.creation_date AS date),
                   p.training_tag,
                   p.post_type
            FROM posts p
            WHERE p.user_id IN (:userIds)
            AND p.post_type = 'CHECKIN'
            ORDER BY p.user_id, CAST(p.creation_date AS date) DESC, p.creation_date DESC
            """, nativeQuery = true)
    List<Object[]> findActivityDaysForUsers(@Param("userIds") Collection<Integer> userIds);

    /**
     * Devuelve los posts de amigos y el contenido del box después del cursor dado (paginación keyset).
     * El cursor es (creationDate, id): se traen posts anteriores en el tiempo al cursor.
     */
    @Query("""
            SELECT p FROM Post p
            JOIN FETCH p.user u
            LEFT JOIN FETCH p.box
            LEFT JOIN FETCH p.wodPost
            WHERE p.user.id <> :userId
            AND (
                (
                    p.postType = com.trainhub.backend.enums.PostType.CHECKIN
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

    /**
     * Indica si el usuario ya tiene un check-in vinculado al WOD dado.
     */
    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
            FROM Post p
            WHERE p.user.id = :userId
            AND p.wodPost.id = :wodPostId
            AND p.postType = com.trainhub.backend.enums.PostType.CHECKIN
            """)
    boolean existsByUserIdAndWodPostId(
            @Param("userId") Integer userId,
            @Param("wodPostId") Integer wodPostId);

    /**
     * WODs del box desde {@code since} (p. ej. lunes de la semana ISO actual), tope por pageable.
     */
    @Query("""
            SELECT p FROM Post p
            WHERE p.box.id = :boxId
            AND p.postType = com.trainhub.backend.enums.PostType.BOX_WOD
            AND p.creationDate >= :since
            ORDER BY p.creationDate DESC, p.id DESC
            """)
    List<Post> findRecentBoxWods(
            @Param("boxId") Integer boxId,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    /**
     * Conteos de check-ins vinculados por WOD. Cada fila: [wodPostId, count].
     */
    @Query("""
            SELECT p.wodPost.id, COUNT(p)
            FROM Post p
            WHERE p.wodPost.id IN :wodIds
            AND p.postType = com.trainhub.backend.enums.PostType.CHECKIN
            GROUP BY p.wodPost.id
            """)
    List<Object[]> countCheckinsByWodIds(@Param("wodIds") Collection<Integer> wodIds);

    /**
     * Autores de check-ins vinculados a WODs, más recientes primero.
     * Cada fila: [wodPostId, userId, username, photoUrl].
     */
    @Query("""
            SELECT p.wodPost.id, u.id, u.username, u.photoUrl
            FROM Post p
            JOIN p.user u
            WHERE p.wodPost.id IN :wodIds
            AND p.postType = com.trainhub.backend.enums.PostType.CHECKIN
            ORDER BY p.creationDate DESC, p.id DESC
            """)
    List<Object[]> findCheckinAuthorsByWodIds(@Param("wodIds") Collection<Integer> wodIds);

    /**
     * Lista completa de autores de check-in de un WOD.
     * Cada fila: [userId, username, photoUrl, creationDate].
     */
    @Query("""
            SELECT u.id, u.username, u.photoUrl, p.creationDate
            FROM Post p
            JOIN p.user u
            WHERE p.wodPost.id = :wodId
            AND p.postType = com.trainhub.backend.enums.PostType.CHECKIN
            ORDER BY p.creationDate DESC, p.id DESC
            """)
    List<Object[]> findCheckinAuthorsByWodId(@Param("wodId") Integer wodId);
}
