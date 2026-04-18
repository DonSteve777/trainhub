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
}
