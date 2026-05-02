package com.trainhub.backend.dto.response;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para un comentario de un post.
 */
public class CommentResponse {

    private Integer id;
    private String username;
    private String avatarUrl;
    private String content;
    private LocalDateTime creationDate;
    private long likesCount;
    private boolean likedByCurrentUser;

    public CommentResponse(Integer id, String username, String avatarUrl,
                           String content, LocalDateTime creationDate,
                           long likesCount, boolean likedByCurrentUser) {
        this.id = id;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.content = content;
        this.creationDate = creationDate;
        this.likesCount = likesCount;
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getContent() { return content; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public long getLikesCount() { return likesCount; }
    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
}
