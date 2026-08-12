package com.trainhub.backend.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Marca de un participante en un objetivo.
 */
public class GoalMarkResponse {

    private Integer id;
    private Integer userId;
    private BigDecimal value;
    private String note;
    private OffsetDateTime recordedAt;

    public GoalMarkResponse(Integer id, Integer userId, BigDecimal value,
                            String note, OffsetDateTime recordedAt) {
        this.id = id;
        this.userId = userId;
        this.value = value;
        this.note = note;
        this.recordedAt = recordedAt;
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public BigDecimal getValue() { return value; }
    public String getNote() { return note; }
    public OffsetDateTime getRecordedAt() { return recordedAt; }
}
