package com.trainhub.backend.dto.request;

import com.trainhub.backend.enums.TrainingTag;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para la solicitud de creación de un check-in de entrenamiento.
 */
public class NewCheckinRequest {

    @NotNull private TrainingTag trainingTag;

    private String description;
    private String mateUsername;
    /** Id opcional de un post BOX_WOD del mismo box. */
    private Integer wodPostId;

    public NewCheckinRequest() {}

    public TrainingTag getTrainingTag() { return trainingTag; }
    public void setTrainingTag(TrainingTag trainingTag) { this.trainingTag = trainingTag; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMateUsername() { return mateUsername; }
    public void setMateUsername(String mateUsername) { this.mateUsername = mateUsername; }

    public Integer getWodPostId() { return wodPostId; }
    public void setWodPostId(Integer wodPostId) { this.wodPostId = wodPostId; }
}
