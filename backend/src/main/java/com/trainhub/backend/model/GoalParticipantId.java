package com.trainhub.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Clave primaria compuesta de la tabla "goal_participants".
 * Identifica de forma única la participación de un usuario en un objetivo.
 */
@Embeddable
public class GoalParticipantId implements Serializable {

    @Column(name = "goal_id", nullable = false)
    private Integer goalId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    public GoalParticipantId() {}

    public GoalParticipantId(Integer goalId, Integer userId) {
        this.goalId = goalId;
        this.userId = userId;
    }

    public Integer getGoalId() { return goalId; }
    public void setGoalId(Integer goalId) { this.goalId = goalId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoalParticipantId)) return false;
        GoalParticipantId that = (GoalParticipantId) o;
        return Objects.equals(goalId, that.goalId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(goalId, userId);
    }
}
