package com.trainhub.backend.model;

import com.trainhub.backend.enums.FriendshipStatus;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una relación de amistad entre dos usuarios.
 * Mapeada a la tabla "friendships".
 * Una sola fila por pareja, con la restricción userAId < userBId.
 */
@Entity
@Table(name = "friendships")
public class Friendship {

    @EmbeddedId
    private FriendshipId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private FriendshipStatus status;

    @Column(name = "requester_id", nullable = false)
    private Integer requesterId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Friendship() {}

    public Friendship(FriendshipId id, FriendshipStatus status, Integer requesterId, LocalDateTime createdAt) {
        this.id = id;
        this.status = status;
        this.requesterId = requesterId;
        this.createdAt = createdAt;
    }

    public FriendshipId getId() { return id; }
    public void setId(FriendshipId id) { this.id = id; }

    public FriendshipStatus getStatus() { return status; }
    public void setStatus(FriendshipStatus status) { this.status = status; }

    public Integer getRequesterId() { return requesterId; }
    public void setRequesterId(Integer requesterId) { this.requesterId = requesterId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
