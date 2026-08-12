package com.trainhub.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Objetivo con participantes y marcas (vista /objetivos).
 * unit/direction en minúsculas para alinear con el contrato del frontend.
 */
public class GoalResponse {

    private Integer id;
    private String title;
    private String description;
    private String metricLabel;
    private BigDecimal targetValue;
    private String unit;
    private String direction;
    private Integer weeks;
    private OffsetDateTime deadline;
    private String status;
    private LocalDateTime createdAt;
    private List<GoalParticipantResponse> participants;

    public GoalResponse(Integer id, String title, String description, String metricLabel,
                        BigDecimal targetValue, String unit, String direction, Integer weeks,
                        OffsetDateTime deadline, String status, LocalDateTime createdAt,
                        List<GoalParticipantResponse> participants) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.metricLabel = metricLabel;
        this.targetValue = targetValue;
        this.unit = unit;
        this.direction = direction;
        this.weeks = weeks;
        this.deadline = deadline;
        this.status = status;
        this.createdAt = createdAt;
        this.participants = participants;
    }

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getMetricLabel() { return metricLabel; }
    public BigDecimal getTargetValue() { return targetValue; }
    public String getUnit() { return unit; }
    public String getDirection() { return direction; }
    public Integer getWeeks() { return weeks; }
    public OffsetDateTime getDeadline() { return deadline; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<GoalParticipantResponse> getParticipants() { return participants; }
}
