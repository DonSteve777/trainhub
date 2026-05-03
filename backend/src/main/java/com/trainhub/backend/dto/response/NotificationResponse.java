package com.trainhub.backend.dto.response;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa una notificación (like o comentario) sobre un post del usuario.
 * Agrupa todas las acciones del mismo tipo sobre el mismo post, mostrando datos del actor más reciente.
 */
public class NotificationResponse {

    /** Tipo de notificación: "LIKE" o "COMMENT". */
    private String type;

    private Integer postId;
    private LocalDateTime postCreationDate;
    private String lastActorUsername;
    private String lastActorPhotoUrl;
    private LocalDateTime lastActionAt;
    private int totalCount;
    private boolean unread;

    public NotificationResponse(String type, Integer postId, LocalDateTime postCreationDate,
                                String lastActorUsername, String lastActorPhotoUrl,
                                LocalDateTime lastActionAt, int totalCount, boolean unread) {
        this.type = type;
        this.postId = postId;
        this.postCreationDate = postCreationDate;
        this.lastActorUsername = lastActorUsername;
        this.lastActorPhotoUrl = lastActorPhotoUrl;
        this.lastActionAt = lastActionAt;
        this.totalCount = totalCount;
        this.unread = unread;
    }

    public String getType() { return type; }
    public Integer getPostId() { return postId; }
    public LocalDateTime getPostCreationDate() { return postCreationDate; }
    public String getLastActorUsername() { return lastActorUsername; }
    public String getLastActorPhotoUrl() { return lastActorPhotoUrl; }
    public LocalDateTime getLastActionAt() { return lastActionAt; }
    public int getTotalCount() { return totalCount; }
    public boolean isUnread() { return unread; }
}
