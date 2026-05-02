package com.trainhub.backend.dto.response;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa un usuario que ha dado like a un post,
 * junto con la fecha en que lo hizo.
 */
public class LikerResponse {

    private Integer userId;
    private String username;
    private String photoUrl;
    private LocalDateTime likedAt;

    public LikerResponse(Integer userId, String username, String photoUrl, LocalDateTime likedAt) {
        this.userId = userId;
        this.username = username;
        this.photoUrl = photoUrl;
        this.likedAt = likedAt;
    }

    public Integer getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPhotoUrl() { return photoUrl; }
    public LocalDateTime getLikedAt() { return likedAt; }
}
