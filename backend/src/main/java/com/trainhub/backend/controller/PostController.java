package com.trainhub.backend.controller;

import com.trainhub.backend.dto.request.NewPostRequest;
import com.trainhub.backend.security.UserPrincipal;
import com.trainhub.backend.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de posts (entrenamientos).
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Crea un nuevo post para el usuario autenticado.
     *
     * @param userPrincipal usuario autenticado (extraído del token JWT)
     * @param request       datos del entrenamiento con los 16 tiempos parciales
     * @return 201 Created sin cuerpo
     */
    @PostMapping
    public ResponseEntity<Void> createPost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody NewPostRequest request) {

        Integer userId = userPrincipal.getUser().getId();
        postService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
