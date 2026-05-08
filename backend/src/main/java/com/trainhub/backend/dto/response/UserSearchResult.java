package com.trainhub.backend.dto.response;

/**
 * DTO de respuesta para el buscador de usuarios (autocomplete).
 */
public class UserSearchResult {

    private Integer id;
    private String username;
    private String photoUrl;
    private String friendshipStatus;

    public UserSearchResult(Integer id, String username, String photoUrl, String friendshipStatus) {
        this.id = id;
        this.username = username;
        this.photoUrl = photoUrl;
        this.friendshipStatus = friendshipStatus;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getFriendshipStatus() { return friendshipStatus; }
    public void setFriendshipStatus(String friendshipStatus) { this.friendshipStatus = friendshipStatus; }
}
