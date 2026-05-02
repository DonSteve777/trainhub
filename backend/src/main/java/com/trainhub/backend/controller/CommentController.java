package com.trainhub.backend.controller;

import com.trainhub.backend.dto.request.NewCommentRequest;
import com.trainhub.backend.dto.response.CommentResponse;
import com.trainhub.backend.dto.response.LikeToggleResponse;
import com.trainhub.backend.security.UserPrincipal;
import com.trainhub.backend.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de comentarios de un post.
 *
 * GET    /api/posts/{postId}/comments                  → lista los comentarios del post
 * POST   /api/posts/{postId}/comments                  → añade un comentario al post
 * POST   /api/posts/{postId}/comments/{commentId}/likes → da/quita like a un comentario
 */
@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Devuelve los comentarios del post indicado, ordenados cronológicamente.
     * Incluye el conteo de likes y si el usuario autenticado ya dio like a cada uno.
     *
     * @param postId        id del post
     * @param userPrincipal usuario autenticado (puede ser null si el endpoint es público)
     * @return lista de CommentResponse
     */
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Integer postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer userId = userPrincipal != null ? userPrincipal.getUser().getId() : null;
        return ResponseEntity.ok(commentService.getComments(postId, userId));
    }

    /**
     * Añade un comentario al post indicado por el usuario autenticado.
     *
     * @param postId        id del post
     * @param userPrincipal usuario autenticado (extraído del token JWT)
     * @param request       cuerpo de la petición con el contenido del comentario
     * @return 201 Created con el comentario creado
     */
    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Integer postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody NewCommentRequest request) {

        Integer userId = userPrincipal.getUser().getId();
        CommentResponse response = commentService.addComment(postId, userId, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Da o quita el like del usuario autenticado sobre el comentario indicado.
     *
     * @param commentId     id del comentario
     * @param userPrincipal usuario autenticado (extraído del token JWT)
     * @return nuevo estado del like y conteo actualizado
     */
    @PostMapping("/{commentId}/likes")
    public ResponseEntity<LikeToggleResponse> toggleLike(
            @PathVariable Integer postId,
            @PathVariable Integer commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer userId = userPrincipal.getUser().getId();
        LikeToggleResponse response = commentService.toggleLike(commentId, userId);
        return ResponseEntity.ok(response);
    }
}
