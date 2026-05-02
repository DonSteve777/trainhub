package com.trainhub.backend.service;

import com.trainhub.backend.dto.response.CommentResponse;
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
import java.util.List;

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
     *
     * @param postId id del post
     * @return lista de CommentResponse
     */
    public List<CommentResponse> getComments(Integer postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post no encontrado");
        }

        return commentRepository.findByPostId(postId)
                .stream()
                .map(this::toResponse)
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

        return toResponse(saved);
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getUser().getUsername(),
                comment.getUser().getPhotoUrl(),
                comment.getContent(),
                comment.getCreationDate()
        );
    }
}
