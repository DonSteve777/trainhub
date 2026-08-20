package com.trainhub.backend.dto.request;

import com.trainhub.backend.enums.TrainingTag;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO para la solicitud de creación de un check-in de entrenamiento.
 */
public class NewCheckinRequest {

    @NotNull private TrainingTag trainingTag;

    private String description;
    /** Id opcional de un post BOX_WOD del mismo box. */
    private Integer wodPostId;
    /**
     * Día del entrenamiento (aparece en la constancia semanal).
     * Si es null, se usa la fecha de hoy.
     */
    private LocalDate trainingDate;

    public NewCheckinRequest() {}

    public TrainingTag getTrainingTag() { return trainingTag; }
    public void setTrainingTag(TrainingTag trainingTag) { this.trainingTag = trainingTag; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getWodPostId() { return wodPostId; }
    public void setWodPostId(Integer wodPostId) { this.wodPostId = wodPostId; }

    public LocalDate getTrainingDate() { return trainingDate; }
    public void setTrainingDate(LocalDate trainingDate) { this.trainingDate = trainingDate; }
}
