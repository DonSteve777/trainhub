package com.trainhub.backend.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Entidad que representa la participación de un usuario en un objetivo.
 * Mapeada a la tabla "goal_participants".
 */
@Entity
@Table(name = "goal_participants")
public class GoalParticipant {

    @EmbeddedId
    private GoalParticipantId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("goalId")
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_owner", nullable = false)
    private boolean owner = false;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "ends_at", nullable = false)
    private OffsetDateTime endsAt;

    public GoalParticipant() {}

    public GoalParticipant(Goal goal, User user, boolean owner,
                           OffsetDateTime startedAt, OffsetDateTime endsAt) {
        this.id = new GoalParticipantId(goal.getId(), user.getId());
        this.goal = goal;
        this.user = user;
        this.owner = owner;
        this.startedAt = startedAt;
        this.endsAt = endsAt;
    }

    public GoalParticipantId getId() { return id; }
    public void setId(GoalParticipantId id) { this.id = id; }

    public Goal getGoal() { return goal; }
    public void setGoal(Goal goal) { this.goal = goal; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public boolean isOwner() { return owner; }
    public void setOwner(boolean owner) { this.owner = owner; }

    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }

    public OffsetDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(OffsetDateTime endsAt) { this.endsAt = endsAt; }
}
