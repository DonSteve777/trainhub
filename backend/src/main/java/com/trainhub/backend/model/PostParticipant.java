package com.trainhub.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa la participación de un usuario en un post
 * (por ejemplo, en un reto de box). Mapeada a la tabla "post_participants".
 */
@Entity
@Table(name = "post_participants")
public class PostParticipant {

    @EmbeddedId
    private PostParticipantId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    public PostParticipant() {}

    public PostParticipant(Post post, User user, LocalDateTime joinedAt) {
        this.id = new PostParticipantId(post.getId(), user.getId());
        this.post = post;
        this.user = user;
        this.joinedAt = joinedAt;
    }

    public PostParticipantId getId() { return id; }
    public void setId(PostParticipantId id) { this.id = id; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
