package com.trainhub.backend.controller;

import com.trainhub.backend.dto.response.LikerResponse;
import com.trainhub.backend.dto.response.NotificationResponse;
import com.trainhub.backend.dto.response.UnreadCountResponse;
import com.trainhub.backend.security.UserPrincipal;
import com.trainhub.backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para las notificaciones de likes.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Devuelve todas las notificaciones del usuario autenticado (una por post con likes).
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(notificationService.getNotifications(principal.getId()));
    }

    /**
     * Devuelve el número de notificaciones no leídas.
     */
    @GetMapping("/count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(new UnreadCountResponse(
                notificationService.getUnreadCount(principal.getId())
        ));
    }

    /**
     * Marca todas las notificaciones como vistas actualizando la marca de agua.
     */
    @PostMapping("/mark-seen")
    public ResponseEntity<Void> markSeen(
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markSeen(principal.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Devuelve la lista de usuarios que dieron like a un post concreto.
     */
    @GetMapping("/posts/{postId}/likers")
    public ResponseEntity<List<LikerResponse>> getPostLikers(
            @PathVariable Integer postId) {
        return ResponseEntity.ok(notificationService.getPostLikers(postId));
    }
}
