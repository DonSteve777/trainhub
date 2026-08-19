package com.trainhub.backend.dto.response;

/**
 * Preview de un amigo (para la vista de "Ver amigos" del usuario actual).
 */
public class FriendPreviewResponse {

    private Integer id;
    private String username;
    private String photoUrl;

    public FriendPreviewResponse() {}

    public FriendPreviewResponse(Integer id, String username, String photoUrl) {
        this.id = id;
        this.username = username;
        this.photoUrl = photoUrl;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}

