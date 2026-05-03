package com.trainhub.backend.repository;

import com.trainhub.backend.model.Comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Comment.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    /**
     * Devuelve todos los comentarios de un post ordenados por fecha ascendente.
     */
    @Query("""
            SELECT c FROM Comment c JOIN FETCH c.user u
            WHERE c.post.id = :postId
            ORDER BY c.creationDate ASC, c.id ASC
            """)
    List<Comment> findByPostId(@Param("postId") Integer postId);

    /**
     * Devuelve el número de comentarios por post para un conjunto de post IDs.
     * Cada elemento del resultado es un Object[] con [postId (Integer), count (Long)].
     * Se usa para rellenar commentsCount en el feed sin hacer N consultas.
     */
    @Query("""
            SELECT c.post.id, COUNT(c)
            FROM Comment c
            WHERE c.post.id IN :postIds
            GROUP BY c.post.id
            """)
    List<Object[]> countByPostIds(@Param("postIds") List<Integer> postIds);

    /**
     * Devuelve el número de likes agrupado por comentario para un conjunto de IDs.
     * Cada elemento es un Object[] con [commentId (Integer), count (Long)].
     */
    @Query("""
            SELECT c.id, COUNT(u)
            FROM Comment c JOIN c.likedBy u
            WHERE c.id IN :commentIds
            GROUP BY c.id
            """)
    List<Object[]> countLikesByCommentIds(@Param("commentIds") List<Integer> commentIds);

    /**
     * Devuelve los IDs de comentarios (del conjunto dado) a los que el usuario ya ha dado like.
     */
    @Query("""
            SELECT c.id
            FROM Comment c JOIN c.likedBy u
            WHERE c.id IN :commentIds AND u.id = :userId
            """)
    List<Integer> findLikedCommentIds(@Param("commentIds") List<Integer> commentIds,
                                      @Param("userId") Integer userId);

    /**
     * Devuelve todos los comentarios sobre posts del usuario dado (excluyendo auto-comentarios),
     * ordenados por fecha descendente. Cada fila es:
     * [postId, postCreationDate, commenterId, commenterUsername, commenterPhotoUrl, commentCreatedAt]
     */
    @Query("""
            SELECT c.post.id,
                   c.post.creationDate,
                   c.user.id,
                   c.user.username,
                   c.user.photoUrl,
                   c.creationDate
            FROM Comment c
            WHERE c.post.user.id = :ownerId
              AND c.user.id <> :ownerId
            ORDER BY c.creationDate DESC
            """)
    List<Object[]> findCommentsOnUserPosts(@Param("ownerId") Integer ownerId);
}
