package com.trainhub.backend.repository;

import com.trainhub.backend.model.PostParticipant;
import com.trainhub.backend.model.PostParticipantId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad PostParticipant.
 */
@Repository
public interface PostParticipantRepository extends JpaRepository<PostParticipant, PostParticipantId> {

    /**
     * Devuelve el número de participantes por post para un conjunto de post IDs.
     * Cada elemento del resultado es un Object[] con [postId (Integer), count (Long)].
     * Se usa para rellenar participantsCount en el feed sin hacer N consultas.
     */
    @Query("""
            SELECT pp.id.postId, COUNT(pp)
            FROM PostParticipant pp
            WHERE pp.id.postId IN :postIds
            GROUP BY pp.id.postId
            """)
    List<Object[]> countByPostIds(@Param("postIds") List<Integer> postIds);

    /**
     * Devuelve los IDs de posts (del conjunto dado) en los que el usuario ya participa.
     * Se usa para rellenar joinedByCurrentUser en el feed sin hacer N consultas.
     */
    @Query("""
            SELECT pp.id.postId
            FROM PostParticipant pp
            WHERE pp.id.postId IN :postIds
            AND pp.id.userId = :userId
            """)
    List<Integer> findJoinedPostIds(@Param("postIds") List<Integer> postIds,
                                     @Param("userId") Integer userId);

    /**
     * Cuenta el número de participantes de un post concreto.
     */
    @Query("SELECT COUNT(pp) FROM PostParticipant pp WHERE pp.id.postId = :postId")
    long countByPostId(@Param("postId") Integer postId);
}
