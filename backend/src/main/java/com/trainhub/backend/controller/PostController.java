package com.trainhub.backend.controller;

import com.trainhub.backend.dto.request.NewBoxPostRequest;
import com.trainhub.backend.dto.request.NewCheckinRequest;
import com.trainhub.backend.dto.response.BoxWodSummaryResponse;
import com.trainhub.backend.security.UserPrincipal;
import com.trainhub.backend.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de posts.
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Crea un check-in de entrenamiento para el usuario autenticado.
     *
     * @param userPrincipal usuario autenticado (extraído del token JWT)
     * @param request       datos del check-in
     * @return 201 Created sin cuerpo
     */
    @PostMapping("/checkin")
    public ResponseEntity<Void> createCheckin(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody NewCheckinRequest request) {

        Integer userId = userPrincipal.getUser().getId();
        postService.createCheckin(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Lista WODs recientes del box del usuario (selector de check-in).
     *
     * @param userPrincipal usuario autenticado
     * @return lista de resúmenes de WOD
     */
    @GetMapping("/box/wods")
    public ResponseEntity<List<BoxWodSummaryResponse>> listRecentBoxWods(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer userId = userPrincipal.getUser().getId();
        return ResponseEntity.ok(postService.listRecentBoxWods(userId));
    }

    /**
     * Crea contenido publicado por el box del administrador autenticado.
     *
     * @param userPrincipal administrador autenticado (extraído del token JWT)
     * @param request       datos del contenido de box
     * @return 201 Created sin cuerpo
     */
    @PostMapping("/box")
    public ResponseEntity<Void> createBoxPost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody NewBoxPostRequest request) {

        Integer userId = userPrincipal.getUser().getId();
        postService.createBoxPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
