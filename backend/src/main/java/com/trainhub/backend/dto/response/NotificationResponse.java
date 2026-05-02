package com.trainhub.backend.dto.response;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa una notificación de like sobre un post del usuario.
 * Agrupa todos los likes de un mismo post, mostrando datos del liker más reciente.
 */
public class NotificationResponse {

    private Integer postId;
    private LocalDateTime postCreationDate;
    private String lastLikerUsername;
    private String lastLikerPhotoUrl;
    private LocalDateTime lastLikedAt;
    private int totalLikers;
    private boolean unread;

    public NotificationResponse(Integer postId, LocalDateTime postCreationDate,
                                String lastLikerUsername, String lastLikerPhotoUrl,
                                LocalDateTime lastLikedAt, int totalLikers, boolean unread) {
        this.postId = postId;
        this.postCreationDate = postCreationDate;
        this.lastLikerUsername = lastLikerUsername;
        this.lastLikerPhotoUrl = lastLikerPhotoUrl;
        this.lastLikedAt = lastLikedAt;
        this.totalLikers = totalLikers;
        this.unread = unread;
    }

    public Integer getPostId() { return postId; }
    public LocalDateTime getPostCreationDate() { return postCreationDate; }
    public String getLastLikerUsername() { return lastLikerUsername; }
    public String getLastLikerPhotoUrl() { return lastLikerPhotoUrl; }
    public LocalDateTime getLastLikedAt() { return lastLikedAt; }
    public int getTotalLikers() { return totalLikers; }
    public boolean isUnread() { return unread; }
}
