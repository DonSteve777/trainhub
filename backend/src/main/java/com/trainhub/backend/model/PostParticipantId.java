package com.trainhub.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Clave primaria compuesta de la tabla "post_participants".
 * Identifica de forma única la participación de un usuario en un post.
 */
@Embeddable
public class PostParticipantId implements Serializable {

    @Column(name = "post_id", nullable = false)
    private Integer postId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    public PostParticipantId() {}

    public PostParticipantId(Integer postId, Integer userId) {
        this.postId = postId;
        this.userId = userId;
    }

    public Integer getPostId() { return postId; }
    public void setPostId(Integer postId) { this.postId = postId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostParticipantId)) return false;
        PostParticipantId that = (PostParticipantId) o;
        return Objects.equals(postId, that.postId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, userId);
    }
}
