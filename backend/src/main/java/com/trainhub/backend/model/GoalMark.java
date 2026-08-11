package com.trainhub.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entidad que representa una marca registrada por un usuario en un objetivo.
 * La unidad vive en el objetivo, no en la marca. Tiempos en segundos.
 * Mapeada a la tabla "goal_marks".
 */
@Entity
@Table(name = "goal_marks")
public class GoalMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "value", nullable = false, precision = 19, scale = 4)
    private BigDecimal value;

    @Column(name = "note", length = 280)
    private String note;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    public GoalMark() {}

    public GoalMark(Goal goal, User user, BigDecimal value, String note, OffsetDateTime recordedAt) {
        this.goal = goal;
        this.user = user;
        this.value = value;
        this.note = note;
        this.recordedAt = recordedAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Goal getGoal() { return goal; }
    public void setGoal(Goal goal) { this.goal = goal; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public OffsetDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(OffsetDateTime recordedAt) { this.recordedAt = recordedAt; }
}
