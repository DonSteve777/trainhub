package com.trainhub.backend.model;

import com.trainhub.backend.enums.FriendshipStatus;

import jakarta.persistence.*;

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

    public Friendship() {}

    public Friendship(FriendshipId id, FriendshipStatus status) {
        this.id = id;
        this.status = status;
    }

    public FriendshipId getId() { return id; }
    public void setId(FriendshipId id) { this.id = id; }

    public FriendshipStatus getStatus() { return status; }
    public void setStatus(FriendshipStatus status) { this.status = status; }
}
