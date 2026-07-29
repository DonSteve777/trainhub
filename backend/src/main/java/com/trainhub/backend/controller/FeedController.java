package com.trainhub.backend.controller;

import com.trainhub.backend.dto.response.FeedHistoryResponse;
import com.trainhub.backend.dto.response.FeedPostResponse;
import com.trainhub.backend.dto.response.LikeToggleResponse;
import com.trainhub.backend.enums.PostCategory;
import com.trainhub.backend.security.UserPrincipal;
import com.trainhub.backend.service.FeedService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para el feed de entrenamientos de amigos.
 *
 * Expone un único endpoint GET /api/feed con paginación keyset:
 *   - Sin cursor  → primera página  (llama a findFeedFirstPage)
 *   - Con cursor  → página siguiente (llama a findFeedWithCursor)
 */
@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    /**
     * Devuelve los posts del feed del usuario autenticado.
     *
     * @param userPrincipal usuario autenticado (extraído del token JWT)
     * @param size          número máximo de posts a devolver (defecto 20)
     * @param cursorDate    fecha del último post recibido (para paginar)
     * @param cursorId      id del último post recibido (para paginar)
     * @return lista de FeedPostResponse ordenada por fecha descendente
     */
    @GetMapping
    public ResponseEntity<List<FeedPostResponse>> getFeed(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorDate,
            @RequestParam(required = false) Integer cursorId) {

        Integer userId = userPrincipal.getUser().getId();

        List<FeedPostResponse> posts = (cursorDate != null && cursorId != null)
                ? feedService.getNextPage(userId, cursorDate, cursorId, size)
                : feedService.getFirstPage(userId, size);

        return ResponseEntity.ok(posts);
    }

    /**
     * Devuelve los históricos de tiempos de los posts de los amigos del usuario
     * autenticado, filtrados por categoría y sin límite de fecha.
     *
     * @param category categoría del post (INDIVIDUAL_MALE, INDIVIDUAL_FEMALE, etc.)
     */
    @GetMapping("/history")
    public ResponseEntity<FeedHistoryResponse> getHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam PostCategory category) {
        Integer userId = userPrincipal.getUser().getId();
        return ResponseEntity.ok(feedService.getHistory(userId, category));
    }

    /**
     * Devuelve los históricos de tiempos de todos los usuarios de la BD,
     * filtrados por categoría, sin límite de fecha.
     *
     * @param category categoría del post (INDIVIDUAL_MALE, INDIVIDUAL_FEMALE, etc.)
     */
    @GetMapping("/global-history")
    public ResponseEntity<FeedHistoryResponse> getGlobalHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam PostCategory category) {
        return ResponseEntity.ok(feedService.getGlobalHistory(category));
    }

    /**
     * Da o quita like al post indicado para el usuario autenticado (toggle).
     * Si el usuario aún no había dado like, lo crea. Si ya lo había dado, lo elimina.
     *
     * @return nuevo estado del like y conteo actualizado
     */
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<LikeToggleResponse> toggleLike(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer postId) {

        Integer userId = userPrincipal.getUser().getId();
        LikeToggleResponse response = feedService.toggleLike(postId, userId);
        return ResponseEntity.ok(response);
    }
}
