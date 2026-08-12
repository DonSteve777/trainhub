package com.trainhub.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO para registrar una marca en un objetivo activo.
 */
public class NewGoalMarkRequest {

    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "0", inclusive = true, message = "El valor debe ser mayor o igual a 0")
    private BigDecimal value;

    @Size(max = 280, message = "El comentario no puede superar 280 caracteres")
    private String note;

    public NewGoalMarkRequest() {}

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
