package com.trainhub.backend.dto.response;

/**
 * Preview de participante en un objetivo (feed).
 */
public class GoalFriendPreviewResponse {

    private Integer userId;
    private String username;
    private String photoUrl;

    public GoalFriendPreviewResponse() {}

    public GoalFriendPreviewResponse(Integer userId, String username, String photoUrl) {
        this.userId = userId;
        this.username = username;
        this.photoUrl = photoUrl;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
