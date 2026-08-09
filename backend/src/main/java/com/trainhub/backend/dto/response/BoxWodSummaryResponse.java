package com.trainhub.backend.dto.response;

import com.trainhub.backend.enums.TrainingTag;
import java.time.LocalDateTime;

/**
 * Resumen de un WOD del box para el selector de check-in.
 */
public class BoxWodSummaryResponse {

    private Integer id;
    private String title;
    private String description;
    private TrainingTag trainingTag;
    private LocalDateTime creationDate;

    public BoxWodSummaryResponse() {}

    public BoxWodSummaryResponse(
            Integer id,
            String title,
            String description,
            TrainingTag trainingTag,
            LocalDateTime creationDate
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.trainingTag = trainingTag;
        this.creationDate = creationDate;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TrainingTag getTrainingTag() { return trainingTag; }
    public void setTrainingTag(TrainingTag trainingTag) { this.trainingTag = trainingTag; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
}
