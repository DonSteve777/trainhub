package com.trainhub.backend.repository;

import com.trainhub.backend.model.PostLike;
import com.trainhub.backend.model.PostLikeId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad PostLike.
 */
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    /**
     * Devuelve el número de likes por post para un conjunto de post IDs.
     * Cada elemento del resultado es un Object[] con [postId (Integer), count (Long)].
     * Se usa para rellenar likesCount en el feed sin hacer N consultas.
     */
    @Query("""
            SELECT pl.id.postId, COUNT(pl)
            FROM PostLike pl
            WHERE pl.id.postId IN :postIds
            GROUP BY pl.id.postId
            """)
    List<Object[]> countByPostIds(@Param("postIds") List<Integer> postIds);

    /**
     * Devuelve los IDs de posts (del conjunto dado) a los que el usuario ya ha dado like.
     * Se usa para rellenar likedByCurrentUser en el feed sin hacer N consultas.
     */
    @Query("""
            SELECT pl.id.postId
            FROM PostLike pl
            WHERE pl.id.postId IN :postIds
            AND pl.id.userId = :userId
            """)
    List<Integer> findLikedPostIds(@Param("postIds") List<Integer> postIds,
                                   @Param("userId") Integer userId);

    /**
     * Cuenta el número de likes de un post concreto.
     */
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.id.postId = :postId")
    long countByPostId(@Param("postId") Integer postId);

    /**
     * Devuelve todos los likes sobre posts del usuario dado (excluyendo auto-likes),
     * ordenados por fecha descendente. Cada fila es:
     * [postId, postCreationDate, likerId, likerUsername, likerPhotoUrl, createdAt]
     */
    @Query("""
            SELECT pl.post.id,
                   pl.post.creationDate,
                   pl.user.id,
                   pl.user.username,
                   pl.user.photoUrl,
                   pl.createdAt
            FROM PostLike pl
            WHERE pl.post.user.id = :ownerId
              AND pl.id.userId <> :ownerId
            ORDER BY pl.createdAt DESC
            """)
    List<Object[]> findLikesOnUserPosts(@Param("ownerId") Integer ownerId);

    /**
     * Devuelve los likers de un post concreto ordenados por fecha descendente.
     * Cada fila es: [userId, username, photoUrl, createdAt]
     */
    @Query("""
            SELECT pl.user.id,
                   pl.user.username,
                   pl.user.photoUrl,
                   pl.createdAt
            FROM PostLike pl
            WHERE pl.id.postId = :postId
            ORDER BY pl.createdAt DESC
            """)
    List<Object[]> findLikersByPostId(@Param("postId") Integer postId);
}
