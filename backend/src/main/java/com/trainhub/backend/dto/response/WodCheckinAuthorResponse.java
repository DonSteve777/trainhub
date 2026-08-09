package com.trainhub.backend.dto.response;

import java.time.LocalDateTime;

/**
 * Autor de un check-in vinculado a un WOD (muro del WOD).
 */
public class WodCheckinAuthorResponse {

    private Integer userId;
    private String username;
    private String photoUrl;
    private LocalDateTime checkedInAt;

    public WodCheckinAuthorResponse() {}

    public WodCheckinAuthorResponse(Integer userId, String username, String photoUrl) {
        this.userId = userId;
        this.username = username;
        this.photoUrl = photoUrl;
    }

    public WodCheckinAuthorResponse(
            Integer userId,
            String username,
            String photoUrl,
            LocalDateTime checkedInAt
    ) {
        this.userId = userId;
        this.username = username;
        this.photoUrl = photoUrl;
        this.checkedInAt = checkedInAt;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(LocalDateTime checkedInAt) { this.checkedInAt = checkedInAt; }
}
