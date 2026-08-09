package com.trainhub.backend.controller;

import com.trainhub.backend.dto.response.FeedPostResponse;
import com.trainhub.backend.dto.response.LikeToggleResponse;
import com.trainhub.backend.dto.response.ParticipationToggleResponse;
import com.trainhub.backend.dto.response.WodCheckinAuthorResponse;
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
        Integer boxId = userPrincipal.getUser().getBox() != null
                ? userPrincipal.getUser().getBox().getId()
                : null;

        List<FeedPostResponse> posts = (cursorDate != null && cursorId != null)
                ? feedService.getNextPage(userId, boxId, cursorDate, cursorId, size)
                : feedService.getFirstPage(userId, boxId, size);

        return ResponseEntity.ok(posts);
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

    /**
     * Apunta o desapunta al usuario autenticado del reto o WOD de box indicado (toggle).
     *
     * @return nuevo estado de participación y conteo actualizado
     */
    @PostMapping("/posts/{postId}/join")
    public ResponseEntity<ParticipationToggleResponse> toggleParticipation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer postId) {

        Integer userId = userPrincipal.getUser().getId();
        ParticipationToggleResponse response = feedService.toggleParticipation(postId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista de usuarios apuntados al WOD indicado.
     */
    @GetMapping("/posts/{postId}/wod-participants")
    public ResponseEntity<List<WodCheckinAuthorResponse>> getWodParticipants(
            @PathVariable Integer postId) {

        return ResponseEntity.ok(feedService.getWodParticipants(postId));
    }
}
