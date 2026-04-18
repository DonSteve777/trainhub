package com.trainhub.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Clave primaria compuesta de la tabla "friendships".
 * Siempre se cumple userAId < userBId (garantizado por constraint en BD).
 */
@Embeddable
public class FriendshipId implements Serializable {

    @Column(name = "user_a_id", nullable = false)
    private Integer userAId;

    @Column(name = "user_b_id", nullable = false)
    private Integer userBId;

    public FriendshipId() {}

    public FriendshipId(Integer userAId, Integer userBId) {
        this.userAId = userAId;
        this.userBId = userBId;
    }

    public Integer getUserAId() { return userAId; }
    public void setUserAId(Integer userAId) { this.userAId = userAId; }

    public Integer getUserBId() { return userBId; }
    public void setUserBId(Integer userBId) { this.userBId = userBId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendshipId)) return false;
        FriendshipId that = (FriendshipId) o;
        return Objects.equals(userAId, that.userAId) && Objects.equals(userBId, that.userBId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userAId, userBId);
    }
}
