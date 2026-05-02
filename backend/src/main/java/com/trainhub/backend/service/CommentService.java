package com.trainhub.backend.service;

import com.trainhub.backend.dto.response.CommentResponse;
import com.trainhub.backend.dto.response.LikeToggleResponse;
import com.trainhub.backend.model.Comment;
import com.trainhub.backend.model.Post;
import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.CommentRepository;
import com.trainhub.backend.repository.PostRepository;
import com.trainhub.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar los comentarios de los posts.
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * Devuelve todos los comentarios de un post, ordenados cronológicamente.
     * Incluye el conteo de likes y si el usuario autenticado ya dio like a cada comentario.
     *
     * @param postId id del post
     * @param userId id del usuario autenticado (null si no hay sesión)
     * @return lista de CommentResponse
     */
    public List<CommentResponse> getComments(Integer postId, Integer userId) {
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post no encontrado");
        }

        List<Comment> comments = commentRepository.findByPostId(postId);
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> commentIds = comments.stream().map(Comment::getId).toList();

        Map<Integer, Long> likesCountMap = commentRepository.countLikesByCommentIds(commentIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> (Long) row[1]
                ));

        Set<Integer> likedIds = userId != null
                ? Set.copyOf(commentRepository.findLikedCommentIds(commentIds, userId))
                : Collections.emptySet();

        return comments.stream()
                .map(c -> toResponse(c,
                        likesCountMap.getOrDefault(c.getId(), 0L),
                        likedIds.contains(c.getId())))
                .toList();
    }

    /**
     * Añade un comentario al post indicado por el usuario autenticado.
     *
     * @param postId  id del post
     * @param userId  id del usuario que comenta
     * @param content texto del comentario
     * @return el comentario creado como CommentResponse
     */
    @Transactional
    public CommentResponse addComment(Integer postId, Integer userId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Comment comment = new Comment(post, user, content, LocalDateTime.now());
        Comment saved = commentRepository.save(comment);

        return toResponse(saved, 0L, false);
    }

    /**
     * Añade o quita el like del usuario autenticado sobre el comentario indicado.
     *
     * @param commentId id del comentario
     * @param userId    id del usuario que da/quita el like
     * @return nuevo estado del like y conteo actualizado
     */
    @Transactional
    public LikeToggleResponse toggleLike(Integer commentId, Integer userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        boolean alreadyLiked = comment.getLikedBy().stream()
                .anyMatch(u -> u.getId().equals(userId));

        if (alreadyLiked) {
            comment.getLikedBy().remove(user);
        } else {
            comment.getLikedBy().add(user);
        }

        commentRepository.save(comment);

        long newCount = comment.getLikedBy().size();
        return new LikeToggleResponse(!alreadyLiked, newCount);
    }

    private CommentResponse toResponse(Comment comment, long likesCount, boolean likedByCurrentUser) {
        return new CommentResponse(
                comment.getId(),
                comment.getUser().getUsername(),
                comment.getUser().getPhotoUrl(),
                comment.getContent(),
                comment.getCreationDate(),
                likesCount,
                likedByCurrentUser
        );
    }
}
