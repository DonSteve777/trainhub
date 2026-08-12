package com.trainhub.backend.dto.request;

import com.trainhub.backend.enums.GoalDirection;
import com.trainhub.backend.enums.GoalUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO para crear un objetivo medible.
 */
public class NewGoalRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede superar 150 caracteres")
    private String title;

    @Size(max = 280, message = "La descripción no puede superar 280 caracteres")
    private String description;

    @NotBlank(message = "La etiqueta métrica es obligatoria")
    @Size(max = 100, message = "La etiqueta métrica no puede superar 100 caracteres")
    private String metricLabel;

    @NotNull(message = "La meta es obligatoria")
    @DecimalMin(value = "0", inclusive = true, message = "La meta debe ser mayor o igual a 0")
    private BigDecimal targetValue;

    @NotNull(message = "La unidad es obligatoria")
    private GoalUnit unit;

    @NotNull(message = "La dirección es obligatoria")
    private GoalDirection direction;

    @NotNull(message = "El plazo en semanas es obligatorio")
    @Min(value = 1, message = "El plazo mínimo es 1 semana")
    @Max(value = 104, message = "El plazo máximo es 104 semanas")
    private Integer weeks;

    public NewGoalRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMetricLabel() { return metricLabel; }
    public void setMetricLabel(String metricLabel) { this.metricLabel = metricLabel; }

    public BigDecimal getTargetValue() { return targetValue; }
    public void setTargetValue(BigDecimal targetValue) { this.targetValue = targetValue; }

    public GoalUnit getUnit() { return unit; }
    public void setUnit(GoalUnit unit) { this.unit = unit; }

    public GoalDirection getDirection() { return direction; }
    public void setDirection(GoalDirection direction) { this.direction = direction; }

    public Integer getWeeks() { return weeks; }
    public void setWeeks(Integer weeks) { this.weeks = weeks; }
}
