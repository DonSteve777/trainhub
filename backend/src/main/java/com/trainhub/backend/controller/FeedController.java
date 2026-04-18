package com.trainhub.backend.controller;

import com.trainhub.backend.dto.response.FeedPostResponse;
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
}
